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
 * A node which performs the bi-conditional logical connective of
 * <code>IFF</code> (if and only if)
 *
 * @since 2.0
 */
public class IfAndOnlyIf extends Node {

    public static final String IDENTIFIER = "IFF";

    /**
     * Constructs an <code>IfAndOnlyIfFunction</code> with two <code>null</code>
     * children
     */
    public IfAndOnlyIf() {
        this(null, null);
    }

    /**
     * Constructs an <code>IfAndOnlyIfFunction</code> with two boolean child
     * nodes
     *
     * @param child1 the first child node
     * @param child2 the second child node
     */
    public IfAndOnlyIf(Node child1, Node child2) {
        super(child1, child2);
    }

    /**
     * Evaluates this function. Both child nodes are evaluated, the results of
     * which must be <code>Boolean</code> instances. The two boolean values
     * determine the result of this evaluation. If both inputs are
     * <code>true</code> or both are <code>false</code>, then the result will be <code>true. All other
     * combinations of the inputs will result in the return of a value of
     * <code>false</code>.
     *
     * @return <code>true</code> if both children evaluate to the same boolean
     * value and <code>false</code> otherwise
     */
    @Override
    public Boolean evaluate() {
        boolean c1 = ((Boolean) getChild(0).evaluate());
        boolean c2 = ((Boolean) getChild(1).evaluate());

        return c1 == c2;
    }

    /**
     * Returns the identifier of this function which is <code>IFF</code>
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
     * @return The <code>Boolean</code> class or <code>null</code> if the input
     * type is invalid
     */
    @Override
    public Class dataType(Class... inputTypes) {
        return (inputTypes.length == 2) && TypeUtil.allEqual(inputTypes, Boolean.class) ? Boolean.class : null;
    }
}
