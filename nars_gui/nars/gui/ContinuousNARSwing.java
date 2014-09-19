package nars.gui;

import nars.core.NAR;
import nars.core.Param;
import nars.core.build.DiscretinuousBagNARBuilder;
import nars.inference.AbstractController;



public class ContinuousNARSwing {

    public static class PriorityMultiplier extends AbstractController {
        private int numConcepts;

        public PriorityMultiplier(NAR n) {
            super(n, 1);
        }

        
        @Override
        public void getSensors() {
            numConcepts = nar.memory.getConcepts().size();
        }

        @Override
        public void setParameters() {
            Param p = nar.param();
            //int c = (int)Math.sqrt(numConcepts);
            int c = numConcepts;
            p.conceptCyclesToForget.set(10+c/2);             
            p.taskCyclesToForget.set(20);
            p.beliefCyclesToForget.set(50);
            p.newTaskCyclesToForget.set(10);
        }
        
    }
    public static void main(String[] arg) {
        NAR cn = new DiscretinuousBagNARBuilder(true).setConceptBagSize(8192).build();
        //new Remeber(cn);
        
        NARSwing w = new NARSwing(cn);
        
    }
}
