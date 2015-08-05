/*
 * Created on 24-May-2006
 */
package ca.nengo.model.impl;

import ca.nengo.model.Units;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.neural.impl.SpikeOutputImpl;
import junit.framework.TestCase;

/**
 * Unit tests for SpikeOutputImpl. 
 * 
 * @author Bryan Tripp
 */
public class SpikeOutputImplTest extends TestCase {

	private SpikeOutput mySpikeOutput;
	
	protected void setUp() throws Exception {
		super.setUp();		
		mySpikeOutput = new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 0);
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.SpikeOutputImpl.getValues()'
	 */
	public void testGetValues() {
		assertEquals(1, mySpikeOutput.getValues().length);
		assertEquals(true, mySpikeOutput.getValues()[0]);
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.SpikeOutputImpl.getUnits()'
	 */
	public void testGetUnits() {
		assertEquals(Units.SPIKES, mySpikeOutput.getUnits());
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.SpikeOutputImpl.getDimension()'
	 */
	public void testGetDimension() {
		assertEquals(1, mySpikeOutput.getDimension());
	}

}
