/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ClassRegistry.java". Description:
"A registry of implementations of selected types of interest (subclasses and interface implementations)"

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
 * Created on 19-Dec-07
 */
package ca.nengo.config;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A registry of implementations of selected types of interest (subclasses and
 * interface implementations). This gets used when generating the list of 
 * available input functions (e.g., PFunction in simulator-ui)
 *
 * TODO: unit tests
 *
 * @author Bryan Tripp
 */
public final class ClassRegistry {

	private static final Logger ourLogger = LogManager.getLogger(ClassRegistry.class);
	private static ClassRegistry ourInstance;

	/**
	 * Location of types...
	 */
	public static final String TYPES_LOCATION_PROPERTY = "ca.nengo.config.types";

	/**
     * Location of implementations...
     */
	public static final String IMPLS_LOCATION_PROPERTY = "ca.nengo.config.implementations";

	/**
	 * @return Shared instance
	 */
	public static synchronized ClassRegistry getInstance() {
		if (ourInstance == null) {
			ourInstance = new ClassRegistry();
		}
		return ourInstance;
	}
	
	private final Map<Class<?>, List<Class<?>>> myImplementations;

	private ClassRegistry() {
		myImplementations = new HashMap<Class<?>, List<Class<?>>>(100);

		String[] types = loadList(System.getProperty(TYPES_LOCATION_PROPERTY, "ca/nengo/config/types.txt"));
		for (String type2 : types) {
			try {
				Class<?> type = Class.forName(type2);
				addRegisterableType(type);
			} catch (ClassNotFoundException e) {
				ourLogger.warn("Can't add type " + type2, e);
			}
		}

		String[] impls = loadList(System.getProperty(IMPLS_LOCATION_PROPERTY, "ca/nengo/config/impls.txt"));
		for (String impl : impls) {
			try {
				register(impl);
			} catch (ClassNotFoundException e) {
				ourLogger.warn("Can't register implementation " + impl, e);
			}
		}

	}

	private static String[] loadList(String resource) {
		List<String> result = new ArrayList<String>(10);

		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
			if (is == null) {
				throw new IOException("Can't open resource " + resource);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() > 0) {
                    result.add(line.trim());
                }
			}
		} catch (IOException e) {
			ourLogger.warn("Can't load list from " + resource, e);
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 * As addRegisterableType(Class), but ancestors are added as well.
	 *
	 * @param type Type at bottom of hierarchy
	 */
//	public void addHierarchy(Class<?> type) {
//		Class<?> c = type;
//		while (c != null) {
//			addRegisterableType(c);
//			c = c.getSuperclass();
//		}
//	}

	/**
	 * Adds a class to the list of types whose implementations can be registered
	 * (only implementations of certain types can be registered).
	 *
	 * @param type Type to add to list of registerable types
	 */
	public void addRegisterableType(Class<?> type) {
		if (!myImplementations.containsKey(type)) {
			myImplementations.put(type, new ArrayList<Class<?>>(10));
		}
	}

	/**
	 * @return The list of types whose implementations can be registered
	 */
//	public Class<?>[] getRegisterableTypes() {
//		return myImplementations.keySet().toArray(new Class[0]);
//	}

	/**
	 * Registers an implementation against any of the registerable types which it is
	 * assignable from.
	 *
	 * @param implementation Class to register as an implementation of matching registerable types
	 */
	public void register(Class<?> implementation) {
		int mods = implementation.getModifiers();
		if (!Modifier.isAbstract(mods) && !Modifier.isPrivate(mods)) {
			Iterator<Class<?>> knownTypes = myImplementations.keySet().iterator();
			while (knownTypes.hasNext()) {
				Class<?> knownType = knownTypes.next();
				if (knownType.isAssignableFrom(implementation)) {
					myImplementations.get(knownType).add(implementation);
				}
			}
		}
	}

	/**
	 * As register(Class), but by name.
	 *
	 * @param implementationName Name of implementation to register
	 * @throws ClassNotFoundException if the class doesn't exist?
	 */
	public void register(String implementationName) throws ClassNotFoundException {
		Class<?> implementation = Class.forName(implementationName);
		register(implementation);
	}

	/**
	 * Registers public, non-abstract classes in the given Jar.
	 *
	 * @param jar Jar from which to draw classes to register
	 * @throws ClassNotFoundException if the JarFile contains bad class files?
	 */
//	public void register(JarFile jar) throws ClassNotFoundException {
//		Enumeration<JarEntry> entries = jar.entries();
//		while (entries.hasMoreElements()) {
//			JarEntry entry = entries.nextElement();
//			String fileName = entry.getName();
//			if (fileName.endsWith(".class")) {
//				String className
//					= fileName.substring(0, fileName.lastIndexOf('.')).replace('/', '.');//.replace('$', '.');
//				System.out.println(className);
//				register(className);
//			}
//		}
//	}

	/**
	 * @param type A registerable type
	 * @return A list of registered implementations of the given type (empty if type is unknown)
	 */
	public List<Class<?>> getImplementations(Class<?> type) {
		List<Class<?>> result = new ArrayList<Class<?>>(20);

		if (myImplementations.containsKey(type)) {
			result.addAll(myImplementations.get(type));
		}

		return result;
	}


	//functional test code
//	public static void main(String[] args) {
//		try {
//			JarFile jar = new JarFile("lib/jmatio.jar");
//			ClassRegistry.getInstance().addType(Object.class);
//			ClassRegistry.getInstance().register(jar);
//			Iterator<Class> impls = ClassRegistry.getInstance().getImplementations(Object.class).iterator();
//			while (impls.hasNext()) {
//				System.out.println("Impl: " + impls.next().getName());
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//	}

}
