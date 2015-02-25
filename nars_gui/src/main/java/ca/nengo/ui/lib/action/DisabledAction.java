package ca.nengo.ui.lib.action;

import ca.nengo.ui.lib.util.UserMessages;

public class DisabledAction extends StandardAction {
	private final String disableMessage;

	public DisabledAction(String description, String disableMessage) {
		super(description);
		this.disableMessage = disableMessage;
		setEnabled(false);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void action() throws ActionException {
		UserMessages.showWarning(disableMessage);
	}

}
