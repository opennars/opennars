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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.inference;

import com.googlecode.opennars.main.Parameters;

/**
 * Common functions on real numbers in [0,1].
 */
public class UtilityFunctions {

    // arithmetic average
    public static float aveAri(float... arr) {
        float sum = 0;
        for(int i=0; i<arr.length; i++)
            sum += arr[i];
        return sum / arr.length;
    }

    public static float or(float... arr) {
        float product = 1;
        for(int i=0; i<arr.length; i++)
            product *= (1 - arr[i]);
        return 1 - product;
    }
    
    public static float and(float... arr) {
        float product = 1;
        for(int i=0; i<arr.length; i++)
            product *= arr[i];
        return product;
    }
    
    // weight to confidence
    public static float w2c(float v) {
        return v / (v + Parameters.NEAR_FUTURE);
    }    
}

