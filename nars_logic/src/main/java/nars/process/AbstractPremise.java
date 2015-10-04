package nars.process;

import nars.NAR;
import nars.premise.Premise;
import nars.task.Task;

/**
 * Base class for premises
 */
abstract public class AbstractPremise implements Premise {

    public final NAR nar;

    public AbstractPremise(NAR m) {
        this.nar = m;
    }

    public NAR nar() {
        return nar;
    }

    @Override public void updateBelief(Task nextBelief) {
        //ignore
    }


}
