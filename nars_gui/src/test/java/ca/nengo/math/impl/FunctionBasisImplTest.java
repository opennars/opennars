/*
 * Created on 24-May-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;
import ca.nengo.math.FunctionBasis;
import junit.framework.TestCase;

/**
 * Unit tests for FunctionBasisImpl. 
 * 
 * TODO: test Function methods 
 * 
 * @author Bryan Tripp
 */
public class FunctionBasisImplTest extends TestCase {

	private FunctionBasis myFunctionBasis;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		Function[] functions = new Function[]{new MockFunction(-1), new MockFunction(1)};
		myFunctionBasis = new FunctionBasisImpl(functions);
	}

	/*
	 * Test method for 'ca.bpt.cn.math.impl.FunctionBasisImpl.getDimensions()'
	 */
	public void testGetDimensions() {
		assertEquals(2, myFunctionBasis.getBasisDimension());
	}

	/*
	 * Test method for 'ca.bpt.cn.math.impl.FunctionBasisImpl.getFunction(int)'
	 */
	public void testGetFunction() {
		float[] from = new float[]{0, 0};
		assertTrue(myFunctionBasis.getFunction(0).map(from) < 0);
		assertTrue(myFunctionBasis.getFunction(1).map(from) > 0);
	}
	
	private static class MockFunction implements Function {

		private static final long serialVersionUID = 1L;
		
		float myConstantResult;
		
		public MockFunction(float constantResult) {
			myConstantResult = constantResult;
		}
		
		public int getDimension() {
			return 1;
		}

		public float map(float[] from) {
			return myConstantResult;
		}

		public float[] multiMap(float[][] from) {
			throw new RuntimeException("not implemented");
		}
		
		@Override
		public Function clone() throws CloneNotSupportedException {
			return (Function) super.clone();
		}
		
	}

}
