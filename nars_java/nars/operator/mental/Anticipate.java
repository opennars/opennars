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
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.Events.CycleEnd;
import nars.core.Events.InduceSucceedingEvent;
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
import static nars.inference.TemporalRules.order;
import nars.io.Symbols;
import nars.language.Negation;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * Operator that creates a judgment with a given statement
 */
public class Anticipate extends Operator implements EventObserver, Mental {

    public class Anticipation
    {
        public Term anticipateTerm;
        public long anticipateTime;
        public Anticipation(Term anticipateTerm, long anticipateTime) {
            this.anticipateTerm=anticipateTerm;
            this.anticipateTime=anticipateTime;
        }
    }
    ArrayList<Anticipation> anticipations = new ArrayList<Anticipation>(); //todo make both arrays
    NAL nal;
    
    //TODO set this by an optional additional parameter to ^anticipate
    float anticipateDurations = 1f;
    
    public Anticipate() {
        super("^anticipate");        
    }

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        n.memory.event.set(this, enabled, Events.InduceSucceedingEvent.class);
        n.memory.event.set(this, enabled, Events.CycleEnd.class);
        return true;
    }
    
    public void manageAnticipations(Task newTask) {
        ArrayList<Anticipation> toRemove=new ArrayList<>();
        long time=nal.memory.time();
        
        for(int i=0;i<anticipations.size();i++) {
            Term anticipateTerm=anticipations.get(i).anticipateTerm;
            long anticipateTime=anticipations.get(i).anticipateTime;

            if (time-anticipateTime>nal.memory.param.duration.get()) {
                Term s = anticipateTerm;
                TruthValue truth = new TruthValue(0.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                Stamp stamp = new Stamp(nal.memory);
                Sentence S = new Sentence(s, Symbols.JUDGMENT_MARK, truth, stamp);
                BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(truth));
                Task task = new Task(S, budget);
                nal.derivedTask(task, false, true, null, null);
                task.NotConsideredByTemporalInduction=false;
                toRemove.add(anticipations.get(i));
            }

            if (newTask!=null && Math.abs(anticipateTime-time)<nal.memory.param.duration.get() 
                    && newTask.sentence.truth.getExpectation()>0.5 && newTask.sentence.content.equals(anticipateTerm)) {
                toRemove.add(anticipations.get(i));//it happened like expected
            }
        }
        for(Anticipation anticipation : toRemove) {
            anticipations.remove(anticipation);
        }
    }
    
    @Override
    public void event(Class event, Object[] args) {
        if (nal!=null && event == CycleEnd.class) {            
            manageAnticipations(null);
        }
        if (event == Events.InduceSucceedingEvent.class) {            
            Task newEvent = (Task)args[0];
            this.nal= (NAL)args[1];
            manageAnticipations(newEvent);
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
        anticipate(args[0],memory);
        
        return null;
    }
    
    public void anticipate(Term content,Memory memory) {
        anticipations.add(new Anticipation(content, (memory.time() + (int)(memory.getDuration() * anticipateDurations))));
    }

}