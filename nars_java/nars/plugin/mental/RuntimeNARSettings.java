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
    
    public double getDuration() {
        return n.param.duration.get();
    }
    public void setDuration(double val) {
        n.param.duration.set((int) val);
    }
    
    public double getDerivationPriorityLeak() {
        return Parameters.DERIVATION_PRIORITY_LEAK;
    }
    public void setDerivationPriorityLeak(double val) {
        Parameters.DERIVATION_PRIORITY_LEAK=(float) val;
    }
    
    public double getDerivationDurabilityLeak() {
        return Parameters.DERIVATION_DURABILITY_LEAK;
    }
    public void setDerivationDurabilityLeak(double val) {
        Parameters.DERIVATION_DURABILITY_LEAK=(float) val;
    }
    
    public double getTemporalInductionPriority() {
        return Parameters.TEMPORAL_INDUCTION_CHAIN_SAMPLES;
    }
    public void setTemporalInductionPriority(double val) {
        Parameters.TEMPORAL_INDUCTION_CHAIN_SAMPLES=(int) val;
    }
    
    public double getEvidentalHorizon() {
        return Parameters.HORIZON;
    }
    public void setEvidentalHorizon(double val) {
        Parameters.HORIZON=(float) val;
    }
    
    public boolean isInductionOnSucceedingEvents() {
        return Parameters.TEMPORAL_INDUCTION_ON_SUCCEEDING_EVENTS;
    }
    public void setInductionOnSucceedingEvents(boolean val) {
        Parameters.TEMPORAL_INDUCTION_ON_SUCCEEDING_EVENTS=val;
    }
    
    public double getInductionChainSamples() {
        return Parameters.TEMPORAL_INDUCTION_CHAIN_SAMPLES;
    }
    public void setInductionChainSamples(double val) {
        Parameters.TEMPORAL_INDUCTION_CHAIN_SAMPLES=(int) val;
    }
    
    public double getInductionSamples() {
        return Parameters.TEMPORAL_INDUCTION_SAMPLES;
    }
    public void setInductionSamples(double val) {
        Parameters.TEMPORAL_INDUCTION_SAMPLES=(int) val;
    }
}
