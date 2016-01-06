//package nars.gui.output;
//
//import automenta.vivisect.swing.dock.DockingContent;
//import automenta.vivisect.swing.dock.DockingRegionRoot;
//import nars.NAR;
//import nars.Video;
//import nars.task.Task;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.HierarchyEvent;
//import java.awt.event.HierarchyListener;
//import java.util.HashMap;
//import java.util.Map;
//
//
///**
// * TODO queue outputs in non-displayed SwingLogPanel's into ArrayDeque without involving
// * any display methods
// *
// * @author me
// */
//public class MultiOutputPanel extends JPanel implements HierarchyListener {
//
//    DockingRegionRoot dock = new DockingRegionRoot();
//
//    final long activityDecayPeriodNS = 100 * 1000 * 1000; //100ms
//
//
//    public Map<Object, MultiModePanel> categories = new HashMap();
//    private final MultiModePanel rootTaskPanel;
//    private final NAR nar;
//    private final JPanel side;
//    private final DefaultListModel categoriesListModel;
//    private final JCategoryList categoriesList;
//
//    public MultiOutputPanel() {
//        super(new BorderLayout());
//
//        JMenuBar menu = new JMenuBar();
//        add(menu, BorderLayout.NORTH);
//
//        JSplitPane innerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//
//
//
//
//        this.nar = null; //c.nar;
//
//        categoriesListModel = new DefaultListModel();
//
//        categoriesList = new JCategoryList(categoriesListModel);
//        categoriesList.setBackground(Color.BLACK);
//
//        side = new JPanel(new BorderLayout());
//        side.add(categoriesList, BorderLayout.CENTER);
//
//        add(innerPanel, BorderLayout.CENTER);
//
//        innerPanel.add(new JScrollPane(side), 0);
//        innerPanel.add(dock, 1);
//        innerPanel.setDividerLocation(0.25f);
//
//
//
//        rootTaskPanel = getModePanel("Root");//new MultiModePanel(nar, "Root");
//
//        showCategory("Root");
//    }
//
//    protected void onShowing(boolean showing) {
////        if (showing) {
////            nar.addOutput(this);
////        }
////        else {
////            nar.removeOutput(this);
////        }
//    }
//
//
//    //TODO use Output instance
//    public void output(Class channel, Object o) {
//        Object category;
//        if (o instanceof Task) {
//            Task t = (Task) o;
//            category = t.getRootTask();
//        } else {
//            category = null;
//        }
//
//        MultiModePanel p = getModePanel(category);
//        if (p!=null)
//            p.output(channel, o);
//
//        decayActivities();
//    }
//
//    public MultiModePanel getModePanel(Object category) {
//        if (category == null) {
//            return categories.get("Root");
//        } else {
//            MultiModePanel p = categories.get(category);
//            if (p == null) {
//
//                p = new MultiModePanel(nar, category);
//                JButton jc = p.newStatusButton();
//
//                jc.addActionListener(new ActionListener() {
//                    @Override public void actionPerformed(ActionEvent e) {
//                        showCategory(category);
//                    }
//                });
//                categories.put(category, p);
//
//                categoriesListModel.addElement(jc);
//            }
//            return p;
//        }
//    }
//
//    public JPanel showCategory(Object category) {
//        String title = category.toString();
//
//        MultiModePanel p = getModePanel(category);
//
//        JMenuBar headerMenu = new JMenuBar();
//        headerMenu.setOpaque(false);
//        headerMenu.setBorder(null);
//        headerMenu.add(p.newMenu());
//
//        //http://stackoverflow.com/questions/4702891/toggling-text-wrap-in-a-jtextpane
//        JPanel ioTextWrap = new JPanel(new BorderLayout());
//        ioTextWrap.add(p);
//
//        JPanel x = new JPanel(new BorderLayout());
//        x.add(new JScrollPane(ioTextWrap), BorderLayout.CENTER);
//        x.validate();
//
//
//        DockingContent cont = new DockingContent("view" + category, title, x);
//        dock.addRootContent(cont);
//
//        cont.getTab().setLabel(p.getLabel());
//        cont.getTab().setFont(Video.fontMono(15));
//        cont.getTab().setMenuButton(headerMenu);
//
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                revalidate();
//                repaint();
//            }
//        });
//
//        return x;
//    }
//
//
//    public void hideCategory(JPanel p) {
//        dock.getDockingRoot().remove(p);
//
//        validate();
//        repaint();
//    }
//
//    @Override
//    public void addNotify() {
//        super.addNotify();
//        addHierarchyListener(this);
//    }
//
//    @Override
//    public void removeNotify() {
//        removeHierarchyListener(this);
//        super.removeNotify();
//    }
//
//    @Override
//    public void hierarchyChanged(HierarchyEvent e) {
//        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
//            boolean showing = isShowing();
//            onShowing(showing);
//        }
//    }
//
//    long lastDecayed = 0;
//    protected void decayActivities() {
//        long now = System.nanoTime();
//        if (now - lastDecayed > activityDecayPeriodNS) {
//            for (MultiModePanel c : categories.values()) {
//                c.decayActvity();
//            }
//            lastDecayed = now;
//            categoriesList.repaint();
//        }
//    }
//
// }
