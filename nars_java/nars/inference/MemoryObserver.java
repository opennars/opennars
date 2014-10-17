package nars.inference;

import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.Events.ConceptAdd;
import nars.core.Events.ConceptRemove;
import nars.core.Events.ResetEnd;
import nars.core.Memory;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Task;
import nars.io.Output;
import nars.io.Output.OUT;


public abstract class MemoryObserver extends AbstractObserver {
    private final Memory memory;
    

    public MemoryObserver(NAR n, boolean active) {
        this(n.memory, active);
    }

    
    public MemoryObserver(Memory m, boolean active) {
        super(m.event, active, Events.ConceptAdd.class, Events.ConceptRemove.class, Output.OUT.class, Events.ResetEnd.class);
        this.memory = m;
    }
    
    @Override
    public void event(final Class event, final Object[] arguments) {
        if (event == Events.ConceptAdd.class) {
            onConceptNew((Concept) arguments[0]);
        } else if (event == Output.OUT.class) {
            output(event, arguments[0].toString());
        } else if (event == Events.ResetEnd.class) {
            output(event);
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

    /** when a concept is instantiated */
    abstract public void onConceptNew(Concept concept);

    /** called at the beginning of each inference clock cycle */
    abstract public void onCycleStart(long clock);

    /** called at the end of each inference clock cycle */
    abstract public void onCycleEnd(long clock);

    /** Added task */
    abstract public void onTaskAdd(Task task, String reason);

    /** Neglected task */
    abstract public void onTaskRemove(Task task, String reason);

}