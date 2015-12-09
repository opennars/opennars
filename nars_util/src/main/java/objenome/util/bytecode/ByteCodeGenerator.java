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
    public ByteCodeGenerator(final ClassPool pool) {
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
    public ByteCodeGenerator(final ClassPool pool, final ClassLoader classLoader) {
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
    public ByteCodeGenerator(final ClassPool pool, final ClassLoader classLoader,
            final ProtectionDomain domain) {
        super();
        if (pool == null) {
            this.pool = ClassPool.getDefault();
        } else {
            this.pool = pool;
        }
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
    private CtClass createCtClass(final SgClass modelClass) throws NotFoundException,
            CannotCompileException {

        // Create class
        final CtClass clasz = pool.makeClass(modelClass.getName());
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

    private void addMethods(final SgClass modelClass, final CtClass clasz)
            throws CannotCompileException, NotFoundException {
        final List<SgMethod> methods = modelClass.getMethods();
        for (int i = 0; i < methods.size(); i++) {

            final SgMethod method = methods.get(i);
            // TODO Javassist cannot handle annotations
            final String src = method.toString(false);
            final CtMethod ctMethod = CtNewMethod.make(src, clasz);
            clasz.addMethod(ctMethod);

            // Add exceptions
            final List<SgClass> exceptions = method.getExceptions();
            if (!exceptions.isEmpty()) {
                final CtClass[] exceptionTypes = new CtClass[exceptions.size()];
                for (int j = 0; j < exceptions.size(); j++) {
                    exceptionTypes[j] = pool.get(exceptions.get(j).getName());
                }
                ctMethod.setExceptionTypes(exceptionTypes);
            }

        }
    }

    private void addConstructors(final SgClass modelClass, final CtClass clasz)
            throws CannotCompileException, NotFoundException {
        final List<SgConstructor> constructors = modelClass.getConstructors();
        for (int i = 0; i < constructors.size(); i++) {

            final SgConstructor constructor = constructors.get(i);
            final String src = constructor.toString();
            final CtConstructor ctConstructor = CtNewConstructor.make(src, clasz);
            clasz.addConstructor(ctConstructor);

            // Add exceptions
            final List<SgClass> exceptions = constructor.getExceptions();
            if (!exceptions.isEmpty()) {
                final CtClass[] exceptionTypes = new CtClass[exceptions.size()];
                for (int j = 0; j < exceptions.size(); j++) {                    
                    exceptionTypes[j] = pool.get(exceptions.get(j).getName());
                }
                ctConstructor.setExceptionTypes(exceptionTypes);
            }

        }
    }

    private static void addFields(final SgClass modelClass, final CtClass clasz)
            throws CannotCompileException {
        final List<SgField> fields = modelClass.getFields();
        for (int i = 0; i < fields.size(); i++) {
            final SgField field = fields.get(i);
            final String src = field.toString();
            final CtField ctField = CtField.make(src, clasz);
            clasz.addField(ctField);
        }
    }

    private void addInterfaces(final SgClass modelClass, final CtClass clasz)
            throws NotFoundException {
        final List<SgClass> interfaces = modelClass.getInterfaces();
        if (!interfaces.isEmpty()) {
            for (int i = 0; i < interfaces.size(); i++) {
                final SgClass intf = interfaces.get(i);
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
    public final Class createClass(final SgClass modelClass) {
        // We don't want to create it if it already exists
        Class implClass = loadClass(modelClass);
        if (implClass == null) {
            try {
                // Create class
                final CtClass clasz = createCtClass(modelClass);
                implClass = clasz.toClass(classLoader, domain);
            } catch (final NotFoundException e) {
                throw new RuntimeException(e);
            } catch (final CannotCompileException e) {
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
    public static Class loadClass(final SgClass modelClass) {
        Class implClass;
        try {
            implClass = Class.forName(modelClass.getName());
            // The class already exists!
        } catch (final ClassNotFoundException e) {
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
    public final Object createInstance(final Class clasz) {
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
    public final Object createInstance(final Class clasz, final Class[] argTypes,
            final Object[] initArgs) {
        try {
            final Constructor constructor = clasz.getConstructor(argTypes);
            return constructor.newInstance(initArgs);
        } catch (final NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (final InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (final IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (final InvocationTargetException ex) {
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
    public final Object createInstance(final SgClass clasz, final Class[] argTypes,
            final Object[] initArgs) {

        final Class newClass = createClass(clasz);
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
    public final Object createInstance(final SgClass clasz) {

        final Class newClass = createClass(clasz);
        return createInstance(newClass, new Class[] {}, new Object[] {});

    }

    /**
     * Creates a generator initialized with default class pool and the context
     * class loader of the current thread.
     * 
     * @return New byte code generator instance.
     */
    public static ByteCodeGenerator createWithCurrentThreadContextClassLoader() {
        final ClassPool pool = ClassPool.getDefault();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        pool.appendClassPath(new LoaderClassPath(classLoader));
        return new ByteCodeGenerator(pool, classLoader);
    }

}
