/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NamedValuePropertyImpl.java". Description:
"Default implementation of NamedValueProperty"

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
 * Created on 18-Jan-08
 */
package ca.nengo.config.impl;

import ca.nengo.config.Configuration;
import ca.nengo.config.NamedValueProperty;
import ca.nengo.model.StructuralException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>Default implementation of NamedValueProperty. This implementation uses reflection to call methods on an
 * underlying configurable object in order to get and set multiple property values.</p>
 *
 * <p>Use of this class is analogous to {@link ca.nengo.config.impl.ListPropertyImpl}. See
 * ListPropertyImpl docs for more information.</p>
 *
 * @author Bryan Tripp
 */
public class NamedValuePropertyImpl extends AbstractProperty implements NamedValueProperty {

	private final Object myTarget;

	private Method myGetter;
	private Method myNamesGetter;
	private Method myNamedSetter;
	private Method myRemover;
	private Method myMapGetter;
	private Method myUnnamedSetter;
	private Method myArrayGetter;
	private Method myNameGetterOnType;

	/**
	 * @param configuration Configuration to which this Property belongs
	 * @param name Parameter name
	 * @param type Parameter type
	 * @return Property or null if the necessary methods don't exist on the underlying class
	 */
	public static NamedValueProperty getNamedValueProperty(Configuration configuration, String name, Class<?> type) {
		NamedValuePropertyImpl result = null;
		Class<?> targetClass = configuration.getConfigurable().getClass();

		String uname = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		String[] getterNames = new String[]{"get"+uname};
		String[] namesGetterNames = new String[]{"get"+uname+"Names"};
		String[] namedSetterNames = new String[]{"set"+uname};
		String[] removerNames = new String[]{"remove"+uname};
		String[] mapGetterNames = new String[]{"get"+uname, "get"+uname+ 's', "get"+uname+"Map"};

		String[] unnamedSetterNames = new String[]{"set"+uname};
		String[] arrayGetterNames = new String[]{"get"+uname, "get"+uname+ 's', "get"+uname+"Array", "getAll"+uname};

		Method getter = ListPropertyImpl.getMethod(targetClass, getterNames, new Class[]{String.class}, type);
		Method namesGetter = ListPropertyImpl.getMethod(targetClass, namesGetterNames, new Class[0], String[].class);
		Method namedSetter = ListPropertyImpl.getMethod(targetClass, namedSetterNames, new Class[]{String.class, type}, null);
		Method remover = ListPropertyImpl.getMethod(targetClass, removerNames, new Class[]{String.class}, null);
		if (remover == null) { //maybe returning removed item
			remover = ListPropertyImpl.getMethod(targetClass, removerNames, new Class[]{String.class}, type);
		}
		Method mapGetter = ListPropertyImpl.getMethod(targetClass, mapGetterNames, new Class[0], Map.class);

		Method nameGetterOnType = ListPropertyImpl.getMethod(type, new String[]{"getName"}, new Class[0], String.class);
		Method unnamedSetter = null;
		Method arrayGetter = null;
		if (nameGetterOnType != null) {
			unnamedSetter = ListPropertyImpl.getMethod(targetClass, unnamedSetterNames, new Class[]{type}, null);
			arrayGetter = ListPropertyImpl.getMethod(targetClass, arrayGetterNames, new Class[0], Array.newInstance(type, 0).getClass());
		}

		if (arrayGetter != null || mapGetter != null || (getter != null && namesGetter != null)) { //OK, minimal method set exists
			result = new NamedValuePropertyImpl(configuration, name, type);
			result.setAccessors(getter, namesGetter, namedSetter, remover, mapGetter, unnamedSetter, arrayGetter, nameGetterOnType);
		}

		return result;
	}

	//constructor used by factory method
	private NamedValuePropertyImpl(Configuration configuration, String name, Class<?> c) {
		super(configuration, name, c, false);
		myTarget = configuration.getConfigurable();
	}

	/**
	 * @param configuration Configuration to which this Property belongs
	 * @param name Parameter name
	 * @param c Parameter type
	 * @param getter A method on type c with a String argument that returns the named property value
	 * @param namesGetter A zero-argument method on type c that returns a String array with names of the property values
	 */
	public NamedValuePropertyImpl(Configuration configuration, String name, Class<?> c, Method getter, Method namesGetter) {
		this(configuration, name, c);
		myGetter = getter;
		myNamesGetter = namesGetter;

		myNameGetterOnType = ListPropertyImpl.getMethod(c, new String[]{"getName"}, new Class[0], String.class);
	}

	/**
	 * @param configuration Configuration to which this Property belongs
	 * @param name Parameter name
	 * @param c Parameter type
	 * @param mapGetter A zero-argument method on type c that returns a Map<String, c> containing values of the property
	 */
	public NamedValuePropertyImpl(Configuration configuration, String name, Class<?> c, Method mapGetter) {
		this(configuration, name, c);
		myMapGetter = mapGetter;

		myNameGetterOnType = ListPropertyImpl.getMethod(c, new String[]{"getName"}, new Class[0], String.class);
	}

	//used by factory method
	private void setAccessors(Method getter, Method namesGetter, Method namedSetter, Method remover, Method mapGetter,
			Method unnamedSetter, Method arrayGetter, Method nameGetterOnType) {
		myGetter = getter;
		myNamesGetter = namesGetter;
		myNamedSetter = namedSetter;
		myRemover = remover;
		myMapGetter = mapGetter;
		myUnnamedSetter = unnamedSetter;
		myArrayGetter = arrayGetter;
		myNameGetterOnType = nameGetterOnType;
	}

	/**
	 * Sets optional methods used to make the property mutable.
	 *
	 * @param setter A setter method with arg types {String, Object}; {Object} is also OK if the getType() has
	 * 		a zero-arg method getName() which returns a String
	 * @param remover A method that removes a value by name; arg types {String}
	 */
	public void setModifiers(Method setter, Method remover) {
		Class<?>[] setterParamTypes = setter.getParameterTypes();
		if (setterParamTypes.length == 1 && getType().isAssignableFrom(setterParamTypes[1]) && myNameGetterOnType != null) {
			myUnnamedSetter = setter;
		} else if (setterParamTypes.length == 2 && String.class.isAssignableFrom(setterParamTypes[0])
				&& getType().isAssignableFrom(setterParamTypes[1])) {
			myNamedSetter = setter;
		} else {
			throw new IllegalArgumentException("Setter should have arg types {String, getType()}, " +
					"although {getType()} is OK if getType() has method getName() : String");
		}

		if (remover.getParameterTypes().length == 1 && String.class.isAssignableFrom(remover.getParameterTypes()[0]) ) {
			myRemover = remover;
		} else {
			throw new IllegalArgumentException("Remover should have arg types {String}");
		}
	}

	/**
	 * @throws StructuralException if the value can't be retrieved
	 * @see ca.nengo.config.NamedValueProperty#getValue(java.lang.String)
	 */
	public Object getValue(String name) throws StructuralException {
		Object result = null;

		if (myGetter == null && myMapGetter == null && (myArrayGetter == null || myNameGetterOnType == null)) {
			throw new RuntimeException("There is no getter method for property "
					+ getName() + " -- this appears to be a bug");
		}
		try {
			if (myGetter != null) {
				result = invoke(myTarget, myGetter, new Object[]{name});
			} else if (myMapGetter != null) {
				Map<?,?> map = (Map<?,?>) invoke(myTarget, myMapGetter, new Object[0]);
				result = map.get(name);
			} else if (myArrayGetter != null) {
				Object array = invoke(myTarget, myArrayGetter, new Object[0]);
				for (int i = 0; i < Array.getLength(array) && result != null; i++) {
					Object o = Array.get(array, i);
					String thisName = (String) invoke(o, myNameGetterOnType, new Object[0]);
					if (name.equals(thisName)) {
						result = o;
					}
				}
			} else {
				throw new RuntimeException("There is no getter method for property "
						+ getName() + " -- this appears to be a bug");
			}
		} catch (Exception e) {
			throw new StructuralException("Can't get value " + name, e);
		}

		return result;
	}

	/**
	 * @see ca.nengo.config.NamedValueProperty#getValueNames()
	 */
	@SuppressWarnings("unchecked")
	public List<String> getValueNames() {
		List<String> result = new ArrayList<String>(10);

		if (myNamesGetter != null) {
			String[] names = (String[]) invoke(myTarget, myNamesGetter, new Object[0]);
			result.addAll(Arrays.asList(names));
		} else if (myArrayGetter != null) {
			Object[] array = (Object[]) invoke(myTarget, myArrayGetter, new Object[0]);
			for (Object o : array) {
				try {
					String name = (String) invoke(o, o.getClass().getMethod("getName"), new Object[0]);
					result.add(name);
				} catch (SecurityException e) {
					throw new RuntimeException(e);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}
		} else if (myMapGetter != null) {
			Map<String, ?> map = (Map<String, ?>) invoke(myTarget, myMapGetter, new Object[0]);
			result.addAll(map.keySet());
		} else {
			throw new RuntimeException("There is no array or map getter for property "
					+ getName() + " -- this appears to be a bug");
		}

		return result;
	}

	/**
	 * @see ca.nengo.config.NamedValueProperty#isNamedAutomatically()
	 */
	public boolean isNamedAutomatically() {
		return myUnnamedSetter != null;
	}

	/**
	 * @see ca.nengo.config.NamedValueProperty#removeValue(java.lang.String)
	 */
	public void removeValue(String name) throws StructuralException {
		if (myRemover != null) {
			invoke(myTarget, myRemover, new Object[]{name});
		} else if (myMapGetter != null) {
			Map<?,?> map = (Map<?,?>) invoke(myTarget, myMapGetter, new Object[0]);
			map.remove(name);
		} else {
			if (!isMutable()) {
				throw new StructuralException("This property is immutable");
			} else {
				throw new RuntimeException("Property is marked as mutable but there is no method for removing a value " +
						"-- this appears to be a bug");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setValue(String name, Object value) throws StructuralException {
		if (myNamedSetter != null) {
			invoke(myTarget, myNamedSetter, new Object[]{name, value});
		} else if (myMapGetter != null) {
			Map<String, Object> map = (Map<String, Object>) invoke(myTarget, myMapGetter, new Object[0]);
			map.put(name, value);
		} else if (myUnnamedSetter != null) {
			invoke(myTarget, myUnnamedSetter, new Object[]{value});
		} else {
			if (!isMutable()) {
				throw new StructuralException("This property is immutable");
			} else {
				throw new RuntimeException("Property is marked as mutable but there is no method for setting a value " +
						"-- this appears to be a bug");
			}
		}

	}

	public void setValue(Object value) throws StructuralException {
		if (myUnnamedSetter != null) {
			invoke(myTarget, myUnnamedSetter, new Object[]{value});
		} else {
			if (!isMutable()) {
				throw new StructuralException("This property is immutable");
			} else if (!isNamedAutomatically()) {
				throw new StructuralException("Names must be specified for values of this property");
			} else {
				throw new RuntimeException("Property is marked as mutable and automatically-named, but there is no method for setting a value " +
						" without specifying a name -- this appears to be a bug");
			}
		}
	}

	/**
	 * @see ca.nengo.config.Property#isFixedCardinality()
	 */
	public boolean isFixedCardinality() {
		return myMapGetter == null
			&& (myRemover == null || (myNamedSetter == null && myUnnamedSetter == null));
	}

	/**
	 * @see ca.nengo.config.impl.AbstractProperty#isMutable()
	 */
	public boolean isMutable() {
		return (myNamedSetter != null || myUnnamedSetter != null)
			|| myMapGetter != null;
	}

	private static Object invoke(Object target, Method method, Object[] args) {
		try {
			return method.invoke(target, args);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDocumentation() {
		String result = super.getDocumentation();

		if (result == null) {
			Method[] methods = new Method[]{myGetter, myNamedSetter, myUnnamedSetter,
					myArrayGetter, myMapGetter, myNamesGetter, myRemover};
			result = getDefaultDocumentation(methods);
		}

		return result;
	}

}