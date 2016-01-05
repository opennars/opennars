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
package nars.nal;

import nars.Global;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Common (static) functions on real numbers, mostly in [0,1].
 */
public class UtilityFunctions   {

    protected UtilityFunctions() {
    }

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
    
    //may be more efficient than the for-loop version above, for 2 params
    public static float and(float a, float b) {
        return a*b;
    }

    public static float and(float a, float b, float c) {
        return a*b*c;
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
        return 1.0f - product;
    }
    
    public static float or(float a, float b) {
        return 1.0f -((1.0f -a)*(1.0f -b));
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
            if (f == 0) return 0;
            product *= f;
        }
        return (float) pow(product, 1.00 / arr.length);
    }

    //may be more efficient than the for-loop version above, for 2 params
    public static float aveGeo(float a, float b) {
        if ((a == 0)||(b==0)) return 0; //early result avoiding pow()
        return (float)sqrt(a*b);
    }

    /**
     * A function to convert weight to confidence
     * @param w Weight of evidence, a non-negative real number
     * @return The corresponding confidence, in [0, 1)
     */
    public static float w2c(float w) {
        return w / (w + Global.HORIZON);
    }

}

