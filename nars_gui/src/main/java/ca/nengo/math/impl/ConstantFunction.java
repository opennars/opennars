/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ConstantFunction.java". Description: 
"A Function that maps everything to the same value"

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
 * Created on 6-Jun-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;

/**
 * A Function that maps everything to the same value. 
 * 
 * @author Bryan Tripp
 */
public class ConstantFunction implements Function {

	private static final long serialVersionUID = 1L;
	
	private int myDimension;
	private float myValue;
	
	/**
	 * @param dimension Input dimension of this Function
	 * @param value Constant output value of this Function 
	 */
	public ConstantFunction(int dimension, float value) {
		myDimension = dimension;
		myValue = value;
	}

	/**
	 * @param value The new constant result of the function
	 */
	public void setValue(float value) {
		myValue = value;
	}
	
	/**
	 * @param dimension New dimension
	 */
	public void setDimension(int dimension) {
		myDimension = dimension;
	}

	/**
	 * @see ca.nengo.math.Function#getDimension()
	 */
	public int getDimension() {
		return myDimension;
	}

	/**
	 * @return The constant value given in the constructor 
	 * 
	 * @see ca.nengo.math.Function#map(float[])
	 */
	public float map(float[] from) {
		return myValue;
	}

	/**
	 * @see ca.nengo.math.Function#multiMap(float[][])
	 */
	public float[] multiMap(float[][] from) {
		float[] result = new float[from.length];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = myValue;
		}
		
		return result;
	}

	/**
	 * @return Value of function
	 */
	public float getValue() {
		return myValue;
	}

	@Override
	public Function clone() throws CloneNotSupportedException {
		return (Function) super.clone();
	}
	
}
