package nars.nal;

import nars.Events;
import nars.Events.CycleEnd;
import nars.Events.CycleStart;
import nars.Memory;
import nars.NAR;
import nars.event.NARReaction;
import nars.nal.concept.Concept;

public abstract class MemoryObserver extends NARReaction {

    private final Memory memory;

    public MemoryObserver(NAR n, boolean active) {
        this(n.memory, active);
    }

    public MemoryObserver(Memory m, boolean active) {
        super(m.event, active,
                Events.CycleStart.class,
                Events.CycleEnd.class,
                //Events.ConceptNew.class,
                Events.ConceptForget.class,
                Events.ConceptBeliefAdd.class,
                Events.ConceptBeliefRemove.class,
                Events.ConceptProcessed.class,
                Events.ConceptGoalAdd.class,
                Events.ConceptGoalRemove.class,
                Events.ConceptQuestionAdd.class,
                Events.ConceptQuestionRemove.class,
                Events.ConceptUnification.class,
                Events.ConceptActive.class,
                Events.PluginsChange.class,
                Events.TaskAdd.class,
                Events.TaskRemove.class,
                Events.TaskDerive.class,
                DirectProcess.class,
                Events.TermLinkSelected.class,
                Events.TermLinkTransformed.class,
                //Events.UnExecutedGoal.class,

                //Output.OUT.class, 

                Events.Restart.class);
        this.memory = m;
    }

    @Override
    public void event(final Class event, final Object[] arguments) {
        if (event == Events.ConceptActive.class) {
            onConceptActive((Concept) arguments[0]);
        } else if (event == Events.OUT.class) {
            output(event, arguments[0].toString());
        } else if (event == Events.Restart.class) {
            output(event);        
        } else if (event == CycleStart.class) {
            onCycleStart(memory.time());
        } else if (event == CycleEnd.class) {
            onCycleEnd(memory.time());
        } else if (event == Events.TaskAdd.class) {
            onTaskAdd((Task)arguments[0]);
        } else {
            output(event, arguments);
        }

        //cycle start
        //cycle end
        //task add
        //task remove
    }

    /**
     * Add new text to display
     */
    abstract public void output(Class channel, Object... args);

    public void output(String s) {
        output(String.class, s);
    }

    /**
     * when a concept is instantiated
     */
    abstract public void onConceptActive(Concept concept);

    /**
     * called at the beginning of each logic clock cycle
     */
    abstract public void onCycleStart(long clock);

    /**
     * called at the end of each logic clock cycle
     */
    abstract public void onCycleEnd(long clock);

    abstract public void onTaskAdd(Task task);

    abstract public void onTaskRemove(Task task, String reason);

}
