/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "TimeSeriesImpl.java". Description: 
"Default implementation of TimeSeriesND"

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
 * Created on May 4, 2006
 */
package ca.nengo.util.impl;

import ca.nengo.model.Units;
import ca.nengo.util.TimeSeries;

/**
 * Default implementation of TimeSeriesND. 
 * 
 * @author Bryan Tripp
 */
public class TimeSeriesImpl implements TimeSeries {

	private static final long serialVersionUID = 1L;
	
	private final float[] myTimes;
	private final float[][] myValues;
	private final Units[] myUnits;
	private final String[] myLabels;
	private String myName;
	
	/**
	 * @param times @see ca.bpt.cn.util.TimeSeries#getTimes()
	 * @param values @see ca.bpt.cn.util.TimeSeries#getValues()
	 * @param units @see ca.bpt.cn.util.TimeSeries#getUnits()
	 */	 
	public TimeSeriesImpl(float[] times, float[][] values, Units[] units) {
		this(times, values, units, getDefaultLabels(units.length));
	}
	
	/**
	 * @param times @see ca.nengo.util.TimeSeries#getTimes()
	 * @param values @see ca.nengo.util.TimeSeries#getValues()
	 * @param units @see ca.nengo.util.TimeSeries#getUnits()
	 * @param labels @see ca.nengo.util.TimeSeries#getLabels()
	 */	 
	public TimeSeriesImpl(float[] times, float[][] values, Units[] units, String[] labels) {		
		checkDimensions(times, values, units);
		
		myTimes = times;
		myValues = values;
		myUnits = units;
		myLabels = labels;		
	}
	
	private static String[] getDefaultLabels(int n) {
		String[] result = new String[n];
		for (int i = 0; i < n; i++) {
			result[i] = String.valueOf(i+1);
		}
		return result;
	}
	
	private void checkDimensions(float[] times, float[][] values, Units[] units) {
		if (times.length != values.length) {
			throw new IllegalArgumentException(times.length + " times were given with " + values.length + " values");
		}
		
		if (values.length > 0 && values[0].length != units.length) {
			throw new IllegalArgumentException("Values have dimension " + values[0].length
					+ " but there are " + units.length + " units");
		}
	}

	/**
	 * @see ca.nengo.util.TimeSeries#getName()
	 */
	public String getName() {
		return myName;
	}
	
	/**
	 * @param name Name of the TimeSeries
	 */
	public void setName(String name) {
		myName = name;
	}

	/**
	 * @see ca.nengo.util.TimeSeries1D#getTimes()
	 */
	public float[] getTimes() {
		return myTimes;		
	}
	
//	private void setTimes(float[] times) {
//		myTimes = times;
//	}

	/**
	 * @see ca.nengo.util.TimeSeries1D#getValues()
	 */
	public float[][] getValues() {
		return myValues;
	}
	
//	private void setValues(float[][] values) {
//		myValues = values;
//	}

	/**
	 * @see ca.nengo.util.TimeSeries1D#getUnits()
	 */
	public Units[] getUnits() {
		return myUnits;
	}
	
	/**
	 * @param index Index of dimension for which to change units 
	 * @param units New units for given dimension
	 */
	public void setUnits(int index, Units units) {
		myUnits[index] = units;
	}

	/**
	 * @see ca.nengo.util.TimeSeries#getDimension()
	 */
	public int getDimension() {
		return myUnits.length;
	}

	/**
	 * @see ca.nengo.util.TimeSeries#getLabels()
	 */
	public String[] getLabels() {
		return myLabels;
	}

	/**
	 * @param index Index of dimension for which to change label
	 * @param label New label for given dimension
	 */
	public void setLabel(int index, String label) {
		myLabels[index] = label;
	}

	@Override
	public TimeSeries clone() throws CloneNotSupportedException {
		Units[] units = new Units[myUnits.length];
        System.arraycopy(myUnits, 0, units, 0, units.length);
		String[] labels = new String[myLabels.length];
        System.arraycopy(myLabels, 0, labels, 0, labels.length);
		TimeSeries result = new TimeSeriesImpl(myTimes.clone(), myValues.clone(), units, labels);

		return result;
	}
	
	
	
}
