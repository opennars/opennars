/*
 * Created on 25-Jul-2006
 */
package ca.nengo;

import junit.framework.TestCase;

/**
 * Common utilities for unit tests. 
 *  
 * @author Bryan Tripp
 */
public class TestUtil {

	/**
	 * Assertion that two float values are close to each other, within given tolerance. 
	 * 
	 * @param a A float value 
	 * @param b Another float value 
	 * @param tolerance Maximum expected difference between them
	 */
	public static void assertClose(float a, float b, float tolerance) {
		if (a > b + tolerance || a < b - tolerance) {
			TestCase.fail("Values " + a + " and " + b + " are not close enough");
		}
	}

}
