/*
 * Created on 29-May-2006
 */
package ca.nengo.model.neuron.impl;

import ca.nengo.model.RealOutput;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SimulationMode;
import ca.nengo.model.SpikeOutput;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.TimeSeries1D;
import junit.framework.TestCase;

/**
 * Unit tests for LIFSpikeGenerator. 
 *  
 * @author Bryan Tripp
 */
public class LIFSpikeGeneratorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.LIFSpikeGenerator.getHistory(String)'
	 */
	public void testGetHistory() throws SimulationException {
		LIFSpikeGenerator sg = new LIFSpikeGenerator(.0005f, .02f, .002f);
		TimeSeries history = sg.getHistory("V");
		assertTrue(history instanceof TimeSeries1D);
		assertEquals(0, history.getTimes().length);
		assertEquals(0, history.getValues().length);
		
		sg.run(new float[]{0f, .002f}, new float[]{1f, 1f});
		history = sg.getHistory("V");
		assertEquals(4, history.getTimes().length);
		assertEquals(4, history.getValues().length);
		assertTrue(history.getTimes()[1] > history.getTimes()[0]);
		assertTrue(history.getValues()[1][0] > history.getValues()[0][0]);

		try {
			sg.getHistory("X");
			fail("Should have thrown exception");
		} catch (SimulationException e) {} //exception is expected
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.LIFSpikeGenerator.reset(boolean)'
	 */
	public void testReset() throws SimulationException {
		float initialVoltage = .2f;
		LIFSpikeGenerator sg = new LIFSpikeGenerator(.0005f, .02f, .002f, initialVoltage);
		sg.run(new float[]{0f, .005f}, new float[]{1f, 1f});
		float[] voltage1 = ((TimeSeries1D) sg.getHistory("V")).getValues1D();

		sg.run(new float[]{0f, .005f}, new float[]{1f, 1f});
		float[] voltage2 = ((TimeSeries1D) sg.getHistory("V")).getValues1D();
		
		sg.reset(false);
		
		sg.run(new float[]{0f, .005f}, new float[]{1f, 1f});
		float[] voltage3 = ((TimeSeries1D) sg.getHistory("V")).getValues1D();
		
		assertBetween(voltage1[0], .2f, .23f);
		assertBetween(voltage2[0], .37f, .4f);
		assertBetween(voltage3[0], .2f, .23f);
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.LIFSpikeGenerator.runConstantRate(float, float)'
	 */
	public void testRunConstantRate() {
		float maxTimeStep = .0005f;
		float[] current = new float[]{0f, 2f, 5f};
		float[] tauRC = new float[]{0.01f, .02f};
		float[] tauRef = new float[]{.001f, .002f};

		LIFSpikeGenerator sg = new LIFSpikeGenerator(maxTimeStep, tauRC[0], tauRef[0]);
		sg.setMode(SimulationMode.CONSTANT_RATE);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[0]})).getValues()[0], -.001f, .001f);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[1]})).getValues()[0], 126f, 127f);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[2]})).getValues()[0], 309f, 310f);
				
		sg = new LIFSpikeGenerator(maxTimeStep, tauRC[0], tauRef[1]);
		sg.setMode(SimulationMode.CONSTANT_RATE);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[0]})).getValues()[0], -.001f, .001f);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[1]})).getValues()[0], 111f, 112f);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[2]})).getValues()[0], 236f, 237f);
				
		sg = new LIFSpikeGenerator(maxTimeStep, tauRC[1], tauRef[0]);
		sg.setMode(SimulationMode.CONSTANT_RATE);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[0]})).getValues()[0], -.001f, .001f);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[1]})).getValues()[0], 67f, 68f);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[2]})).getValues()[0], 183f, 184f);

		sg = new LIFSpikeGenerator(maxTimeStep, tauRC[1], tauRef[1]);
		sg.setMode(SimulationMode.CONSTANT_RATE);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[0]})).getValues()[0], -.001f, .001f);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[1]})).getValues()[0], 63f, 64f);
		assertBetween(((RealOutput) sg.run(new float[1], new float[]{current[2]})).getValues()[0], 154f, 155f);
	}
	
	/*
	 * Test method for 'ca.bpt.cn.model.impl.LIFSpikeGenerator.run(float[], float[])'
	 */
	public void testRun() throws SimulationException {
		float maxTimeStep = .0005f;
		float[] current = new float[]{0f, 2f, 5f};
		float[] tauRC = new float[]{0.01f, .02f};
		float[] tauRef = new float[]{.001f, .002f};

		LIFSpikeGenerator sg = new LIFSpikeGenerator(maxTimeStep, tauRC[0], tauRef[0]);
		assertSpikesCloseToRate(sg, current[0], 1);
		assertSpikesCloseToRate(sg, current[1], 4);
		assertSpikesCloseToRate(sg, current[2], 5);
				
		sg = new LIFSpikeGenerator(maxTimeStep, tauRC[0], tauRef[1]);
		assertSpikesCloseToRate(sg, current[0], 1);
		assertSpikesCloseToRate(sg, current[1], 4);
		assertSpikesCloseToRate(sg, current[2], 4);
				
		sg = new LIFSpikeGenerator(maxTimeStep, tauRC[1], tauRef[0]);
		assertSpikesCloseToRate(sg, current[0], 1);
		assertSpikesCloseToRate(sg, current[1], 1);
		assertSpikesCloseToRate(sg, current[2], 10);

		sg = new LIFSpikeGenerator(maxTimeStep, tauRC[1], tauRef[1]);
		assertSpikesCloseToRate(sg, current[0], 1);
		assertSpikesCloseToRate(sg, current[1], 1);
		assertSpikesCloseToRate(sg, current[2], 10);
	}
		
		
public void testRunPrecise() throws SimulationException {
		LIFSpikeGenerator sg = new LIFSpikeGenerator(0.001f, 0.02f, 0.002f);

		assertSpikesCloseToRate(sg, 1.46335061f, 1, SimulationMode.DEFAULT);
		assertSpikesCloseToRate(sg, 1.46335061f, 1, SimulationMode.PRECISE);

		assertSpikesCloseToRate(sg, 4.80514111f, 10, SimulationMode.DEFAULT);		
		assertSpikesCloseToRate(sg, 4.80514111f, 4, SimulationMode.PRECISE);		

		
		float maxTimeStep = .0005f;
		float[] current = new float[]{0f, 2f, 5f};
		float[] tauRC = new float[]{0.01f, .02f};
		float[] tauRef = new float[]{.001f, .002f};

		sg = new LIFSpikeGenerator(maxTimeStep, tauRC[0], tauRef[0]);
		//assertSpikesCloseToRate(sg, current[0], 1, SimulationMode.PRECISE);
		assertSpikesCloseToRate(sg, current[1], 2, SimulationMode.PRECISE);
		assertSpikesCloseToRate(sg, current[2], 5, SimulationMode.PRECISE);
				
		sg = new LIFSpikeGenerator(maxTimeStep, tauRC[0], tauRef[1]);
		assertSpikesCloseToRate(sg, current[0], 1, SimulationMode.PRECISE);
		assertSpikesCloseToRate(sg, current[1], 3, SimulationMode.PRECISE);
		assertSpikesCloseToRate(sg, current[2], 3, SimulationMode.PRECISE);
				
		sg = new LIFSpikeGenerator(maxTimeStep, tauRC[1], tauRef[0]);
		assertSpikesCloseToRate(sg, current[0], 1, SimulationMode.PRECISE);
		assertSpikesCloseToRate(sg, current[1], 1, SimulationMode.PRECISE);
		assertSpikesCloseToRate(sg, current[2], 2, SimulationMode.PRECISE);

		sg = new LIFSpikeGenerator(maxTimeStep, tauRC[1], tauRef[1]);
		assertSpikesCloseToRate(sg, current[0], 1, SimulationMode.PRECISE);
		assertSpikesCloseToRate(sg, current[1], 1, SimulationMode.PRECISE);
		assertSpikesCloseToRate(sg, current[2], 2, SimulationMode.PRECISE);
		
		

}

	private static void assertBetween(float value, float low, float high) {
		assertTrue(value + " is out of range", value > low && value < high);
	}

	private static void assertSpikesCloseToRate(LIFSpikeGenerator sg, float current, float tolerance) throws SimulationException {
		assertSpikesCloseToRate(sg,current,tolerance,SimulationMode.DEFAULT);
	}
	
	
	private static void assertSpikesCloseToRate(LIFSpikeGenerator sg, float current, float tolerance, SimulationMode mode) throws SimulationException {
		float stepSize = .001f;
		int steps = 1000;
		sg.setMode(SimulationMode.CONSTANT_RATE);
		sg.reset(false);
		float rate = ((RealOutput) sg.run(new float[1], new float[]{current})).getValues()[0];
		
		int spikeCount = 0;
		sg.setMode(mode);
		sg.reset(false);
		for (int i = 0; i < steps; i++) {
			boolean spike = ((SpikeOutput) sg.run(new float[]{stepSize * (float) i, stepSize * (float) (i+1)}, 
					new float[]{current, current})).getValues()[0];
			if (spike) {
				spikeCount++;
			}
		}
		
		//System.out.println(spikeCount + " spikes in simulation, " + rate + " expected");
		assertTrue(spikeCount + " spikes in simulation, " + rate + " expected", 
				spikeCount > rate-tolerance && spikeCount < rate+tolerance);
	}

}
