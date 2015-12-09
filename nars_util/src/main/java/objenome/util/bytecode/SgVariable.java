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
 * Base class for fields and arguments.
 */
public abstract class SgVariable {

    private final String modifiers;

    private final SgClass type;

    private final String name;

    private final List<SgAnnotation> annotations;

    /**
     * Constructor with modifiers, class and name.
     * 
     * @param modifiers
     *            Modifiers (separated by space) - Cannot be null (but empty).
     * @param type
     *            Type of the field - Cannot be null.
     * @param name
     *            Name of the field - Cannot be null.
     */
    public SgVariable(String modifiers, SgClass type, String name) {

        if (modifiers == null) {
            throw new IllegalArgumentException("The argument 'modifiers' cannot be null!");
        }
        this.modifiers = modifiers;

        if (type == null) {
            throw new IllegalArgumentException("The argument 'type' cannot be null!");
        }
        this.type = type;

        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be null!");
        }
        this.name = name;

        annotations = new ArrayList<>();

    }

    /**
     * Returns the modifiers.
     * 
     * @return Modifiers (separated by space) - Always non-null.
     */
    public final String getModifiers() {
        return modifiers;
    }

    /**
     * Returns the type of the field.
     * 
     * @return Type of the field - Always non-null.
     */
    public final SgClass getType() {
        return type;
    }

    /**
     * Returns the name.
     * 
     * @return Name of the field - Always non-null.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the annotations for this field.
     * 
     * @return List of annotations - Always non-null and unmodifiable.
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
