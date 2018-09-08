/*
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

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
    
    public static void main(final String[] args) throws Narsese.InvalidInputException, IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {

        
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
