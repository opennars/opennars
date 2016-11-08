package nars.plugin.mental;

import nars.plugin.mental.InternalExperience;
import nars.plugin.mental.InternalExperience;

/**
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 * @param task The task processed
 */
public class FullInternalExperience extends InternalExperience {

    @Override
    public boolean isFull() {
        return true;
    }

}