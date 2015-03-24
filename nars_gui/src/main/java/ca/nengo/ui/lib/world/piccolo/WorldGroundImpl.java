package ca.nengo.ui.lib.world.piccolo;

import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.ObjectSet;
import ca.nengo.ui.lib.world.World;
import ca.nengo.ui.lib.world.WorldLayer;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.primitive.PXEdge;
import ca.nengo.ui.lib.world.piccolo.primitive.PXNode;
import org.piccolo2d.PLayer;
import org.piccolo2d.PNode;

import javax.swing.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Set;


/**
 * Layer within a world which is zoomable and pannable. It contains world
 * objects.
 * 
 * @author Shu Wu
 */
public class WorldGroundImpl extends WorldLayerImpl implements WorldLayer {

    /*
         * Convenient storage of all children
         */
    private final Set<WorldObject> children = new ObjectSet<WorldObject>().atomic();

    private final GroundNode myLayerNode;

	/**
	 * Extra effects when adding new objects
	 * 
	 * @param wo
	 *            Object to be added
	 * @param centerCameraPosition
	 *            whether the object's position should be changed to appear at
	 *            the center of the camera
	 */
	protected static void dropObject(World world, WorldObject parent, WorldObject wo,
			boolean centerCameraPosition) {
		parent.addChild(wo);

		Point2D finalPosition;
		if (centerCameraPosition) {
			Rectangle2D fullBounds = wo.getFullBoundsReference();

			finalPosition = world.skyToGround(new Point2D.Double(world.getWidth() / 2, world
					.getHeight() / 2));
			/*
			 * The final position is at the center of the full bounds of the
			 * object to be added.
			 */
			finalPosition = new Point2D.Double(finalPosition.getX()
					- (fullBounds.getX() - wo.getOffset().getX()) - (fullBounds.getWidth() / 2d),
					finalPosition.getY() - (fullBounds.getY() - wo.getOffset().getY())
							- (fullBounds.getHeight() / 2d));
		} else {
			finalPosition = wo.getOffset();

		}
		wo.setScale(1 / world.getSky().getViewScale());

		wo.setOffset(finalPosition.getX(), finalPosition.getY()
				- (100 / world.getSky().getViewScale()));

		wo.animateToPositionScaleRotation(finalPosition.getX(), finalPosition.getY(), 1, 0, 500);
	}



	public WorldGroundImpl() {
		super("Ground", new GroundNode());
        myLayerNode = (GroundNode) getPNode();
		myLayerNode.setPickable(false);
	}

	@Override
	protected void prepareForDestroy() {
		myLayerNode.removeFromParent();
		super.prepareForDestroy();
	}

	public void addEdge(PXEdge edge) {
		myLayerNode.addEdge(edge);
	}

	/**
	 * Adds a child object. Like addChild, but with more pizzaz.
	 * 
	 * @param wo
	 *            Object to add to the layer
	 */
	public void addChildFancy(WorldObject wo) {
		addChildFancy(wo, true);
	}

	public void addChildFancy(WorldObject wo, boolean centerCameraPosition) {
		dropObject(world, this, wo, centerCameraPosition);
	}

	@Override
	public void childAdded(WorldObject wo) {
		super.childAdded(wo);

		children.add(wo);
	}

	@Override
	public void childRemoved(WorldObject wo) {
		super.childRemoved(wo);
		if (!children.remove(wo)) {
			Util.Assert(false);
		}
	}

	public boolean containsEdge(PXEdge edge) {
		return myLayerNode.containsEdge(edge);
	}

	@Override
	public Collection<WorldObject> getChildren() {
		return children;
	}

	public Collection<PXEdge> getEdges() {
		return myLayerNode.getEdges();
	}

	/**
	 * @return The scale of the ground in relation to the sky
	 */
	public double getGroundScale() {
		return world.getSky().getViewScale();
	}

	public static interface ChildFilter {
		public boolean acceptChild(WorldObject obj);
	}

	@Override
	public Dimension2D localToParent(Dimension2D localRectangle) {
        return getPNode().localToParent(localRectangle);
	}
}

class GroundNode extends PXNode {

	private static final long serialVersionUID = 1L;

	private final PNode edgeHolder;

    public GroundNode() {
		super();
		this.edgeHolder = new PNode();
	}

	public void addEdge(PXEdge edge) {
		edgeHolder.addChild(edge);
	}

	public boolean containsEdge(PXEdge edge) {
		return edge.getParent() == edgeHolder;
	}

	public Collection<PXEdge> getEdges() {
//		ArrayList<PXEdge> edges = new ArrayList<PXEdge>(edgeHolder.getChildrenCount());
//
//		Iterator<?> it = edgeHolder.getChildrenIterator();
//		while (it.hasNext()) {
//			edges.add((PXEdge) it.next());
//		}
//        return edges;
        //return new ArrayList(edgeHolder.getChildrenReference());
        return edgeHolder.getChildrenReference();
    }

	@Override
	public void setParent(PNode newParent) {
        if (getParent() == newParent) return;

        if (newParent != null && !(newParent instanceof PLayer)) {
			throw new InvalidParameterException();
		}

		super.setParent(newParent);
		/*
		 * Invoke later, otherwise the edge holder may be added below the
		 * ground. We can't add directly here because this function is called
		 * from also addChild
		 */
		SwingUtilities.invokeLater(reparent);

	}
    final private Runnable reparent = new Runnable() {
        public void run() {
            if (getParent() != null) {
                getParent().addChild(0, edgeHolder);
            }
        }
    };

}
