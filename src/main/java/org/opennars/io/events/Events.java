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
package org.opennars.io.events;

import java.util.Arrays;
import java.util.List;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.control.GeneralInferenceControl;
import org.opennars.control.DerivationContext;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;

/** empty event classes for use with EventEmitter */
public class Events {


    /** fired at the beginning of each NAR multi-cycle execution*/
    public static class CyclesStart {     } 
    
    /** fired at the end of each NAR multi-cycle execution */
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
        public ConceptNew(Concept c, long when) {
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
        
        @Override public void event(Class event, Object[] args) {
            onBeliefAdd( (Concept)args[0], (Task)args[1], (Object[])args[2]);
        }
        
    }
    
    abstract public static class ConceptBeliefRemove implements EventObserver { 
        
        abstract public void onBeliefRemove(Concept c, Sentence removed, Task t, Object[] extra);
        
        @Override public void event(Class event, Object[] args) {
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
        
        @Override public void event(Class event, Object[] args) {
            onFire((GeneralInferenceControl)args[0]);
        }
        
    }
    abstract public static class TaskImmediateProcess implements EventObserver { 

        abstract public void onProcessed(Task t, DerivationContext n);
        
        @Override public void event(Class event, Object[] args) {
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
        
        @Override public void event(Class event, Object[] args) {
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
        int STACK_PREFIX = 4;

        protected InferenceEvent(long when) {
            this(when, 0);
        }
        
        protected InferenceEvent(long when, int stackFrames) {
            this.when = when;
            
            if (stackFrames > 0) {
                List<StackTraceElement> sl = Arrays.asList(Thread.currentThread().getStackTrace());

                int frame = 0;
                
                for (StackTraceElement e : sl) {
                    frame++;
                    if (e.getClassName().equals("org.opennars.core.NAR")) {
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

        public ParametricInferenceEvent(O object, long when) {
            super(when);
            this.object = object;
        }
        
        
        
    }
    
    
}
