package ca.nengo.ui.lib.world;

import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import org.piccolo2d.PNode;
import org.piccolo2d.activities.PInterpolatingActivity;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public interface WorldObject extends NamedObject, Destroyable {

    public boolean isDraggable();

    public void setDraggable(boolean draggable);

    public void addChild(WorldObject wo);

    public void addChildrenListener(ChildListener listener);

    public void addPropertyChangeListener(Property event, Listener listener);

    /**
     * Animate this node's bounds from their current location when the activity
     * starts to the specified bounds. If this node descends from the root then
     * the activity will be scheduled, else the returned activity should be
     * scheduled manually. If two different transform activities are scheduled
     * for the same node at the same time, they will both be applied to the
     * node, but the last one scheduled will be applied last on each frame, so
     * it will appear to have replaced the original. Generally you will not want
     * to do that. Note this method animates the node's bounds, but does not
     * change the node's transform. Use animateTransformToBounds() to animate
     * the node's transform instead.
     *
     * @param duration amount of time that the animation should take
     * @return the newly scheduled activity
     */
    public PInterpolatingActivity animateToBounds(double x, double y,
                                                  double width, double height, long duration);

    public void animateToPosition(double x, double y, long duration);

    default public void animateToPosition(double x, double y, double durationSec) {
        animateToPosition(x, y, (long) (1000 * durationSec));
    }

    /**
     * Animate this node's transform from its current location when the activity
     * starts to the specified location, scale, and rotation. If this node
     * descends from the root then the activity will be scheduled, else the
     * returned activity should be scheduled manually. If two different
     * transform activities are scheduled for the same node at the same time,
     * they will both be applied to the node, but the last one scheduled will be
     * applied last on each frame, so it will appear to have replaced the
     * original. Generally you will not want to do that.
     *
     * @param duration amount of time that the animation should take
     * @param theta    final theta value (in radians) for the animation
     * @return the newly scheduled activity
     */
    public void animateToPositionScaleRotation(double x, double y,
                                               double scale, double theta, long duration);

    /**
     * @param scale    Scale to animate to
     * @param duration Duration of animation
     */
    public void animateToScale(double scale, long duration);

    /**
     * @param transparency Transparency to animate to
     * @param duration     Duration of animation
     */
    public void animateToTransparency(float transparency, long duration);

    /**
     * @param wo Child which has just been added
     */
    public void childAdded(WorldObject wo);

    /**
     * @param wo Child which has just been removed
     */
    public void childRemoved(WorldObject wo);


    /**
     * Called if this object is double clicked on
     */
    public void doubleClicked();

    /**
     * Called if this object is clicked on with the 'alt' key held
     */
    public void altClicked();

    /**
     * Offset this node relative to the parents coordinate system, and is NOT
     * effected by this nodes current scale or rotation. This is implemented by
     * directly adding dx to the m02 position and dy to the m12 position in the
     * affine transform.
     */
    public void dragOffset(double dx, double dy);

    public Collection<WorldObject> findIntersectingNodes(Rectangle2D fullBounds, java.util.List<WorldObject> intersectingObjectsBuffer);

    /**
     * Return a copy of this node's bounds. These bounds are stored in the local
     * coordinate system of this node and do not include the bounds of any of
     * this node's children.
     */
    public Rectangle2D getBounds();

    /**
     * Return the list used to manage this node's children. This list should not
     * be modified.
     *
     * @return reference to the children list
     */
    public Iterable<WorldObject> getChildren();

    /**
     * Schedule the given activity with the root, note that only scheduled
     * activities will be stepped. If the activity is successfully added true is
     * returned, else false.
     *
     * @param activity
     *            new activity to schedule
     * @return true if the activity is successfully scheduled.
     */
    // public boolean addActivity(PActivity activity);

    /**
     * @return Number of children
     */
    public int getChildrenCount();

    /**
     * Return a copy of this node's full bounds. These bounds are stored in the
     * parent coordinate system of this node and they include the union of this
     * node's bounds and all the bounds of it's descendents.
     *
     * @return a copy of this node's full bounds.
     */
    public Rectangle2D getFullBoundsClone();

    /**
     * @return
     */
    public double getHeight();

    /**
     * @return
     */
    public String name();

    /**
     * @param name New name for this object
     */
    public void setName(String name);

    /**
     * Return the offset that is being applied to this node by its transform.
     * This offset effects this node and all of its descendents and is specified
     * in the parent coordinate system. This returns the values that are in the
     * m02 and m12 positions in the affine transform.
     *
     * @return a point representing the x and y offset
     */
    public Point2D getOffset();

    /**
     * Set the offset that is being applied to this node by its transform. This
     * offset effects this node and all of its descendents and is specified in
     * the nodes parent coordinate system. This directly sets the values of the
     * m02 and m12 positions in the affine transform. Unlike "PNode.translate()"
     * it is not effected by the transforms scale.
     *
     * @param point a point representing the x and y offset
     */
    public void setOffset(Point2D point);

    public WorldObject getParent();

    /**
     * Returns the rotation applied by this node's transform in radians. This
     * rotation affects this node and all its descendents. The value returned
     * will be between 0 and 2pi radians.
     *
     * @return rotation in radians.
     */
    public double getRotation();

    /**
     * Sets the rotation of this nodes transform in radians. This will affect
     * this node and all its descendents.
     *
     * @param theta rotation in radians
     */
    public boolean setRotation(double theta);

    /**
     * Return the scale applied by this node's transform. The scale is effecting
     * this node and all its descendents.
     *
     * @return scale applied by this nodes transform.
     */
    public double getScale();

    /**
     * Set the scale of this node's transform. The scale will affect this node
     * and all its descendents.
     *
     * @param s the scale to set the transform to
     */
    public boolean setScale(double s);

    /**
     * @return Tooltip object, null if there is none
     */
    public WorldObject getTooltip();

    /**
     * Return the transparency used when painting this node. Note that this
     * transparency is also applied to all of the node's descendents.
     */
    public float getTransparency();

    /**
     * Set the transparency used to paint this node. Note that this transparency
     * applies to this node and all of its descendents.
     */
    public void setTransparency(float zeroToOne);

    /**
     * Return true if this node is visible, that is if it will paint itself and
     * descendents.
     *
     * @return true if this node and its descendents are visible.
     */
    public boolean getVisible();

    public void setVisible(boolean isVisible);

    /**
     * @return
     */
    public double getWidth();

    /**
     * @return World which is an ancestor
     */
    public WorldImpl getWorld();

    /**
     * @return World layer which is an ancestor
     */
    public WorldLayer getWorldLayer();

    /**
     * Return the x position (in local coords) of this node's bounds.
     */
    public double getX();

    /**
     * Return the y position (in local coords) of this node's bounds.
     */
    public double getY();

    /**
     * Transform the given dimension from global coordinates to this node's
     * local coordinate system. Note that this will modify the dimension
     * parameter.
     *
     * @param globalDimension dimension in global coordinates to be transformed.
     * @return dimension in this node's local coordinate system.
     */
    public Dimension2D globalToLocal(Dimension2D globalDimension);

    /**
     * Converts a global coordinate to a local coordinate. This method modifies
     * the parameter.
     *
     * @param globalPoint Global coordinate
     * @return Local coordinate
     */
    public Point2D globalToLocal(Point2D globalPoint);

    /**
     * Converts a local bound to a global bound. This method modifies the
     * parameter.
     *
     * @param globalPoint Global bound
     * @return local bound
     */
    public Rectangle2D globalToLocal(Rectangle2D globalPoint);

    public boolean isAncestorOf(WorldObject wo);

    /**
     * @return Whether this node is animating
     */
    public boolean isAnimating(long now);

    /**
     * @return Whether this Object has been destroyed
     */
    public boolean isDestroyed();

    /**
     * @return Whether this object is selectable by a Selection Handler
     */
    public boolean isSelectable();

    /**
     * @param isSelectable Whether this object is selectable by a Selection handler
     */
    public void setSelectable(boolean isSelectable);

    public boolean isSelected();

    public void setSelected(boolean isSelected);

    /**
     * Nodes that apply layout constraints to their children should override
     * this method and do the layout there.
     */
    public void layoutChildren();

    /**
     * Transform the given point from this node's local coordinate system to the
     * global coordinate system. Note that this will modify the point parameter.
     *
     * @param localPoint point in local coordinate system to be transformed.
     * @return point in global coordinates
     */
    public Point2D localToGlobal(Point2D localPoint);

    /**
     * Transform the given rectangle from this node's local coordinate system to
     * the global coordinate system. Note that this will modify the rectangle
     * parameter.
     *
     * @param localRectangle rectangle in local coordinate system to be transformed.
     * @return rectangle in global coordinates
     */
    public Rectangle2D localToGlobal(Rectangle2D localRectangle);

    /**
     * Transform the given point from this node's local coordinate system to its
     * parent's local coordinate system. Note that this will modify the point
     * parameter.
     *
     * @param localPoint point in local coordinate system to be transformed.
     * @return point in parent's local coordinate system
     */
    public Point2D localToParent(Point2D localPoint);

    /**
     * Transform the given rectangle from this node's local coordinate system to
     * its parent's local coordinate system. Note that this will modify the
     * rectangle parameter.
     *
     * @param localRectangle rectangle in local coordinate system to be transformed.
     * @return rectangle in parent's local coordinate system
     */
    public Rectangle2D localToParent(Rectangle2D localRectangle);

    public Dimension2D localToParent(Dimension2D localRectangle);

    /**
     * Change the order of this node in its parent's children list so that it
     * will draw in back of all of its other sibling nodes.
     */
    public void moveToBack();

    /**
     * Change the order of this node in its parent's children list so that it
     * will draw after the given sibling node.
     */
    public void moveToFront();

    /**
     * @param position Position relative to object
     * @return Position relative to World's ground layer
     */
    public Point2D objectToGround(Point2D position);

    /**
     * @param rectangle relative to object
     * @return Relative to World's ground layer
     */
    public Rectangle2D objectToGround(Rectangle2D rectangle);

    /**
     * @param position relative to object
     * @return relative to World's sky layer
     */
    public Point2D objectToSky(Point2D position);

    /**
     * @param rectangle relative to object
     * @return relative to World's sky layer
     */
    public Rectangle2D objectToSky(Rectangle2D rectangle);

    /**
     * Paint this node behind any of its children nodes. Subclasses that define
     * a different appearance should override this method and paint themselves
     * there.
     *
     * @param paintContext the paint context to use for painting the node
     */
    public void paint(PaintContext paintContext);

    /**
     * Transform the given point from this node's parent's local coordinate
     * system to the local coordinate system of this node. Note that this will
     * modify the point parameter.
     *
     * @param parentPoint point in parent's coordinate system to be transformed.
     * @return point in this node's local coordinate system
     */
    public Point2D parentToLocal(Point2D parentPoint);

    /**
     * Transform the given rectangle from this node's parent's local coordinate
     * system to the local coordinate system of this node. Note that this will
     * modify the rectangle parameter.
     *
     * @param parentRectangle rectangle in parent's coordinate system to be transformed.
     * @return rectangle in this node's local coordinate system
     */
    public Rectangle2D parentToLocal(Rectangle2D parentRectangle);

    public void removeChild(WorldObject wo);

    public void removeChildrenListener(ChildListener listener);

    /**
     * Delete this node by removing it from its parent's list of children.
     */
    public void removeFromParent();

    /**
     * This function must be called before the object is removed from the world,
     * or placed in a new one
     */
    public void removeFromWorld();

    public void removePropertyChangeListener(Property event, Listener listener);

    /**
     * Mark the area on the screen represented by this nodes full bounds as
     * needing a repaint.
     */
    public void repaint();

    /**
     * Set the bounds of this node to the given value. These bounds are stored
     * in the local coordinate system of this node. If the width or height is
     * less then or equal to zero then the bound's emtpy bit will be set to
     * true. Subclasses must call the super.setBounds() method.
     *
     * @return true if the bounds changed.
     */
    public boolean setBounds(double x, double y, double width, double height);

    /**
     * Set the bounds of this node to the given value. These bounds are stored
     * in the local coordinate system of this node.
     *
     * @return true if the bounds changed.
     */
    public boolean setBounds(Rectangle2D newBounds);

    /**
     * Set the children pickable flag. If this flag is false then this node will
     * not try to pick its children. Children are pickable by default.
     *
     * @param areChildrenPickable true if this node tries to pick its children
     */
    public void setChildrenPickable(boolean areChildrenPickable);

    public boolean setHeight(double height);

    /**
     * Set the offset that is being applied to this node by its transform. This
     * offset effects this node and all of its descendents and is specified in
     * the nodes parent coordinate system. This directly sets the values of the
     * m02 and m12 positions in the affine transform. Unlike "PNode.translate()"
     * it is not effected by the transforms scale.
     *
     * @param x amount of x offset
     * @param y amount of y offset
     */
    public void setOffset(double x, double y);

    /**
     * Set the paint used to paint this node. This value may be set to null.
     */
    public void setPaint(Paint newPaint);

    /**
     * Set the pickable flag for this node. Only pickable nodes can receive
     * input events. Nodes are pickable by default.
     *
     * @param isPickable true if this node is pickable
     */
    public void setPickable(boolean isPickable);

    public boolean setWidth(double width);

    /**
     * Show a transient message which appears over the object. The message is
     * added to the world's sky layer.
     *
     * @param msg
     */
    public void showPopupMessage(String msg);

    /**
     * Translate this node's transform by the given amount, using the standard
     * affine transform translate method. This translation effects this node and
     * all of its descendents.
     */
    public void translate(double dx, double dy);

    default public void setSize(double w, double h) {
        //TODO may not be optimal; may invoke 2 change events when they could just be one
        setWidth(w);
        setHeight(h);
    }

    /** gets a list of nodes this node intersects */
    default public Collection<WorldObject> intersecting(List<WorldObject> intersectingObjectsBuffer) {
        return getWorldLayer().findIntersectingNodes(
                localToGlobal(getBounds()), intersectingObjectsBuffer);
    }
    /** gets a list of nodes this node intersects */
    default public Collection<WorldObject> intersecting(List<WorldObject> intersectingObjectsBuffer, double dx, double dy) {
        Rectangle2D bb = getBounds(); //returns a clone
        bb.add(dx, dy);
        return getWorldLayer().findIntersectingNodes(
                localToGlobal(bb), intersectingObjectsBuffer);
    }

    public default List<WorldObject> getParentPath() {
        List<WorldObject> x = new ArrayList();

        WorldObject w = getParent();
        while (w!=null)  {
            x.add(w);
            w = w.getParent();
        }

        return x;
    }

    public Rectangle2D getFullBoundsReference();

    public double getCenterX();
    public double getCenterY();

    public PNode getPNode();


    public enum Property {
        BOUNDS_CHANGED, CHILDREN_CHANGED, FULL_BOUNDS, GLOBAL_BOUNDS, MODEL_CHANGED, PARENTS_BOUNDS, PARENTS_CHANGED, REMOVED_FROM_WORLD, VIEW_TRANSFORM, WIDGET
    }

    public interface ChildListener {
        public void childAdded(WorldObject wo);

        public void childRemoved(WorldObject wo);
    }

    public interface Listener {
        public void propertyChanged(Property event);
    }

    /** returns total # of children */
    default public int printTree(PrintStream s) {
        return printTree(s, 0);
    }

    default public int printTree(PrintStream s, int level) {
        for (int i = 0; i < level; i++)
            s.print("  ");

        int total = 1;
        String x = this.toString();
        if (x.trim().isEmpty()) {
            x = "null";
        }
        s.println(x + ":" + getClass());
        for (WorldObject o : getChildren())
            total += o.printTree(s, level+1);

        return total;
    }

}