/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.lab.inference;

import java.io.File;
import java.io.FileNotFoundException;
import nars.core.Events.Answer;
import nars.core.NAR;
import nars.core.build.Default;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.inference.AbstractObserver;
import nars.io.Output.OUT;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.io.narsese.Narsese;
import nars.language.Term;

/**
 *
 * @author me
 */
public class TuneTuffy {
    
    public static class SolutionMonitor extends AbstractObserver {
        private final Term term;
        Sentence mostConfident = null;
        
        public SolutionMonitor(NAR n, String term) throws Narsese.InvalidInputException {
            super(n, true, OUT.class, Answer.class);
            
            Term t = new Narsese(n).parseTerm(term);
            this.term = t;
            
            n.addInput(t.toString() + "?");
        }

        @Override
        public void event(Class event, Object[] args) {
            if ((event == Answer.class) || (event == OUT.class)) {
                Task task = (Task)args[0];
                Term content = task.sentence.term;
                if (task.sentence.isJudgment()) {
                    if (content.equals(term)) {
                        onJudgment(task.sentence);
                    }
                }
            }
        }

        public void onJudgment(Sentence s) {
            if (mostConfident == null)
                mostConfident = s;
            else {
                float existingConf = mostConfident.truth.getConfidence();
                if (existingConf < s.truth.getConfidence())
                    mostConfident = s;
            }
        }

        @Override
        public String toString() {
            return term + "? " + mostConfident;
        }
        
    }
    
    public static void main(String[] args) throws FileNotFoundException, Narsese.InvalidInputException {
        Default b = new Default().
                setInternalExperience(null);
                
        
        NAR n = new NAR(b);
        n.addInput(new TextInput(new File("nal/use_cases/tuffy.smokes.nal")));
        
        //new TextOutput(n, System.out, 0.95f);                
        
        n.run(0);
        
        SolutionMonitor anna0 = new SolutionMonitor(n, "<Anna <-> [Smokes]>");
        SolutionMonitor bob0 = new SolutionMonitor(n, "<Bob --> [Smokes]>");
        SolutionMonitor edward0 = new SolutionMonitor(n, "<Edward --> [Smokes]>");
        SolutionMonitor frank0 = new SolutionMonitor(n, "<Frank --> [Smokes]>");
        
        SolutionMonitor anna = new SolutionMonitor(n, "<Anna <-> [Cancer]>");
        SolutionMonitor bob = new SolutionMonitor(n, "<Bob --> [Cancer]>");
        SolutionMonitor edward = new SolutionMonitor(n, "<Edward --> [Cancer]>");
        SolutionMonitor frank = new SolutionMonitor(n, "<Frank --> [Cancer]>");


        n.run(15000);

        //first number is the expected Tuffy probability result
        System.out.println("0.75? " + edward);
        System.out.println("0.65? " + anna);                
        System.out.println("0.50? " + bob);
        System.out.println("0.45? " + frank);
    }
}
