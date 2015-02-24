/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "StatefulIndexFinder.java". Description: 
"An IndexFinder that searches linearly, starting where the last answer was"

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
 * Created on 2-Jun-2006
 */
package ca.nengo.util.impl;

import ca.nengo.util.IndexFinder;

/**
 * An IndexFinder that searches linearly, starting where the last answer was. This is 
 * a good choice if many interpolations will be made on the same series, and adjacent 
 * requests will be close to each other.  
 * 
 * TODO: test
 * 
 * @author Bryan Tripp
 */
public class StatefulIndexFinder implements IndexFinder {

	private float[] myValues;
	private int myIndex;
	
	/**
	 * @param values Must be monotonically increasing. 
	 */
	public StatefulIndexFinder(float[] values) {
		assert areMonotonicallyIncreasing(values);
		
		myValues = values;
		myIndex = 0;
	}

	public int findIndexBelow(float value) {
		
		if (myValues[myIndex] <= value) { //forward
			while (myIndex < (myValues.length-1) && myValues[++myIndex] <= value);
			myIndex--;
		} else {  //backward
			while (myIndex > 0 && myValues[--myIndex] > value);
		}
		
		return myIndex;
	}
	
	/**
	 * @param values A list of values  
	 * @return True if list values increases monotonically, false otherwise  
	 */
	public static boolean areMonotonicallyIncreasing(float[] values) {
		boolean result = true;

		for (int i = 1; i < values.length && result == true; i++) {
			if (values[i] < values[i-1]) {
				result = false;
			}
		}
		
		return result;
	}

	@Override
	public StatefulIndexFinder clone() throws CloneNotSupportedException {
		StatefulIndexFinder result = new StatefulIndexFinder(myValues.clone());
		result.myIndex = myIndex;
		return result;
	}

}
