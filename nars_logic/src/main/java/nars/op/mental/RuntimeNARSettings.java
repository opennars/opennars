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

    public double getDerivationPriorityLeak() {
        return Global.DERIVATION_PRIORITY_LEAK;
    }
    public void setDerivationPriorityLeak(double val) {
        Global.DERIVATION_PRIORITY_LEAK=(float) val;
    }
    
    public double getDerivationDurabilityLeak() {
        return Global.DERIVATION_DURABILITY_LEAK;
    }
    public void setDerivationDurabilityLeak(double val) {
        Global.DERIVATION_DURABILITY_LEAK=(float) val;
    }
    
        public boolean isInductionOnSucceedingEvents() {
        return Global.TEMPORAL_INDUCTION_ON_SUCCEEDING_EVENTS;
    }
    public void setInductionOnSucceedingEvents(boolean val) {
        Global.TEMPORAL_INDUCTION_ON_SUCCEEDING_EVENTS=val;
    }
    
    public double getInductionChainSamples() {
        return Global.TEMPORAL_INDUCTION_CHAIN_SAMPLES;
    }
    public void setInductionChainSamples(double val) {
        Global.TEMPORAL_INDUCTION_CHAIN_SAMPLES=(int) val;
    }
    
    public double getInductionSamples() {
        return Global.TEMPORAL_INDUCTION_SAMPLES;
    }
    public void setInductionSamples(double val) {
        Global.TEMPORAL_INDUCTION_SAMPLES=(int) val;
    }
    
    public double getCuriosityDesireConfidenceMul() {
        return Global.CURIOSITY_DESIRE_CONFIDENCE_MUL;
    }
    public void setCuriosityDesireConfidenceMul(double val) {
        Global.CURIOSITY_DESIRE_CONFIDENCE_MUL=(float) val;
    }
    
    public double getCuriosityDesirePriorityMul() {
        return Global.CURIOSITY_DESIRE_PRIORITY_MUL;
    }
    public void setCuriosityDesirePriorityMul(double val) {
        Global.CURIOSITY_DESIRE_PRIORITY_MUL=(float)val;
    }
    
    public double getCuriosityDesireDurabilityMul() {
        return Global.CURIOSITY_DESIRE_DURABILITY_MUL;
    }
    public void setCuriosityDesireDurabilityMul(double val) {
        Global.CURIOSITY_DESIRE_DURABILITY_MUL=(float) val;
    }
    
    public double getCuriosityBusinessThreshold() {
        return Global.CURIOSITY_BUSINESS_THRESHOLD;
    }
    public void setCuriosityBusinessThreshold(double val) {
        Global.CURIOSITY_BUSINESS_THRESHOLD=(float) val;
    }
    
    public boolean isCuriosityForOperatorOnly() {
        return Global.CURIOSITY_FOR_OPERATOR_ONLY;
    }
    public void setCuriosityForOperatorOnly(boolean val) {
        Global.CURIOSITY_FOR_OPERATOR_ONLY=val;
    }
    
    
    public double getHappyEventHigherThreshold() {
        return Global.HAPPY_EVENT_HIGHER_THRESHOLD;
    }
    public void setHappyEventHigherThreshold(double val) {
        Global.HAPPY_EVENT_HIGHER_THRESHOLD=(float) val;
    }
    
    public double getHappyEventLowerThreshold() {
        return Global.HAPPY_EVENT_LOWER_THRESHOLD;
    }
    public void setHappyEventLowerThreshold(double val) {
        Global.HAPPY_EVENT_LOWER_THRESHOLD=(float) val;
    }
    
    public double getBusyEventHigherThreshold() {
        return Global.BUSY_EVENT_HIGHER_THRESHOLD;
    }
    public void setBusyEventHigherThreshold(double val) {
        Global.BUSY_EVENT_HIGHER_THRESHOLD=(float) val;
    }
    
   public double getBusyEventLowerThreshold() {
        return Global.BUSY_EVENT_LOWER_THRESHOLD;
    }
    public void setBusyEventLowerThreshold(double val) {
        Global.BUSY_EVENT_LOWER_THRESHOLD=(float) val;
    }
    
    public boolean isReflectMetaHappyGoal() {
        return Global.REFLECT_META_HAPPY_GOAL;
    }
    public void setReflectMetaHappyGoal(boolean val) {
        Global.REFLECT_META_HAPPY_GOAL=val;
    }
    
    public boolean isUsingConsiderRemind() {
        return Global.CONSIDER_REMIND;
    }
    public void setUsingConsiderRemind(boolean val) {
        Global.CONSIDER_REMIND=val;
    }
    
    public boolean isQuestionGenerationOnDecisionMaking() {
        return Global.QUESTION_GENERATION_ON_DECISION_MAKING;
    }
    public void setQuestionGenerationOnDecisionMaking(boolean val) {
        Global.QUESTION_GENERATION_ON_DECISION_MAKING=val;
    }
    
    public boolean isHowQuestionGenerationOnDecisionMaking() {
        return Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING;
    }
    public void setHowQuestionGenerationOnDecisionMaking(boolean val) {
        Global.HOW_QUESTION_GENERATION_ON_DECISION_MAKING=val;
    }
    
    public boolean isCuriosityAlsoOnLowConfidentHighPriorityBelief() {
        return Global.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF;
    }
    public void setCuriosityAlsoOnLowConfidentHighPriorityBelief(boolean val) {
        Global.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF=val;
    }
    
    public double getCuriosityPriorityThreshold() {
        return Global.CURIOSITY_PRIORITY_THRESHOLD;
    }
    public void setCuriosityPriorityThreshold(double val) {
        Global.CURIOSITY_PRIORITY_THRESHOLD=(float) val;
    }
    
    public double getCuriosityConfidenceThreshold() {
        return Global.CURIOSITY_CONFIDENCE_THRESHOLD;
    }
    public void setCuriosityConfidenceThreshold(double val) {
        Global.CURIOSITY_CONFIDENCE_THRESHOLD=(float) val;
    }
    
    public double getAnticipationConfidence() {
        return Global.ANTICIPATION_CONFIDENCE;
    }
    public void setAnticipationConfidence(double val) {
        Global.ANTICIPATION_CONFIDENCE=(float) val;
    }
    
    public double getSatisfactionThreshold() {
        return Global.SATISFACTION_TRESHOLD;
    }
    public void setSatisfactionThreshold(double val) {
        Global.SATISFACTION_TRESHOLD=(float) val;
    }

    @Override
    public void event(Class event, Object... args) {

    }
}
