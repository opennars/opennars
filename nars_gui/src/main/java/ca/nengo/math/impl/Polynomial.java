/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "Polynomial.java". Description: 
"A one-dimensional polynomial Function"

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
 * Created on 1-Dec-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;

/**
 * A one-dimensional polynomial Function. It is defined by a series of coefficients that 
 * must be given in the constructor.    
 * 
 * @author Bryan Tripp
 */
public class Polynomial extends AbstractFunction implements Function {

	private static final long serialVersionUID = 1L;
	
	private float[] myCoefficients;
	
	/**
	 * @param coefficients Coefficients [a0 a1 a2 ...] in polynomial y = a0 + a1x + a2x^2 + ...
	 */
	public Polynomial(float[] coefficients) {
		super(1);
		myCoefficients = coefficients;
	}
	
	/**
	 * @return Polynomial order
	 */
	public int getOrder() {
		return myCoefficients.length;
	}

	/**
	 * @param order Polynomial order 
	 */
	public void setOrder(int order) {
		float[] newCoeff = new float[order];
		System.arraycopy(myCoefficients, 0, newCoeff, 0, Math.min(order, myCoefficients.length));
		myCoefficients = newCoeff;
	}

	/**
	 * @return Coefficients [a0 a1 a2 ...] in polynomial y = a0 + a1x + a2x^2 + ...
	 */
	public float[] getCoefficients() {
		return myCoefficients;
	}
	
	/**
	 * @param coefficients Coefficients [a0 a1 a2 ...] in polynomial y = a0 + a1x + a2x^2 + ...
	 */
	public void setCoefficients(float[] coefficients) {
		myCoefficients = new float[coefficients.length];
		System.arraycopy(coefficients, 0, myCoefficients, 0, coefficients.length);
	}

	/**
	 * @see ca.nengo.math.Function#map(float[])
	 */
	public float map(float[] from) {
		float result = myCoefficients[0];
		
		float xpowi = from[0];
		for (int i = 1; i < myCoefficients.length; i++) {
			result += myCoefficients[i] * xpowi; 
			xpowi = xpowi*from[0];
		}
		
		return result;
	}

	@Override
	public Function clone() throws CloneNotSupportedException {
		return new Polynomial(myCoefficients.clone());
	}

}
