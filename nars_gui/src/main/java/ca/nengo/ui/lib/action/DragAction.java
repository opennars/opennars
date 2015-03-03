package ca.nengo.ui.lib.action;

import ca.nengo.ui.lib.world.Droppable;
import ca.nengo.ui.lib.world.DroppableX;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.object.Window;

import java.awt.geom.Point2D;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Action which allows the dragging of objects by the selection handler to be
 * done and undone. NOTE: Special care is taken of Window objects. These objects
 * maintain their own Window state, and are thus immune to this action handler's
 * undo action.
 * 
 * @author Shu Wu
 */
public class DragAction extends ReversableAction {

	private static final long serialVersionUID = 1L;
	private final Collection<WeakReference<WorldObject>> selectedObjectsRef;

	private final HashMap<WeakReference<WorldObject>, ObjectState> objectStates;

	/**
	 * @param selectedObjects
	 *            Nodes before they are dragged. Their offset positions will be
	 *            used as initial positions.
	 */
	public DragAction(Collection<WorldObject> selectedObjects) {
		super("Drag operation");

		selectedObjectsRef = new ArrayList<WeakReference<WorldObject>>(selectedObjects.size());

		objectStates = new HashMap<WeakReference<WorldObject>, ObjectState>(selectedObjects.size());

		for (WorldObject wo : selectedObjects) {

			WeakReference<WorldObject> woRef = new WeakReference<WorldObject>(wo);
			selectedObjectsRef.add(woRef);

			ObjectState state = new ObjectState(wo.getParent(), wo.getOffset());
			objectStates.put(woRef, state);

		}

	}

	/**
	 * @param obj
	 *            Object whose drag is being undone
	 * @return True, if Object's drag can be undone
	 */
	public static boolean isObjectDragUndoable(WorldObject obj) {
		if (obj instanceof Window) {
			/*
			 * Window drag actions are immune to being undone
			 */
			return false;
		} else
			return true;
	}

	/**
	 * Stores the final positions based on the node offsets... called from
	 * selection handler after dragging has ended
	 */
	public void setFinalPositions() {
		for (WeakReference<WorldObject> object : selectedObjectsRef) {
			WorldObject node = object.get();

			if (node != null) {
				ObjectState state = objectStates.get(object);
				if (state != null) {
					state.setFinalState(node.getParent(), node.getOffset());
				}
			}
		}
	}

	@Override
	protected void action() throws ActionException {

        List<WorldObject> temporaryBuffer = null;

		for (WeakReference<WorldObject> object : selectedObjectsRef) {
			WorldObject node = object.get();

			if (node != null) {
				ObjectState state = objectStates.get(object);
				WorldObject fParent = state.getFinalParentRef().get();
				if (fParent != null) {

					fParent.addChild(node);
					node.setOffset(state.getFinalOffset());

					try {
                        if (temporaryBuffer == null) temporaryBuffer = new ArrayList();
						dropNode(node, temporaryBuffer);
					} catch (UserCancelledException e) {
						undo();
					}
				}
			}
		}
	}
    public static void dropNode(WorldObject node) throws UserCancelledException {
        dropNode(node, null);
    }


	public static void dropNode(WorldObject node, List<WorldObject> intersectingObjectsBuffer) throws UserCancelledException {
		if (node instanceof DroppableX || node instanceof Droppable) {

            Collection<WorldObject> allTargets = node.intersecting(intersectingObjectsBuffer);

			Collection<WorldObject> goodTargets = null;

            if ((node instanceof DroppableX) || (node instanceof Droppable)) {
                goodTargets = new ArrayList<WorldObject>(allTargets.size());

                // Do not allow a Node to be dropped on a child of itself
                for (WorldObject target : allTargets) {
                    if (!node.isAncestorOf(target)) {
                        goodTargets.add(target);
                    }
                }
            }

			if (node instanceof DroppableX) {
				DroppableX droppable = (DroppableX) node;
				droppable.droppedOnTargets(goodTargets);
			}
			if (node instanceof Droppable) {
				Droppable droppable = (Droppable) node;
				WorldObject target = null;
				for (WorldObject potentialTarget : goodTargets) {
					if (droppable.acceptTarget(potentialTarget)) {
						target = potentialTarget;
					}
				}
				if (target == null) {
					if (droppable.acceptTarget(node.getWorldLayer())) {
						target = node.getWorldLayer();
					}
				}
				else {
					Point2D position = target.globalToLocal(node.localToGlobal(new Point2D.Double(
							0, 0)));

					node.setOffset(position);
					target.addChild(node);
					droppable.justDropped();
				}

			}
		}
	}

	@Override
	protected void undo() throws ActionException {
		for (WeakReference<WorldObject> woRef : selectedObjectsRef) {
			WorldObject wo = woRef.get();
			if (wo != null) {
				if (isObjectDragUndoable(wo)) {
					ObjectState state = objectStates.get(woRef);

					WorldObject iParent = state.getInitialParentRef().get();

					if (iParent != null) {

						iParent.addChild(wo);
						wo.setOffset(state.getInitialOffset());

						dropNode(wo);
					}
				}
			}
		}
	}

	@Override
	protected boolean isReversable() {
		int numOfDraggableObjects = 0;
		for (WeakReference<WorldObject> woRef : selectedObjectsRef) {
			if (woRef.get() != null && isObjectDragUndoable(woRef.get())) {
				numOfDraggableObjects++;
			}
		}

		if (numOfDraggableObjects >= 1) {
			return true;
		} else {
			return false;
		}
	}
}

/**
 * Stores UI state variables required to do and undo drag operations.
 * 
 * @author Shu Wu
 */
class ObjectState {
	private final WeakReference<WorldObject> iParent;
	private final Point2D iOffset;
	private WeakReference<WorldObject> fParent;
	private Point2D fOffset;

	protected ObjectState(WorldObject initialParent, Point2D initialOffset) {
		super();
		this.iParent = new WeakReference<WorldObject>(initialParent);
		this.iOffset = initialOffset;
	}

	protected void setFinalState(WorldObject finalParent, Point2D finalOffset) {
		this.fParent = new WeakReference<WorldObject>(finalParent);
		this.fOffset = finalOffset;
	}

	protected WeakReference<WorldObject> getInitialParentRef() {
		return iParent;
	}

	protected Point2D getInitialOffset() {
		return iOffset;
	}

	protected WeakReference<WorldObject> getFinalParentRef() {
		return fParent;
	}

	protected Point2D getFinalOffset() {
		return fOffset;
	}

}
