//package ca.nengo.ui;
//
////import org.java.ayatana.ApplicationMenu;
////import org.java.ayatana.AyatanaDesktop;
//
//import ca.nengo.ui.data.DataListView;
//import ca.nengo.ui.data.SimulatorDataModel;
//import ca.nengo.ui.lib.AuxillarySplitPane;
//import ca.nengo.ui.lib.style.NengoStyle;
//import ca.nengo.ui.lib.actions.ZoomToFitAction;
//import ca.nengo.ui.lib.objects.models.ModelObject;
//import ca.nengo.ui.lib.util.UIEnvironment;
//import ca.nengo.ui.lib.util.UserMessages;
//import ca.nengo.ui.lib.util.Util;
//import ca.nengo.ui.lib.util.menus.MenuBuilder;
//import ca.nengo.ui.lib.world.WorldObject;
//import ca.nengo.ui.lib.world.handlers.SelectionHandler;
//import ca.nengo.ui.lib.world.piccolo.primitives.Universe;
//import ca.nengo.ui.script.ScriptConsole;
//import ca.nengo.ui.util.ScriptWorldWrapper;
//import org.python.util.PythonInterpreter;
//import org.simplericity.macify.eawt.Application;
//import org.simplericity.macify.eawt.DefaultApplication;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.KeyEvent;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//
//public class NengoClassic extends AbstractNengo {
//
//    private JScrollPane templateViewer;
//    private JPanel templatePanel;
//    private JToolBar toolbarPanel;
//    private AuxillarySplitPane toolbarPane;
//    private AuxillarySplitPane templatePane;
//
//    private PythonInterpreter pythonInterpreter;
//    private ScriptConsole scriptConsole;
//    private AuxillarySplitPane scriptConsolePane;
//
//    /**
//     * Nengo version number
//     */
//    public static final double VERSION = 1.4;
//    /**
//     * String used in the UI to identify Nengo
//     */
//    public static final String APP_NAME = "Nengo V" + VERSION;
//    /**
//     * Description of Nengo to be shown in the "About" Dialog box
//     */
//    public static final String ABOUT =
//            "<H3>" + APP_NAME + "</H3>"
//                    + "<a href=http://www.nengo.ca>www.nengo.ca</a>"
//                    + "<p>&copy; Centre for Theoretical Neuroscience (ctn.uwaterloo.ca) 2006-2012</p>"
//                    + "<b>Contributors:</b> Bryan&nbsp;Tripp, Shu&nbsp;Wu, Chris&nbsp;Eliasmith, Terry&nbsp;Stewart, James&nbsp;Bergstra, "
//                    + "Trevor&nbsp;Bekolay, Dan&nbsp;Rasmussen, Xuan&nbsp;Choo, Travis&nbsp;DeWolf, "
//                    + "Yan&nbsp;Wu, Eric&nbsp;Crawford, Eric&nbsp;Hunsberger, Carter&nbsp;Kolbeck, "
//                    + "Jonathan&nbsp;Lai, Oliver&nbsp;Trujillo, Peter&nbsp;Blouw, Pete&nbsp;Suma, Patrick&nbsp;Ji, Jeff&nbsp;Orchard</p>"
//                    + "<p>This product contains several open-source libraries (copyright their respective authors). "
//                    + "For more information, consult <tt>lib/library-licenses.txt</tt> in the installation directory.</p>"
//                    + "<p>This product includes software developed by The Apache Software Foundation (http://www.apache.org/).</p>";
//
//    /**
//     * template.py calls this function to provide a template bar
//     */
//    public void setTemplatePanel(JPanel panel) {
//        templatePanel = panel;
//    }
//
//    /**
//     * toolbar.py calls this function to provide a toolbar
//     */
//    public void setToolbar(JToolBar bar) {
//        toolbarPanel = bar;
//    }
//
//    @Override
//    public void initViewMenu(JMenuBar menuBar) {
//
//        MenuBuilder viewMenu = new MenuBuilder("View");
//        viewMenu.getJMenu().setMnemonic(KeyEvent.VK_V);
//        menuBar.add(viewMenu.getJMenu());
//
//        int count = 1;
//        for (AuxillarySplitPane splitPane : splitPanes) {
//            if (splitPane==null) continue;
//            byte shortCutChar = splitPane.getAuxTitle().getBytes()[0];
//
//            viewMenu.addAction(new ToggleScriptPane("Toggle " + splitPane.getAuxTitle(), splitPane),
//                    shortCutChar,
//                    KeyStroke.getKeyStroke(0x30 + count++, MENU_SHORTCUT_KEY_MASK));
//
//        }
//        viewMenu.getJMenu().addSeparator();
//
//        viewMenu.addAction(new ZoomToFitAction("Zoom to fit", this.getWorld()),
//                KeyEvent.VK_0,
//                KeyStroke.getKeyStroke(KeyEvent.VK_0, MENU_SHORTCUT_KEY_MASK));
//    }
//
//    @Override
//    protected void initLayout(Universe canvas) {
//        super.initLayout(canvas);
//
//        try {
//            //Tell the UIManager to use the platform look and feel
//            String laf = UIManager.getSystemLookAndFeelClassName();
//            if (laf.equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
//                laf = "javax.swing.plaf.metal.MetalLookAndFeel";
//                File desktopfile = new File(System.getProperty("user.home") +
//                        "/.local/share/applications/nengo.desktop");
//                if (!desktopfile.exists()) {
//                    File defaultdesktop = new File(getClass().getClassLoader().
//                            getResource("ca/nengo/ui/nengo.desktop").getPath());
//                    Util.copyFile(defaultdesktop, desktopfile);
//                }
//                //DesktopFile df = DesktopFile.initialize("nengo", "NengoLauncher");
//                //df.setIcon(getClass().getClassLoader().
//                //		getResource("ca/nengo/ui/nengologo256.png").getPath());
//                //df.setCommand("TODO");
//                //df.update();
//            }
//            UIManager.setLookAndFeel(laf);
//
//            //UIManager.put("Slider.paintValue",Boolean.FALSE);
//        } catch(IOException e) {
//            System.out.println("nengo.desktop not copied.");
//        } catch (UnsupportedLookAndFeelException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//
//        /////////////////////////////////////////////////////////////
//        /// Create split pane components
//
//        // creating the script console calls all python init stuff
//        // so call it first (make toolbar, etc.)
//
//
//
//
//        if (toolbarPanel == null) {
//            toolbarPanel = new JToolBar();
//        }
//        if (templatePanel == null) {
//            templatePanel = new JPanel();
//        }
////        if (toolbarPanel == null || templatePanel == null) {
////            // these should be made and set by template.py and toolbar.py
////            // when the scriptConsole is created, so we shouldn't be here
////            throw new NullPointerException(
////                    "toolbarPanel or templatePanel not created!");
////        }
//
//        dataListViewer = new DataListView(new SimulatorDataModel());
//
//        templateViewer = new JScrollPane(templatePanel,
//                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
//                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        templateViewer.getVerticalScrollBar().setUnitIncrement(20);
//        templateViewer.revalidate();
//        Dimension templateWithScrollbarSize = templateViewer.getPreferredSize();
//        templateViewer.setVerticalScrollBarPolicy(
//                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
//
//        getContentPane().add(templateViewer, BorderLayout.WEST);
//
//        /////////////////////////////////////////////////////////////
//        /// Create nested split panes
//        configPane = new ConfigurationPane(canvas);
//
//
//
//        dataViewerPane = getDataViewer();
//        dataViewerPane.setAuxVisible(true);
//
//
//        templatePane = new AuxillarySplitPane(dataViewerPane, templateViewer,
//                "Templates", AuxillarySplitPane.Orientation.Left,
//                templateWithScrollbarSize, false);
//        templatePane.setResizable(false);
//        templatePane.setAuxVisible(true);
//
//
//        toolbarPane = new AuxillarySplitPane(templatePane, toolbarPanel,
//                "Toolbar", AuxillarySplitPane.Orientation.Top,
//                toolbarPanel.getPreferredSize(), false);
//        toolbarPane.setResizable(false);
//        toolbarPane.setAuxVisible(true);
//
//        getContentPane().add(toolbarPane);
//
//
//        // Add all panes to the list. The order added controls
//        // the order in the View menu
//        splitPanes = new ArrayList<AuxillarySplitPane>();
//        splitPanes.add(dataViewerPane);
//        if (CONFIGURE_PLANE_ENABLED) {
//            splitPanes.add(configPane.toJComponent());
//        }
//        splitPanes.add(templatePane);
//        splitPanes.add(toolbarPane);
//
//
//        pythonInterpreter = new PythonInterpreter();
//
//
//        scriptConsole = new ScriptConsole(pythonInterpreter);
//        NengoStyle.applyStyle(scriptConsole);
//
//        splitPanes.add(scriptConsolePane);
//    }
//
//    public static NengoClassic getInstance() {
//        Util.Assert(UIEnvironment.getInstance() instanceof NengoClassic);
//        return (NengoClassic)UIEnvironment.getInstance();
//    }
//
//    @Override
//    public String getAboutString() {
//        return ABOUT;
//    }
//
//    @Override
//    public String getAppName() {
//        return APP_NAME;
//    }
//
//    public String getAppWindowTitle() {
//        return "Nengo Workspace";
//    }
//    /**
//     * @return TODO
//     */
//    public PythonInterpreter getPythonInterpreter() {
//        return pythonInterpreter;
//    }
//
//    /**
//     * @return the script console
//     */
//    public ScriptConsole getScriptConsole() {
//        return scriptConsole;
//    }
//
//    /**
//     * @return is the script console pane visible
//     */
//    public boolean isScriptConsoleVisible() {
//        return scriptConsolePane.isAuxVisible();
//    }
//
//    protected void updateScriptConsole() {
//        Object model = SelectionHandler.getActiveModel();
//        scriptConsole.setCurrentObject(model);
//    }
//
//    protected AuxillarySplitPane getDataViewer() {
//        return new AuxillarySplitPane(scriptConsolePane, dataListViewer,
//                "Data Viewer", AuxillarySplitPane.Orientation.Left);
//    }
//
//    private void initScriptConsole() {
//        scriptConsolePane = new AuxillarySplitPane(configPane.toJComponent(), scriptConsole,
//                "Script Console", AuxillarySplitPane.Orientation.Bottom);
//
//
//        scriptConsole.addVariable("world", new ScriptWorldWrapper(this));
//
//        // add listeners
//        getWorld().getGround().addChildrenListener(new WorldObject.ChildListener() {
//
//            public void childAdded(WorldObject wo) {
//                if (wo instanceof ModelObject) {
//                    final ModelObject modelObject = ((ModelObject) wo);
//                    //                    final Object model = modelObject.getModel();
//                    final String modelName = modelObject.getName();
//
//                    try {
//                        //scriptConsole.addVariable(modelName, model);
//
//                        modelObject.addPropertyChangeListener(WorldObject.Property.REMOVED_FROM_WORLD,
//                                new WorldObject.Listener() {
//                                    public void propertyChanged(WorldObject.Property event) {
//                                        scriptConsole.removeVariable(modelName);
//                                        modelObject.removePropertyChangeListener(WorldObject.Property.REMOVED_FROM_WORLD,
//                                                this);
//                                    }
//                                });
//
//                    } catch (Exception e) {
//                        UserMessages.showError("Error adding network: " + e.getMessage());
//                    }
//                }
//            }
//
//            public void childRemoved(WorldObject wo) {
//                /*
//                 * Do nothing here. We don't remove the variable here directly
//                 * because the network has already been destroyed and no longer
//                 * has a reference to it's model.
//                 */
//
//            }
//
//        });
//
//    }
//
//    @Override
//    protected void initialize() {
//        super.initialize();
//
//        /// Attach listeners for Script Console
//        initScriptConsole();
//    }
//
//    /**
//     * Runs NengoGraphics with a default name
//     *
//     * @param args
//     */
//    public static void main(String[] args) {
//        System.setProperty("apple.laf.useScreenMenuBar", "true");
//        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Nengo");
//        Application application = new DefaultApplication();
//
//        NengoClassic ng = new NengoClassic();
//        ng.setApplication(application);
//        //if (AyatanaDesktop.isSupported()) {
//        //	ApplicationMenu.tryInstall(ng);
//        //}
//    }
//
//
//}
