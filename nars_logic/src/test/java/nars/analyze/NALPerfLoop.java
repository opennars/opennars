package nars.analyze;

import nars.Global;
import nars.NAR;
import nars.io.in.LibraryInput;
import nars.nal.NALTest;
import nars.nar.experimental.Equalized;

import java.util.Collections;
import java.util.List;

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
        int extraCycles = 10048;
        int randomExtraCycles = 512;
        Global.THREADS = 1;
        Global.EXIT_ON_EXCEPTION = true;
        Global.DEBUG = false;

          
        NAR n = new NAR(new Equalized(maxConcepts,2,3).setInternalExperience(null) );
        //NAR n = new NAR(new Default().setActiveConcepts(maxConcepts).setInternalExperience(null) );
        //NAR n = new NAR(new NewDefault().setActiveConcepts(maxConcepts).setInternalExperience(null) );

        //NAR n = new NAR( new Neuromorphic(16).setConceptBagSize(maxConcepts) );
        //NAR n = new NAR(new Curve());
        
        //NAR n = new NAR(new Discretinuous().setConceptBagSize(maxConcepts));

        //new NARPrologMirror(n,0.75f, true).temporal(true, true);              

        List c = NALTest.params();
        c.addAll(LibraryInput.getAllExamples().values());

        Collections.shuffle(c);

        while (true) {
            for (Object o : c) {
                Object x = o;
                String examplePath = (x instanceof Object[]) ? (String)(((Object[])x)[1]) : (String)x;

                perfNAL(n, examplePath,extraCycles+ (int)(Math.random()*randomExtraCycles),repeats,warmups,false);
            }
        }        
    }
}
