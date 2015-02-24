/*
 * Created on 13-Jun-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.PDF;
import junit.framework.TestCase;

/**
 * Unit tests for IndicatorPDF. 
 * 
 * @author Bryan Tripp
 */
public class IndicatorPDFTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.IndicatorPDF.sample()'
	 */
	public void testSample() {
		PDF pdf = new IndicatorPDF(-1, 1);
		
		for (int i = 0; i < 10; i++) {
			float[] s = pdf.sample();
			assertEquals(1, s.length);
			assertTrue(s[0] > -1 && s[0] < 1);
		}
		
		pdf = new IndicatorPDF(0, 0);
		assertEquals(0f, pdf.sample()[0]);
	}

	/*
	 * Test method for 'ca.nengo.math.impl.IndicatorPDF.getDimension()'
	 */
	public void testGetDimension() {
		PDF pdf = new IndicatorPDF(-1, 1);
		assertEquals(1, pdf.getDimension());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.IndicatorPDF.map(float[])'
	 */
	public void testMap() {
		PDF pdf = new IndicatorPDF(-1, 1);
		assertClose(0f, pdf.map(new float[]{-1.5f}));
		assertClose(.5f, pdf.map(new float[]{-0.5f}));
		assertClose(.5f, pdf.map(new float[]{0.5f}));
		assertClose(0f, pdf.map(new float[]{1.5f}));
		
		pdf = new IndicatorPDF(5, 5);
		assertEquals(Float.POSITIVE_INFINITY, pdf.map(new float[]{5}));
	}

	/*
	 * Test method for 'ca.nengo.math.impl.IndicatorPDF.multiMap(float[][])'
	 */
	public void testMultiMap() {
		PDF pdf = new IndicatorPDF(-1, 1);
		float[] result = pdf.multiMap(new float[][]{new float[]{0f}, new float[]{2f}});
		
		assertEquals(2, result.length);
		assertClose(.5f, result[0]);
		assertClose(0f, result[1]);
	}
	
	//b is within .01 of a
	private static void assertClose(float a, float b) {
		assertTrue(b > a-.01);
		assertTrue(b < a+.01);
	}

}
