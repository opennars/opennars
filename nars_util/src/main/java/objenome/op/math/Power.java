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
import objenome.util.TypeUtil;

/**
 * A node which performs the mathematical operation of exponentiation
 *
 * @since 2.0
 */
public class Power extends MathNode {

    public static final String IDENTIFIER = "POW";

    /**
     * Constructs a PowerFunction with two <code>null</code> children.
     */
    public Power() {
        this(null, null);
    }

    /**
     * Constructs a PowerFunction with two numerical child nodes. When
     * evaluated, both children will be evaluated and the first will be raised
     * to the power of the second.
     *
     * @param base The first child node - the base.
     * @param exponent The second child node - the exponent.
     */
    public Power(Node base, Node exponent) {
        super(base, exponent);
    }

    /**
     * Evaluating a <code>PowerFunction</code> involves raising the first child
     * to the power of the second, after both children are evaluated. For
     * performance, this function is evaluated lazily. The second child is
     * evaluated first, if it evaluates to <code>0.0</code> then the result will
     * always be <code>1.0</code> and the first child will not be evaluated at
     * all.
     */
    @Override
    public double asDouble() {
        return Math.pow(getChildEvaluated(0), getChildEvaluated(1));
    }


    /**
     * Returns the identifier of this function which is POW
     *
     * @return this node's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns this function node's return type for the given child input types.
     * If there are two input types of a numeric type then the return type will
     * be Double. In all other cases this method will return <code>null</code>
     * to indicate that the inputs are invalid.
     *
     * @return the Double class or null if the input type is invalid.
     */
    @Override
    public Class dataType(Class... inputTypes) {
        return (inputTypes.length == 2) && TypeUtil.isAllNumericType(inputTypes) ? Double.class : null;
    }
}
