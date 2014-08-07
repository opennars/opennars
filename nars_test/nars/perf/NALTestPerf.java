package nars.perf;

import nars.util.ContinuousBagNARBuilder;
import java.util.Collection;
import nars.core.DefaultNARBuilder;
import nars.core.NAR;
import nars.core.Param;
import nars.core.Parameters;
import nars.entity.Concept;
import nars.entity.Task;
import nars.storage.AbstractBag;
import nars.test.core.NALTest;
import static nars.test.core.NALTest.newNAR;
import nars.util.ContinuousBag;


/** tests performance of NAL, but can also uncover bugs when NAL runs with a heavy and long load */
public class NALTestPerf  {
    
    public static void perfNAL(final String path, final int extraCycles, int repeats, int warmups) {
        perfNAL(newNAR(), path, extraCycles, repeats, warmups, true);
    }
    
    public static double perfNAL(final NAR n, final String path, final int extraCycles, int repeats, int warmups, boolean gc) {
        
        final String example = NALTest.getExample(path);
        
        Performance p = new Performance(path, repeats, warmups, gc) {
            long totalCycles;
            
            @Override
            public void init() {
                
                totalCycles = 0;
            }

            @Override
            public void run(boolean warmup) {
                try {
                    n.reset();
                    n.addInput(example);
                    n.finish(extraCycles);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                
                totalCycles += n.getTime();
            }

            @Override
            public Performance print() {                
                super.print();
                System.out.print(", " + df.format(getCycleTimeMS() / ((double)totalCycles) * 1000.0) + " ns/cycle, " + (((float)totalCycles)/(warmups+repeats)) + " cycles/run");
                return this;
                
            }

        };
        p.print();
        
        return p.getCycleTimeMS();
                   
    }
    
    public static void test(NAR n) {
        int repeats = 4;
        int warmups = 1;
        int extraCycles = 1000;

        Collection c = NALTest.params();
        double totalTime = 0;
        for (Object o : c) {
            String examplePath = (String)((Object[])o)[0];
            totalTime += perfNAL(n,examplePath,extraCycles,repeats,warmups,true);
        }
        System.out.println("\n\nTotal mean runtime (ms): " + totalTime);        
    }
    
    public static void main(String[] args) {
        

        NAR nd = new DefaultNARBuilder().build();
        test(nd);
        
        NAR nc = new ContinuousBagNARBuilder().build();
        test(nc);
        
    }

}
