/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PiecewiseConstantFunction.java". Description:
"A one-dimensional function for which the output is constant between a finite number of
  discontinuities.

  TODO: unit test

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
 * Created on 31-Jan-2007
 */
package ca.nengo.math.impl;

import ca.nengo.config.PropretiesUtil;
import ca.nengo.config.Configuration;
import ca.nengo.config.impl.ConfigurationImpl;
import ca.nengo.config.impl.SingleValuedPropertyImpl;
import ca.nengo.math.Function;

import java.util.Arrays;

/**
 * A one-dimensional function for which the output is constant between a finite number of
 * discontinuities.
 *
 * TODO: unit test
 *
 * @author Bryan Tripp
 */
public class PiecewiseConstantFunction extends AbstractFunction {

	private static final long serialVersionUID = 1L;

	private float[] myDiscontinuities;
	private float[] myValues;

	/**
	 * @param discontinuities Ordered points x at which the function is y = f(x) is discontinuous
	 * @param values Values y below x1 and above x1..xn
	 */
	public PiecewiseConstantFunction(float[] discontinuities, float[] values) {
		this(discontinuities, values, 1);
	}
	
	/**
	 * A version of the constructor that allows you to specify the dimension (this doesn't do anything since
	 * this function makes no use of its input, but it allows these functions to be attached to multidimensional ensembles).
	 * 
	 * @param discontinuities Ordered points x at which the function is y = f(x) is discontinuous
	 * @param values Values y below x1 and above x1..xn
	 */
	public PiecewiseConstantFunction(float[] discontinuities, float[] values, int dimension) {
		super(dimension);
		
		myDiscontinuities = discontinuities;
		if ( discontinuities.length != (values.length - 1) ) {
			throw new IllegalArgumentException("There must be one more value than point of discontinuity");
		}

		myDiscontinuities = new float[discontinuities.length];
		System.arraycopy(discontinuities, 0, myDiscontinuities, 0, discontinuities.length);
		Arrays.sort(myDiscontinuities);

		setValues(values);
		
	}

	/**
	 * @return Custom configuration
	 */
	public Configuration getConfiguration() {
		ConfigurationImpl result = PropretiesUtil.defaultConfiguration(this);
		result.defineProperty(SingleValuedPropertyImpl.getSingleValuedProperty(result, "numDiscontinuities", Integer.TYPE));
		return result;
	}

	/**
	 * @return Number of discontinuities
	 */
	public int getNumDiscontinuities() {
		return myDiscontinuities.length;
	}

	/**
	 * @param num New number of discontinuities
	 */
	public void setNumDiscontinuities(int num) {
		float[] nd = new float[num];
		System.arraycopy(myDiscontinuities, 0, nd, 0, Math.min(num, myDiscontinuities.length));
		myDiscontinuities = nd;

		float[] nv = new float[num+1];
		System.arraycopy(myValues, 0, nv, 0, Math.min(num+1, myValues.length));
		myValues = nv;
	}

	/**
	 * @return Ordered points x at which the function is y = f(x) is discontinuous
	 */
	public float[] getDiscontinuities() {
		return myDiscontinuities;
	}

	/**
	 * @param discontinuities Ordered points x at which the function is y = f(x) is discontinuous
	 */
	public void setDiscontinuities(float[] discontinuities) {
		if (discontinuities.length != myDiscontinuities.length) {
			throw new IllegalArgumentException(
					"Number of discontinuities must be consistent with number of values (use setNumDiscontinuities() to change).");
		}
		myDiscontinuities = new float[discontinuities.length];
		System.arraycopy(discontinuities, 0, myDiscontinuities, 0, discontinuities.length);
		Arrays.sort(myDiscontinuities);
	}

	/**
	 * @return Values y below x1 and above x1..xn
	 */
	public float[] getValues() {
		return myValues;
	}

	/**
	 * @param values Values y below x1 and above x1..xn
	 */
	public void setValues(float[] values) {
		if (values.length - 1 != myDiscontinuities.length) {
			throw new IllegalArgumentException(
				"Number of discontinuities must be consistent with number of values (use setNumDiscontinuities() to change).");
		}
		myValues = new float[values.length];
		System.arraycopy(values, 0, myValues, 0, values.length);
	}

	/**
	 * @see ca.nengo.math.Function#map(float[])
	 */
	public float map(float[] from) {
		float y = 0;
		float x = from[0];

		if (myDiscontinuities.length == 0 || x <= myDiscontinuities[0]) {
			y = myValues[0];

		} else if (x >= myDiscontinuities[myDiscontinuities.length-1]) {
			y = myValues[myValues.length-1];

		} else {

			int low = 0;
			int high = myDiscontinuities.length;

			while (high-low > 1) {
				int middle = Math.round((low + high) / 2f);
				float xMiddle = myDiscontinuities[middle];

				if (xMiddle > x) {
					high = middle;
				} else {
					low = middle;
				}
			}

			y = myValues[high];
		}

		return y;
	}

	@Override
	public Function clone() throws CloneNotSupportedException {
		return new PiecewiseConstantFunction(myDiscontinuities.clone(), myValues.clone());
	}
}
