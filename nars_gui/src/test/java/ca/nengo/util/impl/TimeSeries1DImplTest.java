package ca.nengo.util.impl;

import ca.nengo.model.Units;
import ca.nengo.util.TimeSeries1D;
import junit.framework.TestCase;

/**
 * Unit tests for TimeSeries1DImpl. 
 * 
 * @author Bryan Tripp
 */
public class TimeSeries1DImplTest extends TestCase {

	private TimeSeries1D myTimeSeries; 
	
	protected void setUp() throws Exception {
		super.setUp();
		
		float[] times = new float[]{0f, 1f};
		float[] values = new float[]{1.1f, 2.1f};		
		myTimeSeries = new TimeSeries1DImpl(times, values, Units.UNK);
	}

	/*
	 * Test method for 'ca.bpt.cn.util.impl.TimeSeriesImpl.getTimes()'
	 */
	public void testGetTimes() {
		assertEquals(2, myTimeSeries.getTimes().length);
		assertTrue(myTimeSeries.getTimes()[1] > 0);
	}

	/*
	 * Test method for 'ca.bpt.cn.util.impl.TimeSeriesImpl.getValues()'
	 */
	public void testGetValues() {
		assertEquals(2, myTimeSeries.getValues().length);
		assertTrue(myTimeSeries.getValues1D()[1] > 2);		
	}

	/*
	 * Test method for 'ca.bpt.cn.util.impl.TimeSeriesImpl.getUnits()'
	 */
	public void testGetUnits() {
		assertEquals(Units.UNK, myTimeSeries.getUnits1D());
	}
	
	public void testConstructor() {
		try {
			new TimeSeries1DImpl(new float[]{0f}, new float[]{0f, 1f}, Units.UNK);
			fail("Should have thrown exception because of unequal times and values length");
		} catch (IllegalArgumentException e) {} //exception is expected
	}

}
