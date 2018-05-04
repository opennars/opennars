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

import org.opennars.plugin.Plugin;
import org.opennars.main.NAR;
import org.opennars.main.Parameters;

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

    
    public double getEvidentalHorizon() {
        return Parameters.HORIZON;
    }
    public void setEvidentalHorizon(double val) {
        Parameters.HORIZON=(float) val;
    }
    
    public double getCuriosityDesireConfidenceMul() {
        return Parameters.CURIOSITY_DESIRE_CONFIDENCE_MUL;
    }
    public void setCuriosityDesireConfidenceMul(double val) {
        Parameters.CURIOSITY_DESIRE_CONFIDENCE_MUL=(float) val;
    }
    
    public double getCuriosityDesirePriorityMul() {
        return Parameters.CURIOSITY_DESIRE_PRIORITY_MUL;
    }
    public void setCuriosityDesirePriorityMul(double val) {
        Parameters.CURIOSITY_DESIRE_PRIORITY_MUL=(float)val;
    }
    
    public double getCuriosityDesireDurabilityMul() {
        return Parameters.CURIOSITY_DESIRE_DURABILITY_MUL;
    }
    public void setCuriosityDesireDurabilityMul(double val) {
        Parameters.CURIOSITY_DESIRE_DURABILITY_MUL=(float) val;
    }
    
    public double getCuriosityBusinessThreshold() {
        return Parameters.CURIOSITY_BUSINESS_THRESHOLD;
    }
    public void setCuriosityBusinessThreshold(double val) {
        Parameters.CURIOSITY_BUSINESS_THRESHOLD=(float) val;
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
    
    public double getBusyEventHigherThreshold() {
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
    }
    
    public boolean isReflectMetaHappyGoal() {
        return Parameters.REFLECT_META_HAPPY_GOAL;
    }
    public void setReflectMetaHappyGoal(boolean val) {
        Parameters.REFLECT_META_HAPPY_GOAL=val;
    }
    
    public boolean isUsingConsiderRemind() {
        return Parameters.CONSIDER_REMIND;
    }
    public void setUsingConsiderRemind(boolean val) {
        Parameters.CONSIDER_REMIND=val;
    }
    
    public boolean isQuestionGenerationOnDecisionMaking() {
        return Parameters.QUESTION_GENERATION_ON_DECISION_MAKING;
    }
    public void setQuestionGenerationOnDecisionMaking(boolean val) {
        Parameters.QUESTION_GENERATION_ON_DECISION_MAKING=val;
    }
    
    public boolean isDecisionQuestionGen() {
        return Parameters.QUESTION_GENERATION_ON_DECISION_MAKING;
    }
    public void setDecisionQuestionGen(boolean val) {
        Parameters.QUESTION_GENERATION_ON_DECISION_MAKING=val;
    }
    
    public boolean isHowQuestionGenerationOnDecisionMaking() {
        return Parameters.HOW_QUESTION_GENERATION_ON_DECISION_MAKING;
    }
    public void setHowQuestionGenerationOnDecisionMaking(boolean val) {
        Parameters.HOW_QUESTION_GENERATION_ON_DECISION_MAKING=val;
    }
    
    public boolean isCuriosityAlsoOnLowConfidentHighPriorityBelief() {
        return Parameters.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF;
    }
    public void setCuriosityAlsoOnLowConfidentHighPriorityBelief(boolean val) {
        Parameters.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF=val;
    }
    
    public double getCuriosityPriorityThreshold() {
        return Parameters.CURIOSITY_PRIORITY_THRESHOLD;
    }
    public void setCuriosityPriorityThreshold(double val) {
        Parameters.CURIOSITY_PRIORITY_THRESHOLD=(float) val;
    }
    
    public double getCuriosityConfidenceThreshold() {
        return Parameters.CURIOSITY_CONFIDENCE_THRESHOLD;
    }
    public void setCuriosityConfidenceThreshold(double val) {
        Parameters.CURIOSITY_CONFIDENCE_THRESHOLD=(float) val;
    }
    
    public double getAnticipationConfidence() {
        return Parameters.ANTICIPATION_CONFIDENCE;
    }
    public void setAnticipationConfidence(double val) {
        Parameters.ANTICIPATION_CONFIDENCE=(float) val;
    }
    
    public double getSatisfactionThreshold() {
        return Parameters.SATISFACTION_TRESHOLD;
    }
    public void setSatisfactionThreshold(double val) {
        Parameters.SATISFACTION_TRESHOLD=(float) val;
    }
}
