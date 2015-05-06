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

/**
 * A field of a class.
 */
public final class SgField extends SgVariable {

    private final SgClass owner;

    private final String initializer;

    /**
     * Constructor with modifiers, class and name. Adds the new instance
     * implicitly to the <code>owner</code>.
     * 
     * @param owner
     *            The type or method the field belongs to.
     * @param modifiers
     *            Modifiers (separated by space) - Cannot be null (but empty).
     * @param type
     *            Type of the field - Cannot be null.
     * @param name
     *            Name of the field - Cannot be null.
     * @param initializer
     *            Initializer for the field - Can be null but should normally be
     *            set to an empty String instead.
     */
    public SgField(final SgClass owner, final String modifiers, final SgClass type,
            final String name, final String initializer) {

        super(modifiers, type, name);

        if (owner == null) {
            throw new IllegalArgumentException("The argument 'owner' cannot be null!");
        }
        this.owner = owner;

        if (initializer == null) {
            this.initializer = initializer;
        } else {
            this.initializer = initializer.trim();
        }

        // TODO Does not work when analyzing classes... Check why!
        // if (owner.findFieldByName(name) != null) {
        // throw new IllegalArgumentException("The class '" + owner.getName()
        // + "' already contains a field with name '" + name + "'!");
        // }

        owner.addField(this);
    }

    /**
     * Returns the type this field belongs to.
     * 
     * @return Class.
     */
    public final SgClass getOwner() {
        return owner;
    }

    /**
     * Returns the initializer.
     * 
     * @return Initializer for the field - Maybe null.
     */
    public final String getInitializer() {
        return initializer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        final StringBuffer sb = new StringBuffer();
        if (getAnnotations().size() > 0) {
            for (int i = 0; i < getAnnotations().size(); i++) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(getAnnotations().get(i));
            }
            sb.append("\n");
        }
        if (getModifiers().length() == 0) {
            sb.append(getType().getSourceName() + " " + getName());
        } else {
            sb.append(getModifiers() + " " + getType().getSourceName() + " " + getName());
        }
        if (initializer == null) {
            sb.append(" /** No initializer source available */ ");
        } else {
            if (initializer.length() > 0) {
                sb.append(" = ");
                sb.append(initializer);
            }
        }
        sb.append(";\n");
        return sb.toString();
    }

}
