/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rl;

import automenta.vivisect.swing.NWindow;
import java.util.List;
import nars.core.Events.CycleEnd;
import nars.core.NAR;
import nars.core.build.Neuromorphic;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.gui.output.ConceptsPanel;
import nars.inference.AbstractObserver;
import nars.io.Answered;
import nars.io.TextOutput;
import nars.io.narsese.Narsese;
import nars.language.Tense;

/**
 *
 * @author me
 */
public class Predict1 {

    public static class TimeModel extends AbstractObserver {
        
        public final List<Concept> concepts;
        
        public TimeModel(NAR n, List<Concept> concepts) {
            super(n, true, CycleEnd.class);
            this.concepts = concepts;
        }

        @Override
        public void event(Class event, Object[] args) {
            if (event == CycleEnd.class) {
                
            }
        }
    
        
    }
    
    public static void main(String[] args) throws Narsese.InvalidInputException, InterruptedException {
        int duration = 24;
        NAR n = new NAR(new Neuromorphic(8).setInternalExperience(null));
        n.param.duration.set(duration);
        n.param.conceptBeliefsMax.set(16);
        
        String xPrevFuncEq0 = "<x_tMin1 --> y0>";
        String xPrevFuncEq1 = "<x_tMin1 --> y1>";
        String xFuncEq0 = "<x_t0 --> y0>";
        String xFuncEq1 = "<x_t0 --> y1>";
        //String xNextFunc = "<x_tPlus1 --> y>";
        n.believe(xPrevFuncEq0, Tense.Present, 0.50f, 0.90f).run(1);
        n.believe(xPrevFuncEq1, Tense.Present, 0.50f, 0.90f).run(1);
        n.believe(xFuncEq0, Tense.Present, 0.50f, 0.90f).run(1);
        n.believe(xFuncEq1, Tense.Present, 0.50f, 0.90f).run(1);
        //n.believe("<(&/," + xPrevFunc + ",+1) =/> " + xFunc + ">", Tense.Eternal, 1.00f, 0.95f).run(1);
        //n.believe("<(&/," + xPrevFunc + ",+1," + xFunc + ",+1) =/> " + xNextFunc + ">", Tense.Eternal, 1.00f, 0.95f).run(1);
        //n.believe("<(&/," + xFunc + ",+1) =/> " + xNextNextFunc + ">", Tense.Eternal, 1.00f, 0.90f).run(1);
        
        Answered a = new Answered() {
            @Override
            public void onSolution(Sentence belief) {
                System.out.println(belief);
            }

            @Override public void onChildSolution(Task child, Sentence belief) {         
                System.out.println(belief);
            }            
            
        };
        
        n.ask("<(&/,<x_t0 --> $x>,+1) =/> <x_t0 --> y0>>");
        n.ask("<(&/,<x_t0 --> $y>,+1) =/> <x_t0 --> y1>>");
        n.ask("<x_t0 --> y0>");
        n.ask("<x_t0 --> y1>");
        
        new TextOutput(n, System.out) {
              
            /** only allow future predictions */
            protected boolean allowTask(Task t) {  
              if (t.sentence.isEternal())   return false;
              if ((t.sentence.getOccurenceTime() > n.time())) {
                  System.out.print(n.time() + ".." + t.sentence.getOccurenceTime() + ": ");
                  return true;
              }
              return false;
            }
        };
        
        n.run(10);
        
        Concept x0Prev = n.concept(xPrevFuncEq0);
        Concept x1Prev = n.concept(xPrevFuncEq1);
        Concept x0Now = n.concept(xFuncEq0);
        Concept x1Now = n.concept(xFuncEq1);
        
        NARSwing.themeInvert();
        new NWindow("x", new ConceptsPanel(n, x0Prev, x1Prev, x0Now, x1Now)).show(900, 600, true);
        new NARSwing(n);
        
        float freq = 0.3f;
        int thinkCycles = duration/2;
        float y = 0.5f;
        int t = 0;
        while (true) {
            
            n.believe(xPrevFuncEq0, Tense.Past, 1.0f, y);
            n.believe(xPrevFuncEq0, Tense.Past, 0.0f, 1f - y);
            n.believe(xPrevFuncEq1, Tense.Past, 1.0f, 1f - y);
            n.believe(xPrevFuncEq1, Tense.Past, 0.0f, y);
            
            //n.run(thinkCycles/2);
            
            //y = (float)Math.sin(freq * t) * 0.5f + 0.5f;
            y = ((float)Math.sin(freq * t) > 0 ? 1f : -1f) * 0.5f + 0.5f;

            
            n.believe(xFuncEq0, Tense.Present, 1.0f, y);
            n.believe(xFuncEq0, Tense.Present, 0.0f, 1f - y);
            n.believe(xFuncEq1, Tense.Present, 1.0f, 1f - y);
            n.believe(xFuncEq1, Tense.Present, 0.0f, y);
            
            
            n.run(thinkCycles);
            
            Thread.sleep(100);
            
            //n.memory.addSimulationTime(1);
            t++;
            
        }
        
    }
}
