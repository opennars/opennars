/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PoissonSpikeGenerator.java". Description:
"A phenomenological SpikeGenerator that produces spikes according to a Poisson
  process with a rate that varies as a function of current"

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

package ca.nengo.neural.neuron.impl;

import ca.nengo.math.Function;
import ca.nengo.math.PDF;
import ca.nengo.math.PDFTools;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.math.impl.LinearFunction;
import ca.nengo.math.impl.PoissonPDF;
import ca.nengo.math.impl.SigmoidFunction;
import ca.nengo.model.*;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.impl.SpikeOutputImpl;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.neural.neuron.SynapticIntegrator;
import ca.nengo.util.MU;

/**
 * A phenomenological SpikeGenerator that produces spikes according to a Poisson
 * process with a rate that varies as a function of current.
 *
 * TODO: test
 *
 * @author Bryan Tripp
 */
public class PoissonSpikeGenerator implements SpikeGenerator {

	private static final long serialVersionUID = 1L;

	private static final SimulationMode[] ourSupportedModes
		= new SimulationMode[]{SimulationMode.DEFAULT, SimulationMode.CONSTANT_RATE, SimulationMode.RATE};

	private Function myRateFunction;
	private SimulationMode myMode;

	/**
	 * @param rateFunction Maps input current to Poisson spiking rate
	 */
	public PoissonSpikeGenerator(Function rateFunction) {
		setRateFunction(rateFunction);
		myMode = SimulationMode.DEFAULT;
	}

	/**
	 * Uses a default sigmoid rate function
	 */
	public PoissonSpikeGenerator() {
		this(new SigmoidFunction(.5f, 10f, 0f, 20f));
	}

	/**
	 * @return Function that maps input current to Poisson spiking rate
	 */
	public Function getRateFunction() {
		return myRateFunction;
	}

	/**
	 * @param function Function that maps input current to Poisson spiking rate
	 */
	public void setRateFunction(Function function) {
		if (function.getDimension() != 1) {
			throw new IllegalArgumentException("Function must be one-dimensional (mapping from driving current to rate)");
		}
		myRateFunction = function;
	}

	/**
	 * @see ca.nengo.neural.neuron.SpikeGenerator#run(float[], float[])
	 */
	public InstantaneousOutput run(float[] time, float[] current) {
		InstantaneousOutput result = null;

		if (myMode.equals(SimulationMode.CONSTANT_RATE)) {
			result = new RealOutputImpl(new float[]{myRateFunction.map(new float[]{current[0]})}, Units.SPIKES_PER_S, time[time.length-1]);
		} else if (myMode.equals(SimulationMode.RATE)) {
			float totalTimeSpan = time[time.length-1] - time[0];
			float ratePerSecond = myRateFunction.map(new float[]{MU.mean(current)});
			float ratePerStep = totalTimeSpan * ratePerSecond;
			float numSpikes = new PoissonPDF(ratePerStep).sample()[0];

			result = new RealOutputImpl(new float[]{numSpikes / totalTimeSpan}, Units.SPIKES_PER_S, time[time.length-1]);
		} else {
			boolean spike = false;
			for (int i = 0; i < time.length - 1 && !spike; i++) {
				float timeSpan = time[i+1] - time[i];

				float rate = myRateFunction.map(new float[]{current[i]});
				double probNoSpikes = Math.exp(-rate*timeSpan);
				spike = (PDFTools.random() > probNoSpikes);
			}

			result = new SpikeOutputImpl(new boolean[]{spike}, Units.SPIKES, time[time.length-1]);
		}

		return result;
	}

	/**
	 * @see ca.nengo.model.SimulationMode.ModeConfigurable#getMode()
	 */
	public SimulationMode getMode() {
		return myMode;
	}

	/**
	 * @see ca.nengo.model.SimulationMode.ModeConfigurable#setMode(ca.nengo.model.SimulationMode)
	 */
	public void setMode(SimulationMode mode) {
		myMode = SimulationMode.getClosestMode(mode, ourSupportedModes);
	}

//	public float runConstantRate(float time, float current) throws SimulationException {
//		return myRateFunction.map(new float[]{current});
//	}

	/**
	 * This method does nothing, because a Poisson process is stateless.
	 *
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
	}

	@Override
	public SpikeGenerator clone() throws CloneNotSupportedException {
		PoissonSpikeGenerator result = (PoissonSpikeGenerator) super.clone();
		result.myRateFunction = myRateFunction.clone();
		return result;
	}

	/**
	 * Creates PoissonSpikeGenerators with linear response functions.
	 *
	 * @author Bryan Tripp
	 */
	public static class LinearFactory implements SpikeGeneratorFactory {

		private static final long serialVersionUID = 1L;

		PDF myMaxRate;
		PDF myIntercept;
		boolean myRectified;

		/**
		 * Set reasonable defaults
		 */
		public LinearFactory() {
			myMaxRate = new IndicatorPDF(200, 400);
			myIntercept = new IndicatorPDF(-.9f, .9f);
			myRectified = true;
		}

		/**
		 * @return Firing rate of produced SpikeGenerators when input current is 1
		 */
		public PDF getMaxRate() {
			return myMaxRate;
		}

		/**
		 * @param maxRate Firing rate of produced SpikeGenerators when input current is 1
		 */
		public void setMaxRate(PDF maxRate) {
			myMaxRate = maxRate;
		}

		/**
		 * @return Input current at which firing rate is zero
		 */
		public PDF getIntercept() {
			return myIntercept;
		}

		/**
		 * @param intercept Input current at which firing rate is zero
		 */
		public void setIntercept(PDF intercept) {
			myIntercept = intercept;
		}

		/**
		 * @return If true, response functions will be rectified (firing rates > 0)
		 */
		public boolean getRectified() {
			return myRectified;
		}

		/**
		 * @param rectified If true, response functions will be rectified (firing rates > 0)
		 */
		public void setRectified(boolean rectified) {
			myRectified = rectified;
		}

		/**
		 * @see ca.nengo.neural.neuron.impl.SpikeGeneratorFactory#make()
		 */
		public SpikeGenerator make() {
			float maxRate = myMaxRate.sample()[0];
			float intercept = myIntercept.sample()[0];
			float slope = maxRate / (1-intercept);
			float bias = - slope * intercept;
			Function line = new LinearFunction(new float[]{slope}, bias, myRectified);
			return new PoissonSpikeGenerator(line);
		}

	}

	/**
	 * A factory for neurons with linear or rectified linear response functions.
	 *
	 * @author bryan
	 *
	 */
	public static class LinearNeuronFactory implements NodeFactory {

		private static final long serialVersionUID = 1L;

		private static final float ourMaxTimeStep = .0005f;
		private static final Units ourCurrentUnits = Units.ACU;

		private final LinearFactory mySpikeGeneratorFactory;

		/**
		 * @param maxRate PDF for maximum spike rate
		 * @param intercept PDF for x-intercept
		 * @param rectified Rectify the curve?
		 */
		public LinearNeuronFactory(PDF maxRate, PDF intercept, boolean rectified) {
			mySpikeGeneratorFactory = new LinearFactory();
			mySpikeGeneratorFactory.setIntercept(intercept);
			mySpikeGeneratorFactory.setMaxRate(maxRate);
			mySpikeGeneratorFactory.setRectified(rectified);
		}

		/**
		 * @see ca.nengo.model.impl.NodeFactory#make(java.lang.String)
		 */
		public Node make(String name) throws StructuralException {
			SynapticIntegrator integrator = new LinearSynapticIntegrator(ourMaxTimeStep, ourCurrentUnits);
			SpikeGenerator generator = mySpikeGeneratorFactory.make();
			return new ExpandableSpikingNeuron(integrator, generator, 1, 0, name);
		}

		/**
		 * @see ca.nengo.model.impl.NodeFactory#getTypeDescription()
		 */
		public String getTypeDescription() {
			return "Linear Neuron";
		}

	}

	/**
	 * Creates sigmoid neurons (I guess rate-mode Poisson neurons?)
	 */
	public static class SigmoidFactory implements SpikeGeneratorFactory {

		private static final long serialVersionUID = 1L;

		private PDF mySlope;
		private PDF myInflection;
		private PDF myMaxRate;

		/**
		 * Set reasonable defaults
		 */
		public SigmoidFactory() {
			mySlope = new IndicatorPDF(1, 10);
			myInflection = new IndicatorPDF(-1f, 1f);
			myMaxRate = new IndicatorPDF(200, 400);
		}

		/**
		 * @return Distribution of slopes of the sigmoid functions that describe current-firing rate relationships
		 * 		before scaling to maxRate (slope at inflection point = slope*maxRate)
		 */
		public PDF getSlope() {
			return mySlope;
		}

		/**
		 * @param slope Distribution of slopes of the sigmoid functions that describe current-firing rate relationships
		 * 		before scaling to maxRate (slope at inflection point = slope*maxRate)
		 */
		public void setSlope(PDF slope) {
			mySlope = slope;
		}

		/**
		 * @return Distribution of inflection points of the sigmoid functions that describe current-firing rate relationships
		 */
		public PDF getInflection() {
			return myInflection;
		}

		/**
		 * @param inflection Distribution of inflection points of the sigmoid functions that describe current-firing rate relationships
		 */
		public void setInflection(PDF inflection) {
			myInflection = inflection;
		}

		/**
		 * @return Distribution of maximum firing rates
		 */
		public PDF getMaxRate() {
			return myMaxRate;
		}

		/**
		 * @param maxRate Distribution of maximum firing rates
		 */
		public void setMaxRate(PDF maxRate) {
			myMaxRate = maxRate;
		}

		/**
		 * @see ca.nengo.neural.neuron.impl.SpikeGeneratorFactory#make()
		 */
		public SpikeGenerator make() {
			Function sigmoid = new SigmoidFunction(myInflection.sample()[0], mySlope.sample()[0], 0, myMaxRate.sample()[0]);
			return new PoissonSpikeGenerator(sigmoid);
		}

	}

	/**
	 * A factory for neurons with sigmoid response functions.
	 *
	 * @author Bryan Tripp
	 */
	public static class SigmoidNeuronFactory implements NodeFactory {

		private static final long serialVersionUID = 1L;

		private static final float ourMaxTimeStep = .0005f;
		private static final Units ourCurrentUnits = Units.ACU;

		private final SigmoidFactory mySigmoidFactory;

		/**
		 * Neurons from this factory will have Poisson firing rates that are sigmoidal functions
		 * of current. The constructor arguments parameterize the sigmoid function.
		 *
		 * @param slope Distribution of slopes of the sigmoid functions that describe current-firing rate relationships,
		 * 		before scaling to maxRate (slope at inflection point = slope*maxRate)
		 * @param inflection Distribution of inflection points of the sigmoid functions that describe current-firing rate relationships
		 * @param maxRate Distribution of maximum firing rates
		 */
		public SigmoidNeuronFactory(PDF slope, PDF inflection, PDF maxRate) {
			mySigmoidFactory = new SigmoidFactory();
			mySigmoidFactory.setSlope(slope);
			mySigmoidFactory.setInflection(inflection);
			mySigmoidFactory.setMaxRate(maxRate);
		}

		/**
		 * @see ca.nengo.model.impl.NodeFactory#make(java.lang.String)
		 */
		public Neuron make(String name) throws StructuralException {
			SynapticIntegrator integrator = new LinearSynapticIntegrator(ourMaxTimeStep, ourCurrentUnits);
			SpikeGenerator generator = mySigmoidFactory.make();
			return new ExpandableSpikingNeuron(integrator, generator, 1, 0, name);
		}

		/**
		 * @see ca.nengo.model.impl.NodeFactory#getTypeDescription()
		 */
		public String getTypeDescription() {
			return "Sigmoid Neuron";
		}

	}
}
