/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "IndicatorPDF.java". Description: 
"Uniform probability between upper and lower limits, zero elsewhere.
  
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
 * Created on 13-Jun-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.PDF;
import ca.nengo.math.PDFTools;

/**
 * Uniform probability between upper and lower limits, zero elsewhere.
 * 
 * @author Bryan Tripp
 */
public class IndicatorPDF implements PDF {

	private static final long serialVersionUID = 1L;
	
	private float myLow;
	private float myHigh;
	private float myDifference;
	private float myVal;
	
	/**
	 * @param low Lower limit of range of possible values
	 * @param high Upper limit of range of possible values
	 */
	public IndicatorPDF(float low, float high) {
		set(low, high);
	}
	
	/**
	 * @param exact A value at which the PDF is infinity (zero at other values) 
	 */
	public IndicatorPDF(float exact) {
		set(exact, exact);
	}
	
	private void set(float low, float high) {
		if (high < low) {
			throw new IllegalArgumentException("High value must be greater than or equal to low value");
		}
		
		myLow = low;
		myHigh = high;
		myDifference = high - low;
		if (high == low) {
			myVal = Float.POSITIVE_INFINITY;
		} else {
			myVal = 1f / myDifference;   					
		}
	}
	
	/**
	 * @param low Lower limit of range of possible values
	 */
	public void setLow(float low) {
		set(low, myHigh);					
	}

	/**
	 * @param high Upper limit of range of possible values
	 */
	public void setHigh(float high) {
		set(myLow, high);			
	}
	
	/**
	 * @return Lower limit of range of possible values
	 */
	public float getLow() {
		return myLow;
	}

	/**
	 * @return Upper limit of range of possible values
	 */
	public float getHigh() {
		return myHigh;
	}
	
	/**
	 * @return Probability density between low and high limits
	 */
	public float getDensity() {
		return myVal;
	}

	/**
	 * @see ca.nengo.math.PDF#sample()
	 */
	public float[] sample() {
		if (myLow == myHigh) {
			return new float[]{myLow};
		} else {
			return new float[] {myLow + myDifference * (float) PDFTools.random()};			
		}
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
		return doMap(myLow, myHigh, myVal, from[0]);
	}

	/**
	 * @see ca.nengo.math.Function#multiMap(float[][])
	 */
	public float[] multiMap(float[][] from) {
		float[] result = new float[from.length];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = doMap(myLow, myHigh, myVal, from[i][0]);
		}
		
		return result;
	}
	
	private static float doMap(float low, float high, float val, float from) {
		return (from >= low && from <= high) ? val : 0f;
	}

	@Override
	public PDF clone() throws CloneNotSupportedException {
		return (PDF) super.clone();
	}
	
}
