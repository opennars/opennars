/**
 * 
 */
package ca.nengo.model.nef.impl;

import ca.nengo.model.nef.NEFGroup;
import ca.nengo.util.MU;
import junit.framework.TestCase;

//import ca.nengo.plot.Plotter;

/**
 * Unit tests for DecodedOrigin. 
 * 
 * @author Bryan Tripp
 */
public class DecodedSourceTest extends TestCase {

	private DecodedSource myOrigin;
	
	/**
	 * @param arg0
	 */
	public DecodedSourceTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		NEFGroupFactoryImpl ef = new NEFGroupFactoryImpl();
		NEFGroup ensemble = ef.make("test", 100, 1);
		myOrigin = (DecodedSource) ensemble.getOrigin(NEFGroup.X);
//		Plotter.plot(ensemble, NEFEnsemble.X);
	}

	/**
	 * Test method for {@link DecodedSource#getError()}.
	 */
	public void testGetError() {
		System.out.println(MU.toString(new float[][]{myOrigin.getError()}, 10));
	}
	
//	public static void main(String[] args) {
//		DecodedOriginTest test = new DecodedOriginTest("");
//		try {
//			test.setUp();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
