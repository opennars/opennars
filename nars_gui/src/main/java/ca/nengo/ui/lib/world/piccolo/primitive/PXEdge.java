package ca.nengo.ui.lib.world.piccolo.primitive;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.Destroyable;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * An edge with direction. An piccolo component.
 * 
 * @author Shu Wu
 */
public class PXEdge extends PXPath implements PropertyChangeListener, Destroyable,
		PiccoloNodeInWorld {

	private static final long serialVersionUID = 1L;

	/**
	 * Default radius of the arc shape used by this edge. Only applies when the
	 * shape of this edge is set to an arc.
	 */
	static final double DEFAULT_MIN_ARC_RADIUS = 200;

	/**
	 * Color of the line by default
	 */
	private Color defaultColor = NengoStyle.COLOR_LINE;

	/**
	 * Whether this edge is hidden in its default state
	 */
	private boolean hideByDefault;

	/**
	 * Color of the line when it is highlighted
	 */
	private Color highlightColor = NengoStyle.COLOR_LINE_HIGHLIGHT;

	private final boolean isDirected;

	/**
	 * Radius of the arc shape used by this edge. Only applies when the shape of
	 * this edge is set to an arc.
	 */
	private double minArcRadius = DEFAULT_MIN_ARC_RADIUS;

	/**
	 * Node which defines the end point of this edge
	 */
	private final WorldObjectImpl myEndNode;

	/**
	 * Shape of this edge
	 */
	private EdgeShape myShape = EdgeShape.STRAIGHT;

	/**
	 * Node which defines the start point of this edge
	 */
	private final WorldObjectImpl myStartNode;

	/**
	 * State of this edge
	 */
	private EdgeState myState;

	/**
	 * Pointer which represents the direction of this edge in the UI
	 */
	private PointerTriangle trianglePointer;

	public PXEdge(WorldObjectImpl startNode, WorldObjectImpl endNode) {
		this(startNode, endNode, true);
	}

	/**
	 * Creates a new directed edge
	 * 
	 * @param startNode
	 *            Starting node
	 * @param endNode
	 *            Ending node
	 * @param isDirected
	 *            Whether the direction of this edge matters
	 */
	public PXEdge(WorldObjectImpl startNode, WorldObjectImpl endNode, boolean isDirected) {
		super();
		this.myStartNode = startNode;
		this.myEndNode = endNode;
		this.isDirected = isDirected;

		setPickable(false);

		Util.Assert(startNode != null);
		Util.Assert(endNode != null);

        startNode.getPNode().addPropertyChangeListener(PXNode.PROPERTY_GLOBAL_BOUNDS, this);

        endNode.getPNode().addPropertyChangeListener(PXNode.PROPERTY_GLOBAL_BOUNDS, this);

        startNode.getPNode().addPropertyChangeListener(PXNode.PROPERTY_REMOVED_FROM_WORLD, this);

        endNode.getPNode().addPropertyChangeListener(PXNode.PROPERTY_REMOVED_FROM_WORLD, this);

		setState(EdgeState.DEFAULT);
	}

	/**
	 * Creates an arc from the starting position to the ending position.
	 * 
	 * @param startPos
	 *            Starting position
	 * @param endPos
	 *            Ending position
	 * @param isUpward
	 *            Whether the arc points upward
	 */
	private void createArc(Point2D startPos, Point2D endPos, boolean isUpward) {
		/*
		 * Find center of arc to connect starting and ending nodes This
		 * algorithm was derived on paper, no explanation provided
		 */
		double deltaX = endPos.getX() - startPos.getX();
		double deltaY = endPos.getY() - startPos.getY();

		double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
		float arcRadius = (float) Math.max(minArcRadius, distance / 2d);

		double ang1 = Math.atan2(-deltaY, deltaX);

		double ang1b = Math.acos(distance / (2d * arcRadius));
		double vertical = Math.sin(ang1b) * (2d * arcRadius);

		double ang2 = Math.atan2(vertical, distance);

		double ang3;

		if (isUpward) {
			if (deltaX >= 0) {
				ang3 = ang1 + ang2;
			} else {
				ang3 = ang1 - ang2;
			}
		} else {
			if (deltaX < 0) {
				ang3 = ang1 + ang2;
			} else {
				ang3 = ang1 - ang2;
			}
		}

		double deltaToCenterX = (Math.cos(ang3) * arcRadius);
		double deltaToCenterY = -(Math.sin(ang3) * arcRadius);

		double circleCenterX = startPos.getX() + deltaToCenterX;
		double circleCenterY = startPos.getY() + deltaToCenterY;

		double x = circleCenterX - arcRadius;
		double y = circleCenterY - arcRadius;

		double startXFromCenter = startPos.getX() - circleCenterX;
		double startYFromCenter = startPos.getY() - circleCenterY;

		double endXFromCenter = endPos.getX() - circleCenterX;
		double endYFromCenter = endPos.getY() - circleCenterY;
		double start = Math.toDegrees(Math.atan2(-startYFromCenter, startXFromCenter));

		double end = Math.toDegrees(Math.atan2(-endYFromCenter, endXFromCenter));

		if (isUpward) {
			if (deltaX > 0) {
				double oldEnd = end;
				end = start;
				start = oldEnd;

			}

		} else {
			if (deltaX < 0) {
				double oldEnd = end;
				end = start;
				start = oldEnd;
			}
		}

		double extent = end - start;

		if (extent <= 0) {
			extent = 360 + extent;
		}

		Arc2D arc = new Arc2D.Double(x, y, arcRadius * 2, arcRadius * 2, start, extent, Arc2D.OPEN);

		append(arc, false);
	}

	/**
	 * Called when the edge state changes
	 */
	private void stateChanged() {
		if (isHideByDefault() && myState == EdgeState.DEFAULT)
			setVisible(false);
		else
			setVisible(true);

		switch (myState) {
		case DEFAULT:
			this.setStrokePaint(defaultColor);
			break;
		case HIGHLIGHT:
			this.setStrokePaint(highlightColor);
			break;
		}
		this.repaint();
	}

	protected Point2D toLocal(WorldObject node, double x, double y) {
		return toLocal(node, new Point2D.Double(x, y));
	}

	protected Point2D toLocal(WorldObject node, Point2D point) {
		return this.globalToLocal(node.localToGlobal(point));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.shu.ui.lib.world.IDestroyable#destroy()
	 */
	public void destroy() {
		removeFromParent();
        myStartNode.getPNode().removePropertyChangeListener(PXNode.PROPERTY_GLOBAL_BOUNDS, this);

        myEndNode.getPNode().removePropertyChangeListener(PXNode.PROPERTY_GLOBAL_BOUNDS, this);

        myStartNode.getPNode().removePropertyChangeListener(PXNode.PROPERTY_REMOVED_FROM_WORLD,
                this);

        myEndNode.getPNode().removePropertyChangeListener(PXNode.PROPERTY_REMOVED_FROM_WORLD,
                this);

	}

	public Color getDefaultColor() {
		return defaultColor;
	}

	public WorldObjectImpl getEndNode() {
		return myEndNode;
	}

	public Color getHighlightColor() {
		return highlightColor;
	}

	public WorldObjectImpl getStartNode() {
		return myStartNode;
	}

	/**
	 * @return Edge state
	 */
	public EdgeState getState() {
		return myState;
	}

	public boolean isDirected() {
		return isDirected;
	}

	/**
	 * @return Whether this edge is visible when in it's default state
	 */
	public boolean isHideByDefault() {
		return hideByDefault;
	}

	/*
	 * Listens to global bound changes from the start and end nodes.
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		/**
		 * Remove this object if both start and endNodes are destroyed
		 */
		if (event.getPropertyName().equals(PXNode.PROPERTY_REMOVED_FROM_WORLD)) {
			removeFromWorld();
		} else {
			updateEdgeBounds();
		}
	}

	public void setDefaultColor(Color defaultColor) {
        if (this.defaultColor!=null && this.defaultColor.equals(defaultColor))
            return;
		this.defaultColor = defaultColor;
		stateChanged();
	}

	/**
	 * @param hideByDefault
	 *            If true, this edge is hidden in it's default state.
	 */
	public void setHideByDefault(boolean hideByDefault) {
		if (hideByDefault)
			this.setVisible(false);
		this.hideByDefault = hideByDefault;
	}

	public void setHighlightColor(Color highlightColor) {
		this.highlightColor = highlightColor;
		stateChanged();
	}

	public void setLineShape(EdgeShape lineShape) {
		this.myShape = lineShape;
		updateEdgeBounds();
	}

	public void setMinArcRadius(double minArcRadius) {
		this.minArcRadius = minArcRadius;
	}

	/**
	 * @param visible
	 *            If true, the pointer will be visible. The pointer shows the
	 *            direction of this edge.
	 */
	public void setPointerVisible(boolean visible) {
		if (!visible && trianglePointer!=null) {
			trianglePointer.removeFromParent();
			trianglePointer = null;
		} else if (trianglePointer==null) {
            trianglePointer = new PointerTriangle(this);
            addChild(trianglePointer);
		}
	}

	/**
	 * @param state
	 *            New edge state
	 */
	public final void setState(EdgeState state) {
		if (this.myState != state) {
			this.myState = state;

			stateChanged();
		}

	}

	/**
	 * Updates the edge when the start or end node has changed
	 */
	public void updateEdgeBounds() {
		reset();

		Point2D startBounds = toLocal(myStartNode, myStartNode.getBounds().getCenter2D());
		Point2D endBounds = toLocal(myEndNode, myEndNode.getBounds().getCenter2D());

        final float sx = (float)startBounds.getX();
        final float sy = (float)startBounds.getY();
        final float ex = (float)endBounds.getX();
        final float ey = (float)endBounds.getY();
        final float cx = (sx + ex) / 2f;
        final float cy = (sy + ey) / 2f;

		switch (myShape) {
		case STRAIGHT:
			this.moveTo((float) startBounds.getX(), (float) startBounds.getY());
			this.lineTo((float) endBounds.getX(), (float) endBounds.getY());
			break;
        case CURVE:
            this.moveTo(sx, sy);
            //this.quadTo( sx, cy,  cx, cy, ex, cy,  ex, ey);
            //this.quadTo(cx, cy, (float) endBounds.getX(), (float) endBounds.getY());
            this.curveTo((float)startBounds.getX(), cy,
                    cx, cy,
                    (float) endBounds.getX(), (float) endBounds.getY());
            break;
		case UPWARD_ARC:
			createArc(startBounds, endBounds, true);
			break;
		case DOWNWARD_ARC:
			createArc(startBounds, endBounds, false);
			break;
		}

		if (trianglePointer != null) {
			trianglePointer.setOffset(endBounds.getX(), endBounds.getY());
		}

	}

	/**
	 * Supported edge shapes
	 * 
	 * @author Shu Wu
	 */
	public static enum EdgeShape {
		DOWNWARD_ARC, STRAIGHT, CURVE, UPWARD_ARC
	}

	/**
	 * Edge states
	 * 
	 * @author Shu Wu
	 */
	public static enum EdgeState {
		DEFAULT, HIGHLIGHT
	}

	private WorldObject woParent;

	public WorldObject getWorldObject() {
		return woParent;
	}

	public boolean isAnimating() {
		return false;
	}

	public void setWorldObject(WorldObject worldObjectParent) {
		woParent = worldObjectParent;
	}
}

/**
 * A triangle which points in the direction of a directed edge
 * 
 * @author Shu Wu
 */
class PointerTriangle extends PXEdge {

	private static final long serialVersionUID = 1L;

	static final double POINTER_DISTANCE_FROM_END_NODE = 100;
	static final double TRIANGLE_EDGE_LENGTH = 13;

	/**
	 * Creates a new pointer triangle
	 * 
	 * @param edge
	 *            Directed edge which dictates where and how to point
	 */
	public PointerTriangle(PXEdge edge) {
		super(edge.getStartNode(), edge.getEndNode());
		setPaint(NengoStyle.COLOR_LINEEND);

		setBounds(-TRIANGLE_EDGE_LENGTH / 2, -TRIANGLE_EDGE_LENGTH / 2, TRIANGLE_EDGE_LENGTH,
				TRIANGLE_EDGE_LENGTH);
	}

	@Override
	public void updateEdgeBounds() {

		/*
		 * Find the angle between well and end
		 */
		Point2D startPosition = getStartNode()
				.localToGlobal(getStartNode().getBounds().getOrigin());
		Point2D endPosition = getEndNode().localToGlobal(getEndNode().getBounds().getOrigin());

		double deltaX = endPosition.getX() - startPosition.getX();
		double deltaY = endPosition.getY() - startPosition.getY();
		double lineLength = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

		double pointerDistanceFromEndNode = POINTER_DISTANCE_FROM_END_NODE;
		if (pointerDistanceFromEndNode > (lineLength / 2d)) {
			pointerDistanceFromEndNode = (lineLength) / 2d;
		}

		double angle = Math.atan2(deltaY, deltaX);

		double x = Math.cos(angle + Math.PI) * pointerDistanceFromEndNode;
		double y = Math.sin(angle + Math.PI) * pointerDistanceFromEndNode;
		Point2D point0 = new Point2D.Double(x, y);

		x += Math.cos(angle + Math.PI * (5d / 6d)) * TRIANGLE_EDGE_LENGTH;
		y += Math.sin(angle + Math.PI * (5d / 6d)) * TRIANGLE_EDGE_LENGTH;
		Point2D point1 = new Point2D.Double(x, y);

		x += Math.cos(angle + Math.PI * (3d / 2d)) * TRIANGLE_EDGE_LENGTH;
		y += Math.sin(angle + Math.PI * (3d / 2d)) * TRIANGLE_EDGE_LENGTH;
		Point2D point2 = new Point2D.Double(x, y);

		Point2D[] path = { point0, point1, point2 };

		this.setPathToPolyline(path);
		this.closePath();

	}

}