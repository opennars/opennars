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

import org.opennars.core.NALTest;
import org.opennars.interfaces.pub.Reasoner;
import org.opennars.main.Nar;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;


/** tests performance of NAL, but can also uncover bugs when NAL runs with a heavy and long load
 *  useful for examining with a profiler.
 */
public class NALStressMeasure  {
    public static double perfNAL(final Reasoner n, final String path, final int extraCycles, final int repeats, final int warmups, final boolean gc) {
        
        final String example = NALTest.getExample(path);
        
        final Performance p = new Performance(path, repeats, warmups, gc) {
            long totalCycles;
            
            @Override
            public void init() {
                System.out.print(name + ": ");
                totalCycles = 0;
            }

            @Override
            public void run(final boolean warmup) {
                n.reset();
                n.addInput(example);
                n.cycles(1);
                n.cycles(extraCycles);
                
                totalCycles += n.time();
            }
                        

            @Override
            public Performance print() {                
                super.print();
                System.out.print(", " + df.format(getCycleTimeMS() / totalCycles * 1000.0) + " uS/cycle, " + (((float)totalCycles)/(warmups+repeats)) + " cycles/run");
                return this;
                
            }
            
            @Override
            public Performance printCSV(final boolean finalComma) {
                super.printCSV(true);
                System.out.print(df.format(getCycleTimeMS() / totalCycles * 1000.0) + ", " + (((float)totalCycles)/(warmups+repeats)));
                if (finalComma)
                    System.out.print(", ");
                return this;
                
            }

        };
        p.print();
        System.out.println();

        /*p.printCSV(false);
        System.out.println();*/
        
        return p.getCycleTimeMS();
                   
    }
    
    public static void test(final Reasoner n) {
        final int repeats = 1;
        final int warmups = 0;
        final int extraCycles = 5000;

        final Collection c = NALTest.params();
        double totalTime = 0;
        for (final Object o : c) {
            final String examplePath = (String)((Object[])o)[0];
            totalTime += perfNAL(n,examplePath,extraCycles,repeats,warmups,true);
        }
        System.out.println("\n\nTotal mean runtime (ms): " + totalTime);        
    }
    
    public static void main(final String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        final Reasoner nd = new Nar();
        test(nd);
    }
}
