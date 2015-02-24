/*
 * Created on 6-Mar-08
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import junit.framework.TestCase;

/**
 * Unit tests for SimpleFunctions. 
 * 
 * @author Bryan Tripp
 */
public class SimpleFunctionsTest extends TestCase {

	private static float ourTolerance = 1e-10f;
	
	public void testSin() {
		TestUtil.assertClose(-0.54402111088936981340474766185138f, 
				new SimpleFunctions.Sin().map(new float[]{10}), ourTolerance);
	}
	
	public void testCos() {
		TestUtil.assertClose(-0.83907152907645245225886394782406f, 
				new SimpleFunctions.Cos().map(new float[]{10}), ourTolerance);
	}
	
	public void testTan() {
		TestUtil.assertClose(0.64836082745908667125912493300981f, 
				new SimpleFunctions.Tan().map(new float[]{10}), ourTolerance);
	}
	
	public void testAsin() {
		TestUtil.assertClose(0.5236f, 
				new SimpleFunctions.Asin().map(new float[]{.5f}), .0001f);
	}
	
	public void testAcos() {
		TestUtil.assertClose(1.0472f, 
				new SimpleFunctions.Acos().map(new float[]{.5f}), .0001f);
	}
	
	public void testAtan() {
		TestUtil.assertClose(0.4636f, 
				new SimpleFunctions.Atan().map(new float[]{.5f}), .0001f);
	}
	
	public void testExp() {
		TestUtil.assertClose(22026f, 
				new SimpleFunctions.Exp().map(new float[]{10}), 1);
	}
	
	public void testLog2() {
		TestUtil.assertClose(3.3219f, 
				new SimpleFunctions.Log2().map(new float[]{10}), .0001f);
	}
	
	public void testLog10() {
		TestUtil.assertClose(1f, 
				new SimpleFunctions.Log10().map(new float[]{10}), ourTolerance);
	}
	
	public void testLn() {
		TestUtil.assertClose(2.3026f, 
				new SimpleFunctions.Ln().map(new float[]{10}), .0001f);
	}
	
	public void testPow() {
		TestUtil.assertClose(3162.2776601683793319988935444327f, 
				new SimpleFunctions.Pow().map(new float[]{10, 3.5f}), ourTolerance);
	}
	
	public void testMax() {
		TestUtil.assertClose(10f, 
				new SimpleFunctions.Max().map(new float[]{10, 3.5f}), ourTolerance);
	}
	
	public void testMin() {
		TestUtil.assertClose(3.5f, 
				new SimpleFunctions.Min().map(new float[]{10, 3.5f}), ourTolerance);
	}
	
	public void testSqrt() {
		TestUtil.assertClose(3.1622776601683793319988935444327f, 
				new SimpleFunctions.Sqrt().map(new float[]{10}), ourTolerance);
	}
	
}
