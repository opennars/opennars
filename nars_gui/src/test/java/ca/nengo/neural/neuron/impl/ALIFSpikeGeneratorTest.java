/*
 * Created on 1-Aug-07
 */
package ca.nengo.neural.neuron.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.math.impl.PiecewiseConstantFunction;
import ca.nengo.model.*;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.model.impl.GroupImpl;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.plot.Plotter;
import ca.nengo.util.Probe;
import ca.nengo.util.TimeSeries;
import junit.framework.TestCase;

/**
 * Unit tests for ALIFSpikeGenerator.
 *
 * @author Bryan Tripp
 */
public class ALIFSpikeGeneratorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetOnsetRate() throws SimulationException {
		float I = 10;
		ALIFSpikeGenerator g1 = new ALIFSpikeGenerator(.002f, .02f, .2f, .1f);
		float rate = run(g1, .001f, 1, I);
		TestUtil.assertClose(rate, g1.getOnsetRate(I), .5f);

		ALIFSpikeGenerator g2 = new ALIFSpikeGenerator(.001f, .01f, .1f, .2f);
		rate = run(g2, .001f, 1, I);
		TestUtil.assertClose(rate, g2.getOnsetRate(I), .5f);
	}

	public void testGetAdaptedRate() throws SimulationException {
		float I = 10;
		ALIFSpikeGenerator g1 = new ALIFSpikeGenerator(.002f, .02f, .2f, .1f);
		float rate = run(g1, .001f, 1000, I);
		TestUtil.assertClose(rate, g1.getAdaptedRate(I), .5f);

		ALIFSpikeGenerator g2 = new ALIFSpikeGenerator(.002f, .02f, .1f, .2f);
		rate = run(g2, .001f, 1000, I);
		TestUtil.assertClose(rate, g2.getAdaptedRate(I), .75f);

		//TODO: are these too far off (~ 0.75%)?

		ALIFSpikeGenerator g3 = new ALIFSpikeGenerator(.001f, .01f, .1f, .2f);
		rate = run(g3, .001f, 1000, I);
		TestUtil.assertClose(rate, g3.getAdaptedRate(I), 1.5f);

		I = 15;
		rate = run(g3, .001f, 1000, I);
		TestUtil.assertClose(rate, g3.getAdaptedRate(I), 2f);
	}

	//returns final firing rate
	private static float run(ALIFSpikeGenerator generator, float dt, int steps, float current) throws SimulationException {
		generator.setMode(SimulationMode.RATE);

		for (int i = 0; i < steps; i++) {
			generator.run(new float[]{i*dt, (i+1)*dt}, new float[]{current, current});
		}

		TimeSeries history = generator.getHistory("rate");
		return history.getValues()[0][0];
	}

	public void testRun() throws SimulationException {
		float maxTimeStep = .0005f;
		float[] current = new float[]{0f, 2f, 5f};
		float[] tauRC = new float[]{0.01f, .02f};
		float[] tauRef = new float[]{.001f, .002f};
		float[] tauN = new float[]{0.1f};

		ALIFSpikeGenerator sg = new ALIFSpikeGenerator(maxTimeStep, tauRC[0], tauRef[0], tauN[0]);
		assertSpikesCloseToRate(sg, current[0], 1);
		assertSpikesCloseToRate(sg, current[1], 5);
		assertSpikesCloseToRate(sg, current[2], 44);

		sg = new ALIFSpikeGenerator(maxTimeStep, tauRC[0], tauRef[1], tauN[0]);
		assertSpikesCloseToRate(sg, current[0], 1);
		assertSpikesCloseToRate(sg, current[1], 4);
		assertSpikesCloseToRate(sg, current[2], 44);

		sg = new ALIFSpikeGenerator(maxTimeStep, tauRC[1], tauRef[0], tauN[0]);
		assertSpikesCloseToRate(sg, current[0], 1);
		assertSpikesCloseToRate(sg, current[1], 2);
		assertSpikesCloseToRate(sg, current[2], 10);

		sg = new ALIFSpikeGenerator(maxTimeStep, tauRC[1], tauRef[1], tauN[0]);
		assertSpikesCloseToRate(sg, current[0], 1);
		assertSpikesCloseToRate(sg, current[1], 1);
		assertSpikesCloseToRate(sg, current[2], 10);
	}



	private static void assertSpikesCloseToRate(ALIFSpikeGenerator sg, float current, float tolerance) throws SimulationException {
		float stepSize = .001f;
		int steps = 1000;
		sg.setMode(SimulationMode.RATE);
		sg.reset(false);
		float rate = ((RealSource) sg.run(new float[1], new float[]{current})).getValues()[0];
		rate=rate*steps*stepSize;

		int spikeCount = 0;
		sg.setMode(SimulationMode.DEFAULT);
		sg.reset(false);
		for (int i = 0; i < steps; i++) {
			boolean spike = ((SpikeOutput) sg.run(new float[]{stepSize * i, stepSize * (i+1)},
					new float[]{current, current})).getValues()[0];
			if (spike) {
				spikeCount++;
			}
		}

		System.out.println(spikeCount + " spikes in simulation, " + rate + " expected");
		assertTrue(spikeCount + " spikes in simulation, " + rate + " expected",
				spikeCount > rate-tolerance && spikeCount < rate+tolerance);
	}

	public void testAdaptation() throws StructuralException, SimulationException {
		NetworkImpl network = new DefaultNetwork();
		LinearSynapticIntegrator integrator = new LinearSynapticIntegrator(.001f, Units.ACU);
		NTarget t = integrator.addTermination("input", new float[]{1}, .005f, false);
		ALIFSpikeGenerator generator = new ALIFSpikeGenerator(.0005f, .02f, .2f, .05f);
		SpikingNeuron neuron = new SpikingNeuron(integrator, generator, 2, 5, "neuron");
		network.addNode(neuron);

		Function f = new PiecewiseConstantFunction(new float[]{1, 2}, new float[]{0, 1, -1});
//		Function f = new SineFunction((float) Math.PI, 1f / (float) Math.PI);
//		Plotter.plot(f, 0, .01f, 3, "input");
		FunctionInput input = new FunctionInput("input", new Function[]{f}, Units.UNK);
		network.addNode(input);

		network.addProjection(input.getSource(FunctionInput.ORIGIN_NAME), t);

//		Probe rate = network.getSimulator().addProbe("neuron", "rate", true);
//		Probe N = network.getSimulator().addProbe("neuron", "N", true);
//		Probe dNdt = network.getSimulator().addProbe("neuron", "dNdt", true);
//		Probe I = network.getSimulator().addProbe("neuron", "I", true);

		setTau(neuron, .1f);
		network.setMode(SimulationMode.RATE);
		network.run(0, 3);

//		Plotter.plot(rate.getData(), "rate");
//		Plotter.plot(N.getData(), "N");
	}

	private void setTau(SpikingNeuron neuron, float tau) {
//		float g_N = 1;
		float alpha = getSlope(neuron) / neuron.getScale();
		float b = neuron.getBias();
		float c = neuron.getScale();

		float tauN = tau/2 * (b/c + 1);
		float A_N = (1/tau - 1/tauN) / alpha;

		//optimal A_N to maximize adaptation range with reasonable tau (see notes 14 April)
//		float A_N = (1/tau - 1e-2f) / alpha;
		((ALIFSpikeGenerator) neuron.getGenerator()).setIncN(A_N);

//		if (tau >= 1/(g_N * A_N * alpha)) {
//			throw new IllegalArgumentException("The requested time constant is too long (can't be supported by other neuron params)");
//		}

//		float tauN = tau / (1 - g_N*A_N*alpha*tau);
		((ALIFSpikeGenerator) neuron.getGenerator()).setTauN(tauN);
	}

	private static float getSlope(SpikingNeuron neuron) {
		SimulationMode mode = neuron.getMode();
		float slope = 0;

		try {
			neuron.setMode(SimulationMode.CONSTANT_RATE);
			neuron.setRadialInput(-1);
			neuron.run(0, 0);
			RealSource low = (RealSource) neuron.getSource(Neuron.AXON).get();
			neuron.setRadialInput(1);
			neuron.run(0, 0);
			RealSource high = (RealSource) neuron.getSource(Neuron.AXON).get();
			slope = (high.getValues()[0] - low.getValues()[0]) / 2f;
			System.out.println("high: " + high.getValues()[0] + " low: " + low.getValues()[0] + " slope: " + slope);
			neuron.setMode(mode);
		} catch (SimulationException e) {
			throw new RuntimeException(e);
		} catch (StructuralException e) {
			throw new RuntimeException(e);
		}

		return slope;
	}


	public static void main(String[] args) {
		ALIFSpikeGeneratorTest test = new ALIFSpikeGeneratorTest();
		try {
			test.testAdaptation();
		} catch (SimulationException e) {
			e.printStackTrace();
		} catch (StructuralException e) {
			e.printStackTrace();
		}
	}

    //functional test
    public static void main2(String[] args) {

        try {
            Network network = new DefaultNetwork();

            //x, .3: varying x keeps time constant, changes adapted rate
//          ALIFSpikeGenerator generator = new ALIFSpikeGenerator(.002f, .02f, .5f, .01f);  //.2: .01 to .3 (150 to 20ms)
//          SynapticIntegrator integrator = new LinearSynapticIntegrator(.001f, Units.ACU);
//          PlasticExpandableSpikingNeuron neuron = new PlasticExpandableSpikingNeuron(integrator, generator, 15f, 0f, "alif");

            ALIFNeuronFactory factory = new ALIFNeuronFactory(new IndicatorPDF(200, 400), new IndicatorPDF(-2.5f, -1.5f),
                    new IndicatorPDF(.1f, .1001f), .0005f, .02f, .2f);

//          VectorGenerator vg = new RandomHypersphereVG(false, 1, 0);
//          ApproximatorFactory factory = new WeightedCostApproximator.Factory(.1f);
//          NEFEnsemble ensemble = new NEFEnsembleImpl("ensemble", new NEFNode[]{neuron}, new float[][]{new float[]{1}}, factory, vg.genVectors(100, 1));

            Node[] neurons = new Node[50];
            float[][] weights = new float[neurons.length][];
            for (int i = 0; i < neurons.length; i++) {
                neurons[i] = factory.make("neuron"+i);
                weights[i] = new float[]{1};
            }
            GroupImpl ensemble = new GroupImpl("ensemble", neurons);
            ensemble.addTarget("input", weights, .005f, false);
            ensemble.collectSpikes(true);
            network.addNode(ensemble);

            FunctionInput input = new FunctionInput("input", new Function[]{new PiecewiseConstantFunction(new float[]{0.2f}, new float[]{0, 0.5f})}, Units.UNK);
            network.addNode(input);

            network.addProjection(input.getSource(FunctionInput.ORIGIN_NAME), ensemble.getTarget("input"));

//          Probe vProbe = network.getSimulator().addProbe("ensemble", 0, "V", true);
//          Probe nProbe = network.getSimulator().addProbe("ensemble", 0, "N", true);
//          Probe iProbe = network.getSimulator().addProbe("ensemble", 0, "I", true);
            Probe rProbe = network.getSimulator().addProbe("ensemble", "rate", true);

            network.setMode(SimulationMode.RATE);
            network.run(0, 1);

//          Plotter.plot(ensemble.getSpikePattern());
//          Plotter.plot(vProbe.getData(), "V");
//          Plotter.plot(nProbe.getData(), "N");
//          Plotter.plot(iProbe.getData(), "I");
            Plotter.plot(rProbe.getData(), "Rate");

        } catch (StructuralException e) {
            e.printStackTrace();
        } catch (SimulationException e) {
            e.printStackTrace();
        }
    }
}
