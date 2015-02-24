package ca.nengo.ui.lib.util;

import ca.nengo.ui.lib.AppFrame;

/**
 * Holds user interface instance variables.
 * 
 * @author Shu Wu
 */
public class UIEnvironment {
	public static final double SEMANTIC_ZOOM_LEVEL = 0.2;
	public static final double ANIMATION_TARGET_FRAME_RATE = 30;

	private static AppFrame uiInstance;

	static boolean debugEnabled = false;

	public static boolean isDebugEnabled() {
		return debugEnabled;
	}

	public static void setDebugEnabled(boolean debugEnabled) {
		UIEnvironment.debugEnabled = debugEnabled;
	}

	/**
	 * @return UI Instance
	 */
	public static AppFrame getInstance() {
		return uiInstance;
	}

	/**
	 * @param instance
	 *            UI Instance
	 */
	public static void setInstance(AppFrame instance) {

		/*
		 * Only one instance of the UI may be running at once
		 */
		if (uiInstance != null) {
			throw new RuntimeException(
					"Only one instance of the User Inteface may be running.");
		}

		uiInstance = instance;
	}

}
