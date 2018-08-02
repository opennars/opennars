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
package org.opennars.control.concept;

import java.util.ArrayList;
import java.util.List;
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
                    processJudgment(concept, nal, task);
                    break;
                case Symbols.GOAL_MARK:
                    processGoal(concept, nal, task);
                    break;
                case Symbols.QUESTION_MARK:
                case Symbols.QUEST_MARK:
                    processQuestion(concept, nal, task);
                    break;
                default:
                    return false;
            }
            List<TermLink> relink = new ArrayList<TermLink>();
            if (task.aboveThreshold()) {    // still need to be processed
                TaskLink taskl = concept.linkToTask(task,nal);                       
                ProcessAnticipation.firePredictions(task, concept, nal, time, taskl);
            }
        }
        return true;
    }

    /**
     * To answer a question by existing beliefs
     *
     * @param concept The concept of the goal
     * @param nal The derivation context
     * @param task The goal task to be processed
     */
    private static void processQuestion(Concept concept, DerivationContext nal, Task task) {
        nal.narParameters.processQuestion.processTask(concept, nal, task);
    }

    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it, potentially executing in case of an operation goal
     *
     * @param concept The concept of the goal
     * @param nal The derivation context
     * @param task The goal task to be processed
     */
    private static void processGoal(Concept concept, DerivationContext nal, Task task) {
        nal.narParameters.processGoal.processTask(concept, nal, task);
    }

    /**
     * To accept a new judgment as belief, and check for revisions and solutions.
     * Revisions will be processed as judgment tasks by themselves.
     * Due to their higher confidence, summarizing more evidence,
     * the will become the top entries in the belief table.
     * Additionally, judgements can themselves be the solution to existing questions
     * and goals, which is also processed here.
     * <p>
     * called only by ConceptProcessing.processTask
     *
     * @param task The judgment task to be accepted
     * @param concept The concept of the judment task
     * @param nal The derivation context
     */
    private static void processJudgment(Concept concept, DerivationContext nal, Task task) {
        nal.narParameters.processJudgment.processTask(concept, nal, task);
    }
}
