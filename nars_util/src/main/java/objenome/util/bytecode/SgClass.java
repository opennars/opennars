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

import javassist.CtMethod;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class. TODO Add annotation handling
 */
public final class SgClass {

    /** Simple "void" type. */
    public static final SgClass VOID = new SgClass("", "", "void", false, null);

    /** Simple "boolean" type. */
    public static final SgClass BOOLEAN = new SgClass("", "", "boolean", false, null);

    /** Simple "byte" type. */
    public static final SgClass BYTE = new SgClass("", "", "byte", false, null);

    /** Simple "char" type. */
    public static final SgClass CHAR = new SgClass("", "", "char", false, null);

    /** Simple "short" type. */
    public static final SgClass SHORT = new SgClass("", "", "short", false, null);

    /** Simple "int" type. */
    public static final SgClass INT = new SgClass("", "", "int", false, null);

    /** Simple "long" type. */
    public static final SgClass LONG = new SgClass("", "", "long", false, null);

    /** Simple "float" type. */
    public static final SgClass FLOAT = new SgClass("", "", "float", false, null);

    /** Simple "double" type. */
    public static final SgClass DOUBLE = new SgClass("", "", "double", false, null);

    /** Base "Object" type. */
    public static final SgClass OBJECT = new SgClass("", "", "Object", false, null);

    private final String modifiers;

    private final String packageName;

    private final String simpleName;

    private final List<SgClass> interfaces;

    private final List<SgField> fields;

    private final boolean isinterface;

    private final SgClass superClass;

    private final List<SgConstructor> constructors;

    private final List<SgMethod> methods;

    private final List<SgClass> classes;

    private final SgClass enclosingClass;

    private final List<SgAnnotation> annotations;

    /**
     * Basic constructor.
     * 
     * @param packageName
     *            Name of the package - Cannot be null.
     * @param simpleName
     *            Name (without package) of the class - Cannot be null.
     */
    public SgClass(String packageName, String simpleName) {
        this("public", packageName, simpleName, null, false, null);
    }

    /**
     * Constructor without super class.
     * 
     * @param modifiers
     *            Modifier names separated with space - Cannot be null.
     * @param packageName
     *            Name of the package - Cannot be null.
     * @param simpleName
     *            Name (without package) of the class - Cannot be null.
     * @param isinterface
     *            Is this an interface?
     * @param enclosingClass
     *            Enclosing class if this is an inner class - Null is allowed.
     */
    public SgClass(String modifiers, String packageName, String simpleName,
                   boolean isinterface, SgClass enclosingClass) {
        this(modifiers, packageName, simpleName, null, isinterface, enclosingClass);
    }

    /**
     * Constructor with super class.
     * 
     * @param modifiers
     *            Modifier names separated with space - Cannot be null.
     * @param packageName
     *            Name of the package - Cannot be null.
     * @param simpleName
     *            Name (without package) of the class - Cannot be null.
     * @param superClass
     *            Super class or null.
     * @param isinterface
     *            Is this an interface?
     * @param enclosingClass
     *            Enclosing class if this is an inner class - Null is allowed
     */
    public SgClass(String modifiers, String packageName, String simpleName,
                   SgClass superClass, boolean isinterface, SgClass enclosingClass) {

        this.modifiers = modifiers;
        this.packageName = packageName;
        this.simpleName = simpleName;

        this.superClass = superClass;
        if (isinterface && (superClass != null)) {
            throw new IllegalArgumentException(
                    "This is an interface. You cannot set a super class. "
                            + "Use 'addInterface(SgClass)' instead!");
        }

        constructors = new ArrayList<>();
        methods = new ArrayList<>();
        interfaces = new ArrayList<>();
        fields = new ArrayList<>();
        this.isinterface = isinterface;
        classes = new ArrayList<>();
        annotations = new ArrayList<>();

        this.enclosingClass = enclosingClass;
        if (enclosingClass != null) {
            this.enclosingClass.addClass(this);
        }

    }

    /**
     * Returns the annotations for this class.
     * 
     * @return List of annotations - Always non-null and is unmodifiable
     */
    public List<SgAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    /**
     * Adds an annotation.
     * 
     * @param annotation
     *            Annotation to add - Cannot be null.
     */
    public void addAnnotation(SgAnnotation annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("The argument 'annotation' cannot be NULL!");
        }
        annotations.add(annotation);
    }

    /**
     * Adds a list of annotations. The internal list will not be cleared! The
     * annotations will simply be added with <code>addAll(..)</code>.
     * 
     * @param annotations
     *            Annotations to add - Cannot be null.
     */
    public void addAnnotations(List<SgAnnotation> annotations) {
        if (annotations == null) {
            throw new IllegalArgumentException("The argument 'annotations' cannot be NULL!");
        }
        this.annotations.addAll(annotations);
    }

    /**
     * Checks if a given annotation is in the list.
     * 
     * @param name
     *            Name of the annotation to find - Cannot be null.
     * 
     * @return If it's found true else false.
     */
    public boolean hasAnnotation(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be NULL!");
        }
        for (SgAnnotation annotation : annotations) {
            if (annotation.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the super class.
     * 
     * @return Super class or null.
     */
    public SgClass getSuperClass() {
        return superClass;
    }

    /**
     * Returns the simple name of the class with an "underscore" inserted before
     * all upper case characters and all characters converted to lower case.
     * 
     * @return Name usable as a package - Always non-null.
     */
    public String getSimpleNameAsPackage() {
        return SgUtils.uppercaseToUnderscore(getSimpleName());
    }

    /**
     * Returns a list of constructors.
     * 
     * @return Constructor list - Always non-null and is unmodifiable.
     */
    public List<SgConstructor> getConstructors() {
        return Collections.unmodifiableList(constructors);
    }

    /**
     * Adds a constructor to the class. Does nothing if the constructor is
     * already in the list of constructors. You will never need to use this
     * method in your code! A constructor is added automatically to the owning
     * class when it's constructed!
     * 
     * @param constructor
     *            Constructor to add - Cannot be null.
     */
    public void addConstructor(SgConstructor constructor) {
        if (constructor == null) {
            throw new IllegalArgumentException("The argument 'constructor' cannot be null!");
        }
        if (constructor.getOwner() != this) {
            throw new IllegalArgumentException(
                    "The owner of 'constructor' is different from 'this'!");
        }
        if (!constructors.contains(constructor)) {
            constructors.add(constructor);
        }
    }

    /**
     * Returns if this is an interface.
     * 
     * @return If it's an interface true else false.
     */
    public boolean isInterface() {
        return isinterface;
    }

    /**
     * Returns a list of all methods.
     * 
     * @return Method list - Always non-null and is unmodifiable.
     */
    public List<SgMethod> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    /**
     * Adds a method to the class. Does nothing if the method is already in the
     * list of methods. You will never need to use this method in your code! A
     * method is added automatically to the owning class when it's constructed!
     * 
     * @param method
     *            Method to add - Cannot be null.
     */
    public void addMethod(SgMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("The argument 'method' cannot be null!");
        }
        if (method.getOwner() != this) {
            throw new IllegalArgumentException("The owner of 'method' is different from 'this'!");
        }
        if (!methods.contains(method)) {
            methods.add(method);
        }
    }

    /**
     * Returns the name of the class.
     * 
     * @return Name with inner classes separated with "$".
     */
    public String getName() {
        return getName("$");
    }

    /**
     * Returns the name of the class for use in source codes.
     * 
     * @return Name with inner classes separated with ".".
     */
    public String getSourceName() {
        return getName(".");
    }

    /**
     * Returns the package and name as a filename without extension.
     * 
     * @return Relative path and filename.
     */
    public String getNameAsFilename() {
        return getName().replace('.', File.separatorChar);
    }

    /**
     * Returns the package and name as a source filename.
     * 
     * @return Relative path and filename with extension ".java".
     */
    public String getNameAsSrcFilename() {
        return getNameAsFilename() + ".java";
    }

    /**
     * Returns the package and name as a class filename.
     * 
     * @return Relative path and filename with extension ".class".
     */
    public String getNameAsBinFilename() {
        return getNameAsFilename() + ".class";
    }

    private String getEnclosingSimpleNames(String innerDivider) {
        StringBuilder sb = new StringBuilder();
        SgClass clasz = enclosingClass;
        while (clasz != null) {
            sb.insert(0, innerDivider);
            sb.insert(0, clasz.getSimpleName());
            clasz = clasz.getEnclosingClass();
        }
        return sb.toString();
    }

    private String getName(String innerDivider) {
        if (packageName.isEmpty()) {
            return enclosingClass == null ? simpleName : getEnclosingSimpleNames(innerDivider) + simpleName;
        }
        return enclosingClass == null ? packageName + '.' + simpleName : packageName + '.' + getEnclosingSimpleNames(innerDivider) + simpleName;
    }

    /**
     * Returns the package of the class.
     * 
     * @return Package name.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the simple name of the class.
     * 
     * @return Name without package.
     */
    public String getSimpleName() {
        return simpleName;
    }

    /**
     * Returns a list of all interfaces.
     * 
     * @return List of interfaces - Always non-null and unmodifiable.
     */
    public List<SgClass> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    /**
     * Add an interface to the class.
     * 
     * @param intf
     *            Interface to add - Cannot be null.
     */
    public void addInterface(SgClass intf) {
        if (intf == null) {
            throw new IllegalArgumentException("The argument 'intf' cannot be null!");
        }
        interfaces.add(intf);
    }

    /**
     * Returns all (fully qualified) interface names.
     * 
     * @return Names separated by a comma - Always non-null.
     */
    public String getInterfacesCommaSeparated() {
        StringBuilder sb = new StringBuilder();
        if (!interfaces.isEmpty()) {
            for (int i = 0; i < interfaces.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(interfaces.get(i).getSourceName());
            }
        }
        return sb.toString();
    }

    /**
     * Returns a list of all fields.
     * 
     * @return List of fields - Always non-null and is unmodifiable
     */
    public List<SgField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    /**
     * Adds a field to the class. Does nothing if the field is already in the
     * list of fields. You will never need to use this method in your code! A
     * field is added automatically to the owning class when it's constructed!
     * 
     * @param field
     *            Field to add - Cannot be null.
     */
    public void addField(SgField field) {
        if (field == null) {
            throw new IllegalArgumentException("The argument 'field' cannot be null!");
        }
        if (field.getOwner() != this) {
            throw new IllegalArgumentException("The owner of 'field' is different from 'this'!");
        }
        if (!fields.contains(field)) {
            fields.add(field);
        }
    }

    /**
     * Returns a list of all inner class.
     * 
     * @return List of inner classes - Always non-null and is unmodifiable.
     */
    public List<SgClass> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    /**
     * Adds an inner to this class. Does nothing if the class is already in the
     * list of inner classes.
     * 
     * @param clasz
     *            Inner class to add - Cannot be null.
     */
    public void addClass(SgClass clasz) {
        if (clasz == null) {
            throw new IllegalArgumentException("The argument 'clasz' cannot be null!");
        }
        if (!classes.contains(clasz)) {
            classes.add(clasz);
        }
    }

    /**
     * Returns the enclosing class.
     * 
     * @return Enclosing class or null.
     */
    public SgClass getEnclosingClass() {
        return enclosingClass;
    }

    /**
     * Find an inner class by it's name.
     * 
     * @param name
     *            Full qualified name of the class to find - Cannot be null.
     * 
     * @return Class or null if it's not found.
     */
    public SgClass findClassByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be null!");
        }
        for (SgClass clasz : classes) {
            if (clasz.getName().equals(name)) {
                return clasz;
            }
        }
        return null;
    }

    /**
     * Find a method by it's name.
     * 
     * @param name
     *            Name of the method to find - Cannot be null.
     * 
     * @return Method or null if it's not found.
     */
    public SgMethod findMethodByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be null!");
        }
        for (SgMethod method : methods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
    
    public SgMethod findMethod(CtMethod em) {
        String key = em.getLongName();
        for (SgMethod method : methods) {
            String key2 = getName() + '.' + method.getTypeSignature();
            //System.out.println(key + "  " + key2);
            if (key2.equals(key)) {
                return method;
            }
        }
        return null;
    }    

    /**
     * Find a field by it's name.
     * 
     * @param name
     *            Name of the field to find - Cannot be null.
     * 
     * @return Field or null if it's not found.
     */
    public SgField findFieldByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be null!");
        }
        for (SgField field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    private void addPackageLine(StringBuffer sb) {
        if (enclosingClass == null) {
            if (!packageName.isEmpty()) {
                sb.append("package ");
                sb.append(packageName);
                sb.append(";\n");
                sb.append('\n');
            }
        }
    }

    private void addNameLine(StringBuffer sb, boolean showAnnotations) {
        if (showAnnotations && (!getAnnotations().isEmpty())) {
            for (int i = 0; i < getAnnotations().size(); i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(getAnnotations().get(i));
            }
            sb.append('\n');
        }
        if (!modifiers.isEmpty()) {
            sb.append(modifiers);
            sb.append(' ');
        }
        if (isinterface) {
            sb.append("interface ");
        } else {
            sb.append("class ");
        }
        sb.append(getSimpleName());
        if (isinterface) {
            if (!interfaces.isEmpty()) {
                sb.append(" extends ");
                sb.append(getInterfacesCommaSeparated());
            }
        } else {
            if ((superClass != null) && (!"Object".equals(superClass.getName()))) {
                sb.append(" extends ");
                sb.append(superClass.getSourceName());
            }
            if (!interfaces.isEmpty()) {
                sb.append(" implements ");
                sb.append(getInterfacesCommaSeparated());
            }
        }
        sb.append(" {\n");
    }

    private void addFields(StringBuffer sb) {
        for (int i = 0; i < getFields().size(); i++) {
            sb.append(getFields().get(i)).append('\n');
            sb.append('\n');
        }
        sb.append('\n');
    }

    private void addConstructors(StringBuffer sb) {
        for (SgConstructor constructor : constructors) {
            sb.append(constructor).append('\n');
            sb.append('\n');
        }
        sb.append('\n');
    }

    private void addMethods(StringBuffer sb) {
        for (SgMethod method : methods) {
            sb.append(method).append('\n');
            sb.append('\n');
        }
    }

    private void addInnerClasses(StringBuffer sb) {
        for (SgClass aClass : classes) {
            sb.append(aClass).append('\n');
        }
        sb.append("}\n");
    }

    /**
     * Returns the modifiers as text.
     * 
     * @return Modifier names.
     */
    public String getModifiers() {
        return modifiers;
    }

    /**
     * Determines if the class represents a primitive type ({@link #VOID},
     * {@link #BOOLEAN}, {@link #BYTE}, {@link #CHAR}, {@link #SHORT},
     * {@link #INT} , {@link #LONG}, {@link #FLOAT} or {@link #DOUBLE}).
     * 
     * @return true if and only if this class represents a primitive type
     */
    public boolean isPrimitive() {
        return equals(VOID) || equals(BOOLEAN) || equals(BYTE) || equals(CHAR)
                || equals(SHORT) || equals(INT) || equals(LONG)
                || equals(FLOAT) || equals(DOUBLE);
    }

    /**
     * Determines if the class represents a basic type ({@link String}
     * , {@link Boolean}, {@link Byte},
     * {@link Character}, {@link Short},
     * {@link Integer} , {@link Long},
     * {@link Float}, {@link Double},
     * {@link BigDecimal} or {@link BigInteger}).
     * 
     * @return true if and only if this class represents one of those types.
     */
    public boolean isBaseType() {
        String name = getName();
        if (name.equals(String.class.getName())) {
            return true;
        }
        if (name.equals(Boolean.class.getName())) {
            return true;
        }
        if (name.equals(Byte.class.getName())) {
            return true;
        }
        if (name.equals(Character.class.getName())) {
            return true;
        }
        if (name.equals(Short.class.getName())) {
            return true;
        }
        if (name.equals(Integer.class.getName())) {
            return true;
        }
        if (name.equals(Long.class.getName())) {
            return true;
        }
        if (name.equals(Float.class.getName())) {
            return true;
        }
        if (name.equals(Double.class.getName())) {
            return true;
        }
        if (name.equals(BigDecimal.class.getName())) {
            return true;
        }
        return name.equals(BigInteger.class.getName());
    }

    /**
     * Checks if this class or any of it's super classes has a given interface.
     * 
     * @param intf
     *            Interface to find - Cannot be <code>null</code>.
     * 
     * @return If the class implements the interface <code>true</code> else
     *         <code>false</code>.
     */
    public boolean hasInterface(SgClass intf) {

        if (intf == null) {
            throw new IllegalArgumentException("The argument 'intf' cannot be null!");
        }
        if (!intf.isInterface()) {
            throw new IllegalArgumentException(
                    "The argument 'intf' is a class an not an interface!");
        }

        for (SgClass anInterface : interfaces) {
            if (anInterface.equals(intf)) {
                return true;
            }
        }
        if (superClass != null) {
            return superClass.hasInterface(intf);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Creates the class source with or without annotations.
     * 
     * @param showAnnotations
     *            To include annotations <code>true</code> else
     *            <code>true</code>.
     * 
     * @return Source code of the class.
     */
    public String toString(boolean showAnnotations) {
        StringBuffer sb = new StringBuffer();
        addPackageLine(sb);
        addNameLine(sb, showAnnotations);
        addFields(sb);
        addConstructors(sb);
        addMethods(sb);
        addInnerClasses(sb);
        return sb.toString();
    }

    /**
     * Creates a model class by loading the "real" class with
     * <code>Class.forName(..)</code> and analyzing it. Throws an
     * <code>IllegalArgumentException</code> if the class cannot be constructed
     * with "forName".
     * 
     * @param pool
     *            Pool to use.
     * @param className
     *            Full qualified name.
     * 
     * @return Class.
     */
    public static SgClass create(SgClassPool pool, String className) {
        if (pool == null) {
            throw new IllegalArgumentException("The argument 'pool' cannot be null!");
        }
        if (className == null) {
            throw new IllegalArgumentException("The argument 'className' cannot be null!");
        }
        SgClass cached = pool.get(className);
        if (cached != null) {
            return cached;
        }
        try {
            return create(pool, Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot create '" + className + "'!", e);
        }
    }

    /**
     * Creates a model class by analyzing the "real" class.
     * 
     * @param pool
     *            Pool to use.
     * @param clasz
     *            Class to analyze.
     * 
     * @return Class.
     */
    public static SgClass create(SgClassPool pool, Class<?> clasz) {
        if (pool == null) {
            throw new IllegalArgumentException("The argument 'pool' cannot be null!");
        }
        if (clasz == null) {
            throw new IllegalArgumentException("The argument 'clasz' cannot be null!");
        }

        SgClass cached = pool.get(clasz.getName());
        if (cached != null) {
            return cached;
        }

        try {

            SgClass cl = createClass(pool, clasz);
            addInterfaces(pool, cl, clasz);
            addFields(pool, cl, clasz);
            addConstructors(pool, cl, clasz);
            addMethods(pool, cl, clasz);
            addInnerClasses(pool, cl, clasz);
            return cl;

        } catch (RuntimeException ex) {
            System.out.println("ERROR CLASS: " + clasz);
            throw ex;
        }

    }

    private static SgClass createClass(SgClassPool pool, Class<?> clasz) {

        SgClass enclosingClass;
        enclosingClass = clasz.getEnclosingClass() == null ? null : create(pool, clasz.getEnclosingClass());
        String clModifiers = Modifier.toString(clasz.getModifiers());

        String packageName;
        packageName = clasz.getPackage() == null ? "" : clasz.getPackage().getName();

        // Set super class
        SgClass superClass;
        superClass = clasz.isInterface() ? null : SgClass.create(pool, clasz.getSuperclass());

        SgClass cl = new SgClass(clModifiers, packageName, clasz.getSimpleName(), superClass,
                clasz.isInterface(), enclosingClass);
        // Add to cache
        if (clasz.isArray()) {
            pool.put(clasz.getName(), cl);
        } else {
            pool.put(cl);
        }

        return cl;
    }

    private static void addInterfaces(SgClassPool pool, SgClass cl, Class<?> clasz) {
        Class<?>[] interfaces = clasz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            cl.addInterface(SgClass.create(pool, anInterface));
        }
    }

    private static void addFields(SgClassPool pool, SgClass cl, Class<?> clasz) {
        Field[] fields = clasz.getDeclaredFields();
        for (Field field1 : fields) {
            SgClass type = SgClass.create(pool, field1.getType());
            String name = field1.getName();
            String modifiers = Modifier.toString(field1.getModifiers());
            // This implicitly adds the field to the class
            SgField field = new SgField(cl, modifiers, type, name, null);
            field.addAnnotations(SgUtils.createAnnotations(field1.getAnnotations()));
        }
    }

    private static void addConstructors(SgClassPool pool, SgClass cl,
                                        Class<?> clasz) {
        if (!cl.isInterface()) {
            Constructor<?>[] constructors = clasz.getDeclaredConstructors();
            for (Constructor<?> constructor1 : constructors) {

                SgConstructor constructor = new SgConstructor(cl,
                        Modifier.toString(constructor1.getModifiers()));

                Class<?>[] parameterTypes = constructor1.getParameterTypes();
                for (int j = 0; j < parameterTypes.length; j++) {
                    // This implicitly adds the argument to the constructor
                    new SgArgument(constructor, create(pool, parameterTypes[j]), "p" + j);
                }

                Class<?>[] exceptions = constructor1.getExceptionTypes();
                for (Class<?> exception : exceptions) {
                    constructor.addException(SgClass.create(pool, clasz));
                }

                cl.addConstructor(constructor);
            }
        }
    }

    private static void addMethods(SgClassPool pool, SgClass cl, Class<?> clasz) {
        Method[] methods = clasz.getDeclaredMethods();
        for (Method method1 : methods) {
            String mModifiers = Modifier.toString(method1.getModifiers());
            SgClass returnType = create(pool, method1.getReturnType());
            SgMethod method = new SgMethod(method1, cl, mModifiers, returnType, method1.getName());
            Class<?>[] parameterTypes = method1.getParameterTypes();
            for (int j = 0; j < parameterTypes.length; j++) {
                // This implicitly adds the argument to the method
                new SgArgument(method, create(pool, parameterTypes[j]), "p" + j);
            }
            method.addAnnotations(SgUtils.createAnnotations(method1.getAnnotations()));

            Class<?>[] exceptions = method1.getExceptionTypes();
            for (Class<?> exception : exceptions) {
                method.addException(SgClass.create(pool, clasz));
            }

            cl.addMethod(method);
        }
    }

    private static void addInnerClasses(SgClassPool pool, SgClass cl,
                                        Class<?> clasz) {
        Class<?>[] innerClasses = clasz.getClasses();
        for (Class<?> innerClass : innerClasses) {
            cl.addClass(create(pool, innerClass));
        }
    }

    /**
     * Returns the corresponding class for a primitive.
     * 
     * @param pool
     *            Pool to use.
     * @param primitive
     *            Primitive class to convert. A call to {@link #isPrimitive()}
     *            on this argument must return true and a call to
     *            <code>equals(SgClass.VOID)</code> returns false else an
     *            {@link IllegalArgumentException} will be thrown.
     * 
     * @return Non primitive class.
     */
    public static SgClass getNonPrimitiveClass(SgClassPool pool, SgClass primitive) {
        if (primitive.equals(BOOLEAN)) {
            return SgClass.create(pool, Boolean.class);
        }
        if (primitive.equals(BYTE)) {
            return SgClass.create(pool, Byte.class);
        }
        if (primitive.equals(CHAR)) {
            return SgClass.create(pool, Character.class);
        }
        if (primitive.equals(SHORT)) {
            return SgClass.create(pool, Short.class);
        }
        if (primitive.equals(INT)) {
            return SgClass.create(pool, Integer.class);
        }
        if (primitive.equals(LONG)) {
            return SgClass.create(pool, Long.class);
        }
        if (primitive.equals(FLOAT)) {
            return SgClass.create(pool, Float.class);
        }
        if (primitive.equals(DOUBLE)) {
            return SgClass.create(pool, Double.class);
        }
        throw new IllegalArgumentException("No primitive or 'void' class: '" + primitive.getName()
                + "'!");
    }

    /**
     * Returns the name of the conversion method to return a primitive type
     * value from the following types: {@link Boolean},
     * {@link Byte}, {@link Character},
     * {@link Short}, {@link Integer},
     * {@link Long}, {@link Float} or
     * {@link Double}. If this class is not one of the above types a
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param clasz
     *            Class to return a conversion method from.
     * 
     * @return Name of the no argument conversion method (like "intValue" for
     *         converting an {@link Integer} into an <code>int</code>
     *         ).
     */
    public static String getToPrimitiveMethod(SgClass clasz) {
        String name = clasz.getName();
        if (name.equals(Boolean.class.getName())) {
            return "booleanValue";
        }
        if (name.equals(Byte.class.getName())) {
            return "byteValue";
        }
        if (name.equals(Character.class.getName())) {
            return "charValue";
        }
        if (name.equals(Short.class.getName())) {
            return "shortValue";
        }
        if (name.equals(Integer.class.getName())) {
            return "intValue";
        }
        if (name.equals(Long.class.getName())) {
            return "longValue";
        }
        if (name.equals(Float.class.getName())) {
            return "floatValue";
        }
        if (name.equals(Double.class.getName())) {
            return "doubleValue";
        }
        throw new IllegalArgumentException("Cannot convert '" + clasz.getName()
                + "' to a primitive type!");
    }

}
