/**
 *
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import ca.nengo.model.StructuralException;
import junit.framework.TestCase;

/**
 * Unit tests for TimeSeriesFunction.
 *
 * @author Daniel Rasmussen
 */
public class FixedSignalFunctionTest extends TestCase {

	/**
	 * @param arg0
	 */
	public FixedSignalFunctionTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test method for {@link ca.nengo.math.impl.FixedSignalFunction#map(float[])}.
	 * @throws StructuralException
	 */
	public void testMap() throws StructuralException {
		float[][] sig = new float[3][1];
		sig[0][0] = 0;
		sig[1][0] = 1;
		sig[2][0] = 2;
		Function f = new FixedSignalFunction(sig , 0);

		TestUtil.assertClose(0f, f.map(new float[]{0f}), .00001f);
		TestUtil.assertClose(1f, f.map(new float[]{0f}), .00001f);
		TestUtil.assertClose(2f, f.map(new float[]{0f}), .00001f);
		TestUtil.assertClose(0f, f.map(new float[]{0f}), .00001f);
		TestUtil.assertClose(1f, f.map(new float[]{0f}), .00001f);
		TestUtil.assertClose(2f, f.map(new float[]{0f}), .00001f);
	}
}
