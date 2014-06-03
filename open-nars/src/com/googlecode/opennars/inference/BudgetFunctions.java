/*
 * BudgetFunctions.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.inference;

import com.googlecode.opennars.entity.*;
import com.googlecode.opennars.language.Term;
import com.googlecode.opennars.main.*;

/**
 * Budget functions controlling the resources allocation
 */
public class BudgetFunctions extends UtilityFunctions {    
    
	private Memory memory;
	
	public BudgetFunctions(Memory memory) {
		this.memory = memory;
	}
	
    /* ----------------------- Belief evaluation ----------------------- */
    
    /**
     * Determine the quality of a judgment by its truth value
     * <p>
     * Mainly decided by confidence, though binary judgment is also preferred
     * @param t The truth value of a judgment
     * @return The quality of the judgment, according to truth value only
     */
    public  float truthToQuality(TruthValue t) {
        float freq = t.getFrequency();
        float conf = t.getConfidence();
        return and(conf, Math.abs(freq - 0.5f) + freq * 0.5f);
    }

    /**
     * Determine the rank of a judgment by its confidence and originality (base length)
     * <p>
     * @param judg The judgment to be ranked
     * @return The rank of the judgment, according to truth value only
     */
    public  float rankBelief(Judgement judg) {
//        TruthValue truth = judg.getTruth();
//        float quality = truthToQuality(truth);
        float confidence = judg.getConfidence();
        float originality = 1.0f / (judg.getBase().length() + 1);
        return or(confidence, originality);
    }
    
    /* ----- Functions used both in direct and indirect processing of tasks ----- */

    /**
     * Evaluate the quality of a belief as a solution to a problem, then reward the belief and de-prioritize the problem
     * @param problem The problem (question or goal) to be solved
     * @param solution The belief as solution
     * @param task The task to be immediatedly processed, or null for continued process
     * @return The budget for the new task which is the belief activated, if necessary
     */
     BudgetValue solutionEval(Sentence problem, Judgement solution, Task task) {
        BudgetValue budget = null;
        boolean feedbackToLinks = false;
        if (task == null) {                 // called in continued processing
            task = memory.currentTask;
            feedbackToLinks = true;
        }
        boolean judgmentTask = task.getSentence().isJudgment();
        float quality;                      // the quality of the solution
        if (problem instanceof Question) 
            quality = solution.solutionQuality((Question) problem);
        else // problem is goal
            quality = solution.getTruth().getExpectation();
        if (judgmentTask)
            task.incPriority(quality);
        else {
            task.setPriority(Math.min(1 - quality, task.getPriority()));
            budget = new BudgetValue(quality, task.getDurability(), truthToQuality(solution.getTruth()), memory);
        }
        if (feedbackToLinks) {
            TaskLink tLink = memory.currentTaskLink;
            tLink.setPriority(Math.min(1 - quality, tLink.getPriority()));
            TermLink bLink = memory.currentBeliefLink;
            bLink.incPriority(quality);
        }
        return budget;
    }

    /**
     * Evaluate the quality of a revision, then de-prioritize the premises
     * @param tTruth The truth value of the judgment in the task
     * @param bTruth The truth value of the belief
     * @param truth The truth value of the conclusion of revision
     * @param task The task to be immediatedly or continuely processed
     * @return The budget for the new task 
     */
     BudgetValue revise(TruthValue tTruth, TruthValue bTruth, TruthValue truth, Task task, boolean feedbackToLinks) {
        float difT = truth.getExpDifAbs(tTruth);
        task.decPriority(1 - difT);
        task.decDurability(1 - difT);
        if (feedbackToLinks) {
            TaskLink tLink = memory.currentTaskLink;
            tLink.decPriority(1 - difT);
            tLink.decDurability(1 - difT);
            TermLink bLink = memory.currentBeliefLink;
            float difB = truth.getExpDifAbs(bTruth);
            bLink.decPriority(1 - difB);            
            bLink.decDurability(1 - difB);
        }
        float dif = truth.getConfidence() - Math.max(tTruth.getConfidence(), bTruth.getConfidence());
        float priority = or(dif, task.getPriority());
        float durability = or(dif, task.getDurability());
        float quality = truthToQuality(truth);
        return new BudgetValue(priority, durability, quality, memory);
    }
    
    /* ----------------------- Links ----------------------- */
    
    public  BudgetValue distributeAmongLinks(BudgetValue b, int n) {
        float priority = (float) (b.getPriority() / (n==0 ? 1 : Math.sqrt(n)));
        return new BudgetValue(priority, b.getDurability(), b.getQuality(), memory);
    }

    /* ----------------------- Concept ----------------------- */

    /**
     * Activate a concept by an incoming item (Task, TaskLink, or TermLink)
     * @param concept The concept
     * @param budget The budget for the new item 
     */
    public  void activate(Concept concept, BudgetValue budget) {
        float quality = aveAri(concept.getQuality(), budget.getPriority());
        float oldPri = concept.getPriority();
        float priority = or(oldPri, quality);
        float durability = aveAri(concept.getDurability(), budget.getDurability(), oldPri / priority);
        concept.setPriority(priority);
        concept.setDurability(durability);
        concept.setQuality(quality);
    }

    /* ---------------- Bag functions, on all Items ------------------- */
    
    /**
     * Decrease Priority after an item is used, called in Bag
     * <p>
     * After a constant time, p should become d*p.  Since in this period, the item is accessed c*p times, 
     * each time p-q should multiple d^(1/(c*p)). 
     * The intuitive meaning of the parameter "forgetRate" is: after this number of times of access, 
     * priority 1 will become d, it is a system parameter adjustable in run time.
     *
     * @param budget The previous budget value
     * @param forgetRate The budget for the new item
     */
    public static  void forget(BudgetValue budget, float forgetRate, float relativeThreshold) {
        double quality = budget.getQuality() * relativeThreshold;      // re-scaled quality
        double p = budget.getPriority() - quality;                     // priority above quality
        if (p > 0)
            quality += p * Math.pow(budget.getDurability(), 1.0 / (forgetRate * p));    // priority Durability
        budget.setPriority((float) quality);
    }
    
    /**
     * Merge an item into another one in a bag, when the two are identical except in budget values
     * @param baseValue The budget value to be modified
     * @param adjustValue The budget doing the adjusting
     */
    public  void merge(BudgetValue baseValue, BudgetValue adjustValue) {
        baseValue.incPriority(adjustValue.getPriority());
        baseValue.setDurability(Math.max(baseValue.getDurability(), adjustValue.getDurability()));
        baseValue.setQuality(Math.max(baseValue.getQuality(), adjustValue.getQuality()));
    }

    /* ----- Task derivation in MatchingRules and SyllogisticRules ----- */

    /**
     * Forward inference result and adjustment
     * @param truth The truth value of the conclusion
     * @return The budget value of the conclusion
     */
     BudgetValue forward(TruthValue truth) {
        return budgetInference(truthToQuality(truth), 1);
    }
    
    // backward inference result and adjustment
    public  BudgetValue backward(TruthValue truth) {
        return budgetInference(truthToQuality(truth), 1);
    }
    
    public  BudgetValue backwardWeak(TruthValue truth) {
        return budgetInference(w2c(1) * truthToQuality(truth), 1);
    }

    /* ----- Task derivation in CompositionalRules and StructuralRules ----- */

    // forward inference with CompoundTerm conclusion and adjustment
    public  BudgetValue compoundForward(TruthValue truth, Term content) {
        return budgetInference(truthToQuality(truth), content.getComplexity());
    }
    
    public  BudgetValue compoundBackward(Term content) {
        return budgetInference(1, content.getComplexity());
    }

    public  BudgetValue compoundBackwardWeak(Term content) {
        return budgetInference(w2c(1), content.getComplexity());
    }

    /* ----- common function for all inference ----- */
    
    private  BudgetValue budgetInference(float qual, int complexity) {
        TaskLink tLink = memory.currentTaskLink;
        TermLink bLink = memory.currentBeliefLink;
        float priority = tLink.getPriority();
        float durability = tLink.getDurability();
        float quality = (float) (qual / Math.sqrt(complexity));
        if (bLink != null) {
            priority = aveAri(priority, bLink.getPriority());
            durability = aveAri(durability, bLink.getDurability());
            bLink.incPriority(quality);
        }
        return new BudgetValue(and(priority, quality), and(durability, quality), quality, memory);
    }    
}
