/**
 * 
 */
package ca.nengo.math.impl;

import junit.framework.TestCase;

//import ca.nengo.util.MU;

/**
 * Unit tests for LinearFunction. 
 * 
 * @author Bryan Tripp
 */
public class LinearFunctionTest extends TestCase {

	/**
	 * @param arg0
	 */
	public LinearFunctionTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test method for {@link ca.nengo.math.impl.AbstractFunction#clone()}.
	 * @throws CloneNotSupportedException 
	 */
	public void testClone() throws CloneNotSupportedException {
		float[] map = new float[]{1, 1};
		LinearFunction f = new LinearFunction(map, 0, true);
		LinearFunction f1 = (LinearFunction) f.clone();
		f.getMap()[0] = 2;
		f.setBias(1);
		f.setRectified(false);
		
		assertTrue(f1.getMap()[0] < 1.5f);
		assertTrue(f1.getBias() < .5f);
		assertTrue(f1.getRectified());
	}

}
