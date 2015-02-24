/*
 * Created on 25-Jul-2006
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import junit.framework.TestCase;

public class IdentityFunctionTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.IdentityFunction.getDimension()'
	 */
	public void testGetDimension() {
		IdentityFunction f = new IdentityFunction(3, 0);
		assertEquals(3, f.getDimension());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.IdentityFunction.map(float[])'
	 */
	public void testMap() {
		IdentityFunction f = new IdentityFunction(3, 0);
		TestUtil.assertClose(.1f, f.map(new float[]{.1f, .2f, .3f}), .00001f);

		f = new IdentityFunction(3, 1);
		TestUtil.assertClose(.2f, f.map(new float[]{.1f, .2f, .3f}), .00001f);
	}

	/*
	 * Test method for 'ca.nengo.math.impl.IdentityFunction.multiMap(float[][])'
	 */
	public void testMultiMap() {
		IdentityFunction f = new IdentityFunction(3, 0);

		float[] values = f.multiMap(new float[][]{new float[]{.1f, .2f, .3f}, new float[]{.2f, .3f, .4f}});
		TestUtil.assertClose(.1f, values[0], .00001f);
		TestUtil.assertClose(.2f, values[1], .00001f);
	}

}
