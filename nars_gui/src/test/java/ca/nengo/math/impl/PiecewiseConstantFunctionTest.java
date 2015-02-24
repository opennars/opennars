/*
 * Created on 20-May-2007
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import ca.nengo.plot.Plotter;
import junit.framework.TestCase;

public class PiecewiseConstantFunctionTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.PiecewiseConstantFunction.getDimension()'
	 */
	public void testGetDimension() {
		PiecewiseConstantFunction f = new PiecewiseConstantFunction(new float[]{-1,0,1}, new float[]{.1f,.2f,.3f,.4f});
		assertEquals(1, f.getDimension());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.PiecewiseConstantFunction.map(float[])'
	 */
	public void testMap() {
		PiecewiseConstantFunction f = new PiecewiseConstantFunction(new float[]{-1,0,1}, new float[]{.1f,.2f,.3f,.4f});
		TestUtil.assertClose(.1f, f.map(new float[]{-2.f}), .00001f);
		TestUtil.assertClose(.2f, f.map(new float[]{-0.5f}), .00001f);
		TestUtil.assertClose(.3f, f.map(new float[]{0.f}), .00001f);
		TestUtil.assertClose(.4f, f.map(new float[]{2.f}), .00001f);
	}

	/*
	 * Test method for 'ca.nengo.math.impl.PiecewiseConstantFunction.multiMap(float[][])'
	 */
	public void testMultiMap() {
		PiecewiseConstantFunction f = new PiecewiseConstantFunction(new float[]{-1,0,1}, new float[]{.1f,.2f,.3f,.4f});

		float[] values = f.multiMap(new float[][]{new float[]{3}, new float[]{-0.5f}});
		TestUtil.assertClose(.4f, values[0], .00001f);
		TestUtil.assertClose(.2f, values[1], .00001f);
	}

    public static void main(String args[]) {
//      Function f = new PiecewiseConstantFunction(new float[]{0, 1, 3, 7}, new float[]{5, 2});
//      Function f = new PiecewiseConstantFunction(new float[]{0, 1, 3, 7}, new float[]{5, 2, -3, 6, 7});
        Function f = new PiecewiseConstantFunction(new float[0], new float[]{5});
        Plotter.plot(f, -1, .01f, 10, "");
    }
}
