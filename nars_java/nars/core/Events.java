package nars.core;

/** empty event classes for use with EventEmitter */
public class Events {

    public static EventEmitter newEventEmitter() {
        //TODO use reflection to get all subclasses
        return new EventEmitter(
                CycleStart.class,
                CycleStop.class,
                WorkCycleStart.class,
                WorkCycleStop.class,
                ResetPre.class,
                ResetPost.class,
                ConceptAdd.class,
                ConceptRemove.class,
                ConceptBeliefAdd.class,
                ConceptBeliefRemove.class
        );
    }

    /** fired at the beginning of each main cycle */
    public static class CycleStart {
    }

    /** fired at the end of each main cycle */
    public static class CycleStop {
    }

    /** fired at the beginning of each individual Memory work cycle */
    public static class WorkCycleStart {
    }

    /** fired at the end of each Memory individual cycle */
    public static class WorkCycleStop {
    }

    /** called before memory.reset() proceeds */
    public static class ResetPre {
    }

    /** called after memory.reset() proceeds */
    public static class ResetPost {
    }
    
    public static class ConceptAdd { }
    public static class ConceptRemove { }
    public static class ConceptBeliefAdd { }
    public static class ConceptBeliefRemove { }
}
