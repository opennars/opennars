package nars.event;

import nars.AbstractMemory;
import nars.Events;
import nars.Memory;
import nars.NAR;
import nars.concept.Concept;
import nars.task.Task;
import nars.util.event.Observed;

public abstract class MemoryReaction extends ConceptReaction {

    public static final Class[] memoryEvents = new Class[]{
            /*CycleStart.class,
            CycleEnd.class,*/
            //Events.ConceptNew.class,
            //Events.ConceptProcessed.class,

            //Events.ConceptUnification.class,
            Events.PluginsChange.class,
            Events.OUT.class,
            //Events.TaskRemove.class,
            Events.TaskDerive.class,
            Events.TermLinkTransformed.class,
            //Events.UnExecutedGoal.class,

            //Output.OUT.class,

            Events.Restart.class};
    private final AbstractMemory memory;
    private final Observed.DefaultObserved.DefaultObservableRegistration taskRemoved;
    private final Observed.DefaultObserved.DefaultObservableRegistration cycleStart;
    private final Observed.DefaultObserved.DefaultObservableRegistration cycleEnd;

    public MemoryReaction(NAR n, boolean active) {
        this(n.memory, active);
    }

    public MemoryReaction(Memory m, boolean active) {
        super(m, active, memoryEvents);
        this.memory = m;

        taskRemoved = m.eventTaskRemoved.on(t -> {
            onTaskRemove(t);
        });

        cycleStart = m.eventCycleStart.on(c -> {
            onCycleStart(memory.time());
        });
        cycleEnd = m.eventCycleEnd.on(c -> {
            onCycleEnd(memory.time());
        });



        /*conceptProcessed = m.eventConceptProcessed.on(t-> {

        });*/
    }

    @Override
    public void event(final Class event, final Object[] arguments) {
        if (event == Events.OUT.class) {
            output(event, arguments[0].toString());
        } else if (event == Events.Restart.class) {
            output(event);        
        } else if (event == Events.OUT.class) {
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
    abstract public void output(Object channel, Object... args);

    public void output(String s) {
        output(String.class, s);
    }

    /**
     * when a concept is instantiated
     */
    abstract public void onConceptActive(Concept concept);

    public void onConceptForget(Concept concept) {

    }

    /**
     * called at the beginning of each logic clock cycle
     */
    abstract public void onCycleStart(long clock);

    /**
     * called at the end of each logic clock cycle
     */
    abstract public void onCycleEnd(long clock);

    abstract public void onTaskAdd(Task task);

    abstract public void onTaskRemove(Task task);

}
