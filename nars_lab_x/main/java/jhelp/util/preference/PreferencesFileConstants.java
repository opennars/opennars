package jhelp.util.preference;

/**
 * Defines the constants
 */
public interface PreferencesFileConstants
{
	/** Main markup to encapsulate all preferences */
	public static final String MARKUP_PREFERENCES="Preferences";
	/** Markup use for one preference */
	public static final String MARKUP_PREFERENCE="Preference";
	/** Parameter name of preference */
	public static final String PARAMETER_NAME="name";
	/** Parameter type of preference.<br>Can be ARRAY (byte[]), BOOLEAN (boolean), FILE (java.io.File), INTEGER (int) or STRING (java.lang.String) */
	public static final String PARAMETER_TYPE="type";
	/** Parameter value of preference */
	public static final String PARAMETER_VALUE="value";
}