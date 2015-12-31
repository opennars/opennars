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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for constructors and methods.
 */
public abstract class SgBehavior {

    private final SgClass owner;

    private final String modifiers;

    private final List<SgArgument> arguments;

    private final List<SgClass> exceptions;

    private final List<SgAnnotation> annotations;

    /**
     * Constructor with class and modifiers.
     * 
     * @param owner
     *            Class the behavior belongs to - Cannot be null.
     * @param modifiers
     *            Modifiers for the constructor/method - Cannot be null (but
     *            empty).
     */
    public SgBehavior(SgClass owner, String modifiers) {
        if (owner == null) {
            throw new IllegalArgumentException("The argument 'owner' cannot be null!");
        }
        this.owner = owner;

        if (modifiers == null) {
            throw new IllegalArgumentException("The argument 'modifiers' cannot be null!");
        }
        this.modifiers = modifiers;

        arguments = new ArrayList<>();
        exceptions = new ArrayList<>();
        annotations = new ArrayList<>();
    }

    /**
     * Returns the class the behavior belongs to.
     * 
     * @return Owner - Always non-null.
     */
    public final SgClass getOwner() {
        return owner;
    }

    /**
     * Returns the modifiers for the constructor/method.
     * 
     * @return Modifiers (space separated) - Always non-null (but maybe empty).
     */
    public final String getModifiers() {
        return modifiers;
    }

    /**
     * Returns the constructor/method arguments.
     * 
     * @return Arguments - Always non-null and is unmodifiable.
     */
    public final List<SgArgument> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    private static String commaSeparated(List<SgArgument> args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(args.get(i).getName());
        }
        return sb.toString();
    }

    /**
     * Returns the argument names from 0..(N+less) separated by a comma.
     * 
     * @param less
     *            The number of arguments NOT to return from the end of the
     *            list. It's always a NEGATIVE value.
     * 
     * @return Arguments without "less" arguments at the end of the list.
     */
    public final String getCommaSeparatedArgumentNames(int less) {
        List<SgArgument> args = getArguments(less);
        return commaSeparated(args);
    }

    /**
     * Returns the argument names separated by a comma.
     * 
     * @return Arguments.
     */
    public final String getCommaSeparatedArgumentNames() {
        return commaSeparated(arguments);
    }

    /**
     * Returns the arguments from 0..(N+less).
     * 
     * @param less
     *            The number of arguments NOT to return from the end of the
     *            list. It's always a NEGATIVE value.
     * 
     * @return Unmodifiable argument list without "less" arguments at the end of
     *         the list.
     */
    public final List<SgArgument> getArguments(int less) {
        if (less >= 0) {
            throw new IllegalArgumentException("Only negative values are allowed! [" + less
                    + ']');
        }
        int count = arguments.size() + less;
        if (count < 0) {
            throw new IllegalArgumentException("There are only " + arguments.size()
                    + " arguments! Subtracting '" + less + "' would be below zero!");
        }
        List<SgArgument> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(arguments.get(i));
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns the last argument of the list.
     * 
     * @return Last argument or null if the list is empty.
     */
    public final SgArgument getLastArgument() {
        int size = arguments.size();
        if (size == 0) {
            return null;
        }
        return arguments.get(size - 1);
    }

    /**
     * Adds an argument to the list. Does nothing if the argument is already in
     * the list of arguments. You will never need to use this method in your
     * code! An argument is added automatically to the owning behavior when it's
     * constructed!
     * 
     * @param arg
     *            Argument to add - Non null.
     */
    public final void addArgument(SgArgument arg) {
        if (arg == null) {
            throw new IllegalArgumentException("The argument 'arg' cannot be null!");
        }
        if (arg.getOwner() != this) {
            throw new IllegalArgumentException("The owner of 'arg' is different from 'this'!");
        }
        if (!arguments.contains(arg)) {
            arguments.add(arg);
        }
    }

    /**
     * Returns the list of exceptions thrown by the constructor/method.
     * 
     * @return List of exceptions - Always non-null and is unmodifiable.
     */
    public final List<SgClass> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    /**
     * Adds an exception to the list. Does nothing if the class is already in
     * the list of exceptions.
     * 
     * @param clasz
     *            Exception to add.
     */
    public final void addException(SgClass clasz) {
        if (clasz == null) {
            throw new IllegalArgumentException("The argument 'clasz' cannot be null!");
        }
        // TODO Check if any superclass is of type Exception.
        if (!exceptions.contains(clasz)) {
            exceptions.add(clasz);
        }
    }

    /**
     * Returns the annotations for this method.
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
    public final void addAnnotation(SgAnnotation annotation) {
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
    public final void addAnnotations(List<SgAnnotation> annotations) {
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
    public final boolean hasAnnotation(String name) {
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

}
