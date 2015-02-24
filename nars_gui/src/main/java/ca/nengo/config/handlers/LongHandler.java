/**
 *
 */
package ca.nengo.config.handlers;

/**
 * ConfigurationHandler for Long values.
 * @author Bryan Tripp
 */
public class LongHandler extends BaseHandler {

	/**
	 * ConfigurationHandler for Long values.
	 */
	public LongHandler() {
		super(Long.class);
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#getDefaultValue(java.lang.Class)
	 */
	public Object getDefaultValue(Class<?> c) {
		return Long.valueOf(0);
	}

}
