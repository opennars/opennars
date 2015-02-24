/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "CompositeApproximator.java". Description:
"A LinearApproximator that approximates multi-dimensional functions as sums of
  lower-dimensional functions"

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
 * Created on 5-Feb-2007
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;
import ca.nengo.math.LinearApproximator;

/**
 * <p>A LinearApproximator that approximates multi-dimensional functions as sums of
 * lower-dimensional functions. Each lower-dimensional function is approximated by
 * a component approximator, which is provided in the constructor. The resulting
 * approximation is the sum of approximations produced by each component.</p>
 *
 * <p>CompositeApproximator is similar to the simpler IndependentDimensionApproximator,
 * but more general because dimensions can be handled either independently or in
 * arbitrary groups.</p>
 *
 * <p>CompositeApproximator is useful for low-dimensionally non-linear functions
 * of high-dimensional vectors, eg x1*x2 + x3*x4 - x5*x6.</p>
 *
 * <p>It is also useful for creating accurate, high-dimensional ensembles of neurons
 * with a little overlap between dimensions.</p>
 *
 * TODO: should LinearApproximator have getDimension()? would be possible to get rid of 2nd constructor arg then
 * TODO: test
 *
 * @author Bryan Tripp
 */
public class CompositeApproximator implements LinearApproximator {

	private static final long serialVersionUID = 1L;

	private LinearApproximator[] myComponents;
	private int[][] myDimensions;
//	private int myDimension;

	/**
	 * @param components LinearApproximators that make up the composite
	 * @param dimensions dimensionality of each LinearApproximator
	 */
	public CompositeApproximator(LinearApproximator[] components, int[][] dimensions) {
		if (components.length != dimensions.length) {
			throw new IllegalArgumentException("Length of dimensions list must equal number of components ("
					+ dimensions.length + " vs " + components.length + ')');
		}

		myComponents = components;
		myDimensions = dimensions;

//		myDimension = 0;
//		for (int i = 0; i < dimensions.length; i++) {
//			myDimension += dimensions[i].length;
//		}
	}

	/**
	 * @see ca.nengo.math.LinearApproximator#getEvalPoints()
	 */
    public float[][] getEvalPoints() {
		throw new RuntimeException("This method has not yet been implemented for CompositeApproximator");
	}

	/**
	 * @see ca.nengo.math.LinearApproximator#getValues()
	 */
    public float[][] getValues() {
		throw new RuntimeException("This method has not yet been implemented for CompositeApproximator");
	}

	/**
	 * @see ca.nengo.math.LinearApproximator#findCoefficients(ca.nengo.math.Function)
	 */
    public float[] findCoefficients(Function target) {
		float[] result = new float[0];

		for (int i = 0; i < myDimensions.length; i++) {
			Function f = new FunctionWrapper(target, myDimensions[i]);
			float[] compCoeff = myComponents[i].findCoefficients(f);

			float[] newResult = new float[result.length + compCoeff.length];
			System.arraycopy(result, 0, newResult, 0, result.length);
			System.arraycopy(compCoeff, 0, newResult, result.length, compCoeff.length);
			result = newResult;
		}

		return result;
	}

	private static class FunctionWrapper extends AbstractFunction {

		private static final long serialVersionUID = 1L;

		private Function myFunction;
		private int[] myDimensions;

		public FunctionWrapper(Function function, int[] dimensions) {
			super(dimensions.length);
			myFunction = function;
			myDimensions = dimensions;
		}

		@Override
        public float map(float[] from) {
			assert from.length == myDimensions.length;
			float[] projection = new float[myFunction.getDimension()];

			for (int i = 0; i < from.length; i++) {
				projection[myDimensions[i]] = from[i];
			}

			return myFunction.map(projection);
		}

		@Override
		public Function clone() throws CloneNotSupportedException {
			final FunctionWrapper result = (FunctionWrapper) super.clone();
			result.myFunction = myFunction.clone();
			result.myDimensions = myDimensions.clone();
			return result;
		}

	}

	@Override
	public LinearApproximator clone() throws CloneNotSupportedException {
		CompositeApproximator result = (CompositeApproximator) super.clone();

		LinearApproximator[] components = new LinearApproximator[myComponents.length];
		for (int i = 0; i < components.length; i++) {
			components[i] = myComponents[i].clone();
		}
		result.myComponents = components;

		int[][] dimensions = new int[myDimensions.length][];
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i] = myDimensions[i].clone();
		}
		result.myDimensions = dimensions;

		return result;
	}

}
