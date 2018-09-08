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
package org.opennars.perf;

import java.text.DecimalFormat;


public abstract class Performance {
    public final int repeats;
    final String name;
    private long totalTime;
    private long totalMemory;
    protected final DecimalFormat df = new DecimalFormat("#.###");

    public Performance(final String name, final int repeats, final int warmups) {
        this(name, repeats, warmups, true);
    }
    
    public Performance(final String name, final int repeats, int warmups, final boolean gc) {
        this.repeats = repeats;        
        this.name = name;
        
        init();
        
        totalTime = 0;
        totalMemory = 0;

        final int total = repeats+warmups;
        for (int r = 0; r < total; r++) {

            if (gc) {
                System.gc();
            }

            final long usedMemStart = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            
            final long start = System.nanoTime();
            
            run(warmups != 0);

            if (warmups == 0) {
                totalTime += System.nanoTime() - start;
                totalMemory += (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - usedMemStart;
            }
            else
                warmups--;
        }
    }    
 
    public Performance print() {
        System.out.print(": " + df.format(getCycleTimeMS()) + "ms/run, ");
        System.out.print(df.format(totalMemory/repeats/1024.0) + " kb/run");
        return this;
    }
    public Performance printCSV(final boolean finalComma) {
        System.out.print(name + ", " + df.format(getCycleTimeMS()) + ", ");
        System.out.print(df.format(totalMemory/repeats/1024.0));
        if (finalComma)
            System.out.print(",");
        return this;
    }    
            
    abstract public void init();
    abstract public void run(boolean warmup);
    
    
    public double getCycleTimeMS() {
        return totalTime/repeats/1000000.0;
    }
}
