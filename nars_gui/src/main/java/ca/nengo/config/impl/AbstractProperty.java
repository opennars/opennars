/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "AbstractProperty.java". Description: 
"Base implementation of Property"

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
import ca.nengo.config.Property;

import java.lang.reflect.Method;

/**
 * Base implementation of Property. 
 *  
 * @author Bryan Tripp
 */
public abstract class AbstractProperty implements Property {
		
	private final Configuration myConfiguration;
	private String myName;
	private final Class<?> myClass;
	private final boolean myMutable;
	private String myDocumentation;
	
	/**
	 * @param configuration Configuration to which the Property belongs
	 * @param name Name of the Property
	 * @param c Type of the Property
	 * @param mutable Whether the Property value(s) can be modified
	 */
	public AbstractProperty(Configuration configuration, String name, Class<?> c, boolean mutable) {
		myConfiguration = configuration;
		myName = name;
		myClass = c;	
		myMutable = mutable;
	}
	
	/**
	 * @see ca.nengo.config.Property#getName()
	 */
	public String getName() {
		return myName;
	}
	
	/**
	 * @see ca.nengo.config.Property#setName(java.lang.String)
	 */
	public void setName(String name) {
		myName = name;
	}

	/**
	 * @see ca.nengo.config.Property#getType()
	 */
	public Class<?> getType() {
		return myClass;
	}

	/**
	 * @see ca.nengo.config.Property#isMutable()
	 */
	public boolean isMutable() {
		return myMutable;
	}
	
	protected Configuration getConfiguration() {
		return myConfiguration;
	}

	/**
	 * @see ca.nengo.config.Property#getDocumentation()
	 */
	public String getDocumentation() {
		return myDocumentation;
	}

	/**
	 * @param text New documentation text (can be plain text or HTML)
	 */
	public void setDocumentation(String text) {
		myDocumentation = text;
	}
	
	/**
	 * @param methods The methods that underlie this property
	 * @return A default documentation string composed of javadocs for these methods
	 */
	protected String getDefaultDocumentation(Method[] methods) {
		StringBuffer buf = new StringBuffer("<p><small>[Note: No documentation has been written specifically for the property <i>");
		buf.append(getName());  
		buf.append("</i>. Documentation for the API methods that support this property is shown below.]</small></p>");
		
		for (int i = 0; i < methods.length; i++) {
			appendDocs(buf, methods[i]);			
		}
		
		return buf.toString();
	}
	
	private static void appendDocs(StringBuffer buf, Method method) {
//		if (method != null) {
//			buf.append("<p><i>").append(JavaSourceParser.getSignature(method)).append("</i><br>");
//			String docs = JavaSourceParser.getDocs(method);
//			if (docs != null) buf.append(docs);
//			buf.append("</p>");
//		}
	}

}