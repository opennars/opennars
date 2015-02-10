package nars.analyze;

import nars.logic.NALTest;
import nars.core.NAR;
import nars.core.Parameters;
import nars.build.Default;

import java.util.Collection;

import static nars.analyze.NALStressMeasure.perfNAL;

/**
 * Runs NALTestPerf continuously, for profiling
 */
public class NALPerfLoop {
    
    public static void main(String[] args) {
       
        int repeats = 1;
        int warmups = 1;
        int maxConcepts = 1024;
        int extraCycles = 2048;
        int randomExtraCycles = 512;
        Parameters.THREADS = 1;
          
        NAR n = new NAR(new Default().setConceptBagSize(maxConcepts) );
        //NAR n = new NAR( new Neuromorphic(16).setConceptBagSize(maxConcepts) );
        //NAR n = new NAR(new Curve());
        
        //NAR n = new NAR(new Discretinuous());

        //new NARPrologMirror(n,0.75f, true).temporal(true, true);              
        
        Collection c = NALTest.params();
        while (true) {
            for (Object o : c) {
                String examplePath = (String)((Object[])o)[1];
                Parameters.DEBUG = false;

                
                perfNAL(n, examplePath,extraCycles+ (int)(Math.random()*randomExtraCycles),repeats,warmups,false);
            }
        }        
    }
}
