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
package org.opennars.storage;

import org.opennars.control.DerivationContext;
import org.opennars.interfaces.Timable;
import org.opennars.language.Term;
import org.opennars.main.Parameters;
import org.opennars.interfaces.Timable;
import org.opennars.main.Nar;
import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.inference.TemporalRules;
import org.opennars.io.Symbols;
import org.opennars.io.events.Events;
import org.opennars.language.CompoundTerm;
import org.opennars.operator.Operation;
import org.opennars.storage.Bag;
import org.opennars.storage.Memory;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ArrayList; 
import java.util.List;
import java.util.Set;

public class Buffer extends Bag<Task<Term>,Sentence<Term>> {
    
    Nar nar;
    Parameters narParameters;
    long max_duration;
    static int duration = 100; //buffer duration, TODO make a param
    public Buffer seq_current; //for temporal inference support via BufferInference.java
    
    public Buffer(Nar nar, int levels, int capacity, Parameters narParameters) {
        super(levels, capacity, narParameters);
        this.nar = nar;
        this.narParameters = narParameters;
    }
    
    @Override
    public boolean expired(long creationTime) {
        long currentTime = nar.time();
        long delta = currentTime - creationTime;
        long maxDuration = narParameters.DURATION * narParameters.MAX_BUFFER_DURATION_FACTOR;
        return delta > maxDuration;
    }
    
    public void clearExpiredItem(){

        ArrayList<Sentence> expiredKey = new ArrayList<Sentence>();

        nameTable.forEach((key, task) -> {
            if((nar.time() - task.sentence.stamp.getPutInTime()) > duration){
                expiredKey.add(key);
            }
        });

        for (int i = 0; i < expiredKey.size(); i++) {
            Task task = nameTable.get(expiredKey.get(i));
            super.pickOut(task.sentence); //task.getKey()
        }

    }
    
    public Task putIn(Task task){
        if(task.sequenceTask) //essentially marked as relevant for temporal reasoning by being input event or temporal rule
        {
            addToSequenceTasks(task);
            return task;
        }
        task.sentence.stamp.setPutInTime(nar.time());
        return (Task) super.putIn(task);
    } 
    
    public Task takeOut(){
        clearExpiredItem();
        return super.takeOut();
    }
    
    public static List<Task> proceedWithTemporalInduction(final Sentence newEvent, final Sentence stmLast, final Task controllerTask, final DerivationContext nal, final boolean SucceedingEventsInduction, final boolean addToMemory, final boolean allowSequence) {
        
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
        
        nal.setTheNewStamp(newEvent.stamp, stmLast.stamp, nal.time.time());
        nal.setCurrentTask(controllerTask);

        final Sentence previousBelief = stmLast;
        nal.setCurrentBelief(previousBelief);

        final Sentence currentBelief = newEvent;

        //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
        return TemporalRules.temporalInduction(currentBelief, previousBelief, nal, SucceedingEventsInduction, addToMemory, allowSequence);
    }

    public boolean eventInference(final Task newEvent, final DerivationContext nal) {

        if(newEvent.getTerm() == null || newEvent.budget==null || !newEvent.isElemOfSequenceBuffer()) { //todo refine, add directbool in task
            return false;
       }

        nal.emit(Events.InduceSucceedingEvent.class, newEvent, nal);

        if (!newEvent.sentence.isJudgment() || newEvent.sentence.isEternal() || !newEvent.isInput()) {
            return false;
       }

        final Set<Task> already_attempted = new LinkedHashSet<>();
        final Set<Task> already_attempted_ops = new LinkedHashSet<>();
        //Sequence formation:
        for(int i =0; i<nal.narParameters.SEQUENCE_BAG_ATTEMPTS; i++) {
            synchronized(seq_current) {
                final Task takeout = seq_current.takeOut();
                if(takeout == null) {
                    break; //there were no elements in the bag to try
                }

                if(already_attempted.contains(takeout) || 
                        Stamp.baseOverlap(newEvent.sentence.stamp, takeout.sentence.stamp)) {
                    seq_current.putBack(takeout, nal.memory.cycles(nal.memory.narParameters.EVENT_FORGET_DURATIONS), nal.memory);
                    continue;
                }
                already_attempted.add(takeout);
                proceedWithTemporalInduction(newEvent.sentence, takeout.sentence, newEvent, nal, true, true, true);
                seq_current.putBack(takeout, nal.memory.cycles(nal.memory.narParameters.EVENT_FORGET_DURATIONS), nal.memory);
            }
        }

        //Conditioning:
        if(nal.memory.lastDecision != null && newEvent != nal.memory.lastDecision) {
            already_attempted_ops.clear();
            for(int k = 0; k<nal.narParameters.OPERATION_SAMPLES;k++) {
                already_attempted.clear(); //todo move into k loop
                final Task Toperation = k == 0 ? nal.memory.lastDecision : nal.memory.recent_operations.takeOut();
                if(Toperation == null) {
                    break; //there were no elements in the bag to try
                }
                if(already_attempted_ops.contains(Toperation)) {
                    //put opc back into bag
                    //(k>0 holds here):
                    nal.memory.recent_operations.putBack(Toperation, nal.memory.cycles(nal.memory.narParameters.EVENT_FORGET_DURATIONS), nal.memory);
                    continue;
                }
                already_attempted_ops.add(Toperation);
                final Concept opc = nal.memory.concept(Toperation.getTerm());
                if(opc != null) {
                    if(opc.seq_before == null) {
                        opc.seq_before = new Bag<>(nal.narParameters.SEQUENCE_BAG_LEVELS, nal.narParameters.SEQUENCE_BAG_SIZE, nal.narParameters);
                    }
                    for(int i = 0; i<nal.narParameters.CONDITION_BAG_ATTEMPTS; i++) {
                        final Task takeout = opc.seq_before.takeOut();
                        if(takeout == null) {
                            break; //there were no elements in the bag to try
                        }
                        if(already_attempted.contains(takeout)) {
                            opc.seq_before.putBack(takeout, nal.memory.cycles(nal.memory.narParameters.EVENT_FORGET_DURATIONS), nal.memory);
                            continue;
                        }
                        already_attempted.add(takeout);
                        final long x = Toperation.sentence.getOccurenceTime();
                        final long y = takeout.sentence.getOccurenceTime();
                        if(y > x) { //something wrong here?
                            System.out.println("analyze case in TemporalInferenceControl!");
                            continue;
                        }
                        final List<Task> seq_op = proceedWithTemporalInduction(Toperation.sentence, takeout.sentence, nal.memory.lastDecision, nal, true, false, true);
                        for(final Task t : seq_op) {
                            if(!t.sentence.isEternal()) { //TODO do not return the eternal here probably..;
                                final List<Task> res = proceedWithTemporalInduction(newEvent.sentence, t.sentence, newEvent, nal, true, true, false); //only =/> </> ..
                                /*DETAILED: for(Task seq_op_cons : res) {
                                    System.out.println(seq_op_cons.toString());
                                }*/
                            }
                        }

                        opc.seq_before.putBack(takeout, nal.memory.cycles(nal.memory.narParameters.EVENT_FORGET_DURATIONS), nal.memory);
                    }
                }
                //put Toperation back into bag if it was taken out
                if(k > 0) {
                    nal.memory.recent_operations.putBack(Toperation, nal.memory.cycles(nal.memory.narParameters.EVENT_FORGET_DURATIONS), nal.memory);
                }
            }
        }
        
        addToSequenceTasks(newEvent);
        return true;
    }
    
    private void addToSequenceTasks(final Task newEvent) {
        //multiple versions are necessary, but we do not allow duplicates
        Task removal = null;
        synchronized(seq_current) {
            for(final Task s : seq_current) {
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
                    removal = s;
                    break;
                }
            }
            if (removal != null) {
                seq_current.pickOut(removal);
            }

            //ok now add the new one:
            //making sure we do not mess with budget of the task:
            if(!(newEvent.sentence.getTerm() instanceof Operation)) {
                final Concept c = nar.memory.concept(newEvent.getTerm());
                final float event_quality = BudgetFunctions.truthToQuality(newEvent.sentence.truth);
                float event_priority = event_quality;
                if(c != null) {
                    event_priority = Math.max(event_quality, c.getPriority());
                }
                final Task t2 = new Task(newEvent.sentence,
                                         new BudgetValue(event_priority, 1.0f/(float)newEvent.sentence.term.getComplexity(), event_quality, nar.narParameters),
                                         newEvent.getParentBelief(),
                                         newEvent.getBestSolution());
                seq_current.putIn(t2);
            }
        }
    }
}
