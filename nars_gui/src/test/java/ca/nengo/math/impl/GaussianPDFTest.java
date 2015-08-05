/*
 * Created on 25-Jul-2006
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import ca.nengo.plot.Plotter;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Unit test for GaussianPDF. 
 * 
 * TODO: more thorough 
 * 
 * @author Bryan Tripp
 */
public class GaussianPDFTest extends TestCase {

	private final static Logger ourLogger = LogManager.getLogger(GaussianPDFTest.class);
	/*
	 * Test method for 'ca.nengo.math.impl.GaussianPDF.sample()'
	 */
	public void testSample() {
		int n = 1000;
		
		GaussianPDF pdf = new GaussianPDF(0f, 1f);
		assertEquals(1, pdf.sample().length);
		
		int c = 0;
		for (int i = 0; i < n; i++) {
			float sample = pdf.sample()[0];
			if (sample > -1f && sample < 1f) c++;
		}
		ourLogger.info("GaussianPDFTest c: " + c);
		assertTrue(c > 620 && c < 740); //should be about 682 but will vary randomly

		pdf = new GaussianPDF(-10f, 4f);
		assertEquals(1, pdf.sample().length);
		
		c = 0;
		for (int i = 0; i < n; i++) {
			float sample = pdf.sample()[0];
			if (sample > -12f && sample < -8f) c++;
		}
		ourLogger.info("GaussianPDFTest c: " + c);
		assertTrue(c > 620 && c < 740); 
	}

	/*
	 * Test method for 'ca.nengo.math.impl.GaussianPDF.getDimension()'
	 */
	public void testGetDimension() {
		GaussianPDF pdf = new GaussianPDF(0f, 1f);
		assertEquals(1, pdf.getDimension());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.GaussianPDF.map(float[])'
	 */
	public void testMap() {
		float tolerance = .0001f;

		GaussianPDF pdf = new GaussianPDF(0f, 1f);		
		TestUtil.assertClose(0.1109f, pdf.map(new float[]{-1.6f}), tolerance); 
		TestUtil.assertClose(0.3989f, pdf.map(new float[]{0f}), tolerance); 
		TestUtil.assertClose(0.3910f, pdf.map(new float[]{.2f}), tolerance); 

		pdf = new GaussianPDF(1f, 2f);		
		TestUtil.assertClose(0.0521f, pdf.map(new float[]{-1.6f}), tolerance); 
		TestUtil.assertClose(0.2197f, pdf.map(new float[]{0f}), tolerance); 
		TestUtil.assertClose(0.2404f, pdf.map(new float[]{.2f}), tolerance); 
	}
	
	/**
	 * Test of optional peak constructor argument
	 */
	public void testScale() {
		float tolerance = .002f;

		GaussianPDF pdf = new GaussianPDF(1f, 2f, 10f); 
		float scale = 35.4491f; //based on unscaled peak of .2821
		TestUtil.assertClose(scale*0.0521f, pdf.map(new float[]{-1.6f}), tolerance); 
		TestUtil.assertClose(scale*0.2197f, pdf.map(new float[]{0f}), tolerance); 
		TestUtil.assertClose(scale*0.2404f, pdf.map(new float[]{.2f}), tolerance); 
		
//		Plotter.plot(pdf, -3, .001f, 3, "foo");
//		try { Thread.sleep(1000*10); } catch (InterruptedException e) {}
	}

	/*
	 * Test method for 'ca.nengo.math.impl.GaussianPDF.multiMap(float[][])'
	 */
	public void testMultiMap() {
		GaussianPDF pdf = new GaussianPDF(0f, 1f);
		float[] from1 = new float[]{-.5f};		
		float val1 = pdf.map(from1);
		float[] from2 = new float[]{-.3f};		
		float val2 = pdf.map(from2);
		
		float[] vals = pdf.multiMap(new float[][]{from1, from2});
		TestUtil.assertClose(val1, vals[0], .0001f);
		TestUtil.assertClose(val2, vals[1], .0001f);
	}
	
	public static void main(String[] args) {
		GaussianPDF pdf = new GaussianPDF(1f, 2f);
		Plotter.plot(pdf, -2, .01f, 2, "Gaussian");
	}

}
