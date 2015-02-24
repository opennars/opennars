package ca.nengo.ui.lib.world.piccolo;

import ca.nengo.ui.lib.objects.activities.TransientMessage;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.Destroyable;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.WorldLayer;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.WorldObject.Listener;
import ca.nengo.ui.lib.world.piccolo.primitives.PXNode;
import ca.nengo.ui.lib.world.piccolo.primitives.PiccoloNodeInWorld;
import org.piccolo2d.PCamera;
import org.piccolo2d.PNode;
import org.piccolo2d.activities.PInterpolatingActivity;
import org.piccolo2d.event.PInputEventListener;
import org.piccolo2d.util.PBounds;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;


/**
 * World objects are visible UI objects which exist in a World layer (Ground or
 * Sky).
 * 
 * @author Shu Wu
 */
public class WorldObjectImpl implements WorldObject {
    private static Hashtable<String, Property> EVENT_CONVERSION_TABLE_1;

    private static Hashtable<Property, String> EVENT_CONVERSION_TABLE_2;

    public static final Object[][] CONVERSION_MAP = new Object[][] {
        { Property.PARENTS_CHANGED, PNode.PROPERTY_PARENT },
        { Property.BOUNDS_CHANGED, PNode.PROPERTY_BOUNDS },
        { Property.PARENTS_BOUNDS, PXNode.PROPERTY_PARENT_BOUNDS },
        { Property.FULL_BOUNDS, PXNode.PROPERTY_GLOBAL_BOUNDS},
        { Property.GLOBAL_BOUNDS, PXNode.PROPERTY_GLOBAL_BOUNDS },
        { Property.VIEW_TRANSFORM, PCamera.PROPERTY_VIEW_TRANSFORM },
        { Property.REMOVED_FROM_WORLD, PXNode.PROPERTY_REMOVED_FROM_WORLD },
        { Property.CHILDREN_CHANGED, PNode.PROPERTY_CHILDREN } };

    public static final long TIME_BETWEEN_POPUPS = 1500;

    protected static Property piccoloEventToWorldEvent(String propertyName) {
        if (EVENT_CONVERSION_TABLE_1 == null) {
            EVENT_CONVERSION_TABLE_1 = new Hashtable<String, Property>(CONVERSION_MAP.length);
            for (Object[] conversion : CONVERSION_MAP) {
                EVENT_CONVERSION_TABLE_1.put((String) conversion[1], (Property) conversion[0]);

            }

        }

        return EVENT_CONVERSION_TABLE_1.get(propertyName);
    }

    protected static String worldEventToPiccoloEvent(Property type) {
        if (EVENT_CONVERSION_TABLE_2 == null) {
            EVENT_CONVERSION_TABLE_2 = new Hashtable<Property, String>(CONVERSION_MAP.length);
            for (Object[] conversion : CONVERSION_MAP) {
                EVENT_CONVERSION_TABLE_2.put((Property) conversion[0], (String) conversion[1]);
            }
        }
        return EVENT_CONVERSION_TABLE_2.get(type);
    }

    private final Set<ChildListener> childListeners = new HashSet<ChildListener>();

    private boolean draggable = true;

    private Map<Property, Map<Listener, ListenerAdapter>> eventListenerMap;

    /**
     * Whether this object has been destroyed
     */
    private boolean isDestroyed = false;

    /**
     * Whether this object is selectable by the Selection handler
     */
    private boolean isSelectable = true;

    /**
     * Whether this node is selected by a handler
     */
    private boolean isSelected = false;

    /**
     * The last time a popup was created
     */
    private long lastPopupTime = 0;

    /**
     * This object's name
     */
    private String myName;

    /**
     * Piccolo counterpart of this object
     */
    private PNode myPNode;

    protected WorldObjectImpl(String name, PiccoloNodeInWorld pNode) {
        super();

        if (pNode == null) {
            pNode = new PXNode();
        }

        if (!(pNode instanceof PNode)) {
            throw new InvalidParameterException();
        }

        myPNode = (PNode) pNode;

        ((PiccoloNodeInWorld) myPNode).setWorldObject(this);

        init(name);
    }

    /**
     * Creates an unnamed WorldObject
     */
    public WorldObjectImpl() {
        this("", null);
    }

    public WorldObjectImpl(PiccoloNodeInWorld node) {
        this("", node);
    }

    /**
     * Creates a named WorldObject
     * 
     * @param name
     *            Name of this object
     */
    public WorldObjectImpl(String name) {
        this(name, null);
    }

    private Collection<WorldObject> getChildrenInternal() {
        ArrayList<WorldObject> objects = new ArrayList(getPiccolo().getChildrenCount());

        Iterator<?> it = getPiccolo().getChildrenIterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof PiccoloNodeInWorld) {
                WorldObject wo = ((PiccoloNodeInWorld) next).getWorldObject();

                if (wo != null) {
                    objects.add(wo);
                }
            }
        }
        return objects;
    }

    /**
     * Initializes this instance
     * 
     * @param name
     *            Name of this Object
     */
    private void init(String name) {
        setSelectable(false);
        this.myName = name;
    }

    protected void firePropertyChange(Property event) {
        if (eventListenerMap != null) {
            Map<Listener, ListenerAdapter> eventListeners = eventListenerMap.get(event);
            if (eventListeners != null) {
                Set<Listener> listeners = eventListeners.keySet();
                for (Listener l : listeners)
                    l.propertyChanged(event);
            }

        }
    }

    // public boolean addActivity(PActivity arg0) {
    // return myPNode.addActivity(arg0);
    // }

    /**
     * Perform any operations before being destroyed
     */
    protected void prepareForDestroy() {

    }

    public void addChild(WorldObject wo) {
        addChild(wo, -1);
    }

    public void addChild(WorldObject wo, int index) {
        if (wo instanceof WorldObjectImpl) {
            if (index == -1) {
                myPNode.addChild(((WorldObjectImpl) wo).myPNode);
            } else {
                myPNode.addChild(index, ((WorldObjectImpl) wo).myPNode);
            }
        } else {
            throw new InvalidParameterException("Invalid child object");
        }
    }

    public void addChildrenListener(ChildListener listener) {
        if (childListeners.contains(listener)) {
            throw new InvalidParameterException();
        }
        childListeners.add(listener);
    }

    public void addInputEventListener(PInputEventListener arg0) {
        myPNode.addInputEventListener(arg0);
    }

    public void addPropertyChangeListener(Property eventType, Listener worldListener) {

        if (eventListenerMap == null) {
            eventListenerMap = new HashMap();
        }

        Map<Listener, ListenerAdapter> eventListeners = eventListenerMap.get(eventType);
        if (eventListeners == null) {
            eventListeners = new HashMap();
            eventListenerMap.put(eventType, eventListeners);
        }

        eventListeners.put(worldListener, new ListenerAdapter(eventType, worldListener));

        /*
         * If there is an associated piccolo event, add the listener to the
         * piccolo object as well
         */

        // if (piccoloPropertyName != null) {
        //
        // HashSet<PiccoloChangeListener> picoloListenerSet = piccoloListeners
        // .get(piccoloPropertyName);
        //
        // if (picoloListenerSet == null) {
        // picoloListenerSet = new Hashset<EventListener,
        // PiccoloChangeListener>();
        //
        // }
        // PiccoloChangeListener piccoloListener =
        // piccoloListeners.get(worldListener);
        //
        // if (piccoloListener == null) {
        // piccoloListener = new PiccoloChangeListener(worldListener);
        // piccoloListeners.put(worldListener, piccoloListener);
        // }
        //
        // getPiccolo().addPropertyChangeListener(worldEventToPiccoloEvent(eventType),
        // piccoloListener);
        //
        // }
    }

    public PInterpolatingActivity animateToBounds(double x, double y, double width, double height,
            long duration) {
        return myPNode.animateToBounds(x, y, width, height, duration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#animateToPosition(double,
     *      double, long)
     */
    public void animateToPosition(double x, double y, long duration) {
        myPNode.animateToPositionScaleRotation(x, y, myPNode.getScale(), myPNode.getRotation(),
                duration);
    }

    public void animateToPositionScaleRotation(double x, double y, double scale, double theta,
            long duration) {
        myPNode.animateToPositionScaleRotation(x, y, scale, theta, duration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#animateToScale(double, long)
     */
    public void animateToScale(double scale, long duration) {
        myPNode.animateToPositionScaleRotation(myPNode.getOffset().getX(), myPNode.getOffset()
                .getY(), scale, myPNode.getRotation(), duration);
    }

    public void animateToTransparency(float transparency, long duration) {
        myPNode.animateToTransparency(transparency, duration);
    }

    public void childAdded(WorldObject wo) {
        for (ChildListener listener : childListeners) {
            listener.childAdded(wo);
        }
    }

    public void childRemoved(WorldObject wo) {
        for (ChildListener listener : childListeners) {
            listener.childRemoved(wo);
        }
    }

    public final void destroy() {
        if (!isDestroyed) {
            isDestroyed = true;

            prepareForDestroy();

            destroyChildren();

            if (myPNode instanceof PXNode) {
                ((PXNode) myPNode).removeFromWorld();
            }

        }
    }

    public final void destroyChildren() {
        /*
         * Copy to list to avoid concurrency error
         */
        List<WorldObject> objects = new ArrayList<WorldObject>(getChildrenCount());
        for (WorldObject wo : getChildren()) {
            objects.add(wo);
        }
        for (WorldObject wo : objects) {
            wo.destroy();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#doubleClicked()
     */
    public void doubleClicked() {
    }

    public void dragOffset(double dx, double dy) {
        if (isDraggable()) {
            Point2D offset = getOffset();
            offset.setLocation(offset.getX() + dx, offset.getY() + dy);
            setOffset(offset);
        }
    }

    public Collection<WorldObject> findIntersectingNodes(Rectangle2D fullBounds) {
        ArrayList<PNode> interesectingNodes = new ArrayList<PNode>();
        myPNode.findIntersectingNodes(fullBounds, interesectingNodes);

        Collection<WorldObject> interesectingObjects = new ArrayList<WorldObject>(
                interesectingNodes.size());

        for (PNode node : interesectingNodes) {
            if (node instanceof PiccoloNodeInWorld && node.getVisible()) {
                interesectingObjects.add(((PiccoloNodeInWorld) node).getWorldObject());
            }

        }
        return interesectingObjects;
    }

    public PBounds getBounds() {
        return myPNode.getBounds();
    }

    public Collection<WorldObject> getChildren() {
        return getChildrenInternal();
    }

    public int getChildrenCount() {
        return getChildrenInternal().size();
    }

    public PBounds getFullBounds() {
        return myPNode.getFullBounds();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#getHeight()
     */
    public double getHeight() {
        return myPNode.getHeight();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#getName()
     */
    public String getName() {
        return myName;
    }

    public Point2D getOffset() {
        return myPNode.getOffset();
    }

    public WorldObject getParent() {
        PNode parent = getPiccolo().getParent();
        if (parent != null) {
            return ((PiccoloNodeInWorld) parent).getWorldObject();
        } else {
            return null;
        }
    }

    public PNode getPiccolo() {
        return myPNode;
    }

    public double getRotation() {
        return myPNode.getRotation();
    }

    public double getScale() {
        return myPNode.getScale();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#getTooltip()
     */
    public WorldObject getTooltip() {
        return null;
    }

    public float getTransparency() {
        return myPNode.getTransparency();
    }

    public boolean getVisible() {
        return myPNode.getVisible();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#getWidth()
     */
    public double getWidth() {
        return myPNode.getWidth();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#getWorld()
     */
    public WorldImpl getWorld() {
        if (getWorldLayer() != null) {
            return getWorldLayer().getWorld();
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#getWorldLayer()
     */
    public WorldLayer getWorldLayer() {
        PNode node = myPNode;

        while (node != null) {
            if (node instanceof PiccoloNodeInWorld) {
                WorldObject wo = ((PiccoloNodeInWorld) node).getWorldObject();

                if (wo instanceof WorldLayer) {
                    return (WorldLayer) wo;
                }
            }

            node = node.getParent();
        }

        return null;

    }

    public double getX() {
        return myPNode.getX();
    }

    public double getY() {
        return myPNode.getY();
    }

    public Dimension2D globalToLocal(Dimension2D globalDimension) {
        return myPNode.globalToLocal(globalDimension);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#globalToLocal(java.awt.geom.Point2D)
     */
    public Point2D globalToLocal(Point2D arg0) {
        return myPNode.globalToLocal(arg0);
    }

    public Rectangle2D globalToLocal(Rectangle2D globalPoint) {
        return myPNode.globalToLocal(globalPoint);
    }

    public boolean isAncestorOf(WorldObject wo) {
        return getPiccolo().isAncestorOf(((WorldObjectImpl) wo).getPiccolo());
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#isAnimating()
     */
    public boolean isAnimating() {
        if (myPNode instanceof PiccoloNodeInWorld) {
            return ((PiccoloNodeInWorld) myPNode).isAnimating();
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#isDestroyed()
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    public boolean isDraggable() {
        return draggable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#isSelectable()
     */
    public boolean isSelectable() {
        return isSelectable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#isSelected()
     */
    public boolean isSelected() {
        return isSelected;
    }

    public void layoutChildren() {

    }

    public Point2D localToGlobal(Point2D arg0) {
        return myPNode.localToGlobal(arg0);
    }

    public Rectangle2D localToGlobal(Rectangle2D arg0) {
        return myPNode.localToGlobal(arg0);
    }

    public Dimension2D localToParent(Dimension2D localRectangle) {
        return myPNode.localToParent(localRectangle);
    }

    public Point2D localToParent(Point2D localPoint) {
        return myPNode.localToParent(localPoint);
    }

    public Rectangle2D localToParent(Rectangle2D localRectangle) {
        return myPNode.localToParent(localRectangle);
    }

    public void moveToBack() {
        myPNode.lowerToBottom();
    }

    public void moveToFront() {
        myPNode.raiseToTop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#objectToGround(java.awt.geom.Point2D)
     */
    public Point2D objectToGround(Point2D position) {
        WorldLayer layer = getWorldLayer();

        myPNode.localToGlobal(position);

        if (layer instanceof WorldSkyImpl) {
            layer.getWorld().getSky().localToView(position);
            return position;
        } else if (layer instanceof WorldGroundImpl) {
            return position;
        }
        return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#objectToGround(java.awt.geom.Rectangle2D)
     */
    public Rectangle2D objectToGround(Rectangle2D rectangle) {
        WorldLayer layer = getWorldLayer();

        myPNode.localToGlobal(rectangle);

        if (layer instanceof WorldSkyImpl) {
            layer.getWorld().getSky().localToView(rectangle);
            return rectangle;
        } else if (layer instanceof WorldGroundImpl) {
            return rectangle;
        }
        return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#objectToSky(java.awt.geom.Point2D)
     */
    public Point2D objectToSky(Point2D position) {
        WorldLayer layer = getWorldLayer();

        myPNode.localToGlobal(position);

        if (layer instanceof WorldGroundImpl) {
            layer.getWorld().getSky().viewToLocal(position);
            return position;
        } else if (layer instanceof WorldSkyImpl) {
            return position;
        } else {
            throw new InvalidParameterException();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#objectToSky(java.awt.geom.Rectangle2D)
     */
    public Rectangle2D objectToSky(Rectangle2D rectangle) {
        WorldLayer layer = getWorldLayer();

        myPNode.localToGlobal(rectangle);

        if (layer != null) {
            if (layer instanceof WorldGroundImpl) {
                layer.getWorld().getSky().viewToLocal(rectangle);

            } else if (layer instanceof WorldSkyImpl) {

            } else {
                throw new InvalidParameterException();
            }
        }
        return rectangle;
    }

    public void paint(PaintContext paintContext) {

    }

    public Point2D parentToLocal(Point2D parentPoint) {
        return myPNode.parentToLocal(parentPoint);
    }

    public Rectangle2D parentToLocal(Rectangle2D parentRectangle) {
        return myPNode.parentToLocal(parentRectangle);
    }

    public void removeChild(WorldObject wo) {
        if (wo instanceof WorldObjectImpl) {
            myPNode.removeChild(((WorldObjectImpl) wo).getPiccolo());
        } else {
            throw new InvalidParameterException("Invalid child object");
        }
    }

    public void removeChildrenListener(ChildListener listener) {
        if (!childListeners.contains(listener)) {
            throw new InvalidParameterException();
        }
        childListeners.remove(listener);
    }

    public void removeFromParent() {
        myPNode.removeFromParent();
    }

    public void removeFromWorld() {
        if (myPNode instanceof PXNode) {
            ((PXNode) myPNode).removeFromWorld();
        }
    }

    public void removeInputEventListener(PInputEventListener arg0) {
        myPNode.removeInputEventListener(arg0);
    }

    public void removePropertyChangeListener(Property event, Listener listener) {
        boolean successfull = false;
        if (eventListenerMap != null) {
            Map<Listener, ListenerAdapter> eventListeners = eventListenerMap.get(event);

            if (eventListeners != null) {

                if (eventListeners.containsKey(listener)) {
                    ListenerAdapter adapter = eventListeners.get(listener);
                    adapter.destroy();
                    eventListeners.remove(listener);
                    successfull = true;
                }
            }
        }
        if (!successfull) {
            throw new InvalidParameterException("Listener is not attached");
        }
    }

    public void repaint() {
        myPNode.repaint();
    }

    public boolean setBounds(double arg0, double arg1, double arg2, double arg3) {
        return myPNode.setBounds(arg0, arg1, arg2, arg3);
    }

    public boolean setBounds(Rectangle2D arg0) {
        return myPNode.setBounds(arg0);
    }

    public void setChildrenPickable(boolean areChildrenPickable) {
        myPNode.setChildrenPickable(areChildrenPickable);

    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    public boolean setHeight(double height) {
        return myPNode.setHeight(height);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#setName(java.lang.String)
     */
    public void setName(String name) {
        this.myName = name;
    }

    public void setOffset(double arg0, double arg1) {
        myPNode.setOffset(arg0, arg1);
    }

    public void setOffset(Point2D arg0) {
        setOffset(arg0.getX(), arg0.getY());
    }

    public void setPaint(Paint arg0) {
        myPNode.setPaint(arg0);
    }

    public void setPickable(boolean isPickable) {
        myPNode.setPickable(isPickable);
    }

    public void setRotation(double theta) {
        myPNode.setRotation(theta);
    }

    public void setScale(double arg0) {
        myPNode.setScale(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#setSelectable(boolean)
     */
    public void setSelectable(boolean isSelectable) {
        this.isSelectable = isSelectable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#setSelected(boolean)
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setTransparency(float zeroToOne) {
        myPNode.setTransparency(zeroToOne);

    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#setVisible(boolean)
     */
    public void setVisible(boolean isVisible) {
        myPNode.setVisible(isVisible);
    }

    public boolean setWidth(double width) {
        return myPNode.setWidth(width);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#showPopupMessage(java.lang.String)
     */
    public synchronized void showPopupMessage(String msg) {
        if (getWorld() != null) {

            Util.debugMsg("UI Popup Msg: " + msg);

            TransientMessage msgObject = new TransientMessage(msg);

            double offsetX = -(msgObject.getWidth() - myPNode.getWidth()) / 2d;

            Point2D position = objectToSky(new Point2D.Double(offsetX, -5));

            msgObject.setOffset(position);
            getWorld().getSky().addChild(msgObject);

            long currentTime = System.currentTimeMillis();
            long delay = TIME_BETWEEN_POPUPS - (currentTime - lastPopupTime);

            if (delay < 0) {
                delay = 0;
            }

            msgObject.popup(delay);

            lastPopupTime = currentTime + delay;
        }
    }

    public String toString() {
        return getName();
    }

    public void translate(double dx, double dy) {
        myPNode.translate(dx, dy);
    }

    class ListenerAdapter implements Destroyable {
        private final Property eventType;
        private final Listener listener;
        private PiccoloChangeListener piccoloListener;

        public ListenerAdapter(Property eventType, Listener listener) {
            this.listener = listener;
            this.eventType = eventType;

            String piccoloPropertyName = worldEventToPiccoloEvent(eventType);
            if (piccoloPropertyName != null) {
                piccoloListener = new PiccoloChangeListener(listener);

                getPiccolo().addPropertyChangeListener(worldEventToPiccoloEvent(eventType),
                        piccoloListener);
            }
        }

        public void destroy() {
            if (piccoloListener != null) {
                getPiccolo().removePropertyChangeListener(worldEventToPiccoloEvent(eventType),
                        piccoloListener);
            }
        }

        public Listener getListener() {
            return listener;
        }
    }

    public void altClicked() {
        // override this to implement action
    }

}

/**
 * Adapater for WorldObjectChange listener to PropertyChangeListener
 * 
 * @author Shu Wu
 */
class PiccoloChangeListener implements PropertyChangeListener {
    final Listener woChangeListener;

    public PiccoloChangeListener(Listener worldListener) {
        super();
        this.woChangeListener = worldListener;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        woChangeListener.propertyChanged(WorldObjectImpl.piccoloEventToWorldEvent(evt
                .getPropertyName()));
    }
}