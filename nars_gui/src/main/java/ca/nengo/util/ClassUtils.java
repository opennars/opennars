/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ClassUtils.java". Description: 
"Class-related utility methods"

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
 * Created on 24-Jan-08
 */
package ca.nengo.util;

import java.lang.reflect.Array;

/**
 * Class-related utility methods. 
 *  
 * @author Bryan Tripp
 */
public class ClassUtils {

	/**
	 * As Class.forName(String) but arrays and primitives are also handled. 
	 * 
	 * @param name Name of a Class
	 * @return Named Class 
	 * @throws ClassNotFoundException
	 */
	public static Class<?> forName(String name) throws ClassNotFoundException {
		Class<?> result = null;
		
		if (name.endsWith("[]")) {
			Class<?> baseClass = forName(name.substring(0, name.length()-2));
			result = Array.newInstance(baseClass, 0).getClass();
		} else if (name.equals("byte")) {
			result = Byte.TYPE;
		} else if (name.equals("short")) {
			result = Short.TYPE;
		} else if (name.equals("int")) {
			result = Integer.TYPE;
		} else if (name.equals("long")) {
			result = Long.TYPE;
		} else if (name.equals("float")) {
			result = Float.TYPE;
		} else if (name.equals("double")) {
			result = Double.TYPE;
		} else if (name.equals("boolean")) {
			result = Boolean.TYPE;
		} else if (name.equals("char")) {
			result = Character.TYPE;
		} else {
			result = Class.forName(name);
		}
		
		return result;
	}
	
	/**
	 * @param c A Class
	 * @return The class name, with arrays identified with trailing "[]"
	 */
	public static String getName(Class<?> c) {
		if (c.isArray()) {
			return getName(c.getComponentType()) + "[]";
		} else {
			return c.getName();
		}
	}
}
