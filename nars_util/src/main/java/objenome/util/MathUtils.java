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
package objenome.util;

/**
 * This class provides static utility methods for mathematical functions.
 *
 * <p>
 * <i>Thanks to the <a href="http://jsci.sourceforge.net/">JSci science API</a>
 * for some of these.</i>
 */
public   enum MathUtils {
    ;

    static final double EPSILON = 0.0001;

    public static boolean equalEnough(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    /**
     * Returns the cosecant of a <code>double</code> value
     *
     * @param d the number whose cosecant is sought
     * @return the cosecant of <code>d</code>
     */
    public static double csc(double d) {
        return 1.0 / Math.sin(d);
    }

    /**
     * Returns the secant of a <code>double</code> value
     *
     * @param d the number whose secant is sought
     * @return the secant of <code>d</code>
     */
    public static double sec(double d) {
        return 1.0 / Math.cos(d);
    }

    /**
     * Returns the hyperbolic secant of a <code>double</code> value.
     * <p>
     * The identity is:
     * <p>
     * <i>sech(x)&nbsp;=&nbsp;(2/(e<sup>x</sup>&nbsp;+&nbsp;e<sup>-x</sup>)</i>,
     * in other words, 1/{@linkplain Math#cosh cosh(<i>x</i>)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is an infinity (positive or negative), then the
     * result is <code>+0.0</code>.
     * </ul>
     *
     * @param d the number whose hyperbolic secant is sought
     * @return the hyperbolic secant of <code>d</code>
     */
    public static double sech(double d) {
        return 1.0 / Math.cosh(d);
    }

    /**
     * Returns the hyperbolic cosecant of a <code>double</code> value.
     * <p>
     * The identity is:
     * <p>
     * <i>csch(x)&nbsp;=&nbsp;(2/(e<sup>x</sup>&nbsp;-&nbsp;e<sup>-x</sup>)</i>,
     * in other words, 1/{@linkplain Math#sinh sinh(<i>x</i>)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is zero, then the result is an infinity with the same
     * sign as the argument.
     * <li>If the argument is positive infinity, then the result is
     * <code>+0.0</code>.
     * <li>If the argument is negative infinity, then the result is
     * <code>-0.0</code>.
     * </ul>
     *
     * @param d the number whose hyperbolic cosecant is sought
     * @return the hyperbolic cosecant of <code>d</code>
     */
    public static double csch(double d) {
        return 1.0 / Math.sinh(d);
    }

    /**
     * Returns the hyperbolic cotangent of a <code>double</code> value.
     * <p>
     * The identity is:
     * <p>
     * <i>coth(x)&nbsp;=&nbsp;(e<sup>x</sup>&nbsp;+&nbsp;e<sup>-x</sup>)/(e<sup>x</sup>&nbsp;-&nbsp;e<sup>-x</sup>)</i>,
     * in other words,
     * {@linkplain Math#cosh cosh(<i>x</i>)}/{@linkplain Math#sinh sinh(<i>x</i>)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is zero, then the result is an infinity with the same
     * sign as the argument.
     * <li>If the argument is positive infinity, then the result is
     * <code>+1.0</code>.
     * <li>If the argument is negative infinity, then the result is
     * <code>-1.0</code>.
     * </ul>
     *
     * @param d the number whose hyperbolic cotangent is sought
     * @return the hyperbolic cotangent of <code>d</code>
     */
    public static double coth(double d) {
        return 1.0 / Math.tanh(d);
    }
}
