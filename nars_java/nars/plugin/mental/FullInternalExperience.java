package nars.plugin.mental;

import nars.core.NAR;
import nars.core.Parameters;
import nars.core.Plugin;

/**
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 * @param task The task processed
 */
public class FullInternalExperience implements Plugin {

    @Override public boolean setEnabled(NAR n, boolean enabled) {
        MinimalInternalExperience exp=new MinimalInternalExperience();
        exp.setEnabled(n, enabled);
        Parameters.INTERNAL_EXPERIENCE_FULL=enabled;
        return true;
    }
    
}