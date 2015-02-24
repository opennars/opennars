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
public class PDFFunctionTest extends TestCase {

	/**
	 * @param arg0
	 */
	public PDFFunctionTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test method for {@link ca.nengo.math.impl.PDFFunction#map(float[])}.
	 * @throws StructuralException
	 */
	public void testMap() throws StructuralException {
		Function f = new PDFFunction(new IndicatorPDF(0,10));
		
		int[] counts = new int[10];
		for(int i=0; i < 10000; i++)
			counts[(int)f.map(new float[]{0f})]++;
		for(int i=0; i < 10; i++)
			TestUtil.assertClose(1000, counts[i], 100);
	}
}
