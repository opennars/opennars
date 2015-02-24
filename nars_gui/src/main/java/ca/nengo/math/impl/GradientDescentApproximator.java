/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "GradientDescentApproximator.java". Description:
"A LinearApproximator that searches for coefficients by descending an error gradient"

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

package ca.nengo.math.impl;

import ca.nengo.math.ApproximatorFactory;
import ca.nengo.math.Function;
import ca.nengo.math.LinearApproximator;
import ca.nengo.util.MU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

//import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * A LinearApproximator that searches for coefficients by descending an error gradient.
 * This method is slower and less powerful than WeightedCostApproximator, but
 * constraints on coefficients are allowed.
 *
 * @author Bryan Tripp
 */
public class GradientDescentApproximator implements LinearApproximator {

	private static final Logger ourLogger = LogManager.getLogger(GradientDescentApproximator.class);
	private static final long serialVersionUID = 1L;

	private float[][] myEvalPoints;
	private float[][] myValues;
	private float[] myStartingCoefficients;
	private Constraints myConstraints;
	private int myMaxIterations;
	private float myRate;
	private float myTolerance;
	private boolean myIgnoreBias;

	/**
	 * @param evaluationPoints Points at which error is evaluated (should be uniformly
	 * 		distributed, as the sum of error at these points is treated as an integral
	 * 		over the domain of interest). Examples include vector inputs to an ensemble,
	 * 		or different points in time	within different simulation regimes.
	 * @param values The values of whatever functions are being combined, at the
	 * 		evaluationPoints. Commonly neuron firing rates. The first dimension makes up
	 * 		the list of functions, and the second the values of these functions at each
	 * 		evaluation point.
	 * @param constraints Constraints on coefficients
	 * @param ignoreBias If true, bias in constituent and target functions is ignored (resulting
	 * 		estimate will be biased)
	 */
	public GradientDescentApproximator(float[][] evaluationPoints, float[][] values, Constraints constraints, boolean ignoreBias) {
		assert MU.isMatrix(evaluationPoints);
		assert MU.isMatrix(values);
		assert evaluationPoints.length == values[0].length;

		myEvalPoints = evaluationPoints;
		myValues = values;
		myConstraints = constraints;
		myMaxIterations = 1000;
		myStartingCoefficients = new float[values.length];
		myRate = 0.5f / myValues.length;
		myTolerance = .000000001f;

		myIgnoreBias = ignoreBias;
		if (ignoreBias) {
			for (int i = 0; i < myValues.length; i++) {
				myValues[i] = unbias(myValues[i]);
			}
		}
	}

	/**
	 * @see ca.nengo.math.LinearApproximator#getEvalPoints()
	 */
	public float[][] getEvalPoints() {
		return myEvalPoints;
	}

	/**
	 * @see ca.nengo.math.LinearApproximator#getValues()
	 */
	public float[][] getValues() {
		return myValues;
	}

	/**
	 * @param coefficients Coefficients at which to start the optimization
	 */
	public void setStartingCoefficients(float[] coefficients) {
		myStartingCoefficients = coefficients;
	}

	/**
	 * @return Maximum iterations per findCoefficients(...)
	 */
	public int getMaxIterations() {
		return myMaxIterations;
	}

	/**
	 * @param max New maximum number of iterations per findCoefficients(...)
	 */
	public void setMaxIterations(int max) {
		myMaxIterations = max;
	}

	/**
	 * @return Target mean-squared error
	 */
	public float getTolerance()  {
		return myTolerance;
	}

	/**
	 * @param tolerance Target mean-squared error
	 */
	public void setTolerance(float tolerance) {
		myTolerance = tolerance;
	}

	/**
	 * @see ca.nengo.math.LinearApproximator#findCoefficients(ca.nengo.math.Function)
	 */
	public float[] findCoefficients(Function target) {
		float[] result = new float[myValues.length];
		System.arraycopy(myStartingCoefficients, 0, result, 0, result.length);

		float[] targetValues = getTargetValues(target);

		boolean stuck = false;
		boolean done = false;
		float[] error = findError(targetValues, result);
		for (int i = 0; i < myMaxIterations && !stuck && !done; i++) {
			for (int j = 0; j < myValues.length; j++) {
				float norm = MU.prod(myValues[j], myValues[j]);
				if (norm > 0) {
					result[j] -= myRate * MU.prod(error, myValues[j]) / norm; //(float) myEvalPoints.length;
				}
			}
			stuck = myConstraints.correct(result);

			error = findError(targetValues, result);
			float mse = MU.prod(error, error) / error.length;
			done = mse < myTolerance;
			ourLogger.debug("Iteration: " + i + "  MSE: " + mse + " Stuck: " + stuck);
		}

		return result;
	}

	//finds values of target function at eval points
	private float[] getTargetValues(Function target) {
		float[] result = new float[myEvalPoints.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = target.map(myEvalPoints[i]);
		}
		if (myIgnoreBias) {
            result = unbias(result);
        }
		return result;
	}

	//finds approximation error with given coefficients and target function values
	//we don't use matrix ops here because a a transpose would be needed
	private float[] findError(float[] target, float[] coefficients) {
		float[] result = new float[target.length];
		for (int i = 0; i < result.length; i++) {
			float estimate = 0f;
			for (int j = 0; j < myValues.length; j++) {
				estimate += myValues[j][i] * coefficients[j];
			}
			result[i] = estimate - target[i];
		}
		return result;
	}

	//removes bias
	private float[] unbias(float[] x) {
		float sum = 0;
		for (float element : x) {
			sum += element;
		}
		float bias = sum / x.length;

		float[] result = new float[x.length];
		for (int i = 0; i < x.length; i++) {
			result[i] = x[i] - bias;
		}

		return result;
	}

	@Override
	public LinearApproximator clone() throws CloneNotSupportedException {
		GradientDescentApproximator result = (GradientDescentApproximator) super.clone();

		result.myStartingCoefficients = myStartingCoefficients.clone();

		result.myConstraints = myConstraints.clone();

		float[][] evalPoints = new float[myEvalPoints.length][];
		for (int i = 0; i < evalPoints.length; i++) {
			evalPoints[i] = myEvalPoints[i].clone();
		}
		result.myEvalPoints = evalPoints;

		float[][] values = new float[myValues.length][];
		for (int i = 0; i < values.length; i++) {
			values[i] = myValues[i].clone();
		}
		result.myValues = values;

		return result;
	}

	/**
	 * Enforces constraints on coefficients.
	 *
	 * TODO: should this be generalized to LinearApproximator?
	 *
	 * @author Bryan Tripp
	 */
	public static interface Constraints extends Serializable, Cloneable {

		/**
		 * @param coefficients A set of coefficients which may violate constraints (they
		 * 		are altered as little as possible by this method so that they satisfy
		 * 		constraints after the call)
		 * @return True if all coefficients had to be corrected (no further improvement
		 * 		is possible in the attempted direction)
		 */
		boolean correct(float[] coefficients);

		/**
		 * @return Valid clone
		 * @throws CloneNotSupportedException if clone can't be made
		 */
		public Constraints clone() throws CloneNotSupportedException;
	}

	/**
	 * An ApproximatorFactory that produces GradientDescentApproximators.
	 *
	 * @author Bryan Tripp
	 */
	public static class Factory implements ApproximatorFactory {

		private static final long serialVersionUID = 1L;

		private final Constraints myConstraints;
		private final boolean myIgnoreBiasFlag;

		/**
		 * @param constraints As in GradientDescentApproximator constructor
		 * @param ignoreBias As in GradientDescentApproximator constructor
		 */
		public Factory(Constraints constraints, boolean ignoreBias) {
			myConstraints = constraints;
			myIgnoreBiasFlag = ignoreBias;
		}

		/**
		 * @see ca.nengo.math.ApproximatorFactory#getApproximator(float[][], float[][])
		 */
		public LinearApproximator getApproximator(float[][] evalPoints, float[][] values) {
			return new GradientDescentApproximator(evalPoints, values, myConstraints, myIgnoreBiasFlag);
		}

		@Override
		public ApproximatorFactory clone() throws CloneNotSupportedException {
			return new Factory(myConstraints.clone(), myIgnoreBiasFlag);
		}

	}

	/**
	 * Forces all decoding coefficients to be >= 0.
	 *
	 * @author Bryan Tripp
	 */
	public static class CoefficientsSameSign implements Constraints {

		private static final long serialVersionUID = 1L;

		private final boolean mySignPositive;

		/**
		 * @param positive Sign to force all coefficients to
		 */
		public CoefficientsSameSign(boolean positive) {
			mySignPositive = positive;
		}

		/**
		 * @see Constraints#correct(float[])
		 */
		public boolean correct(float[] coefficients) {
			boolean allCorrected = true;
			for (int i = 0; i < coefficients.length; i++) {
				if ( (mySignPositive && coefficients[i] < 0) || (!mySignPositive && coefficients[i] > 0)) {
					coefficients[i] = 0;
				} else {
					allCorrected = false;
				}
			}
			return allCorrected;
		}

		@Override
		public Constraints clone() throws CloneNotSupportedException {
			return (Constraints) super.clone();
		}

	}
}
