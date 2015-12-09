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

import objenome.op.Doubliteral;
import objenome.op.Node;
import objenome.util.TypeUtil;

/**
 * A node which performs the mathematical function of addition.
 *
 * Addition can be performed on inputs of the following types:
 * <ul>
 * <li>Integer</li>
 * <li>Long</li>
 * <li>Float</li>
 * <li>Double</li>
 * </ul>
 *
 * Addition can be performed between mixed types, with a widening operation
 * performed and the result being of the wider of the two types.
 *
 * @since 2.0
 */
public class Add extends MathNode {

    public static final String IDENTIFIER = "ADD";

    /**
     * Constructs an AddFunction with two <code>null</code> children.
     */
    public Add() {
        this(null, null);
    }

    /**
     * Constructs an AddFunction with two numerical child nodes. When evaluated,
     * both children will be evaluated and added together.
     *
     * @param child1 The first child node.
     * @param child2 The second child node.
     */
    public Add(Node child1, Node child2) {
        super(child1, child2);
    }

//    /**
//     * Evaluates this function. Both child nodes are evaluated, the result of
//     * both must be of numeric type. If necessary, the inputs are widened to
//     * both be of the same type, then addition is performed and the return value
//     * will be of that wider type.
//     *
//     * @return the sum of the inputs after evaluating the two children
//     */
//    @Override
//    public Object evaluate() {
//        Object c1 = getChild(0).evaluate();
//        Object c2 = getChild(1).evaluate();
//
//        Class<?> returnType = TypeUtil.widestNumberType(c1.getClass(), c2.getClass());
//
//        if (returnType == Double.class) {
//            // Add as doubles.
//            double d1 = NumericUtils.asDouble(c1);
//            double d2 = NumericUtils.asDouble(c2);
//
//            return d1 + d2;
//        } else if (returnType == Float.class) {
//            // Add as floats.
//            float f1 = NumericUtils.asFloat(c1);
//            float f2 = NumericUtils.asFloat(c2);
//
//            return f1 + f2;
//        } else if (returnType == Long.class) {
//            // Add as longs.
//            long l1 = NumericUtils.asLong(c1);
//            long l2 = NumericUtils.asLong(c2);
//
//            return l1 + l2;
//        } else if (returnType == Integer.class) {
//            // Add as integers.
//            int i1 = NumericUtils.asInteger(c1);
//            int i2 = NumericUtils.asInteger(c2);
//
//            return i1 + i2;
//        }
//
//        return null;
//    }

    /**
     * Returns the identifier of this function which is ADD
     *
     * @return this node's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns this function node's return type for the given child input types.
     * If there are two input types of numeric type then the return type will be
     * the wider of those numeric types. In all other cases this method will
     * return <code>null</code> to indicate that the inputs are invalid.
     *
     * @return A numeric class or null if the input type is invalid.
     */
    @Override
    public Class dataType(Class... inputTypes) {
        if (inputTypes.length == 2) {
            return TypeUtil.widestNumberType(inputTypes);
        }
        return null;
    }

    @Override
    public Node normalize() {
        Node a = getChild(0);
        Node b = getChild(1);
        if (a.equals(b)) return new Multiply(a, two); //allows the numeric parameter to undergo mutation

        double an = getChildConstantValue(0);
        double bn = getChildConstantValue(1);


        if (Double.isFinite(an) && Double.isFinite(bn)) {
            if (an == -bn) return zero;
            return new Doubliteral(an + bn);
        }
        if (an == 0) return b;
        if (bn == 0) return a;

        return super.normalize();
    }

    @Override
    public double asDouble() {
        double a = getChildEvaluated(0);
        double b = getChildEvaluated(1);
        return a + b;
    }

}
