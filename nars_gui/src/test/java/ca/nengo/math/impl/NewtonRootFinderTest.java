/*
 * Created on 31-May-2007
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import junit.framework.TestCase;

//import ca.nengo.math.RootFinder;

public class NewtonRootFinderTest extends TestCase {

	/**
	 * Test method for 'ca.nengo.math.impl.NewtonRootFinder.findRoot()'
	 */
	public void testFindRoot() {
		float root = -1f;
		NewtonRootFinder nrf = new NewtonRootFinder(20, false);
		
		Function func = new ConstantFunction(1, 1f);
		try {
			root = nrf.findRoot(func, -5, 15, 0.0001f);
		} catch (RuntimeException e) { // failure after too many attempts
		}
		
		func = new FourierFunction(new float[]{1, 0.5f, 1}, new float[]{1, 1, 0.5f}, new float[]{0, -0.5f, 0.2f});
		root = nrf.findRoot(func, -5, 5, 0.0001f);
		TestUtil.assertClose(func.map(new float[]{root}), 0, 0.001f);
		
		func = new IdentityFunction(1, 0);
		root = nrf.findRoot(func, -5, 5, 0.0001f);
		TestUtil.assertClose(func.map(new float[]{root}), 0, 0.001f);
		
		func = new PiecewiseConstantFunction(new float[]{-1,1}, new float[]{2,0,-2});
		try {
			root = nrf.findRoot(func, -5, 15, 0.0001f);
		} catch (RuntimeException e) { // failure after too many attempts
		}
		
		func = new Polynomial(new float[]{4f, 2f, -3f, 1f});
		root = nrf.findRoot(func, -5, 15, 0.0001f);
		TestUtil.assertClose(func.map(new float[]{root}), 0, 0.001f);
		
		func = new SigmoidFunction(-1f, 0.3f, -0.5f, 1f);
		root = nrf.findRoot(func, -5, 5, 0.0001f);
		TestUtil.assertClose(func.map(new float[]{root}), 0, 0.001f);
		
		func = new SineFunction(0.5f);
		root = nrf.findRoot(func, -5, 5, 0.0001f);
		TestUtil.assertClose(func.map(new float[]{root}), 0, 0.001f);
	}
}
