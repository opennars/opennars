package nars.test.multistep;

import nars.core.NAR;
import nars.core.build.Default;
import nars.gui.NARSwing;
import nars.gui.NWindow;
import nars.gui.output.graph.ImplicationGraphCanvas;
import nars.gui.output.graph.ProcessingGraphPanel;



public class RealtimeSequenceExperiment {

    public RealtimeSequenceExperiment() throws InterruptedException {
        int seqLength = 8;
        int framePeriodMS = 100;
        int seqPeriodMS = 800;
        int durationMS = 50;
        int cycPerFrame = 5;
        
        NAR n = new Default().realTime().build();
                
        (n.param).duration.set(durationMS);
        (n.param).noiseLevel.set(0);
        (n.param).decisionThreshold.set(0.9);
        
        new NARSwing(n);
        new NWindow("Implication Graph", 
                            new ProcessingGraphPanel(n, 
                                    new ImplicationGraphCanvas(
                                            n.memory.executive.graph))).show(500, 500);
        
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
            n.addInput("(^pick,x" + i + ")!");
            //n.addInput("<(^pick,x" + i + ") =/> e" + i + ">. :|:");
            //n.addInput("e" + last + ". :|:");
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
