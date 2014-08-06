package nars.test.core;

import java.util.Collection;
import nars.core.NAR;
import static nars.test.core.NALTest.newNAR;


public class NALTestPerf  {
    
    public static void perfNAL(final String path, final int extraCycles, int repeats, int warmups) {
        perfNAL(path, extraCycles, repeats, warmups, true);
    }
    
    public static void perfNAL(final String path, final int extraCycles, int repeats, int warmups, boolean gc) {
        
        final String example = NALTest.getExample(path);
        
        new Performance(path, repeats, warmups, gc) {
            private NAR n;
            long totalCycles;
            
            @Override
            public void init() {
                NAR.resetStatics();
                
                n = newNAR();
                totalCycles = 0;
                
            }

            @Override
            public void run(boolean warmup) {
                n.reset();
                n.addInput(example);
                n.finish(extraCycles);
                
                if (!warmup)
                    totalCycles += n.getTime();
            }

            @Override
            public Performance print() {                
                super.print();
                System.out.print(", " + df.format(getCycleTimeMS() / ((double)totalCycles) * 1000.0) + " ns/cycle, " + (totalCycles/repeats) + " cycles/run");
                return this;
                
            }

        }.print();
        
           
    }
    
    public static void main(String[] args) {
        int repeats = 10;
        int warmups = 1;
        int extraCycles = 1;
        
        Collection c = NALTest.params();
        for (Object o : c) {
            String examplePath = (String)((Object[])o)[0];
            perfNAL(examplePath,extraCycles,repeats,warmups,true);
        }
    }
}
