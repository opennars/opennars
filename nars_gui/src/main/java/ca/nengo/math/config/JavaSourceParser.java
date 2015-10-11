///*
//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the License.
//You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
//WARRANTY OF ANY KIND, either express or implied. See the License for the specific
//language governing rights and limitations under the License.
//
//The Original Code is "JavaSourceParser.java". Description:
//"Utilities for extracting data from Java source code files, including
//  variable names and documentation"
//
//The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.
//
//Alternatively, the contents of this file may be used under the terms of the GNU
//Public License license (the GPL License), in which case the provisions of GPL
//License are applicable  instead of those above. If you wish to allow use of your
//version of this file only under the terms of the GPL License and not to allow
//others to use your version of this file under the MPL, indicate your decision
//by deleting the provisions above and replace  them with the notice and other
//provisions required by the GPL License.  If you do not delete the provisions above,
//a recipient may use your version of this file under either the MPL or the GPL License.
//*/
//
///*
// * Created on 13-Nov-07
// */
//package ca.nengo.config;
//
//import ca.nengo.util.ClassUtils;
//import com.thoughtworks.qdox.JavaProjectBuilder;
//import com.thoughtworks.qdox.model.DocletTag;
//import com.thoughtworks.qdox.model.JavaClass;
//import com.thoughtworks.qdox.model.JavaMethod;
//import com.thoughtworks.qdox.model.JavaParameter;
//import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
//
//import java.io.File;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.StringTokenizer;
//
///**
// * Utilities for extracting data from Java source code files, including
// * variable names and documentation.
// *
// * @author Bryan Tripp
// */
//public class JavaSourceParser {
//
//
//	private static final Logger ourLogger = LogManager.getLogger(JavaSourceParser.class);
//	private static final JavaProjectBuilder ourBuilder;
//
//	static {
//		ourBuilder = new JavaProjectBuilder();
//		//TODO: make this configurable
////		addSource(new File("src/java/main"));
//	}
//
//	/**
//	 * Adds source code under the given directory to the database.
//	 *
//	 * @param baseDir Root directory of source code
//	 */
//	public static void addSource(File baseDir) {
//		ourBuilder.addSourceTree(baseDir);
//		ourLogger.debug("Adding source tree: " + baseDir.getAbsolutePath());
//	}
//
//	/**
//	 * @param c A Java class
//	 * @return Class-level documentation if available, othewise null
//	 */
//	public static String getDocs(Class<?> c) {
//		JavaClass jc = ourBuilder.getClassByName(c.getName());
//		List<JavaClass> interfaces = jc.getImplementedInterfaces();
//
//		StringBuilder docs = new StringBuilder(jc.getName());
//		if (c.getSuperclass() != null && c.getSuperclass() != Object.class) {
//			docs.append(" extends ");
//			docs.append(c.getSuperclass().getSimpleName());
//		}
//
//		if (interfaces.size() > 0) {
//			docs.append(" implements ");
//		}
//
//		for (int i = 0; i < interfaces.size(); i++) {
//			docs.append(interfaces.get(i).getName());
//			if (i < interfaces.size() - 1) {
//                docs.append(", ");
//            }
//		}
//		docs.append(":\r\n");
//
//		docs.append(jc.getComment());
//		for (JavaClass interface1 : interfaces) {
//			docs.append("\r\n\r\n").append(interface1.getFullyQualifiedName()).append(":\r\n");
//			docs.append(interface1.getComment());
//		}
//
//		return docs.toString();
//	}
//
//	/**
//	 * @param reference A JavaDoc see-tag-style reference, ie fully.qualified.ClassName#methodName(ArgType0, ArgType1)
//	 * @param referringClassName Name of class on which the see tag is written (used to find default package if
//	 * 		arg types are not qualified, and class if undefined)
//	 * @return Matching Method if possible, otherwise null
//	 * @throws NoSuchMethodException if the method doesn't exist
//	 * @throws SecurityException if we can't access the method
//	 * @throws ClassNotFoundException if the class doesn't exist
//	 */
//	public static Method getMethod(String reference, String referringClassName)
//			throws SecurityException, NoSuchMethodException {
//
//		Method result = null;
//
//		if (reference.startsWith("@see")) {
//			reference = reference.substring(4);
//		}
//
//		String className = referringClassName;
//		int index = reference.indexOf('#');
//		if (index > 0) {
//			className = reference.substring(0, index).trim();
//			reference = reference.substring(index+1);
//		}
//
//		String packageName = referringClassName.substring(0, referringClassName.lastIndexOf('.'));
//		Class<?> type = getType(className, packageName);
//
//		StringTokenizer tok = new StringTokenizer(reference, "(, )", false);
//		String methodName = tok.hasMoreTokens() ? tok.nextToken() : null;
//
//		if (type != null && methodName != null) {
//			List<Class<?>> argTypes = new ArrayList<Class<?>>(10);
//			while (tok.hasMoreTokens()) {
//				String argTypeName = tok.nextToken().trim();
//				argTypes.add(getType(argTypeName, packageName));
//			}
//
//			result = type.getMethod(methodName, argTypes.toArray(new Class[argTypes.size()]));
//		}
//
//		return result;
//	}
//
//	private static Class<?> getType(String name, String packageName) {
//		Class<?> result = null;
//
//		result = getType(name);
//		if (result == null) {
//            getType(packageName + '.' + name);
//        }
//		if (result == null) {
//            getType("java.lang." + name);
//        }
//
//		return result;
//	}
//
//	//eats any ClassNotFoundExceptions and returns null
//	private static Class<?> getType(String name) {
//		Class<?> result = null;
//		try {
//			result = ClassUtils.forName(name);
//		} catch (ClassNotFoundException e) {
//			ourLogger.warn("JavaSourceParser.getType(...) can't find type " + name);
//		}
//		return result;
//	}
//
//	/**
//	 * @param m A Java method
//	 * @return Method-level documentation if available, otherwise empty string
//	 */
//	public static String getDocs(Method m) {
//		JavaMethod jm = getJavaMethod(m);
//		return getDocs(jm);
//	}
//
//	/**
//	 * @param c A Java constructor
//	 * @return Constructor documentation if available, otherwise empty string
//	 */
//	public static String getDocs(Constructor<?> c) {
//		JavaMethod jm = getJavaMethod(c);
//		return getDocs(jm);
//	}
//
//	private static String getDocs(JavaMethod jm) {
//		StringBuilder result = new StringBuilder();
//
//		if (jm != null) {
//			String comment = jm.getComment();
//			if (comment != null) {
//				result.append(comment);
//				result.append("\r\n\r\n");
//			}
//			result.append(getTagText(jm));
//		}
//
//		return result.toString();
//	}
//
//	//returns concatenated text of doc tag names and values
//	private static String getTagText(JavaMethod entity) {
//		StringBuilder result = new StringBuilder();
//
//		List<DocletTag> tags = entity.getTags();
//		for (DocletTag tag : tags) {
//			if (tag.getName().equals("see")) { //attempt to substitute references docs
//				String className = ".";
//				if (entity instanceof JavaMethod) {
//					className = entity.getParentClass().getFullyQualifiedName();
//				} else if (entity instanceof JavaClass) {
//					className = ((JavaClass) entity).getFullyQualifiedName();
//				}
//				try {
//					Method referencedMethod = getMethod(tag.getValue(), className);
//					String referencedDocs = getDocs(referencedMethod);
//					result.append("\r\n").append(referencedDocs).append("\r\n");
//				} catch (Exception e) {
//					ourLogger.warn("Can't get docs for reference " + tag.getValue(), e);
//				}
//			} else {
//				result.append("<p>");
//				result.append("<b>");
//				result.append(tag.getName());
//				result.append(": </b>");
//				result.append(tag.getValue());
//				result.append("</p>");
//				result.append("\r\n");
//			}
//		}
//
//		return result.toString();
//	}
//
//	/**
//	 * @param m A Java method
//	 * @return Names of method arguments if available, otherwise the default {"arg0", "arg1", ...}
//	 */
//	public static String[] getArgNames(Method m) {
//		String[] result = new String[m.getParameterTypes().length];
//
//		JavaMethod jm = getJavaMethod(m);
//		for (int i = 0; i < result.length; i++) {
//			result[i] = (jm == null) ? "arg"+i : jm.getParameters().get(i).getName();
//		}
//
//		return result;
//	}
//
//	/**
//	 * @param c A Java constructor
//	 * @return Names of constructor arguments if available, otherwise the default {"arg0", "arg1", ...}
//	 */
//	public static String[] getArgNames(Constructor<?> c) {
//		String[] result = new String[c.getParameterTypes().length];
//
//		JavaMethod jm = getJavaMethod(c);
//		for (int i = 0; i < result.length; i++) {
//			result[i] = (jm == null) ? "arg"+i : jm.getParameters().get(i).getName();
//		}
//
//		return result;
//	}
//
//	/**
//	 * @param m A Java method
//	 * @param arg Index of an argument on this method
//	 * @return Argument documentation if available, otherwise null
//	 */
//	public static String getArgDocs(Method m, int arg) {
//		return getArgDocs(getJavaMethod(m), arg);
//	}
//
//	/**
//	 * @param c A Java constructor
//	 * @param arg Index of an argument on this constructor
//	 * @return Argument documentation if available, otherwise null
//	 */
//	public static String getArgDocs(Constructor<?> c, int arg) {
//		return getArgDocs(getJavaMethod(c), arg);
//	}
//
//	private static String getArgDocs(JavaMethod jm, int arg) {
//		String result = null;
//
//		if (jm != null && jm.getParameters().size() > arg) {
//			String argName = jm.getParameters().get(arg).getName();
//			List<DocletTag> tags = jm.getTags();
//			for (int i = 0; i < tags.size() && result == null; i++) {
//                DocletTag docletTag = tags.get(i);
//                if (docletTag.getName().equals("param") && docletTag.getValue().startsWith(argName)) {
//					result = docletTag.getValue().substring(argName.length()).trim();
//				}
//			}
//		}
//
//		return result;
//	}
//
//	/**
//	 * @param m A Java method
//	 * @return A text representation of the method signature (for display)
//	 */
//	public static String getSignature(Method m) {
//		StringBuilder result = new StringBuilder();
//
//		String[] argNames = getArgNames(m);
//
//		Class<?> returnType = m.getReturnType();
//		if (returnType != null) {
//            result.append(ClassUtils.getName(returnType)).append(' ');
//        }
//		result.append(m.getName());
//		result.append('(');
//		Class<?>[] paramTypes = m.getParameterTypes();
//		for (int i = 0; i < paramTypes.length; i++) {
//			result.append(ClassUtils.getName(paramTypes[i]));
//			result.append(' ');
//			result.append(argNames[i]);
//			if (i < paramTypes.length - 1) {
//                result.append(", ");
//            }
//		}
//		result.append(')');
//
//		return result.toString();
//	}
//
//	//returns source wrapper for given method or null
//	private static JavaMethod getJavaMethod(Method m) {
//		return getJavaMethod(m.getDeclaringClass().getName(), m.getName(), m.getParameterTypes());
//	}
//
//	//returns source wrapper for given constructor or null
//	private static JavaMethod getJavaMethod(Constructor<?> c) {
//		return getJavaMethod(c.getDeclaringClass().getName(), c.getDeclaringClass().getSimpleName(), c.getParameterTypes());
//	}
//
//	private static JavaMethod getJavaMethod(String className, String methodName, Class<?>[] paramTypes) {
//		JavaMethod result = null;
//
//		JavaClass sourceClass = ourBuilder.getClassByName(className);
//		List<JavaMethod> sourceMethods = sourceClass.getMethods();
//
//		for (int i = 0; i < sourceMethods.size() && result == null; i++) {
//			List<JavaParameter> sourceParams = sourceMethods.get(i).getParameters();
//			if (sourceMethods.get(i).getName().equals(methodName) && sourceParams.size() == paramTypes.length) {
//				boolean matches = true;
//				for (int j = 0; j < sourceParams.size() && matches; j++) {
//					String typeName = ClassUtils.getName(paramTypes[j]);
//					String sourceTypeName = sourceParams.get(j).getType().toString();
//					if (!sourceTypeName.equals(typeName)) {
//						matches = false;
//					}
//				}
//
//				if (matches) {
//					result = sourceMethods.get(i);
//				}
//			}
//		}
//
//		return result;
//	}
//
//	/**
//	 * @param html Some text
//	 * @return The same text with HTML tags removed
//	 */
//	public static String removeTags(String html) {
//		if (html == null) {
//			return null;
//		} else {
//			return html.replaceAll("<\\\\p>", "\r\n\r\n").replaceAll("<br>", "\r\n").replaceAll("<.+?>", "");
//		}
//	}
//
//}
