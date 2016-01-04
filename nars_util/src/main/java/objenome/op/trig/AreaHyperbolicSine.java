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
 * A node which performs the inverse hyperbolic trigonometric function of area
 * hyperbolic sine, called ARSINH
 *
 * @since 2.0
 */
public class AreaHyperbolicSine extends Node {

    public static final String IDENTIFIER = "ARSINH";

    /**
     * Constructs an AreaHyperbolicSineFunction with one <code>null</code>
     * child.
     */
    public AreaHyperbolicSine() {
        this(null);
    }

    /**
     * Constructs a AreaHyperbolicSineFunction with one numerical child node.
     *
     * @param child the child node.
     */
    public AreaHyperbolicSine(Node child) {
        super(child);
    }

    /**
     * Returns the area (inverse) hyperbolic sine of a <code>double</code>
     * value.
     * <p>
     * The identity is:
     * <p>
     * <i>arsinh(x)&nbsp;=&nbsp;ln(x&nbsp;+&nbsp;sqrt(x<sup>2</sup>&nbsp;+&nbsp;1))</i>
     * <p>
     * Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is infinite, then the result is an infinity with the
     * same sign as the argument.
     * <li>If the argument is zero, then the result is a zero with the same sign
     * as the argument.
     * </ul>
     *
     * @param d the number whose inverse hyperbolic sine is sought
     * @return the inverse hyperbolic sine of <code>d</code>
     */
    public static double arsinh(double d) {
        return Double.isInfinite(d) ? d : (d == 0.0) ? d : Math.log(d + Math.sqrt(d * d + 1.0));
    }

    /**
     * Evaluates this function. The child node is evaluated, the result of which
     * must be a numeric type (one of Double, Float, Long, Integer). The area
     * hyperbolic sine of this value becomes the result of this method as a
     * double value.
     *
     * @return area hyperbolic sine of the value returned by the child
     */
    @Override
    public Double evaluate() {
        Object c = getChild(0).evaluate();

        return arsinh(NumericUtils.asDouble(c));
    }

    /**
     * Returns the identifier of this function which is ARSINH.
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
