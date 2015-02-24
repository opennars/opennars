/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "LinearFunction.java". Description: 
"A linear map into one dimension"

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
import ca.nengo.util.MU;

/**
 * A linear map into one dimension. Optionally, the result can be biased and/or 
 * rectified. 
 *  
 * @author Bryan Tripp
 */
public class LinearFunction extends AbstractFunction {

	private static final long serialVersionUID = 1L;
	
	private float[] myMap;
	private float myBias;
	private boolean myRectified;

	/**
	 * @param map A 1Xn matrix that defines a map from input onto one dimension
	 * 		(i.e. f(x) = m'x, where m is the map)
	 * @param bias Bias to add to result
	 * @param rectified If true, result is rectified (set to 0 if less than 0) 
	 */
	public LinearFunction(float[] map, float bias, boolean rectified) {
		super(map.length);
		myMap = map;
		myBias = bias;
		myRectified = rectified;
	}
	
	/**
	 * @return map A 1Xn matrix that defines a map from input onto one dimension
	 * 		(i.e. f(x) = m'x, where m is the map)
	 */
	public float[] getMap() {
		return myMap;
	}
	
	/**
	 * @param map map A 1Xn matrix that defines a map from input onto one dimension
	 * 		(i.e. f(x) = m'x, where m is the map)
	 */
	public void setMap(float[] map) {
		myMap = map;
	}
	
	/**
	 * @return Bias to add to result
	 */
	public float getBias() {
		return myBias;
	}
	
	/**
	 * @param bias Bias to add to result
	 */
	public void setBias(float bias) {
		myBias = bias;
	}
	
	/**
	 * @return If true, result is rectified (set to 0 if less than 0)
	 */
	public boolean getRectified() {
		return myRectified;
	}
	
	/**
	 * @param rectified If true, result is rectified (set to 0 if less than 0)
	 */
	public void setRectified(boolean rectified) {
		myRectified = rectified;
	}

	@Override
	public float map(float[] from) {
		float result = MU.prod(from, myMap) + myBias;
		return (myRectified && result < 0) ? 0 : result;
	}

	@Override
	public Function clone() throws CloneNotSupportedException {
		LinearFunction result = (LinearFunction) super.clone();
		result.setMap(this.getMap().clone());
		return result;
	}

}
