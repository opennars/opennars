/*
 * Created on 24-May-2006
 */
package ca.nengo.model.impl;

import ca.nengo.model.RealOutput;
import ca.nengo.model.Units;
import junit.framework.TestCase;

/**
 * Unit tests for RealOutputImpl. 
 * 
 * @author Bryan Tripp
 */
public class RealOutputImplTest extends TestCase {

	private RealOutput myRealOutput;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		myRealOutput = new RealOutputImpl(new float[]{1f}, Units.SPIKES_PER_S, 0);
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.RealOutputImpl.getValues()'
	 */
	public void testGetValues() {
		assertEquals(1, myRealOutput.getValues().length);
		float val = myRealOutput.getValues()[0];
		assertTrue(val > .99f && val < 1.01f);
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.RealOutputImpl.getUnits()'
	 */
	public void testGetUnits() {
		assertEquals(Units.SPIKES_PER_S, myRealOutput.getUnits());
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.RealOutputImpl.getDimension()'
	 */
	public void testGetDimension() {
		assertEquals(1, myRealOutput.getDimension());
	}

}
