/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.perf;

import java.util.Collection;
import nars.core.EventEmitter;
import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.test.core.NALTest;

/**
 *
 * @author me
 */
public class NALTestScore extends NALTest {

    
    
    
    static public NAR nextNAR = null;
    
    @Override public NAR newNAR() {
        return nextNAR;
    }

    public NALTestScore(String scriptPath) {
        super(scriptPath);
    }
    
    public static double score(NAR nn, int maxCycles/* randomseed, etc. */) {
        Parameters.THREADS = 1;
        
        nextNAR = nn;
          
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
        
        
        NAR n = nextNAR;

        double score = 0;

        for (Object o : c) {
            String examplePath = (String)((Object[])o)[0];
            
            NALTest.saveSimilar = false;
            
            Memory.resetStatic();
            n.reset();

            if (maxCycles!=-1) {
                
                //TODO extract as TimeLimit plugin
                n.on(Events.CycleEnd.class, new EventEmitter.Observer() {
                    @Override public void event(Class event, Object[] arguments) {
                        if (n.time() > maxCycles) {                            
                            n.stop();
                        }
                    }            
                });                
            }
                        
            double s = new NALTestScore(examplePath).run();
            if (s == Double.POSITIVE_INFINITY)
                s = maxCycles + 1; //didnt complete so use the max time
            score += s;

            //System.out.println(examplePath + " " + score + " " + n.time());


        }       
        
        return score;        

    }
    
    
    
}
