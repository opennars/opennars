package ca.nengo.model.neuron.impl;

import junit.framework.TestCase;

public class LIFSpikingNeuronTest extends TestCase {

	public void testPerformance() {
//		float stepSize = 0.001f;
//		int steps = 100000;
		
//		Projection signalProjection = new ProjectionImpl("signal", 0, 1);
//		Input signalInput = new InputImpl(signalProjection, new float[0], new float[]{1f});
//		Activity signalActivity = new ActivityImpl("signal", new boolean[0], new float[]{1f});
		
		int n = 500;
		boolean[] spikes = new boolean[n];
		float[] weights = new float[n];
		for (int i = 0; i < n; i++) {
			spikes[i] = i < n/2.5;
			weights[i] = (float) Math.random() / 20000;
		}
//		Projection spikeProjection = new ProjectionImpl("spikes", n, 0);
//		Input spikeInput = new InputImpl(spikeProjection, weights, new float[0]);
//		Activity spikeActivity = new ActivityImpl("spikes", spikes, new float[0]);
//		
//		LIFSpikingNeuron neuron = new LIFSpikingNeuron(new Input[]{spikeInput}, new float[]{.01f}, 
//				(stepSize*.5f), 0, 5, .02f, .002f);
//		RecorderImpl voltageProbe = new RecorderImpl(neuron, "V");
//		RecorderImpl currentProbe = new RecorderImpl(neuron, "I");
//		
//		long testStartTime = System.currentTimeMillis();
//		int nSpikes = 0;
//		int i = 0;
//		for (; i < steps; i++) {
//			neuron.run((float) i * stepSize, (float) (i+1) * stepSize, new Activity[]{spikeActivity});
//			voltageProbe.collect();
//			//currentProbe.collect();
//
//			if (neuron.getOutput()) {
//				nSpikes += 1;
//			}
//		}
		
//		System.out.println("# Spikes: " + nSpikes + " Steps: " + i + "  Run time:" + (System.currentTimeMillis() - testStartTime));

//		System.out.println(voltageProbe.getData().times.length);
//		for (int i = 0; i < 8; i++) {
//			System.out.println("Time: " + voltageProbe.getData().times[i] 
//			   + "  Potential: " + voltageProbe.getData().values[i] + " " + voltageProbe.getData().units.getName()
//			   + "  Current: " + currentProbe.getData().values[i] + " " + currentProbe.getData().units.getName());
//		}
	}
	
	/*
	 * Test method for 'ca.bpt.cn.model.impl.LIFSpikingNeuron.getHistory(String)'
	 */
	public void testGetHistory() {

	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.LIFSpikingNeuron.listStates()'
	 */
	public void testListStates() {

	}
	
	public static void main(String[] args) {
		LIFSpikingNeuronTest test = new LIFSpikingNeuronTest();
		test.testPerformance();
	}

}
