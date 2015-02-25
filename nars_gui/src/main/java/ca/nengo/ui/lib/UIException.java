package ca.nengo.ui.lib;

/**
 * Exception thrown by User Interface processes.
 * 
 * @author Shu Wu
 */
public class UIException extends Exception {

	private static final long serialVersionUID = 1L;

	public UIException() {
		super();
	}

	public UIException(String arg0) {
		super(arg0);
	}

	public UIException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public UIException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Default way to handle this exception
	 */
	public void defaultHandleBehavior() {
		printStackTrace();
	}

}
