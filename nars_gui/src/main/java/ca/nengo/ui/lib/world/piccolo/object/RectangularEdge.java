package ca.nengo.ui.lib.world.piccolo.object;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.PXEdge;

import java.awt.geom.Point2D;

/**
 * An set 4 edges which attach the respective corners of its starting and ending
 * nodes. Has a pseudo-3d effect.
 * 
 * @author Shu Wu
 */
public class RectangularEdge extends WorldObjectImpl {

	/**
	 * Creates a new rectangular edge
	 * 
	 * @param startNode
	 *            Starting node
	 * @param endNode
	 *            Ending node
	 */
	public RectangularEdge(WorldObjectImpl startNode, WorldObjectImpl endNode) {
		super(new RectangleEdgeNode(startNode, endNode));
	}

}

class RectangleEdgeNode extends PXEdge {

	public RectangleEdgeNode(WorldObjectImpl startNode, WorldObjectImpl endNode) {
		super(startNode, endNode);
		setDefaultColor(NengoStyle.colorTimes(NengoStyle.COLOR_FOREGROUND, 0.2f));
		setPaint(getDefaultColor());
		updateEdgeBounds();
	}

	@Override
	public void updateEdgeBounds() {

		WorldObject start = getStartNode();
		WorldObject end = getEndNode();

		if (start.isDestroyed() || end.isDestroyed()) {
			removeFromWorld();
			return;
		}

		double sX = start.getX();
		double sY = start.getY();
		double sHY = start.getHeight() + sY;
		double sWX = start.getWidth() + sX;

		double eX = end.getX();
		double eY = end.getY();
		double eHY = end.getHeight() + eY;
		double eWX = end.getWidth() + eX;

		Point2D s0 = toLocal(start, sX, sY);
		Point2D s1 = toLocal(start, sWX, sY);
		Point2D s2 = toLocal(start, sX, sHY);
		Point2D s3 = toLocal(start, sWX, sHY);

		Point2D e0 = toLocal(end, eX, eY);
		Point2D e1 = toLocal(end, eWX, eY);
		Point2D e2 = toLocal(end, eX, eHY);
		Point2D e3 = toLocal(end, eWX, eHY);

		Point2D[] path = { s0, e0, s0, s1, e1, s1, s3, e3, s3, s2, e2, s2, s0,
				e0, s0 };

		this.setPathToPolyline(path);
		this.closePath();

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}