/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.op.mental;

import nars.Global;
import nars.NAR;
import nars.util.event.AbstractReaction;

/**
 *
 * @author tc
 */
public class RuntimeNARSettings extends AbstractReaction {

    private final NAR n;

    public RuntimeNARSettings(NAR x) {
        this.n = x;
    }

    public boolean isImmediateEternalization() {
        return Global.IMMEDIATE_ETERNALIZATION;
    }
    public void setImmediateEternalization(boolean val) {
        Global.IMMEDIATE_ETERNALIZATION=val;
    }
    
    public double getDuration() {
        return n.param.duration.get();
    }
    public void setDuration(double val) {
        n.param.duration.set((int) val);
    }
    
    public double getTemporalInductionPriority() {
        return Global.TEMPORAL_INDUCTION_CHAIN_SAMPLES;
    }
    public void setTemporalInductionPriority(double val) {
        Global.TEMPORAL_INDUCTION_CHAIN_SAMPLES=(int) val;
    }
    
    public double getEvidentalHorizon() {
        return Global.HORIZON;
    }
    public void setEvidentalHorizon(double val) {
        Global.HORIZON=(float) val;
    }

    @Override
    public void event(Class event, Object... args) {

    }
}
