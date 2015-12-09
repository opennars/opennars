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
 * A node which performs the mathematical function of division.
 *
 * Division can be performed on inputs of the following types:
 * <ul>
 * <li>Integer</li>
 * <li>Long</li>
 * <li>Float</li>
 * <li>Double</li>
 * </ul>
 *
 * Division can be performed between mixed types, with a widening operation
 * performed and the result being of the wider of the two types. This version of
 * the function is protected, so if the divisor input is 0.0 then the result
 * will be the protected value, which by default is 0.0 to protect against
 * divide by zero.
 *
 * @since 2.0
 */
public class DivisionProtected extends MathNode {

    public static final String IDENTIFIER = "DIV";

    // The value returned in place of divide-by-zero.
    private Double protectionValue;

    /**
     * Constructs a ProtectedDivisionFunction with two <code>null</code>
     * children. By default a protection value of 0.0 is used.
     */
    public DivisionProtected() {
        this(null, null);
    }

    /**
     * Constructs a ProtectedDivisionFunction with two <code>null</code>
     * children.
     *
     * @param protectionValue a double value to return in the case of
     * divide-by-zeros.
     */
    public DivisionProtected(double protectionValue) {
        this(null, null, protectionValue);
    }

    /**
     * Constructs a ProtectedDivisionFunction with two numerical child nodes. A
     * default protection value that is returned in the case of divide-by-zero
     * is set as 0.0.
     *
     * @param dividend The first child node - the dividend.
     * @param divisor The second child node - the divisor.
     */
    public DivisionProtected(Node dividend, Node divisor) {
        this(dividend, divisor, 0.0);
    }

    /**
     * Constructs a ProtectedDivisionFunction with two numerical child nodes.
     *
     * @param dividend The first child node - the dividend.
     * @param divisor The second child node - the divisor.
     * @param protectionValue a double value to return in the case of
     * divide-by-zeros.
     */
    public DivisionProtected(Node dividend, Node divisor, double protectionValue) {
        super(dividend, divisor);

        this.protectionValue = protectionValue;
    }

//    /**
//     * Evaluates this function. Both child nodes are evaluated, the result of
//     * both must be of numeric type. If necessary, the inputs are widened to
//     * both be of the same type, then division is performed and the return value
//     * will be of that wider type. If the divisor resolves to zero then the
//     * result returned will be the protection value to avoid the divide by zero
//     * issue.
//     *
//     * @return the result of dividing the value returned from the first child by
//     * the value from the second child, or the protected value if the divisor is
//     * zero
//     */
//    @Override
//    public Object evaluate() {
//        Object c1 = getChild(0).evaluate();
//        Object c2 = getChild(1).evaluate();
//
//        Class<?> returnType = TypeUtil.widestNumberType(c1.getClass(), c2.getClass());
//
//        if (returnType == Double.class) {
//            // Divide as doubles.
//            double d1 = NumericUtils.asDouble(c1);
//            double d2 = NumericUtils.asDouble(c2);
//
//            return (d2 == 0) ? NumericUtils.asDouble(protectionValue) : (d1 / d2);
//        } else if (returnType == Float.class) {
//            // Divide as floats.
//            float f1 = NumericUtils.asFloat(c1);
//            float f2 = NumericUtils.asFloat(c2);
//
//            return (f2 == 0) ? NumericUtils.asFloat(protectionValue) : (f1 / f2);
//        } else if (returnType == Long.class) {
//            // Divide as longs.
//            long l1 = NumericUtils.asLong(c1);
//            long l2 = NumericUtils.asLong(c2);
//
//            return (l2 == 0) ? NumericUtils.asLong(protectionValue) : (l1 / l2);
//        } else if (returnType == Integer.class) {
//            // Divide as integers.
//            int i1 = NumericUtils.asInteger(c1);
//            int i2 = NumericUtils.asInteger(c2);
//
//            return (i2 == 0) ? NumericUtils.asInteger(protectionValue) : (i1 / i2);
//        }
//
//        return null;
//    }

    /**
     * Returns the identifier of this function which is PDIV
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

    /**
     * Sets the protection value that should be returned in case of
     * divide-by-zero.
     *
     * @param protectionValue the value to be returned if divide-by-zero is
     * attempted.
     */
    public void setProtectionValue(Double protectionValue) {
        this.protectionValue = protectionValue;
    }

    /**
     * Returns the protection value that will be returned in the case of
     * divide-by-zero.
     *
     * @return the protectionValue the value that will be returned in the case
     * of divide-by-zero.
     */
    public Double getProtectionValue() {
        return protectionValue;
    }


    @Override
    public Node normalize() {
        Node a = getChild(0);
        Node b = getChild(1);
        if (a.equals(b)) return one;

        double an = getChildConstantValue(0);
        double bn = getChildConstantValue(1);


        if (Double.isFinite(an) && Double.isFinite(bn)) {
            return new Doubliteral(asDouble());
        }
        if (bn == 1) return a;
        if (an == 0) return zero;

        //if (bn == 0) return new Negate

        return super.normalize();
    }

    @Override
    public double asDouble() {
        double a = getChildEvaluated(0);
        double b = getChildEvaluated(1);
        return a/b;
    }

}
