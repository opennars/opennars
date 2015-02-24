/*
 * Created on 24-Jul-2006
 */
package ca.nengo.math.impl;

import junit.framework.TestCase;

public class ConstantFunctionTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.ConstantFunction.getDimension()'
	 */
	public void testGetDimension() {
		ConstantFunction f = new ConstantFunction(1, 1f);
		assertEquals(1, f.getDimension());

		f = new ConstantFunction(10, 1f);
		assertEquals(10, f.getDimension());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.ConstantFunction.map(float[])'
	 */
	public void testMap() {
		ConstantFunction f = new ConstantFunction(1, 1f);
		assertClose(1f, f.map(new float[]{0f}), .00001f);
		assertClose(1f, f.map(new float[]{1f}), .00001f);
	}

	/*
	 * Test method for 'ca.nengo.math.impl.ConstantFunction.multiMap(float[][])'
	 */
	public void testMultiMap() {
		ConstantFunction f = new ConstantFunction(1, 1f);
		float[] result = f.multiMap(new float[][]{new float[]{0f}, new float[]{1f}});
		
		assertEquals(2, result.length);
		assertClose(1f, result[0], .00001f);
		assertClose(1f, result[1], .00001f);
	}
	
	private void assertClose(float a, float b, float tolerance) {
		assertTrue(a > b - tolerance);
		assertTrue(a < b + tolerance);
	}

}
