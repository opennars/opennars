/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ConfigurationImpl.java". Description:
"Default implementation of Configuration"

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
 * Created on 3-Dec-07
 */
package ca.nengo.config.impl;

import ca.nengo.config.Configuration;
import ca.nengo.config.Property;
import ca.nengo.model.StructuralException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Default implementation of Configuration. This implementation reports property names in
 * the order they are defined.</p>
 *
 * @author Bryan Tripp
 */
public class ConfigurationImpl implements Configuration {

	private final Object myConfigurable;
	private final List<String> myPropertyNames;
	private final Map<String, Property> myProperties;

	/**
	 * @param configurable The Object to which this Configuration belongs
	 */
	public ConfigurationImpl(Object configurable) {
		myConfigurable = configurable;
		myPropertyNames = new ArrayList<String>(20);
		myProperties = new HashMap<String, Property>(20);
	}

	/**
	 * @see ca.nengo.config.Configuration#getConfigurable()
	 */
	public Object getConfigurable() {
		return myConfigurable;
	}

	/**
	 * To be called by the associated Configurable, immediately after construction (once
	 * per property).
	 *
	 * @param property The new Property
	 */
	public void defineProperty(Property property) {
		String name = property.getName();
		myProperties.put(name, property);
		if (myPropertyNames.contains(name)) {
            myPropertyNames.remove(name);
        }
		myPropertyNames.add(name);
	}

	/**
	 * @param name Property to remove
	 */
	public void removeProperty(String name) {
		myProperties.remove(name);
		myPropertyNames.remove(name);
	}

	/**
	 * @param oldName The existing name of the Property
	 * @param newName The replacement name of the Property
	 */
	public void renameProperty(String oldName, String newName) {
		if (myPropertyNames.contains(oldName)) {
			int index = myPropertyNames.indexOf(oldName);
			myPropertyNames.remove(index);
			myPropertyNames.add(index, newName);
			Property p = myProperties.remove(oldName);
			p.setName(newName);
			myProperties.put(newName, p);
		} else {
			throw new IllegalArgumentException("There is no Property named " + oldName);
		}
	}

	/**
	 * @param name Property to be defined
	 * @param c Class on which the property is defined
	 * @param mutable Mutable?
	 * @return SingleValuedPropertyImpl
	 */
	public SingleValuedPropertyImpl defineSingleValuedProperty(String name, Class<?> c, boolean mutable) {
		SingleValuedPropertyImpl property
			= (SingleValuedPropertyImpl) SingleValuedPropertyImpl.getSingleValuedProperty(this, name, c);
		defineProperty(property);
		return property;
	}

	/**
	 * @param name Property to be defined
	 * @param c Class on which the property is defined
	 * @param defaultValue Default object
	 * @return TemplateProperty
	 */
	public TemplateProperty defineTemplateProperty(String name, Class<?> c, Object defaultValue) {
		TemplateProperty property = new TemplateProperty(this, name, c, defaultValue);
		defineProperty(property);
		return property;
	}

	/**
	 * @see ca.nengo.config.Configuration#getPropertyNames()
	 */
	public List<String> getPropertyNames() {
		return new ArrayList<String>(myPropertyNames);
	}

	/**
	 * @see ca.nengo.config.Configuration#getProperty(java.lang.String)
	 */
	public Property getProperty(String name) throws StructuralException {
		if (myProperties.containsKey(name)) {
			return myProperties.get(name);
		} else {
			throw new StructuralException("The property " + name + " is unknown");
		}
	}

}
