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
public final class MathUtils {

    final static double EPSILON = 0.0001;

    public static boolean equalEnough(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    private MathUtils() {
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
     * Returns the inverse cotangent of a <code>double</code> value
     *
     * @param d the number whose inverse cotangent is sought
     * @return the inverse cotangent of <code>d</code>
     */
    public static double arccot(double d) {
        return Math.atan(1.0 / d);
    }

    /**
     * Returns the inverse secant of a <code>double</code> value
     *
     * @param d the number whose inverse secant is sought
     * @return the inverse secant of <code>d</code>
     */
    public static double arcsec(double d) {
        return Math.acos(1.0 / d);
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
     * Returns the cotangent of a <code>double</code> value
     *
     * @param d the number whose cotangent is sought
     * @return the cotangent of <code>d</code>
     */
    public static double cot(double d) {
        return 1 / Math.tan(d);
    }

    /**
     * Returns the inverse hyperbolic cosine of a <code>double</code> value.
     * Note that <i>cosh(acosh(x))&nbsp;=&nbsp;x</i>; this function arbitrarily
     * returns the positive branch.
     * <p>
     * The identity is:
     * <p>
     * <i>arcosh(x)&nbsp;=&nbsp;ln(x&nbsp;&nbsp;sqrt(x<sup>2</sup>&nbsp;-&nbsp;1))</i>
     * <p>
     * Special cases:
     * <ul>
     * <li>If the argument is NaN or less than one, then the result is NaN.
     * <li>If the argument is a positive infinity, then the result is (positive)
     * infinity.
     * <li>If the argument is one, then the result is (positive) zero.
     * </ul>
     *
     * @param d the number whose inverse hyperbolic cosine is sought
     * @return the inverse hyperbolic cosine of <code>d</code>
     */
    public static double arcosh(double d) {
        return Math.log(d + Math.sqrt(d * d - 1.0));
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
     * Returns the inverse hyperbolic tangent of a <code>double</code> value.
     * <p>
     * The identity is:
     * <p>
     * <i>artanh(x)&nbsp;=&nbsp;(1/2)*ln((1&nbsp;+&nbsp;x)/(1&nbsp;-&nbsp;x))</i>
     * <p>
     * Special cases:
     * <ul>
     * <li>If the argument is NaN, an infinity, or has a modulus of greater than
     * one, then the result is NaN.
     * <li>If the argument is plus or minus one, then the result is infinity
     * with the same sign as the argument.
     * <li>If the argument is zero, then the result is a zero with the same sign
     * as the argument.
     * </ul>
     *
     * @param d a <code>double</code> specifying the value whose inverse
     * hyperbolic tangent is sought
     * @return a <code>double</code> specifying the inverse hyperbolic tangent
     * of <code>d</code>
     */
    public static double artanh(double d) {
        return (d != 0.0) ? (Math.log(1.0 + d) - Math.log(1.0 - d)) / 2.0 : d;
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
