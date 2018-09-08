/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.inference;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import org.opennars.main.Parameters;

/**
 * Common functions on real numbers, mostly in [0,1].
 *
 * @author Pei Wang
 * @author Patrick Hammer
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
        return 1 - product;
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

    /**
     * A function where the output is the geometric average the inputs
     * @param arr The inputs, each in [0, 1]
     * @return The geometric average the inputs
     */
    public final static float aveGeo(final float... arr) {
        float product = 1;
        for (final float f : arr) {
            product *= f;
        }
        
        if (arr.length == 2) {
            return (float)sqrt(arr[0]*arr[1]);
        }
        return (float) pow(product, 1.00 / arr.length);
    }

    /**
     * A function to convert weight to confidence
     * @param w Weight of evidence, a non-negative real number
     * @param narParameters parameters of the reasoner
     * @return The corresponding confidence, in [0, 1)
     */
    public final static float w2c(final float w, Parameters narParameters) {
        return w / (w + narParameters.HORIZON);
    }

    /**
     * A function to convert confidence to weight
     * @param c confidence, in [0, 1)
     * @param narParameters parameters of the reasoner
     * @return The corresponding weight of evidence, a non-negative real number
     */
    public final static float c2w(final float c, Parameters narParameters) {
        return narParameters.HORIZON * c / (1 - c);
    }
}

