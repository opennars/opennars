/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package org.opennars.util.test;

import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.io.Narsese;
import org.opennars.io.events.EventHandler;
import org.opennars.io.events.Events.Answer;
import org.opennars.io.events.OutputHandler.OUT;
import org.opennars.language.Term;
import org.opennars.main.Nar;

/**
 *
 * @author me
 */
public class TuneTuffy {
    
    public static class SolutionMonitor extends EventHandler {
        private final Term term;
        Sentence mostConfident = null;
        
        public SolutionMonitor(final Nar n, final String term) throws Narsese.InvalidInputException {
            super(n, true, OUT.class, Answer.class);
            
            final Term t = new Narsese(n).parseTerm(term);
            this.term = t;
            
            n.addInput(t.toString() + "?");
        }

        @Override
        public void event(final Class event, final Object[] args) {
            if ((event == Answer.class) || (event == OUT.class)) {
                final Task task = (Task)args[0];
                final Term content = task.sentence.term;
                if (task.sentence.isJudgment()) {
                    if (content.equals(term)) {
                        onJudgment(task.sentence);
                    }
                }
            }
        }

        public void onJudgment(final Sentence s) {
            if (mostConfident == null)
                mostConfident = s;
            else {
                final float existingConf = mostConfident.truth.getConfidence();
                if (existingConf < s.truth.getConfidence())
                    mostConfident = s;
            }
        }

        @Override
        public String toString() {
            return term + "? " + mostConfident;
        }
        
    }
    
    public static void main(final String[] args) throws Narsese.InvalidInputException {

        
        final Nar n = new Nar();
        n.addInputFile("nal/use_cases/tuffy.smokes.nal");
        
        //new TextOutput(n, System.out, 0.95f);                
        
        
        final SolutionMonitor anna0 = new SolutionMonitor(n, "<Anna <-> [Smokes]>");
        final SolutionMonitor bob0 = new SolutionMonitor(n, "<Bob --> [Smokes]>");
        final SolutionMonitor edward0 = new SolutionMonitor(n, "<Edward --> [Smokes]>");
        final SolutionMonitor frank0 = new SolutionMonitor(n, "<Frank --> [Smokes]>");
        
        final SolutionMonitor anna = new SolutionMonitor(n, "<Anna <-> [Cancer]>");
        final SolutionMonitor bob = new SolutionMonitor(n, "<Bob --> [Cancer]>");
        final SolutionMonitor edward = new SolutionMonitor(n, "<Edward --> [Cancer]>");
        final SolutionMonitor frank = new SolutionMonitor(n, "<Frank --> [Cancer]>");


        n.run();

        //first number is the expected Tuffy probability result
        System.out.println("0.75? " + edward);
        System.out.println("0.65? " + anna);                
        System.out.println("0.50? " + bob);
        System.out.println("0.45? " + frank);
    }
}
