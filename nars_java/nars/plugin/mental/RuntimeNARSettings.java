/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.plugin.mental;

import nars.core.EventEmitter;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.Plugin;

/**
 *
 * @author tc
 */
public class RuntimeNARSettings implements Plugin {

    NAR n=null;
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        this.n=n;
        return true;
    }

    public boolean isImmediateEternalization() {
        return Parameters.IMMEDIATE_ETERNALIZATION;
    }
    public void setImmediateEternalization(boolean val) {
        Parameters.IMMEDIATE_ETERNALIZATION=val;
    }
    
    public int getDuration() {
        return n.param.duration.get();
    }
    public void setDuration(int val) {
        n.param.duration.set(val);
    }
    
    public int getTemporalInductionPriority() {
        return Parameters.TEMPORAL_INDUCTION_CHAIN_SAMPLES;
    }
    public void setTemporalInductionPriority(int val) {
        Parameters.TEMPORAL_INDUCTION_CHAIN_SAMPLES=val;
    }
    
    public float getEvidentalHorizon() {
        return Parameters.HORIZON;
    }
    public void setEvidentalHorizon(float val) {
        Parameters.HORIZON=val;
    }
    
}
