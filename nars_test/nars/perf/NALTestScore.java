/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.perf;

import java.util.Collection;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.DefaultNARBuilder;
import nars.test.core.NALTest;

/**
 *
 * @author me
 */
public class NALTestScore extends NALTest {

    static NAR nextNAR = null;
    
    @Override public NAR newNAR() {
        return nextNAR;
    }

    public NALTestScore(String scriptPath) {
        super(scriptPath);
    }
    
    public static void main(String[] arg) {
        int warmups = 1;
        int maxConcepts = 1000;
        int extraCycles = 50;
        int randomExtraCycles = 0;
        Parameters.THREADS = 1;
          
        requireSuccess = false;
        showFail = false;
        showSuccess = false;
        saveSimilar = false;
        showReport = false;
        
        //NAR n = new NeuromorphicNARBuilder().setConceptBagSize(maxConcepts).build();
        //NAR n = new CurveBagNARBuilder().setConceptBagSize(maxConcepts).build();
        
        //NAR n = new DiscretinuousBagNARBuilder().setConceptBagSize(maxConcepts).build();

        //new NARPrologMirror(n,0.75f, true).temporal(true, true);              
        
        final Collection c = NALTest.params();
        
        while (true) {
            NAR n = nextNAR = new DefaultNARBuilder().setConceptBagSize(maxConcepts).build();
            System.out.println(n);
            
            double score = 0;
            
            for (Object o : c) {
                String examplePath = (String)((Object[])o)[0];
                Parameters.DEBUG = true;
        
                Memory.resetStatic();
                n.reset();
                
                try {
                    score += new NALTestScore(examplePath).run();
                }
                catch (Exception e) { }
                
                
                //perfNAL(n, examplePath,extraCycles+ (int)(Math.random()*randomExtraCycles),repeats,warmups,true);
            }
            
            System.out.println(" score: "+ score);
        }        
        

    }
    
    
    
}
