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

import objenome.op.EpoxParser;
import objenome.op.Node;
import objenome.util.NumericUtils;
import objenome.util.TypeUtil;

/**
 * A node which performs the simple comparison function of determining which of
 * a set of numbers is smaller, as per the boolean less-than function. This
 * function can have a flexible number of children and as such cannot be parsed
 * by the {@link EpoxParser}, look at using {@link Min2} or {@link Min3}
 * instead.
 *
 * @since 2.0
 */
class Min extends Node {

    public static final String IDENTIFIER = "MIN";

    /**
     * Constructs a MinFunction with the given number of <code>null</code>
     * children.
     *
     * @param n the number of <code>null</code> children to set this function up
     * for.
     */
    public Min(int n) {
        this((Node) null);

        setChildren(new Node[n]);
    }

    /**
     * Constructs a MinFunction with given numerical child nodes.
     *
     * @param children the numeric child nodes.
     */
    public Min(Node... children) {
        super(children);
    }

    /**
     * Evaluates this function. The child nodes are evaluated, the results of
     * which must be numerically typed (any of Double, Float, Long, Integer).
     * The smallest of the child values will be returned as the result as the
     * widest of the numeric types.
     *
     * @return the smallest of the values returned by its child nodes
     */
    @Override
    public Object evaluate() {
        int arity = getArity();

        Object[] childValues = new Object[arity];
        Class<?>[] types = new Class<?>[arity];
        for (int i = 0; i < arity; i++) {
            childValues[i] = getChild(i).evaluate();
            types[i] = childValues[i].getClass();
        }
        Class<?> returnType = TypeUtil.widestNumberType(types);

        //noinspection IfStatementWithTooManyBranches
        if (returnType == Double.class) {
            double min = Double.MAX_VALUE;
            for (int i = 0; i < arity; i++) {
                double value = NumericUtils.asDouble(childValues[i]);
                min = Math.min(value, min);
            }
            return min;
        } else if (returnType == Float.class) {
            float min = Float.MAX_VALUE;
            for (int i = 0; i < arity; i++) {
                float value = NumericUtils.asFloat(childValues[i]);
                min = Math.min(value, min);
            }
            return min;
        } else if (returnType == Integer.class) {
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < arity; i++) {
                int value = NumericUtils.asInteger(childValues[i]);
                min = Math.min(value, min);
            }
            return min;
        } else if (returnType == Long.class) {
            long min = Long.MAX_VALUE;
            for (int i = 0; i < arity; i++) {
                long value = NumericUtils.asLong(childValues[i]);
                min = Math.min(value, min);
            }
            return min;
        } else {
            return null;
        }
    }

    /**
     * Returns the identifier of this function which is MIN
     *
     * @return this node's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns this function node's return type for the given child input types.
     * If there is the correct number of numeric input types then the return
     * type will be the widest of those types. In all other cases this method
     * will return <code>null</code> to indicate that the inputs are invalid.
     *
     * @return the widest numeric type or null if the input types are invalid.
     */
    @Override
    public Class dataType(Class... inputTypes) {
        if (inputTypes.length == getArity()) {
            return TypeUtil.widestNumberType(inputTypes);
        }
        return null;
    }
}
