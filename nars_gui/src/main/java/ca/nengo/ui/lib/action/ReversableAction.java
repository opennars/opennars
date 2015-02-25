package ca.nengo.ui.lib.action;

import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.UserMessages;

import javax.swing.*;

/**
 * A reversable action than can be undone.
 * 
 * @author Shu
 */
public abstract class ReversableAction extends StandardAction {

	private static final long serialVersionUID = 1L;

	public ReversableAction(String description) {
		super(description);
	}

	public ReversableAction(String description, String actionName) {
		super(description, actionName);
	}

	public ReversableAction(String description, String actionName,
			boolean isSwingAction) {
		super(description, actionName, isSwingAction);
	}

	/**
	 * Handles exceptions from undo
	 */
	private void undoInternal() {
		try {
			undo();
		} catch (ActionException e) {
			e.defaultHandleBehavior();
		} catch (Exception e) {
			e.printStackTrace();
			UserMessages.showWarning("Unexpected Exception: " + e.toString());
		}
	}

	/**
	 * This function is called if completing the action requires two stages.
	 * First stage does the action but it can still be undone (leaving some
	 * threads intact). This function is the second (optional stage). It
	 * completes the action in such a way that it cannot be undone.
	 */
	protected void finalizeAction() {

	}

	/**
	 * @return True, if this action is reversable
	 */
	protected boolean isReversable() {
		return true;
	}

	@Override
	protected void postAction() {
		/*
		 * Only add the action once to the Action manager
		 */
		if (!isActionCompleted() && isReversable()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					UIEnvironment.getInstance().getActionManager()
							.addReversableAction(ReversableAction.this);
				}
			});
		}
	}

	/**
	 * Does the undo work
	 * 
	 * @return Whether the undo action was successful
	 */
	protected abstract void undo() throws ActionException;

	/**
	 * Undo the action
	 */
	public void undoAction() {
		if (!isActionCompleted()) {
			UserMessages
					.showError("Action was never done, so it can't be undone");
			return;
		}

		if (runSwingType == StandardAction.RunThreadType.JAVA_SWING) {
			if (SwingUtilities.isEventDispatchThread()) {
				undoInternal();
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						undoInternal();
					}
				});
			}

		} else {
			if (SwingUtilities.isEventDispatchThread()) {
				(new Thread() {
					public void run() {
						undoInternal();
					}
				}).start();
			} else {
				undoInternal();
			}
		}
	}

}
