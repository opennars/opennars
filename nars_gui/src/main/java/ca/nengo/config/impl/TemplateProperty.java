/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "TemplateProperty.java". Description: 
"A SingleValuedProperty that is not attached to getter/setter methods on an underlying class, but instead stores 
  its value internally"

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
import ca.nengo.config.SingleValuedProperty;
import ca.nengo.model.StructuralException;

/**
 * <p>A SingleValuedProperty that is not attached to getter/setter methods on an underlying class, but instead stores 
 * its value internally. It can be used to manage values of constructor/method arguments (rather than object properties).</p> 
 *      
 * @author Bryan Tripp
 */
public class TemplateProperty extends AbstractProperty implements SingleValuedProperty {

	private Object myValue;

	/**
	 * @param configuration Configuration to which this Property belongs
	 * @param name Name of the property 
	 * @param c Type of the property value
	 * @param defaultValue Default property value
	 */
	public TemplateProperty(Configuration configuration, String name, Class<?> c, Object defaultValue) {
		super(configuration, name, c, true);
		myValue = defaultValue;
	}

	/**
	 * @see ca.nengo.config.SingleValuedProperty#getValue()
	 */
	public Object getValue() {
		return myValue;
	}

	/**
	 * @see ca.nengo.config.Property#isFixedCardinality()
	 */
	public boolean isFixedCardinality() {
		return true;
	}

	/**
	 * @see ca.nengo.config.SingleValuedProperty#setValue(java.lang.Object)
	 */
	public void setValue(Object value) throws StructuralException {
		checkClass(value);
		myValue = value;
	}

	private void checkClass(Object value) throws StructuralException {
		if (!getType().isAssignableFrom(value.getClass())) {
			throw new StructuralException("Value must be of type " + getType() 
					+ " (was " + value.getClass().getName() + ')');
		}
	}
	
}