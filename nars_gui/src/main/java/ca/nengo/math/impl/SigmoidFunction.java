/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "SigmoidFunction.java". Description: 
"A one-dimensional sigmoid function with configurable high and low 
  values, slope, and inflection point.
  
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

package ca.nengo.math.impl;

import ca.nengo.math.DifferentiableFunction;
import ca.nengo.math.Function;

/**
 * A one-dimensional sigmoid function with configurable high and low 
 * values, slope, and inflection point.
 * 
 * TODO: unit tests
 * 
 * @author Bryan Tripp
 */
public class SigmoidFunction extends AbstractFunction implements DifferentiableFunction {

	private static final long serialVersionUID = 1L;

	private float myLow;
	private float myHigh;
	private float myInflection;
	private float myMultiplier;
	private Function myDerivative;	
	
	/**
	 * Default parameters (inflection=0; slope=1/4; low=0; high=1). 
	 */
	public SigmoidFunction() {
		this(0, 1f/4f, 0, 1);
	}

	/**
	 * @param inflection Inflection point
	 * @param slope Slope at inflection point (usually 1/4)
	 * @param low Result for inputs much lower than inflection point 
	 * @param high Result for inputs much higher than inflection point
	 */
	public SigmoidFunction(float inflection, float slope, float low, float high) {
		super(1);
		
		myLow = low;
		myHigh = high;
		myInflection = inflection;
		myMultiplier = slope * 4f; //usual slope is 1/4
		myDerivative = new SigmoidDerivative(myHigh-myLow, myInflection, myMultiplier);
	}
	
	/**
	 * @return Inflection point
	 */
	public float getInflection() {
		return myInflection;
	}
	
	/**
	 * @param inflection Inflection point
	 */
	public void setInflection(float inflection) {
		myInflection = inflection;
		myDerivative = new SigmoidDerivative(myHigh-myLow, myInflection, myMultiplier);
	}
	
	/**
	 * @return Slope at inflection point 
	 */
	public float getSlope() {
		return myMultiplier / 4f;
	}

	/**
	 * @param slope Slope at inflection point
	 */
	public void setSlope(float slope) {
		myMultiplier = 4f * slope;
		myDerivative = new SigmoidDerivative(myHigh-myLow, myInflection, myMultiplier);	
	}
	
	/**
	 * @return Result for inputs much lower than inflection point 
	 */
	public float getLow() {
		return myLow;
	}
	
	/**
	 * @param low Result for inputs much lower than inflection point 
	 */
	public void setLow(float low) {
		myLow = low;
		myDerivative = new SigmoidDerivative(myHigh-myLow, myInflection, myMultiplier);	
	}
	
	/**
	 * @return Result for inputs much higher than inflection point 
	 */
	public float getHigh() {
		return myHigh;
	}
	
	/**
	 * @param high Result for inputs much higher than inflection point 
	 */
	public void setHigh(float high) {
		myHigh = high;
		myDerivative = new SigmoidDerivative(myHigh-myLow, myInflection, myMultiplier);		
	}
	
	/**
	 * @see ca.nengo.math.DifferentiableFunction#getDerivative()
	 */
	public Function getDerivative() {
		return myDerivative;
	}

	/**
	 * @see ca.nengo.math.Function#map(float[])
	 */
	public float map(float[] from) {			
		return myLow + (myHigh-myLow) * ( 1f / (1f + (float) Math.exp(-myMultiplier*(from[0]-myInflection))) ) ;
	}
	
	@Override
	public Function clone() throws CloneNotSupportedException {
		SigmoidFunction result = (SigmoidFunction) super.clone();
		result.myDerivative = myDerivative.clone();
		return result;
	}

	/**
	 * Derivative of a sigmoid. 
	 * 
	 * Note: this has a read-only configuration because it is never created by the user or loaded from a file, 
	 * it's only created by SigmoidFunction 
	 * 
	 * @author Bryan Tripp
	 */
	private static class SigmoidDerivative extends AbstractFunction {

		private static final long serialVersionUID = 1L;
		
		private final float myScale;
		private final float myInflection;
		private final float myMultiplier;
		
		public SigmoidDerivative(float scale, float inflection, float multiplier) {
			super(1);
			myScale = scale;
			myInflection = inflection;
			myMultiplier = multiplier;
		}
		
		public float map(float[] from) {
			float sigmoidResult = 1f / (1f + (float) Math.exp(-myMultiplier*(from[0]-myInflection)));
			return myScale * myMultiplier * sigmoidResult * (1 - sigmoidResult);
		}
		
	}
	
//	public static void main(String[] args) {
//		//DifferentiableFunction f = new SigmoidFunction();
//		DifferentiableFunction f = new SigmoidFunction(1, 1f, 1, -1);
//		Plotter.plot(f, -6, .01f, 6, "sigmoid");
//		Plotter.plot(f.getDerivative(), -6, .01f, 6, "derivative");
//	}

}
