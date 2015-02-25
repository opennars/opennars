package ca.nengo.neural.neuron.impl;

import ca.nengo.math.PDF;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.LinearExponentialTarget;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.neural.neuron.SynapticIntegrator;

/**
 * Creates GruberNeurons
 */
public class GruberNeuronFactory implements NodeFactory {
	private static final long serialVersionUID = 1L;

	/**
	 * Name of distinguished dopamine termination.
	 */
	public static final String DOPAMINE = "dopamine";

	private final PDF myScalePDF;
	private final PDF myBiasPDF;

	/**
	 * @param scale PDF to pick neuron scale from
	 * @param bias PDF to pick neuron bias from
	 */
	public GruberNeuronFactory(PDF scale, PDF bias) {
//		myScalePDF = new IndicatorPDF(2.9f, 3.1f);
//		myBiasPDF = new IndicatorPDF(-2f, 3f);
		myScalePDF = scale;
		myBiasPDF = bias;
	}

	public Node make(String name) throws StructuralException {
		GruberSpikeGenerator generator = new GruberSpikeGenerator();
		LinearSynapticIntegrator integrator = new LinearSynapticIntegrator(.001f, Units.uAcm2);
		LinearExponentialTarget dopamineTermination
			= (LinearExponentialTarget) integrator.addTermination(DOPAMINE, new float[]{1}, .05f, true);

		float scale = myScalePDF.sample()[0];
//		float scale = 3;
		float bias = myBiasPDF.sample()[0];
		return new GruberNeuron(integrator, generator, scale, bias, name, dopamineTermination);
	}

	/**
	 * @see ca.nengo.model.impl.NodeFactory#getTypeDescription()
	 */
	public String getTypeDescription() {
		return "Adapting LIF Neuron";
	}


	/**
	 * Class representing the actual neuron
	 */
	public static class GruberNeuron extends ExpandableSpikingNeuron {

		private static final long serialVersionUID = 1L;

		private final LinearExponentialTarget myDopamineTermination;
		private final GruberSpikeGenerator mySpikeGenerator;

		/**
		 * @param integrator synaptic integrator
		 * @param generator generator object
		 * @param scale Neuron gain
		 * @param bias Neuron bias
		 * @param name Neuron name
		 * @param dopamineTermination Termination through which the dopamine signal is transmitted
		 */
		public GruberNeuron(SynapticIntegrator integrator, GruberSpikeGenerator generator, float scale, float bias,
		        String name, LinearExponentialTarget dopamineTermination) {
			super(integrator, generator, scale, bias, name);

			myDopamineTermination = dopamineTermination;
			mySpikeGenerator = generator;
		}

		@Override
		public void run(float startTime, float endTime) throws SimulationException {
			float dopamine = myDopamineTermination.getOutput();
			mySpikeGenerator.setDopamine(dopamine);

			super.run(startTime, endTime);
		}
	}


}