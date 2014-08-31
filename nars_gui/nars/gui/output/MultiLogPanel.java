package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
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
import nars.io.Output;

/**
 * TODO queue outputs in non-displayed SwingLogPanel's into ArrayDeque without involving
 * any display methods
 * 
 * @author me
 */
public class MultiLogPanel extends JSplitPane implements Output {

    /** extension of SwingLogText with functionality specific to MultiLogPanel */
    public class SwingLogTextM extends SwingLogText  implements ChangeListener {
    
        
        public final JCheckBox enabler;
        final Object category;
        JPanel displayed = null;

        public SwingLogTextM(Object category) {
            super(nar);
            this.category = category;
            this.enabler = new JCheckBox(category.toString());
            
            enabler.addChangeListener(this);
            
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
    private final JPanel content;
    private final JPanel side;
    private final DefaultListModel categoriesListModel;
    private final JCheckBoxList categoriesList;

    public MultiLogPanel(NARControls c) {
        super(JSplitPane.HORIZONTAL_SPLIT);
        
        categoriesListModel = new DefaultListModel();
        categoriesList = new JCheckBoxList(categoriesListModel);
        

        side = new JPanel(new BorderLayout());
        side.add(categoriesList, BorderLayout.CENTER);
        add(new JScrollPane(side), 0);

        content = new JPanel(new GridLayout(1, 0));
        add(new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), 1);

        setDividerLocation(0.14);

        this.nar = c.nar;
        nar.addOutput(this);

        rootTaskPanel = new SwingLogTextM("Root");
        
        
        getLogPanel("Root").show();
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
            p.print(channel, o, false, nar);
    }

    public SwingLogText getLogPanel(Object category) {
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
        SwingLogText p = getLogPanel(category);
        int columnWidth = 400;
        SwingLogPanel.setConsoleStyle(p, true);

        JPanel x = new JPanel(new BorderLayout());
        x.add(new JButton(title), BorderLayout.NORTH);
        
        //http://stackoverflow.com/questions/4702891/toggling-text-wrap-in-a-jtextpane        
        JPanel ioTextWrap = new JPanel(new BorderLayout());
        ioTextWrap.add(p);        
        x.add(new JScrollPane(ioTextWrap), BorderLayout.CENTER);

        x.setMinimumSize(new Dimension(columnWidth, 0));
        x.setMaximumSize(new Dimension(columnWidth, Integer.MAX_VALUE / 2));
        x.setPreferredSize(new Dimension(columnWidth, 0));
        x.validate();

        content.add(x);
        
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
        content.remove(p);
        validate();
        repaint();
    }
}
