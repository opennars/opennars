/*
 * Created on 26-May-2006
 */
package ca.nengo.model.neuron.impl;

import ca.nengo.model.*;
import ca.nengo.model.impl.SpikeOutputImpl;
import ca.nengo.model.neuron.ExpandableSynapticIntegrator;
import ca.nengo.util.TimeSeries1D;
import junit.framework.TestCase;

/**
 * Unit tests for LinearSynapticIntegrator. 
 * 
 * @author Bryan Tripp
 */
public class LinearSynapticIntegratorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.LinearSynapticIntegrator.getTerminations()'
	 */
	public void testGetTerminations() throws StructuralException {
		ExpandableSynapticIntegrator si = new LinearSynapticIntegrator(1, Units.ACU);
		assertEquals(0, si.getTerminations().length);
		
		si.addTermination("test1", new float[]{1f, 1f}, 1f, false);
		assertEquals(1, si.getTerminations().length);
		assertEquals("test1", si.getTerminations()[0].getName());
		assertEquals(2, si.getTerminations()[0].getDimensions());
		assertEquals(1f, si.getTerminations()[0].getTau());
		
		si.addTermination("test2", new float[0], 1f, false);
		assertEquals(2, si.getTerminations().length);
		
		si.removeTermination("test2");
		assertEquals(1, si.getTerminations().length);		
		
		try {
			si.addTermination("test1", new float[0], 1f, false);
			fail("Should have thrown exception due to duplicate termination name");
		} catch (StructuralException e) {} //exception is expected
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.LinearSynapticIntegrator.run(float, float)'
	 */
	public void testRun() throws StructuralException, SimulationException {
		ExpandableSynapticIntegrator si = new LinearSynapticIntegrator(.001f, Units.ACU);
		si.addTermination("one", new float[]{1f}, 1f, false);
		si.addTermination("two", new float[]{1f}, 1f, false);
		si.addTermination("three", new float[]{1f}, 1f, true);

		Target[] t = si.getTerminations();
		
		InstantaneousOutput spike = new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 0);
		
		t[0].setValues(spike);
		t[1].setValues(spike);
		t[2].setValues(spike);
		
		TimeSeries1D current = si.run(0f, .01f); 
		assertEquals(11, current.getTimes().length);
		assertTrue(current.getValues1D()[0] > 1.99f && current.getValues1D()[0] < 2.01f);
		for (int i = 1; i < current.getTimes().length; i++) {
			assertTrue(current.getValues1D()[i] < current.getValues1D()[i-1]); //decaying
		}
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.LinearSynapticIntegrator.reset(boolean)'
	 */
	public void testReset() throws StructuralException, SimulationException {
		ExpandableSynapticIntegrator si = new LinearSynapticIntegrator(.001f, Units.ACU);
		si.addTermination("test", new float[]{1f}, 1f, false);
		
		Target t = si.getTerminations()[0];
		t.setValues(new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 0));		
		for (int i = 0; i < 10; i++) {
			si.run(.001f * ((float) i), .001f * ((float) i+1));			
			t.setValues(new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, 0));
		}
		TimeSeries1D current = si.run(.010f, .011f);
		assertTrue(current.getValues()[1][0] > .9f);
		
		si.reset(false); //there is no random setting to test
		current = si.run(.011f, .012f);
		assertTrue(current.getValues1D()[1] < .01f);
	}

}
