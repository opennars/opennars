package nars.core;

import java.util.Arrays;
import java.util.List;
import nars.entity.Concept;

/** empty event classes for use with EventEmitter */
public class Events {


    /** fired at the beginning of each NAR frame */
    public static class FrameStart {     } 
    
    /** fired at the end of each NAR frame */
    public static class FrameEnd {     }
    
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
    
    //when remembered a previously forgotten concept
    public static class ConceptRemember {    }
    
    public static class ConceptForget { }
    public static class ConceptBeliefAdd { }
    public static class ConceptBeliefRemove { }
    
    public static class ConceptGoalAdd { }
    public static class ConceptGoalRemove { }
    public static class ConceptQuestionAdd { }
    public static class ConceptQuestionRemove { }

    
    //planning
    public static class UnexecutableGoal {   }
    public static class UnexecutableOperation {   }
    public static class NewTaskExecution {    }
    

    public static class TermLinkAdd { }
    public static class TermLinkRemove { }
    public static class TaskLinkAdd { }
    public static class TaskLinkRemove { }
    
    public static class Solved { }
    public static class Unsolved { }
    
    
    public static class ConceptFire { }
    public static class TaskImmediateProcess { }
    public static class TermLinkSelect { }
    public static class BeliefSelect { }
    public static class ConceptUnification { } //2nd level unification in CompositionalRules

    public static class TaskAdd { }
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
                    if (e.getClassName().equals("nars.core.NAR")) {
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
