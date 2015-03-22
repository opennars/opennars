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
    
    public double getCuriosityDesireConfidence() {
        return Parameters.CURIOSITY_DESIRE_CONFIDENCE;
    }
    public void setCuriosityDesireConfidence(double val) {
        Parameters.CURIOSITY_DESIRE_CONFIDENCE=(float) val;
    }
    
    public double getCuriosityDesirePriority() {
        return Parameters.CURIOSITY_DESIRE_PRIORITY_MUL;
    }
    public void setCuriosityDesirePriority(double val) {
        Parameters.CURIOSITY_DESIRE_PRIORITY_MUL=(float)val;
    }
    
    public double getCuriosityDesireDurability() {
        return Parameters.CURIOSITY_DESIRE_DURABILITY_MUL;
    }
    public void setCuriosityDesireDurability(double val) {
        Parameters.CURIOSITY_DESIRE_DURABILITY_MUL=(float) val;
    }
    
    public boolean isCuriosityForOperatorOnly() {
        return Parameters.CURIOSITY_FOR_OPERATOR_ONLY;
    }
    public void setCuriosityForOperatorOnly(boolean val) {
        Parameters.CURIOSITY_FOR_OPERATOR_ONLY=val;
    }
    
    
    public double getHappyEventHigherThreshold() {
        return Parameters.HAPPY_EVENT_HIGHER_THRESHOLD;
    }
    public void setHappyEventHigherThreshold(double val) {
        Parameters.HAPPY_EVENT_HIGHER_THRESHOLD=(float) val;
    }
    
    public double getHappyEventLowerThreshold() {
        return Parameters.HAPPY_EVENT_LOWER_THRESHOLD;
    }
    public void setHappyEventLowerThreshold(double val) {
        Parameters.HAPPY_EVENT_LOWER_THRESHOLD=(float) val;
    }
    
    /*  public double getBusyEventHigherThreshold() {
        return Parameters.BUSY_EVENT_HIGHER_THRESHOLD;
    }
    public void setBusyEventHigherThreshold(double val) {
        Parameters.BUSY_EVENT_HIGHER_THRESHOLD=(float) val;
    }
    
   public double getBusyEventLowerThreshold() {
        return Parameters.BUSY_EVENT_LOWER_THRESHOLD;
    }
    public void setBusyEventLowerThreshold(double val) {
        Parameters.BUSY_EVENT_LOWER_THRESHOLD=(float) val;
    }*/
    
    public boolean isReflectMetaHappyGoal() {
        return Parameters.REFLECT_META_HAPPY_GOAL;
    }
    public void setReflectMetaHappyGoal(boolean val) {
        Parameters.REFLECT_META_HAPPY_GOAL=val;
    }
}
