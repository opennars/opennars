/*
 * Created June 8, 2007
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.CurveFitter;
import ca.nengo.math.Function;
import junit.framework.TestCase;

//import ca.nengo.plot.Plotter;
//import ca.nengo.util.MU;

/*
 * Unit tests for LinearCurveFitter
 */
public class LinearCurveFitterTest extends TestCase {

	/* 
	 * Test method for 'ca.nengo.math.impl.LinearCurveFitter.fit()'
	 */
	public void testFindCoefficients() {
		Function target = new Polynomial(new float[]{1f,4f,-3f,0.5f});
		CurveFitter lcf = new LinearCurveFitter();
		float[][] values = new float[2][10];
		
		for (int i=0; i<values[0].length; i++) {
			values[0][i] = -9 + i * 2;
			values[1][i] = target.map(new float[]{values[0][i]});
		}
		
		Function fitted = lcf.fit(values[0], values[1]);
		
//		Plotter.plot(target, -10, 0.1f, 10, "target");
//		Plotter.plot(fitted, -10, 0.5f, 10, "fitted");
		
		float targetVal = 0f;
		float fittedVal = 0f;
		for (int i=-8; i<9; i=i+2) {
			targetVal = target.map(new float[]{i});
			fittedVal = fitted.map(new float[]{i});
			TestUtil.assertClose(targetVal, fittedVal, 15f);
		}
	
	}
	
	public void testCloneInterpolatedFunction() throws CloneNotSupportedException {
		float[] x = new float[]{0, 1};
		float[] y = new float[]{1, 2};
		
		InterpolatedFunction f = new InterpolatedFunction(x, y);
		InterpolatedFunction f2 = (InterpolatedFunction) f.clone();
		f.setX(new float[]{0, 5});
		assertTrue(f2.getX()[1] < 2);
	}
}
