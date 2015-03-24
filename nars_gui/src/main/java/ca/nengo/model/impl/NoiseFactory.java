/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NoiseFactory.java". Description:
"Default additive Noise implementations.

  TODO: unit tests

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
 * Created on 24-May-07
 */
package ca.nengo.model.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.math.Function;
import ca.nengo.math.PDF;
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.model.Noise;
import ca.nengo.model.Units;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;

/**
 * Default additive Noise implementations.
 *
 * TODO: unit tests
 *
 * @author Bryan Tripp
 */
public class NoiseFactory {

	/**
	 * @param frequency Frequency (in simulation time) with which new noise values are drawn from the PDF
	 * @param pdf PDF from which new noise values are drawn. The dimension must equal
	 * 		the input dimension of the dynamics.
	 * @return a new NoiseImplPDF with the given parameters
	 */
	public static Noise makeRandomNoise(float frequency, PDF pdf) {
		return new NoiseImplPDF(frequency, pdf, null, null);
	}

	/**
	 * @param frequency Frequency (in simulation time) with which new noise values are drawn from the PDF
	 * @param pdf PDF from which new noise values are drawn. The dimension must equal
	 * 		the input dimension of the dynamics.
	 * @param dynamics Dynamics through which raw noise values pass before they are combined with non-noise.
	 * 		The output dimension must equal the dimension of expected input to getValues().
	 * @param integrator Integrator used to solve dynamics
	 * @return a new NoiseImplPDF with the given parameters
	 */
	public static Noise makeRandomNoise(float frequency, PDF pdf, DynamicalSystem dynamics, Integrator integrator) {
		return new NoiseImplPDF(frequency, pdf, dynamics, integrator);
	}

	/**
	 * @return Zero additive Noise
	 */
	public static Noise makeNullNoise() {
		return new NoiseImplNull();
	}

	/**
	 * @param function A function of time
	 * @return Additive Noise where values are given explicit functions of time
	 */
	public static Noise makeExplicitNoise(Function function) {
		return new NoiseImplFunction(function);
	}

	/**
	 * Note: there are no public setters here for the same rule as in NoiseImplPDF.
	 *
	 * @author Bryan Tripp
	 *
	 */
	public static class NoiseImplFunction implements Noise {

		private static final long serialVersionUID = 1L;

		private Function myFunction;

		/**
		 * @param function A function of time that explicitly defines the noise
		 */
		public NoiseImplFunction(Function function) {
			myFunction = function;
		}

		/**
		 * Default zero noise.
		 */
		public NoiseImplFunction() {
			myFunction = new ConstantFunction(1, 0);
		}

		/**
		 * @return The function of time that explicitly defines the noise
		 */
		public Function getFunction() {
			return myFunction;
		}

		/**
		 * @see ca.nengo.model.Noise#getValue(float, float, float)
		 */
		public float getValue(float startTime, float endTime, float input) {
			return input + myFunction.map(new float[]{startTime});
		}

		@Override
		public Noise clone() {
			try {
				NoiseImplFunction result = (NoiseImplFunction) super.clone();
				result.myFunction = myFunction.clone();
				return result;
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * @see ca.nengo.model.Resettable#reset(boolean)
		 */
		public void reset(boolean randomize) {
		}

	}

	/**
	 * Zero additive Noise
	 */
	public static class NoiseImplNull implements Noise {

		private static final long serialVersionUID = 1L;

		/**
		 * @see ca.nengo.model.Noise#getValue(float, float, float)
		 */
		public float getValue(float startTime, float endTime, float input) {
			return input;
		}

		@Override
		public Noise clone() {
			return this; //allows sharing between dimensions
		}

		/**
		 * @see ca.nengo.model.Resettable#reset(boolean)
		 */
		public void reset(boolean randomize) {
		}

	}

	/**
	 * Note: setters are private, because Origins typically make copies for each output dimension,
	 * which would then not be updated with changes to the original. So to change noise properties
	 * the Noise object must be replaced.
	 *
	 * @author Bryan Tripp
	 */
	public static class NoiseImplPDF implements Noise {

		private static final long serialVersionUID = 1L;

		private float myPeriod;
		private PDF myPDF;
		private DynamicalSystem myDynamics;
		private Integrator myIntegrator;
		private float myLastGenTime = 0;
		private float myLastDynamicsTime = 0;
		private float[] myLastRawNoise;
		private float[] myCurrentRawNoise;
		private Units[] myUnits;
		private float[] myInitialState;

		/**
		 * @param frequency Frequency (in simulation time) with which new noise values are drawn from the PDF
		 * @param pdf PDF from which new noise values are drawn. The dimension of the space over which the PDF is defined
		 * 		must equal the input dimension of the dynamics.
		 * @param dynamics Dynamics through which raw noise values pass before they are combined with non-noise.
		 * 		The input dimension must match the PDF and the output dimension must equal one. Can be null in which
		 * 		case the PDF must be one-dimensional.
		 * @param integrator Integrator used to solve dynamics. Can be null if dynamics is null.
		 */
		public NoiseImplPDF(float frequency, PDF pdf, DynamicalSystem dynamics, Integrator integrator) {
			if (dynamics != null && pdf.getDimension() != dynamics.getInputDimension()) {
				throw new IllegalArgumentException("PDF dimension (" + pdf.getDimension() + ") must equal dynamics input dimension ("
						+ dynamics.getInputDimension() + ')');
			}

			setFrequency(frequency);
			setPDF(pdf);
			setDynamics(dynamics);
			myIntegrator = integrator;
		}

		/**
		 * @return Frequency (in simulation time) with which new noise values are drawn from the PDF
		 */
		public float getFrequency() {
			return 1f /myPeriod;
		}

		/**
		 * @param frequency Frequency (in simulation time) with which new noise values are drawn from the PDF
		 */
		public void setFrequency(float frequency) {
			myPeriod = 1f / frequency;
		}

		/**
		 * @return PDF from which new noise values are drawn. The dimension of the space over which the PDF is defined
		 * 		must equal the input dimension of the dynamics.
		 */
		public PDF getPDF() {
			return myPDF;
		}

		/**
		 * @param pdf PDF from which new noise values are drawn. The dimension of the space over which the PDF is defined
		 * 		must equal the input dimension of the dynamics.
		 */
		private void setPDF(PDF pdf) {
			if (myDynamics == null && pdf.getDimension() != 1) {
				throw new IllegalArgumentException("With null dynamics, the PDF must be defined over one dimension.");
			}

			myPDF = pdf;
			myCurrentRawNoise = myPDF.sample();
			myUnits = Units.uniform(Units.UNK, myCurrentRawNoise.length);
		}

		/**
		 * @return Dynamics through which raw noise values pass before they are combined with non-noise.
		 * 		The input dimension must match the PDF and the output dimension must equal one. Can be null in which
		 * 		case the PDF must be one-dimensional.
		 */
		public DynamicalSystem getDynamics() {
			return myDynamics;
		}

		/**
		 * @param dynamics Dynamics through which raw noise values pass before they are combined with non-noise.
		 * 		The input dimension must match the PDF and the output dimension must equal one. Can be null in which
		 * 		case the PDF must be one-dimensional.
		 */
		private void setDynamics(DynamicalSystem dynamics) {
			if (dynamics != null && dynamics.getOutputDimension() != 1) {
				throw new IllegalArgumentException("The output of the dynamics must be one-dimensional");
			}

			myDynamics = dynamics;
			if (myDynamics != null) {
                myInitialState = dynamics.getState();
            }
		}

		/**
		 * @return Integrator used to solve dynamics. Can be null if dynamics is null.
		 */
		public Integrator getIntegrator() {
			return myIntegrator;
		}

		/**
		 * @see ca.nengo.model.Noise#getValue(float, float, float)
		 */
		public float getValue(float startTime, float endTime, float input) {
			float result = input;

			myLastRawNoise = myCurrentRawNoise;
			if (endTime >= myLastGenTime + myPeriod || endTime < myLastGenTime) {
				myCurrentRawNoise = myPDF.sample();
				myLastGenTime = endTime;
			}

			if (myDynamics == null) {
				result = input + myCurrentRawNoise[0];
			} else {
				TimeSeries raw = new TimeSeriesImpl(new float[]{myLastDynamicsTime, endTime},
						new float[][]{myLastRawNoise, myCurrentRawNoise}, myUnits);
				float[][] output = myIntegrator.integrate(myDynamics, raw).getValues();
				result = input + output[output.length-1][0];
				myLastDynamicsTime = endTime;
			}

			return result;
		}

		@Override
		public Noise clone() {
			//must return an independent copy of this Noise since there may be a DynamicalSystem with state
			try {
				NoiseImplPDF result = (NoiseImplPDF) super.clone();
				if (myDynamics != null) {
					result.setDynamics(myDynamics.clone());
				}
				return result;
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * @see ca.nengo.model.Resettable#reset(boolean)
		 */
		public void reset(boolean randomize) {
			if (myDynamics != null) {
                myDynamics.setState(myInitialState);
            }
			myLastDynamicsTime = 0;
			myLastGenTime = 0;
		}

	}
}
