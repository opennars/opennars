/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "IdentityFunction.java". Description: 
"Identity function on a particular dimension of input, ie f(x) = x_i, 
  where i is a constant.
  
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
 * Created on 14-Jun-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;

/**
 * Identity function on a particular dimension of input, ie f(x) = x_i, 
 * where i is a constant.
 * 
 * @author Bryan Tripp
 */
public class IdentityFunction implements Function {

	private static final long serialVersionUID = 1L;
	
	private int myDimension;
	private int myIdentityDimension;
	
	/**
	 * @param dimension Dimension of input vector  
	 * @param i Index (from 0) of input vector of which this function is 
	 * 		an identity 
	 */
	public IdentityFunction(int dimension, int i) {
		setDimension(dimension);
		setIdentityDimension(i);
	}
	
	/**
	 * Defaults to one dimension. 
	 */
	public IdentityFunction() {
		this(1, 0);
	}
	/**
	 * @see ca.nengo.math.Function#getDimension()
	 */
	public int getDimension() {
		return myDimension;
	}
	
	/**
	 * @param dimension New dimension of expected input vectors
	 */
	public void setDimension(int dimension) {
		if (dimension <= 0) {
			throw new IllegalArgumentException("Dimension must be a +ve integer");
		}
		if (dimension <= myIdentityDimension) {
			throw new IllegalArgumentException("Can't have dimension " + dimension 
					+ " with index of identity dimension set to " + myIdentityDimension);
		}
		myDimension = dimension;
	}
	
	/**
	 * @return Index on input vector of which this funciton is an identity
	 */
	public int getIdentityDimension() {
		return myIdentityDimension;
	}
	
	/**
	 * @param i Index (from 0) of input vector of which this function is 
	 * 		an identity 
	 */
	public void setIdentityDimension(int i) {
		if (i >= myDimension || i < 0) {
			throw new IllegalArgumentException("Index " + i + " is out of range");
		}
		myIdentityDimension = i;
	}

	/**
	 * @see ca.nengo.math.Function#map(float[])
	 */
	public float map(float[] from) {
		return from[myIdentityDimension];
	}

	/**
	 * @see ca.nengo.math.Function#multiMap(float[][])
	 */
	public float[] multiMap(float[][] from) {
		float[] result = new float[from.length];
		
		for (int i = 0; i < from.length; i++) {
			result[i] = from[i][myIdentityDimension];
		}
		
		return result;
	}
	
	@Override
	public Function clone() throws CloneNotSupportedException {
		return (Function) super.clone();
	}
	
}
