package ca.nengo.ui.lib;

import automenta.vivisect.swing.NPanel;
import ca.nengo.plot.Plotter;
import ca.nengo.ui.lib.action.*;
import ca.nengo.ui.lib.menu.MenuBuilder;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.world.World;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.lib.world.piccolo.primitive.PXGrid;
import ca.nengo.ui.lib.world.piccolo.primitive.Universe;
import ca.nengo.ui.util.NengoWorld;
import org.piccolo2d.PCamera;
import org.piccolo2d.activities.PActivity;
import org.piccolo2d.util.PDebug;
import org.piccolo2d.util.PPaintContext;
import org.piccolo2d.util.PUtil;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;

import javax.swing.FocusManager;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;


/**
 * This class is based on PFrame by Jesse Grosjean
 * 
 * @author Shu Wu
 */
public abstract class AppFrame extends NPanel implements ApplicationListener {
    private static final long serialVersionUID = 2769082313231407201L;

    /**
     * TODO
     */
    public static final int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    /**
     * Name of the directory where UI Files are stored
     */
    public static final String USER_FILE_DIR = "UIFiles";

    /**
     * A String which briefly describes some commands used in this application
     */
    public static final String WORLD_TIPS =
    	"<H3>Mouse</H3>"
    	+ "Right Click >> Context menus<BR>"
        + "Right Click + Drag >> Zoom<BR>"
        + "Scroll Wheel >> Zoom"
        + "<H3>Keyboard</H3>"
        + "CTRL/CMD F >> Search the current window<BR>"
        + "SHIFT >> Multiple select<BR>"
        + "SHIFT + Drag >> Marquee select<BR>"
        + "<H3>Additional Help</H3>" 
        + "<a href=\"http://nengo.ca/docs/html/index.html\">Full documentation</a> (http://nengo.ca/docs/html/index.html)<BR>"
        + "<a href=\"http://nengo.ca/faq\">Frequently Asked Questions</a> (http://nengo.ca/faq)";

    private ReversableActionManager actionManager;

    private EventListener escapeFullScreenModeListener;

    private GraphicsDevice graphicsDevice;

    private boolean isFullScreenMode;

    private UserPreferences preferences;

    private ShortcutKey[] shortcutKeys;

    private Window topWindow;

    private Universe universe;

    private MenuBuilder worldMenu;

    protected MenuBuilder editMenu;

    protected MenuBuilder runMenu;
    protected JMenuBar menuBar;

    /**
     * TODO
     */
    public AppFrame() {
        //super(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
          //      .getDefaultConfiguration());

        super();

        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        initialize();
                    }
                });
            } catch (InvocationTargetException e) {
                e.getTargetException().printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            initialize();
        }

    }

//    /**
//     * Initializes the menu
//     */
//    private void initMenu() {
//        menuBar = new JMenuBar();
//        menuBar.setBorder(null);
//        //style.applyMenuStyle(menuBar, true);
//
//        MenuBuilder fileMenu = new MenuBuilder("File");
//        fileMenu.getJMenu().setMnemonic(KeyEvent.VK_F);
//        initFileMenu(fileMenu);
//        fileMenu.addAction(new ExitAction(this, "Quit"), KeyEvent.VK_P);
//        menuBar.add(fileMenu.getJMenu());
//
//        editMenu = new MenuBuilder("Edit");
//        editMenu.getJMenu().setMnemonic(KeyEvent.VK_E);
//
//        menuBar.add(editMenu.getJMenu());
//
//        initViewMenu(menuBar);
//
//        runMenu = new MenuBuilder("Run");
//        runMenu.getJMenu().setMnemonic(KeyEvent.VK_R);
//
//        menuBar.add(runMenu.getJMenu());
//
//        worldMenu = new MenuBuilder("Misc");
//        worldMenu.getJMenu().setMnemonic(KeyEvent.VK_O);
//        menuBar.add(worldMenu.getJMenu());
//
//        updateWorldMenu();
//        updateEditMenu();
//        updateRunMenu();
//
//        MenuBuilder helpMenu = new MenuBuilder("Help");
//        helpMenu.getJMenu().setMnemonic(KeyEvent.VK_H);
//        menuBar.add(helpMenu.getJMenu());
//
//        helpMenu.addAction(new OpenURLAction("Documentation (opens in browser)",
//                "http://www.nengo.ca/documentation"), KeyEvent.VK_F1);
//        helpMenu.addAction(new TipsAction("Tips and Commands", false), KeyEvent.VK_T);
//        boolean isMacOS = System.getProperty("mrj.version") != null;
//        if (!isMacOS) {
//            helpMenu.addAction(new AboutAction("About"), KeyEvent.VK_A);
//        }
//
//        menuBar.setVisible(true);
//        this.setJMenuBar(menuBar);
//    }

    protected void chooseBestDisplayMode(GraphicsDevice device) {
        DisplayMode best = getBestDisplayMode(device);
        if (best != null) {
            device.setDisplayMode(best);
        }
    }

    protected PCamera createDefaultCamera() {
        return PUtil.createBasicScenegraph();
    }

    protected abstract NengoWorld createWorld();

    protected DisplayMode getBestDisplayMode(GraphicsDevice device) {
        Iterator<DisplayMode> itr = getPreferredDisplayModes(device).iterator();
        while (itr.hasNext()) {
            DisplayMode each = itr.next();
            DisplayMode[] modes = device.getDisplayModes();
            for (DisplayMode element : modes) {
                if (element.getWidth() == each.getWidth()
                        && element.getHeight() == each.getHeight()
                        && element.getBitDepth() == each.getBitDepth()) {
                    return each;
                }
            }
        }

        return null;
    }

    /**
     * By default return the current display mode. Subclasses may override this
     * method to return other modes in the collection.
     */
    protected Collection<DisplayMode> getPreferredDisplayModes(GraphicsDevice device) {
        ArrayList<DisplayMode> result = new ArrayList<DisplayMode>();

        result.add(device.getDisplayMode());
        /*
         * result.add(new DisplayMode(640, 480, 32, 0)); result.add(new
         * DisplayMode(640, 480, 16, 0)); result.add(new DisplayMode(640, 480,
         * 8, 0));
         */

        return result;
    }

    protected ShortcutKey[] getShortcutKeys() {
        return shortcutKeys;
    }

    /**
     * Use this function to add menu items to the frame menu bar
     * 
     *            is attached to the frame
     */
    protected void initFileMenu(MenuBuilder menu) {

    }

    protected void initialize() {
        /*
         * Initialize shortcut keys
         */
        FocusManager.getCurrentManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (getShortcutKeys() != null && e.getID() == KeyEvent.KEY_PRESSED) {
                    for (ShortcutKey shortcutKey : getShortcutKeys()) {
                        if (shortcutKey.getModifiers() == e.getModifiers()) {
                            if (shortcutKey.getKeyCode() == e.getKeyCode()) {
                                shortcutKey.getAction().doAction();
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        });

        graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        loadPreferences();
        UIEnvironment.setInstance(this);

        if (preferences!=null) {
            if (preferences.isWelcomeScreen()) {
                preferences.setWelcomeScreen(false);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        (new TipsAction("", true)).doAction();
                    }
                });
            }
        }


        actionManager = new ReversableActionManager(this);
        setLayout(new BorderLayout());

        universe = new Universe();
        universe.setMinimumSize(new Dimension(200, 200));
        universe.setPreferredSize(new Dimension(400, 400));
        universe.initialize(createWorld());
        universe.setFocusable(true);

        // getContentPane().add(canvas);
        // canvas.setPreferredSize(new Dimension(200, 200));

        initLayout(universe);

        setBounds(new Rectangle(100, 100, 800, 600));
        setBackground(null);
        /*addWindowListener(new MyWindowListener());

        try {
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        } catch (SecurityException e) {
            e.printStackTrace();
        }*/

        universe.setSelectionMode(false);

        //initMenu();

        /*
         * Initialize shortcut keys
         */
        List<ShortcutKey> shortcuts = new ArrayList<ShortcutKey>();
        constructShortcutKeys(shortcuts);
        this.shortcutKeys = shortcuts.toArray(new ShortcutKey[shortcuts.size()]);

        validate();
        //setFullScreenMode(false);

    }

    protected void constructShortcutKeys(List<ShortcutKey> shortcuts) {
        shortcuts.add(new ShortcutKey(MENU_SHORTCUT_KEY_MASK, KeyEvent.VK_0, 
        		new ZoomToFitAction("Zoom to fit", (WorldImpl)getTopWorld())));
    }

    private World getTopWorld() {
        Window window = getTopWindow();
        if (window != null) {
            WorldObject wo = window.getContents();
            if (wo instanceof World) {
                return (World) wo;
            } else {
                return null;
            }
        } else {
            return getWorld();
        }
    }

    abstract protected void initLayout(Universe canvas);
//    protected void initLayout(Universe canvas) {
//        Container cp = getContentPane();
//
//        //cp.add(canvas);
//        setContentPane(new GLG2DCanvas(canvas));
//
//
//
//        canvas.requestFocus();
//    }

    /**
     * Use this function to add menu items to the frame menu bar
     * 
     * @param menuBar
     *            is attached to the frame
     */
    protected void initViewMenu(JMenuBar menuBar) {

    }

    /**
     * Loads saved preferences related to the application
     */
    protected void loadPreferences() {
        File file = new File(USER_FILE_DIR);
        if (!file.exists()) {
            file.mkdir();
        }

        File preferencesFile = new File(USER_FILE_DIR, "userSettings");

        if (preferencesFile.exists()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(preferencesFile);

                ObjectInputStream ois = new ObjectInputStream(fis);
                try {
                    preferences = (UserPreferences) ois.readObject();
                } catch (ClassNotFoundException e) {
                    System.out.println("Could not load preferences");
                }
            } catch (IOException e1) {
                System.out.println("Could not read preferences file");
            }
        }

        if (preferences == null) {
            preferences = new UserPreferences();

        }
        preferences.apply(this);
    }

    /**
     * Save preferences to file
     */
    protected void savePreferences() {
        File file = new File(USER_FILE_DIR);
        if (!file.exists()) {
            file.mkdir();
        }

        File preferencesFile = new File(USER_FILE_DIR, "userSettings");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(bos);

            oos.writeObject(preferences);

            FileOutputStream fos = new FileOutputStream(preferencesFile);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the menu 'edit'
     */
    protected void updateEditMenu() {
        if (editMenu!=null) {
            editMenu.reset();

            editMenu.addAction(new UndoAction(), KeyEvent.VK_Z, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                    MENU_SHORTCUT_KEY_MASK));

            editMenu.addAction(new RedoAction(), KeyEvent.VK_Y, KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                    MENU_SHORTCUT_KEY_MASK));

            editMenu.getJMenu().addSeparator();
        }

    }

    /**
     * Updates the menu 'run'
     */
    protected void updateRunMenu() {
        if (runMenu!=null)
            runMenu.reset();

        // Configure parallelization
    }

    /**
     * Updates the menu 'world'
     */
    protected void updateWorldMenu() {
        if (worldMenu==null)
            return;

        worldMenu.reset();

        if (!universe.isSelectionMode()) {
            worldMenu.addAction(new SwitchToSelectionMode(), KeyEvent.VK_S);
        } else {
            worldMenu.addAction(new SwitchToNavigationMode(), KeyEvent.VK_S);
        }        
        
        worldMenu.getJMenu().addSeparator();
        
        worldMenu.addAction(new CloseAllPlots(), KeyEvent.VK_M);
        
        worldMenu.getJMenu().addSeparator();

        if (!isFullScreenMode) {
            // worldMenu.addAction(new TurnOnFullScreen(), KeyEvent.VK_F);
        } else {
            // worldMenu.addAction(new TurnOffFullScreen(), KeyEvent.VK_F);
        }

        if (preferences!=null) {
            if (!preferences.isEnableTooltips()) {
                worldMenu.addAction(new TurnOnTooltips(), KeyEvent.VK_T);
            } else {
                worldMenu.addAction(new TurnOffTooltips(), KeyEvent.VK_T);
            }
        }

        if (!PXGrid.isGridVisible()) {
            worldMenu.addAction(new TurnOnGrid(), KeyEvent.VK_G);
        } else {
            worldMenu.addAction(new TurnOffGrid(), KeyEvent.VK_G);
        }

        worldMenu.getJMenu().addSeparator();
        
        MenuBuilder qualityMenu = worldMenu.addSubMenu("Rendering Quality");

        qualityMenu.getJMenu().setMnemonic(KeyEvent.VK_Q);

        qualityMenu.addAction(new LowQualityAction(), KeyEvent.VK_L);
        qualityMenu.addAction(new MediumQualityAction(), KeyEvent.VK_M);
        qualityMenu.addAction(new HighQualityAction(), KeyEvent.VK_H);

        MenuBuilder debugMenu = worldMenu.addSubMenu("Debug");
        debugMenu.getJMenu().setMnemonic(KeyEvent.VK_E);

        if (!PDebug.debugPrintUsedMemory) {
            debugMenu.addAction(new ShowDebugMemory(), KeyEvent.VK_S);
        } else {
            debugMenu.addAction(new HideDebugMemory(), KeyEvent.VK_H);
        }
    }

    /**
     * @param activity TODO
     * @return TODO
     */
    public boolean addActivity(PActivity activity) {
        return universe.getRoot().addActivity(activity);
    }

//    /**
//     * This method adds a key listener that will take this PFrame out of full
//     * screen mode when the escape key is pressed. This is called for you
//     * automatically when the frame enters full screen mode.
//     */
//    public void addEscapeFullScreenModeListener() {
//        removeEscapeFullScreenModeListener();
//        escapeFullScreenModeListener = new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent aEvent) {
//                if (aEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
//                    setFullScreenMode(false);
//                }
//            }
//        };
//        universe.addKeyListener((KeyListener) escapeFullScreenModeListener);
//    }

    /**
     * @param window TODO
     */
    public void addWorldWindow(Window window) {
        universe.getWorld().getSky().addChild(window);
    }

    /**
     * Called when the user closes the Application window
     */
    public void exitAppFrame() {
        savePreferences();
        System.exit(0);
    }

    /**
     * @return String which describes what the application is about
     */
    public abstract String getAboutString();

    /**
     * @return Action manager responsible for managing actions. Enables undo,
     *         redo functionality.
     */
    public ReversableActionManager getActionManager() {
        return actionManager;
    }

    /**
     * @return Name of the application
     */
    public abstract String getAppName();

    /**
     * @return TODO
     */
    public abstract String getAppWindowTitle();

    /**
     * @return TODO
     */
    public Window getTopWindow() {
        return topWindow;
    }

    /**
     * @return Canvas which hold the zoomable UI
     */
    public Universe getUniverse() {
        return universe;
    }

    /**
     * @return the top-most World associated with this frame
     */
    public NengoWorld getWorld() {
        return universe.getWorld();
    }

    /**
     * This method removes the escape full screen mode key listener. It will be
     * called for you automatically when full screen mode exits, but the method
     * has been made public for applications that wish to use other methods for
     * exiting full screen mode.
     */
    public void removeEscapeFullScreenModeListener() {
        if (escapeFullScreenModeListener != null) {
            universe.removeKeyListener((KeyListener) escapeFullScreenModeListener);
            escapeFullScreenModeListener = null;
        }
    }

//    /**
//     * TODO
//     */
//    public void restoreDefaultTitle() {
//        setTitle(getAppWindowTitle());
//    }

    /**
     * Called when reversable actions have changed. Updates the edit menu.
     */
    public void reversableActionsUpdated() {
        updateEditMenu();
    }

    /**
     * @param fullScreenMode
     *            sets the screen to fullscreen
     */
//    public void setFullScreenMode(boolean fullScreenMode) {
//        this.isFullScreenMode = fullScreenMode;
//        if (fullScreenMode) {
//            addEscapeFullScreenModeListener();
//
//            if (isDisplayable()) {
//                dispose();
//            }
//
//            setUndecorated(true);
//            setResizable(false);
//            //graphicsDevice.setFullScreenWindow(this);
//
//            if (graphicsDevice.isDisplayChangeSupported()) {
//                chooseBestDisplayMode(graphicsDevice);
//            }
//            validate();
//        } else {
//            removeEscapeFullScreenModeListener();
//
//            if (isDisplayable()) {
//                dispose();
//            }
//
//            setUndecorated(false);
//            setResizable(true);
//            graphicsDevice.setFullScreenWindow(null);
//            validate();
//            setVisible(true);
//        }
//    }

//    /**
//     * @param window TODO
//     */
//    public void setTopWindow(Window window) {
//        topWindow = window;
//        if (topWindow != null) {
//            setTitle(window.getName() + " - " + getAppWindowTitle());
//        } else {
//            UIEnvironment.getInstance().restoreDefaultTitle();
//        }
//    }

    /**
     * Action to set rendering mode to high quality.
     * 
     * @author Shu Wu
     */
    protected class HighQualityAction extends StandardAction {

        private static final long serialVersionUID = 1L;

        public HighQualityAction() {
            super("High Quality");
        }

        @Override
        protected void action() throws ActionException {
            getUniverse().setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
            getUniverse().setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
            getUniverse().setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
            updateWorldMenu();
        }

    }

    // Everything starting with handle is done for MacOSX only
    public void handleAbout(ApplicationEvent event) {
        new AboutAction("About").doAction();
        event.setHandled(true);
    }
    public void handleOpenApplication(ApplicationEvent event) {}

    public void handleOpenFile(ApplicationEvent event) {}

    public void handlePreferences(ApplicationEvent event) {}

    public void handlePrintFile(ApplicationEvent event) {
        JOptionPane.showMessageDialog(this, "Sorry, printing not implemented");
    }

    public void handleQuit(ApplicationEvent event) {
        new ExitAction(this, "Quit").doAction();
    }

    public void handleReOpenApplication(ApplicationEvent event) {}

    /**
     * Action to show the 'about' dialog
     * 
     * @author Shu Wu
     */
    class AboutAction extends StandardAction {

        private static final long serialVersionUID = 1L;

        public AboutAction(String actionName) {
            super("About", actionName);
        }

        @Override
        protected void action() throws ActionException {
            int width = 350;
            String css = "<style type = \"text/css\">" +
                    "body { width: " + width + "px }" +
                    "p { margin-top: 12px }" +
                    "b { text-decoration: underline }" +
                    "</style>";
            JLabel editor = new JLabel("<html><head>" + css + "</head><body>" + getAboutString() + "</body></html>");
            JOptionPane.showMessageDialog(UIEnvironment.getInstance(), editor, "About "
                    + getAppName(), JOptionPane.PLAIN_MESSAGE);
        }

    }

    /**
     * Action to hide debug memory messages printed to the console.
     * 
     * @author Shu Wu
     */
    class HideDebugMemory extends StandardAction {

        private static final long serialVersionUID = 1L;

        public HideDebugMemory() {
            super("Stop printing Memory Used to console");
        }

        @Override
        protected void action() throws ActionException {
            PDebug.debugPrintUsedMemory = false;
            updateWorldMenu();
        }

    }

    /**
     * Action to set rendering mode to low quality.
     * 
     * @author Shu Wu
     */
    class LowQualityAction extends StandardAction {

        private static final long serialVersionUID = 1L;

        public LowQualityAction() {
            super("Low Quality");
        }

        @Override
        protected void action() throws ActionException {
            getUniverse().setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
            getUniverse().setAnimatingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
            getUniverse().setInteractingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
            updateWorldMenu();
        }

    }

    /**
     * Action to set rendering mode to medium quality.
     * 
     * @author Shu Wu
     */
    class MediumQualityAction extends StandardAction {

        private static final long serialVersionUID = 1L;

        public MediumQualityAction() {
            super("Medium Quality");
        }

        @Override
        protected void action() throws ActionException {
            getUniverse().setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
            getUniverse().setAnimatingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
            getUniverse().setInteractingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
            updateWorldMenu();
        }

    }

    /**
     * Minimizes all windows in the top-level world
     * 
     * @author Shu Wu
     */
    class MinimizeAllWindows extends StandardAction {

        private static final long serialVersionUID = 1L;

        public MinimizeAllWindows() {
            super("Minimize all windows");
        }

        @Override
        protected void action() throws ActionException {
            getWorld().minimizeAllWindows();

        }

    }

    /**
     * Listener which listens for Application window close events
     * 
     * @author Shu Wu
     */
    class MyWindowListener implements WindowListener {

        public void windowActivated(WindowEvent arg0) {
        }

        public void windowClosed(WindowEvent arg0) {

        }

        public void windowClosing(WindowEvent arg0) {
            AppFrame.this.exitAppFrame();
        }

        public void windowDeactivated(WindowEvent arg0) {
        }

        public void windowDeiconified(WindowEvent arg0) {
        }

        public void windowIconified(WindowEvent arg0) {
        }

        public void windowOpened(WindowEvent arg0) {
        }

    }

    /**
     * Action to redo the last reversable action
     * 
     * @author Shu Wu
     */
    class RedoAction extends StandardAction {

        private static final long serialVersionUID = 1L;

        public RedoAction() {
            super("Redo: " + actionManager.getRedoActionDescription());
            if (!actionManager.canRedo()) {
                setEnabled(false);
            }
        }

        @Override
        protected void action() throws ActionException {
            actionManager.redoAction();

        }

    }

    /**
     * Action to enable the printing of memory usage messages to the console
     * 
     * @author Shu Wu
     */
    class ShowDebugMemory extends StandardAction {

        private static final long serialVersionUID = 1L;

        public ShowDebugMemory() {
            super("Print Memory Used to console");
        }

        @Override
        protected void action() throws ActionException {
            PDebug.debugPrintUsedMemory = true;
            updateWorldMenu();
        }

    }

    /**
     * Action to switch to navigation mode
     * 
     * @author Shu Wu
     */
    class SwitchToNavigationMode extends StandardAction {

        private static final long serialVersionUID = 1L;

        public SwitchToNavigationMode() {
            super("Switch to Navigation Mode");
        }

        @Override
        protected void action() throws ActionException {
            universe.setSelectionMode(false);
            updateWorldMenu();
        }

    }

    /**
     * Action to switch to selection mode
     * 
     * @author Shu Wu
     */
    class SwitchToSelectionMode extends StandardAction {

        private static final long serialVersionUID = 1L;

        public SwitchToSelectionMode() {
            super("Switch to Selection Mode   Shift");
        }

        @Override
        protected void action() throws ActionException {
            universe.setSelectionMode(true);
            updateWorldMenu();
        }

    }

    protected String getHelp() {
        return WORLD_TIPS + "<BR>";
    }

    /**
     * Action which shows the tips dialog
     * 
     * @author Shu Wu
     */
    class TipsAction extends StandardAction {

        private static final long serialVersionUID = 1L;

        private final boolean welcome;

        public TipsAction(String actionName, boolean isWelcomeScreen) {
            super("Show UI tips", actionName);

            this.welcome = isWelcomeScreen;
        }

        @Override
        protected void action() throws ActionException {
            JEditorPane editor;

            if (welcome) {
                String appendum = "To show this message again, click <b>Help -> Tips and Commands</b>";
                editor = new JEditorPane("text/html", "<html><H2>Welcome to " + getAppName() + "</H2>" + getHelp()
                        + "<BR><BR>" + appendum + "</html>");
            } else {
                editor = new JEditorPane("text/html", "<html>" + getHelp() + "</html>");
            }
            
            editor.setEditable(false);
            editor.setOpaque(false);
            editor.addHyperlinkListener(new HyperlinkListener() {
            	public void hyperlinkUpdate(HyperlinkEvent hle) {
            		if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
            			new OpenURLAction(hle.getDescription(),hle.getDescription()).doAction();
            		}            		
            	}
            });

            JOptionPane.showMessageDialog(UIEnvironment.getInstance(), editor, getAppName()
                    + " Tips", JOptionPane.PLAIN_MESSAGE);
        }
    }

//    /**
//     * Action to turn off full screen mode
//     *
//     * @author Shu Wu
//     */
//    class TurnOffFullScreen extends StandardAction {
//
//        private static final long serialVersionUID = 1L;
//
//        public TurnOffFullScreen() {
//            super("Full screen off");
//        }
//
//        @Override
//        protected void action() throws ActionException {
//            setFullScreenMode(false);
//            updateWorldMenu();
//        }
//
//    }

    /**
     * Action to turn off the grid
     * 
     * @author Shu Wu
     */
    class TurnOffGrid extends StandardAction {

        private static final long serialVersionUID = 1L;

        public TurnOffGrid() {
            super("Grid off");

        }

        @Override
        protected void action() throws ActionException {
            preferences.setGridVisible(false);
            updateWorldMenu();
        }

    }

    /**
     * Action to turn off tooltips
     * 
     * @author Shu Wu
     */
    class TurnOffTooltips extends StandardAction {

        private static final long serialVersionUID = 1L;

        public TurnOffTooltips() {
            super("Autoshow Tooltips off");
        }

        @Override
        protected void action() throws ActionException {
            preferences.setEnableTooltips(false);
            updateWorldMenu();
        }

    }

//    /**
//     * Action to turn on full screen mode
//     *
//     * @author Shu Wu
//     */
//    class TurnOnFullScreen extends StandardAction {
//
//        private static final long serialVersionUID = 1L;
//
//        public TurnOnFullScreen() {
//            super("Full screen on");
//        }
//
//        @Override
//        protected void action() throws ActionException {
//            setFullScreenMode(true);
//            updateWorldMenu();
//        }
//
//    }

    /**
     * Action to turn on the grid
     * 
     * @author Shu Wu
     */
    class TurnOnGrid extends StandardAction {

        private static final long serialVersionUID = 1L;

        public TurnOnGrid() {
            super("Grid on");
        }

        @Override
        protected void action() throws ActionException {
            preferences.setGridVisible(true);

            updateWorldMenu();
        }

    }

    /**
     * Action to turn on tooltips
     * 
     * @author Shu Wu
     */
    class TurnOnTooltips extends StandardAction {

        private static final long serialVersionUID = 1L;

        public TurnOnTooltips() {
            super("Autoshow Tooltips on");
        }

        @Override
        protected void action() throws ActionException {
            preferences.setEnableTooltips(true);
            updateWorldMenu();
        }

    }

    /**
     * Action which undos the last reversable action
     * 
     * @author Shu Wu
     */
    class UndoAction extends StandardAction {

        private static final long serialVersionUID = 1L;

        public UndoAction() {
            super("Undo: " + actionManager.getUndoActionDescription());
            if (!actionManager.canUndo()) {
                setEnabled(false);
            }
        }

        @Override
        protected void action() throws ActionException {
            actionManager.undoAction();
        }
    }

    /**
     * Action to close all plots
     * 
     * @author Daniel Rasmussen
     */
    static class CloseAllPlots extends StandardAction {
        private static final long serialVersionUID = 1L;

        public CloseAllPlots() {
            super("Close all plots");
        }

        @Override
        protected void action() throws ActionException {
            Plotter.closeAll();
        }
    }
}

/**
 * Serializable object which contains UI preferences of the application
 * 
 * @author Shu Wu
 */
/**
 * @author Shu
 */
class UserPreferences implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean enableTooltips = true;
    private boolean gridVisible = true;
    private boolean isWelcomeScreen = true;

    /**
     * Applies preferences
     * 
     * @param applyTo
     *            The application in which to apply the preferences to
     */
    public void apply(AppFrame applyTo) {
        setEnableTooltips(enableTooltips);
        setGridVisible(gridVisible);
    }

    public boolean isEnableTooltips() {
        return enableTooltips;
    }

    public boolean isGridVisible() {
        return gridVisible;
    }

    public boolean isWelcomeScreen() {
        return isWelcomeScreen;
    }

    public void setEnableTooltips(boolean enableTooltips) {
        this.enableTooltips = enableTooltips;
        WorldImpl.setTooltipsVisible(this.enableTooltips);
    }

    public void setGridVisible(boolean gridVisible) {
        this.gridVisible = gridVisible;
        PXGrid.setGridVisible(gridVisible);
    }

    public void setWelcomeScreen(boolean isWelcomeScreen) {
        this.isWelcomeScreen = isWelcomeScreen;
    }

}