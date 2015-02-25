/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "SpikeOutputImpl.java". Description: 
"Default implementation of SpikeOutput"

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
 * Created on 23-May-2006
 */
package ca.nengo.neural.impl;

import ca.nengo.neural.SpikeOutput;
import ca.nengo.model.Units;

/**
 * Default implementation of SpikeOutput. 
 *  
 * @author Bryan Tripp
 */
public class SpikeOutputImpl implements SpikeOutput {

	private static final long serialVersionUID = 1L;
	
	private final boolean[] myValues;
	private final Units myUnits;
	private final float myTime;

	/**
	 * @param values @see #getValues()
	 * @param units @see #getUnits()
	 * @param time @see #getTime()
	 */
	public SpikeOutputImpl(boolean[] values, Units units, float time) {
		myValues = values;
		myUnits = units;
		myTime = time;
	}

	/**
	 * @see ca.nengo.neural.SpikeOutput#getValues()
	 */
	public boolean[] getValues() {
		return myValues;
	}

	/**
	 * @see ca.nengo.model.InstantaneousOutput#getUnits()
	 */
	public Units getUnits() {
		return myUnits;
	}

	/**
	 * @see ca.nengo.model.InstantaneousOutput#getDimension()
	 */
	public int getDimension() {
		return myValues.length;
	}

	/**
	 * @see ca.nengo.model.InstantaneousOutput#getTime()
	 */
	public float getTime() {
		return myTime;
	}

	@Override
	public SpikeOutput clone() throws CloneNotSupportedException {
		return new SpikeOutputImpl(myValues.clone(), myUnits, myTime);
	}

}
