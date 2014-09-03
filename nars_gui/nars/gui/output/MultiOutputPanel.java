package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import nars.core.NAR;
import nars.entity.Task;
import nars.gui.NARControls;
import nars.gui.dock.DockingContent;
import nars.gui.dock.DockingRegionRoot;
import nars.io.Output;

/**
 * TODO queue outputs in non-displayed SwingLogPanel's into ArrayDeque without involving
 * any display methods
 * 
 * @author me
 */
public class MultiOutputPanel extends JPanel implements Output, HierarchyListener {

    DockingRegionRoot dock = new DockingRegionRoot();
    
    final long activityDecayPeriodNS = 100 * 1000 * 1000; //100ms
    
    /** extension of SwingLogText with functionality specific to MultiLogPanel */
    public class SwingLogTextM extends SwingLogText  implements ChangeListener {
    
        final float activityIncrement = 0.5f;
        final float activityMomentum = 0.95f;
        
        float activity = 0;
        public final JCheckBox enabler;
        final Object category;
        JPanel displayed = null;
        private final String label;

        public SwingLogTextM(Object category) {
            super(nar);
            this.category = category;
            
            
            if (category instanceof Task) {
                label = ((Task)category).sentence.toString();
            }
            else {
                label = category.toString();
            }
            
            this.enabler = new JCheckBox(label) {

                @Override public void paint(Graphics g) {
                    Color c = new Color(1f, 1f-activity/2f, 1f-activity/2f );
                    g.setColor(c);
                    g.fillRect(0, 0, getWidth(), getHeight());                    
                    super.paint(g);
                }
                
            };
            enabler.setOpaque(false);
            
            enabler.addChangeListener(this);
            
        }

        @Override
        public void out(Class c, Object o) {
            super.out(c, o);
            
            updateActivity(activity + activityIncrement);
        }
        
        public void updateActivity(float newActivity) {
            if (activity!=newActivity) {
                activity = newActivity;
                if (activity > 1.0f) activity = 1.0f;
            }
        }
        
        public void decayActvity() {
            updateActivity(activity * activityMomentum);
        }

        
        @Override
        public void stateChanged(ChangeEvent e) {
            if (enabler.isSelected()) {
                displayed = showCategory(category);                
            }
            else {                
                hideCategory(displayed);
                
                displayed = null;
            }
        }

        public void show() {
            enabler.setSelected(true);
        }
    }
    
    public Map<Object, SwingLogTextM> categories = new HashMap();
    private final SwingLogText rootTaskPanel;
    private final NAR nar;    
    private final JPanel side;
    private final DefaultListModel categoriesListModel;
    private final JCheckBoxList categoriesList;

    public MultiOutputPanel(NARControls c) {
        super(new BorderLayout());

        JMenuBar menu = new JMenuBar();
        add(menu, BorderLayout.NORTH);
        
        JSplitPane innerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        
        
        
        this.nar = c.nar;

        categoriesListModel = new DefaultListModel();
        categoriesList = new JCheckBoxList(categoriesListModel);
        

        side = new JPanel(new BorderLayout());
        side.add(categoriesList, BorderLayout.CENTER);
        
        add(innerPanel, BorderLayout.CENTER);
        
        innerPanel.add(new JScrollPane(side), 0);
        innerPanel.add(dock, 1);
        innerPanel.setDividerLocation(0.25f);

        

        rootTaskPanel = new SwingLogTextM("Root");
        
        
        getLogPanel("Root").show();
    }
    
    protected void onShowing(boolean showing) {
        if (showing) {
            nar.addOutput(this);
        }
        else {
            nar.removeOutput(this);
        }
    }

    
    @Override
    public void output(Class channel, Object o) {
        Object category;
        if (o instanceof Task) {
            Task t = (Task) o;
            category = t.getRootTask();
        } else {
            category = null;
        }
                
        SwingLogText p = getLogPanel(category);
        if (p!=null)
            p.out(channel, o);
        
        decayActivities();
    }

    public SwingLogTextM getLogPanel(Object category) {
        if (category == null) {
            return categories.get("Root");
        } else {
            SwingLogTextM p = categories.get(category);
            if (p == null) {
                
                p = new SwingLogTextM(category);
                JCheckBox jc = p.enabler;
                
                categories.put(category, p);
                                        
                categoriesListModel.addElement(jc);
            }
            return p;
        }
    }

    public JPanel showCategory(Object category) {
        String title = category.toString();
        SwingLogTextM p = getLogPanel(category);
        int columnWidth = 400;
        SwingLogPanel.setConsoleStyle(p, true);

        JPanel x = new JPanel(new BorderLayout());
        
        JMenuBar headerMenu = new JMenuBar();
        JMenu m = new JMenu(p.label);
        m.add(new JMenuItem("Statement List"));
        m.add(new JMenuItem("Log"));
        m.add(new JMenuItem("Concept List"));
        m.add(new JMenuItem("Concept Cloud"));        
        m.add(new JMenuItem("Concepts Network"));
        m.add(new JMenuItem("Statements Network"));
        m.add(new JMenuItem("Truth vs. Confidence"));        
        m.addSeparator();
        m.add(new JButton("Priority +"));
        m.add(new JButton("Priority -"));
        m.addSeparator();
        m.add(new JMenuItem("Close"));
        headerMenu.add(m);
        
                
        x.add(headerMenu, BorderLayout.NORTH);
        
        //http://stackoverflow.com/questions/4702891/toggling-text-wrap-in-a-jtextpane        
        JPanel ioTextWrap = new JPanel(new BorderLayout());
        ioTextWrap.add(p);        
        x.add(new JScrollPane(ioTextWrap), BorderLayout.CENTER);

        x.validate();


        DockingContent cont = new DockingContent("view" + category, title, x);
        dock.getDockingRoot().addDockContent(cont);
        
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                revalidate();
                repaint();
            }
        });
        
        return x;
    }

    /**
     * @author http://stackoverflow.com/questions/19766/how-do-i-make-a-list-with-checkboxes-in-java-swing
     */
    @SuppressWarnings("serial")
    public static class JCheckBoxList extends JList<JCheckBox> {

        protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public JCheckBoxList() {
            setCellRenderer(new CellRenderer());
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int index = locationToIndex(e.getPoint());
                    if (index != -1) {
                        JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                        checkbox.setSelected(!checkbox.isSelected());
                        repaint();
                    }
                }
            });
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        public JCheckBoxList(ListModel<JCheckBox> model) {
            this();
            setModel(model);
        }

        protected class CellRenderer implements ListCellRenderer<JCheckBox> {

            public Component getListCellRendererComponent(
                    JList<? extends JCheckBox> list, JCheckBox value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JCheckBox checkbox = value;

                //Drawing checkbox, change the appearance here
                checkbox.setBackground(isSelected ? getSelectionBackground()
                        : getBackground());
                checkbox.setForeground(isSelected ? getSelectionForeground()
                        : getForeground());
                checkbox.setEnabled(isEnabled());
                checkbox.setFont(getFont());
                checkbox.setFocusPainted(false);
                checkbox.setBorderPainted(true);
                checkbox.setBorder(isSelected ? UIManager
                        .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
                return checkbox;
            }
        }
    }
    
    public void hideCategory(JPanel p) {
        dock.getDockingRoot().remove(p);
        
        validate();
        repaint();
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        addHierarchyListener(this);
    }

    @Override
    public void removeNotify() {
        removeHierarchyListener(this);
        super.removeNotify();
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            boolean showing = isShowing();
            onShowing(showing);
        }
    }    

    long lastDecayed = 0;
    protected void decayActivities() {
        long now = System.nanoTime();
        if (now - lastDecayed > activityDecayPeriodNS) {
            for (SwingLogTextM c : categories.values()) {
                c.decayActvity();
            }
            lastDecayed = now;
            categoriesList.repaint();
        }
    }
    
}
