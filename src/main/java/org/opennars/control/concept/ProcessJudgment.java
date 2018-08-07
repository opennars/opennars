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

import com.google.common.base.Optional;
import org.opennars.control.DerivationContext;
import org.opennars.control.TemporalInferenceControl;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;

import static com.google.common.collect.Iterables.tryFind;
import static org.opennars.inference.LocalRules.revisible;
import static org.opennars.inference.LocalRules.revision;
import static org.opennars.inference.LocalRules.trySolution;

import org.opennars.inference.TemporalRules;
import org.opennars.io.events.Events;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Conjunction;
import org.opennars.language.Implication;
import org.opennars.language.Interval;
import org.opennars.language.Term;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.operator.mental.Anticipate;
import org.opennars.operator.mental.Believe;
import org.opennars.operator.mental.Evaluate;
import org.opennars.operator.mental.Want;
import org.opennars.operator.mental.Wonder;

public class ProcessJudgment {
    /**
     * To accept a new judgment as belief, and check for revisions and solutions.
     * Revisions will be processed as judgment tasks by themselves.
     * Due to their higher confidence, summarizing more evidence,
     * the will become the top entries in the belief table.
     * Additionally, judgements can themselves be the solution to existing questions
     * and goals, which is also processed here.
     * 
     * @param task The judgment task to be accepted
     * @param concept The concept of the judment task
     * @param nal The derivation context
     */
    public static void processJudgment(final Concept concept, final DerivationContext nal, final Task task) {
        handleOperationFeedback(task, nal);
        final Sentence judg = task.sentence;
        ProcessAnticipation.confirmAnticipation(task, concept, nal);
        final Task oldBeliefT = concept.selectCandidate(task, concept.beliefs, nal.time);   // only revise with the strongest -- how about projection?
        Sentence oldBelief = null;
        if (oldBeliefT != null) {
            oldBelief = oldBeliefT.sentence;
            final Stamp newStamp = judg.stamp;
            final Stamp oldStamp = oldBelief.stamp;       //when table is full, the latter check is especially important too
            if (newStamp.equals(oldStamp,false,false,true)) {
                concept.memory.removeTask(task, "Duplicated");
                return;
            } else if (revisible(judg, oldBelief, nal.narParameters)) {
                nal.setTheNewStamp(newStamp, oldStamp, nal.time.time());
                final Sentence projectedBelief = oldBelief.projection(nal.time.time(), newStamp.getOccurrenceTime(), concept.memory);
                if (projectedBelief!=null) {
                    nal.setCurrentBelief(projectedBelief);
                    revision(judg, projectedBelief, concept, false, nal);
                }
            }
        }
        if (!task.aboveThreshold()) {
            return;
        }
        final int nnq = concept.questions.size();
        for (int i = 0; i < nnq; i++) {
            trySolution(judg, concept.questions.get(i), nal, true);
        }
        final int nng = concept.desires.size();
        for (int i = 0; i < nng; i++) {
            trySolution(judg, concept.desires.get(i), nal, true);
        }
        concept.addToTable(task, false, concept.beliefs, concept.memory.narParameters.CONCEPT_BELIEFS_MAX, Events.ConceptBeliefAdd.class, Events.ConceptBeliefRemove.class);
        if(isExecutableHypothesis(task,nal)) {
            addToTargetConceptsPreconditions(task, nal, concept);
        }
    }

    /**
     * Handle the feedback of the operation that was processed as a judgment.
     * <br>
     * The purpose is to start a new operation frame which makes the operation concept 
     * interpret current events as preconditions and future events as post-conditions to the invoked operation.
     * 
     * @param task The judgement task be checked
     * @param nal The derivation context
     */
    public static void handleOperationFeedback(Task task, DerivationContext nal) {
        if(task.isInput() && !task.sentence.isEternal() && task.sentence.term instanceof Operation) {
            final Operation op = (Operation) task.sentence.term;
            final Operator o = (Operator) op.getPredicate();
            //only consider these mental ops an operation to track when executed not already when generated as internal event
            if(!(o instanceof Believe) && !(o instanceof Want) && !(o instanceof Wonder)
                    && !(o instanceof Evaluate) && !(o instanceof Anticipate)) {
                TemporalInferenceControl.NewOperationFrame(nal.memory, task);
            }
        }
    }
    
    /**
     * Check whether the task is an executable hypothesis of the form
     * &lt;(&amp;/,a,op()) =/&gt; b&gt;.
     * 
     * @param task The judgement task be checked
     * @param nal The derivation context
     * @return Whether task is an executable precondition
     */
    protected static boolean isExecutableHypothesis(Task task, final DerivationContext nal) {
        final Term term = task.getTerm();
        if(!task.sentence.isEternal() ||
           !(term instanceof Implication) ||
           term.hasVarIndep())  //Might be relaxed in the future!!
        {
            return false;
        }
        final Implication imp = (Implication) term;
        if(imp.getTemporalOrder() != TemporalRules.ORDER_FORWARD) {
            return false;
        }
        //also it has to be enactable, meaning the last entry of the sequence before the interval is an operation:
        final Term subj = imp.getSubject();
        final Term pred = imp.getPredicate();
        final Concept pred_conc = nal.memory.concept(pred);
        if (pred_conc == null /*|| (pred instanceof Operation)*/ || !(subj instanceof Conjunction)) {
            return false;
        }
        final Conjunction conj = (Conjunction) subj;
        boolean isInExecutableFormat = !conj.isSpatial && 
                                        conj.getTemporalOrder() == TemporalRules.ORDER_FORWARD &&
                                        conj.term.length >= 4 && conj.term.length%2 == 0 &&
                                        conj.term[conj.term.length-1] instanceof Interval &&
                                        conj.term[conj.term.length-2] instanceof Operation;
        return isInExecutableFormat;
    }
    
    /**
     * Add &lt;(&amp;/,a,op()) =/&gt; b&gt; beliefs to preconditions in concept b
     * 
     * @param task The potential implication task
     * @param nal The derivation context
     * @param concept The concept of the task
     */
    protected static void addToTargetConceptsPreconditions(final Task task, final DerivationContext nal, final Concept concept) {
        final Concept target_concept = nal.memory.concept(((Implication)task.getTerm()).getPredicate());
        // we do not add the target, instead the strongest belief in the target concept

        // get the first eternal. the highest confident one (due to the sorted order):
        Optional<Task> strongest_target = tryFind(concept.beliefs, iTask -> iTask.sentence.isEternal());
        if (!strongest_target.isPresent()) {
            return;
        }

        synchronized(target_concept) {
            //at first we have to remove the last one with same content from table
            int i_delete = -1;
            for(int i=0; i < target_concept.executable_preconditions.size(); i++) {
                if(CompoundTerm.replaceIntervals(target_concept.executable_preconditions.get(i).getTerm()).equals(
                        CompoundTerm.replaceIntervals(strongest_target.get().getTerm()))) {
                    i_delete = i; //even these with same term but different intervals are removed here
                    break;
                }
            }
            if(i_delete != -1) {
                target_concept.executable_preconditions.remove(i_delete);
            }
            final Term[] prec = ((Conjunction) ((Implication) strongest_target.get().getTerm()).getSubject()).term;
            for (int i = 0; i<prec.length-2; i++) {
                if (prec[i] instanceof Operation) { //don't react to precondition with an operation before the last
                    return; //for now, these can be decomposed into smaller such statements anyway
                }
            }
            //this way the strongest confident result of this content is put into table but the table ranked according to truth expectation
            target_concept.addToTable(strongest_target.get(), true, target_concept.executable_preconditions, target_concept.memory.narParameters.CONCEPT_BELIEFS_MAX, Events.EnactableExplainationAdd.class, Events.EnactableExplainationRemove.class);
        }
    }
}
