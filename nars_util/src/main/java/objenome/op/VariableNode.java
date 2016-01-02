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

/**
 * The <code>VariableNode</code> class provides a wrapper for
 * <code>Variable</code> objects. Each <code>Node</code> must only appear in one
 * place in a tree, so to allow the same variable instance to be reused in
 * multiple places, variables are not themselves nodes. Instead, a
 * <code>VariableNode</code> wrapper is used. This allows the same variable
 * object to be used in multiple locations within the same program tree while
 * keeping the nodes unique.
 *
 * @see Variable
 *
 * @since 2.0
 */
public class VariableNode<V> extends Node {

    public Variable<V> variable;

    /**
     * Constructs a new <code>VariableNode</code> wrapper for the given variable
     *
     * @param variable the <code>Variable</code> object to wrap
     */
    public VariableNode(Variable<V> variable) {
        if (variable == null) {
            throw new IllegalArgumentException("variable cannot be null");
        }

        this.variable = variable;
    }

    /**
     * Returns the <code>Variable</code> object that this node is a wrapper for
     *
     * @return the variable
     */
    public Variable getVariable() {
        return variable;
    }

    /**
     * Returns the value of the variable
     *
     * @return the variable's value
     */
    @Override
    public V evaluate() {
        return variable.getValue();
    }

    /** default "fast" evaluation method, which should be overrided in math-related subclasses */
    @Override
    public double asDouble() {
        //if (variable instanceof DoubleVariable)
        return ((DoubleVariable)variable).get();
    }

    /**
     * Returns the name of the variable
     *
     * @return the name of the variable
     */
    @Override
    public String getIdentifier() {
        return variable.getName();
    }

    /**
     * Returns the data-type of the variable
     *
     * @return the data-type of the variable's value
     */
    @Override
    public Class dataType(Class... inputTypes) {
        if (inputTypes.length != 0) {
            throw new IllegalArgumentException("variables do not have input types");
        }

        return variable.getDataType();
    }

    /**
     * Returns a string representation of the variable
     *
     * @return a string representation of the variable
     */
    @Override
    public String toString() {
        return variable.getName();
    }

    /**
     * Compares this <code>VariableNode</code> to the given object for equality.
     * Two <code>VariableNode</code> objects are only considered to be equal if
     * they refer to the same variable instance.
     *
     * @return <code>true</code> if the given object refers to the same variable
     * instance as this node and <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof VariableNode) && (((VariableNode) obj).variable.equals(variable));
    }

    @Override
    public boolean isVariable() {
        return true;
    }

    @Override
    public VariableNode newInstance() {
        /*VariableNode n = (VariableNode) super.newInstance();
        n.variable = n.variable.clone();
        return n;*/
        return this;
    }
}
