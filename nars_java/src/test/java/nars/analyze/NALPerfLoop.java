package nars.analyze;

import nars.build.Discretinuous;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.ExampleFileInput;
import nars.logic.NALTest;

import java.util.Collection;

import static nars.analyze.experimental.NALStressMeasure.perfNAL;

/**
 * Runs NALTestPerf continuously, for profiling
 * TODO keep up to date with the new test script layout
 */
public class NALPerfLoop {
    
    public static void main(String[] args) {
       
        int repeats = 1;
        int warmups = 1;
        int maxConcepts = 1024;
        int extraCycles = 4048;
        int randomExtraCycles = 512;
        Parameters.THREADS = 1;
          
        //NAR n = new NAR(new Default().setConceptBagSize(maxConcepts) );
        //NAR n = new NAR( new Neuromorphic(16).setConceptBagSize(maxConcepts) );
        //NAR n = new NAR(new Curve());
        
        NAR n = new NAR(new Discretinuous());

        //new NARPrologMirror(n,0.75f, true).temporal(true, true);              
        
        Collection c = NALTest.params();
        c.addAll((ExampleFileInput.getUnitTests("test1", "test2", "test3", "test4", "test5", "test6", "test7", "test8")).values());

        while (true) {
            for (Object o : c) {
                Object x = o;
                String examplePath = (x instanceof Object[]) ? (String)(((Object[])x)[1]) : (String)x;
                Parameters.DEBUG = false;

                
                perfNAL(n, examplePath,extraCycles+ (int)(Math.random()*randomExtraCycles),repeats,warmups,false);
            }
        }        
    }
}
