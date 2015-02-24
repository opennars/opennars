/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NewtonRootFinder.java". Description: 
"Root finder that uses Newton's method"

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
 * Created on 20-Jul-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;
import ca.nengo.math.RootFinder;

/**
 * Root finder that uses Newton's method. Assumes that functions are generally increasing.  
 * 
 * TODO: test 
 * 
 * @author Bryan Tripp
 */
public class NewtonRootFinder implements RootFinder {

	private final int myMaxIterations;
	private final boolean myAdditiveBoundarySearch;
	
	/**
	 * @param maxIterations Maximum search iterations to attempt before returning an error
	 * @param additiveBoundarySearch If true, when low and high boundaries need to be widened, a proportion  
	 * 		of their difference is added/substracted. If false, they are multiplied/divided by a constant. 
	 * 		False is a good idea for boundaries that should not cross zero.
	 */
	public NewtonRootFinder(int maxIterations, boolean additiveBoundarySearch) {
		myMaxIterations = maxIterations;
		myAdditiveBoundarySearch = additiveBoundarySearch;
	}

	/**
	 * @see ca.nengo.math.RootFinder#findRoot(ca.nengo.math.Function, float, float, float)
	 */
	public float findRoot(Function function, float startLow, float startHigh, float tolerance) {
		if (startLow >= startHigh) {
			throw new IllegalArgumentException("Starting low value must be < high value");
		}
		if (function.getDimension() != 1) {
			throw new IllegalArgumentException("This root finder can only deal with 1-dimensional functions");
		}
		
		tolerance = Math.abs(tolerance);
		
		float low = startLow;
		float high = startHigh;		
		float fLow = function.map(new float[]{low});
		float fHigh = function.map(new float[]{high});

		int c = 0;
		while (fLow > 0) {
			if (++c > myMaxIterations) quit();
			
			if (myAdditiveBoundarySearch) {
				low = low - (startHigh - startLow) / 2f;				
			} else {
				low = .5f * low;
			}
			fLow = function.map(new float[]{low});					
		}

		c = 0;
		while (fHigh < 0) {
			if (++c > myMaxIterations) quit();
			
			if (myAdditiveBoundarySearch) {
				high = high + (startHigh - startLow) / 2f;				
			} else {
				high = 2f * high;
			}
			fHigh = function.map(new float[]{high});
		}

		c = 0;
		while (Math.abs(fLow) > tolerance && Math.abs(fHigh) > tolerance) {
			if (++c > myMaxIterations) quit();

			float middle = (low + high) / 2f;
			float fMiddle = function.map(new float[]{middle});
			
			if (fMiddle > 0) {
				high = middle;
				fHigh = fMiddle;
			} else {
				low = middle;
				fLow = fMiddle;
			}
		}
		
		return (Math.abs(fLow) < Math.abs(fHigh)) ? low : high;
	}
	
	private static void quit() throws RuntimeException {
		throw new RuntimeException("Maximum iterations exceeded while searching for function root");
	}

}
