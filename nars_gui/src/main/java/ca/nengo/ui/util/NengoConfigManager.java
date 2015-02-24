package ca.nengo.ui.util;

import ca.nengo.ui.lib.util.Util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class NengoConfigManager {

	public static final String NENGO_CONFIG_FILE = "NengoGraphics.config";
	public static final String USER_CONFIG_FILE = "User.config";
	public static final String USER_CONFIG_COMMENTS = "This file persists user preferences for Nengo Graphics";

	private static Properties nengoConfig;
	private static Properties userConfig;

	public enum UserProperties {
		ModelWorkingLocation, PlotterDefaultTauFilter, PlotterDefaultSubSampling, NengoWindowExtendedState
	}

	public static Properties getNengoConfig() {
		if (nengoConfig == null) {
			nengoConfig = loadConfig(NENGO_CONFIG_FILE);
		}

		return nengoConfig;
	}

	private static Properties getUserConfig() {
		if (userConfig == null) {
			userConfig = loadConfig(USER_CONFIG_FILE);
		}

		return userConfig;
	}

	public static String getUserProperty(UserProperties property) {
		return getUserConfig().getProperty(property.toString());
	}

	public static boolean getUserBoolean(UserProperties property, boolean defaultvalue) {
		String value = getUserProperty(property);

		return value != null ? Boolean.parseBoolean(value) : defaultvalue;
	}

	public static int getUserInteger(UserProperties property, int defaultvalue) {
		String value = getUserProperty(property);

		return value != null ? Integer.parseInt(value) : defaultvalue;
	}

	public static void setUserProperty(UserProperties property, String value) {
		getUserConfig().setProperty(property.toString(), value);
	}

	public static void setUserProperty(UserProperties property, boolean value) {
		setUserProperty(property, Boolean.toString(value));
	}

	public static void setUserProperty(UserProperties property, int value) {
		setUserProperty(property, Integer.toString(value));
	}

	public static void saveUserConfig() {
		if (userConfig != null) {
			try {
				FileOutputStream fos = new FileOutputStream(USER_CONFIG_FILE);
				try {
					userConfig.store(fos, USER_CONFIG_COMMENTS);
					fos.close();
				} finally {
					fos.close();
				}
			} catch (IOException e) {
				Util.debugMsg("Problem saving config file: " + e.getMessage());
			}
		}
	}

	private static Properties loadConfig(String name) {
		try {
			FileInputStream fis = new FileInputStream(name);
			try {

				Properties props = new Properties();
				props.load(fis);
				return props;
			} finally {
				fis.close();
			}
		} catch (IOException e) {
			Util.debugMsg("Problem loading config file: " + e.getMessage());
		}

		return new Properties();
	}

}
