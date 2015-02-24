/*
 * Created on 22-May-2007
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import junit.framework.TestCase;

public class SigmoidFunctionTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.SigmoidFunction.getDimension()'
	 */
	public void testGetDimension() {
		SigmoidFunction f = new SigmoidFunction();
		assertEquals(1, f.getDimension());
		f = new SigmoidFunction(0f,1f,2f,3f);
		assertEquals(1, f.getDimension());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.SigmoidFunction.map(float[])'
	 */
	public void testMap() {
		SigmoidFunction f = new SigmoidFunction();
		TestUtil.assertClose(0.5f, f.map(new float[]{0}), .00001f);
		TestUtil.assertClose(1f, f.map(new float[]{100}), .00001f);
		TestUtil.assertClose(0.952574f, f.map(new float[]{3}), .00001f);
		f = new SigmoidFunction(0f,1f,2f,3f);
		TestUtil.assertClose(2f, f.map(new float[]{-100}), .00001f);
		TestUtil.assertClose(2.5f, f.map(new float[]{0}), .00001f);
		TestUtil.assertClose(2.960834f, f.map(new float[]{.8f}), .00001f);
	}

	/*
	 * Test method for 'ca.nengo.math.impl.SigmoidFunction.multiMap(float[][])'
	 */
	public void testMultiMap() {
		SigmoidFunction f = new SigmoidFunction();

		float[] values = f.multiMap(new float[][]{new float[]{3}, new float[]{-2}});
		TestUtil.assertClose(0.952574f, values[0], .00001f);
		TestUtil.assertClose(0.1192029f, values[1], .00001f);
	}
	
	/*
	 * 	Test method for 'ca.nengo.math.impl.SigmoidFunction.getDerivative()'
	 */
	public void testGetDerivative() {
		SigmoidFunction f = new SigmoidFunction(-1f,0.5f,1f,2f);
		AbstractFunction g = (AbstractFunction)f.getDerivative();
		
		assertEquals(1, g.getDimension());
		TestUtil.assertClose(0.209987f, g.map(new float[]{0f}), .00001f);
		TestUtil.assertClose(0.5f, g.map(new float[]{-1f}), .00001f);
		TestUtil.assertClose(0.000670475f, g.map(new float[]{3f}), .00001f);
	}

}
