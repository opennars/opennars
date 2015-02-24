/*
 * Created on 25-Jul-2006
 */
package ca.nengo.math.impl;

import junit.framework.TestCase;

//import ca.nengo.plot.Plotter;

public class FourierFunctionTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.FourierFunction.FourierFunction(float[], float[], float[])'
	 */
	public void testFourierFunctionFloatArrayFloatArrayFloatArray() {
		FourierFunction f = new FourierFunction(new float[]{1.5f}, new float[]{5f}, new float[]{.2f});
		assertClose(0.0000f, f.map(new float[]{.2f}));
		assertClose(1.5451f, f.map(new float[]{1.5f}));
		assertClose(2.9389f, f.map(new float[]{2.8f}));
	}

	/*
	 * Test method for 'ca.nengo.math.impl.FourierFunction.FourierFunction(float, float, float)'
	 * 
	 * TODO: make this self-verifying 
	 */
	public void testFourierFunctionFloatFloatFloat() {
//		FourierFunction f = new FourierFunction(1, 20, 1, (long) Math.random());
		
		int n = 100;
		float[][] from = new float[n][];
		float[] from2 = new float[n];
		for (int i = 0; i < 100; i++) {
			float x = (float) i / (float) n;
			from[i] = new float[]{x};
			from2[i] = x;
		}
//		float[] result = f.multiMap(from);
		
//		Plotter.plot(f, 0, .001f, 1, "fourier");
	}

	/*
	 * Test method for 'ca.nengo.math.impl.FourierFunction.getDimension()'
	 */
	public void testGetDimension() {
		FourierFunction f = new FourierFunction(new float[]{1f}, new float[]{1f}, new float[]{0f});
		assertEquals(1, f.getDimension());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.FourierFunction.multiMap(float[][])'
	 */
	public void testMultiMap() {
		FourierFunction f = new FourierFunction(new float[]{1f}, new float[]{1f}, new float[]{0f});

		float[] from1 = new float[]{.5f};
		float val1 = f.map(from1);
		float[] from2 = new float[]{.6f};
		float val2 = f.map(from2);
		
		float[] vals = f.multiMap(new float[][]{from1, from2});
		assertClose(vals[0], val1);
		assertClose(vals[1], val2);
	}
	
	private void assertClose(float a, float b) {
		float tolerance = .0001f;
		if (a > b + tolerance || a < b - tolerance) {
			fail("Values " + a + " and " + b + " are not close enough");
		}
	}
	
	public void testClone() throws CloneNotSupportedException {
		FourierFunction f1 = new FourierFunction(new float[]{1.5f}, new float[]{5f}, new float[]{.2f});
		FourierFunction f2 = (FourierFunction) f1.clone();
		f2.setFrequencies(new float[][]{new float[]{2f}});
		assertClose(0.0000f, f1.map(new float[]{.2f}));
		assertClose(1.5451f, f1.map(new float[]{1.5f}));
		assertClose(2.9389f, f1.map(new float[]{2.8f}));
	}
	
//	public static void main(String[] args) {
////		FourierFunctionTest test = new FourierFunctionTest();
////		test.testFourierFunctionFloatFloatFloat();
//		
//		float[][] frequencies = new float[][]{new float[]{1, 1}, new float[]{1, 3.5f}};
//		float[] amplitudes = new float[]{1, 1};
//		float[][] phases = new float[][]{new float[]{0, 0}, new float[]{0, 0}};
//		FourierFunction f = new FourierFunction(frequencies, amplitudes, phases);
//		
//		Plotter.plot(f, 0, .01f, 1, "foo");
//	}

}
