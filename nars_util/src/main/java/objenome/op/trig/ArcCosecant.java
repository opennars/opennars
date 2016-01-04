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
 * A node which performs the inverse trigonometric function of arc-cosecant,
 * called ARCCSC
 *
 * @since 2.0
 */
public class ArcCosecant extends Node {

    public static final String IDENTIFIER = "ARCCSC";

    /**
     * Constructs an ArcCosecantFunction with one <code>null</code> child.
     */
    public ArcCosecant() {
        this(null);
    }

    /**
     * Constructs an ArcCosecantFunction with one numerical child node.
     *
     * @param child the child node.
     */
    public ArcCosecant(Node child) {
        super(child);
    }

    /**
     * Returns the inverse cosecant of a <code>double</code> value
     *
     * @param d the number whose inverse cosecant is sought
     * @return the inverse cosecant of <code>d</code>
     */
    public static double arccsc(double d) {
        return Math.asin(1.0 / d);
    }

    /**
     * Evaluates this function. The child node is evaluated, the result of which
     * must be a numeric type (one of Double, Float, Long, Integer). The
     * arc-cosecant of this value becomes the result of this method as a double
     * value.
     *
     * @return arc-cosecant of the value returned by the child
     */
    @Override
    public Double evaluate() {
        Object c = getChild(0).evaluate();

        return arccsc(NumericUtils.asDouble(c));
    }

    /**
     * Returns the identifier of this function which is ARCCSC
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
