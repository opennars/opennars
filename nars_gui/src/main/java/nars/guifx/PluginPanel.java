package nars.guifx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import nars.Global;
import nars.NAR;
import nars.guifx.demo.NARide;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static javafx.application.Platform.runLater;

/**
 * Manages the activated set of plugins in a NAR, and a menu for adding additional ones
 * and presets of them.
 */
public class PluginPanel extends VBox {

    private final NAR nar;
    private final NARide ide;

    double itemSpacing = 8.0;

    public PluginPanel(NARide ide) {

        setSpacing(itemSpacing);

        this.ide = ide;
        nar = ide.nar;


        nar.onEachFrame((n) -> update());
        update();


    }


    public void update() {

        List<Node> toAdd = Global.newArrayList();
        nar.memory.getSingletons().forEach((k, v) -> toAdd.add(node(k, v)));

        //TODO use faster comparison method


        runLater(() -> {
            if (!getChildren().equals(toAdd)) {
                getChildren().setAll(toAdd);
                layout();
            }
        });


//        menu.add(new JLabel(" + "));
//
//        TreeMap<String, JMenu> menus = new TreeMap();
//        try {
//            TreeSet<Class> plugins = new TreeSet<>(new Comparator<Class>() {
//                @Override public int compare(Class o1, Class o2) {
//                    return o1.getSimpleName().compareTo(o2.getSimpleName());
//                }
//            });
//            plugins.addAll(PackageUtility.getClasses("nars.operate", false));
//            for (Class c : plugins) {
//                if (!IOperator.class.isAssignableFrom(c))
//                    continue;
//
//                String[] p = c.getPackage().getName().split("\\.");
//                String category = p[1];
//                JMenu j = menus.get(category);
//                if (j == null) {
//                    j = new JMenu(category);
//                    menus.put(category, j);
//                }
//                JMenuItem x = newAddPluginItem(c);
//                j.add(x);
//            }
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        for (JMenu j : menus.values()) {
//            menu.add(j);
//        }
//
    }

    Map<String, Node> nodes = new ConcurrentHashMap<>();

    private Node node(String k, Object v) {
        return nodes.computeIfAbsent(k, (K) -> {


            ToggleButton p = new ToggleButton();
            p.getStyleClass().add("plugin_button");
            p.setGraphic(icon(K, v));
            p.setMaxWidth(Double.MAX_VALUE);
            p.setMaxHeight(Double.MAX_VALUE);
            //p.maxHeight(100);
            //p.prefHeight(100);
            p.minHeight(128);
            p.minWidth(128);
            return p;
        });
    }

    private Node icon(String k, Object v) {

        if (v instanceof FXIconPaneBuilder) {
            // instance implements its own node builder
            return ((FXIconPaneBuilder) v).newIconPane();
        }

        Function<Object,Node> override = ide.nodeBuilders.get(v.getClass());
        if (override != null) {
            //create via the type-dependent override
            return override.apply(v);
        } else {
            //create the default:
            BorderPane bp = new BorderPane();

            Label label = new Label(k);
            Label content = new Label(v.toString());

            content.setWrapText(true);
            content.setTextAlignment(TextAlignment.LEFT);

            label.getStyleClass().add("h1");

            content.setCache(true);
            label.setCache(true);

            bp.setTop(label);
            bp.setBottom(content);

            return bp;
        }
    }


//    public class PluginPane extends JPanel {
//        private final OperatorRegistration plugin;
//
//        public PluginPane(OperatorRegistration p) {
//            super(new BorderLayout());
//
//            this.plugin = p;
//            final JLabel j = new JLabel(p.IOperator.toString());
//            j.setFont(Video.monofont);
//            add(j, BorderLayout.NORTH);
//
//            JPanel buttons = new JPanel(new FlowLayout());
//            add(buttons, BorderLayout.EAST);
//
//            JCheckBox e = new JCheckBox();
//            e.setSelected(p.isEnabled());
//            e.addActionListener(new ActionListener() {
//                @Override public void actionPerformed(ActionEvent ae) {
//                    SwingUtilities.invokeLater(new Runnable() {
//                        @Override public void run() {
//                            boolean s = e.isSelected();
//                            p.setEnabled(s);
//                        }
//                    });
//                }
//            });
//            buttons.add(e);
//
//            JButton removeButton = new JButton("X");
//            removeButton.addActionListener(new ActionListener() {
//                @Override public void actionPerformed(ActionEvent ae) {
//                    SwingUtilities.invokeLater(new Runnable() {
//                        @Override public void run() {
//                            removePlugin(plugin);
//                        }
//                    });
//                }
//            });
//
//            buttons.add(removeButton);
//
//
//            add(new ReflectPanel(p.IOperator), BorderLayout.CENTER);
//        }
//
//    }
//
//
//
//    protected void update() {
//        content.removeAll();
//
//        int i = 0;
//        List<OperatorRegistration> ppp = nar.getPlugins();
//        if (!ppp.isEmpty()) {
//            for (OperatorRegistration p : ppp) {
//                PluginPane pp = new PluginPane(p);
//                pp.setBorder(new BevelBorder(BevelBorder.RAISED));
//                addVertically(pp, i++);
//            }
//        }
//        else {
//            addVertically(new JLabel("(No plugins active.)"), i++);
//        }
//
//
//        //contentWrap.doLayout();
//        //contentWrap.validate();
//    }
//
//
//    @Override
//    public void visibility(boolean appearedOrDisappeared) {
//        observer.setActive(appearedOrDisappeared);
//    }
//
//    private JMenuItem newAddPluginItem(Class c) {
//        String name = c.getSimpleName();
//        JMenuItem j = new JMenuItem(name);
//        j.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                addPlugin(c);
//                SwingUtilities.invokeLater(new Runnable() {
//                    @Override public void run() {
//                        update();
//                    }
//                });
//            }
//        });
//        return j;
//    }
//
//    protected void addPlugin(Class c) {
//        try {
//            IOperator p = (IOperator)c.newInstance();
//            nar.on(p);
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(this, ex.toString());
//        }
//    }
//    protected void removePlugin(OperatorRegistration ps) {
//        ps.off();
//    }
//
//


}
