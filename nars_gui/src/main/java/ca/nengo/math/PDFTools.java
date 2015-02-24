/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "PDFTools.java". Description: 
"Convenience methods for using PDFs"

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
 * Created on May 16, 2006
 */
package ca.nengo.math;

import java.util.Random;

/**
 * Convenience methods for using PDFs. 
 * 
 * @author Bryan Tripp
 */
public class PDFTools {
	
	private static final Random ourRandom = new Random();

	/**
	 * Note: PDF treated as univariate (only first dimension considered). 
	 * 
	 * @param pdf The PDF from which to sample
	 * @return Sample from PDF rounded to nearest integer 
	 */
	public static int sampleInt(PDF pdf) {
		return Math.round(pdf.sample()[0]);
	}

	/**
	 * Note: PDF treated as univariate (only first dimension considered).
	 *  
	 * @param pdf The PDF from which to sample
	 * @return True iff sample from PDF is > 1
	 */
	public static boolean sampleBoolean(PDF pdf) {
		return pdf.sample()[0] > 1;
	}
	
	/**
	 * Note: PDF treated as univariate (only first dimension considered).
	 *  
	 * @param pdf The PDF from which to sample
	 * @return Sample from PDF (this is a convenience method for getting 1st 
	 * 		dimension of sample() result)
	 */
	public static float sampleFloat(PDF pdf) {
		return pdf.sample()[0];
	}
	
	/**
	 * Use this rather than Math.random(), to allow user to reproduce random results
	 * by setting the seed. 
	 * 
	 * @return A random sample between 0 and 1
	 */
	public static double random() {
		return ourRandom.nextDouble();
	}
	
	/**
	 * @param seed New random seed for random()
	 */
	public static void setSeed(long seed) {
		ourRandom.setSeed(seed);
	}
	
}
