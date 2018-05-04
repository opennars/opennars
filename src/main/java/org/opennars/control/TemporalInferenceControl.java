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
package org.opennars.control;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.opennars.main.Parameters;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.inference.BudgetFunctions;
import org.opennars.inference.TemporalRules;
import org.opennars.io.Symbols;
import org.opennars.language.CompoundTerm;
import org.opennars.operator.Operation;
import org.opennars.storage.LevelBag;
import org.opennars.storage.Memory;
import org.opennars.io.events.Events;

/**
 *
 * @author patrick.hammer
 */
public class TemporalInferenceControl {
    public static List<Task> proceedWithTemporalInduction(final Sentence newEvent, final Sentence stmLast, Task controllerTask, DerivationContext nal, boolean SucceedingEventsInduction, boolean addToMemory, boolean allowSequence) {
        
        if(SucceedingEventsInduction && !controllerTask.isElemOfSequenceBuffer()) { //todo refine, add directbool in task
            return null;
        }
        if (newEvent.isEternal() || !controllerTask.isInput()) {
            return null;
        }
        /*if (equalSubTermsInRespectToImageAndProduct(newEvent.term, stmLast.term)) {
            return false;
        }*/
        
        if(newEvent.punctuation!=Symbols.JUDGMENT_MARK || stmLast.punctuation!=Symbols.JUDGMENT_MARK)
            return null; //temporal inductions for judgements only
        
        nal.setTheNewStamp(newEvent.stamp, stmLast.stamp, nal.memory.time());
        nal.setCurrentTask(controllerTask);

        Sentence previousBelief = stmLast;
        nal.setCurrentBelief(previousBelief);

        Sentence currentBelief = newEvent;

        //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
        return TemporalRules.temporalInduction(currentBelief, previousBelief, nal, SucceedingEventsInduction, addToMemory, allowSequence);
    }

    public static boolean eventInference(final Task newEvent, DerivationContext nal) {

        if(newEvent.getTerm() == null || newEvent.budget==null || !newEvent.isElemOfSequenceBuffer()) { //todo refine, add directbool in task
            return false;
       }

        nal.emit(Events.InduceSucceedingEvent.class, newEvent, nal);

        if (!newEvent.sentence.isJudgment() || newEvent.sentence.isEternal() || !newEvent.isInput()) {
            return false;
       }

        HashSet<Task> already_attempted = new HashSet<Task>();
        HashSet<Task> already_attempted_ops = new HashSet<Task>();
        //Sequence formation:
        for(int i =0; i<Parameters.SEQUENCE_BAG_ATTEMPTS; i++) {
            Task takeout = nal.memory.seq_current.takeNext();
            if(takeout == null) {
                break; //there were no elements in the bag to try
            }

            if(already_attempted.contains(takeout) || 
                    Stamp.baseOverlap(newEvent.sentence.stamp.evidentialBase,
                            takeout.sentence.stamp.evidentialBase)) {
                nal.memory.seq_current.putBack(takeout, nal.memory.cycles(nal.memory.param.eventForgetDurations), nal.memory);
                continue;
            }
            already_attempted.add(takeout);
            try {
                proceedWithTemporalInduction(newEvent.sentence, takeout.sentence, newEvent, nal, true, true, true);
            } catch (Exception ex) {
                if(Parameters.DEBUG) {
                    System.out.println("issue in temporal induction");
                }
            }
            nal.memory.seq_current.putBack(takeout, nal.memory.cycles(nal.memory.param.eventForgetDurations), nal.memory);
        }

        //Conditioning:
        if(nal.memory.lastDecision != null && newEvent != nal.memory.lastDecision) {
            already_attempted_ops.clear();
            for(int k = 0; k<Parameters.OPERATION_SAMPLES;k++) {
                already_attempted.clear(); //todo move into k loop
                Task Toperation = k == 0 ? nal.memory.lastDecision : nal.memory.recent_operations.takeNext();
                if(Toperation == null) {
                    break; //there were no elements in the bag to try
                }
                if(already_attempted_ops.contains(Toperation)) {
                    //put opc back into bag
                    //(k>0 holds here):
                    nal.memory.recent_operations.putBack(Toperation, nal.memory.cycles(nal.memory.param.eventForgetDurations), nal.memory);
                    continue;
                }
                already_attempted_ops.add(Toperation);
                Concept opc = nal.memory.concept(Toperation.getTerm());
                if(opc != null) {
                    if(opc.seq_before == null) {
                        opc.seq_before = new LevelBag<>(Parameters.SEQUENCE_BAG_LEVELS, Parameters.SEQUENCE_BAG_SIZE);
                    }
                    for(int i = 0; i<Parameters.CONDITION_BAG_ATTEMPTS; i++) {
                        Task takeout = opc.seq_before.takeNext();
                        if(takeout == null) {
                            break; //there were no elements in the bag to try
                        }
                        if(already_attempted.contains(takeout)) {
                            opc.seq_before.putBack(takeout, nal.memory.cycles(nal.memory.param.eventForgetDurations), nal.memory);
                            continue;
                        }
                        already_attempted.add(takeout);
                        try {
                            long x = Toperation.sentence.getOccurenceTime();
                            long y = takeout.sentence.getOccurenceTime();
                            if(y > x) { //something wrong here?
                                System.out.println("analyze case in TemporalInferenceControl!");
                                continue;
                            }
                            List<Task> seq_op = proceedWithTemporalInduction(Toperation.sentence, takeout.sentence, nal.memory.lastDecision, nal, true, false, true);
                            for(Task t : seq_op) {
                                if(!t.sentence.isEternal()) { //TODO do not return the eternal here probably..;
                                    List<Task> res = proceedWithTemporalInduction(newEvent.sentence, t.sentence, newEvent, nal, true, true, false); //only =/> </> ..
                                    /*DEBUG: for(Task seq_op_cons : res) {
                                        System.out.println(seq_op_cons.toString());
                                    }*/
                                }
                            }

                        } catch (Exception ex) {
                            if(Parameters.DEBUG) {
                                System.out.println("issue in temporal induction");
                            }
                        }
                        opc.seq_before.putBack(takeout, nal.memory.cycles(nal.memory.param.eventForgetDurations), nal.memory);
                    }
                }
                //put Toperation back into bag if it was taken out
                if(k > 0) {
                    nal.memory.recent_operations.putBack(Toperation, nal.memory.cycles(nal.memory.param.eventForgetDurations), nal.memory);
                }
            }
        }
        
        addToSequenceTasks(nal, newEvent);
        return true;
    }
    
    public static void addToSequenceTasks(DerivationContext nal, final Task newEvent) {
        //multiple versions are necessary, but we do not allow duplicates
        List<Task> removals = new LinkedList<Task>();
        for(Task s : nal.memory.seq_current) {
            if(CompoundTerm.replaceIntervals(s.getTerm()).equals(
                    CompoundTerm.replaceIntervals(newEvent.getTerm()))) {
                    // && //-- new outcommented
                    //s.sentence.stamp.equals(newEvent.sentence.stamp,false,true,true,false) ) {
                //&& newEvent.sentence.getOccurenceTime()>s.sentence.getOccurenceTime() ) { 
                //check term indices
                if(s.getTerm().term_indices != null && newEvent.getTerm().term_indices != null) {
                    boolean differentTermIndices = false;
                    for(int i=0;i<s.getTerm().term_indices.length;i++) {
                       if(s.getTerm().term_indices[i] != newEvent.getTerm().term_indices[i]) {
                           differentTermIndices = true;
                       }
                    }
                    if(differentTermIndices) {
                        continue;
                    }
                }
                removals.add(s);
                break;
            }
        }
        for(Task removal : removals) {
            nal.memory.seq_current.take(removal);
        }
        //ok now add the new one:
        //making sure we do not mess with budget of the task:
        if(!(newEvent.sentence.getTerm() instanceof Operation)) {
            Concept c = nal.memory.concept(newEvent.getTerm());
            float event_quality = BudgetFunctions.truthToQuality(newEvent.sentence.truth);
            float event_priority = event_quality;
            if(c != null) {
                event_priority = Math.max(event_quality, c.getPriority());
            }
            Task t2 = new Task(newEvent.sentence, new BudgetValue(event_priority, 1.0f/(float)newEvent.sentence.term.getComplexity(), event_quality), newEvent.getParentBelief(), newEvent.getBestSolution());
            nal.memory.seq_current.putIn(t2);
        }
    }
    
    public static void NewOperationFrame(Memory mem, Task task) {
        List<Task> toRemove = new LinkedList<Task>(); //can there be more than one? I don't think so..
        float priorityGain = 0.0f;
        for(Task t : mem.recent_operations) {   //when made sure, make single element and add break
            if(t.getTerm().equals(task.getTerm())) {
                priorityGain = BudgetFunctions.or(priorityGain, t.getPriority());
                toRemove.add(t);
            }
        }
        for(Task t : toRemove) {
            mem.recent_operations.take(t);
        }
        task.setPriority(BudgetFunctions.or(task.getPriority(), priorityGain)); //this way operations priority of previous exections
        mem.recent_operations.putIn(task);                 //contributes to the current (enhancement)
        mem.lastDecision = task;
        Concept c = (Concept) mem.concept(task.getTerm());
        if(c != null) {
            if(c.seq_before == null) {
                c.seq_before = new LevelBag<>(Parameters.SEQUENCE_BAG_LEVELS, Parameters.SEQUENCE_BAG_SIZE);
            }
            for(Task t : mem.seq_current) {
                if(task.sentence.getOccurenceTime() > t.sentence.getOccurenceTime()) {
                    c.seq_before.putIn(t);
                }
            }
        }
        mem.seq_current.clear();
    }
}
