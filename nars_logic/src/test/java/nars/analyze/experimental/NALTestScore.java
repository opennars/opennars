/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.analyze.experimental;

import nars.Events;
import nars.Memory;
import nars.NAR;
import nars.Global;
import nars.event.Reaction;
import nars.nal.NALTest;
import org.junit.Ignore;

import java.util.Collection;

/**
 *
 * @author me
 */
@Ignore
@Deprecated public class NALTestScore extends NALTest {

    
    
    
    static public NAR nextNAR = null;
    
    @Override public NAR newNAR() {
        return nextNAR;
    }

    public NALTestScore(String scriptPath) {
        super(null, scriptPath);
    }
    
    public static double score(NAR nn, int maxCycles/* randomseed, etc. */) {
        Global.THREADS = 1;
        
        nextNAR = nn;
          
        requireSuccess = false;
        showFail = false;
        showSuccess = false;
        saveSimilar = false;
        showReport = false;


        
        final Collection c = NALTest.params();
        
        
        NAR n = nextNAR;

        double score = 0;

        for (Object o : c) {
            String examplePath = (String)((Object[])o)[1];
            
            NALTest.saveSimilar = false;
            
            Memory.resetStatic(1);
            n.reset();

            if (maxCycles!=-1) {
                
//                //TODO extract as TimeLimit plugin
//                n.on(new Reaction() {
//                    @Override public void event(Class event, Object[] arguments) {
//                        if (n.time() > maxCycles) {
//                            n.stop();
//                        }
//                    }
//                }, Events.CycleEnd.class);
            }
                        
            double s = new NALTestScore(examplePath).score();
            if (s == Double.POSITIVE_INFINITY)
                s = maxCycles + 1; //didnt complete so use the max time
            score += s;

            //System.out.println(examplePath + " " + score + " " + n.time());


        }       
        
        return score;        

    }


    
}
