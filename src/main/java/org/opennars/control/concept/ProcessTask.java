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
package org.opennars.control.concept;


import org.opennars.control.DerivationContext;
import org.opennars.entity.*;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;

/**
 * Encapsulates the dispatching task processing
 *
 * @author Patrick Hammer
 *
 */
public class ProcessTask {
    /**
     * Directly process a new task within a concept. 
     * Here task can either be a judgement, goal, question or quest.
     * The function is called exactly once on each task. Using
     * local information and finishing in a constant time. Also providing feedback 
     * in the budget value of the task:
     * de-priorize already fullfilled questions and goals
     * increase quality of beliefs if they turned out to be useful.
     * After the re-priorization is done, a tasklink is finally constructed.
     * For input events the concept is set observable too. 
     *
     * @param task The task to be processed
     * @return whether it was processed
     */
    // called in Memory.localInference only, for both derived and input tasks
    public static boolean processTask(final Concept concept, final DerivationContext nal, final Task task, Timable time) {
        synchronized(concept) {
            concept.observable |= task.isInput();
            final char type = task.sentence.punctuation;
            switch (type) {
                case Symbols.JUDGMENT_MARK:
                    ProcessJudgment.processJudgment(concept, nal, task);
                    break;
                case Symbols.GOAL_MARK:
                    ProcessGoal.processGoal(concept, nal, task);
                    break;
                case Symbols.QUESTION_MARK:
                case Symbols.QUEST_MARK:
                    ProcessQuestion.processQuestion(concept, nal, task);
                    break;
                default:
                    return false;
            }
            
            if (task.aboveThreshold()) {    // still need to be processed
                TaskLink taskl = concept.linkToTask(task,nal);               
                if(task.sentence.isJudgment() && ProcessJudgment.isExecutableHypothesis(task,nal)) { //after linkToTask
                    ProcessJudgment.addToTargetConceptsPreconditions(task, nal, null); //because now the components are there
                }
                ProcessAnticipation.firePredictions(task, concept, nal, time, taskl);
            }
        }
        return true;
    }     
}
