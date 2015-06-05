package nars.op.mental;

import nars.Events;
import nars.NAR;
import nars.nal.process.TaskProcess;

/**
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 * @param task The task processed
 */
public class FullInternalExperience extends InternalExperience {

    public FullInternalExperience(NAR n) {
        super(n, TaskProcess.class, Events.BeliefReason.class);
    }


    @Override
    public boolean isFull() {
        return true;
    }

}