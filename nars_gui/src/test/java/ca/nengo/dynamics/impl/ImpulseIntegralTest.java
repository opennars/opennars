/*
 * Created on 19-Jun-2006
 */
package ca.nengo.dynamics.impl;

import ca.nengo.model.Units;
import junit.framework.TestCase;

/**
 * Units tests for ImpulseIntegral. 
 * 
 * @author Bryan Tripp
 */
public class ImpulseIntegralTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.dynamics.impl.ImpulseIntegral.integrate(LTISystem)'
	 * 
	 * 1st order decay
	 */
	public void testIntegrate1stOrder() {
		
		float[][] D = new float[][]{new float[]{0f}};		
		LTISystem system = new LTISystem(
				new float[][]{new float[]{-100f}}, 
				new float[][]{new float[]{1f}}, 
				new float[][]{new float[]{1f}}, 
				D, 
				new float[]{0f}, 
				new Units[]{Units.UNK}
		);
		
		D = new float[][]{new float[]{1f}};		
		system = new LTISystem(
				new float[][]{new float[]{-100f}}, 
				new float[][]{new float[]{1f}}, 
				new float[][]{new float[]{1f}}, 
				D, 
				new float[]{0f}, 
				new Units[]{Units.UNK}
		);
				
		float[][] integral = ImpulseIntegral.integrate(system);
		assertClose(1.01f, integral[0][0], .000001f);
	}
	
	/*
	 * Test method for 'ca.nengo.dynamics.impl.ImpulseIntegral.integrate(LTISystem)'
	 * 
	 * 2nd order system with distinct poles
	 */
	public void testIntegrate2ndOrder() {
		float eig1 = -100f;
		float eig2 = -500f;
		float a0 = -(eig1 + eig2);
		float a1 = eig1 * eig2;
		LTISystem system = new LTISystem(
				new float[][]{new float[]{0f, 1f}, new float[]{-a1, -a0}}, 
				new float[][]{new float[]{0f}, new float[]{1f}}, 
				new float[][]{new float[]{1f, 0f}}, 
				new float[][]{new float[]{0f}}, 
				new float[]{0f, 0f}, 
				new Units[]{Units.UNK}
		);
		
		float[][] integral = ImpulseIntegral.integrate(system);
		assertEquals(1, integral.length);
		assertEquals(1, integral[0].length);
		assertClose(.00002f, integral[0][0], .000000001f);
	}
	
	/*
	 * Test method for 'ca.nengo.dynamics.impl.ImpulseIntegral.integrate(LTISystem)'
	 * 
	 * Repeated pole
	 */ 	
	public void testIntegrate2ndOrderRepeated() {
		float eig1 = -100f;
		float eig2 = -100f;
		float a0 = -(eig1 + eig2);
		float a1 = eig1 * eig2;
		LTISystem system = new LTISystem(
				new float[][]{new float[]{0f, 1f}, new float[]{-a1, -a0}}, 
				new float[][]{new float[]{0f}, new float[]{1f}}, 
				new float[][]{new float[]{1f, 0f}}, 
				new float[][]{new float[]{0f}}, 
				new float[]{0f, 0f}, 
				new Units[]{Units.UNK}
		);
		
		float[][] integral = ImpulseIntegral.integrate(system);
		assertClose(.0001f, integral[0][0], .00000001f);		
	}

	/*
	 * Test method for 'ca.nengo.dynamics.impl.ImpulseIntegral.integrate(LTISystem)'
	 * 
	 * Complex eigenvalues (oscillatory)
	 */ 
	public void testIntegrate2ndOrderComplex() {
		float a0 = 200;
		float a1 = 30000;
		LTISystem system = new LTISystem(
				new float[][]{new float[]{0f, 1f}, new float[]{-a1, -a0}}, 
				new float[][]{new float[]{0f}, new float[]{1f}}, 
				new float[][]{new float[]{1f, 0f}}, 
				new float[][]{new float[]{0f}}, 
				new float[]{0f, 0f}, 
				new Units[]{Units.UNK}
		);
		
		float[][] integral = ImpulseIntegral.integrate(system);
		assertClose(.000033333f, integral[0][0], .00000001f);				
	}
	
	/*
	 * Test method for 'ca.nengo.dynamics.impl.ImpulseIntegral.integrate(LTISystem)'
	 * 
	 * Multi-input-multi-output
	 */ 
	public void testMIMO() {
		float eig1 = -100f;
		float eig2 = -500f;
		float a0 = -(eig1 + eig2);
		float a1 = eig1 * eig2;
		LTISystem system = new LTISystem(
				new float[][]{new float[]{0f, 1f}, new float[]{-a1, -a0}}, 
				new float[][]{new float[]{0f, 0f, 0f}, new float[]{1f, 1f, 1f}}, 
				new float[][]{new float[]{1f, 0f}, new float[]{2f, 0f}}, 
				new float[][]{new float[]{0f, 0f, 0f}, new float[]{0f, 0f, 0f}}, 
				new float[]{0f, 0f}, 
				new Units[]{Units.UNK, Units.UNK}
		);
		
		float[][] integral = ImpulseIntegral.integrate(system);
		assertEquals(2, integral.length);
		assertEquals(3, integral[0].length);
		assertClose(.00002f, integral[0][0], .000000001f);		
		assertClose(.00002f, integral[0][1], .000000001f);		
		assertClose(.00002f, integral[0][2], .000000001f);		
		assertClose(.00004f, integral[1][0], .000000002f);		
		assertClose(.00004f, integral[1][1], .000000002f);		
		assertClose(.00004f, integral[1][2], .000000002f);		
	}

	//checks that first two values are close to each other
	private static void assertClose(float a, float b, float tol) {
		assertTrue(a > b-tol);
		assertTrue(a < b+tol);
	}
 

	public static void main(String[] args) {
		ImpulseIntegralTest test = new ImpulseIntegralTest();
		test.testIntegrate2ndOrderComplex();
	}
	
}
