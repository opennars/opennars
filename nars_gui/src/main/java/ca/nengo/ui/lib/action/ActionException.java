package ca.nengo.ui.lib.action;

import ca.nengo.ui.lib.UIException;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.util.Util;

/**
 * Exception thrown during an action
 * 
 * @author Shu Wu
 */
public class ActionException extends UIException {
	private static final long serialVersionUID = 1L;

	/**
	 * Whether an warning should be shown when the action is handled by defaults
	 */
	private final boolean showWarning;

	private final Exception targetException;

	public ActionException(Exception e) {
		this(e.getMessage(), true, e);
	}

	public ActionException(String description) {
		this(description, true, null);
	}

	public ActionException(String description, Exception e) {
		this(description, true, e);
	}

	public ActionException(String description, boolean showWarningPopup) {
		this(description, showWarningPopup, null);
	}

	/**
	 * @param description
	 *            Description of the exception
	 * @param showWarningPopup
	 *            If true, a warning should be shown to the user
	 * @param targetException
	 *            Target exception
	 */
	public ActionException(String description, boolean showWarningPopup, Exception targetException) {
		super(description);

		this.targetException = targetException;

		this.showWarning = showWarningPopup;

	}

	@Override
	public void defaultHandleBehavior() {

		if (showWarning) {
			Util.debugMsg("Action Exception: " + toString());

			if (getMessage() != null) {
				UserMessages.showWarning(getMessage());
			} else {
				Util.showException(this);
			}

		} else {
			Util.debugMsg("Action Exception: " + toString());
		}
	}

	/**
	 * @return Target Exception. Null, if it dosen't exist
	 */
	public Exception getTargetException() {
		return targetException;
	}

	@Override
	public String getMessage() {
		String message = super.getMessage();
		if (message == null) {
			message = "";
		}
		if (targetException != null) {
			message += targetException.getMessage();
		}
		return message;
	}
}
