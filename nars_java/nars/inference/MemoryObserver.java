package nars.inference;

import nars.util.Events;
import nars.util.Events.CycleEnd;
import nars.util.Events.CycleStart;
import nars.core.Memory;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Task;
import nars.io.Output;

public abstract class MemoryObserver extends AbstractObserver {

    private final Memory memory;

    public MemoryObserver(NAR n, boolean active) {
        this(n.memory, active);
    }

    public MemoryObserver(Memory m, boolean active) {
        super(m.event, active,
                Events.CycleStart.class,
                Events.CycleEnd.class,
                Events.ConceptNew.class,
                Events.ConceptForget.class,
                Events.ConceptBeliefAdd.class,
                Events.ConceptBeliefRemove.class,
                Events.ConceptDirectProcessedTask.class,
                Events.ConceptFire.class,
                Events.ConceptGoalAdd.class,
                Events.ConceptGoalRemove.class,
                Events.ConceptQuestionAdd.class,
                Events.ConceptQuestionRemove.class,
                Events.ConceptUnification.class,
                Events.BeliefSelect.class,
                Events.PluginsChange.class,
                Events.TaskAdd.class,
                Events.TaskRemove.class,
                Events.TaskDerive.class,
                Events.TaskImmediateProcess.class,
                Events.TermLinkSelect.class,
                //Events.UnExecutedGoal.class,

                //Output.OUT.class, 

                Events.ResetEnd.class);
        this.memory = m;
    }

    @Override
    public void event(final Class event, final Object[] arguments) {
        if (event == Events.ConceptNew.class) {
            onConceptAdd((Concept) arguments[0]);
        } else if (event == Output.OUT.class) {
            output(event, arguments[0].toString());
        } else if (event == Events.ResetEnd.class) {
            output(event);        
        } else if (event == CycleStart.class) {
            onCycleStart(memory.time());
        } else if (event == CycleEnd.class) {
            onCycleEnd(memory.time());
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
     *
     * @param s The line to be displayed
     */
    abstract public void output(Class channel, Object... args);

    public void output(String s) {
        output(String.class, s);
    }

    /**
     * when a concept is instantiated
     */
    abstract public void onConceptAdd(Concept concept);

    /**
     * called at the beginning of each inference clock cycle
     */
    abstract public void onCycleStart(long clock);

    /**
     * called at the end of each inference clock cycle
     */
    abstract public void onCycleEnd(long clock);

    /**
     * Added task
     */
    abstract public void onTaskAdd(Task task, String reason);

    /**
     * Neglected task
     */
    abstract public void onTaskRemove(Task task, String reason);

}
