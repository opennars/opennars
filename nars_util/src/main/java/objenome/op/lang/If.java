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
package objenome.op.lang;

import objenome.op.Node;
import objenome.util.TypeUtil;

/**
 * A node which represents the conditional if-then-else statement
 *
 * @since 2.0
 */
public class If extends Node {

    public static final String IDENTIFIER = "IF";

    /**
     * Constructs an <code>IfFunction</code> with three <code>null</code>
     * children
     */
    public If() {
        this(null, null, null);
    }

    /**
     * Constructs an <code>IfFunction</code> with three child nodes
     *
     * @param condition a boolean child node which will determine which of the
     * other nodes are evaluated.
     * @param ifStatement the child node to be evaluated if the condition
     * evaluates to true.
     * @param elseStatement the child node to be evaluated if the condition
     * evaluates to false.
     */
    public If(Node condition, Node ifStatement, Node elseStatement) {
        super(condition, ifStatement, elseStatement);
    }

    /**
     * Evaluates this function. The first child node is evaluated, the result of
     * which must be a <code>Boolean</code> instance. If the result is a true
     * value then the second child is also evaluated, the result of which
     * becomes the result of this function. If the first child evaluated to a
     * false value then the third child is evaluated and its result returned.
     *
     * @return the result of evaluating either the second or third child
     * depending on the value that the first child evaluates to
     */
    @Override
    public Object evaluate() {
        boolean c1 = (boolean)(getChild(0).evaluate());

        return c1 ? getChild(1).evaluate() : getChild(2).evaluate();
    }

    /**
     * Returns the identifier of this function which is <code>IF</code>
     *
     * @return this node's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns this function node's return type for the given child input types.
     * If there are three children, the first of which has the data-type
     * Boolean, then the return type of this function will be whichever of the
     * second and third children is a super type the other. If neither of the
     * other two children are a subclass of the other, then these input types
     * are invalid and <code>null</code> will be returned.
     *
     * @return The <code>Boolean</code> class or <code>null</code> if the input
     * type is invalid.
     */
    @Override
    public Class dataType(Class... inputTypes) {
        return (inputTypes.length == 3) && (inputTypes[0] == Boolean.class) ? TypeUtil.getSuper(inputTypes[1], inputTypes[2]) : null;
    }


}
