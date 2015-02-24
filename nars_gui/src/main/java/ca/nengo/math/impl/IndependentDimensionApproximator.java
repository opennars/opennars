/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "IndependentDimensionApproximator.java". Description:
"A LinearApproximator for functions with no multidimensional nonlinearities"

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
 * Created on 28-Jul-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.ApproximatorFactory;
import ca.nengo.math.Function;
import ca.nengo.math.LinearApproximator;
import ca.nengo.util.MU;
import ca.nengo.util.VectorGenerator;
import ca.nengo.util.impl.RandomHypersphereVG;

/**
 * A LinearApproximator for functions with no multidimensional nonlinearities. Each of the source functions
 * is assumed to be a function of one dimension. Consequently, only functions of one dimension can be decoded
 * directly. Linear functions of multiple dimensions can obtained later by combining weights of one-dimensional
 * functions.
 *
 * @author Bryan Tripp
 */
public class IndependentDimensionApproximator implements LinearApproximator {

	private static final long serialVersionUID = 1L;

	private LinearApproximator[] myApproximators; //for each dimension
	private int[][] myIndices; //values indices for each dimension
	private int[] myDimensions; //dimension for each neuron
	private float[][] myEvalPoints;
	private float[][] myValues;

	/**
	 * @param evaluationPoints Points of evaluation of source functions, in the dimension along which they vary
	 * @param values Values of each source function at each point
	 * @param dimensions The dimension along which each function varies
	 * @param dimension Dimension of the space from which source functions map
	 * @param costFunction As in WeightedCostApproximator, but in dimension along which functions vary
	 * @param noise Proportion of noise to add
	 */
	public IndependentDimensionApproximator(float[] evaluationPoints, float[][] values, int[] dimensions, int dimension, Function costFunction, float noise) {
		assert MU.isMatrix(values);
		assert evaluationPoints.length == values[0].length;
		assert values.length == dimensions.length;

		myIndices = new int[dimension][];
		int[] dimCount = new int[dimension];
		for (int i = 0; i < dimension; i++) {
			myIndices[i] = new int[values.length];
		}

		for (int i = 0; i < dimensions.length; i++) {
			int dim = dimensions[i];
			myIndices[dim][dimCount[dim]++] = i;
		}

		float[][] ep = new float[evaluationPoints.length][];
		for (int i = 0; i < ep.length; i++) {
			ep[i] = new float[]{evaluationPoints[i]};
		}

		myApproximators = new LinearApproximator[dimension];
		for (int i = 0; i < dimension; i++) {
			float[][] dimValues = new float[dimCount[i]][];
			for (int j = 0; j < dimCount[i]; j++) {
				dimValues[j] = values[myIndices[i][j]];
			}
			myApproximators[i] = new WeightedCostApproximator(ep, dimValues, costFunction, noise, -1);
		}

		myDimensions = dimensions;
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
	 * @see ca.nengo.math.LinearApproximator#findCoefficients(ca.nengo.math.Function)
	 */
	public float[] findCoefficients(Function target) {
		if ( !(target instanceof IdentityFunction) ) {
			throw new IllegalArgumentException("Only IdentityFunction supported");
			//TODO: could generalize to any function that maps from a single dimension by wrapping below instead of new IdentityFunction
		}

		int dim = ((IdentityFunction) target).getIdentityDimension();

		float[] weights = myApproximators[dim].findCoefficients(new IdentityFunction(1, 0));
		float[] result = new float[myDimensions.length];

		for (int i = 0; i < weights.length; i++) {
			result[myIndices[dim][i]] = weights[i];
		}

		return result;
	}

	@Override
	public LinearApproximator clone() throws CloneNotSupportedException {
		IndependentDimensionApproximator result = (IndependentDimensionApproximator) super.clone();

		result.myDimensions = myDimensions.clone();

		LinearApproximator[] approximators = new LinearApproximator[myApproximators.length];
		for (int i = 0; i < approximators.length; i++) {
			approximators[i] = myApproximators[i].clone();
		}
		result.myApproximators = approximators;

		int[][] indices = new int[myIndices.length][];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = myIndices[i].clone();
		}
		result.myIndices = indices;

		return result;
	}

	/**
	 * Factory for IndependentDimensionApproximators.
	 *
	 * @author Bryan Tripp
	 */
	public static class Factory implements ApproximatorFactory {

		private static final long serialVersionUID = 1L;

////		private int myDimensions;
//		private int myNeuronsPerDim;
//
//		public Factory(int dimensions, int neurons) {
//			myDimensions = dimensions;
//			myNeuronsPerDim = neurons / dimensions;
//			if (neurons % dimensions != 0) {
//				throw new IllegalArgumentException("Expected # neurons to be evenly divisible by # dimensions");
//			}
//		}

		/**
		 * @see ca.nengo.math.ApproximatorFactory#getApproximator(float[][], float[][])
		 */
		public LinearApproximator getApproximator(float[][] evalPoints, float[][] values) {
			int dimensions = evalPoints[0].length; //OK
			int nodes = values.length;
			int nodesPerDim = nodes / dimensions;
			if (nodes % dimensions != 0) {
				throw new IllegalArgumentException("Expected # nodes (" + nodes + ") to be evenly divisible by # dimensions (" + dimensions + ')');
			}

			//TODO: this only work with axis-clustered ... is this enforced anywhere?
			//duplicate point along axis to all dimensions (shouldn't matter because
			//preferred directions are clustered on axes)
			float[][] allDimEvalPoints = new float[evalPoints.length][];
			float[] oneDimEvalPoints = new float[evalPoints.length];
			for (int i = 0; i < evalPoints.length; i++) {
				allDimEvalPoints[i] = new float[dimensions];
				for (int j = 0; j < dimensions; j++) {
					allDimEvalPoints[i][j] = evalPoints[i][0];
				}
				oneDimEvalPoints[i] = evalPoints[i][0];
			}

			int[] indepDims = new int[values.length];
			for (int i = 0; i < indepDims.length; i++) {
				indepDims[i] = (int) Math.floor((double) i / (double) nodesPerDim);
			}

			Function costWeight = new ConstantFunction(1, 1);
			return new IndependentDimensionApproximator(oneDimEvalPoints, values, indepDims, dimensions, costWeight, .1f);
		}

		@Override
		public ApproximatorFactory clone() throws CloneNotSupportedException {
			return (ApproximatorFactory) super.clone();
		}

	}

	/**
	 * A VectorGenerator for use with IndependentDimensionApproximator as an evaluation point factory.
	 * It returns a constant number of vectors regardless of the number requested. In each vector, all
	 * the elements are the same. The element is drawn from an underlying one-dimensional VectorGenerator.
	 * This allows creation of high dimensional ensembles where all encoders are on an axis, without
	 * evaluation responses at a number of evaluation points that grows with the number of dimensions
	 * (as would normally be required).
	 *
	 * @author Bryan Tripp
	 */
	public static class EvalPointFactory implements VectorGenerator {

		private float myRadius;
		private VectorGenerator myVG;
		private int myPoints;

		/**
		 * @param radius As RandomHypersphereGenerator arg
		 * @param points Number of vectors produced, regardless of number requested
		 */
		public EvalPointFactory(float radius, int points) {
			setRadius(radius);
			myPoints = points;
		}

		/**
		 * @return radius
		 */
		public float getRadius() {
			return myRadius;
		}

		/**
		 * @param radius Radius
		 */
		public void setRadius(float radius) {
			myRadius = radius;
			myVG = new RandomHypersphereVG(false, radius, 1);
		}

		/**
		 * @return Number of evaluation points
		 */
		public int getPoints() {
			return myPoints;
		}

		/**
		 * @param points Number of evaluation points
		 */
		public void setPoints(int points) {
			myPoints = points;
		}

		/**
		 * @see ca.nengo.util.VectorGenerator#genVectors(int, int)
		 */
		public float[][] genVectors(int number, int dimension) {
			float[][] oneDimensional = myVG.genVectors(myPoints, 1);
			float[][] result = new float[myPoints][];
			for (int i = 0; i < result.length; i++) {
				result[i] = new float[dimension];
				for (int j = 0; j < dimension; j++) {
					result[i][j] = oneDimensional[i][0];
				}
			}
			return result;
		}
	}

	/**
	 * A VectorGenerator for use with IndependentDimensionApproximator as an encoder factory. Encoders
	 * are derived from 1D encoders, and distributed to different dimensions in a round-robin manner.
	 * This convention is needed so that the ApproximatorFactory knows which response is associated with
	 * which dimension.
	 *
	 * @author Bryan Tripp
	 */
	public static class EncoderFactory implements VectorGenerator {

		private float myRadius;
		private VectorGenerator myVG;

		/**
		 * @param radius As RandomHypersphereGenerator arg
		 */
		public EncoderFactory(float radius) {
			setRadius(radius);
		}

		/**
		 * Defaults to radius 1.
		 */
		public EncoderFactory() {
			this(1);
		}

		/**
		 * @return radius
		 */
		public float getRadius() {
			return myRadius;
		}

		/**
		 * @param radius Radius
		 */
		public void setRadius(float radius) {
			myRadius = radius;
			myVG = new RandomHypersphereVG(true, radius, 1);
		}

		/**
		 * @see ca.nengo.util.VectorGenerator#genVectors(int, int)
		 */
		public float[][] genVectors(int number, int dimension) {
			float neuronsPerDim=number/dimension;
			float[][] oneDimensional = myVG.genVectors(number, 1);
			float[][] result = new float[number][];
			for (int neuron = 0; neuron < number; neuron++) {
				result[neuron] = new float[dimension];
				result[neuron][(int)(neuron/neuronsPerDim)] = oneDimensional[neuron][0];
				//result[i][i % dimension] = oneDimensional[i][0];
			}
			return result;
		}

	}
}
