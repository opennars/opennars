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
package objenome.op.math;

import objenome.op.Node;
import objenome.util.NumericUtils;
import objenome.util.TypeUtil;

/**
 * A node which performs the mathematical sign function that extracts the sign
 * of a number
 *
 * @since 2.0
 */
public class Signum extends Node {

    public static final String IDENTIFIER = "SGN";

    /**
     * Constructs a SignumFunction with one <code>null</code> child.
     */
    public Signum() {
        this(null);
    }

    /**
     * Constructs a SignumFunction with one numerical child node.
     *
     * @param child the child node.
     */
    public Signum(Node child) {
        super(child);
    }

    /**
     * Evaluates this function. The child node is evaluated, the result of which
     * must be a numeric type (one of Double, Float, Long, Integer). Then the
     * result will be -1, if the value is negative, +1, if the value is positive
     * and 0 if the value is zero. The type of the value returned will be the
     * same as the input type.
     *
     * @return zero if the result of evaluating the child is zero, one if the
     * result is positive and minus one if it is negative
     */
    @Override
    public Object evaluate() {
        Object c = getChild(0).evaluate();

        double result = Math.signum(NumericUtils.asDouble(c));

        if (c instanceof Double) {
            return result;
        }
        if (c instanceof Float) {
            return (float) result;
        }
        if (c instanceof Integer) {
            return (int) result;
        }
        if (c instanceof Long) {
            return (long) result;
        }

        return null;
    }

    /**
     * Returns the identifier of this function which is SGN
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
     * that same numeric type. In all other cases this method will return
     * <code>null</code> to indicate that the inputs are invalid.
     *
     * @return A numeric class or null if the input type is invalid.
     */
    @Override
    public Class dataType(Class... inputTypes) {
        return (inputTypes.length == 1) && TypeUtil.isNumericType(inputTypes[0]) ? inputTypes[0] : null;
    }
}
