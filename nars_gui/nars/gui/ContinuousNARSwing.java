package nars.gui;

import nars.core.NAR;
import nars.core.Param;
import nars.core.build.Discretinuous;
import nars.inference.AbstractController;



public class ContinuousNARSwing {

    public static class PriorityMultiplier extends AbstractController {
        //private int numConcepts;

        public PriorityMultiplier(NAR n) {
            super(n, 1);
        }

        
        @Override
        public void getSensors() {
            //numConcepts = nar.memory.getConcepts().size();
        }

        @Override
        public void setParameters() {
            Param p = nar.param;
            //int c = (int)Math.sqrt(numConcepts);
            
            p.conceptForgetDurations.set(2);             
            p.taskLinkForgetDurations.set(4);
            p.termLinkForgetDurations.set(10);
            p.novelTaskForgetDurations.set(2);
        }
        
    }
    public static void main(String[] arg) {
        NAR cn = new Discretinuous(true).setConceptBagSize(8192).build();
        //new Remeber(cn);
        
        NARSwing w = new NARSwing(cn);
        
    }
}
