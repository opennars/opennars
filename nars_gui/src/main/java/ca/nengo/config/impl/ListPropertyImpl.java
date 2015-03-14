/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ListPropertyImpl.java". Description:
"Default implementation of ListProperty"

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

import ca.nengo.config.PropretiesUtil;
import ca.nengo.config.Configuration;
import ca.nengo.config.ListProperty;
import ca.nengo.model.StructuralException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * <p>Default implementation of ListProperty. This implementation uses reflection to call methods on an
 * underlying configurable object in order to get and set multiple property values.</p>
 *
 * <p>The easiest way to use this class is via the factory method getListProperty(...). In this case,
 * the class of configuration.getConfigurable() is searched for methods that appear to be getters,
 * setters, etc. of a named parameter. For example if the parameter is named "X" and has type Foo, then
 * a method getX(int) with return type Foo is taken as the getter. Methods to get, set, insert, add, remove values,
 * get/set arrays, and get lists are searched based on return and argument types, and a variety of probable names
 * (including bean patterns). If there are not methods available for at least getting and counting values,
 * null is returned. The set of other methods found determines whether the property is mutable and has
 * fixed cardinality (i.e. # of values).</p>
 *
 * <p>If customization is needed, there are two alternative public constructors that accept user-specified methods,
 * and additional functionality can then be imparted via setSetter(...), setArraySetter(...), and setInserter(...).
 * This allows use of methods with unexpected names, although the expected arguments and return types are
 * still required.</p>
 *
 * <p>If further customization is needed (e.g. using an Integer index argument instead of an int), then the methods
 * of this class must be overridden.</p>
 *
 * @author Bryan Tripp
 */
public class ListPropertyImpl extends AbstractProperty implements ListProperty {

	private final Object myTarget;

	private Method myGetter;
	private Method mySetter;
	private Method myCountGetter;
	private Method myArrayGetter;
	private Method myArraySetter;
	private Method myListGetter;
	private Method myInserter;
	private Method myRemover;
	private Method myAdder;

	/**
	 * @param configuration Configuration to which this Property belongs
	 * @param name Parameter name
	 * @param type Parameter type
	 * @return Property or null if the necessary methods don't exist on the underlying class
	 */
	public static ListProperty getListProperty(Configuration configuration, String name, Class<?> type) {
		ListPropertyImpl result = null;
		Class<?> targetClass = configuration.getConfigurable().getClass();

		String uname = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		String[] getterNames = new String[]{"get"+uname};
		String[] setterNames = new String[]{"set"+uname,
				"set"+ PropretiesUtil.stripSuffix(uname, "s"), "set"+ PropretiesUtil.stripSuffix(uname, "es")};
		String[] countGetterNames = new String[]{"getNum"+uname, "getNum"+uname+ 's', "get"+uname+"Count"};
		String[] arrayGetterNames = new String[]{"get"+uname, "get"+uname+ 's', "get"+uname+"Array", "getAll"+uname};
		String[] arraySetterNames = new String[]{"set"+uname, "set"+uname+ 's', "set"+uname+"Array", "setAll"+uname};
		String[] listGetterNames = new String[]{"get"+uname, "get"+uname+ 's', "get"+uname+"List"};
		String[] inserterNames = new String[]{"insert"+uname};
		String[] adderNames = new String[]{"add"+uname};
		String[] removerNames = new String[]{"remove"+uname};

		Method getter = getMethod(targetClass, getterNames, new Class[]{Integer.TYPE}, type);
		Method setter = getMethod(targetClass, setterNames, new Class[]{Integer.TYPE, type}, null);
		Method countGetter = getMethod(targetClass, countGetterNames, new Class[0], Integer.TYPE);
		Method arrayGetter = getMethod(targetClass, arrayGetterNames, new Class[0], Array.newInstance(type, 0).getClass());
		Method arraySetter = getMethod(targetClass, arraySetterNames, new Class[]{Array.newInstance(type, 0).getClass()}, null);
		Method listGetter = getMethod(targetClass, listGetterNames, new Class[0], List.class);
		Method inserter = getMethod(targetClass, inserterNames, new Class[]{Integer.TYPE, type}, null);
		Method adder = getMethod(targetClass, adderNames, new Class[]{type}, null);
		Method remover = getMethod(targetClass, removerNames, new Class[]{Integer.TYPE}, null);
		if (remover == null) {
			remover = getMethod(targetClass, removerNames, new Class[]{Integer.TYPE}, type);
		}

		if (arrayGetter != null || listGetter != null || (getter != null && countGetter != null)) { //OK, minimal method set exists
			result = new ListPropertyImpl(configuration, name, type);
			result.setAccessors(getter, setter, countGetter, arrayGetter, arraySetter, listGetter, inserter, remover, adder);
		}

		return result;
	}

	/**
	 * Looks for defined method
	 *
	 * @param c Class to search
	 * @param names Methods to find?
	 * @param argTypes Argument types
	 * @param returnType Return type
	 * @return The Method, or null if it doesn't exist
	 */
	public static Method getMethod(Class<?> c, String[] names, Class<?>[] argTypes, Class<?> returnType) {
		Method result = null;

		Method[] methods = c.getMethods();
		for (int i = 0; i < methods.length && result == null; i++) {
			for (String name : names) {
				if (methods[i].getName().equals(name) && typesCompatible(methods[i].getParameterTypes(), argTypes)) {
					if (returnType == null || methods[i].getReturnType().equals(returnType)) {
						result = methods[i];
					}
				}
			}
		}

		return result;
	}

	//checks that two lists of classes are the same
	private static boolean typesCompatible(Class<?>[] a, Class<?>[] b) {
		boolean match = a.length == b.length;

		for (int i = 0; i < a.length && match; i++) {
			if (!a[i].equals(b[i])) {
				match = false;
			}
		}

		return match;
	}

	//used by factory method
	private ListPropertyImpl(Configuration configuration, String name, Class<?> c) {
		super(configuration, name, c, false);
		myTarget = configuration.getConfigurable();
	}

	/**
	 * @param configuration Configuration to which this Property is to belong
	 * @param name Name of the Property, eg X if it is accessed via getX(...)
	 * @param c Class of the Property
	 * @param listGetter A method on the underlying class (the configuration target; not c) that
	 * 		returns multiple values of the property, as either an array of c or a list of c.
	 */
	public ListPropertyImpl(Configuration configuration, String name, Class<?> c, Method listGetter) {
		super(configuration, name, c, false);
		myTarget = configuration.getConfigurable();

		if (listGetter.getReturnType().equals(List.class)) {
			myListGetter = listGetter;
		} else {
			myArrayGetter = listGetter;
		}
	}

	/**
	 * @param configuration Configuration to which this Property is to belong
	 * @param name Name of the Property, eg X if it is accessed via getX()
	 * @param c Class of the Property
	 * @param getter A method on the underlying class (the configuration target; not c) that
	 * 		returns a single indexed value of the property, eg getX(int).
	 * @param countGetter A method on the underlying class that returns the number of
	 * 		values of the property
	 */
	public ListPropertyImpl(Configuration configuration, String name, Class<?> c, Method getter, Method countGetter) {
		super(configuration, name, c, false);
		myTarget = configuration.getConfigurable();
		myGetter = getter;
		myCountGetter = countGetter;
	}

	/**
	 * @param setter A method on the underlying class acts as a setter for a single
	 * 		value of the property, eg setX(int, Object)
	 */
	public void setSetter(Method setter) {
		mySetter = setter;
	}

	/**
	 * @param arraySetter A method on the underlying class that acts as a setter for
	 * 		all values of the property using an array argument, eg setX(Object[])
	 */
	public void setArraySetter(Method arraySetter) {
		myArraySetter = arraySetter;
	}

	/**
	 * @param inserter A method on the underlying class that inserts a value, eg insertX(int, Object)
	 * @param adder A method on the underlying class that adds a value to the end of the list, eg addX(Object)
	 * @param remover A method on the underlying class that removes a value, eg removeX(int)
	 */
	public void setInserter(Method inserter, Method adder, Method remover) {
		myInserter = inserter;
		myAdder = adder;
		myRemover = remover;
	}

	//used by factory method
	private void setAccessors(Method getter, Method setter, Method countGetter, Method arrayGetter, Method arraySetter,
			Method listGetter, Method inserter, Method remover, Method adder) {
		myGetter = getter;
		mySetter = setter;
		myCountGetter = countGetter;
		myArrayGetter = arrayGetter;
		myArraySetter = arraySetter;
		myListGetter = listGetter;
		myInserter = inserter;
		myRemover = remover;
		myAdder = adder;
	}

	/**
	 * @see ca.nengo.config.ListProperty#getDefaultValue()
	 */
	public Object getDefaultValue() {
		try {
			return getNumValues() > 0 ? getValue(0) : null;
		} catch (StructuralException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see ca.nengo.config.ListProperty#getNumValues()
	 */
	public int getNumValues() {
		int result = -1;

		try {
			if (myCountGetter != null) {
				result = ((Integer) myCountGetter.invoke(myTarget)).intValue();
			} else if (myArrayGetter != null) {
				Object array = myArrayGetter.invoke(myTarget);
				result = Array.getLength(array);
			} else if (myListGetter != null) {
				result = getList(myTarget, myListGetter).size();
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * @see ca.nengo.config.ListProperty#getValue(int)
	 */
	public Object getValue(int index) throws StructuralException {
		Object result = null;

		try {
			if (myGetter != null) {
				result = myGetter.invoke(myTarget, Integer.valueOf(index));
			} else if (myArrayGetter != null) {
				Object array = myArrayGetter.invoke(myTarget);
				result = Array.get(array, index);
			} else if (myListGetter != null) {
				result = getList(myTarget, myListGetter).get(index);
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * @see ca.nengo.config.ListProperty#setValue(int, java.lang.Object)
	 */
	public void setValue(int index, Object value) throws StructuralException {
		try {
			if (mySetter != null) {
				mySetter.invoke(myTarget, Integer.valueOf(index), value);
			} else if (myArrayGetter != null && myArraySetter != null) {
				Object array = myArrayGetter.invoke(myTarget);
				Array.set(array, index, value);
				myArraySetter.invoke(myTarget, array);
			} else if (myListGetter != null) {
				getList(myTarget, myListGetter).set(index, value);
			} else {
				if (isMutable()) {
					throw new RuntimeException("The property is marked as mutable but there are no methods for changing -- this appears to be a bug");
				} else {
					throw new StructuralException("This property is immutable");
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if ((e.getCause() instanceof StructuralException)) {
				throw (StructuralException) e.getCause();
			}
			else {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * @see ca.nengo.config.ListProperty#addValue(java.lang.Object)
	 */
	public void addValue(Object value) throws StructuralException {
		try {
			if (myAdder != null) {
				myAdder.invoke(myTarget, value);
			} else if (myInserter != null) {
				int index = getNumValues();
				myInserter.invoke(myTarget, Integer.valueOf(index), value);
			} else if (myListGetter != null) {
				getList(myTarget, myListGetter).add(value);
			} else if (myArrayGetter != null && myArraySetter != null) {
				Object array = myArrayGetter.invoke(myTarget);
				Object newArray = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array) + 1);
				System.arraycopy(array, 0, newArray, 0, Array.getLength(array));
				Array.set(newArray, Array.getLength(array), value);
				myArraySetter.invoke(myTarget, newArray);
			} else {
				if (isFixedCardinality()) {
					throw new StructuralException("This property has fixed cardinality");
				} else {
					throw new RuntimeException("The property is not marked as having fixed cardinality " +
							"but there are no methods for adding a value -- this appears to be a bug");
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see ca.nengo.config.ListProperty#insert(int, java.lang.Object)
	 */
	public void insert(int index, Object value) throws StructuralException {
		try {
			if (myInserter != null) {
				myInserter.invoke(myTarget, Integer.valueOf(index), value);
			} else if (myListGetter != null) {
				getList(myTarget, myListGetter).add(index, value);
			} else if (myArrayGetter != null && myArraySetter != null) {
				Object array = myArrayGetter.invoke(myTarget);
				Object newArray = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array) + 1);
				System.arraycopy(array, 0, newArray, 0, index);
				Array.set(newArray, index, value);
				System.arraycopy(array, index, newArray, index+1, Array.getLength(array)-index);
				myArraySetter.invoke(myTarget, newArray);
			} else {
				if (isFixedCardinality()) {
					throw new StructuralException("This property has fixed cardinality");
				} else {
					throw new RuntimeException("The property is not marked as having fixed cardinality " +
							"but there are no methods for inserting a value -- this appears to be a bug");
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see ca.nengo.config.ListProperty#remove(int)
	 */
	public void remove(int index) throws StructuralException {
		try {
			if (myRemover != null) {
				myRemover.invoke(myTarget, Integer.valueOf(index));
			} else if (myListGetter != null) {
				getList(myTarget, myListGetter).remove(index);
			} else if (myArrayGetter != null && myArraySetter != null) {
				Object array = myArrayGetter.invoke(myTarget);
				Object newArray = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array) - 1);
				System.arraycopy(array, 0, newArray, 0, index);
				System.arraycopy(array, index+1, newArray, index, Array.getLength(newArray) - index);
				myArraySetter.invoke(myTarget, newArray);
			} else {
				if (isFixedCardinality()) {
					throw new StructuralException("This property has fixed cardinality");
				} else {
					throw new RuntimeException("The property is not marked as having fixed cardinality " +
							"but there are no methods for removing a value -- this appears to be a bug");
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Object> getList(Object target, Method listGetter) {
		try {
			return (List<Object>) listGetter.invoke(target);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see ca.nengo.config.Property#isFixedCardinality()
	 */
	public boolean isFixedCardinality() {
		return (myListGetter == null && (myArrayGetter == null || myArraySetter == null)
				&& (myInserter == null || myRemover == null));
	}

	@Override
	public boolean isMutable() {
		return (mySetter != null || myListGetter != null || myArraySetter != null);
	}

	@Override
	public String getDocumentation() {
		String result = super.getDocumentation();

		if (result == null) {
			Method[] methods = new Method[]{myGetter, mySetter, myArrayGetter, myArraySetter,
					myCountGetter, myListGetter, myAdder, myInserter, myRemover};
			result = getDefaultDocumentation(methods);
		}

		return result;
	}

}