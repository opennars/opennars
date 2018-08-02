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
                ProcessAnticipation.firePredictions(task, concept, nal, time, taskl);
            }
        }
        return true;
    }     
}
