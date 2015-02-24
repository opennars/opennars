/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "TimeSeries.java". Description: 
"A series of vector values at ordered points in time"

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
package ca.nengo.util;

import ca.nengo.model.Units;

import java.io.Serializable;

/**
 * A series of vector values at ordered points in time. 
 * 
 * @author Bryan Tripp
 */
public interface TimeSeries extends Serializable, Cloneable {

	/**
	 * @return Name of the TimeSeries
	 */
	public String getName();

	/**
	 * @return Times for which values are available
	 */
	public float[] getTimes();
	
	/**
	 * @return dimension of vector values 
	 */
	public int getDimension();
	
	/**
	 * @return Values at getTimes(). Each value is a vector of size getDimension() 
	 */
	public float[][] getValues();
	
	/**
	 * @return Units in which values in each dimension are expressed (length 
	 * 		equals getDimension()) 
	 */	
	public Units[] getUnits();
	
	/**
	 * @return Name of each series (numbered by default)
	 */
	public String[] getLabels();
	
	public TimeSeries clone() throws CloneNotSupportedException;
		
}
