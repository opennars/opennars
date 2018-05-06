/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package org.opennars.plugin.misc;

import org.opennars.main.Nar;
import org.opennars.main.Parameters;
import org.opennars.plugin.Plugin;

/**
 *
 * @author tc
 */
public class RuntimeNARSettings implements Plugin {

    Nar n=null;
    @Override
    public boolean setEnabled(final Nar n, final boolean enabled) {
        this.n=n;
        return true;
    }

    public boolean isImmediateEternalization() {
        return Parameters.IMMEDIATE_ETERNALIZATION;
    }
    public void setImmediateEternalization(final boolean val) {
        Parameters.IMMEDIATE_ETERNALIZATION=val;
    }
    
    public double getDerivationPriorityLeak() {
        return Parameters.DERIVATION_PRIORITY_LEAK;
    }
    public void setDerivationPriorityLeak(final double val) {
        Parameters.DERIVATION_PRIORITY_LEAK=(float) val;
    }
    
    public double getDerivationDurabilityLeak() {
        return Parameters.DERIVATION_DURABILITY_LEAK;
    }
    public void setDerivationDurabilityLeak(final double val) {
        Parameters.DERIVATION_DURABILITY_LEAK=(float) val;
    }

    
    public double getEvidentalHorizon() {
        return Parameters.HORIZON;
    }
    public void setEvidentalHorizon(final double val) {
        Parameters.HORIZON=(float) val;
    }
    
    public double getCuriosityDesireConfidenceMul() {
        return Parameters.CURIOSITY_DESIRE_CONFIDENCE_MUL;
    }
    public void setCuriosityDesireConfidenceMul(final double val) {
        Parameters.CURIOSITY_DESIRE_CONFIDENCE_MUL=(float) val;
    }
    
    public double getCuriosityDesirePriorityMul() {
        return Parameters.CURIOSITY_DESIRE_PRIORITY_MUL;
    }
    public void setCuriosityDesirePriorityMul(final double val) {
        Parameters.CURIOSITY_DESIRE_PRIORITY_MUL=(float)val;
    }
    
    public double getCuriosityDesireDurabilityMul() {
        return Parameters.CURIOSITY_DESIRE_DURABILITY_MUL;
    }
    public void setCuriosityDesireDurabilityMul(final double val) {
        Parameters.CURIOSITY_DESIRE_DURABILITY_MUL=(float) val;
    }
    
    public double getCuriosityBusinessThreshold() {
        return Parameters.CURIOSITY_BUSINESS_THRESHOLD;
    }
    public void setCuriosityBusinessThreshold(final double val) {
        Parameters.CURIOSITY_BUSINESS_THRESHOLD=(float) val;
    }
    
    public boolean isCuriosityForOperatorOnly() {
        return Parameters.CURIOSITY_FOR_OPERATOR_ONLY;
    }
    public void setCuriosityForOperatorOnly(final boolean val) {
        Parameters.CURIOSITY_FOR_OPERATOR_ONLY=val;
    }
    
    
    public double getHappyEventHigherThreshold() {
        return Parameters.HAPPY_EVENT_HIGHER_THRESHOLD;
    }
    public void setHappyEventHigherThreshold(final double val) {
        Parameters.HAPPY_EVENT_HIGHER_THRESHOLD=(float) val;
    }
    
    public double getHappyEventLowerThreshold() {
        return Parameters.HAPPY_EVENT_LOWER_THRESHOLD;
    }
    public void setHappyEventLowerThreshold(final double val) {
        Parameters.HAPPY_EVENT_LOWER_THRESHOLD=(float) val;
    }
    
    public double getBusyEventHigherThreshold() {
        return Parameters.BUSY_EVENT_HIGHER_THRESHOLD;
    }
    public void setBusyEventHigherThreshold(final double val) {
        Parameters.BUSY_EVENT_HIGHER_THRESHOLD=(float) val;
    }
    
   public double getBusyEventLowerThreshold() {
        return Parameters.BUSY_EVENT_LOWER_THRESHOLD;
    }
    public void setBusyEventLowerThreshold(final double val) {
        Parameters.BUSY_EVENT_LOWER_THRESHOLD=(float) val;
    }
    
    public boolean isReflectMetaHappyGoal() {
        return Parameters.REFLECT_META_HAPPY_GOAL;
    }
    public void setReflectMetaHappyGoal(final boolean val) {
        Parameters.REFLECT_META_HAPPY_GOAL=val;
    }
    
    public boolean isUsingConsiderRemind() {
        return Parameters.CONSIDER_REMIND;
    }
    public void setUsingConsiderRemind(final boolean val) {
        Parameters.CONSIDER_REMIND=val;
    }
    
    public boolean isQuestionGenerationOnDecisionMaking() {
        return Parameters.QUESTION_GENERATION_ON_DECISION_MAKING;
    }
    public void setQuestionGenerationOnDecisionMaking(final boolean val) {
        Parameters.QUESTION_GENERATION_ON_DECISION_MAKING=val;
    }
    
    public boolean isDecisionQuestionGen() {
        return Parameters.QUESTION_GENERATION_ON_DECISION_MAKING;
    }
    public void setDecisionQuestionGen(final boolean val) {
        Parameters.QUESTION_GENERATION_ON_DECISION_MAKING=val;
    }
    
    public boolean isHowQuestionGenerationOnDecisionMaking() {
        return Parameters.HOW_QUESTION_GENERATION_ON_DECISION_MAKING;
    }
    public void setHowQuestionGenerationOnDecisionMaking(final boolean val) {
        Parameters.HOW_QUESTION_GENERATION_ON_DECISION_MAKING=val;
    }
    
    public boolean isCuriosityAlsoOnLowConfidentHighPriorityBelief() {
        return Parameters.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF;
    }
    public void setCuriosityAlsoOnLowConfidentHighPriorityBelief(final boolean val) {
        Parameters.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF=val;
    }
    
    public double getCuriosityPriorityThreshold() {
        return Parameters.CURIOSITY_PRIORITY_THRESHOLD;
    }
    public void setCuriosityPriorityThreshold(final double val) {
        Parameters.CURIOSITY_PRIORITY_THRESHOLD=(float) val;
    }
    
    public double getCuriosityConfidenceThreshold() {
        return Parameters.CURIOSITY_CONFIDENCE_THRESHOLD;
    }
    public void setCuriosityConfidenceThreshold(final double val) {
        Parameters.CURIOSITY_CONFIDENCE_THRESHOLD=(float) val;
    }
    
    public double getAnticipationConfidence() {
        return Parameters.ANTICIPATION_CONFIDENCE;
    }
    public void setAnticipationConfidence(final double val) {
        Parameters.ANTICIPATION_CONFIDENCE=(float) val;
    }
    
    public double getSatisfactionThreshold() {
        return Parameters.SATISFACTION_TRESHOLD;
    }
    public void setSatisfactionThreshold(final double val) {
        Parameters.SATISFACTION_TRESHOLD=(float) val;
    }
}
