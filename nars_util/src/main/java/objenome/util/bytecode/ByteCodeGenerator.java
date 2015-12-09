/**
 * Copyright (C) 2009 Future Invent Informationsmanagement GmbH. All rights
 * reserved. <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package objenome.util.bytecode;

import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * Creates the byte code for a model class and has some more helper methods.
 */
public final class ByteCodeGenerator {

    private final ClassPool pool;

    private final ClassLoader classLoader;

    private final ProtectionDomain domain;

    /**
     * Default constructor that uses <code>ClassPool.getDefault()</code>,
     * default class loader and default domain.
     */
    public ByteCodeGenerator() {
        this(null, null, null);
    }

    /**
     * Constructor with user defined class pool, default class loader and
     * default protection domain.
     * 
     * @param pool
     *            Class pool to use - If it's null
     *            <code>ClassPool.getDefault()</code>
     */
    public ByteCodeGenerator(ClassPool pool) {
        this(pool, null, null);
    }

    /**
     * Constructor with user defined class pool, class loader and default
     * protection domain.
     * 
     * @param pool
     *            Class pool to use - If it's null
     *            <code>ClassPool.getDefault()</code>
     * @param classLoader
     *            The class loader used to load classes. If it is null, the
     *            class loader returned by
     *            <code>ClassPool.getClassLoader()</code> is used.
     */
    public ByteCodeGenerator(ClassPool pool, ClassLoader classLoader) {
        this(pool, classLoader, null);
    }

    /**
     * Constructor with user defined class pool, class loader and domain.
     * 
     * @param pool
     *            Class pool to use - If it's null
     *            <code>ClassPool.getDefault()</code>
     * @param classLoader
     *            The class loader used to load classes. If it is null, the
     *            class loader returned by
     *            <code>ClassPool.getClassLoader()</code> is used.
     * @param domain
     *            The protection domain that classes belong to. If it is null,
     *            the default domain created by
     *            <code>java.lang.ClassLoader</code> is used.
     */
    public ByteCodeGenerator(ClassPool pool, ClassLoader classLoader,
                             ProtectionDomain domain) {
        this.pool = pool == null ? ClassPool.getDefault() : pool;
        this.classLoader = this.pool.getClassLoader();
        this.domain = null;
    }

    /**
     * Creates a Javassist class from a given model class.
     * 
     * @param modelClass
     *            Model class to convert into a Javassist class.
     * 
     * @return Javassist class.
     * 
     * @throws NotFoundException
     *             A class or interface from the model was not found.
     * @throws CannotCompileException
     *             Some source from the model cannot be compiled.
     */
    private CtClass createCtClass(SgClass modelClass) throws NotFoundException,
            CannotCompileException {

        // Create class
        CtClass clasz = pool.makeClass(modelClass.getName());
        clasz.setModifiers(SgUtils.toModifiers(modelClass.getModifiers()));

        // Add superclass
        if (modelClass.getSuperClass() != null) {
            clasz.setSuperclass(pool.get(modelClass.getSuperClass().getName()));
        }

        addInterfaces(modelClass, clasz);
        addFields(modelClass, clasz);
        addConstructors(modelClass, clasz);
        addMethods(modelClass, clasz);

        return clasz;
    }

    private void addMethods(SgClass modelClass, CtClass clasz)
            throws CannotCompileException, NotFoundException {
        List<SgMethod> methods = modelClass.getMethods();
        for (SgMethod method : methods) {

            // TODO Javassist cannot handle annotations
            String src = method.toString(false);
            CtMethod ctMethod = CtNewMethod.make(src, clasz);
            clasz.addMethod(ctMethod);

            // Add exceptions
            List<SgClass> exceptions = method.getExceptions();
            if (!exceptions.isEmpty()) {
                CtClass[] exceptionTypes = new CtClass[exceptions.size()];
                for (int j = 0; j < exceptions.size(); j++) {
                    exceptionTypes[j] = pool.get(exceptions.get(j).getName());
                }
                ctMethod.setExceptionTypes(exceptionTypes);
            }

        }
    }

    private void addConstructors(SgClass modelClass, CtClass clasz)
            throws CannotCompileException, NotFoundException {
        List<SgConstructor> constructors = modelClass.getConstructors();
        for (SgConstructor constructor : constructors) {

            String src = constructor.toString();
            CtConstructor ctConstructor = CtNewConstructor.make(src, clasz);
            clasz.addConstructor(ctConstructor);

            // Add exceptions
            List<SgClass> exceptions = constructor.getExceptions();
            if (!exceptions.isEmpty()) {
                CtClass[] exceptionTypes = new CtClass[exceptions.size()];
                for (int j = 0; j < exceptions.size(); j++) {
                    exceptionTypes[j] = pool.get(exceptions.get(j).getName());
                }
                ctConstructor.setExceptionTypes(exceptionTypes);
            }

        }
    }

    private static void addFields(SgClass modelClass, CtClass clasz)
            throws CannotCompileException {
        List<SgField> fields = modelClass.getFields();
        for (SgField field : fields) {
            String src = field.toString();
            CtField ctField = CtField.make(src, clasz);
            clasz.addField(ctField);
        }
    }

    private void addInterfaces(SgClass modelClass, CtClass clasz)
            throws NotFoundException {
        List<SgClass> interfaces = modelClass.getInterfaces();
        if (!interfaces.isEmpty()) {
            for (SgClass intf : interfaces) {
                clasz.addInterface(pool.get(intf.getName()));
            }
        }
    }

    /**
     * Generates the byte code for a model class and returns it. The class will
     * only be created if it's not known (<code>Class.forName(..)</code> throws
     * a <code>ClassNotFoundException</code>). If it's known the class will be
     * returned instead of creating a new one.
     * 
     * @param modelClass
     *            Model class to create the byte code for.
     * 
     * @return Class.
     */
    @SuppressWarnings("unchecked")
    public Class createClass(SgClass modelClass) {
        // We don't want to create it if it already exists
        Class implClass = loadClass(modelClass);
        if (implClass == null) {
            try {
                // Create class
                CtClass clasz = createCtClass(modelClass);
                implClass = clasz.toClass(classLoader, domain);
            } catch (NotFoundException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }
        return implClass;
    }

    /**
     * Tries to load the model class calling <code>Class.forName(..)</code>.
     * 
     * @param modelClass
     *            Model class to lookup.
     * 
     * @return Class (if it already exists) or null if it's unknown.
     */
    @SuppressWarnings("unchecked")
    public static Class loadClass(SgClass modelClass) {
        Class implClass;
        try {
            implClass = Class.forName(modelClass.getName());
            // The class already exists!
        } catch (ClassNotFoundException e) {
            implClass = null;
        }
        return implClass;
    }

    /**
     * Creates an instance using the nor-arguments constructor and maps all
     * exceptions into <code>RuntimeException</code>.
     * 
     * @param clasz
     *            Class to create an instance for.
     * 
     * @return New instance.
     */
    @SuppressWarnings("unchecked")
    public Object createInstance(Class clasz) {
        return createInstance(clasz, new Class[] {}, new Object[] {});
    }

    /**
     * Creates an instance mapping all exceptions into
     * <code>RuntimeException</code>.
     * 
     * @param clasz
     *            Class to create an instance for.
     * @param argTypes
     *            Argument types of the constructor to use.
     * @param initArgs
     *            Argument values for the constructor.
     * 
     * @return New instance.
     */
    @SuppressWarnings("unchecked")
    public Object createInstance(Class clasz, Class[] argTypes,
                                 Object[] initArgs) {
        try {
            Constructor constructor = clasz.getConstructor(argTypes);
            return constructor.newInstance(initArgs);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * Creates an instance from a model class mapping all exceptions into
     * <code>RuntimeException</code>.
     * 
     * @param clasz
     *            Class to create an instance for.
     * @param argTypes
     *            Argument types of the constructor to use.
     * @param initArgs
     *            Argument values for the constructor.
     * 
     * @return New instance.
     */
    @SuppressWarnings("unchecked")
    public Object createInstance(SgClass clasz, Class[] argTypes,
                                 Object[] initArgs) {

        Class newClass = createClass(clasz);
        return createInstance(newClass, argTypes, initArgs);

    }

    /**
     * Creates an instance from a model class with it's no argument constructor
     * and maps all exceptions into <code>RuntimeException</code>.
     * 
     * @param clasz
     *            Class to create an instance for.
     * 
     * @return New instance.
     */
    @SuppressWarnings("unchecked")
    public Object createInstance(SgClass clasz) {

        Class newClass = createClass(clasz);
        return createInstance(newClass, new Class[] {}, new Object[] {});

    }

    /**
     * Creates a generator initialized with default class pool and the context
     * class loader of the current thread.
     * 
     * @return New byte code generator instance.
     */
    public static ByteCodeGenerator createWithCurrentThreadContextClassLoader() {
        ClassPool pool = ClassPool.getDefault();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        pool.appendClassPath(new LoaderClassPath(classLoader));
        return new ByteCodeGenerator(pool, classLoader);
    }

}
