/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX: genetic programming software for research
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.op;


import java.util.Objects;

/**
 * Literal values are terminal nodes with a specific value. Typically the value
 * of a literal will not change.
 *
 * @see Variable
 *
 * @since 2.0
 */
public class Literal<X extends Object> extends Node<Void,X> {

    protected X value;

    /**
     * Constructs a new <code>Literal</code> node with the given value.
     * Evaluation of this literal will return the value set here. The data-type
     * of this node will be determined by the type of the object specified here.
     *
     * @param value the value of this literal
     */
    public Literal(X value) {
        this.value = value;
    }

    /**
     * Returns the value of this literal. Has the same functionality as calling
     * <code>getValue</code>.
     *
     * @return the value of this literal
     */
    @Override
    public X evaluate() {
        return value;
    }

    /**
     * Returns this node's string representation, which is the string
     * representation of the literal value
     *
     * @return a <code>String</code> representation
     */
    @Override
    public String getIdentifier() {
        return toString();
    }

    /**
     * Returns the data-type of this node. The data-type of a literal node is
     * the data-type of its value. If no value has been set then
     * <code>null</code> will be returned.
     *
     * @param inputTypes the data-types of the node's inputs. Literals take no
     * inputs so this should be an empty array.
     * @return the data-type of this node
     * @throws IllegalArgumentException if <code>inputTypes</code> is anything
     * other than an empty array
     */
    @Override
    public Class dataType(Class... inputTypes) {
        if (inputTypes.length != 0) {
            throw new IllegalArgumentException("literals have no input types");
        }

        return value != null ? value.getClass() : null;
    }

    /**
     * Sets the value of this literal. Implementations of this class may wish to
     * use this method to delay the setting of a literal's value.
     *
     * @param value the value to set for this literal
     */
    protected void setValue(X value) {
        this.value = value;
    }

    /**
     * Returns the current value of this literal
     *
     * @return the value of this literal
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns this node's string representation, which is the string
     * representation of the literal value. If no value is set then an empty
     * string will be returned.
     *
     * @return a <code>String</code> representation.
     */
    @Override
    public String toString() {
        return value != null ? value.toString() : "";
    }

    /**
     * Compares this literal to the given object for equality. An object is
     * equal to this literal if it is an instance of <code>Literal</code> and
     * its value is equal to this literal's value.
     *
     * @return <code>true</code> if the two objects are equal,
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Literal)) {
            return false;
        }

        Object objVal = ((Literal) obj).value;
        Object thisVal = value;

        return Objects.equals(objVal, thisVal);
    }

    /**
     * Creates a new <code>Literal</code> instance which is a copy of this
     * instance. The clone will contain a value which is a reference to the same
     * object as this literal's value. For mutable object values, users should
     * consider extending this class to provide a clone method which can provide
     * an appropriate deep clone.
     *
     * @return a new <code>Literal</code> instance which is a clone of this
     * object
     */
    @Override
    public Literal clone() {
        Literal clone = (Literal) super.clone();

        clone.value = value;

        return clone;
    }

    /**
     * Constructs a new instance of this literal. This implementation is
     * equivalent to calling <code>clone()</code>.
     *
     * @return a new <code>Literal</code> instance which is a clone of this
     * object
     */
    @Override
    public Literal newInstance() {
        return clone();
    }

}
