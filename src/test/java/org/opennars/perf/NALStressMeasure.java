/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.perf;

import java.util.Collection;
import org.opennars.main.NAR;
import org.opennars.core.NALTest;


/** tests performance of NAL, but can also uncover bugs when NAL runs with a heavy and long load
 *  useful for examining with a profiler.
 */
public class NALStressMeasure  {
    
    public static void perfNAL(final String path, final int extraCycles, int repeats, int warmups) {
        //perfNAL(newNAR(), path, extraCycles, repeats, warmups, true);
    }
    
    public static double perfNAL(final NAR n, final String path, final int extraCycles, int repeats, int warmups, boolean gc) {
        
        final String example = NALTest.getExample(path);
        
        Performance p = new Performance(path, repeats, warmups, gc) {
            long totalCycles;
            
            @Override
            public void init() {
                System.out.print(name + ": ");
                totalCycles = 0;
            }

            @Override
            public void run(boolean warmup) {
                n.reset();
                n.addInput(example);
                n.cycles(1);
                n.cycles(extraCycles);
                
                totalCycles += n.memory.time();
            }
                        

            @Override
            public Performance print() {                
                super.print();
                System.out.print(", " + df.format(getCycleTimeMS() / totalCycles * 1000.0) + " uS/cycle, " + (((float)totalCycles)/(warmups+repeats)) + " cycles/run");
                return this;
                
            }
            
            @Override
            public Performance printCSV(boolean finalComma) {
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
    
    public static void test(NAR n) {
        int repeats = 1;
        int warmups = 0;
        int extraCycles = 5000;

        Collection c = NALTest.params();
        double totalTime = 0;
        for (Object o : c) {
            String examplePath = (String)((Object[])o)[0];
            totalTime += perfNAL(n,examplePath,extraCycles,repeats,warmups,true);
        }
        System.out.println("\n\nTotal mean runtime (ms): " + totalTime);        
    }
    
    public static void main(String[] args) {
       
        NAR nd = new NAR();
        test(nd);
        
        
    }

}
