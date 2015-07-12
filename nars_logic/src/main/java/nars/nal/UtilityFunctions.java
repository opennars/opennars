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
 * Common functions on real numbers, mostly in [0,1].
 */
public class UtilityFunctions {

    
    
    /**
     * A function where the output is conjunctively determined by the inputs
     * @param arr The inputs, each in [0, 1]
     * @return The output that is no larger than each input
     */
    public final static float and(final float... arr) {        
        float product = 1;
        for (final float f : arr) {
            product *= f;
        }
        return product;
    }
    
    //may be more efficient than the for-loop version above, for 2 params
    public final static float and(final float a, final float b) {
        return a*b;
    }

    public final static float and(final float a, final float b, final float c) {
        return a*b*c;
    }

    
    /**
     * A function where the output is disjunctively determined by the inputs
     * @param arr The inputs, each in [0, 1]
     * @return The output that is no smaller than each input
     */
    public final static float or(final float... arr) {
        float product = 1;
        for (final float f : arr) {
            product *= (1 - f);
        }
        return 1f - product;
    }
    
    public final static float or(final float a, final float b) {
        return 1f-((1f-a)*(1f-b));
    }
    
    /**
     * A function where the output is the arithmetic average the inputs
     * @param arr The inputs, each in [0, 1]
     * @return The arithmetic average the inputs
     */
    public final static float aveAri(final float... arr) {
        float sum = 0;
        for (final float f : arr) {
            sum += f;
        }
        return sum / arr.length;
    }
    
    //may be more efficient than the for-loop version above, for 2 params
    public final static float aveAri(final float a, final float b) {
        return (a + b)/2f;
    }

    /**
     * A function where the output is the geometric average the inputs
     * @param arr The inputs, each in [0, 1]
     * @return The geometric average the inputs
     */
    public final static float aveGeo(final float... arr) {
        float product = 1;
        for (final float f : arr) {
            if (f == 0) return 0;
            product *= f;
        }
        return (float) pow(product, 1.00 / arr.length);
    }

    //may be more efficient than the for-loop version above, for 2 params
    public final static float aveGeo(final float a, final float b) {
        if ((a == 0)||(b==0)) return 0; //early result avoiding pow()
        return (float)sqrt(a*b);
    }
    
    //may be more efficient than the for-loop version above, for 3 params
    public final static float aveGeo(final float a, final float b, final float c) {
        if ((a == 0)||(b==0)||(c==0)) return 0; //early result avoiding pow()
        return (float)pow(a*b*c, 1.0/3.0);
    }

    public final static boolean aveGeoNotLessThan(final float min, final float a, final float b, final float c) {
        final float minCubed = min*min*min; //cube both sides
        return (a*b*c) >= minCubed;
    }
    
    /**
     * A function to convert weight to confidence
     * @param w Weight of evidence, a non-negative real number
     * @return The corresponding confidence, in [0, 1)
     */
    public final static float w2c(final float w) {
        return w / (w + Global.HORIZON);
    }

    /**
     * A function to convert confidence to weight
     * @param c confidence, in [0, 1)
     * @return The corresponding weight of evidence, a non-negative real number
     */
    public final static float c2w(final float c) {
        return Global.HORIZON * c / (1 - c);
    }
}

