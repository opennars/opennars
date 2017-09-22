/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.control;

import java.util.HashSet;
import java.util.List;
import nars.config.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Term;
import nars.operator.Operation;
import nars.storage.Bag;
import nars.storage.Memory;
import nars.util.Events;

/**
 *
 * @author patrick.hammer
 */
public class TemporalInferenceControl {
    public static List<Task> proceedWithTemporalInduction(final Sentence newEvent, final Sentence stmLast, Task controllerTask, DerivationContext nal, boolean SucceedingEventsInduction, boolean addToMemory, boolean allowSequence) {
        
        if(SucceedingEventsInduction && !controllerTask.isElemOfSequenceBuffer()) { //todo refine, add directbool in task
            return null;
        }
        if (newEvent.isEternal() || !controllerTask.isInputOrOperation()) {
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

        if (!newEvent.sentence.isJudgment() || newEvent.sentence.isEternal() || !newEvent.isInputOrOperation()) {
            return false;
       }

        if(Parameters.TEMPORAL_INDUCTION_ON_SUCCEEDING_EVENTS) {
            HashSet<Task> already_attempted = new HashSet<Task>();
            
            //Sequence formation:
            for(int i =0; i<Parameters.SEQUENCE_BAG_ATTEMPTS; i++) {
                Task takeout = nal.memory.seq_current.takeNext();
                if(takeout == null) {
                    break; //there were no elements in the bag to try
                }
                if(already_attempted.contains(takeout)) {
                    nal.memory.seq_current.putBack(takeout, nal.memory.cycles(nal.memory.param.sequenceForgetDurations), nal.memory);
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
                nal.memory.seq_current.putBack(takeout, nal.memory.cycles(nal.memory.param.sequenceForgetDurations), nal.memory);
            }
            
            //Conditioning:
            if(nal.memory.lastDecision != null && newEvent != nal.memory.lastDecision) {
                already_attempted.clear();
                for(int i =0; i<Parameters.CONDITION_BAG_ATTEMPTS; i++) {
                    Task takeout = nal.memory.seq_before.takeNext();
                    if(takeout == null) {
                        break; //there were no elements in the bag to try
                    }
                    if(already_attempted.contains(takeout)) {
                        nal.memory.seq_before.putBack(takeout, nal.memory.cycles(nal.memory.param.sequenceForgetDurations), nal.memory);
                        continue;
                    }
                    already_attempted.add(takeout);
                    try {
                        List<Task> seq_op = proceedWithTemporalInduction(nal.memory.lastDecision.sentence, takeout.sentence, nal.memory.lastDecision, nal, true, false, true);
                        for(Task t : seq_op) {
                            if(!t.sentence.isEternal()) { //TODO do not return the eternal here probably..
                                List<Task> res = proceedWithTemporalInduction(newEvent.sentence, t.sentence, newEvent, nal, true, true, false); //only =/> </> ..
                                for(Task seq_op_cons : res) {
                                    System.out.println(seq_op_cons.toString());
                                }
                            }
                        }

                    } catch (Exception ex) {
                        if(Parameters.DEBUG) {
                            System.out.println("issue in temporal induction");
                        }
                    }
                    nal.memory.seq_before.putBack(takeout, nal.memory.cycles(nal.memory.param.sequenceForgetDurations), nal.memory);
                }
            }
        }
        
        addToSequenceTasks(nal, newEvent);
        /*for (int i = 0; i < 10; ++i) System.out.println();
        System.out.println("----------");
        for(Task t : nal.memory.sequenceTasks) {
            System.out.println(t.sentence.getTerm().toString()+ " " +String.valueOf(t.getPriority()));
        }
        System.out.println("^^^^^^^^^");*/
        return true;
    }
    
    public static void addToSequenceTasks(DerivationContext nal, final Task newEvent) {
        //multiple versions are necessary, but we do not allow duplicates
        Task removal = null;
        do
        {
            removal = null;
            for(Task s : nal.memory.seq_current) {
                if(CompoundTerm.cloneDeepReplaceIntervals(s.getTerm()).equals(
                        CompoundTerm.cloneDeepReplaceIntervals(newEvent.getTerm()))) {
                        // && //-- new outcommented
                        //s.sentence.stamp.equals(newEvent.sentence.stamp,false,true,true,false) ) {
                    //&& newEvent.sentence.getOccurenceTime()>s.sentence.getOccurenceTime() ) { 
                    removal = s;
                    break;
                }
            }
            if(removal != null) {
                nal.memory.seq_current.take(removal);
            }
        }
        while(removal != null);
        //ok now add the new one:
        //making sure we do not mess with budget of the task:
        if(!(newEvent.sentence.getTerm() instanceof Operation)) {
            Concept c = nal.memory.concept(newEvent.getTerm());
            float event_quality = 0.1f;
            float event_priority = event_quality;
            if(c != null) {
                event_priority = Math.max(event_quality, c.getPriority());
            }
            Task t2 = new Task(newEvent.sentence, new BudgetValue(event_priority, 1.0f/(float)newEvent.sentence.term.getComplexity(), event_quality), newEvent.getParentTask(), newEvent.getParentBelief(), newEvent.getBestSolution());
            nal.memory.seq_current.putIn(t2);
        }
        //debug:
        /*System.out.println("---------");
        for(Task t : this.sequenceTasks) {
            System.out.println(t.getTerm().toString());
        }
        System.out.println("^^^^^^");*/
    }
    
    public static void NewOperationFrame(Memory mem, Task task) {
        mem.lastDecision = task;
        mem.seq_before.clear(); //since no one samples from it, apply that radical forgetting for now
        for(Task t : mem.seq_current) {
            mem.seq_before.putIn(t);
        }
        mem.seq_current.clear();
    }
}
