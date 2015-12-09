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
 * A node which performs the logical operation of <code>NAND</code>, which is
 * equivalent to the negation of the conjunction, <code>NOT AND</code>
 *
 * @since 2.0
 */
public class Nand extends Node {

    public static final String IDENTIFIER = "NAND";

    /**
     * Constructs a <code>NandFunction</code> with two <code>null</code>
     * children
     */
    public Nand() {
        this(null, null);
    }

    /**
     * Constructs a <code>NandFunction</code> with two boolean child nodes
     *
     * @param child1 the first child node
     * @param child2 the second child node
     */
    public Nand(Node child1, Node child2) {
        super(child1, child2);
    }

    /**
     * Evaluates this function lazily. The first child node is evaluated, the
     * result of which must be a <code>Boolean</code> instance. The second child
     * is only evaluated if the first had a <code>true</code> value. The result
     * of this method will be <code>true</code> if both children evaluate to
     * <code>false</code>, otherwise it will return <code>false</code>.
     *
     * @return <code>true</code> if both children evaluate to <code>false</code>
     * otherwise <code>false</code> is returned
     */
    @Override
    public Boolean evaluate() {
        boolean result = ((Boolean) getChild(0).evaluate());

        if (result) {
            result = ((Boolean) getChild(1).evaluate());
        }

        return !result;
    }

    /**
     * Returns the identifier of this function which is <code>NAND</code>
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
