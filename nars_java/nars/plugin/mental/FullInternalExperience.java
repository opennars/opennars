package nars.plugin.mental;

import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.Plugin;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Symbols;
import nars.language.Term;
import nars.operator.Operation;

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
        Parameters.INTERNAL_EXPERIENCE_FULL=true;
        return true;
    }
    
}