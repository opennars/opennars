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

import java.util.List;
import org.opennars.control.DerivationContext;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.entity.TaskLink;
import static org.opennars.inference.LocalRules.trySolution;
import org.opennars.io.Symbols;
import org.opennars.io.events.Events;
import org.opennars.language.Term;
import org.opennars.language.Variables;

/**
 *
 * @author Patrick
 */
public class ProcessQuestion {
    /**
     * To answer a question by existing beliefs
     *
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected static void processQuestion(final Concept concept, final DerivationContext nal, final Task task) {
        Task quesTask = task;
        boolean newQuestion = true;
        List<Task> questions = concept.questions;
        if(task.sentence.punctuation == Symbols.QUEST_MARK) {
            questions = concept.quests;
        }
        for (final Task t : questions) {
            if (t.sentence.term.equals(quesTask.sentence.term)) {
                quesTask = t;
                newQuestion = false;
                break;
            }
        }
        if (newQuestion) {
            if (questions.size() + 1 > concept.memory.narParameters.CONCEPT_QUESTIONS_MAX) {
                final Task removed = questions.remove(0);    // FIFO
                concept.memory.event.emit(Events.ConceptQuestionRemove.class, concept, removed);
            }

            questions.add(task);
            concept.memory.event.emit(Events.ConceptQuestionAdd.class, concept, task);
        }
        final Sentence ques = quesTask.sentence;
        final Task newAnswerT = (ques.isQuestion())
                ? concept.selectCandidate(quesTask, concept.beliefs, nal.time)
                : concept.selectCandidate(quesTask, concept.desires, nal.time);

        if (newAnswerT != null) {
            trySolution(newAnswerT.sentence, task, nal, true);
        }
        else if(task.isInput() && !quesTask.getTerm().hasVarQuery() && quesTask.getBestSolution() != null) { // show previously found solution anyway in case of input
            concept.memory.emit(Events.Answer.class, quesTask, quesTask.getBestSolution());
        }
    }
    
    /**
     * Recognize an existing belief task as solution to the what question task, which contains a query variable
     * <p>
     * called only in GeneralInferenceControl.insertTaskLink on concept selection
     * 
     * @param concept The concept which potentially outdated anticipations should be processed
     * @paramt t The belief task
     * @param nal The derivation context
     */
    public static void ProcessWhatQuestion(final Concept concept, final Task ques, final DerivationContext nal) {
        if(!(ques.sentence.isJudgment()) && ques.getTerm().hasVarQuery()) { //ok query var, search
            boolean newAnswer = false;
            for(final TaskLink t : concept.taskLinks) {
                final Term[] u = new Term[] { ques.getTerm(), t.getTerm() };
                if(!t.getTerm().hasVarQuery() && Variables.unify(Symbols.VAR_QUERY, u)) {
                    final Concept c = nal.memory.concept(t.getTerm());
                    if(c == null) {
                        continue; //target concept is already gone
                    }
                    synchronized(c) { //changing target concept, lock it
                        final List<Task> answers = ques.sentence.isQuest() ? c.desires : c.beliefs;
                        if(c != null && answers.size() > 0) {
                            final Task taskAnswer = answers.get(0);
                            if(taskAnswer!=null) {
                                newAnswer |= trySolution(taskAnswer.sentence, ques, nal, false); //order important here
                            }
                        }
                    }
                }
            }
            if(newAnswer && ques.isInput()) {
                nal.memory.emit(Events.Answer.class, ques, ques.getBestSolution());
            }
        }
    }
    
    /**
     * Recognize an added belief task as solution to what questions, those that contain query variable
     * <p>
     * called only in GeneralInferenceControl.insertTaskLink on concept selection
     * 
     * @param concept The concept which potentially outdated anticipations should be processed
     * @paramt t The belief task
     * @param nal The derivation context
     */
    public static void ProcessWhatQuestionAnswer(final Concept concept, final Task t, final DerivationContext nal) {
        if(!t.sentence.term.hasVarQuery() && t.sentence.isJudgment() || t.sentence.isGoal()) { //ok query var, search
            for(final TaskLink quess: concept.taskLinks) {
                final Task ques = quess.getTarget();
                if(((ques.sentence.isQuestion() && t.sentence.isJudgment()) ||
                    (ques.sentence.isGoal()     && t.sentence.isJudgment()) ||
                    (ques.sentence.isQuest()    && t.sentence.isGoal())) && ques.getTerm().hasVarQuery()) {
                    boolean newAnswer = false;
                    final Term[] u = new Term[] { ques.getTerm(), t.getTerm() };
                    if(ques.sentence.term.hasVarQuery() && !t.getTerm().hasVarQuery() && Variables.unify(Symbols.VAR_QUERY, u)) {
                        final Concept c = nal.memory.concept(t.getTerm());
                        if(c == null) {
                            continue; //target doesn't exist anymore
                        }
                        synchronized(c) { //changing target concept, lock it
                            final List<Task> answers = ques.sentence.isQuest() ? c.desires : c.beliefs;
                            if(c != null && answers.size() > 0) {
                                final Task taskAnswer = answers.get(0);
                                if(taskAnswer!=null) {
                                    newAnswer |= trySolution(taskAnswer.sentence, ques, nal, false); //order important here
                                }
                            }
                        }
                    }
                    if(newAnswer && ques.isInput()) {
                       nal.memory.emit(Events.Answer.class, ques, ques.getBestSolution());
                    }
                }
            }
        }
    }
}
