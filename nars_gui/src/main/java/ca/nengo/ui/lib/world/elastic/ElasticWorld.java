package ca.nengo.ui.lib.world.elastic;

import ca.nengo.ui.config.ConfigException;
import ca.nengo.ui.config.ConfigResult;
import ca.nengo.ui.config.Property;
import ca.nengo.ui.config.descriptors.PInt;
import ca.nengo.ui.config.managers.ConfigManager;
import ca.nengo.ui.config.managers.ConfigManager.ConfigMode;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.LayoutAction;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.object.activity.TrackedAction;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.WorldSkyImpl;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.Layout;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A World which supports Spring layout. Objects within this world attract and
 * repel each other
 * 
 * @author Shu Wu
 */
public class ElasticWorld extends WorldImpl {
    /**
     * Default layout bounds
     */
    private static final Dimension DEFAULT_LAYOUT_BOUNDS = new Dimension(1000, 1000);

    /**
     * Layout bounds
     */
    private Dimension layoutBounds = DEFAULT_LAYOUT_BOUNDS;

    public ElasticWorld(String name) {
        this(name, new WorldSkyImpl(), new ElasticGround());
    }

    public ElasticWorld(String name, WorldGroundImpl ground) {
        this(name, new WorldSkyImpl(), ground);

    }

    public ElasticWorld(String name, WorldSkyImpl sky, WorldGroundImpl ground) {
        super(name, sky, ground);
    }

    protected void applyJungLayout(Class<? extends Layout> layoutType) {
        (new DoJungLayout(layoutType)).doAction();
    }

    /**
     * Creates the layout context menu
     * 
     * @param menu
     *            menu builder
     */
    /*   protected void constructLayoutMenu(MenuBuilder menu) {

        menu.addSection("Elastic layout");
        if (!getGround().isElasticMode()) {
            menu.addAction(new SetElasticLayoutAction("Enable", true));
        } else {
            menu.addAction(new SetElasticLayoutAction("Disable", false));
        }

        menu.addSection("Apply layout");

        MenuBuilder algorithmLayoutMenu = menu.addSubMenu("Algorithm");

        algorithmLayoutMenu.addAction(new JungLayoutAction(FeedForwardLayout.class, "Feed-Forward"));
        algorithmLayoutMenu.addAction(new JungLayoutAction(StretchedFeedForwardLayout.class, "Streched Feed-Forward"));
        algorithmLayoutMenu.addAction(new JungLayoutAction(CircleLayout.class, "Circle"));
        algorithmLayoutMenu.addAction(new JungLayoutAction(ISOMLayout.class, "ISOM"));

        MenuBuilder layoutSettings = algorithmLayoutMenu.addSubMenu("Settings");
        layoutSettings.addAction(new SetLayoutBoundsAction("Set preferred bounds", this));

    }
     */
    public void doFeedForwardLayout() {
        new JungLayoutAction(FeedForwardLayout.class, "Feed-Forward").doAction();
    }

    @Override
    protected void constructMenu(PopupMenuBuilder menu, Double posX, Double posY) {
        super.constructMenu(menu, posX, posY);
        //constructLayoutMenu(menu.addSubMenu("Layout"));
    }

    protected void constructMenu(PopupMenuBuilder menu) {
        super.constructMenu(menu, 0.0, 0.0);
    }

    /**
     * @return Layout bounds to be used by Layout algorithms
     */
    protected Dimension getLayoutBounds() {
        return layoutBounds;
    }

    @Override
    public ElasticGround getGround() {
        return (ElasticGround) super.getGround();
    }

    /**
     * @param bounds
     *            New bounds
     */
    public void setLayoutBounds(Dimension bounds) {
        this.layoutBounds = bounds;
    }

    /**
     * Activity for performing a Jung Layout.
     * 
     * @author Shu Wu
     */
    class DoJungLayout extends TrackedAction {

        private static final long serialVersionUID = 1L;

        private Layout layout;

        private final Class<? extends Layout> layoutType;

        public DoJungLayout(Class<? extends Layout> layoutType) {
            super("Performing layout: " + layoutType.getSimpleName());
            this.layoutType = layoutType;
        }

        @Override
        protected void action() throws ActionException {

            try {
                Class<?>[] ctArgs = new Class[1];
                ctArgs[0] = Graph.class;

                Constructor<?> ct = layoutType.getConstructor(ctArgs);
                Object[] args = new Object[1];

                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        getGround().updateGraph();
                    }
                });
                args[0] = getGround().getGraph();
                layout = (Layout) ct.newInstance(args);

            } catch (InvocationTargetException e) {
                e.getTargetException().printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                throw new ActionException("Could not apply layout: " + e.getMessage(), e);
            }

            layout.initialize(getLayoutBounds());

            if (layout.isIncremental()) {
                long timeNow = System.currentTimeMillis();
                while (!layout.incrementsAreDone()
                        && (System.currentTimeMillis() - timeNow < 1000 && !layout
                                .incrementsAreDone())) {
                    layout.advancePositions();
                }
            }
            /**
             * Layout nodes needs to be done in the Swing dispatcher thread
             */
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        getGround().updateChildrenFromLayout(layout, true, true);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Action for applying a Jung Layout. It implements LayoutAction, which
     * allows it to be reversable.
     * 
     * @author Shu
     */
    class JungLayoutAction extends LayoutAction {

        private static final long serialVersionUID = 1L;

        final Class<? extends Layout> layoutClass;

        public JungLayoutAction(Class<? extends Layout> layoutClass, String name) {
            super(ElasticWorld.this, "Apply layout " + name, name);
            this.layoutClass = layoutClass;
        }

        @Override
        protected void applyLayout() {
            getGround().setElasticEnabled(false);
            (new DoJungLayout(layoutClass)).doAction();
        }

    }

    /**
     * Action for starting and running a Iterable Jung Layout
     * 
     * @author Shu
     */
    class SetElasticLayoutAction extends LayoutAction {

        private static final long serialVersionUID = 1L;
        private final boolean enabled;

        public SetElasticLayoutAction(String name, boolean enabled) {
            super(ElasticWorld.this, "Set Spring Layout: " + enabled, name);
            this.enabled = enabled;
        }

        @Override
        protected void applyLayout() {
            getGround().setElasticEnabled(enabled);
        }

    }
}

/**
 * Action to set layout bounds.
 * 
 * @author Shu Wu
 */
class SetLayoutBoundsAction extends StandardAction {

    private static final Property pHeight = new PInt("Height");
    private static final Property pWidth = new PInt("Width");
    private static final long serialVersionUID = 1L;
    private static final Property[] zProperties = { pWidth, pHeight };

    private final ElasticWorld parent;

    public SetLayoutBoundsAction(String actionName, ElasticWorld parent) {
        super("Set layout bounds", actionName);
        this.parent = parent;
    }

    private void completeConfiguration(ConfigResult properties) {
        parent.setLayoutBounds(new Dimension((Integer) properties.getValue(pWidth),
                (Integer) properties.getValue(pHeight)));

    }

    @Override
    protected void action() throws ActionException {

        try {
            ConfigResult properties = ConfigManager.configure(zProperties, "Layout bounds",
                    UIEnvironment.getInstance(), ConfigMode.TEMPLATE_NOT_CHOOSABLE);
            completeConfiguration(properties);

        } catch (ConfigException e) {
            e.defaultHandleBehavior();
        }

    }

}
