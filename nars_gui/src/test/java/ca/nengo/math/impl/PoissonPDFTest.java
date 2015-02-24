/**
 * 
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import junit.framework.TestCase;

/**
 * Unit tests for PoissonPDF. 
 * 
 * @author Bryan Tripp
 */
public class PoissonPDFTest extends TestCase {

	/**
	 * @param arg0
	 */
	public PoissonPDFTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testMap() {
		//values taken from http://stattrek.com/Tables/poisson.aspx
		doTestMap(5, 0, 0.00673794699908547f);
		doTestMap(1, 1, 0.367879441171442f);
		doTestMap(1, 5, 0.00306566200976202f);
		doTestMap(5, 1, 0.0336897349954273f);
		doTestMap(5, 5, 0.175467369767851f);
		doTestMap(10, 3, 0.00756665496041415f);
		doTestMap(10, 10, 0.125110035721133f);
		doTestMap(25, 1, 3.47198596624101E-10f);
		doTestMap(25, 10, 0.000364984991504298f);
		doTestMap(25, 30, 0.045412785130119f);
		doTestMap(50, 30, 0.000677198457150213f);
		doTestMap(50, 50, 0.056325006325191f);
		doTestMap(50, 80, 2.22919740702943E-05f);
		doTestMap(19, 55, 9.46612391237032E-12f);
		doTestMap(30, 0, 9.35762296884019E-14f);
	}
	
	private static void doTestMap(float rate, float observation, float probability) {
		float tolerance = .00001f;
		PoissonPDF pdf = new PoissonPDF(rate);
		float result = pdf.map(new float[]{observation});
		TestUtil.assertClose(probability, result, tolerance);
	}
	
	public void testSample() {
		doTestSample(1);
		doTestSample(2);
		doTestSample(3);
		doTestSample(4);
		doTestSample(5);
		doTestSample(10);
		doTestSample(20);
		doTestSample(30);
		doTestSample(50);
		doTestSample(100);
	}
	
	private static void doTestSample(float rate) {
		PoissonPDF pdf = new PoissonPDF(rate);
		int n = 1000;
		int[] bins = new int[100];
		for (int i = 0; i < n; i++) {
			int sample = Math.round(pdf.sample()[0]);
			if (sample < bins.length) bins[sample]++;
		}
		for (int i = 0; i < bins.length; i++) {
			TestUtil.assertClose(pdf.map(new float[]{i}), (float) bins[i]/ (float) n, .05f);
		}
		
	}
	
}
