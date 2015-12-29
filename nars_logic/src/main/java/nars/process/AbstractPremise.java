package nars.process;

import nars.NAR;
import nars.Premise;
import nars.task.Task;

/**
 * Base class for premises
 */
public abstract class AbstractPremise implements Premise {

    public final NAR nar;

    protected AbstractPremise(NAR m) {
        nar = m;
    }

    @Override
    public NAR nar() {
        return nar;
    }

    @Override public void updateBelief(Task nextBelief) {
        //ignore
    }


}
