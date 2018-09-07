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
package org.opennars.core.bag;

import org.junit.Test;
import org.opennars.storage.Distributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Report the distribution of the bag
 */
public class DistributorAnalyzer {

    @Test public void testDistributorProbabilities() {
        
        final int levels = 20;
        final Distributor d = Distributor.get(levels);
        final int[] count = new int[levels];
        
        double total = 0;
        for (final short x : d.order) {
            count[x]++;
            total++;
        }
        
        final List<Double> probability = new ArrayList(levels);
        for (int i = 0; i < levels; i++) {
            probability.add( count[i] / total);
        }
        
        final List<Double> probabilityActiveAdjusted = new ArrayList(levels);
        final double activeIncrease = 0.009;
        final double dormantDecrease = ((0.1 * levels) * activeIncrease) / ((1.0 - 0.1) * levels);
        for (int i = 0; i < levels; i++) {
            double p = count[i] / total;
            final double pd = i < ((1.0 - 0.1) * levels) ? -dormantDecrease : activeIncrease;
            
            p+=pd;
            
            probabilityActiveAdjusted.add( p );
            System.out.println((i/((double)levels)) + "\t" + p);
        }
        //System.out.println(probabilityActiveAdjusted);
        
    }

    
}
