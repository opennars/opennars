/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ExponentialPDF.java". Description:
"A one-dimensional exponential probability density function.

  TODO: unit tests
  TODO: generalize to any function with invertible integral (see numerical recipies in C chapter 7)

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
 * Created on 19-May-07
 */
package ca.nengo.math.impl;

import ca.nengo.math.PDF;
import ca.nengo.math.PDFTools;

/**
 * A one-dimensional exponential probability density function.
 *
 * TODO: unit tests
 * TODO: generalize to any function with invertible integral (see numerical recipes in C chapter 7)
 *
 * @author Bryan Tripp
 */
public class ExponentialPDF extends AbstractFunction implements PDF {

	private static final long serialVersionUID = 1L;

	private float myTau;

	/**
	 * @param tau Rate parameter of exponential distribution
	 */
	public ExponentialPDF(float tau) {
		super(1);
		myTau = tau;
	}

	/**
	 * @return Rate parameter of exponential distribution
	 */
	public float getTau() {
		return myTau;
	}

	/**
	 * @param tau Rate parameter of exponential distribution
	 */
	public void setTau(float tau) {
		myTau = tau;
	}

	/**
	 * @see ca.nengo.math.impl.AbstractFunction#map(float[])
	 */
	public float map(float[] from) {
		return from[0] >= 0 ? (1f/myTau) * (float) Math.exp(-from[0]/myTau) : 0;
	}

	/**
	 * @see ca.nengo.math.PDF#sample()
	 */
	public float[] sample() {
		double x = 0;
		do {
			x = PDFTools.random();
		} while (x == 0);

		return new float[]{myTau * (float) -Math.log(x)};
	}

	@Override
	public PDF clone() throws CloneNotSupportedException {
		return (PDF) super.clone();
	}
}
