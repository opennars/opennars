/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "SingleValuedPropertyImpl.java". Description: 
"Default implementation of single-valued Properties.
  
  @author Bryan Tripp"

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

package ca.nengo.config.impl;

import ca.nengo.config.Configuration;
import ca.nengo.config.SingleValuedProperty;
import ca.nengo.model.StructuralException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Default implementation of single-valued Properties.
 * 
 * @author Bryan Tripp
 */
public class SingleValuedPropertyImpl extends AbstractProperty implements SingleValuedProperty {
	
	private final Method myGetter;
	private Method mySetter;

	/**
	 * @param configuration Configuration to which this Property belongs
	 * @param name Parameter name
	 * @param type Parameter type
	 * @return Property or null if the necessary methods don't exist on the underlying class  
	 */
	public static SingleValuedProperty getSingleValuedProperty(Configuration configuration, String name, Class<?> type) {
		SingleValuedPropertyImpl result = null;
		Class<?> targetClass = configuration.getConfigurable().getClass();
		
		String uname = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		String[] getterNames = new String[]{"get"+uname};
		String[] setterNames = new String[]{"set"+uname}; 

		Method getter = ListPropertyImpl.getMethod(targetClass, getterNames, new Class[0], type);
		Method setter = ListPropertyImpl.getMethod(targetClass, setterNames, new Class[]{type}, null);

		if (getter != null && setter != null) {
			result = new SingleValuedPropertyImpl(configuration, name, type, getter, setter);
		} else if (getter != null) {
			result = new SingleValuedPropertyImpl(configuration, name, type, getter);
		}
		
		return result;
	}
	
	/**
	 * Constructor for immutable single-valued properties. 
	 * 
	 * @param configuration Configuration to which this Property belongs
	 * @param name Parameter name
	 * @param c Parameter type
	 * @param getter Zero-arg getter method
	 */
	public SingleValuedPropertyImpl(Configuration configuration, String name, Class<?> c, Method getter) {
		super(configuration, name, c, false);
		myGetter = getter;
	}

	/**
	 * Constructor for mutable single-valued properties. 
	 * 
	 * @param configuration Configuration to which this Property belongs
	 * @param name Parameter name
	 * @param c Parameter type
	 * @param getter Zero-arg getter method
	 * @param setter Single-arg setter method
	 */
	public SingleValuedPropertyImpl(Configuration configuration, String name, Class<?> c, Method getter, Method setter) {
		super(configuration, name, c, true);
		myGetter = getter;
		mySetter = setter;
	}

	/**
	 * @see ca.nengo.config.SingleValuedProperty#getValue()
	 */
	public Object getValue() {
		Object result = null;
		
		Object configurable = getConfiguration().getConfigurable();
		try {
			result = myGetter.invoke(configurable);
		} catch (Exception e) {
			throw new RuntimeException("Can't get property", e);
		}
		
		return result;
	}
	
	/**
	 * @see ca.nengo.config.Property#isFixedCardinality()
	 */
	public boolean isFixedCardinality() {
		return true;
	}

	/**
	 * By default, attempts to call method setX(y) on Configurable, where X is the name of the property (with 
	 * first letter capitalized) and y is the value (changed to a primitive if it's a primitive wrapper).
	 * A Configurable that needs different behaviour should override this method.   
	 *  
	 * @see ca.nengo.config.SingleValuedProperty#setValue(java.lang.Object)
	 */
	public void setValue(Object value) throws StructuralException {
		Object configurable = getConfiguration().getConfigurable();
		
		try {
			mySetter.invoke(configurable, value);
		} catch (InvocationTargetException e) {
			throw new StructuralException("Can't set " + getName() + ": " + e.getCause().getMessage(), e);
		} catch (Exception e) {
			throw new StructuralException("Can't set " + getName(), e);
		}
	}

	@Override
	public String getDocumentation() {
		String result = super.getDocumentation();
		
		if (result == null) {
			result = getDefaultDocumentation(new Method[]{myGetter, mySetter});
		}
			
		return result;
	}
	
}