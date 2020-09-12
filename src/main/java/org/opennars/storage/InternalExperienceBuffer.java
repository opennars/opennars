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

import org.opennars.main.Nar;
import org.opennars.main.Parameters;

import org.opennars.control.DerivationContext;
import org.opennars.entity.Concept;
import org.opennars.entity.Task;
import org.opennars.inference.BudgetFunctions;

import java.util.List;
import java.util.LinkedList;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.operator.mental.Anticipate;
import org.opennars.operator.mental.Believe;
import org.opennars.operator.mental.Evaluate;
import org.opennars.operator.mental.Want;
import org.opennars.operator.mental.Wonder;

/**
 *
 * @author Peter
 */

public class InternalExperienceBuffer extends Buffer{

    public InternalExperienceBuffer(Nar nar, int levels, int capacity, Parameters narParameters)
    {
        super(nar, levels, capacity, narParameters);
    }

    /**
     * A helper function for handling op feedback more efficiently
     * 
     **/
    private static void NewOperationFrame(final Memory mem, final Task task) {
        final List<Task> toRemove = new LinkedList<>(); //can there be more than one? I don't think so..
        float priorityGain = 0.0f;
        for(final Task t : mem.recent_operations) {   //when made sure, make single element and add break
            if(t.getTerm().equals(task.getTerm())) {
                priorityGain = BudgetFunctions.or(priorityGain, t.getPriority());
                toRemove.add(t);
            }
        }
        for(final Task t : toRemove) {
            mem.recent_operations.pickOut(t);
        }
        task.setPriority(BudgetFunctions.or(task.getPriority(), priorityGain)); //this way operations priority of previous exections
        mem.recent_operations.putIn(task);                 //contributes to the current (enhancement)
        mem.lastDecision = task;
        final Concept c = mem.concept(task.getTerm());
        synchronized(mem.globalBuffer.seq_current) {
            if(c != null) {
                if(c.seq_before == null) {
                    c.seq_before = new Bag<>(mem.narParameters.SEQUENCE_BAG_LEVELS, mem.narParameters.SEQUENCE_BAG_SIZE, mem.narParameters);
                }
                for(final Task t : mem.globalBuffer.seq_current) {
                    if(task.sentence.getOccurenceTime() > t.sentence.getOccurenceTime()) {
                        c.seq_before.putIn(t);
                    }
                }
            }
            mem.globalBuffer.seq_current.clear();
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
                NewOperationFrame(nal.memory, task);
            }
        }
    }
}
