package nars.test.core;

import java.util.Collection;
import static nars.test.core.NALTestPerf.perfNAL;

/**
 * Runs NALTestPerf continuously, for profiling
 */
public class NALTestPerfContinuous {
    
    public static void main(String[] args) {
       
        int repeats = 1;
        int warmups = 0;
        int extraCycles = 128;
        
        Collection c = NALTest.params();
        while (true) {
            for (Object o : c) {
                String examplePath = (String)((Object[])o)[0];
                perfNAL(examplePath,extraCycles,repeats,warmups,false);
            }
        }        
    }
}
