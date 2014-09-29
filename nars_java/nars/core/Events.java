package nars.core;

/** empty event classes for use with EventEmitter */
public class Events {

    public static EventEmitter newEventEmitter() {
        //TODO use reflection to get all subclasses
        return new EventEmitter(
                FrameStart.class,
                FrameEnd.class,
                CycleStart.class,
                CycleEnd.class,
                WorkCycleStart.class,
                WorkCycleEnd.class,
                ResetStart.class,
                ResetEnd.class,
                ConceptAdd.class,
                ConceptRemove.class,
                ConceptBeliefAdd.class,
                ConceptBeliefRemove.class,
                ConceptGoalAdd.class,
                ConceptGoalRemove.class,
                ConceptQuestionAdd.class,
                ConceptQuestionRemove.class,
                TaskDerived.class,
                PluginsChange.class                
        );
    }

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
    
    public static class ConceptAdd { }
    public static class ConceptRemove { }
    public static class ConceptBeliefAdd { }
    public static class ConceptBeliefRemove { }
    public static class ConceptGoalAdd { }
    public static class ConceptGoalRemove { }
    public static class ConceptQuestionAdd { }
    public static class ConceptQuestionRemove { }

    public static class TaskDerived {    }

    public static class PluginsChange {    }

    public static class UnExecutedGoal {    }
}
