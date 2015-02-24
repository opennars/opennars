/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "PoissonPDF.java". Description: 
"A Poisson distribution"

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

/**
 * 
 */
package ca.nengo.math.impl;

import ca.nengo.math.PDF;
import ca.nengo.math.PDFTools;

/**
 * A Poisson distribution. 
 * 
 * @author Bryan Tripp
 */
public class PoissonPDF extends AbstractFunction implements PDF {

	private static final long serialVersionUID = 1L;

	private final float myRate;

	/**
	 * @param rate The mean & variance of the distribution 
	 */
	public PoissonPDF(float rate) {
		super(1);
		myRate = rate;
	}

	/**
	 * @see ca.nengo.math.impl.AbstractFunction#map(float[])
	 */
	@Override
	public float map(float[] from) {
		assert from.length == 0;
		
		float result = 0;
		int observation = (int) Math.floor(from[0]);
		
		result = (float) doMap((double) myRate, observation);
		
		return result;
	}

	/**
	 * @see ca.nengo.math.PDF#sample()
	 */
	public float[] sample() {
		double L = Math.exp(-myRate);
		float k = 0;
		double p = 1;

	    do {
	    	k = k + 1;
	    	double u = PDFTools.random();
	    	p = p * u;
	    } while (p >= L);
		
		return new float[]{k-1};
	}

	//this doesn't work ... overflows with large rate or observation; huge roundoff error with small rate & large observation 
//	private static double doMap(double rate, int observation) {
//		return Math.pow(rate, observation) * Math.exp(-rate) / factorial(observation);
//	}
	
//	private static long factorial(long n) {
//		assert n >= 0;
//		return (n == 0) ? 1 : n * factorial(n-1);
//	}

	private static double doMap(double rate, int observation) {
		double result = Math.exp(-rate);
		
		for (int i = 0; i < observation; i++) {
			result = result * rate / (i+1);
		}
		
		return result;
	}
		
	@Override
	public PDF clone() throws CloneNotSupportedException {
		return (PDF) super.clone();
	}
}
