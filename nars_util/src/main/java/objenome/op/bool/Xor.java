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
package objenome.op.bool;

import objenome.op.Node;
import objenome.util.TypeUtil;

/**
 * A node which performs exclusive disjunction, also known as exclusive OR
 *
 * @since 2.0
 */
public class Xor extends Node {

    public static final String IDENTIFIER = "XOR";

    /**
     * Constructs an <code>XorFunction</code> with two <code>null</code>
     * children
     */
    public Xor() {
        this(null, null);
    }

    /**
     * Constructs an <code>XorFunction</code> with two boolean child nodes
     *
     * @param child1 the first child node
     * @param child2 the second child node
     */
    public Xor(Node child1, Node child2) {
        super(child1, child2);
    }

    /**
     * Evaluates this function. Both child nodes are evaluated, the result of
     * both must be a <code>Boolean</code> instance. The result of this function
     * will be <code>true</code> if either child (but not both) evaluate to
     * <code>true</code>, otherwise the result will be <code>false</code>.
     *
     * @return <code>true</code> if either child (but not both) evaluate to
     * <code>true</code>, otherwise the result with be <code>false</code>
     */
    @Override
    public Boolean evaluate() {
        boolean c1 = ((Boolean) getChild(0).evaluate());
        boolean c2 = ((Boolean) getChild(1).evaluate());

        return c1 != c2;
    }

    /**
     * Returns the identifier of this function which is <code>XOR</code>
     *
     * @return this node's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns this function node's return type for the given child input types.
     * If there are two children, both of which have a return type of
     * <code>Boolean</code>, then the return type of this function will also be
     * <code>Boolean</code>. In all other cases this method will return
     * <code>null</code> to indicate that the inputs are invalid.
     *
     * @return The Boolean class or null if the input type is invalid.
     */
    @Override
    public Class dataType(Class... inputTypes) {
        return (inputTypes.length == 2) && TypeUtil.allEqual(inputTypes, Boolean.class) ? Boolean.class : null;
    }
}
