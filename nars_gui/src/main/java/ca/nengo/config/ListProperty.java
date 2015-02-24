/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ListProperty.java". Description: 
"A Property that can have multiple values, each of which is identified by an integer index"

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
 * Created on 15-Jan-08
 */
package ca.nengo.config;

import ca.nengo.model.StructuralException;

/**
 * A Property that can have multiple values, each of which is identified by an integer index. 
 * 
 * @author Bryan Tripp
 */
public interface ListProperty extends Property {

	/**
	 * @param index Index of a certain single value of a multi-valued property 
	 * @return The value at the given index
	 * @throws StructuralException if the given index is out of range
	 */
	public Object getValue(int index) throws StructuralException;
	
	/**
	 * @param index Index of a certain single value of a multi-valued property 
	 * @param value New value to replace that at the given index 
	 * @throws StructuralException if the value is invalid (as in setValue) or the given index is 
	 * 		out of range or the Property is immutable
	 */
	public void setValue(int index, Object value) throws StructuralException;

	/**
	 * @param value New value to be added to the end of the list 
	 * @throws StructuralException if the value is invalid (as in setValue) or the Property is 
	 * 		immutable or fixed-cardinality 
	 */
	public void addValue(Object value) throws StructuralException;
	
	/**
	 * @return Number of repeated values of this Property
	 */
	public int getNumValues();
	
	/**
	 * @param index Index at which new value is to be inserted  
	 * @param value New value
	 * @throws StructuralException if the value is invalid (as in setValue) or the Property is 
	 * 		immutable or fixed-cardinality or the index is out of range 
	 */
	public void insert(int index, Object value) throws StructuralException;
	
	/**
	 * @param index Index of a single value of a multi-valued property that is to be removed
	 * @throws StructuralException if the given index is out of range or the Property is immutable or fixed cardinality
	 */
	public void remove(int index) throws StructuralException;
	
	/**
	 * @return Default value for insertions
	 * TODO: remove; use default from NewConfigurableDialog (move to ConfigUtil)
	 */
	public Object getDefaultValue(); 

}
