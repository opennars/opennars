package nars.process;

import nars.NAR;
import nars.Premise;
import nars.task.Task;

import java.util.Collection;

/**
 * Base class for premises
 */
public abstract class AbstractPremise implements Premise {

    /** derivation queue (this might also work as a Set) */
    protected Collection<Task> derived = null;

    public final NAR nar;

    public AbstractPremise(NAR m) {
        this.nar = m;
    }

    @Override
    public NAR nar() {
        return nar;
    }

    @Override public void updateBelief(Task nextBelief) {
        //ignore
    }


}
