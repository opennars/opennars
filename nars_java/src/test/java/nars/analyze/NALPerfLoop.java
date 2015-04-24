package nars.analyze;

import nars.Global;
import nars.NAR;
import nars.io.ExampleFileInput;
import nars.nal.NALTest;
import nars.prototype.Default;

import java.util.Collection;

import static nars.analyze.experimental.NALStressMeasure.perfNAL;

/**
 * Runs NALTestPerf continuously, for profiling
 * TODO keep up to date with the new test script layout
 */
public class NALPerfLoop {
    
    public static void main(String[] args) {
       
        int repeats = 1;
        int warmups = 0;
        int maxConcepts = 2048;
        int extraCycles = 10048;
        int randomExtraCycles = 512;
        Global.THREADS = 1;
        Global.EXIT_ON_EXCEPTION = true;
        Global.DEBUG = false;
          
        NAR n = new NAR(new Default().setConceptBagSize(maxConcepts) );
        //NAR n = new NAR( new Neuromorphic(16).setConceptBagSize(maxConcepts) );
        //NAR n = new NAR(new Curve());
        
        //NAR n = new NAR(new Discretinuous().setConceptBagSize(maxConcepts));

        //new NARPrologMirror(n,0.75f, true).temporal(true, true);              
        
        Collection c = NALTest.params();
        c.addAll((ExampleFileInput.getUnitTests("test1", "test2", "test3", "test4", "test5", "test6", "test7", "test8")).values());

        while (true) {
            for (Object o : c) {
                Object x = o;
                String examplePath = (x instanceof Object[]) ? (String)(((Object[])x)[1]) : (String)x;


                
                perfNAL(n, examplePath,extraCycles+ (int)(Math.random()*randomExtraCycles),repeats,warmups,false);
            }
        }        
    }
}
