package ca.nengo.ui.lib.action;

import ca.nengo.ui.lib.AppFrame;

public class ExitAction extends StandardAction {
	private final AppFrame appFrame;

	public ExitAction(AppFrame appFrame, String description) {
		super(description);
		this.appFrame = appFrame;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void action() throws ActionException {
		appFrame.exitAppFrame();
//		appFrame.dispose();  //do not want to always close frame in case user selected to cancel exit operation.
	}

}
