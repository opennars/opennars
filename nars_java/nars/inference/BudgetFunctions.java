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
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.inference;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import nars.core.Memory;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.entity.TruthValue;
import nars.language.Term;

/**
 * Budget functions for resources allocation
 */
public final class BudgetFunctions extends UtilityFunctions {

    /* ----------------------- Belief evaluation ----------------------- */
    /**
     * Determine the quality of a judgment by its truth value alone
     * <p>
     * Mainly decided by confidence, though binary judgment is also preferred
     *
     * @param t The truth value of a judgment
     * @return The quality of the judgment, according to truth value only
     */
    public final static float truthToQuality(final TruthValue t) {
        final float exp = t.getExpectation();
        return (float) max(exp, (1 - exp)*0.75);
    }

    /**
     * Determine the rank of a judgment by its quality and originality (stamp
 baseLength), called from Concept
     *
     * @param judg The judgment to be ranked
     * @return The rank of the judgment, according to truth value only
     */
    public final static float rankBelief(final Sentence judg) {        
        final float confidence = judg.truth.getConfidence();
        final float originality = 1.0f / (judg.stamp.evidentialBase.length + 1);
        return or(confidence, originality);
    }


    /**
     * Evaluate the quality of a revision, then de-prioritize the premises
     *
     * @param tTruth The truth value of the judgment in the task
     * @param bTruth The truth value of the belief
     * @param truth The truth value of the conclusion of revision
     * @return The budget for the new task
     */
    static BudgetValue revise(final TruthValue tTruth, final TruthValue bTruth, final TruthValue truth, final boolean feedbackToLinks, final Memory memory) {
        final float difT = truth.getExpDifAbs(tTruth);
        final Task task = memory.getCurrentTask();
        task.decPriority(1 - difT);
        task.decDurability(1 - difT);
        if (feedbackToLinks) {
            TaskLink tLink = memory.getCurrentTaskLink();
            tLink.decPriority(1 - difT);
            tLink.decDurability(1 - difT);
            TermLink bLink = memory.getCurrentBeliefLink();
            final float difB = truth.getExpDifAbs(bTruth);
            bLink.decPriority(1 - difB);
            bLink.decDurability(1 - difB);
        }
        float dif = truth.getConfidence() - max(tTruth.getConfidence(), bTruth.getConfidence());
        
        //TODO determine if this is correct
        if (dif < 0) dif = 0;  
        
        
        float priority = or(dif, task.getPriority());
        float durability = aveAri(dif, task.getDurability());
        float quality = truthToQuality(truth);
        
        /*
        if (priority < 0) {
            memory.nar.output(ERR.class, 
                    new RuntimeException("BudgetValue.revise resulted in negative priority; set to 0"));
            priority = 0;
        }
        if (durability < 0) {
            memory.nar.output(ERR.class, 
                    new RuntimeException("BudgetValue.revise resulted in negative durability; set to 0; aveAri(dif=" + dif + ", task.getDurability=" + task.getDurability() +") = " + durability));
            durability = 0;
        }
        if (quality < 0) {
            memory.nar.output(ERR.class, 
                    new RuntimeException("BudgetValue.revise resulted in negative quality; set to 0"));
            quality = 0;
        }
        */
        
        return new BudgetValue(priority, durability, quality);
    }

    /**
     * Update a belief
     *
     * @param task The task containing new belief
     * @param bTruth Truth value of the previous belief
     * @return Budget value of the updating task
     */
    static BudgetValue update(final Task task, final TruthValue bTruth) {
        final TruthValue tTruth = task.sentence.truth;
        final float dif = tTruth.getExpDifAbs(bTruth);
        final float priority = or(dif, task.getPriority());
        final float durability = aveAri(dif, task.getDurability());
        final float quality = truthToQuality(bTruth);
        return new BudgetValue(priority, durability, quality);
    }

    /* ----------------------- Links ----------------------- */
    /**
     * Distribute the budget of a task among the links to it
     *
     * @param b The original budget
     * @param n Number of links
     * @return Budget value for each link
     */
    public static BudgetValue distributeAmongLinks(final BudgetValue b, final int n) {
        final float priority = (float) (b.getPriority() / sqrt(n));
        return new BudgetValue(priority, b.getDurability(), b.getQuality());
    }

    /* ----------------------- Concept ----------------------- */
    /**
     * Activate a concept by an incoming TaskLink
     *
     * @param concept The concept
     * @param budget The budget for the new item
     */
    public static void activate(final Concept concept, final BudgetValue budget) {
        final float oldPri = concept.getPriority();
        final float priority = or(oldPri, budget.getPriority());
        final float durability = aveAri(concept.getDurability(), budget.getDurability());
        final float quality = concept.getQuality();
        concept.setPriority(priority);
        concept.setDurability(durability);
        concept.setQuality(quality);
    }

    /* ---------------- Bag functions, on all Items ------------------- */
    /**
     * Decrease Priority after an item is used, called in Bag.
     * After a constant time, p should become d*p. Since in this period, the
     * item is accessed c*p times, each time p-q should multiple d^(1/(c*p)).
     * The intuitive meaning of the parameter "forgetRate" is: after this number
     * of times of access, priority 1 will become d, it is a system parameter
     * adjustable in run time.
     *
     * @param budget The previous budget value
     * @param forgetCycles The budget for the new item
     * @param relativeThreshold The relative threshold of the bag
     */
    public static void forget(final BudgetValue budget, final float forgetCycles, final float relativeThreshold) {
        float quality = budget.getQuality() * relativeThreshold;      // re-scaled quality
        final float p = budget.getPriority() - quality;                     // priority above quality
        if (p > 0) {
            quality += p * pow(budget.getDurability(), 1.0 / (forgetCycles * p));
        }    // priority Durability
        budget.setPriority(quality);
    }

    /**
     * Merge an item into another one in a bag, when the two are identical
     * except in budget values
     *
     * @param b The budget baseValue to be modified
     * @param a The budget adjustValue doing the adjusting
     */
    public static void merge(final BudgetValue b, final BudgetValue a) {        
        b.setPriority(max(b.getPriority(), a.getPriority()));
        b.setDurability(max(b.getDurability(), a.getDurability()));
        b.setQuality(max(b.getQuality(), a.getQuality()));
    }

    /* ----- Task derivation in LocalRules and SyllogisticRules ----- */
    /**
     * Forward inference result and adjustment
     *
     * @param truth The truth value of the conclusion
     * @return The budget value of the conclusion
     */
    public static BudgetValue forward(final TruthValue truth, final Memory memory) {
        return budgetInference(truthToQuality(truth), 1, memory);
    }

    /**
     * Backward inference result and adjustment, stronger case
     *
     * @param truth The truth value of the belief deriving the conclusion
     * @param memory Reference to the memory
     * @return The budget value of the conclusion
     */
    public static BudgetValue backward(final TruthValue truth, final Memory memory) {
        return budgetInference(truthToQuality(truth), 1, memory);
    }

    /**
     * Backward inference result and adjustment, weaker case
     *
     * @param truth The truth value of the belief deriving the conclusion
     * @param memory Reference to the memory
     * @return The budget value of the conclusion
     */
    public static BudgetValue backwardWeak(final TruthValue truth, final Memory memory) {
        return budgetInference(w2c(1) * truthToQuality(truth), 1, memory);
    }

    /* ----- Task derivation in CompositionalRules and StructuralRules ----- */
    /**
     * Forward inference with CompoundTerm conclusion
     *
     * @param truth The truth value of the conclusion
     * @param content The content of the conclusion
     * @param memory Reference to the memory
     * @return The budget of the conclusion
     */
    public static BudgetValue compoundForward(final TruthValue truth, final Term content, final Memory memory) {
        final int complexity = (content == null) ? 1 : content.getComplexity();
        return budgetInference(truthToQuality(truth), complexity, memory);
    }

    /**
     * Backward inference with CompoundTerm conclusion, stronger case
     *
     * @param content The content of the conclusion
     * @param memory Reference to the memory
     * @return The budget of the conclusion
     */
    public static BudgetValue compoundBackward(final Term content, final Memory memory) {
        return budgetInference(1, content.getComplexity(), memory);
    }

    /**
     * Backward inference with CompoundTerm conclusion, weaker case
     *
     * @param content The content of the conclusion
     * @param memory Reference to the memory
     * @return The budget of the conclusion
     */
    public static BudgetValue compoundBackwardWeak(final Term content, final Memory memory) {
        return budgetInference(w2c(1), content.getComplexity(), memory);
    }

    /**
     * Common processing for all inference step
     *
     * @param qual Quality of the inference
     * @param complexity Syntactic complexity of the conclusion
     * @param memory Reference to the memory
     * @return Budget of the conclusion task
     */
    private static BudgetValue budgetInference(final float qual, final int complexity, final Memory memory) {
        Item t = memory.getCurrentTaskLink();
        if (t == null) {
            t = memory.getCurrentTask();
        }
        float priority = t.getPriority();
        float durability = t.getDurability() / complexity;
        final float quality = qual / complexity;
        final TermLink bLink = memory.getCurrentBeliefLink();
        if (bLink != null) {
            priority = or(priority, bLink.getPriority());
            durability = and(durability, bLink.getDurability());
            final float targetActivation = memory.conceptActivation(bLink.target);
            bLink.incPriority(or(quality, targetActivation));
            bLink.incDurability(quality);
        }
        return new BudgetValue(priority, durability, quality);
    }

    @Deprecated static BudgetValue solutionEval(final Sentence problem, final Sentence solution, Task task, final Memory memory) {
        throw new RuntimeException("Moved to TemporalRules.java");
    }    
}
