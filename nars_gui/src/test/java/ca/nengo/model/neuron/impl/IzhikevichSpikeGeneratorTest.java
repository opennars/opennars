/*
 * Created on 21-Mar-08
 */
package ca.nengo.model.neuron.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.model.*;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.plot.Plotter;
import ca.nengo.util.MU;
import ca.nengo.util.Probe;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for IzhikevichSpikeGenerator. 
 * 
 * @author Bryan Tripp
 */
public class IzhikevichSpikeGeneratorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSetPreset() {
		float tolerance = 1e-10f;
		
		IzhikevichSpikeGenerator sg = new IzhikevichSpikeGenerator(IzhikevichSpikeGenerator.Preset.DEFAULT);
		TestUtil.assertClose(.02f, sg.getA(), tolerance);
		TestUtil.assertClose(.2f, sg.getB(), tolerance);
		TestUtil.assertClose(-65f, sg.getC(), tolerance);
		TestUtil.assertClose(2f, sg.getD(), tolerance);
		
		sg.setPreset(IzhikevichSpikeGenerator.Preset.CHATTERING);
		TestUtil.assertClose(-50f, sg.getC(), tolerance);

		sg.setPreset(IzhikevichSpikeGenerator.Preset.RESONATOR);
		TestUtil.assertClose(.1f, sg.getA(), tolerance);  
		TestUtil.assertClose(.26f, sg.getB(), tolerance);
		TestUtil.assertClose(-65f, sg.getC(), tolerance);
		
		sg.setA(.05f);
		assertTrue(sg.getPreset() == IzhikevichSpikeGenerator.Preset.CUSTOM);
		TestUtil.assertClose(.05f, sg.getA(), tolerance);  
	}

	public void testRun() {
		//we will compare spike times against results from matlab
		IzhikevichSpikeGenerator rs = new IzhikevichSpikeGenerator(IzhikevichSpikeGenerator.Preset.REGULAR_SPIKING);		
		IzhikevichSpikeGenerator fs = new IzhikevichSpikeGenerator(IzhikevichSpikeGenerator.Preset.FAST_SPIKING);
		
		float I = 5;
		List<Integer> firings1 = new ArrayList<Integer>(10); 
		List<Integer> firings2 = new ArrayList<Integer>(10); 
		for (int i = 0; i < 1000; i++) {
			SpikeOutput o1 = (SpikeOutput) rs.run(new float[]{i/1000f, (i+1)/1000f}, new float[]{I, I});
			if (o1.getValues()[0]) firings1.add(Integer.valueOf(i));
			
			SpikeOutput o2 = (SpikeOutput) fs.run(new float[]{i/1000f, (i+1)/1000f}, new float[]{I, I});
			if (o2.getValues()[0]) firings2.add(Integer.valueOf(i));
		}
		
		assertEquals(10, firings1.size());
		assertEquals(33, firings2.size());
		
		//TODO: the timing doesn't quite match
	}
	
	/**
	 * Plots voltage and recovery variable for a simulation 
	 */
	public static void main(String[] args) throws StructuralException, SimulationException {
		float I = 4;
		
		LinearSynapticIntegrator integrator = new LinearSynapticIntegrator();
		IzhikevichSpikeGenerator generator = new IzhikevichSpikeGenerator(IzhikevichSpikeGenerator.Preset.REGULAR_SPIKING);
		ExpandableSpikingNeuron neuron = new ExpandableSpikingNeuron(integrator, generator, 1, 0, "neuron");		
		Target t = neuron.addTermination("input", MU.I(1), .001f, false);
		
		FunctionInput input = new FunctionInput("input", new Function[]{new ConstantFunction(1, I)}, Units.UNK);
		
		Network network = new NetworkImpl();
		network.addNode(input);
		network.addNode(neuron);
		network.addProjection(input.getOrigin(FunctionInput.ORIGIN_NAME), t);
		
		Probe v = network.getSimulator().addProbe("neuron", IzhikevichSpikeGenerator.V, true);
		Probe u = network.getSimulator().addProbe("neuron", IzhikevichSpikeGenerator.U, true);
		
		network.run(0, 1);
		
		Plotter.plot(v.getData(), "voltage");
		Plotter.plot(u.getData(), "recovery");
	}

}
