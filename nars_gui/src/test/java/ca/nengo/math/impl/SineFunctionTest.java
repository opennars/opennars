/*
 * Created on 22-May-2007
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import junit.framework.TestCase;

public class SineFunctionTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.SineFunction.getDimension()'
	 */
	public void testGetDimension() {
		SineFunction f = new SineFunction(0.5f);
		assertEquals(1, f.getDimension());
	}
	
	/*
	 * Test method for 'ca.nengo.math.impl.SineFunction.getOmega()'
	 */
	public void testGetOmega() {
		SineFunction f = new SineFunction(0.5f);
		assertEquals(0.5f, f.getOmega());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.SineFunction.map(float[])'
	 */
	public void testMap() {
		SineFunction f = new SineFunction(0.5f);
		TestUtil.assertClose(0f, f.map(new float[]{0f}), .00001f);
		TestUtil.assertClose(1f, f.map(new float[]{(float)Math.PI}), .00001f);
		TestUtil.assertClose(0.84147f, f.map(new float[]{2f}), .00001f);
	}

	/*
	 * Test method for 'ca.nengo.math.impl.SineFunction.multiMap(float[][])'
	 */
	public void testMultiMap() {
		SineFunction f = new SineFunction(0.5f);

		float[] values = f.multiMap(new float[][]{new float[]{3f}, new float[]{-2f}, new float[]{(float)-Math.PI}});
		TestUtil.assertClose(0.997495f, values[0], .00001f);
		TestUtil.assertClose(-0.84147f, values[1], .00001f);
		TestUtil.assertClose(-1f, values[2], .00001f);
	}
	

}
