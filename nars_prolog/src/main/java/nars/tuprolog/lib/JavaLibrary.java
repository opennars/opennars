/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog.lib;

import nars.nal.term.Term;
import nars.tuprolog.*;
import nars.tuprolog.util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 *
 * This class represents a tuProlog library enabling the interaction with the
 * Java environment from tuProlog.
 *
 * Works only with JDK 1.2 (because of setAccessible method)
 *
 * The most specific method algorithm used to find constructors / methods has
 * been inspired by the article "What Is Interactive Scripting?", by Michael
 * Travers Dr. Dobb's -- Software Tools for the Professional Programmer January
 * 2000 CMP Media Inc., a United News and Media Company
 *
 * Library/Theory Dependency: BasicLibrary
 *
 *
 *
 */
@SuppressWarnings("serial")
public class JavaLibrary extends Library {

    /**
     * java objects referenced by prolog terms (keys)
     */
    private Map<String, Object> currentObjects = new HashMap();
    /**
     * inverse map useful for implementation issue
     */
    private IdentityHashMap<Object, Struct> currentObjects_inverse = new IdentityHashMap<>();

    private Map<String, Object> staticObjects = new HashMap();
    private IdentityHashMap<Object, Struct> staticObjects_inverse = new IdentityHashMap<>();

    /**
     * progressive conter used to identify registered objects
     */
    private int id = 0;

    /**
     * @author Alessio Mercurio
     *
     * used to manage different classloaders.
     */
    private AbstractDynamicClassLoader dynamicLoader;

    /**
     * library theory
     */
    public JavaLibrary() {
        if (System.getProperty("java.vm.name").equals("Dalvik")) {
            dynamicLoader = new AndroidDynamicClassLoader(new URL[]{}, getClass().getClassLoader());
        } else {
            dynamicLoader = new JavaDynamicClassLoader(new URL[]{}, getClass().getClassLoader());
        }
    }

    public String getTheory() {
        return //
                // operators defined by the JavaLibrary theory
                //
                ":- op(800,xfx,'<-').\n"
                        + ":- op(850,xfx,'returns').\n"
                        + ":- op(200,xfx,'as').\n"
                        + ":- op(600,xfx,'.'). \n"
                        + //
                        // flags defined by the JavaLibrary theory
                        //
                        // ":- flag(java_object_backtrackable,[true,false],false,true).\n"
                        // +
                        //
                        //
                        // "java_object(ClassName,Args,Id):- current_prolog_flag(java_object_backtrackable,false),!,java_object_nb(ClassName,Args,Id).\n"
                        // +
                        // "java_object(ClassName,Args,Id):- !,java_object_bt(ClassName,Args,Id).\n"
                        // +
                        "java_object_bt(ClassName,Args,Id):- java_object(ClassName,Args,Id).\n"
                        + "java_object_bt(ClassName,Args,Id):- destroy_object(Id).\n"
                        //+ "class(Path,Class) <- What :- !, java_call(Path, Class, What, Res), Res \\== false.\n"
                        //+ "class(Path,Class) <- What returns Res :- !,  java_call(Path, Class, What, Res).\n"
                        + "Obj <- What :- java_call(Obj,What,Res), Res \\== false.\n"
                        + "Obj <- What returns Res :- java_call(Obj,What,Res).\n"
                        + "java_array_set(Array,Index,Object):- class('java.lang.reflect.Array') <- set(Array as 'java.lang.Object',Index,Object as 'java.lang.Object'), !.\n"
                        + "java_array_set(Array,Index,Object):- java_array_set_primitive(Array,Index,Object).\n"
                        + "java_array_get(Array,Index,Object):- class('java.lang.reflect.Array') <- get(Array as 'java.lang.Object',Index) returns Object,!.\n"
                        + "java_array_get(Array,Index,Object):- java_array_get_primitive(Array,Index,Object).\n"
                        + "java_array_length(Array,Length):- class('java.lang.reflect.Array') <- getLength(Array as 'java.lang.Object') returns Length.\n"
                        + "java_object_string(Object,String):- Object <- toString returns String.    \n"
                        + // java_catch/3
                        "java_catch(JavaGoal, List, Finally) :- call(JavaGoal), call(Finally).\n";
    }

    public void dismiss() {
        currentObjects.clear();
        currentObjects_inverse.clear();
    }

    public void dismissAll() {
        currentObjects.clear();
        currentObjects_inverse.clear();
        staticObjects.clear();
        staticObjects_inverse.clear();
    }

    public void onSolveBegin(Term goal) {
        // id = 0;
        currentObjects.clear();
        currentObjects_inverse.clear();
        Iterator<Map.Entry<Object, Struct>> it = staticObjects_inverse.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Struct> en = it.next();
            bindDynamicObject(en.getValue(), en.getKey());
        }
        preregisterObjects();
    }

    public void onSolveEnd() {
    }

    /**
     * objects actually pre-registered in order to be available since the
     * beginning of demonstration
     */
    protected void preregisterObjects() {
        try {
            bindDynamicObject(new Struct("stdout"), System.out);
            bindDynamicObject(new Struct("stderr"), System.err);
            bindDynamicObject(new Struct("runtime"), Runtime.getRuntime());
            bindDynamicObject(new Struct("current_thread"), Thread.currentThread());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------------
    /**
     * @author Michele Mannino
     *
     * Creates of a java object - not backtrackable case
     *
     * @throws JavaException
     */
    public boolean java_object_3(Term className, Term argl, Term id)
            throws JavaException {
        className = className.getTerm();
        Struct arg = (Struct) argl.getTerm();
        id = id.getTerm();
        try {
            if (!className.isAtom()) {
                throw new JavaException(new ClassNotFoundException(
                        "Java class not found: " + className));
            }
            String clName = ((Struct) className).getName();
            // check for array type
            if (clName.endsWith("[]")) {
                Object[] list = getArrayFromList(arg);
                int nargs = ((PNum) list[0]).intValue();
                if (java_array(clName, nargs, id)) {
                    return true;
                } else {
                    throw new JavaException(new Exception());
                }
            }
            Signature args = parseArg(getArrayFromList(arg));
            if (args == null) {
                throw new IllegalArgumentException(
                        "Illegal constructor arguments  " + arg);
            }
            // object creation with argument described in args
            try {
                Class<?> cl = Class.forName(clName, true, dynamicLoader);
                Object[] args_value = args.getValues();
                //
                // Constructor co=cl.getConstructor(args.getTypes());
                Constructor<?> co = lookupConstructor(cl, args.getTypes(),
                        args_value);
                //
                //
                if (co == null) {
                    getEngine().warn("Constructor not found: class " + clName);
                    throw new JavaException(new NoSuchMethodException(
                            "Constructor not found: class " + clName));
                }

                Object obj = co.newInstance(args_value);
                if (bindDynamicObject(id, obj)) {
                    return true;
                } else {
                    throw new JavaException(new Exception());
                }
            } catch (ClassNotFoundException ex) {
                getEngine().warn("Java class not found: " + clName);
                throw new JavaException(ex);
            } catch (InvocationTargetException ex) {
                getEngine().warn("Invalid constructor arguments.");
                throw new JavaException(ex);
            } catch (NoSuchMethodException ex) {
                getEngine().warn("Constructor not found: " + Arrays.toString(args.getTypes()));
                throw new JavaException(ex);
            } catch (InstantiationException ex) {
                getEngine().warn(
                        "Objects of class " + clName
                                + " cannot be instantiated");
                throw new JavaException(ex);
            } catch (IllegalArgumentException ex) {
                getEngine().warn("Illegal constructor arguments  " + args);
                throw new JavaException(ex);
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
            throw new JavaException(ex);
        }
    }


    /*
     * @author Michele Mannino
     * Creates of a java object - not backtrackable case
     * @param className The name of the class 
     * @oaram path The list of the paths where the class may be contained
     * @param argl The list of the arguments used by the constructor
     * @param id The name of the prolog term
     * @throws JavaException
     * 
     * Deprecated in 2.8, see the manual.
     * 
     public boolean java_object_4(Term className, Term argl, Term id, Term paths)
     throws JavaException {
     paths = paths.getTerm();
     try
     {
     if(!paths.isList())
     throw new IllegalArgumentException();
     String[] listOfPaths = getStringArrayFromStruct((Struct) paths);
     URL[] urls = getURLsFromStringArray(listOfPaths);
     // Update the list of paths of the URLClassLoader
     classLoader.addURLs(urls);
        	
     // Delegation to java_object_3 method used to load the class
     boolean result = java_object_3(className, argl, id);
        	
     // Reset the URLClassLoader at default configuration
     classLoader.removeURLs(urls);
        	
     return result;
     }catch(IllegalArgumentException e)
     {
     getEngine().warn("Illegal list of paths " + paths);
     throw new JavaException(e);
     }
     catch (Exception e) {
     throw new JavaException(e);
     }
     }
     */
    /**
     * Destroy the tlink to a java object - called not directly, but from
     * predicate java_object (as second choice, for backtracking)
     *
     * @throws JavaException
     */
    public boolean destroy_object_1(Term id) throws JavaException {
        id = id.getTerm();
        try {
            if (id instanceof PTerm && !((PTerm)id).isGround())
                return false;

            unregisterDynamic((Struct) id);

            return true;
        } catch (Exception ex) {
            // ex.printStackTrace();
            throw new JavaException(ex);
        }
    }

    /**
     * Creates of a java class
     *
     * @throws JavaException
     */
    public boolean java_class_4(Term clSource, Term clName, Term clPathes,
                                Term id) throws JavaException {
        Struct classSource = (Struct) clSource.getTerm();
        Struct className = (Struct) clName.getTerm();
        Struct classPathes = (Struct) clPathes.getTerm();
        id = id.getTerm();
        try {
            String fullClassName = Tools.removeApices(className
                    .toString());

            String fullClassPath = fullClassName.replace('.', '/');
            Iterator<? extends Term> it = classPathes.listIterator();
            String cp = "";
            while (it.hasNext()) {
                if (cp.length() > 0) {
                    cp += ';';
                }
                cp += Tools.removeApices(it.next()
                        .toString());
            }
            if (cp.length() > 0) {
                cp = " -classpath " + cp;
            }

            String text = Tools.removeApices(classSource.toString());

            // System.out.println("class source: "+text+
            // "\nid: "+id+
            // "\npath: "+fullClassPath);
            try {
                FileWriter file = new FileWriter(fullClassPath + ".java");
                file.write(text);
                file.close();
            } catch (IOException ex) {
                getEngine().warn("Compilation of java sources failed");
                getEngine().warn(
                        "(creation of " + fullClassPath + ".java fail failed)");
                throw new JavaException(ex);
            }
            String cmd = "javac " + cp + ' ' + fullClassPath + ".java";
            // System.out.println("EXEC: "+cmd);
            try {
                Process jc = Runtime.getRuntime().exec(cmd);
                int res = jc.waitFor();
                if (res != 0) {
                    getEngine().warn("Compilation of java sources failed");
                    getEngine().warn(
                            "(java compiler (javac) has stopped with errors)");
                    throw new IOException("Compilation of java sources failed");
                }
            } catch (IOException ex) {
                getEngine().warn("Compilation of java sources failed");
                getEngine().warn("(java compiler (javac) invocation failed)");
                throw new JavaException(ex);
            }
            try {
                Class<?> the_class;

                /**
                 * @author Alessio Mercurio
                 *
                 * On Dalvik VM we can only use the DexClassLoader.
                 */
                if (System.getProperty("java.vm.name").equals("Dalvik")) {
                    the_class = Class.forName(fullClassName, true, dynamicLoader);
                } else {
                    the_class = Class.forName(fullClassName, true, new ClassLoader());
                }

                if (bindDynamicObject(id, the_class)) {
                    return true;
                } else {
                    throw new JavaException(new Exception());
                }
            } catch (ClassNotFoundException ex) {
                getEngine().warn("Compilation of java sources failed");
                getEngine().warn(
                        "(Java Class compiled, but not created: "
                                + fullClassName + " )");
                throw new JavaException(ex);
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
            throw new JavaException(ex);
        }
    }

    /**
     *
     * Calls a method of a Java object
     *
     * @throws JavaException
     *
     */
    public boolean java_call_3(Term objId, Term method_name, Term idResult)
            throws JavaException {
        objId = objId.getTerm();
        idResult = idResult.getTerm();
        Struct method = (Struct) method_name.getTerm();
        Object obj = null;
        Signature args = null;
        String methodName = null;
        try {
            methodName = method.getName();
            // check for accessing field Obj.Field <- set/get(X)
            // in that case: objId is '.'(Obj, Field)

            if (!objId.isAtom()) {
                if (objId instanceof Var) {
                    throw new JavaException(new IllegalArgumentException(objId
                            .toString()));
                }
                Struct sel = (Struct) objId;
                if (sel.getName().equals(".") && sel.size() == 2
                        && method.size() == 1) {
                    if (methodName.equals("set")) {
                        return java_set(sel.getTerm(0), sel.getTerm(1), method
                                .getTerm(0));
                    } else if (methodName.equals("get")) {
                        return java_get(sel.getTerm(0), sel.getTerm(1), method
                                .getTerm(0));
                    }
                }
            }
            args = parseArg(method);
            // object and argument must be instantiated
            if (objId instanceof Var) {
                throw new JavaException(new IllegalArgumentException(objId
                        .toString()));
            }
            if (args == null) {
                throw new JavaException(new IllegalArgumentException());
            }
            // System.out.println(args);
            String objName = Tools.removeApices(objId.toString());
            obj = staticObjects.containsKey(objName) ? staticObjects.get(objName) : currentObjects.get(objName);
            Object res = null;

            if (obj != null) {
                Class<?> cl = obj.getClass();
                //
                //
                Object[] args_values = args.getValues();
                Method m = lookupMethod(cl, methodName, args.getTypes(),
                        args_values);
                //
                //
                if (m != null) {
                    try {
                        // works only with JDK 1.2, NOT in Sun Application
                        // Server!
                        // m.setAccessible(true);

                        res = m.invoke(obj, args_values);

                    } catch (IllegalAccessException ex) {
                        getEngine().warn(
                                "Method invocation failed: " + methodName
                                        + "( signature: " + args + " )");
                        // ex.printStackTrace();
                        throw new JavaException(ex);
                    }
                } else {
                    getEngine().warn(
                            "Method not found: " + methodName + "( signature: "
                                    + args + " )");
                    throw new JavaException(new NoSuchMethodException(
                            "Method not found: " + methodName + "( signature: "
                                    + args + " )"));
                }
            } else {
                if (objId.isCompound()) {
                    Struct id = (Struct) objId;

                    if (id.size() == 1 && id.getName().equals("class")) {
                        try {
                            String clName = Tools
                                    .removeApices(id.getTermX(0).toString());
                            Class<?> cl = Class.forName(clName, true, dynamicLoader);

//							Method m = cl.getMethod(methodName, args.getTypes());
                            Method m = InspectionUtils.searchForMethod(cl, methodName, args.getTypes());
                            m.setAccessible(true);
                            res = m.invoke(null, args.getValues());
                        } catch (ClassNotFoundException ex) {
                            // if not found even as a class id -> consider as a
                            // String object value
                            getEngine().warn("Unknown class.");
                            // ex.printStackTrace();
                            throw new JavaException(ex);
                        }
                    } /*
                     * Deprecated in 2.8, see the manual.
                     * 
                     else if (id.getArity() == 2 && id.getName().equals("class")) {

                     String clName = Tools.removeApices(((Struct) objId).getArg(1).toString());
                     Struct paths = (Struct) ((Struct) objId).getArg(0);

                     String[] listOfPaths = getStringArrayFromStruct((Struct) paths);

                     classLoader.addURLs(getURLsFromStringArray(listOfPaths));

                     try {
                     Class<?> cl = Class.forName(clName, true, classLoader);

                     //							Method m = cl.getMethod(methodName, args.getTypes());
                     Method m = InspectionUtils.searchForMethod(cl, methodName, args.getTypes());
                     m.setAccessible(true);
                     res = m.invoke(null, args.getValues());
                     } catch (ClassNotFoundException ex) {
                     // if not found even as a class id -> consider as a
                     // String object value
                     getEngine().warn("Unknown class.");
                     // ex.printStackTrace();
                     throw new JavaException(ex);
                     }
                     finally
                     {
                     // Pulisco il class loader (solo gli URL inseriti)
                     classLoader.removeURLs(getURLsFromStringArray(listOfPaths));
                     }
                     } 
                     */ else {
                        // the object is the string itself
                        Method m = java.lang.String.class.getMethod(methodName, args.getTypes());
                        m.setAccessible(true);
                        res = m.invoke(objName, args.getValues());
                    }
                } else {
                    // the object is the string itself
                    Method m = java.lang.String.class.getMethod(methodName,
                            args.getTypes());
                    m.setAccessible(true);
                    res = m.invoke(objName, args.getValues());
                }
            }
            if (parseResult(idResult, res)) {
                return true;
            } else {
                throw new JavaException(new Exception());
            }
        } catch (InvocationTargetException ex) {
            getEngine().warn(
                    "Method failed: " + methodName + " - ( signature: " + args
                            + " ) - Original Exception: "
                            + ex.getTargetException());
            // ex.printStackTrace();
            throw new JavaException(new IllegalArgumentException());
        } catch (NoSuchMethodException ex) {
            // ex.printStackTrace();
            getEngine().warn(
                    "Method not found: " + methodName + " - ( signature: "
                            + args + " )");
            throw new JavaException(ex);
        } catch (IllegalArgumentException ex) {
            // ex.printStackTrace();
            getEngine().warn(
                    "Invalid arguments " + args + " - ( method: " + methodName
                            + " )");
            // ex.printStackTrace();
            throw new JavaException(ex);
        } catch (Exception ex) {
            // ex.printStackTrace();
            getEngine()
                    .warn("Generic error in method invocation " + methodName);
            throw new JavaException(ex);
        }
    }

    /**
     * @author Michele Mannino
     *
     * Set global classpath
     *
     * @throws JavaException
     *
     */
    public boolean set_classpath_1(Term paths) throws JavaException {
        try {
            paths = paths.getTerm();
            if (!paths.isList()) {
                throw new IllegalArgumentException();
            }
            String[] listOfPaths = getStringArrayFromStruct((Struct) paths);
            dynamicLoader.removeAllURLs();
            dynamicLoader.addURLs(getURLsFromStringArray(listOfPaths));
            return true;
        } catch (IllegalArgumentException e) {
            getEngine().warn("Illegal list of paths " + paths);
            throw new JavaException(e);
        } catch (Exception e) {
            throw new JavaException(e);
        }
    }

    /**
     * @author Michele Mannino
     *
     * Get global classpath
     *
     * @throws JavaException
     *
     */
    public boolean get_classpath_1(Term paths) throws JavaException {
        try {
            paths = paths.getTerm();
            if (!(paths instanceof Var)) {
                throw new IllegalArgumentException();
            }
            URL[] urls = dynamicLoader.getURLs();
            String stringURLs = null;
            Term pathTerm = null;
            if (urls.length > 0) {
                stringURLs = "[";

                for (URL url : urls) {
                    File file = new File(java.net.URLDecoder.decode(url.getFile(), "UTF-8"));
                    stringURLs = stringURLs + '\'' + file.getPath() + "',";
                }

                stringURLs = stringURLs.substring(0, stringURLs.length() - 1);
                stringURLs += "]";
            } else {
                stringURLs = "[]";
            }
//        	pathTerm = orderPathList(Term.createTerm(stringURLs));
            pathTerm = PTerm.createTerm(stringURLs);
//        	if(paths.isList())
//          		paths = orderPathList(paths);
            return unify(paths, pathTerm);
        } catch (IllegalArgumentException e) {
            getEngine().warn("Illegal list of paths " + paths);
            throw new JavaException(e);
        } catch (Exception e) {
            throw new JavaException(e);
        }
    }

//	private Term orderPathList(Term path)
//	{
//		Term result = null;
//		if(path == null)
//			return result;
//		if(path.isList())
//		{
//			path = path.getTerm();
//			List<String> list = new ArrayList<String>();
//			Struct str = (Struct) path.getTerm();
//			if(!str.isEmptyList())
//			{
//				for (Iterator<?> iterator = str.listIterator();iterator.hasNext();) {
//					list.add(((Term)iterator.next()).toString());
//				}
//				Collections.sorted(list);
//				result = getStructFromStringList(list);
//			}
//			else
//				result = path;
//		}
//		return result;
//	}
//	
//	
//	private Struct getStructFromStringList(List<String> paths)
//	{
//		Term[] array = null;
//		if(paths == null)
//			return null;
//		array = new Term[paths.size()];
//		int i = 0;
//		for (String string : paths) {
//			array[i++] = Term.createTerm(string);
//		}
//		return new Struct(array);
//	}
    /**
     * set the field value of an object
     */
    private boolean java_set(Term objId, Term fieldTerm, Term what) {
        // System.out.println("SET "+objId+" "+fieldTerm+" "+what);
        what = what.getTerm();
        if (!fieldTerm.isAtom() || what instanceof Var) {
            return false;
        }
        String fieldName = ((Struct) fieldTerm).getName();
        Object obj = null;
        try {
            Class<?> cl = null;
            if (objId.isCompound() && ((Struct) objId).getName().equals("class")) {
                String clName = null;
//            	String[] listOfPaths = null;
                // Case: class(className)
                if (((Struct) objId).size() == 1) {
                    clName = Tools.removeApices(((Struct) objId).getTermX(0).toString());
                }

                /*
                 * Deprecated in 2.8, see the manual.
                 // Case: class(paths, className)
                 else if(((Struct) objId).getArity() == 2)
                 {
                 clName = Tools.removeApices(((Struct) objId)
                 .getArg(1).toString());
                 Struct paths = (Struct) ((Struct) objId).getArg(0);
                	
                 listOfPaths = getStringArrayFromStruct((Struct) paths);
                	
                 classLoader.addURLs(getURLsFromStringArray(listOfPaths));
                 }
                 */
                if (clName != null) {
                    try {
                        cl = Class.forName(clName, true, dynamicLoader);
                    } catch (ClassNotFoundException ex) {
                        getEngine().warn("Java class not found: " + clName);
                        return false;
                    } catch (Exception ex) {
                        getEngine().warn(
                                "Static field "
                                        + fieldName
                                        + " not found in class "
                                        + Tools
                                        .removeApices(((Struct) objId)
                                                .getTermX(0).toString()));
                        return false;
                    }
                    /*
                     * Deprecated in 2.8, see the manual.
                     finally{
                     if(((Struct) objId).getArity() == 2)
                     classLoader.removeURLs(getURLsFromStringArray(listOfPaths));
                     }
                     */
                }
            } else {
                String objName = Tools
                        .removeApices(objId.toString());
                obj = currentObjects.get(objName);
                if (obj != null) {
                    cl = obj.getClass();
                } else {
                    return false;
                }
            }

            // first check for primitive data field
            Field field = cl.getField(fieldName);
            if (what instanceof PNum) {
                PNum wn = (PNum) what;
                if (wn instanceof Int) {
                    field.setInt(obj, wn.intValue());
                } else if (wn instanceof nars.tuprolog.Double) {
                    field.setDouble(obj, wn.doubleValue());
                } else if (wn instanceof nars.tuprolog.Long) {
                    field.setLong(obj, wn.longValue());
                } else if (wn instanceof nars.tuprolog.Float) {
                    field.setFloat(obj, wn.floatValue());
                } else {
                    return false;
                }
            } else {
                String what_name = Tools.removeApices(what
                        .toString());
                Object obj2 = currentObjects.get(what_name);
                if (obj2 != null) {
                    field.set(obj, obj2);
                } else {
                    // consider value as a simple string
                    field.set(obj, what_name);
                }
            }
            return true;
        } catch (NoSuchFieldException ex) {
            getEngine().warn(
                    "Field " + fieldName + " not found in class " + objId);
            return false;
        } catch (Exception ex) {
            // ex.printStackTrace();
            return false;
        }
    }

    /*
     * get the value of the field
     */
    private boolean java_get(Term objId, Term fieldTerm, Term what) {
        // System.out.println("GET "+objId+" "+fieldTerm+" "+what);
        if (!fieldTerm.isAtom()) {
            return false;
        }
        String fieldName = ((Struct) fieldTerm).getName();
        Object obj = null;
        try {
            Class<?> cl = null;
            if (objId.isCompound() && ((Struct) objId).getName().equals("class")) {
                String clName = null;
//            	String[] listOfPaths = null;
                // Case: class(className)
                if (((Struct) objId).size() == 1) {
                    clName = Tools.removeApices(((Struct) objId)
                            .getTermX(0).toString());
                }
                /*
                 * Deprecated in 2.8, see the manual.
                 // Case: class(paths, className)
                 else if(((Struct) objId).getArity() == 2)
                 {
                 clName = Tools.removeApices(((Struct) objId)
                 .getArg(1).toString());
                 Struct paths = (Struct) ((Struct) objId).getArg(0);
                	
                 listOfPaths = getStringArrayFromStruct((Struct) paths);
                	
                 classLoader.addURLs(getURLsFromStringArray(listOfPaths));
                 }
                 */

                if (clName != null) {
                    try {
                        cl = Class.forName(clName, true, dynamicLoader);
                    } catch (ClassNotFoundException ex) {
                        getEngine().warn("Java class not found: " + clName);
                        return false;
                    } catch (Exception ex) {
                        getEngine().warn(
                                "Static field "
                                        + fieldName
                                        + " not found in class "
                                        + Tools
                                        .removeApices(((Struct) objId)
                                                .getTermX(0).toString()));
                        return false;
                    }
                    /*
                     * Deprecated in 2.8, see the manual.
                     finally{
                     if(((Struct) objId).getArity() == 2)
                     classLoader.removeURLs(getURLsFromStringArray(listOfPaths));
                     }
                     */
                }
            } else {
                String objName = Tools
                        .removeApices(objId.toString());
                obj = currentObjects.get(objName);
                if (obj == null) {
                    return false;
                }
                cl = obj.getClass();
            }

            Field field = cl.getField(fieldName);
            Class<?> fc = field.getType();
            // work only with JDK 1.2
            field.setAccessible(true);

            // first check for primitive types
            if (fc.equals(Integer.TYPE) || fc.equals(Byte.TYPE)) {
                int value = field.getInt(obj);
                return unify(what, new nars.tuprolog.Int(value));
            } else if (fc.equals(java.lang.Long.TYPE)) {
                long value = field.getLong(obj);
                return unify(what, new nars.tuprolog.Long(value));
            } else if (fc.equals(java.lang.Float.TYPE)) {
                float value = field.getFloat(obj);
                return unify(what, new nars.tuprolog.Float(value));
            } else if (fc.equals(java.lang.Double.TYPE)) {
                double value = field.getDouble(obj);
                return unify(what, new nars.tuprolog.Double(value));
            } else {
                // the field value is an object
                Object res = field.get(obj);
                return bindDynamicObject(what, res);
            }
            // } catch (ClassNotFoundException ex){
            // getEngine().warn("object of unknown class "+objId);
            // ex.printStackTrace();
            // return false;

        } catch (NoSuchFieldException ex) {
            getEngine().warn(
                    "Field " + fieldName + " not found in class " + objId);
            return false;
        } catch (Exception ex) {
            getEngine().warn("Generic error in accessing the field");
            // ex.printStackTrace();
            return false;
        }
    }

    public boolean java_array_set_primitive_3(Term obj_id, Term i, Term what)
            throws JavaException {
        Struct objId = (Struct) obj_id.getTerm();
        PNum index = (PNum) i.getTerm();
        what = what.getTerm();
        // System.out.println("SET "+objId+" "+fieldTerm+" "+what);
        Object obj = null;
        if (!index.isInteger()) {
            throw new JavaException(new IllegalArgumentException(index
                    .toString()));
        }
        try {
            Class<?> cl = null;
            String objName = Tools.removeApices(objId.toString());
            obj = currentObjects.get(objName);
            if (obj != null) {
                cl = obj.getClass();
            } else {
                throw new JavaException(new IllegalArgumentException(objId
                        .toString()));
            }

            if (!cl.isArray()) {
                throw new JavaException(new IllegalArgumentException(objId
                        .toString()));
            }
            String name = cl.toString();
            if (name.equals("class [I")) {
                if (!(what instanceof PNum)) {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
                byte v = (byte) ((PNum) what).intValue();
                Array.setInt(obj, index.intValue(), v);
            } else if (name.equals("class [D")) {
                if (!(what instanceof PNum)) {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
                double v = ((PNum) what).doubleValue();
                Array.setDouble(obj, index.intValue(), v);
            } else if (name.equals("class [F")) {
                if (!(what instanceof PNum)) {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
                float v = ((PNum) what).floatValue();
                Array.setFloat(obj, index.intValue(), v);
            } else if (name.equals("class [L")) {
                if (!(what instanceof PNum)) {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
                long v = ((PNum) what).longValue();
                Array.setFloat(obj, index.intValue(), v);
            } else if (name.equals("class [C")) {
                String s = what.toString();
                Array.setChar(obj, index.intValue(), s.charAt(0));
            } else if (name.equals("class [Z")) {
                String s = what.toString();
                if (s.equals("true")) {
                    Array.setBoolean(obj, index.intValue(), true);
                } else if (s.equals("false")) {
                    Array.setBoolean(obj, index.intValue(), false);
                } else {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
            } else if (name.equals("class [B")) {
                if (!(what instanceof PNum)) {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
                int v = ((PNum) what).intValue();
                Array.setByte(obj, index.intValue(), (byte) v);
            } else if (name.equals("class [S")) {
                if (!(what instanceof PNum)) {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
                short v = (short) ((PNum) what).intValue();
                Array.setShort(obj, index.intValue(), v);
            } else {
                throw new JavaException(new Exception());
            }
            return true;
        } catch (Exception ex) {
            // ex.printStackTrace();
            throw new JavaException(ex);
        }
    }

    public boolean java_array_get_primitive_3(Term obj_id, Term i, Term what)
            throws JavaException {
        Struct objId = (Struct) obj_id.getTerm();
        PNum index = (PNum) i.getTerm();
        what = what.getTerm();
        // System.out.println("SET "+objId+" "+fieldTerm+" "+what);
        Object obj = null;
        if (!index.isInteger()) {
            throw new JavaException(new IllegalArgumentException(index
                    .toString()));
        }
        try {
            Class<?> cl = null;
            String objName = Tools.removeApices(objId.toString());
            obj = currentObjects.get(objName);
            if (obj != null) {
                cl = obj.getClass();
            } else {
                throw new JavaException(new IllegalArgumentException(objId
                        .toString()));
            }

            if (!cl.isArray()) {
                throw new JavaException(new IllegalArgumentException(objId
                        .toString()));
            }
            String name = cl.toString();
            if (name.equals("class [I")) {
                Term value = new nars.tuprolog.Int(Array.getInt(obj, index
                        .intValue()));
                if (unify(what, value)) {
                    return true;
                } else {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
            } else if (name.equals("class [D")) {
                Term value = new nars.tuprolog.Double(Array.getDouble(obj,
                        index.intValue()));
                if (unify(what, value)) {
                    return true;
                } else {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
            } else if (name.equals("class [F")) {
                Term value = new nars.tuprolog.Float(Array.getFloat(obj, index
                        .intValue()));
                if (unify(what, value)) {
                    return true;
                } else {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
            } else if (name.equals("class [L")) {
                Term value = new nars.tuprolog.Long(Array.getLong(obj, index
                        .intValue()));
                if (unify(what, value)) {
                    return true;
                } else {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
            } else if (name.equals("class [C")) {
                Term value = new nars.tuprolog.Struct(Character.toString(Array.getChar(obj, index.intValue())));
                if (unify(what, value)) {
                    return true;
                } else {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
            } else if (name.equals("class [Z")) {
                boolean b = Array.getBoolean(obj, index.intValue());
                if (b) {
                    if (unify(what, PTerm.TRUE)) {
                        return true;
                    } else {
                        throw new JavaException(new IllegalArgumentException(
                                what.toString()));
                    }
                } else {
                    if (unify(what, PTerm.FALSE)) {
                        return true;
                    } else {
                        throw new JavaException(new IllegalArgumentException(
                                what.toString()));
                    }
                }
            } else if (name.equals("class [B")) {
                Term value = new nars.tuprolog.Int(Array.getByte(obj, index
                        .intValue()));
                if (unify(what, value)) {
                    return true;
                } else {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
            } else if (name.equals("class [S")) {
                Term value = new nars.tuprolog.Int(Array.getInt(obj, index
                        .intValue()));
                if (unify(what, value)) {
                    return true;
                } else {
                    throw new JavaException(new IllegalArgumentException(what
                            .toString()));
                }
            } else {
                throw new JavaException(new Exception());
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
            throw new JavaException(ex);
        }

    }

    private boolean java_array(String type, int nargs, Term id) {
        try {
            Object array = null;
            String obtype = type.substring(0, type.length() - 2);

            if (obtype.equals("boolean")) {
                array = new boolean[nargs];
            } else if (obtype.equals("byte")) {
                array = new byte[nargs];
            } else if (obtype.equals("char")) {
                array = new char[nargs];
            } else if (obtype.equals("short")) {
                array = new short[nargs];
            } else if (obtype.equals("int")) {
                array = new int[nargs];
            } else if (obtype.equals("long")) {
                array = new long[nargs];
            } else if (obtype.equals("float")) {
                array = new float[nargs];
            } else if (obtype.equals("double")) {
                array = new double[nargs];
            } else {
                Class<?> cl = Class.forName(obtype, true, dynamicLoader);
                array = Array.newInstance(cl, nargs);
            }
            return bindDynamicObject(id, array);
        } catch (Exception ex) {
            // ex.printStackTrace();
            return false;
        }
    }

    /**
     * Returns an URL array from a String array
     *
     * @throws JavaException
     */
    private static URL[] getURLsFromStringArray(String[] paths) throws MalformedURLException {
        URL[] urls = null;
        if (paths != null) {
            urls = new URL[paths.length];

            for (int i = 0; i < paths.length; i++) {
                if (paths[i] == null) {
                    continue;
                }
                if (paths[i].contains("http") || paths[i].contains("https") || paths[i].contains("ftp")) {
                    urls[i] = new URL(paths[i]);
                } else {
                    File file = new File(paths[i]);
                    urls[i] = (file.toURI().toURL());
                }
            }
        }
        return urls;
    }

    /**
     * Returns a String array from a Struct contains a list
     *
     * @throws JavaException
     */
    private static String[] getStringArrayFromStruct(Struct list) {
        String args[] = new String[list.listSize()];
        Iterator<? extends Term> it = list.listIterator();
        int count = 0;
        while (it.hasNext()) {
            String path = Tools.removeApices(it.next().toString());
            args[count++] = path;
        }
        return args;
    }

    /**
     * creation of method signature from prolog data
     */
    private Signature parseArg(Struct method) {
        Object[] values = new Object[method.size()];
        Class<?>[] types = new Class[method.size()];
        for (int i = 0; i < method.size(); i++) {
            if (!parse_arg(values, types, i, method.getTerm(i))) {
                return null;
            }
        }
        return new Signature(values, types);
    }

    private Signature parseArg(Object[] objs) {
        Object[] values = new Object[objs.length];
        Class<?>[] types = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            if (!parse_arg(values, types, i, (Term) objs[i])) {
                return null;
            }
        }
        return new Signature(values, types);
    }

    private boolean parse_arg(Object[] values, Class<?>[] types, int i, Term term) {
        try {
            if (term == null) {
                values[i] = null;
                types[i] = null;
            } else if (term.isAtom()) {
                String name = Tools.removeApices(term.toString());
                if (name.equals("true")) {
                    values[i] = Boolean.TRUE;
                    types[i] = Boolean.TYPE;
                } else if (name.equals("false")) {
                    values[i] = Boolean.FALSE;
                    types[i] = Boolean.TYPE;
                } else {
                    Object obj = currentObjects.get(name);
                    if (obj == null) {
                        values[i] = name;
                    } else {
                        values[i] = obj;
                    }
                    types[i] = values[i].getClass();
                }
            } else if (term instanceof PNum) {
                PNum t = (PNum) term;
                if (t instanceof Int) {
                    values[i] = t.intValue();
                    types[i] = java.lang.Integer.TYPE;
                } else if (t instanceof nars.tuprolog.Double) {
                    values[i] = t.doubleValue();
                    types[i] = java.lang.Double.TYPE;
                } else if (t instanceof nars.tuprolog.Long) {
                    values[i] = t.longValue();
                    types[i] = java.lang.Long.TYPE;
                } else if (t instanceof nars.tuprolog.Float) {
                    values[i] = t.floatValue();
                    types[i] = java.lang.Float.TYPE;
                }
            } else if (term instanceof Struct) {
                // argument descriptors
                Struct tc = (Struct) term;
                if (tc.getName().equals("as")) {
                    return parse_as(values, types, i, tc.getTerm(0), tc
                            .getTerm(1));
                } else {
                    Object obj = currentObjects.get(Tools
                            .removeApices(tc.toString()));
                    if (obj == null) {
                        values[i] = Tools
                                .removeApices(tc.toString());
                    } else {
                        values[i] = obj;
                    }
                    types[i] = values[i].getClass();
                }
            } else if (term instanceof Var && !((Var) term).isBound()) {
                values[i] = null;
                types[i] = Object.class;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     *
     * parsing 'as' operate, which makes it possible to define the specific
     * class of an argument
     *
     */
    private boolean parse_as(Object[] values, Class<?>[] types, int i,
                             Term castWhat, Term castTo) {
        try {
            if (!(castWhat instanceof PNum)) {
                String castTo_name = Tools
                        .removeApices(((Struct) castTo).getName());
                String castWhat_name = Tools.removeApices(castWhat
                        .getTerm().toString());
                // System.out.println(castWhat_name+" "+castTo_name);
                if (castTo_name.equals("java.lang.String")
                        && castWhat_name.equals("true")) {
                    values[i] = "true";
                    types[i] = String.class;
                    return true;
                } else if (castTo_name.equals("java.lang.String")
                        && castWhat_name.equals("false")) {
                    values[i] = "false";
                    types[i] = String.class;
                    return true;
                } else if (castTo_name.endsWith("[]")) {
                    if (castTo_name.equals("boolean[]")) {
                        castTo_name = "[Z";
                    } else if (castTo_name.equals("byte[]")) {
                        castTo_name = "[B";
                    } else if (castTo_name.equals("short[]")) {
                        castTo_name = "[S";
                    } else if (castTo_name.equals("char[]")) {
                        castTo_name = "[C";
                    } else if (castTo_name.equals("int[]")) {
                        castTo_name = "[I";
                    } else if (castTo_name.equals("long[]")) {
                        castTo_name = "[L";
                    } else if (castTo_name.equals("float[]")) {
                        castTo_name = "[F";
                    } else if (castTo_name.equals("double[]")) {
                        castTo_name = "[D";
                    } else {
                        castTo_name = "[L"
                                + castTo_name.substring(0,
                                castTo_name.length() - 2) + ';';
                    }
                }
                if (!castWhat_name.equals("null")) {
                    Object obj_to_cast = currentObjects.get(castWhat_name);
                    if (obj_to_cast == null) {
                        if (castTo_name.equals("boolean")) {
                            if (castWhat_name.equals("true")) {
                                values[i] = true;
                            } else if (castWhat_name.equals("false")) {
                                values[i] = false;
                            } else {
                                return false;
                            }
                            types[i] = Boolean.TYPE;
                        } else {
                            // conversion to array
                            return false;
                        }
                    } else {
                        values[i] = obj_to_cast;
                        try {
                            types[i] = Class.forName(castTo_name, true, dynamicLoader);
                        } catch (ClassNotFoundException ex) {
                            getEngine().warn(
                                    "Java class not found: " + castTo_name);
                            return false;
                        }
                    }
                } else {
                    values[i] = null;
                    if (castTo_name.equals("byte")) {
                        types[i] = Byte.TYPE;
                    } else if (castTo_name.equals("short")) {
                        types[i] = Short.TYPE;
                    } else if (castTo_name.equals("char")) {
                        types[i] = Character.TYPE;
                    } else if (castTo_name.equals("int")) {
                        types[i] = java.lang.Integer.TYPE;
                    } else if (castTo_name.equals("long")) {
                        types[i] = java.lang.Long.TYPE;
                    } else if (castTo_name.equals("float")) {
                        types[i] = java.lang.Float.TYPE;
                    } else if (castTo_name.equals("double")) {
                        types[i] = java.lang.Double.TYPE;
                    } else if (castTo_name.equals("boolean")) {
                        types[i] = java.lang.Boolean.TYPE;
                    } else {
                        try {
                            types[i] = Class.forName(castTo_name, true, dynamicLoader);
                        } catch (ClassNotFoundException ex) {
                            getEngine().warn(
                                    "Java class not found: " + castTo_name);
                            return false;
                        }
                    }
                }
            } else {
                PNum num = (PNum) castWhat;
                String castTo_name = ((Struct) castTo).getName();
                if (castTo_name.equals("byte")) {
                    values[i] = (byte) num.intValue();
                    types[i] = Byte.TYPE;
                } else if (castTo_name.equals("short")) {
                    values[i] = (short) num.intValue();
                    types[i] = Short.TYPE;
                } else if (castTo_name.equals("int")) {
                    values[i] = num.intValue();
                    types[i] = Integer.TYPE;
                } else if (castTo_name.equals("long")) {
                    values[i] = num.longValue();
                    types[i] = java.lang.Long.TYPE;
                } else if (castTo_name.equals("float")) {
                    values[i] = num.floatValue();
                    types[i] = java.lang.Float.TYPE;
                } else if (castTo_name.equals("double")) {
                    values[i] = num.doubleValue();
                    types[i] = java.lang.Double.TYPE;
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            getEngine().warn(
                    "Casting " + castWhat + " to " + castTo + " failed");
            return false;
        }
        return true;
    }

    /**
     * parses return value of a method invokation
     */
    private boolean parseResult(Term id, Object obj) {
        if (obj == null) {
            // return unify(id,Term.TRUE);
            return unify(id, new Var());
        }
        try {
            if (Boolean.class.isInstance(obj)) {
                if ((Boolean) obj) {
                    return unify(id, PTerm.TRUE);
                } else {
                    return unify(id, PTerm.FALSE);
                }
            } else if (Byte.class.isInstance(obj)) {
                return unify(id, new Int(((Byte) obj).intValue()));
            } else if (Short.class.isInstance(obj)) {
                return unify(id, new Int(((Short) obj).intValue()));
            } else if (Integer.class.isInstance(obj)) {
                return unify(id, new Int((Integer) obj));
            } else if (java.lang.Long.class.isInstance(obj)) {
                return unify(id, new nars.tuprolog.Long((java.lang.Long) obj));
            } else if (java.lang.Float.class.isInstance(obj)) {
                return unify(id, new nars.tuprolog.Float(
                        (java.lang.Float) obj));
            } else if (java.lang.Double.class.isInstance(obj)) {
                return unify(id, new nars.tuprolog.Double(
                        (java.lang.Double) obj));
            } else if (String.class.isInstance(obj)) {
                return unify(id, new Struct((String) obj));
            } else if (Character.class.isInstance(obj)) {
                return unify(id, new Struct(obj.toString()));
            } else {
                return bindDynamicObject(id, obj);
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
            return false;
        }
    }

    private static Object[] getArrayFromList(Struct list) {
        Object args[] = new Object[list.listSize()];
        Iterator<? extends Term> it = list.listIterator();
        int count = 0;
        while (it.hasNext()) {
            args[count++] = it.next();
        }
        return args;
    }

    /**
     * Register an object with the specified id. The life-time of the tlink to
     * the object is engine life-time, available besides the individual query.
     *
     *
     * @param id object identifier
     * @param obj the object
     * @return true if the operation is successful
     * @throws InvalidObjectIdException if the object id is not valid
     */
    public boolean register(Struct id, Object obj)
            throws InvalidObjectIdException {
        /*
         * note that this method act on the staticObject and
         * staticObject_inverse hashmaps
         */
        if (!id.isGround()) {
            throw new InvalidObjectIdException();
        }
        // already registered object?
        synchronized (staticObjects) {
            Object aKey = staticObjects_inverse.get(obj);

            if (aKey != null) {
                // object already referenced
                return false;
            } else {
                String raw_name = Tools.removeApices(id.getTerm()
                        .toString());
                staticObjects.put(raw_name, obj);
                staticObjects_inverse.put(obj, id);
                return true;
            }
        }
    }

    /**
     * Register an object with the specified id. The life-time of the tlink to
     * the object is engine life-time, available besides the individual query.
     *
     * The identifier must be a ground object.
     *
     * @param id object identifier
     *
     * @return true if the operation is successful
     * @throws JavaException if the object id is not valid
     */
    public boolean register_1(Term id) throws JavaException {
        id = id.getTerm();
        Object obj = null;
        try {
            obj = getRegisteredDynamicObject((Struct) id);
            return register((Struct) id, obj);
        } catch (InvalidObjectIdException e) {
            getEngine().warn("Illegal object id " + id.toString());
            throw new JavaException(e);
        }
    }

    /**
     * Unregister an object with the specified id.
     *
     * The identifier must be a ground object.
     *
     * @param id object identifier
     *
     * @return true if the operation is successful
     * @throws JavaException if the object id is not valid
     */
    public boolean unregister_1(Term id) throws JavaException {
        id = id.getTerm();
        try {
            return unregister((Struct) id);
        } catch (InvalidObjectIdException e) {
            getEngine().warn("Illegal object id " + id.toString());
            throw new JavaException(e);
        }
    }

    /**
     * Registers an object, with automatic creation of the identifier.
     *
     * If the object is already registered, its identifier is returned
     *
     * @param obj object to be registered.
     * @return fresh id
     */
    public Struct register(Object obj) {
        // System.out.println("lib: "+this+" current id: "+this.id);

        // already registered object?
        synchronized (staticObjects) {
            Object aKey = staticObjects_inverse.get(obj);
            if (aKey != null) {
                // object already referenced -> unifying terms
                // referencing the object
                // log("obj already registered: unify "+id+" "+aKey);
                return (Struct) aKey;
            } else {
                Struct id = generateFreshId();
                staticObjects.put(id.getName(), obj);
                staticObjects_inverse.put(obj, id);
                return id;
            }
        }
    }

    /**
     * Gets the reference to an object previously registered
     *
     * @param id object id
     * @return the object, if present
     * @throws InvalidObjectIdException
     */
    public Object getRegisteredObject(Struct id)
            throws InvalidObjectIdException {
        if (!id.isGround()) {
            throw new InvalidObjectIdException();
        }
        synchronized (staticObjects) {
            return staticObjects.get(Tools.removeApices(id
                    .toString()));
        }
    }

    /**
     * Unregisters an object, given its identifier
     *
     *
     * @param id object identifier
     * @return true if the operation is successful
     * @throws InvalidObjectIdException if the id is not valid (e.g. is not
     * ground)
     */
    public boolean unregister(Struct id) throws InvalidObjectIdException {
        if (!id.isGround()) {
            throw new InvalidObjectIdException();
        }
        synchronized (staticObjects) {
            String raw_name = Tools.removeApices(id.toString());
            Object obj = staticObjects.remove(raw_name);
            if (obj != null) {
                staticObjects_inverse.remove(obj);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Registers an object only for the running query life-time
     *
     * @param id object identifier
     * @param obj object
     */
    public void registerDynamic(Struct id, Object obj) {
        synchronized (currentObjects) {
            String raw_name = Tools.removeApices(id.toString());
            currentObjects.put(raw_name, obj);
            currentObjects_inverse.put(obj, id);
        }
    }

    /**
     * Registers an object for the query life-time, with the automatic
     * generation of the identifier.
     *
     * If the object is already registered, its identifier is returned
     *
     * @param obj object to be registered
     * @return identifier
     */
    public Struct registerDynamic(Object obj) {
        // System.out.println("lib: "+this+" current id: "+this.id);

        // already registered object?
        synchronized (currentObjects) {
            Object aKey = currentObjects_inverse.get(obj);
            if (aKey != null) {
                // object already referenced -> unifying terms
                // referencing the object
                // log("obj already registered: unify "+id+" "+aKey);
                return (Struct) aKey;
            } else {
                Struct id = generateFreshId();
                currentObjects.put(id.getName(), obj);
                currentObjects_inverse.put(obj, id);
                return id;
            }
        }
    }

    /**
     * Gets a registered dynamic object (returns null if not presents)
     */
    public Object getRegisteredDynamicObject(Struct id)
            throws InvalidObjectIdException {
        if (!id.isGround()) {
            throw new InvalidObjectIdException();
        }
        synchronized (currentObjects) {
            return currentObjects.get(Tools.removeApices(id
                    .toString()));
        }
    }

    /**
     * Unregister the object, only for dynamic case
     *
     * @param id object identifier
     * @return true if the operation is successful
     */
    public boolean unregisterDynamic(Struct id) {
        synchronized (currentObjects) {
            String raw_name = Tools.removeApices(id.toString());
            Object obj = currentObjects.remove(raw_name);
            if (obj != null) {
                currentObjects_inverse.remove(obj);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Tries to bind specified id to a provided java object.
     *
     * Term id can be a variable or a ground term.
     */
    protected boolean bindDynamicObject(Term id, Object obj) {
        // null object are considered to _ variable
        if (obj == null) {
            return unify(id, new Var());
        }
        // already registered object?
        synchronized (currentObjects) {
            Object aKey = currentObjects_inverse.get(obj);
            if (aKey != null) {
                // object already referenced -> unifying terms
                // referencing the object
                // log("obj already registered: unify "+id+" "+aKey);
                return unify(id, (Term) aKey);
            } else {
                // object not previously referenced
                if (id instanceof Var) {
                    // get a ground term
                    Struct idTerm = generateFreshId();
                    unify(id, idTerm);
                    registerDynamic(idTerm, obj);
                    // log("not ground id for a new obj: "+id+" as ref for "+obj);
                    return true;
                } else {
                    // verify of the id is already used
                    String raw_name = Tools.removeApices(id
                            .getTerm().toString());
                    Object linkedobj = currentObjects.get(raw_name);
                    if (linkedobj == null) {
                        registerDynamic((Struct) (id.getTerm()), obj);
                        // log("ground id for a new obj: "+id+" as ref for "+obj);
                        return true;
                    } else {
                        // an object with the same id is already
                        // present: must be the same object
                        return obj == linkedobj;
                    }
                }
            }
        }
    }

    /**
     * Generates a fresh numeric identifier
     *
     * @return
     */
    protected Struct generateFreshId() {
        return new Struct("$obj_" + id++);
    }

    /**
     * handling writeObject method is necessary in order to make the library
     * serializable, 'nullyfing' eventually objects registered in maps
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        Map<String, Object> bak00 = currentObjects;
        IdentityHashMap<Object, Struct> bak01 = currentObjects_inverse;
        try {
            currentObjects = null;
            currentObjects_inverse = null;
            out.defaultWriteObject();
        } catch (IOException ex) {
            currentObjects = bak00;
            currentObjects_inverse = bak01;
            throw new IOException();
        }
        currentObjects = bak00;
        currentObjects_inverse = bak01;
    }

    /**
     * handling readObject method is necessary in order to have the library
     * reconstructed after a serialization
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        currentObjects = new HashMap<>();
        currentObjects_inverse = new IdentityHashMap<>();
        preregisterObjects();
    }

    // --------------------------------------------------
    private static Method lookupMethod(Class<?> target, String name,
                                       Class<?>[] argClasses, Object[] argValues)
            throws NoSuchMethodException {
        // first try for exact match
        try {
            Method m = target.getMethod(name, argClasses);
            return m;
        } catch (NoSuchMethodException e) {
            if (argClasses.length == 0) { // if no args & no exact match, out of
                // luck
                return null;
            }
        }

        // go the more complicated route
        Method[] methods = target.getMethods();
        Vector<Method> goodMethods = new Vector<>();
        for (int i = 0; i != methods.length; i++) {
            if (name.equals(methods[i].getName())
                    && matchClasses(methods[i].getParameterTypes(), argClasses)) {
                goodMethods.addElement(methods[i]);
            }
        }
        switch (goodMethods.size()) {
            case 0:
                // no methods have been found checking for assignability
                // and (int -> long) conversion. One last chance:
                // looking for compatible methods considering also
                // type conversions:
                // double --> float
                // (the first found is used - no most specific
                // method algorithm is applied )

                for (int i = 0; i != methods.length; i++) {
                    if (name.equals(methods[i].getName())) {
                        Class<?>[] types = methods[i].getParameterTypes();
                        Object[] val = matchClasses(types, argClasses, argValues);
                        if (val != null) {
                            // found a method compatible
                            // after type conversions
                            for (int j = 0; j < types.length; j++) {
                                argClasses[j] = types[j];
                                argValues[j] = val[j];
                            }
                            return methods[i];
                        }
                    }
                }

                return null;
            case 1:
                return goodMethods.firstElement();
            default:
                return mostSpecificMethod(goodMethods);
        }
    }

    private static Constructor<?> lookupConstructor(Class<?> target,
                                                    Class<?>[] argClasses, Object[] argValues)
            throws NoSuchMethodException {
        // first try for exact match
        try {
            return target.getConstructor(argClasses);
        } catch (NoSuchMethodException e) {
            if (argClasses.length == 0) { // if no args & no exact match, out of
                // luck
                return null;
            }
        }

        // go the more complicated route
        Constructor<?>[] constructors = target.getConstructors();
        Vector<Constructor<?>> goodConstructors = new Vector<>();
        for (int i = 0; i != constructors.length; i++) {
            if (matchClasses(constructors[i].getParameterTypes(), argClasses)) {
                goodConstructors.addElement(constructors[i]);
            }
        }
        switch (goodConstructors.size()) {
            case 0:
                // no constructors have been found checking for assignability
                // and (int -> long) conversion. One last chance:
                // looking for compatible methods considering also
                // type conversions:
                // double --> float
                // (the first found is used - no most specific
                // method algorithm is applied )

                for (int i = 0; i != constructors.length; i++) {
                    Class<?>[] types = constructors[i].getParameterTypes();
                    Object[] val = matchClasses(types, argClasses, argValues);
                    if (val != null) {
                        // found a method compatible
                        // after type conversions
                        for (int j = 0; j < types.length; j++) {
                            argClasses[j] = types[j];
                            argValues[j] = val[j];
                        }
                        return constructors[i];
                    }
                }

                return null;
            case 1:
                return goodConstructors.firstElement();
            default:
                return mostSpecificConstructor(goodConstructors);
        }
    }

    // 1st arg is from method, 2nd is actual parameters
    private static boolean matchClasses(Class<?>[] mclasses, Class<?>[] pclasses) {
        if (mclasses.length == pclasses.length) {
            for (int i = 0; i != mclasses.length; i++) {
                if (!matchClass(mclasses[i], pclasses[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean matchClass(Class<?> mclass, Class<?> pclass) {
        boolean assignable = mclass.isAssignableFrom(pclass);
        if (assignable) {
            return true;
        } else {
            if (mclass.equals(java.lang.Long.TYPE)
                    && (pclass.equals(java.lang.Integer.TYPE))) {
                return true;
            }
        }
        return false;
    }

    private static Method mostSpecificMethod(Vector<Method> methods)
            throws NoSuchMethodException {
        for (int i = 0; i != methods.size(); i++) {
            for (int j = 0; j != methods.size(); j++) {
                if ((i != j)
                        && (moreSpecific(methods.elementAt(i),
                        methods.elementAt(j)))) {
                    methods.removeElementAt(j);
                    if (i > j) {
                        i--;
                    }
                    j--;
                }
            }
        }
        if (methods.size() == 1) {
            return methods.elementAt(0);
        } else {
            throw new NoSuchMethodException(">1 most specific method");
        }
    }

    // true if c1 is more specific than c2
    private static boolean moreSpecific(Method c1, Method c2) {
        Class<?>[] p1 = c1.getParameterTypes();
        Class<?>[] p2 = c2.getParameterTypes();
        int n = p1.length;
        for (int i = 0; i != n; i++) {
            if (!matchClass(p2[i], p1[i])) {
                return false;
            }
        }
        return true;
    }

    private static Constructor<?> mostSpecificConstructor(Vector<Constructor<?>> constructors)
            throws NoSuchMethodException {
        for (int i = 0; i != constructors.size(); i++) {
            for (int j = 0; j != constructors.size(); j++) {
                if ((i != j)
                        && (moreSpecific(constructors.elementAt(i), constructors.elementAt(j)))) {
                    constructors.removeElementAt(j);
                    if (i > j) {
                        i--;
                    }
                    j--;
                }
            }
        }
        if (constructors.size() == 1) {
            return constructors.elementAt(0);
        } else {
            throw new NoSuchMethodException(">1 most specific constructor");
        }
    }

    // true if c1 is more specific than c2
    private static boolean moreSpecific(Constructor<?> c1, Constructor<?> c2) {
        Class<?>[] p1 = c1.getParameterTypes();
        Class<?>[] p2 = c2.getParameterTypes();
        int n = p1.length;
        for (int i = 0; i != n; i++) {
            if (!matchClass(p2[i], p1[i])) {
                return false;
            }
        }
        return true;
    }

    // Checks compatibility also considering explicit type conversion.
    // The method returns the argument values, since they could be changed
    // after a type conversion.
    //
    // In particular the check must be done for the DEFAULT type of tuProlog,
    // that are int and double; so
    // (required X, provided a DEFAULT -
    // with DEFAULT to X conversion 'conceivable':
    // for instance *double* to *int* is NOT considered good
    //
    // required a float, provided an int OK
    // required a double, provided a int OK
    // required a long, provided a int ==> already considered by
    // previous match test
    // required a float, provided a double OK
    // required a int, provided a double => NOT CONSIDERED
    // required a long, provided a double => NOT CONSIDERED
    //
    private static Object[] matchClasses(Class<?>[] mclasses, Class<?>[] pclasses,
                                         Object[] values) {
        if (mclasses.length == pclasses.length) {
            Object[] newvalues = new Object[mclasses.length];

            for (int i = 0; i != mclasses.length; i++) {
                boolean assignable = mclasses[i].isAssignableFrom(pclasses[i]);
                if (assignable
                        || (mclasses[i].equals(java.lang.Long.TYPE) && pclasses[i]
                        .equals(java.lang.Integer.TYPE))) {
                    newvalues[i] = values[i];
                } else if (mclasses[i].equals(java.lang.Float.TYPE)
                        && pclasses[i].equals(java.lang.Double.TYPE)) {
                    // arg required: a float, arg provided: a double
                    // so we need an explicit conversion...
                    newvalues[i] = ((java.lang.Double) values[i]).floatValue();
                } else if (mclasses[i].equals(java.lang.Float.TYPE)
                        && pclasses[i].equals(java.lang.Integer.TYPE)) {
                    // arg required: a float, arg provided: an int
                    // so we need an explicit conversion...
                    newvalues[i] = (float) ((Integer) values[i]).intValue();
                } else if (mclasses[i].equals(java.lang.Double.TYPE)
                        && pclasses[i].equals(java.lang.Integer.TYPE)) {
                    // arg required: a double, arg provided: an int
                    // so we need an explicit conversion...
                    newvalues[i] = ((Integer) values[i]).doubleValue();
                } else if (values[i] == null && !mclasses[i].isPrimitive()) {
                    newvalues[i] = null;
                } else {
                    return null;
                }
            }
            return newvalues;
        } else {
            return null;
        }
    }

}

/**
 * Signature class mantains information about type and value of a method
 * arguments
 */
@SuppressWarnings("serial")
class Signature implements Serializable {

    Class<?>[] types;
    Object[] values;

    public Signature(Object[] v, Class<?>[] c) {
        values = v;
        types = c;
    }

    public Class<?>[] getTypes() {
        return types;
    }

    Object[] getValues() {
        return values;
    }

    public String toString() {
        String st = "";
        for (int i = 0; i < types.length; i++) {
            st = st + "\n  Argument " + i + " -  VALUE: " + values[i]
                    + " TYPE: " + types[i];
        }
        return st;
    }
}

/**
 * used to load new classes without touching system class loader
 */
class ClassLoader extends java.lang.ClassLoader {
}

/**
 * Information about an EventListener
 */
@SuppressWarnings("serial")
class ListenerInfo implements Serializable {

    public String listenerInterfaceName;
    public EventListener listener;
    // public String eventName;
    public String eventFullClass;

    public ListenerInfo(EventListener l, String eventClass, String n) {
        listener = l;
        // this.eventName=eventName;
        this.eventFullClass = eventClass;
        listenerInterfaceName = n;
    }
}
