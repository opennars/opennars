/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "HodgkinHuxleySpikeGenerator.java". Description:
"A SpikeGenerator based on the Hodgkin-Huxley model.

  TODO: unit test

  @author Bryan Tripp"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

/*
 * Created on 30-Mar-07
 */
package ca.nengo.neural.neuron.impl;

import ca.nengo.dynamics.impl.AbstractDynamicalSystem;
import ca.nengo.dynamics.impl.RK45Integrator;
import ca.nengo.model.Node;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.neural.neuron.SpikeGenerator;

/**
 * A SpikeGenerator based on the Hodgkin-Huxley model.
 *
 * TODO: unit test
 *
 * @author Bryan Tripp
 */
public class HodgkinHuxleySpikeGenerator extends DynamicalSystemSpikeGenerator {

	private static final long serialVersionUID = 1L;

	/**
	 * Makes the dynamic system
	 */
	public HodgkinHuxleySpikeGenerator() {
		super(new HodgkinHuxleySystem(new float[4]), new RK45Integrator(), 0, 30f, .002f);
	}

	/**
	 * Hodgkin-Huxley spiking dynamics.
	 *
	 * @author Bryan Tripp
	 */
	public static class HodgkinHuxleySystem extends AbstractDynamicalSystem {

		private static final long serialVersionUID = 1L;
	    private static final float G_Na = 120f;
	    private static final float E_Na = 115f; //this potential and others are relative to -60 mV
	    private static final float G_K = 36f;
	    private static final float E_K = -12f;
	    private static final float G_m = 0.3f;
	    private static final float V_rest = 10.613f;
	    private static final float C_m = 1f;

		/**
		 * @param state Initial state
		 */
		public HodgkinHuxleySystem(float[] state) {
			super(state);
		}

		/**
		 * Set up the dynamical system
		 */
		public HodgkinHuxleySystem() {
			this(new float[4]);
		}

		public float[] f(float t, float[] u) {

			float I_inj = u[0];

			float[] state = getState();
			float V = state[0];
			float m = state[1];
			float h = state[2];
			float n = state[3];

		    float alpha_m = (25f-V) / (10f * ((float) Math.exp((25d-V)/10d) - 1f));
		    float beta_m = 4 * (float) Math.exp(-V/18d);
		    float alpha_h = 0.07f * (float) Math.exp(-V/20d);
		    float beta_h = 1f / ((float) Math.exp((30d-V)/10d) + 1f);
		    float alpha_n = (10f-V) / (100f * ((float) Math.exp((10d-V)/10d) - 1f));
		    float beta_n = 0.125f * (float) Math.exp(-V/80d);

		    return new float[] { // dV, dm, dh, dn
				    1000 * ((G_Na * (m*m*m) * h * (E_Na - V) + G_K * (n*n*n*n) * (E_K - V) + G_m * (V_rest - V) + I_inj) / C_m),
				    1000 * (alpha_m * (1-m) - beta_m * m),
				    1000 * (alpha_h * (1-h) - beta_h * h),
				    1000 * (alpha_n * (1-n) - beta_n * n)
		    };
		}

		/**
		 * @see ca.nengo.dynamics.impl.AbstractDynamicalSystem#g(float, float[])
		 */
		public float[] g(float t, float[] u) {
			return getState();
		}

		/**
		 * @see ca.nengo.dynamics.impl.AbstractDynamicalSystem#getInputDimension()
		 */
		public int getInputDimension() {
			return 1;
		}

		/**
		 * @see ca.nengo.dynamics.impl.AbstractDynamicalSystem#getOutputDimension()
		 */
		public int getOutputDimension() {
			return 4;
		}

	}

	/**
	 * A factory of neurons with linear synaptic integration and Hodgkin-Huxley spike
	 * generation.
	 *
	 * @author Bryan Tripp
	 */
	public static class HodgkinHuxleyNeuronFactory implements NodeFactory {

		private static final long serialVersionUID = 1L;

		/**
		 * @see ca.nengo.model.impl.NodeFactory#make(java.lang.String)
		 */
		public Node make(String name) throws StructuralException {
			LinearSynapticIntegrator integrator = new LinearSynapticIntegrator(.001f, Units.ACU);
			SpikeGenerator sg = new HodgkinHuxleySpikeGenerator();
			return new ExpandableSpikingNeuron(integrator, sg, 10, 0, name);
		}

		/**
		 * @see ca.nengo.model.impl.NodeFactory#getTypeDescription()
		 */
		public String getTypeDescription() {
			return "Hodgkin-Huxley Neuron";
		}
	}

	//functional test
//	public static void main(String[] args) {
//		float[] x0 = new float[4];
////		x0[0] = 5;
//		DynamicalSystem hh = new HodgkinHuxleySystem(x0);
//		Integrator integrator = new RK45Integrator(1e-6f);
//		TimeSeries input = new TimeSeries1DImpl(new float[]{0, .1f}, new float[]{10, 10}, Units.uAcm2);
//		long start = System.currentTimeMillis();
//		TimeSeries result = integrator.integrate(hh, input);
//		System.out.println("Elapsed time: " + (System.currentTimeMillis() - start));
//		Plotter.plot(result, "Hodgkin-Huxley");
//
//		try {
//			Network network = new NetworkImpl();
//
//			LinearSynapticIntegrator si = new LinearSynapticIntegrator(.001f, Units.ACU);
//
//			SpikeGenerator sg = new HodgkinHuxleySpikeGenerator();
//			PlasticExpandableSpikingNeuron neuron = new PlasticExpandableSpikingNeuron(si, sg, 10, 0, "neuron");
//			Ensemble ensemble = new EnsembleImpl("ensemble", new Node[]{neuron});
//			ensemble.collectSpikes(true);
//			network.addNode(ensemble);
//
//			FunctionInput fi = new FunctionInput("input", new Function[]{new ConstantFunction(1, 1)}, Units.ACU);
//			network.addNode(fi);
//
//			neuron.addTermination("input", new float[][]{new float[]{1}}, .005f, false);
//			network.addProjection(fi.getOrigin(FunctionInput.ORIGIN_NAME), neuron.getTermination("input"));
//
//			network.run(0, .5f);
//
//			Plotter.plot(ensemble.getSpikePattern());
//		} catch (StructuralException e) {
//			e.printStackTrace();
//		} catch (SimulationException e) {
//			e.printStackTrace();
//		}
//
//	}

}
