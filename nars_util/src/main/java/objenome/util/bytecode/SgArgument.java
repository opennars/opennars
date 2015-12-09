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
 * Argument for a behavior (constructor or method).
 */
public final class SgArgument extends SgVariable {

    private final SgBehavior owner;

    /**
     * Constructor with argument. Adds the new instance implicitly to the
     * <code>owner</code>.
     * 
     * @param owner
     *            The constructor or method the argument belongs to.
     * @param argument
     *            Argument to copy type and name from - Cannot be null.
     */
    public SgArgument(final SgBehavior owner, final SgArgument argument) {
        this(owner, argument.getModifiers(), argument.getType(), argument.getName());
    }

    /**
     * Constructor with type and name. Adds the new instance implicitly to the
     * <code>owner</code>.
     * 
     * @param owner
     *            The constructor or method the argument belongs to.
     * @param type
     *            Type - Cannot be null.
     * @param name
     *            Name - Cannot be null and cannot be empty.
     */
    public SgArgument(final SgBehavior owner, final SgClass type, final String name) {
        this(owner, "", type, name);
    }

    /**
     * Constructor with type and name. Adds the new instance implicitly to the
     * <code>owner</code>.
     * 
     * @param owner
     *            The constructor or method the argument belongs to.
     * @param modifiers
     *            Modifiers (separated by space) - Cannot be null (but empty).
     * @param type
     *            Type - Cannot be null.
     * @param name
     *            Name - Cannot be null and cannot be empty.
     */
    public SgArgument(final SgBehavior owner, final String modifiers, final SgClass type,
            final String name) {
        super(modifiers, type, name);

        if (owner == null) {
            throw new IllegalArgumentException("The argument 'owner' cannot be null!");
        }
        this.owner = owner;

        this.owner.addArgument(this);
    }

    /**
     * Returns the method or constructor this argument belongs to.
     * 
     * @return Behaviour.
     */
    public SgBehavior getOwner() {
        return owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < getAnnotations().size(); i++) {
            sb.append(getAnnotations().get(i));
            sb.append(' ');
        }
        if (getModifiers().isEmpty()) {
            sb.append(getType().getSourceName()).append(' ').append(getName());
        } else {
            sb.append(getModifiers()).append(' ').append(getType().getSourceName()).append(' ').append(getName());
        }
        return sb.toString();
    }

}
