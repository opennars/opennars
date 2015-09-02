package nars.op.mental;

import nars.NAR;
import nars.process.TaskProcess;

/**
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 * @param task The task processed
 */
public class FullInternalExperience extends InternalExperience {

    public FullInternalExperience(NAR n) {
        super(n, TaskProcess.class);
    }


    @Override
    public boolean isFull() {
        return true;
    }

}