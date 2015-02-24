/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NamedValueProperty.java". Description: 
"A property that can have multiple values, each of which is indexed by a String name"

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
 * Created on 17-Jan-08
 */
package ca.nengo.config;

import ca.nengo.model.StructuralException;

import java.util.List;

/**
 * A property that can have multiple values, each of which is indexed by a String name. 
 *  
 * @author Bryan Tripp
 */
public interface NamedValueProperty extends Property {

	/**
	 * @param name Name of a value of this property 
	 * @return The value
	 * @throws StructuralException if there is no value of the given name
	 */
	public Object getValue(String name) throws StructuralException;
	
	/**
	 * @return True if values are named automatically, in which case the setter 
	 * 		setValue(Object) can be used; otherwise value names must be provided 
	 * 		by the caller via setValue(String, Object)    
	 */
	public boolean isNamedAutomatically();

	/**
	 * Sets a value by name. 
	 * 
	 * @param name Name of the value
	 * @param value New value of the value
	 * @throws StructuralException if !isMutable
	 */
	public void setValue(String name, Object value) throws StructuralException;
	
	/**
	 * Sets an automatically-named value  
	 *  
	 * @param value New value of the value, from which the Property can automaticall
	 * 		determine the name 
	 * @throws StructuralException if !isNamedAutomatically() or !isMutable 
	 */
	public void setValue(Object value) throws StructuralException;
	
	/**
	 * Removes a value by name
	 * 
	 * @param name Name of value to remove
	 * @throws StructuralException if isFixedCardinality()
	 */
	public void removeValue(String name) throws StructuralException;
	
	/**
	 * @return Names of all values
	 */
	public List<String> getValueNames();
	
}
