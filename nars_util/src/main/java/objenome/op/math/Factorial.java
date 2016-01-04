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
 * A node which performs the mathematical function of factorial, which is
 * normally expressed with an exclamation mark !
 *
 * For example: 5! = 5 x 4 x 3 x 2 x 1 = FACTORIAL 5
 *
 * @since 2.0
 */
public class Factorial extends Node {

    public static final String IDENTIFIER = "FACTORIAL";

    /**
     * Constructs a FactorialFunction with one <code>null</code> child.
     */
    public Factorial() {
        this(null);
    }

    /**
     * Constructs a FactorialFunction with one numerical child node.
     *
     * @param child the child node.
     */
    public Factorial(Node child) {
        super(child);
    }

    /**
     * Tests whether the given class type is for an integer type (one of
     * <code>Byte, Short, Integer, Long</code>)
     *
     * @param type the type to check
     * @return <code>true</code> if it is a primitive integer type,
     * <code>false</code> otherwise
     */
    public static boolean isIntegerType(Class<?> type) {
        return ((type == Integer.class) || (type == Long.class) || (type == Byte.class) || (type == Short.class));
    }

    /**
     * Evaluates this function. The child node is evaluated, the result of which
     * must be of an integer type (one of Byte, Short, Integer, Long). If the
     * value is negative, then its absolute value is used to avoid a divide by
     * zero. The factorial function is performed on this value and the result
     * returned as an Integer if the input was one of Byte, Short or Integer and
     * returned as a Long if the input is a Long.
     *
     * @return factorial of the value returned from the child
     */
    @Override
    public Object evaluate() {
        Object c = getChild(0).evaluate();

        long cint = Math.abs(NumericUtils.asLong(c));

        long factorial = 1;
        for (long i = 1; i <= cint; i++) {
            factorial = factorial * i;
        }

        return c instanceof Long ? factorial : (int) factorial;
    }

    /**
     * Returns the identifier of this function which is FACTORIAL
     *
     * @return this node's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns this function node's return type for the given child input types.
     * If there is one input type of Byte, Short or Integer then the return type
     * will be Integer, if it is Long then the return type will also be Long. In
     * all other cases this method will return <code>null</code> to indicate
     * that the inputs are invalid.
     *
     * @return the Integer or Long class or null if the input type is invalid.
     */
    @Override
    public Class dataType(Class... inputTypes) {
        if ((inputTypes.length == 1) && isIntegerType(inputTypes[0])) {
            return TypeUtil.widestNumberType(inputTypes[0]);
        }

        return null;
    }
}
