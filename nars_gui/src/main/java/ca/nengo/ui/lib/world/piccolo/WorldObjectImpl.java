package ca.nengo.ui.lib.world.piccolo;

import ca.nengo.ui.lib.object.activity.TransientMessage;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.Destroyable;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.WorldLayer;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.WorldObject.Listener;
import ca.nengo.ui.lib.world.piccolo.primitive.PXNode;
import ca.nengo.ui.lib.world.piccolo.primitive.PiccoloNodeInWorld;
import com.google.common.base.Function;
import com.google.common.collect.*;
import org.piccolo2d.PCamera;
import org.piccolo2d.PNode;
import org.piccolo2d.activities.PInterpolatingActivity;
import org.piccolo2d.event.PInputEventListener;
import org.piccolo2d.extras.nodes.PNodeCache;
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


    public static final Object[][] CONVERSION_MAP = new Object[][] {
            { Property.PARENTS_CHANGED, PNode.PROPERTY_PARENT },
            { Property.BOUNDS_CHANGED, PNode.PROPERTY_BOUNDS },
            { Property.PARENTS_BOUNDS, PXNode.PROPERTY_PARENT_BOUNDS },
            { Property.FULL_BOUNDS, PXNode.PROPERTY_GLOBAL_BOUNDS},
            { Property.GLOBAL_BOUNDS, PXNode.PROPERTY_GLOBAL_BOUNDS },
            { Property.VIEW_TRANSFORM, PCamera.PROPERTY_VIEW_TRANSFORM },
            { Property.REMOVED_FROM_WORLD, PXNode.PROPERTY_REMOVED_FROM_WORLD },
            { Property.CHILDREN_CHANGED, PNode.PROPERTY_CHILDREN } };

    private static EnumHashBiMap<Property, String> EVENT_CONVERSION_TABLE_2 = EnumHashBiMap.create(Property.class);
    private static BiMap<String, Property> EVENT_CONVERSION_TABLE_1;
    static {
        EVENT_CONVERSION_TABLE_1 = EVENT_CONVERSION_TABLE_2.inverse();
    }


    public static final long TIME_BETWEEN_POPUPS = 1500;


    protected static Property piccoloEventToWorldEvent(final String propertyName) {
        return EVENT_CONVERSION_TABLE_1.get(propertyName);
    }


    protected static String worldEventToPiccoloEvent(final Property type) {
        return EVENT_CONVERSION_TABLE_2.get(type);
    }

    private final Set<ChildListener> childListeners = new LinkedHashSet(1);

    private boolean draggable = true;

    //private Map<Property, Map<Listener, ListenerAdapter>> eventListenerMap;
    private Table<Property, Listener, ListenerAdapter> eventListenerMap;


    /**
     * Perform any operations before being destroyed
     */
    protected void prepareForDestroy() {
    }

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
    protected final PNode pnode;

    protected WorldObjectImpl(String name, PiccoloNodeInWorld pNode) {
        super();

        if (pNode == null) {
            pNode = new PXNode();
        }
        else {
            if (!(pNode instanceof PNode)) {
                throw new InvalidParameterException();
            }
        }

        pnode = (PNode) pNode;

        ((PiccoloNodeInWorld) pnode).setWorldObject(this);

        init(name);
    }

//    @Override
//    public int hashCode() {
//        return getName().hashCode();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        return (obj == this);
//    }

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

//    @Deprecated private Collection<WorldObject> getChildrenInternal() {
//        List piccoloChildren = getPiccolo().getChildrenReference();
//        int numChildren = piccoloChildren.size();
//        List<WorldObject> objects = new ArrayList(numChildren);
//
//        for (int i = 0; i < numChildren; i++) {
//            Object next = piccoloChildren.get(i);
//            if (next instanceof PiccoloNodeInWorld) {
//                WorldObject wo = ((PiccoloNodeInWorld) next).getWorldObject();
//
//                if (wo != null) {
//                    objects.add(wo);
//                }
//            }
//        }
//        return objects;
//    }

    public int getChildrenCount() {
        return Iterables.size(getChildren());
    }

    public Iterable<WorldObject> getChildren() {
        return Iterables.transform(Iterables.filter(getPNode().getChildrenReference(), PiccoloNodeInWorld.class),
                new Function<PiccoloNodeInWorld,WorldObject>() {
                    @Override public WorldObject apply(PiccoloNodeInWorld input) {
                        return input.getWorldObject();
                    }
                });
    }

    /**
     * Initializes this instance
     * 
     * @param name
     *            Name of this Object
     */
    protected void init(String name) {
        setSelectable(false);
        this.myName = name;
    }

    protected void firePropertyChange(final Property event) {
        if (eventListenerMap != null) {
            for (Listener l :eventListenerMap.row(event).keySet())
                l.propertyChanged(event);
        }
    }

    // public boolean addActivity(PActivity arg0) {
    // return myPNode.addActivity(arg0);
    // }


    @Override
    public PNode getPNode() {
        return pnode;
    }

    public void addChild(WorldObject wo) {
        addChild(wo, -1);
    }
    public void addChildren(Collection<WorldObject> wo) {
        pnode.addChildren(Collections2.transform(wo, new Function<WorldObject, PNode>() {

            @Override
            public PNode apply(WorldObject input) {
                return input.getPNode();
            }
        }));
    }

    public void addChild(WorldObject wo, int index) {
        if (index == -1) {
            pnode.addChild(wo.getPNode());
        } else {
            pnode.addChild(index, wo.getPNode());
        }
    }
    public PNodeCache addChildCache(WorldObject wo) {
        PNodeCache cacheNode = new PNodeCache();
        cacheNode.addChild(wo.getPNode());
        pnode.addChild(cacheNode);
        return cacheNode;
    }
    public PNodeCache addChildCache(PNode p) {
        PNodeCache cacheNode = new PNodeCache();
        cacheNode.addChild(p);
        pnode.addChild(cacheNode);
        return cacheNode;
    }

    public void addChildrenListener(ChildListener listener) {
        if (!childListeners.add(listener)) {
            throw new InvalidParameterException();
        }
    }

    public void addInputEventListener(PInputEventListener arg0) {
        pnode.addInputEventListener(arg0);
    }

    public void addPropertyChangeListener(final Property eventType, final Listener worldListener) {

        if (eventListenerMap == null) {
            eventListenerMap = HashBasedTable.create();
        }

        eventListenerMap.put(eventType, worldListener, new ListenerAdapter(this, eventType, worldListener));

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
        return pnode.animateToBounds(x, y, width, height, duration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#animateToPosition(double,
     *      double, long)
     */
    @Override
    public void animateToPosition(double x, double y, long duration) {
        pnode.animateToPositionScaleRotation(x, y, pnode.getScale(), pnode.getRotation(),
                duration);
    }


    public void animateToPositionScaleRotation(double x, double y, double scale, double theta,
            long duration) {
        pnode.animateToPositionScaleRotation(x, y, scale, theta, duration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#animateToScale(double, long)
     */
    public void animateToScale(double scale, long duration) {
        pnode.animateToPositionScaleRotation(pnode.getOffset().getX(), pnode.getOffset()
                .getY(), scale, pnode.getRotation(), duration);
    }

    public void animateToTransparency(float transparency, long duration) {
        pnode.animateToTransparency(transparency, duration);
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

    synchronized public final void destroy() {
        if (isDestroyed)
            return;

        isDestroyed = true;

        setVisible(false);

        prepareForDestroy();


        Collection children = getChildrenReference();




        removeFromParent();
        removeFromWorld();

        pnode.removeAllChildren();
        if (pnode instanceof PXNode) {
            ((PXNode) pnode).removeFromWorld();
        }
        else {
            pnode.removeFromParent();
        }
        for (Object o : children) {
            if (o instanceof WorldObject)
                ((WorldObject)o).destroy();
        }
//        if (eventListenerMap!=null)
//            eventListenerMap.clear();
//
//        if (childListeners!=null)
//            childListeners.clear();

    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#doubleClicked()
     */
    public void doubleClicked() {
    }

    public void dragTo(double x, double y) {
        if (isDraggable()) {
            Point2D offset = getOffset();
            offset.setLocation(x, y);
            setOffset(offset);
        }
    }

    public void dragTo(double x, double y, double speed, double arrivalSpeed /* max speed */) {
        final double epsilon = 20f; //min movement which wont matter

        if (isDraggable()) {
            Point2D offset = getOffset();
            double cx = offset.getX(), cy = offset.getY();
            double dx = x - cx, dy = y - cy;
            double normSquare = dx * dx + dy * dy;
            double nx, ny;
            if (normSquare < epsilon*epsilon) {
                return;
            }
            else if (normSquare < speed*speed) {
                //nx = x; ny = y;

                //apply arrival speed (LERP)
                nx = (x * arrivalSpeed) + (1.0 - arrivalSpeed) * cx;
                ny = (y * arrivalSpeed) + (1.0 - arrivalSpeed) * cy;
            }
            else {
                double norm = Math.sqrt(normSquare);
                dx /= norm; dy /= norm;
                nx = cx + dx;
                ny = cy + dy;
            }
            offset.setLocation(nx, ny);
            setOffset(offset);
        }
    }

    public boolean scaleTo(double targetScale, double speed) {
        double nextScale = getScale() * (1.0 - speed) + targetScale * speed;
        return setScale( nextScale );
    }


    public void dragOffset(double dx, double dy) {
        if (isDraggable()) {
            Point2D offset = getOffset();
            offset.setLocation(offset.getX() + dx, offset.getY() + dy);
            setOffset(offset);
        }
    }

    public Collection<WorldObject> findIntersectingNodes(Rectangle2D fullBounds, List<WorldObject> intersectingObjectsBuffer) {
        ArrayList<PNode> intersectingNodes = new ArrayList<PNode>();
        pnode.findIntersectingNodes(fullBounds, intersectingNodes);

        if (intersectingObjectsBuffer == null) {
            intersectingObjectsBuffer = new ArrayList<WorldObject>(
                    intersectingNodes.size());
        }
        else {
            intersectingObjectsBuffer.clear();
        }

        for (int i = 0; i < intersectingNodes.size(); i++) {
            PNode node = intersectingNodes.get(i);
            if (node instanceof PiccoloNodeInWorld && node.getVisible()) {
                WorldObject wo = ((PiccoloNodeInWorld) node).getWorldObject();
                if (wo != null)
                    intersectingObjectsBuffer.add(wo);
            }

        }
        return intersectingObjectsBuffer;
    }

    public PBounds getBounds() {
        return pnode.getBounds();
    }


    public Collection<WorldObject> getChildrenReference() {
        return getPNode().getChildrenReference();
    }


    public PBounds getFullBoundsClone() {
        return pnode.getFullBounds();
    }

    @Override
    public Rectangle2D getFullBoundsReference() {
        return pnode.getFullBoundsReference();
    }

    public double getCenterX() {
        return getFullBoundsReference().getCenterX();
    }
    public double getCenterY() {
        return getFullBoundsReference().getCenterY();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#getHeight()
     */
    public double getHeight() {
        return pnode.getHeight();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#getName()
     */
    public String name() {
        return myName;
    }

    public Point2D getOffset() {
        return pnode.getOffset();
    }

    public WorldObject getParent() {
        PNode parent = getPNode().getParent();
        if (parent != null) {
            return ((PiccoloNodeInWorld) parent).getWorldObject();
        } else {
            return null;
        }
    }

    public double getRotation() {
        return pnode.getRotation();
    }

    public double getScale() {
        return pnode.getScale();
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
        return pnode.getTransparency();
    }

    public boolean getVisible() {
        return pnode.getVisible() && !isDestroyed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#getWidth()
     */
    public double getWidth() {
        return pnode.getWidth();
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
        PNode node = this.pnode;

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
        return pnode.getX();
    }

    public double getY() {
        return pnode.getY();
    }

    public Dimension2D globalToLocal(Dimension2D globalDimension) {
        return pnode.globalToLocal(globalDimension);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#globalToLocal(java.awt.geom.Point2D)
     */
    public Point2D globalToLocal(Point2D arg0) {
        return pnode.globalToLocal(arg0);
    }

    public Rectangle2D globalToLocal(Rectangle2D globalPoint) {
        return pnode.globalToLocal(globalPoint);
    }

    public boolean isAncestorOf(WorldObject wo) {
        return getPNode().isAncestorOf(((WorldObjectImpl) wo).getPNode());
    }


    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#isAnimating()
     */
    public boolean isAnimating(long now) {
        if (pnode instanceof PiccoloNodeInWorld) {
            return ((PiccoloNodeInWorld) pnode).isAnimating(now);
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
        return pnode.localToGlobal(arg0);
    }

    public Rectangle2D localToGlobal(Rectangle2D arg0) {
        return pnode.localToGlobal(arg0);
    }

    public Dimension2D localToParent(Dimension2D localRectangle) {
        return pnode.localToParent(localRectangle);
    }

    public Point2D localToParent(Point2D localPoint) {
        if (localPoint == null) localPoint = new Point2D.Double(0,0);
        return pnode.localToParent(localPoint);
    }

    public Rectangle2D localToParent(Rectangle2D localRectangle) {
        return pnode.localToParent(localRectangle);
    }

    public void moveToBack() {
        pnode.lowerToBottom();
    }

    public void moveToFront() {
        pnode.raiseToTop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#objectToGround(java.awt.geom.Point2D)
     */
    public Point2D objectToGround(Point2D position) {
        WorldLayer layer = getWorldLayer();

        pnode.localToGlobal(position);

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

        pnode.localToGlobal(rectangle);

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

        pnode.localToGlobal(position);

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

        pnode.localToGlobal(rectangle);

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
        return pnode.parentToLocal(parentPoint);
    }

    public Rectangle2D parentToLocal(Rectangle2D parentRectangle) {
        return pnode.parentToLocal(parentRectangle);
    }

    public void removeChild(WorldObject wo) {
        pnode.removeChild(wo.getPNode());
    }

    public void removeChildrenListener(ChildListener listener) {
        if (!childListeners.remove(listener)) {
            throw new InvalidParameterException();
        }
    }

    public void removeFromParent() {
        pnode.removeFromParent();
    }

    public void removeFromWorld() {
        if (pnode instanceof PXNode) {
            ((PXNode) pnode).removeFromWorld();
        }
    }

    public void removeInputEventListener(PInputEventListener arg0) {
        pnode.removeInputEventListener(arg0);
    }

    public void removePropertyChangeListener(Property event, Listener listener) {
        boolean successfull = false;
        if (eventListenerMap != null) {
            ListenerAdapter adapter = eventListenerMap.remove(event, listener);
            if (adapter!=null) {
                adapter.destroy();
                successfull = true;
            }
        }
        if (!successfull && !isDestroyed()) {
            //throw new InvalidParameterException("Listener is not attached");
        }
    }

    public void repaint() {
        pnode.repaint();
    }

    public boolean setBounds(double arg0, double arg1, double arg2, double arg3) {
        return pnode.setBounds(arg0, arg1, arg2, arg3);
    }

    public boolean setBounds(Rectangle2D arg0) {
        return pnode.setBounds(arg0);
    }

    public void setChildrenPickable(boolean areChildrenPickable) {
        pnode.setChildrenPickable(areChildrenPickable);

    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    public boolean setHeight(double height) {
        return pnode.setHeight(height);
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
        pnode.setOffset(arg0, arg1);
        firePropertyChange(Property.GLOBAL_BOUNDS);

    }

    public void setOffset(Point2D arg0) {
        setOffset(arg0.getX(), arg0.getY());
    }

    public void setPaint(Paint p) {
        Paint oldPaint = pnode.getPaint();
        if (oldPaint!=null && oldPaint.equals(p))
            return;//same paint
        Color c;
        pnode.setPaint(p);
    }

    public void setPickable(boolean isPickable) {
        pnode.setPickable(isPickable);
    }


    final static double rotationEpsilon = 0.01;
    final static double scaleEpsilon = 0.01;
    public boolean setRotation(double theta) {
        if (Math.abs(getRotation() - theta) < rotationEpsilon)
            return false;
        pnode.setRotation(theta);
        return true;
    }

    public boolean setScale(double s) {
        if (Math.abs(getScale() - s) < scaleEpsilon)
            return false;
        pnode.setScale(s);
        return true;
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

    public void setTransparency(final float zeroToOne) {
        pnode.setTransparency(zeroToOne);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#setVisible(boolean)
     */
    public void setVisible(boolean isVisible) {
        pnode.setVisible(isVisible);
    }

    public boolean setWidth(double width) {
        return pnode.setWidth(width);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.shu.ui.lib.world.impl.IWorldObject#showPopupMessage(java.lang.String)
     */
    public synchronized void showPopupMessage(String msg) {
        if (getWorld() != null) {

            if (UIEnvironment.isDebugEnabled())
                Util.debugMsg("UI Popup Msg: " + msg);

            TransientMessage msgObject = new TransientMessage(msg);

            double offsetX = -(msgObject.getWidth() - pnode.getWidth()) / 2d;

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
        return name();
    }

    public void translate(double dx, double dy) {
        pnode.translate(dx, dy);
    }

    static class ListenerAdapter implements Destroyable {
        private final Property eventType;
        private final Listener listener;
        private final PiccoloChangeListener piccoloListener;
        private WorldObjectImpl piccolo;

        public ListenerAdapter(WorldObjectImpl piccolo, Property eventType, Listener listener) {
            this.listener = listener;
            this.eventType = eventType;

            String piccoloPropertyName = worldEventToPiccoloEvent(eventType);
            if (piccoloPropertyName != null) {
                piccoloListener = new PiccoloChangeListener(listener);

                piccolo.getPNode().addPropertyChangeListener(worldEventToPiccoloEvent(eventType),
                        piccoloListener);
            }
            else
                piccoloListener = null;
            this.piccolo = piccolo;
        }

        public void destroy() {
            if (piccoloListener != null) {
                piccolo.getPNode().removePropertyChangeListener(worldEventToPiccoloEvent(eventType),
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