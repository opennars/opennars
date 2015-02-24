/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "Convolution.java". Description:
"A numerical convolution of two one-dimensional functions.

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
 * Created on 12-May-07
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;

/**
 * A numerical convolution of two one-dimensional functions.
 *
 * TODO: unit tests
 *
 * @author Bryan Tripp
 */
public class Convolution extends AbstractFunction {

	private static final long serialVersionUID = 1L;

	private Function myOne;
	private Function myTwo;
	private float myStepSize;
	private float myWindow;

	/**
	 * @param one First of two functions to convolve together
	 * @param two Second of two functions to convolve together
	 * @param stepSize Step size at which to numerically evaluate convolution integral
	 * @param window Window over which to evaluate convolution integral
	 */
	public Convolution(Function one, Function two, float stepSize, float window) {
		super(1);
		setFunctionOne(one);
		setFunctionTwo(two);
		myStepSize = stepSize;
		myWindow = window;
	}

	/**
	 * @return First of two functions to convolve together
	 */
	public Function getFunctionOne() {
		return myOne;
	}

	/**
	 * @param function First of two functions to convolve together
	 */
	public void setFunctionOne(Function function) {
		checkDimension(function);
		myOne = function;
	}

	/**
	 * @return Second of two functions to convolve together
	 */
	public Function getFunctionTwo() {
		return myTwo;
	}

	/**
	 * @param function Second of two functions to convolve together
	 */
	public void setFunctionTwo(Function function) {
		checkDimension(function);
		myTwo = function;
	}

	/**
	 * @return Step size at which to numerically evaluate convolution integral
	 */
	public float getStepSize() {
		return myStepSize;
	}

	/**
	 * @param stepSize Step size at which to numerically evaluate convolution integral
	 */
	public void setStepSize(float stepSize) {
		myStepSize = stepSize;
	}

	/**
	 * @return Window over which to evaluate convolution integral
	 */
	public float getWindow() {
		return myWindow;
	}

	/**
	 * @param window Window over which to evaluate convolution integral
	 */
	public void setWindow(float window) {
		myWindow = window;
	}

	private static void checkDimension(Function function) {
		if (function.getDimension() != 1) {
			throw new IllegalArgumentException("Functions for convolution must be one-dimensional");
		}
	}

	/**
	 * @see ca.nengo.math.impl.AbstractFunction#map(float[])
	 */
	public float map(float[] from) {
		float result = 0;

		float time = from[0];
		float tau = 0;
		while (tau <= myWindow) {
			result += myOne.map(new float[]{time - tau}) * myTwo.map(new float[]{tau}) * myStepSize;
			tau += myStepSize;
		}

		return result;
	}

	@Override
	public Convolution clone() throws CloneNotSupportedException {
		final Convolution result = (Convolution) super.clone();
		result.setFunctionOne(myOne.clone());
		result.setFunctionTwo(myTwo.clone());
		result.myStepSize = myStepSize;
		result.myWindow = myWindow;
		return result;
	}
}
