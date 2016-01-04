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
package objenome.op.trig;

import objenome.op.Node;
import objenome.util.NumericUtils;
import objenome.util.TypeUtil;

/**
 * A node which performs the reciprocal trigonometric function of cotangent,
 * called COT. Cotangent x is equal to 1/tan x.
 *
 * @since 2.0
 */
public class Cotangent extends Node {

    public static final String IDENTIFIER = "COT";

    /**
     * Constructs an CotangentFunction with one <code>null</code> child.
     */
    public Cotangent() {
        this(null);
    }

    /**
     * Constructs a CotangentFunction with one numerical child node.
     *
     * @param child the child node.
     */
    public Cotangent(Node child) {
        super(child);
    }

    /**
     * Returns the cotangent of a <code>double</code> value
     *
     * @param d the number whose cotangent is sought
     * @return the cotangent of <code>d</code>
     */
    public static double cot(double d) {
        return 1 / Math.tan(d);
    }

    /**
     * Evaluates this function. The child node is evaluated, the result of which
     * must be a numeric type (one of Double, Float, Long, Integer). 1 is
     * divided by the tan of this value to give the result as a double value.
     *
     * @return cotangent of the value returned by the child
     */
    @Override
    public Double evaluate() {
        Object c = getChild(0).evaluate();

        return cot(NumericUtils.asDouble(c));
    }

    /**
     * Returns the identifier of this function which is COT
     *
     * @return this node's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns this function node's return type for the given child input types.
     * If there is one input type of a numeric type then the return type will be
     * Double. In all other cases this method will return <code>null</code> to
     * indicate that the inputs are invalid.
     *
     * @return the Double class or null if the input type is invalid.
     */
    @Override
    public Class dataType(Class... inputTypes) {
        return (inputTypes.length == 1) && TypeUtil.isNumericType(inputTypes[0]) ? Double.class : null;
    }
}
