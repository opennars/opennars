/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "TemplateArrayProperty.java". Description: 
"A ListProperty that is not attached to getter/setter methods on an underlying class, but instead stores 
  its values internally"

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
package ca.nengo.config.impl;

import ca.nengo.config.Configuration;
import ca.nengo.config.ListProperty;
import ca.nengo.model.StructuralException;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A ListProperty that is not attached to getter/setter methods on an underlying class, but instead stores 
 * its values internally. It can be used to manage array or list values of constructor/method arguments 
 * (rather than multi-valued object properties). Similar to TemplateProperty but multivalued.</p>
 * 
 * @author Bryan Tripp
 */
public class TemplateArrayProperty extends AbstractProperty implements ListProperty {

	private final List<Object> myValues;
	
	/**
	 * @param configuration Configuration to which this Property belongs
	 * @param name Name of the property 
	 * @param c Type of the property value
	 */
	public TemplateArrayProperty(Configuration configuration, String name, Class<?> c) {
		super(configuration, name, c, true);
		myValues = new ArrayList<Object>(10);
	}

	/**
	 * @see ca.nengo.config.ListProperty#addValue(java.lang.Object)
	 */
	public void addValue(Object value) throws StructuralException {
		checkClass(value);
		myValues.add(value);
	}

	/**
	 * @see ca.nengo.config.ListProperty#getNumValues()
	 */
	public int getNumValues() {
		return myValues.size();
	}

	/**
	 * @see ca.nengo.config.ListProperty#getValue(int)
	 */
	public Object getValue(int index) throws StructuralException {
		try {
			return myValues.get(index);
		} catch (IndexOutOfBoundsException e) {
			throw new StructuralException("Value " + index + " doesn't exist", e);
		}
	}

	/**
	 * @see ca.nengo.config.ListProperty#insert(int, java.lang.Object)
	 */
	public void insert(int index, Object value) throws StructuralException {
		checkClass(value);
		try {
			myValues.add(value);					
		} catch (IndexOutOfBoundsException e) {
			throw new StructuralException("Value " + index + " doesn't exist", e);
		}
	}

	/**
	 * @see ca.nengo.config.Property#isFixedCardinality()
	 */
	public boolean isFixedCardinality() {
		return false;
	}

	/**
	 * @see ca.nengo.config.ListProperty#remove(int)
	 */
	public void remove(int index) throws StructuralException {
		try {
			myValues.remove(index);					
		} catch (IndexOutOfBoundsException e) {
			throw new StructuralException("Value " + index + " doesn't exist", e);
		}
	}

	/**
	 * @see ca.nengo.config.ListProperty#setValue(int, java.lang.Object)
	 */
	public void setValue(int index, Object value) throws StructuralException {
		checkClass(value);
		try {
			myValues.set(index, value);					
		} catch (IndexOutOfBoundsException e) {
			throw new StructuralException("Value " + index + " doesn't exist", e);
		}
	}
	
	private void checkClass(Object value) throws StructuralException {
		if (!getType().isAssignableFrom(value.getClass())) {
			throw new StructuralException("Value must be of type " + getType() 
					+ " (was " + value.getClass().getName() + ')');
		}
	}

	/**
	 * @see ca.nengo.config.ListProperty#getDefaultValue()
	 */
	public Object getDefaultValue() {
		return (myValues.size() > 0) ? myValues.get(0) : null;
	}		
	
}