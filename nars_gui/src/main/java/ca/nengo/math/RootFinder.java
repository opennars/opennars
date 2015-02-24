/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "RootFinder.java". Description: 
"Finds a root of a function"

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
package ca.nengo.math;

/**
 * Finds a root of a function. 
 * 
 * @author Bryan Tripp
 */
public interface RootFinder {

	/**
	 * @param function Function f(x) to find root of
	 * @param startLow Low-valued x from which to start search 
	 * @param startHigh High-valued x from which to start. You typically give startLow and startHigh so that
	 * 		you expect the signs of the functions at these values to be different.  
	 * @param tolerance Max acceptable |f(x)| for which to return x  
	 * @return x for which |f(x)| <= tolerance
	 */
	public float findRoot(Function function, float startLow, float startHigh, float tolerance);
	
//	/**
//	 * A function for which f(x)=0 for some x. 
//	 *  
//	 * @author Bryan Tripp
//	 */
//	public static interface Function {
//
//		/**
//		 * @param value Value at which to evaluate function
//		 * @return f(value)
//		 */
//		public float map(float value);		
//	}
}
