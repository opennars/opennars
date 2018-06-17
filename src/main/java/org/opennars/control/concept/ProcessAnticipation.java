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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opennars.control.concept;

import org.opennars.control.DerivationContext;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.entity.TaskLink;
import org.opennars.entity.TruthValue;
import org.opennars.inference.TemporalRules;
import org.opennars.interfaces.Timable;
import org.opennars.io.events.OutputHandler;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Implication;
import org.opennars.language.Statement;
import org.opennars.operator.Operator;
import org.opennars.operator.mental.Anticipate;
import org.opennars.plugin.mental.InternalExperience;

/**
 *
 * @author Patrick
 */
public class ProcessAnticipation {

    public static void anticipate(final DerivationContext nal, final Sentence mainSentence, final BudgetValue budget, final long mintime, final long maxtime, final float priority) {
        //derivation was successful and it was a judgment event
        final Stamp stamp = new Stamp(nal.time, nal.memory);
        stamp.setOccurrenceTime(Stamp.ETERNAL);
        float eternalized_induction_confidence = 0.33f;
        //long serial = stamp.evidentialBase[0];
        final Sentence s = new Sentence(
            mainSentence.term,
            mainSentence.punctuation,
            new TruthValue(0.0f, eternalized_induction_confidence, nal.narParameters),
            stamp);
        final Task t = new Task(s, new BudgetValue(0.99f,0.1f,0.1f, nal.narParameters), Task.EnumType.DERIVED); //Budget for one-time processing
        final Concept c = nal.memory.concept(((Statement) mainSentence.term).getPredicate()); //put into consequence concept
        if(c != null /*&& mintime > nal.memory.time()*/ && c.observable && mainSentence.getTerm() instanceof Statement && mainSentence.getTerm().getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
            if(c.negConfirmation == null || priority > c.negConfirmationPriority /*|| t.getPriority() > c.negConfirmation.getPriority() */) {
                c.negConfirmation = t;
                c.negConfirmationPriority = priority;
                c.negConfirm_abort_maxtime = maxtime;
                c.negConfirm_abort_mintime = mintime;
                if(c.negConfirmation.sentence.term instanceof Implication) {
                    final Implication imp = (Implication) c.negConfirmation.sentence.term;
                    final Concept ctarget = nal.memory.concept(imp.getPredicate());
                    if(ctarget != null && ctarget.getPriority()>=InternalExperience.MINIMUM_CONCEPT_PRIORITY_TO_CREATE_ANTICIPATION) {
                        Operator anticipate_op = ((Anticipate)c.memory.getOperator("^anticipate"));
                        if(anticipate_op != null && anticipate_op instanceof Anticipate) {
                            ((Anticipate)anticipate_op).anticipationFeedback(imp.getPredicate(), null, c.memory, nal.time);
                        }
                    }
                }
                nal.memory.emit(OutputHandler.ANTICIPATE.class,((Statement) c.negConfirmation.sentence.term).getPredicate()); //disappoint/confirm printed anyway
            }
        }
    }

    /**
     * Process outdated anticipations within the concept,
     * these which are outdated generate negative feedback
     * <p>
     * called only in GeneralInferenceControl on concept selection
     * 
     * @param concept The concept which potentially outdated anticipations should be processed
     */
    public static void maintainDisappointedAnticipations(final Concept concept, final Timable time) {
        //here we can check the expiration of the feedback:
        if(concept.negConfirmation == null || time.time() <= concept.negConfirm_abort_maxtime) {
            return;
        }
        //at first search beliefs for input tasks:
        boolean cancelled = false;
        for(final TaskLink tl : concept.taskLinks) { //search for input in tasklinks (beliefs alone can not take temporality into account as the eternals will win)
            final Task t = tl.targetTask;
            if(t!= null && t.sentence.isJudgment() && t.isInput() && !t.sentence.isEternal() && t.sentence.truth.getExpectation() > concept.memory.narParameters.DEFAULT_CONFIRMATION_EXPECTATION &&
                    CompoundTerm.replaceIntervals(t.sentence.term).equals(CompoundTerm.replaceIntervals(concept.getTerm()))) {
                if(t.sentence.getOccurenceTime() >= concept.negConfirm_abort_mintime && t.sentence.getOccurenceTime() <= concept.negConfirm_abort_maxtime) {
                    cancelled = true;
                    break;
                }
            }
        }
        if(cancelled) {
            concept.memory.emit(OutputHandler.CONFIRM.class,((Statement) concept.negConfirmation.sentence.term).getPredicate());
            concept.negConfirmation = null; //confirmed
            return;
        }        
        concept.memory.inputTask(time, concept.negConfirmation, false);
        concept.memory.emit(OutputHandler.DISAPPOINT.class,((Statement) concept.negConfirmation.sentence.term).getPredicate());
        concept.negConfirmation = null;
    }
    
    /**
     * Whether a processed judgement task satisfies the anticipation withon concept
     * <p>
     * called in processJudgment only
     * 
     * @param task The judgement task be checked
     * @param concept The concept that is processed
     * @param nal The derivation context
     */
    public static void confirmAnticipation(Task task, Concept concept, final DerivationContext nal) {
        final boolean satisfiesAnticipation = task.isInput() && !task.sentence.isEternal() &&
                                              concept.negConfirmation != null &&
                                              task.sentence.getOccurenceTime() > concept.negConfirm_abort_mintime;
        final boolean isExpectationAboveThreshold = task.sentence.truth.getExpectation() > nal.narParameters.DEFAULT_CONFIRMATION_EXPECTATION;
        if(satisfiesAnticipation && isExpectationAboveThreshold &&
          ((Statement) concept.negConfirmation.sentence.term).getPredicate().equals(task.sentence.getTerm())) {
            nal.memory.emit(OutputHandler.CONFIRM.class, ((Statement)concept.negConfirmation.sentence.term).getPredicate());
            concept.negConfirmation = null; // confirmed
        }
    }
}
