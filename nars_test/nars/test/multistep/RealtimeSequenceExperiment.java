package nars.test.multistep;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.gui.NARSwing;

/**
 *
 * @author me
 */


public class RealtimeSequenceExperiment {

    public RealtimeSequenceExperiment() throws InterruptedException {
        int seqLength = 12;
        int framePeriodMS = 100;
        int seqPeriodMS = 400;
        int durationMS = 100;
        int cycPerFrame = 10;
        
        NAR n = new DefaultNARBuilder().build();
        
        //n.param().setRealtime(true);
        n.param().duration.set(durationMS);
        n.param().noiseLevel.set(0);
        n.param().decisionThreshold.set(0.9);
        
        new NARSwing(n);
        
        n.start(framePeriodMS, cycPerFrame);
    
        int last = 0;
        int i = 1;
//        while (true) {
//            n.addInput("<(&/,e" + last + ",(^pick,x" + last + ")) =/> e" + i + ">. :|:");
//            Thread.sleep(seqPeriodMS);
//            i++;
//            last++;
//            i%=seqLength;
//            last%=seqLength;
//            if (last == 0)
//                n.addInput("e" + last + "!");
//            else {
//                //n.addInput("e" + last + ". :|:");
//            }
//        }
        
        while (true) {
            n.addInput("e" + last + ". :|:");
            Thread.sleep(seqPeriodMS);
            i++;
            last++;
            i%=seqLength;
            last%=seqLength;
        }

    }
    
    public static void main(String[] args) throws InterruptedException {
        new RealtimeSequenceExperiment();
    }

    
}
