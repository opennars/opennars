/*
 * Believe.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.operator.mental;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.Events.CycleEnd;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.language.Conjunction;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * Operator that creates a judgment with a given statement
 */
public class Anticipate extends Operator implements EventObserver, Mental {

    public static class Anticipation {
        public final Term anticipateTerm;
        public final long anticipateTime;
        public Anticipation(Term anticipateTerm, long anticipateTime) {
            this.anticipateTerm=anticipateTerm;
            this.anticipateTime=anticipateTime;
        }
    }
    
    public final LinkedList<Anticipation> anticipations = new LinkedList<Anticipation>(); //todo make both arrays
    Set<Term> newTasks = new LinkedHashSet();
    NAL nal;
    
    //TODO set this by an optional additional parameter to ^anticipate
    float anticipateDurations = 2f;
    
    /** how long to allow a hoped-for event to occurr before counting evidence
     *  that it has not.  usually a <0.5 value which is a factor of duration 
        "disappointmentOvercomesHopeDuration" */
    float hopeExpirationDurations = 0f;
    
    public Anticipate() {
        super("^anticipate");        
    }

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        n.memory.event.set(this, enabled, Events.InduceSucceedingEvent.class, Events.CycleEnd.class);
        return true;
    }
    
    
    
    public void updateAnticipations() {
        
        
        if (anticipations.isEmpty()) return;

        
        long now=nal.memory.time();
        
        
        long duration = nal.memory.getDuration();
        long window = (long)(duration/2f * anticipateDurations);
        long hopeExpirationWindow = (long)(duration * hopeExpirationDurations);
                
        
        //share stamps created by tasks in this cycle
        
        boolean hasNewTasks = !newTasks.isEmpty();
        
        Iterator<Anticipation> ii = anticipations.iterator();
        while (ii.hasNext()) {
            final Anticipation a = ii.next();
            
            Term aTerm = a.anticipateTerm;
            long aTime = a.anticipateTime;

            boolean remove = false;
            
            if (now-aTime > hopeExpirationWindow) {
                TruthValue truth = new TruthValue(0.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                
                Stamp cycleStamp = null;
                if (cycleStamp == null) {
                    cycleStamp = new Stamp(nal.memory);
                    int derivation_tolerance_mul=2;
                    cycleStamp.setOccurrenceTime(
                        cycleStamp.getOccurrenceTime()-derivation_tolerance_mul*duration);
                }
                
                Sentence S = new Sentence(aTerm, Symbols.JUDGMENT_MARK, truth, cycleStamp);
                BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(truth));
                Task task = new Task(S, budget);
                
                nal.derivedTask(task, false, true, null, null); 

                task.NotConsideredByTemporalInduction=false;
                
                
                remove = true;
            }

            if (hasNewTasks && Math.abs(aTime - now) <= window && newTasks.remove(aTerm)) {
                //it happened like expected                
                remove = true; 
                hasNewTasks = !newTasks.isEmpty();
            }
            
            
            if (remove)
                ii.remove();
        }
       
    
        newTasks.clear();
        
    }
    
    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.InduceSucceedingEvent.class) {            
            Task newEvent = (Task)args[0];
            this.nal= (NAL)args[1];
            
            if (newEvent.sentence.truth!=null) {
                float newTaskExpectation = newEvent.sentence.truth.getExpectation();
                if (newTaskExpectation > 0.5)
                    newTasks.add(newEvent.getContent());
            }
        }

        if (nal!=null && event == CycleEnd.class) {            
            updateAnticipations();
        }
        
    }
    

    /**
     * To create a judgment with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
+    * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        if(operation!=null) {
            return null; //not as mental operator but as fundamental principle
        }
        
        anticipate(args[0],memory,memory.time()+memory.getDuration());
        
        return null;
    }
    
    public void anticipate(Term content,Memory memory, long occurenceTime) {
        if(content instanceof Conjunction && ((Conjunction)content).getTemporalOrder()!=TemporalRules.ORDER_NONE) {
            return;
        }
        anticipations.add(new Anticipation(content, occurenceTime));
    }

}