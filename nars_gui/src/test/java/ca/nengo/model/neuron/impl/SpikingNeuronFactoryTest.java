/*
 * Created on 29-May-2006
 */
package ca.nengo.model.neuron.impl;

import ca.nengo.config.ConfigUtil;
import ca.nengo.math.impl.IndicatorPDF;
import junit.framework.TestCase;

import java.awt.*;

/**
 * Unit tests for SpikingNeuron.
 * 
 * @author Bryan Tripp
 */
public class SpikingNeuronFactoryTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testNothing() {
    }

    public static void main(String[] args) {
        SpikingNeuronFactory factory = new SpikingNeuronFactory(
                new LinearSynapticIntegrator.Factory(),
                new LIFSpikeGenerator.Factory(),
                new IndicatorPDF(1),
                new IndicatorPDF(0));

        ConfigUtil.configure((Frame) null, factory);
    }
}
