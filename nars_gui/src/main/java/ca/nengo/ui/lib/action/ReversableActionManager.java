package ca.nengo.ui.lib.action;

import ca.nengo.ui.lib.AppFrame;
import ca.nengo.ui.lib.util.UserMessages;

import java.util.Vector;

/**
 * Manages reversable actions
 * 
 * @author Shu Wu
 */
public class ReversableActionManager {

	/**
	 * Max number of undo steps to reference
	 */
	static final int MAX_NUM_OF_UNDO_ACTIONS = 5;

	private final AppFrame parent;

	/**
	 * A collection of reversable actions
	 */
	private final Vector<ReversableAction> reversableActions;

	/**
	 * Number of undo steps that have been taken
	 */
	private int undoStepCount = 0;

	/**
	 * Create a new reversable action manager
	 * 
	 * @param parent
	 *            Application parent of this manager
	 */
	public ReversableActionManager(AppFrame parent) {
		super();
		reversableActions = new Vector<ReversableAction>(
				MAX_NUM_OF_UNDO_ACTIONS + 1);
		this.parent = parent;
	}

	/**
	 * Updates the application parent that reversable actions have changed
	 */
	private void updateParent() {
		parent.reversableActionsUpdated();
	}

	/**
	 * @param action
	 *            Action to add
	 */
	public void addReversableAction(ReversableAction action) {

		while (undoStepCount > 0) {
			reversableActions.remove(reversableActions.size() - 1);
			undoStepCount--;
		}
		reversableActions.add(action);

		if (reversableActions.size() > MAX_NUM_OF_UNDO_ACTIONS) {
			ReversableAction reversableAction = reversableActions.remove(0);
			reversableAction.finalizeAction();
		}
		updateParent();
	}

	/**
	 * @return True, if an action can be redone
	 */
	public boolean canRedo() {
		if (undoStepCount > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return True, if an action can be undone
	 */
	public boolean canUndo() {
		if ((reversableActions.size() - undoStepCount) > 0)
			return true;
		else
			return false;

	}

	/**
	 * @return Description of the action that can be redone
	 */
	public String getRedoActionDescription() {
		if (canRedo()) {
			return reversableActions.get(
					reversableActions.size() - undoStepCount).getDescription();
		} else {
			return "none";
		}
	}

	/**
	 * @return Description of the action that can be undone
	 */
	public String getUndoActionDescription() {
		if (canUndo()) {
			return reversableActions.get(
					reversableActions.size() - 1 - undoStepCount)
					.getDescription();
		} else {
			return "none";
		}
	}

	/**
	 * Redo the focused action
	 */
	public void redoAction() {
		ReversableAction action = reversableActions.get(reversableActions
				.size()
				- undoStepCount);

		undoStepCount--;

		action.doAction();
		updateParent();
	}

	/**
	 * Undo the focused action
	 */
	public void undoAction() {
		if (canUndo()) {

			ReversableAction action = reversableActions.get(reversableActions
					.size()
					- 1 - undoStepCount);
			undoStepCount++;
			action.undoAction();
		} else {
			UserMessages.showError("Cannot undo anymore steps");
		}

		updateParent();
	}
}
