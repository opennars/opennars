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
import objenome.op.Numeric1d;
import org.apache.commons.math3.util.FastMath;

/**
 * A node which performs the hyperbolic trigonometric function of hyperbolic
 * tangent, called TANH
 *
 * @since 2.0
 */
public class HyperbolicTangent extends Numeric1d {


    /**
     * Evaluates this function. The child node is evaluated, the result of which
     * must be a numeric type (one of Double, Float, Long, Integer). The
     * hyperbolic tangent of this value becomes the result of this method as a
     * double value.
     *
     * @return hyperbolic tangent of the value returned by the child
     */
    @Override
    public double value(double x) {
        return FastMath.tanh(x);
    }


    public HyperbolicTangent(Node child) {
        super(child);
    }

    public HyperbolicTangent() {
        this(null);
    }



    public static final String IDENTIFIER = "TANH";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

}
