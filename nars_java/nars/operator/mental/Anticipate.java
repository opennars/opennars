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

    Term anticipateTerm = null;
    long anticipateTime = 0;
    
    //TODO set this by an optional additional parameter to ^anticipate
    float anticipateDurations = 1f;
    
    public Anticipate() {
        super("^anticipate");        
    }

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        n.memory.event.set(this, enabled, Events.InduceSucceedingEvent.class);
        return true;
    }
    
    @Override
    public void event(Class event, Object[] args) {
        if (event == InduceSucceedingEvent.class) {            
            
            Task newEvent = (Task)args[0];
            NAL nal = (NAL)args[1];
            Sentence newSentence = newEvent.sentence;
            
            if ((anticipateTerm!=null) && order(anticipateTime, newSentence.getOccurenceTime(), nal.memory.getDuration()) == TemporalRules.ORDER_FORWARD) {
                Term s = newEvent.sentence.content;
                TruthValue truth = new TruthValue(0.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                Term N = Negation.make(s);
                Stamp stamp = new Stamp(nal.memory);
                Sentence S = new Sentence(N, Symbols.JUDGMENT_MARK, truth, stamp);
                BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(truth));
                Task task = new Task(S, budget);
                nal.derivedTask(task, false, true, null, null);
                anticipateTerm = null;
            }
            
            if ((anticipateTerm!=null) && newSentence.truth.getExpectation() > 0.5 && newSentence.content.equals(anticipateTerm)) {
                anticipateTerm = null; //it happened like expected
            }
            
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
        
        Term content = args[0];
        anticipateTime=memory.time() + (int)(memory.getDuration() * anticipateDurations);
        anticipateTerm=content;
        
        return null;
    }

}