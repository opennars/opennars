package ca.nengo.ui.lib.action;


public class UserCancelledException extends ActionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserCancelledException() {
		super("User cancelled operation", false);
	}

}
