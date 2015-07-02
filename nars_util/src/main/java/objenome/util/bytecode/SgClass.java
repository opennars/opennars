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
    public SgClass(final String packageName, final String simpleName) {
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
    public SgClass(final String modifiers, final String packageName, final String simpleName,
            final boolean isinterface, final SgClass enclosingClass) {
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
    public SgClass(final String modifiers, final String packageName, final String simpleName,
            final SgClass superClass, final boolean isinterface, final SgClass enclosingClass) {
        super();

        this.modifiers = modifiers;
        this.packageName = packageName;
        this.simpleName = simpleName;

        this.superClass = superClass;
        if (isinterface && (superClass != null)) {
            throw new IllegalArgumentException(
                    "This is an interface. You cannot set a super class. "
                            + "Use 'addInterface(SgClass)' instead!");
        }

        this.constructors = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.isinterface = isinterface;
        this.classes = new ArrayList<>();
        this.annotations = new ArrayList<>();

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
    public final List<SgAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    /**
     * Adds an annotation.
     * 
     * @param annotation
     *            Annotation to add - Cannot be null.
     */
    public final void addAnnotation(final SgAnnotation annotation) {
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
    public final void addAnnotations(final List<SgAnnotation> annotations) {
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
    public final boolean hasAnnotation(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be NULL!");
        }
        for (int i = 0; i < annotations.size(); i++) {
            final SgAnnotation annotation = annotations.get(i);
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
    public final SgClass getSuperClass() {
        return superClass;
    }

    /**
     * Returns the simple name of the class with an "underscore" inserted before
     * all upper case characters and all characters converted to lower case.
     * 
     * @return Name usable as a package - Always non-null.
     */
    public final String getSimpleNameAsPackage() {
        return SgUtils.uppercaseToUnderscore(getSimpleName());
    }

    /**
     * Returns a list of constructors.
     * 
     * @return Constructor list - Always non-null and is unmodifiable.
     */
    public final List<SgConstructor> getConstructors() {
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
    public final void addConstructor(final SgConstructor constructor) {
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
    public final boolean isInterface() {
        return isinterface;
    }

    /**
     * Returns a list of all methods.
     * 
     * @return Method list - Always non-null and is unmodifiable.
     */
    public final List<SgMethod> getMethods() {
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
    public final void addMethod(final SgMethod method) {
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
    public final String getName() {
        return getName("$");
    }

    /**
     * Returns the name of the class for use in source codes.
     * 
     * @return Name with inner classes separated with ".".
     */
    public final String getSourceName() {
        return getName(".");
    }

    /**
     * Returns the package and name as a filename without extension.
     * 
     * @return Relative path and filename.
     */
    public final String getNameAsFilename() {
        return getName().replace('.', File.separatorChar);
    }

    /**
     * Returns the package and name as a source filename.
     * 
     * @return Relative path and filename with extension ".java".
     */
    public final String getNameAsSrcFilename() {
        return getNameAsFilename() + ".java";
    }

    /**
     * Returns the package and name as a class filename.
     * 
     * @return Relative path and filename with extension ".class".
     */
    public final String getNameAsBinFilename() {
        return getNameAsFilename() + ".class";
    }

    private String getEnclosingSimpleNames(final String innerDivider) {
        final StringBuffer sb = new StringBuffer();
        SgClass clasz = enclosingClass;
        while (clasz != null) {
            sb.insert(0, innerDivider);
            sb.insert(0, clasz.getSimpleName());
            clasz = clasz.getEnclosingClass();
        }
        return sb.toString();
    }

    private final String getName(final String innerDivider) {
        if (packageName.length() == 0) {
            if (enclosingClass == null) {
                return simpleName;
            } else {
                return getEnclosingSimpleNames(innerDivider) + simpleName;
            }
        }
        if (enclosingClass == null) {
            return packageName + '.' + simpleName;
        } else {
            return packageName + '.' + getEnclosingSimpleNames(innerDivider) + simpleName;
        }
    }

    /**
     * Returns the package of the class.
     * 
     * @return Package name.
     */
    public final String getPackageName() {
        return packageName;
    }

    /**
     * Returns the simple name of the class.
     * 
     * @return Name without package.
     */
    public final String getSimpleName() {
        return simpleName;
    }

    /**
     * Returns a list of all interfaces.
     * 
     * @return List of interfaces - Always non-null and unmodifiable.
     */
    public final List<SgClass> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    /**
     * Add an interface to the class.
     * 
     * @param intf
     *            Interface to add - Cannot be null.
     */
    public final void addInterface(final SgClass intf) {
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
    public final String getInterfacesCommaSeparated() {
        final StringBuffer sb = new StringBuffer();
        if (interfaces.size() > 0) {
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
    public final List<SgField> getFields() {
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
    public final void addField(final SgField field) {
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
    public final List<SgClass> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    /**
     * Adds an inner to this class. Does nothing if the class is already in the
     * list of inner classes.
     * 
     * @param clasz
     *            Inner class to add - Cannot be null.
     */
    public final void addClass(final SgClass clasz) {
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
    public final SgClass getEnclosingClass() {
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
    public final SgClass findClassByName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be null!");
        }
        for (int i = 0; i < classes.size(); i++) {
            final SgClass clasz = classes.get(i);
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
    public final SgMethod findMethodByName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be null!");
        }
        for (int i = 0; i < methods.size(); i++) {
            final SgMethod method = methods.get(i);
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
    
    public final SgMethod findMethod(final CtMethod em) {
        String key = em.getLongName();
        for (int i = 0; i < methods.size(); i++) {
            final SgMethod method = methods.get(i);
            String key2 = getName() + '.' + method.getTypeSignature();
            //System.out.println(key + "  " + key2);
            if ( key2.equals(key)) {
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
    public final SgField findFieldByName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be null!");
        }
        for (int i = 0; i < fields.size(); i++) {
            final SgField field = fields.get(i);
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    private void addPackageLine(final StringBuffer sb) {
        if (enclosingClass == null) {
            if (packageName.length() > 0) {
                sb.append("package ");
                sb.append(packageName);
                sb.append(";\n");
                sb.append('\n');
            }
        }
    }

    private void addNameLine(final StringBuffer sb, final boolean showAnnotations) {
        if (showAnnotations && (getAnnotations().size() > 0)) {
            for (int i = 0; i < getAnnotations().size(); i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(getAnnotations().get(i));
            }
            sb.append('\n');
        }
        if (modifiers.length() > 0) {
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
            if (interfaces.size() > 0) {
                sb.append(" extends ");
                sb.append(getInterfacesCommaSeparated());
            }
        } else {
            if ((superClass != null) && (!"Object".equals(superClass.getName()))) {
                sb.append(" extends ");
                sb.append(superClass.getSourceName());
            }
            if (interfaces.size() > 0) {
                sb.append(" implements ");
                sb.append(getInterfacesCommaSeparated());
            }
        }
        sb.append(" {\n");
    }

    private void addFields(final StringBuffer sb) {
        for (int i = 0; i < getFields().size(); i++) {
            sb.append(getFields().get(i)).append("\n");
            sb.append('\n');
        }
        sb.append('\n');
    }

    private void addConstructors(final StringBuffer sb) {
        for (int i = 0; i < constructors.size(); i++) {
            sb.append(constructors.get(i)).append("\n");
            sb.append('\n');
        }
        sb.append('\n');
    }

    private void addMethods(final StringBuffer sb) {
        for (int i = 0; i < methods.size(); i++) {
            sb.append(methods.get(i)).append("\n");
            sb.append('\n');
        }
    }

    private void addInnerClasses(final StringBuffer sb) {
        for (int i = 0; i < classes.size(); i++) {
            sb.append(classes.get(i)).append("\n");
        }
        sb.append("}\n");
    }

    /**
     * Returns the modifiers as text.
     * 
     * @return Modifier names.
     */
    public final String getModifiers() {
        return modifiers;
    }

    /**
     * Determines if the class represents a primitive type ({@link #VOID},
     * {@link #BOOLEAN}, {@link #BYTE}, {@link #CHAR}, {@link #SHORT},
     * {@link #INT} , {@link #LONG}, {@link #FLOAT} or {@link #DOUBLE}).
     * 
     * @return true if and only if this class represents a primitive type
     */
    public final boolean isPrimitive() {
        return this.equals(VOID) || this.equals(BOOLEAN) || this.equals(BYTE) || this.equals(CHAR)
                || this.equals(SHORT) || this.equals(INT) || this.equals(LONG)
                || this.equals(FLOAT) || this.equals(DOUBLE);
    }

    /**
     * Determines if the class represents a basic type ({@link java.lang.String}
     * , {@link java.lang.Boolean}, {@link java.lang.Byte},
     * {@link java.lang.Character}, {@link java.lang.Short},
     * {@link java.lang.Integer} , {@link java.lang.Long},
     * {@link java.lang.Float}, {@link java.lang.Double},
     * {@link java.math.BigDecimal} or {@link java.math.BigInteger}).
     * 
     * @return true if and only if this class represents one of those types.
     */
    public final boolean isBaseType() {
        final String name = getName();
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
        if (name.equals(BigInteger.class.getName())) {
            return true;
        }
        return false;
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
    public final boolean hasInterface(final SgClass intf) {

        if (intf == null) {
            throw new IllegalArgumentException("The argument 'intf' cannot be null!");
        }
        if (!intf.isInterface()) {
            throw new IllegalArgumentException(
                    "The argument 'intf' is a class an not an interface!");
        }

        for (int i = 0; i < interfaces.size(); i++) {
            if (interfaces.get(i).equals(intf)) {
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
    public final String toString() {
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
    public final String toString(final boolean showAnnotations) {
        final StringBuffer sb = new StringBuffer();
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
    public static SgClass create(final SgClassPool pool, final String className) {
        if (pool == null) {
            throw new IllegalArgumentException("The argument 'pool' cannot be null!");
        }
        if (className == null) {
            throw new IllegalArgumentException("The argument 'className' cannot be null!");
        }
        final SgClass cached = pool.get(className);
        if (cached != null) {
            return cached;
        }
        try {
            return create(pool, Class.forName(className));
        } catch (final ClassNotFoundException e) {
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
    public static SgClass create(final SgClassPool pool, final Class<?> clasz) {
        if (pool == null) {
            throw new IllegalArgumentException("The argument 'pool' cannot be null!");
        }
        if (clasz == null) {
            throw new IllegalArgumentException("The argument 'clasz' cannot be null!");
        }

        final SgClass cached = pool.get(clasz.getName());
        if (cached != null) {
            return cached;
        }

        try {

            final SgClass cl = createClass(pool, clasz);
            addInterfaces(pool, cl, clasz);
            addFields(pool, cl, clasz);
            addConstructors(pool, cl, clasz);
            addMethods(pool, cl, clasz);
            addInnerClasses(pool, cl, clasz);
            return cl;

        } catch (final RuntimeException ex) {
            System.out.println("ERROR CLASS: " + clasz);
            throw ex;
        }

    }

    private static SgClass createClass(final SgClassPool pool, final Class<?> clasz) {

        final SgClass enclosingClass;
        if (clasz.getEnclosingClass() == null) {
            enclosingClass = null;
        } else {
            enclosingClass = create(pool, clasz.getEnclosingClass());
        }
        final String clModifiers = Modifier.toString(clasz.getModifiers());

        final String packageName;
        if (clasz.getPackage() == null) {
            packageName = "";
        } else {
            packageName = clasz.getPackage().getName();
        }

        // Set super class
        final SgClass superClass;
        if (clasz.isInterface()) {
            superClass = null;
        } else {
            superClass = SgClass.create(pool, clasz.getSuperclass());
        }

        final SgClass cl = new SgClass(clModifiers, packageName, clasz.getSimpleName(), superClass,
                clasz.isInterface(), enclosingClass);
        // Add to cache
        if (clasz.isArray()) {
            pool.put(clasz.getName(), cl);
        } else {
            pool.put(cl);
        }

        return cl;
    }

    private static void addInterfaces(final SgClassPool pool, final SgClass cl, final Class<?> clasz) {
        final Class<?>[] interfaces = clasz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            cl.addInterface(SgClass.create(pool, interfaces[i]));
        }
    }

    private static void addFields(final SgClassPool pool, final SgClass cl, final Class<?> clasz) {
        final Field[] fields = clasz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            final SgClass type = SgClass.create(pool, fields[i].getType());
            final String name = fields[i].getName();
            final String modifiers = Modifier.toString(fields[i].getModifiers());
            // This implicitly adds the field to the class
            final SgField field = new SgField(cl, modifiers, type, name, null);
            field.addAnnotations(SgUtils.createAnnotations(fields[i].getAnnotations()));
        }
    }

    private static void addConstructors(final SgClassPool pool, final SgClass cl,
            final Class<?> clasz) {
        if (!cl.isInterface()) {
            final Constructor<?>[] constructors = clasz.getDeclaredConstructors();
            for (int i = 0; i < constructors.length; i++) {

                final SgConstructor constructor = new SgConstructor(cl,
                        Modifier.toString(constructors[i].getModifiers()));

                final Class<?>[] parameterTypes = constructors[i].getParameterTypes();
                for (int j = 0; j < parameterTypes.length; j++) {
                    // This implicitly adds the argument to the constructor
                    new SgArgument(constructor, create(pool, parameterTypes[j]), "p" + j);
                }

                final Class<?>[] exceptions = constructors[i].getExceptionTypes();
                for (int j = 0; j < exceptions.length; j++) {
                    constructor.addException(SgClass.create(pool, clasz));
                }

                cl.addConstructor(constructor);
            }
        }
    }

    private static void addMethods(final SgClassPool pool, final SgClass cl, final Class<?> clasz) {
        final Method[] methods = clasz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            final String mModifiers = Modifier.toString(methods[i].getModifiers());
            final SgClass returnType = create(pool, methods[i].getReturnType());
            final SgMethod method = new SgMethod(methods[i], cl, mModifiers, returnType, methods[i].getName());
            final Class<?>[] parameterTypes = methods[i].getParameterTypes();
            for (int j = 0; j < parameterTypes.length; j++) {
                // This implicitly adds the argument to the method
                new SgArgument(method, create(pool, parameterTypes[j]), "p" + j);
            }
            method.addAnnotations(SgUtils.createAnnotations(methods[i].getAnnotations()));

            final Class<?>[] exceptions = methods[i].getExceptionTypes();
            for (int j = 0; j < exceptions.length; j++) {
                method.addException(SgClass.create(pool, clasz));
            }

            cl.addMethod(method);
        }
    }

    private static void addInnerClasses(final SgClassPool pool, final SgClass cl,
            final Class<?> clasz) {
        final Class<?>[] innerClasses = clasz.getClasses();
        for (int i = 0; i < innerClasses.length; i++) {
            cl.addClass(create(pool, innerClasses[i]));
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
    public static final SgClass getNonPrimitiveClass(final SgClassPool pool, final SgClass primitive) {
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
     * value from the following types: {@link java.lang.Boolean},
     * {@link java.lang.Byte}, {@link java.lang.Character},
     * {@link java.lang.Short}, {@link java.lang.Integer},
     * {@link java.lang.Long}, {@link java.lang.Float} or
     * {@link java.lang.Double}. If this class is not one of the above types a
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param clasz
     *            Class to return a conversion method from.
     * 
     * @return Name of the no argument conversion method (like "intValue" for
     *         converting an {@link java.lang.Integer} into an <code>int</code>
     *         ).
     */
    public static final String getToPrimitiveMethod(final SgClass clasz) {
        final String name = clasz.getName();
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
