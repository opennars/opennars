package ca.nengo.ui.lib.object.line;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.ui.lib.world.DroppableX;
import ca.nengo.ui.lib.world.Interactable;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.WorldObject.Listener;
import ca.nengo.ui.lib.world.WorldObject.Property;
import ca.nengo.ui.lib.world.elastic.ElasticEdge;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.PXEdge;
import org.piccolo2d.util.PPaintContext;

import javax.swing.*;
import java.util.Collection;

/**
 * @author Shu
 */
public abstract class LineConnector extends WorldObjectImpl implements Interactable, DroppableX {

	private final DestroyListener myDestroyListener;

	private final Edge myEdge;

	private final LineSourceIcon myIcon;

	private ILineTermination myTermination;
	private final LineWell myWell;

	public LineConnector(LineWell well) {
		super();
		this.myWell = well;

		setSelectable(true);
		myEdge = new Edge(well, this, 300);
		myEdge.setPointerVisible(true);
		well.getWorldLayer().addEdge(myEdge);

		myIcon = new LineSourceIcon();
		myIcon.setColor(NengoStyle.COLOR_LINEEND);
		addChild(myIcon);

		setBounds(parentToLocal(getFullBoundsClone()));
		setChildrenPickable(false);
		myDestroyListener = new DestroyListener(this);
		myWell.addPropertyChangeListener(Property.REMOVED_FROM_WORLD, myDestroyListener);

	}

	public boolean tryConnectTo(ILineTermination newTermination, boolean modifyModel) {
		if (getTermination() != null) {
			return false;
		}

		if (newTermination != getTermination()) {

			if (newTermination.getConnector() != null) {
				/*
				 * There is already something connected to the termination
				 */
			} else if (initTarget(newTermination, modifyModel)) {

				if (getTermination() != null) {
					disconnectFromTermination();
					destroy();
				} else {

				}
				this.setOffset(0, 0);
				myTermination = newTermination;
				((WorldObject) newTermination).addChild(this);

				return true;
			}
		}
		return false;

	}

	protected PXEdge getEdge() {
		return myEdge;
	}

	protected LineWell getWell() {
		return myWell;
	}

	/**
	 * @param target
	 * @return Whether the connection was successfully initialized
	 */
	protected boolean initTarget(ILineTermination target, boolean modifyModel) {

		return true;
	}

	/**
	 * Called when the LineEnd is first disconnected from a Line end holder
	 */
	protected abstract void disconnectFromTermination();

	@Override
	protected void prepareForDestroy() {
		super.prepareForDestroy();
		myWell.removePropertyChangeListener(Property.REMOVED_FROM_WORLD, myDestroyListener);
	}

	public ILineTermination getTermination() {
		return myTermination;
	}

	public void droppedOnTargets(Collection<WorldObject> targets) {

		for (WorldObject target : targets) {
			if (target == getTermination()) {
				/*
				 * Don't do anything if the target is the same, except move it
				 * back into position
				 */
				this.setOffset(0, 0);
				return;
			}
		}

		/*
		 * If already connected, destroy current connection and delegate the
		 * targets to a new connector
		 */
		if (getTermination() != null) {
			LineConnector newConnector = getWell().createProjection();
			newConnector.setOffset(newConnector.localToParent(newConnector
					.globalToLocal(localToGlobal(parentToLocal(getOffset())))));

			disconnectFromTermination();
			destroy();

			newConnector.droppedOnTargets(targets);

		} else {
			boolean success = false;
			boolean attemptedConnection = false;

			for (WorldObject target : targets) {
				if (target == getWell()) {
					// Connector has been receded back into the origin
					destroy();
					target = null;
				}

				if (target instanceof ILineTermination) {
					attemptedConnection = true;
					if (tryConnectTo((ILineTermination) target, true)) {
						success = true;
						break;
					}
				}
			}

			/*
			 * If not successful and tried to connect, nudge the connector away
			 * to indicate failure
			 */
			if (!success && attemptedConnection) {
				translate(-40, -20);
			}
		}
	}

	/**
	 * @param visible
	 *            Whether the edge associated with this LineEnd has it's
	 *            direction pointer visible
	 */
	public void setPointerVisible(boolean visible) {
		myEdge.setPointerVisible(visible);
	}

	@Override
	public void altClicked() {
		/*
		 * Delegate to the termination, if it exists
		 */
		if (myTermination != null && myTermination instanceof WorldObject) {
			((WorldObject) myTermination).altClicked();
		}
	}

	public JPopupMenu getContextMenu() {

		/*
		 * delegate the context menu from the target if it's attached
		 */
		if ((myTermination != null) && (myTermination instanceof Interactable)) {
			return ((Interactable) myTermination).getContextMenu();
		}

		PopupMenuBuilder menu = new PopupMenuBuilder("Line End");
		menu.addAction(new StandardAction("Remove") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void action() throws ActionException {
				destroy();

			}

		});
		return menu.toJPopupMenu();
	}

}

/**
 * Listens for destroy events from the Well and destroys the connector Note: The
 * connector isn't destroyed automatically by the well's destruct function
 * because it is not a Piccolo child of the well.
 * 
 * @author Shu Wu
 */
class DestroyListener implements Listener {
	private final LineConnector parent;

	public DestroyListener(LineConnector parent) {
		super();
		this.parent = parent;
	}

	public void propertyChanged(Property event) {
		parent.destroy();
	}
}

/**
 * This edge is only visible when the LineEndWell is visible or the LineEnd is
 * connected
 * 
 * @author Shu Wu
 */
class Edge extends ElasticEdge {

	private static final long serialVersionUID = 1L;

	public Edge(LineWell startNode, LineConnector endNode, double length) {
		super(startNode, endNode, length);
	}

	@Override
	protected void paint(PPaintContext paintContext) {
		/*
		 * Only paint this edge, if the LineEndWell is visible, or the LineEnd
		 * is connected
		 */
		if (getStartNode().getVisible() || ((LineConnector) getEndNode()).getTermination() != null) {
			super.paint(paintContext);
		}
	}

}