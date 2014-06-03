/*
 * UtilityFunctions.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.inference;

import nars.main_nogui.Parameters;

/**
 * Common functions on real numbers, mostly in [0,1].
 */
public class UtilityFunctions {

    /**
     * A function where the output is conjunctively determined by the inputs
     * @param arr The inputs, each in [0, 1]
     * @return The output that is no larger than each input
     */
    public static float and(float... arr) {
        float product = 1;
        for (float f : arr) {
            product *= f;
        }
        return product;
    }

    /**
     * A function where the output is disjunctively determined by the inputs
     * @param arr The inputs, each in [0, 1]
     * @return The output that is no smaller than each input
     */
    public static float or(float... arr) {
        float product = 1;
        for (float f : arr) {
            product *= (1 - f);
        }
        return 1 - product;
    }

    /**
     * A function where the output is the arithmetic average the inputs
     * @param arr The inputs, each in [0, 1]
     * @return The arithmetic average the inputs
     */
    public static float aveAri(float... arr) {
        float sum = 0;
        for (float f : arr) {
            sum += f;
        }
        return sum / arr.length;
    }

    /**
     * A function where the output is the geometric average the inputs
     * @param arr The inputs, each in [0, 1]
     * @return The geometric average the inputs
     */
    public static float aveGeo(float... arr) {
        float product = 1;
        for (float f : arr) {
            product *= f;
        }
        return (float) Math.pow(product, 1.00 / arr.length);
    }

    /**
     * A function to convert weight to confidence
     * @param w Weight of evidence, a non-negative real number
     * @return The corresponding confidence, in [0, 1)
     */
    public static float w2c(float w) {
        return w / (w + Parameters.HORIZON);
    }

    /**
     * A function to convert confidence to weight
     * @param c confidence, in [0, 1)
     * @return The corresponding weight of evidence, a non-negative real number
     */
    public static float c2w(float c) {
        return Parameters.HORIZON * c / (1 - c);
    }
}

