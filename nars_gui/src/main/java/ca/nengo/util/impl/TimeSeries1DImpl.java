/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "TimeSeries1DImpl.java". Description: 
"Default implementation of TimeSeries"

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

import ca.nengo.config.PropretiesUtil;
import ca.nengo.config.Configuration;
import ca.nengo.config.SingleValuedProperty;
import ca.nengo.config.impl.ConfigurationImpl;
import ca.nengo.config.impl.SingleValuedPropertyImpl;
import ca.nengo.model.Units;
import ca.nengo.util.TimeSeries1D;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Default implementation of TimeSeries.  
 * 
 * @author Bryan Tripp
 */
public class TimeSeries1DImpl implements TimeSeries1D, Serializable {

	private static final long serialVersionUID = 1L;
	
	private float[] myTimes;
	private float[] myValues;
	private Units myUnits;
	private String myLabel;
	private String myName;
	
	/**
	 * @param times @see ca.bpt.cn.util.TimeSeries#getTimes()
	 * @param values @see ca.bpt.cn.util.TimeSeries#getValues()
	 * @param units @see ca.bpt.cn.util.TimeSeries#getUnits()
	 */	 
	public TimeSeries1DImpl(float[] times, float[] values, Units units) {
		if (times.length != values.length) {
			throw new IllegalArgumentException(times.length + " times were given with " + values.length + " values");
		}
		
		this.myTimes = times;
		this.myValues = values;
		this.myUnits = units;
		this.myLabel = "1";		
	}
	
	/**
	 * @return Custom Configuration (to more cleanly handle properties in 1D) 
	 */
	public Configuration getConfiguration() {
		ConfigurationImpl result = PropretiesUtil.defaultConfiguration(this);
		result.removeProperty("units");
		result.removeProperty("units1D");
		result.removeProperty("values");
		result.removeProperty("values1D");
		result.removeProperty("labels");
		
		try {
			Method unitsGetter = this.getClass().getMethod("getUnits1D");
			Method unitsSetter = this.getClass().getMethod("setUnits", Units.class);
			result.defineProperty(new SingleValuedPropertyImpl(result, "units", Units.class, unitsGetter, unitsSetter));
			
			Method valuesGetter = this.getClass().getMethod("getValues1D");
			result.defineProperty(new SingleValuedPropertyImpl(result, "values", float[].class, valuesGetter));

			final Method labelGetter = this.getClass().getMethod("getLabels");
			Method labelSetter = this.getClass().getMethod("setLabel", String.class);
			SingleValuedProperty labelProp = new SingleValuedPropertyImpl(result, "label", String.class, labelGetter, labelSetter) {

				@Override
				public Object getValue() {
					Object result = null;
					try {
						Object configurable = getConfiguration().getConfigurable();
						String[] labels = (String[]) labelGetter.invoke(configurable);
						result = labels[0];
					} catch (Exception e) {
						throw new RuntimeException("Can't get label value", e);
					}
					return result;
				}
				
			};
			result.defineProperty(labelProp);
		} catch (SecurityException e) {
			throw new RuntimeException("Can't access getter/setter -- this is a bug", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Can't access getter/setter -- this is a bug", e);
		}
		return result;
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
	 * @see ca.nengo.util.TimeSeries1D#getValues1D()
	 */
	public float[] getValues1D() {
		return myValues;
	}

	/**
	 * @see ca.nengo.util.TimeSeries1D#getUnits1D()
	 */
	public Units getUnits1D() {
		return myUnits;
	}

	/**
	 * @see ca.nengo.util.TimeSeries#getDimension()
	 */
	public int getDimension() {
		return 1;
	}

	/**
	 * @see ca.nengo.util.TimeSeries#getValues()
	 */
	public float[][] getValues() {
		float[][] result = new float[myValues.length][];
		
		for (int i = 0; i < myValues.length; i++) {
			result[i] = new float[]{myValues[i]};
		}
		
		return result;
	}
	
//	private void setValues(float[] values) {
//		myValues = values;
//	}

	/**
	 * @see ca.nengo.util.TimeSeries#getUnits()
	 */
	public Units[] getUnits() {
		return new Units[]{myUnits};
	}
	
	/**
	 * @param units New Units
	 */
	public void setUnits(Units units) {
		myUnits = units;
	}
	
	/**
	 * @see ca.nengo.util.TimeSeries#getLabels()
	 */
	public String[] getLabels() {
		return new String[]{myLabel};
	}
	
	/**
	 * @param label New label
	 */
	public void setLabel(String label) {
		myLabel = label;
	}

	@Override
	public TimeSeries1D clone() throws CloneNotSupportedException {
		return (TimeSeries1D) super.clone();
	}
	
}
