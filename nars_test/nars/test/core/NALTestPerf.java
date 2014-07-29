package nars.test.core;

import java.util.Collection;

/**
 * Not a unit-test, but runs all unit tests for profiling and other analysis.
 */
public class NALTestPerf {
    
    public static void main(String[] args) {
        int totalRepeats = 12;
        
        
        int repeats = 2;
        int warmups = 1;
        int extraCycles = 10000;
        
        Collection c = NALTest.params();
        for (int i = 0; i < totalRepeats; i++) {
            for (Object o : c) {
                String examplePath = (String)((Object[])o)[0];
                System.out.println(examplePath);
                NALTest.perfNAL(examplePath,extraCycles,repeats,warmups,false);
            }
        }
    }
}
