/*
 * Created on 20-Jun-2006
 */
package ca.nengo.dynamics.impl;

import ca.nengo.model.Units;
import junit.framework.TestCase;

public class CanonicalModelTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.dynamics.impl.CanonicalModel.getRealization(float[], float[], float)'
	 */
	public void testGetRealization() {
		LTISystem system = CanonicalModel.getRealization(new float[]{0f, 1f}, new float[]{10f, 100f}, 1f);
		
		float[][] A = system.getA(0f);
		assertClose(0f, A[0][0]);
		assertClose(1f, A[0][1]);
		assertClose(-100f, A[1][0]);
		assertClose(-10f, A[1][1]);

		float[][] B = system.getB(0f);
		assertClose(0f, B[0][0]);
		assertClose(1f, B[1][0]);

		float[][] C = system.getC(0f);
		assertClose(1f, C[0][0]);
		assertClose(0f, C[0][1]);

		float[][] D = system.getD(0f);
		assertClose(1f, D[0][0]);
	}
	
	/*
	 * Test method for 'ca.nengo.dynamics.impl.CanonicalModel.isControllableCanonical(LTISystem)'
	 */
	public void testIsControllableCanonical() {
		LTISystem system = CanonicalModel.getRealization(new float[]{0f, 1f}, new float[]{10f, 100f}, 1f);
		assertTrue(CanonicalModel.isControllableCanonical(system));
		
		system = new SimpleLTISystem(
				new float[]{-1f, -1f}, 
				new float[][]{new float[]{0f}, new float[]{1f}},
				new float[][]{new float[]{1f, 1f}}, 				
				new float[2], 
				new Units[]{Units.UNK}
		);
		assertFalse(CanonicalModel.isControllableCanonical(system));
		
		system = new LTISystem(
				new float[][]{new float[]{0f, 1f}, new float[]{1f, 2f}}, 
				new float[][]{new float[]{1f}, new float[]{1f}},
				new float[][]{new float[]{1f, 1f}}, 				
				new float[][]{new float[]{0f}},
				new float[2], 
				new Units[]{Units.UNK}
		);
		assertFalse(CanonicalModel.isControllableCanonical(system));
		
	}

	/*
	 * Test method for 'ca.nengo.dynamics.impl.CanonicalModel.changeTimeConstant(LTISystem, float)'
	 */
	public void testChangeTimeConstant() {
		//distinct roots
		LTISystem system = CanonicalModel.getRealization(new float[]{0f, 1f}, new float[]{30f, 200f}, 1f);
		system = CanonicalModel.changeTimeConstant(system, 1f/5f);
		float[][] A = system.getA(0f);
		assertClose(-100f, A[1][0]);
		assertClose(-25f, A[1][1]);
		
		//complex conjugate roots
		system = CanonicalModel.getRealization(new float[]{0f, 1f}, new float[]{2f, 2f}, 1f);
		system = CanonicalModel.changeTimeConstant(system, 1f/5f);
		A = system.getA(0f);
		assertClose(-26f, A[1][0]);
		assertClose(-10f, A[1][1]);
	}

	private static void assertClose(float a, float b) {
		assertTrue(a > b-.001);
		assertTrue(a < b+.001);
	}

}
