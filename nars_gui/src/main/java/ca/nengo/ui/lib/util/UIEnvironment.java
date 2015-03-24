package ca.nengo.ui.lib.util;

import ca.nengo.ui.lib.AppFrame;
import nars.Global;

/**
 * Holds user interface instance variables.
 * 
 * @author Shu Wu
 */
public class UIEnvironment {

    //private static AppFrame uiInstance;

	static boolean debugEnabled = Global.DEBUG;

	public static boolean isDebugEnabled() {
		return debugEnabled;
	}

	public static void setDebugEnabled(boolean debugEnabled) {
		UIEnvironment.debugEnabled = debugEnabled;
	}

    private static final ThreadLocal<AppFrame> instances = new ThreadLocal<>();

	/**
	 * @return UI Instance
	 */
	public static AppFrame getInstance() {
        AppFrame a = instances.get();
        return a;
	}

	/**
	 * @param instance
	 *            UI Instance
	 */
	public static void setInstance(AppFrame instance) {
        instances.set(instance);
	}

}
