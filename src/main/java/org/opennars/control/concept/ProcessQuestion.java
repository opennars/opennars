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

import java.util.List;

import com.google.common.base.Optional;
import org.opennars.control.DerivationContext;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.entity.TaskLink;

import static com.google.common.collect.Iterables.tryFind;
import static org.opennars.inference.LocalRules.trySolution;
import org.opennars.io.Symbols;
import org.opennars.io.events.Events;
import org.opennars.language.Term;
import org.opennars.language.Variables;

/**
 *
 * @author Patrick Hammer
 */
public class ProcessQuestion {
    /**
     * To answer a question by existing beliefs
     *
     * @param task The task to be processed
     */
    protected static void processQuestion(final Concept concept, final DerivationContext nal, final Task task) {
        Task quesTask = task;
        List<Task> questions = concept.questions;
        if(task.sentence.punctuation == Symbols.QUEST_MARK) {
            questions = concept.quests;
        }
        if(task.sentence.isEternal()) {
            final Optional<Task> eternalQuestionTask = tryFind(questions, iQuestionTask -> iQuestionTask.sentence.isEternal());

            // we can override the question task with the eternal question task if any was found
            if(eternalQuestionTask.isPresent()) {
                quesTask = eternalQuestionTask.get();
            }
        }
        if (questions.size() + 1 > concept.memory.narParameters.CONCEPT_QUESTIONS_MAX) {
            final Task removed = questions.remove(0);    // FIFO
            concept.memory.event.emit(Events.ConceptQuestionRemove.class, concept, removed);
        }

        questions.add(quesTask);
        concept.memory.event.emit(Events.ConceptQuestionAdd.class, concept, task);
            
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
     *
     * @param concept The concept which potentially outdated anticipations should be processed
     * @param ques The belief task
     * @param nal The derivation context
     */
    // called only in GeneralInferenceControl.insertTaskLink on concept selection
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
     *
     * @param concept The concept which potentially outdated anticipations should be processed
     * @param t The belief task
     * @param nal The derivation context
     */
    // called only in GeneralInferenceControl.insertTaskLink on concept selection
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
