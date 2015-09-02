package nars;

import java.util.Arrays;
import java.util.List;

/* NAR reasoner events - to be replaced with individual Observed event emittters */
@Deprecated public class Events {


    /** implicitly repeated input (a repetition of all input) */
    public static interface IN  { }

    /** for misc debug events */
    public interface DEBUG {     }

    public interface OUT {     }


    /** warnings, errors & exceptions */
    public static interface ERR { }

    /** operation execution */
    public static interface EXE  { }

    /** fired at the beginning of each NAR frame */
    public static class FrameStart {     } 
    
    /** fired at the end of each NAR frame */
    public static class FrameEnd {     }
    

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
    public static class Restart {
    }
    
    


    /** if a concept is completely removed from both main, and subconcepts (or if subconcepts has capacity 0) */
    @Deprecated public static class ConceptDelete { }
    

    
    //Executive & Planning
    //public static class UnexecutableGoal {   }
    public static class UnexecutableOperation {   }
    public static class NewTaskExecution {    }
    public static class InduceSucceedingEvent {    }
    public static class EventBasedReasoningEvent {    }

    /*
    public static class TermLinkAdd { }
    public static class TermLinkRemove { }
    public static class TaskLinkAdd { }
    public static class TaskLinkRemove { }
    */
    
    public static class Answer { }


    public static class TermLinkTransformed {    }


    public static class BeliefSelect { }
    
//    /** called from RuleTables.rule for a given Belief */
//    public static class BeliefReason {    }
    
    public static class ConceptUnification { } //2nd level unification in CompositionalRules



    /** when a task has been derived */
    public static class TaskDerive {    }

    /** for derivations that occurr in the future; events sent here are also sent to Taskderive event */
    public static class TaskDeriveFuture {    }

    public static class PluginsChange {    }

    //public static class UnExecutedGoal {    }


    abstract public static class InferenceEvent {

        public final long when;
        public final List<StackTraceElement> stack;

        //how many stack frames down to record from; we don't need to include the current and the previous (InferenceEvent subclass's constructor
        final static int STACK_PREFIX = 4;

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

        public Object getType() {
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
