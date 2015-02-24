/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "SineFunction.java". Description: 
"Function wrapper for sin(omega x), where x is in radians and omega is the angular frequency"

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
 * Created on 8-Jun-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;

/**
 * Function wrapper for sin(omega x), where x is in radians and omega is the angular frequency.  
 * 
 * TODO: test
 *  
 * @author Bryan Tripp
 */
public class SineFunction implements Function {

	private static final long serialVersionUID = 1L;
	
	private float myOmega;
	private float myAmplitude;

	/**
	 * Uses default angular frequency of 2pi and amplitude of 1
	 */
	public SineFunction() {
		this(2 * (float) Math.PI);
	}
	
	/**
	 * Uses default amplitude of 1. 
	 * 
	 * @param omega Angular frequency
	 */
	public SineFunction(float omega) {
		this(omega, 1);
	}
	
	/**
	 * @param omega Angular frequency
	 * @param amplitude Amplitude (peak value)
	 */
	public SineFunction(float omega, float amplitude) {
		myOmega = omega;
		myAmplitude = amplitude;
	}

	/**
	 * @return Angular frequency
	 */
	public float getOmega() {
		return myOmega;
	}
	
	/**
	 * @param omega Angular frequency
	 */
	public void setOmega(float omega) {
		myOmega = omega;
	}
	
	/**
	 * @return Amplitude (peak value)
	 */
	public float getAmplitude() {
		return myAmplitude;
	}
	
	/**
	 * @param amplitude Amplitude (peak value)
	 */
	public void setAmplitude(float amplitude) {
		myAmplitude = amplitude;
	}

	/**
	 * @return 1
	 * @see ca.nengo.math.Function#getDimension()
	 */
	public int getDimension() {
		return 1;
	}

	/**
	 * @see ca.nengo.math.Function#map(float[])
	 */
	public float map(float[] from) {
		return doMap(from, myOmega, myAmplitude);
	}

	/**
	 * @see ca.nengo.math.Function#multiMap(float[][])
	 */
	public float[] multiMap(float[][] from) {
		float[] result = new float[from.length];
		
		for (int i = 0; i < from.length; i++) {
			result[i] = doMap(from[i], myOmega, myAmplitude);
		}
		
		return result;
	}
	
	private static float doMap(float[] from, float omega, float amplitude) {
		return amplitude * (float) Math.sin(from[0] * omega);
	}

	@Override
	public Function clone() throws CloneNotSupportedException {
		return (Function) super.clone();
	}
}
