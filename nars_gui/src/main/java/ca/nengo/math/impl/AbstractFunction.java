/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "AbstractFunction.java". Description:
"Base class for Function implementations"

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

import ca.nengo.math.Function;

/**
 * Base class for Function implementations. The default implementation of
 * multiMap() calls map(). This will be a little slower than if both methods
 * were to call a static function, so if multiMap speed is an issue this
 * method could be overridden, or it might be better not to use this abstract class.
 *
 * @author Bryan Tripp
 */
public abstract class AbstractFunction implements Function {

	private static final long serialVersionUID = 1L;

	/**
	 * How should we refer to the dimension?
	 */
	public static final String DIMENSION_PROPERTY = "dimension";

	private final int myDim;
	private String myCode;
	private String myName;

	/**
	 * @param dim Input dimension of the function
	 */
	public AbstractFunction(int dim) {
		myDim = dim;
		myCode = "";
		myName = "";
	}

	/**
	 * @see ca.nengo.math.Function#getDimension()
	 */
	public int getDimension() {
		return myDim;
	}
	
	public String getCode(){
		return myCode;
	}
	
	public void setCode(String code){
		myCode = code;
	}
	
	public String getName(){
		return myName;
	}
	
	public void setName(String name){
		myName = name;
	}
	/**
	 * @see ca.nengo.math.Function#map(float[])
	 */
	public abstract float map(float[] from);

	/**
	 * @see ca.nengo.math.Function#multiMap(float[][])
	 */
	public float[] multiMap(float[][] from) {
		float[] result = new float[from.length];

		for (int i = 0; i < from.length; i++) {
			result[i] = map(from[i]);
		}

		return result;
	}

	/**
	 * @throws CloneNotSupportedException is super does not support clone
	 */
	public Function clone() throws CloneNotSupportedException {
		return (Function) super.clone();
	}
	

}
