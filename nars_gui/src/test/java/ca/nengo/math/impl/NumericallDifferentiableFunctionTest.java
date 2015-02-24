/*
 * Created on 12-Jun-2007
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import junit.framework.TestCase;

public class NumericallDifferentiableFunctionTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.NumericallyDifferentiableFunction.getDimension()'
	 */
	public void testGetDimension() {
		SigmoidFunction f = new SigmoidFunction();
		NumericallyDifferentiableFunction wrap = new NumericallyDifferentiableFunction(f, 0, 0.1f);
		assertEquals(f.getDimension(), wrap.getDimension());
		assertEquals(1, wrap.getDimension());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.NumericallyDifferentiableFunction.map(float[])'
	 */
	public void testMap() {
		SigmoidFunction f = new SigmoidFunction();
		NumericallyDifferentiableFunction wrap = new NumericallyDifferentiableFunction(f, 0, 0.1f);
		
		assertEquals(wrap.map(new float[]{0}), f.map(new float[]{0}));
		assertEquals(wrap.map(new float[]{3}), f.map(new float[]{3}));
		assertEquals(wrap.map(new float[]{100}), f.map(new float[]{100}));
	}

	/*
	 * Test method for 'ca.nengo.math.impl.NumericallyDifferentiableFunction.multiMap(float[][])'
	 */
	public void testMultiMap() {
		SigmoidFunction f = new SigmoidFunction();
		NumericallyDifferentiableFunction wrap = new NumericallyDifferentiableFunction(f, 0, 0.1f);

		float[] values = f.multiMap(new float[][]{new float[]{3}, new float[]{-2}});
		float[] newVals = wrap.multiMap(new float[][]{new float[]{3}, new float[]{-2}});
		assertEquals(values[0],newVals[0]);
		assertEquals(values[1],newVals[1]);
	}
	
	/*
	 * 	Test method for 'ca.nengo.math.impl.NumericallyDifferentiableFunction.getDerivative()'
	 */
	public void testGetDerivative() {
		SigmoidFunction f = new SigmoidFunction(-1f,0.5f,1f,2f);
		Function g = f.getDerivative();
		NumericallyDifferentiableFunction wrap = new NumericallyDifferentiableFunction(f, 0, 0.01f);
		Function gWrap = wrap.getDerivative();
		
		assertEquals(gWrap.getDimension(), g.getDimension());
		TestUtil.assertClose(gWrap.map(new float[]{0f}), g.map(new float[]{0f}), .0001f);
		TestUtil.assertClose(gWrap.map(new float[]{-1f}), g.map(new float[]{-1f}), .0001f);
		TestUtil.assertClose(gWrap.map(new float[]{3f}), g.map(new float[]{3f}), .0001f);
	}

}
