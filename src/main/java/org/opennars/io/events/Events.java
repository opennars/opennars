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
package org.opennars.io.events;

import org.opennars.control.DerivationContext;
import org.opennars.control.GeneralInferenceControl;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.io.events.EventEmitter.EventObserver;

import java.util.Arrays;
import java.util.List;

/** empty event classes for use with EventEmitter
 *
 */
public class Events {


    /** fired at the beginning of each Nar multi-cycle execution*/
    public static class CyclesStart {     } 
    
    /** fired at the end of each Nar multi-cycle execution */
    public static class CyclesEnd {     }
    
    /** fired at the beginning of each memory cycle */
    public static class CycleStart {     } 
    
    /** fired at the end of each memory cycle */
    public static class CycleEnd {     }

    /** fired at the beginning of each individual Memory work cycle */
    public static class WorkCycleStart {
    }

    /** fired at the end of each Memory individual cycle */
    public static class WorkCycleEnd {
    }

    /** called before memory.reset() proceeds */
    public static class ResetStart {
    }

    /** called after memory.reset() proceeds */
    public static class ResetEnd {
    }
    
    
    public static class ConceptNew extends ParametricInferenceEvent<Concept> {
        public ConceptNew(final Concept c, final long when) {
            super(c, when);
        }
        
        @Override public String toString() {
            return "Concept Created: " + object;
        }        
    }
    
    public static class Perceive {    }
    
    public static class ConceptForget { }
    
    public static class EnactableExplainationAdd { }
    public static class EnactableExplainationRemove { }
    
    abstract public static class ConceptBeliefAdd implements EventObserver {  
        
        abstract public void onBeliefAdd(Concept c, Task t, Object[] extra);
        
        @Override public void event(final Class event, final Object[] args) {
            onBeliefAdd( (Concept)args[0], (Task)args[1], (Object[])args[2]);
        }
        
    }
    
    abstract public static class ConceptBeliefRemove implements EventObserver { 
        
        abstract public void onBeliefRemove(Concept c, Sentence removed, Task t, Object[] extra);
        
        @Override public void event(final Class event, final Object[] args) {
            onBeliefRemove( (Concept)args[0], (Sentence)args[1], (Task)args[2], (Object[])args[3]);
        }        

    }
    
    public static class ConceptGoalAdd { }
    public static class ConceptGoalRemove { }
    public static class ConceptQuestionAdd { }
    public static class ConceptQuestionRemove { }

    
    //Executive & Planning
    public static class UnexecutableGoal {   }
    public static class UnexecutableOperation {   }
    public static class NewTaskExecution {    }
    public static class InduceSucceedingEvent {    }
    

    public static class TermLinkAdd { }
    public static class TermLinkRemove { }
    public static class TaskLinkAdd { }
    public static class TaskLinkRemove { }
    
    public static class Answer { }
    public static class Unsolved { }
    
    
    
    abstract public static class ConceptFire implements EventObserver { 
        
        /**
         * use:
         * Concept n.getCurrentConcept()
         * TaskLink n.getCurrentTaskLink()
         */
        abstract public void onFire(GeneralInferenceControl n);
        
        @Override public void event(final Class event, final Object[] args) {
            onFire((GeneralInferenceControl)args[0]);
        }
        
    }
    abstract public static class TaskImmediateProcess implements EventObserver { 

        abstract public void onProcessed(Task t, DerivationContext n);
        
        @Override public void event(final Class event, final Object[] args) {
            onProcessed((Task)args[0], (DerivationContext)args[1]);
        }
        
    }
    public static class TermLinkSelect { }
    public static class BeliefSelect { }
    
    /** called from RuleTables.reason for a given Belief */
    public static class BeliefReason {    }
    
    public static class ConceptUnification { } //2nd level unification in CompositionalRules

    abstract public static class TaskAdd implements EventObserver { 
        
        abstract public void onTaskAdd(Task t, String reason);
        
        @Override public void event(final Class event, final Object[] args) {
            onTaskAdd((Task)args[0], (String)args[1]);
        }
    }
    public static class TaskRemove { }
    public static class TaskDerive {    }

    public static class PluginsChange {    }

    //public static class UnExecutedGoal {    }

    public static class ConceptDirectProcessedTask {    }

    abstract public static class InferenceEvent {

        public final long when;
        public final List<StackTraceElement> stack;

        //how many stack frames down to record from; we don't need to include the current and the previous (InferenceEvent subclass's constructor
        final int STACK_PREFIX = 4;

        protected InferenceEvent(final long when) {
            this(when, 0);
        }
        
        protected InferenceEvent(final long when, final int stackFrames) {
            this.when = when;
            
            if (stackFrames > 0) {
                final List<StackTraceElement> sl = Arrays.asList(Thread.currentThread().getStackTrace());

                int frame = 0;
                
                for (final StackTraceElement e : sl) {
                    frame++;
                    if (e.getClassName().equals("org.opennars.core.Nar")) {
                        break;
                    }                    
                }
                if (frame - STACK_PREFIX > stackFrames)
                    frame = STACK_PREFIX + stackFrames;
                this.stack = sl.subList(STACK_PREFIX, frame);
            }
            else {
                this.stack = null;
            }
        }

        public Class getType() {
            return getClass();
        }

    }

    abstract public static class ParametricInferenceEvent<O> extends InferenceEvent {    
        public final O object;

        public ParametricInferenceEvent(final O object, final long when) {
            super(when);
            this.object = object;
        }
        
        
        
    }
    
    
}
