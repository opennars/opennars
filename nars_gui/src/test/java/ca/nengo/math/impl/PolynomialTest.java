/*
 * Created on 22-May-2007
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import junit.framework.TestCase;

public class PolynomialTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.Polynomial.getDimension()'
	 */
	public void testGetDimension() {
		Polynomial f = new Polynomial(new float[]{-1,0,1,2});
		assertEquals(1, f.getDimension());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.Polynomial.map(float[])'
	 */
	public void testMap() {
		Polynomial f = new Polynomial(new float[]{-1,0,2,1});
		TestUtil.assertClose(2, f.map(new float[]{1}), .00001f);
		TestUtil.assertClose(15, f.map(new float[]{2}), .00001f);
		TestUtil.assertClose(-1, f.map(new float[]{-2}), .00001f);
		TestUtil.assertClose(-1, f.map(new float[]{0}), .00001f);
	}

	/*
	 * Test method for 'ca.nengo.math.impl.Polynomial.multiMap(float[][])'
	 */
	public void testMultiMap() {
		Polynomial f = new Polynomial(new float[]{-1,0,2,-1});

		float[] values = f.multiMap(new float[][]{new float[]{3}, new float[]{-2}});
		TestUtil.assertClose(-10, values[0], .00001f);
		TestUtil.assertClose(15, values[1], .00001f);
	}

}
