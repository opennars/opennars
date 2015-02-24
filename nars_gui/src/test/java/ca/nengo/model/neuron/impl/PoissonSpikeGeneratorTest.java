package ca.nengo.model.neuron.impl;

import ca.nengo.math.Function;
import ca.nengo.math.impl.FourierFunction;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.math.impl.SigmoidFunction;
import ca.nengo.math.impl.SineFunction;
import ca.nengo.model.*;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.nef.NEFGroup;
import ca.nengo.model.nef.NEFGroupFactory;
import ca.nengo.model.nef.impl.NEFGroupFactoryImpl;
import ca.nengo.model.neuron.impl.PoissonSpikeGenerator.LinearNeuronFactory;
import ca.nengo.plot.Plotter;
import ca.nengo.util.MU;
import ca.nengo.util.Probe;
import ca.nengo.util.SpikePattern;
import junit.framework.TestCase;

/**
 * Unit tests for SpikeGeneratorOrigin.
 *
 * @author Bryan Tripp
 */
public class PoissonSpikeGeneratorTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testNothing() {
    }

    //functional test
    public static void main(String[] args) {
        Function current = new FourierFunction(1f, 5f, 1f, (long) Math.random());
        Function rate = new SigmoidFunction(0, 5, 0, 40);
        PoissonSpikeGenerator generator = new PoissonSpikeGenerator(rate);

        float T = 1;
        float dt = .0005f;
        int steps = (int) Math.floor(T/dt);
        float[] spikeTimes = new float[steps];
        int spikes = 0;
        for (int i = 0; i < steps; i++) {
            float time = dt * i;
            float c1 = current.map(new float[]{time});
            float c2 = current.map(new float[]{time+dt});
            boolean spike = ((SpikeOutput) generator.run(new float[]{time, time+dt}, new float[]{c1, c2})).getValues()[0];

            if (spike) {
                spikeTimes[spikes] = time+dt;
                spikes++;
            }
        }

        final float[] spikeTimesTrimmed = new float[spikes];
        System.arraycopy(spikeTimes, 0, spikeTimesTrimmed, 0, spikes);

        SpikePattern pattern = new SpikePattern() {
            private static final long serialVersionUID = 1L;
            public int getNumNeurons() {
                return 1;
            }
            public float[] getSpikeTimes(int neuron) {
                return spikeTimesTrimmed;
            }
            @Override
            public SpikePattern clone() throws CloneNotSupportedException {
                return (SpikePattern) super.clone();
            }

        };

        Plotter.plot(rate, -1, .001f, 1, "rate");
        Plotter.plot(current, 0, dt, T, "current");
        Plotter.plot(pattern);

        //      SigmoidNeuronFactory snf = new SigmoidNeuronFactory(new IndicatorPDF(-10, 10), new IndicatorPDF(-1, 1), new IndicatorPDF(100, 200));
        LinearNeuronFactory lnf = new LinearNeuronFactory(new IndicatorPDF(200, 400), new IndicatorPDF(-1, 1), true);
        NEFGroupFactory ef = new NEFGroupFactoryImpl();
        ef.setNodeFactory(lnf);

        try {
            NEFGroup ensemble = ef.make("test", 20, 1);
            ensemble.addDecodedTermination("input", MU.I(1), .01f, false);
            Plotter.plot(ensemble);

            Network network = new NetworkImpl();
            network.addNode(ensemble);
            FunctionInput input = new FunctionInput("input", new Function[]{new SineFunction(3)}, Units.UNK);
            network.addNode(input);
            network.addProjection(input.getOrigin(FunctionInput.ORIGIN_NAME), ensemble.getTermination("input"));

            network.setMode(SimulationMode.RATE);
            Probe rates = network.getSimulator().addProbe("test", "rate", true);
            network.run(0, 2);
            //          Plotter.plot(rates.getData(), .05f, "rates");
            Plotter.plot(rates.getData(), "rates");
        } catch (StructuralException e) {
            e.printStackTrace();
        } catch (SimulationException e) {
            e.printStackTrace();
        }

    }

}
