package nars;

import nars.concept.Concept;
import nars.premise.Premise;
import nars.task.Sentence;
import nars.task.Task;
import nars.util.event.Reaction;

import java.util.Arrays;
import java.util.List;

/* NAR reasoner events */
public class Events {


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
    public static class Restart {
    }
    
    
    public static class ConceptNew extends ParametricInferenceEvent<Concept> {
        public ConceptNew(Concept c, long when) {
            super(c, when);
        }
        
        @Override public String toString() {
            return "Concept Created: " + object;
        }        
    }

    /** when remembered a previously forgotten concept from subconcepts, and after first intialized */
    public static class ConceptActive {    }

    /** when a concept leaves main memory and is either moved to subconcepts or ConceptDelete */
    public static class ConceptForget { }

    /** if a concept is completely removed from both main, and subconcepts (or if subconcepts has capacity 0) */
    @Deprecated public static class ConceptDelete { }
    
    abstract public static class ConceptBeliefAdd implements Reaction<Class> {
        
        abstract public void onBeliefAdd(Concept c, Task t, Object[] extra);
        
        @Override public void event(Class event, Object... args) {
            onBeliefAdd( (Concept)args[0], (Task)args[1], (Object[])args[2]);
        }
        
    }
    
    abstract public static class ConceptBeliefRemove implements Reaction<Class> {

        abstract public void onBeliefRemove(Concept c, Sentence removed, Task t, Object[] extra);
        
        @Override public void event(Class event, Object... args) {
            onBeliefRemove( (Concept)args[0], (Sentence)args[1], (Task)args[2], (Object[])args[3]);
        }        

    }
    
    public static class ConceptGoalAdd { }
    public static class ConceptGoalRemove { }
    public static class ConceptQuestionAdd { } //applied for Quests too
    public static class ConceptQuestionRemove { } //applied for Quests too

    
    //Executive & Planning
    //public static class UnexecutableGoal {   }
    public static class UnexecutableOperation {   }
    public static class NewTaskExecution {    }
    public static class InduceSucceedingEvent {    }
    

    /*
    public static class TermLinkAdd { }
    public static class TermLinkRemove { }
    public static class TaskLinkAdd { }
    public static class TaskLinkRemove { }
    */
    
    public static class Answer { }

    
    /** fired at the START of a ConceptFire task */
    abstract public static class ConceptProcessed implements Reaction<Class> {
        
        /**
         * use:
         * Concept n.getCurrentConcept()
         * TaskLink n.getCurrentTaskLink()
         */
        abstract public void onFire(Premise n);
        
        @Override public void event(Class event, Object... args) {
            onFire((Premise)args[0]);
        }
        
    }

    public static class TermLinkTransformed {    }

    /** emitted after a TermLink has been selected and reasoned */
    public static class TermLinkSelected { }

    public static class BeliefSelect { }
    
    /** called from RuleTables.rule for a given Belief */
    public static class BeliefReason {    }
    
    public static class ConceptUnification { } //2nd level unification in CompositionalRules

    public static class TaskRemove { }

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
