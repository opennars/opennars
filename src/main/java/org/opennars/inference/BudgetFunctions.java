/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.inference;

import org.opennars.entity.*;
import org.opennars.language.Term;
import org.opennars.storage.Memory;

import static java.lang.Math.*;
import org.opennars.main.Parameters;

/**
 * Budget functions for resources allocation
 *
 * @author Pei Wang
 * @author Patrick Hammer
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
    public final static float rankBelief(final Sentence judg, final boolean rankTruthExpectation) {        
        if(rankTruthExpectation) {
            return judg.getTruth().getExpectation();
        }
        final float confidence = judg.truth.getConfidence();
        //final float originality = judg.stamp.getOriginality();
        return confidence; //or(confidence, originality);
    }


    /**
     * Evaluate the quality of a revision, then de-prioritize the premises
     *
     * @param tTruth The truth value of the judgment in the task
     * @param bTruth The truth value of the belief
     * @param truth The truth value of the conclusion of revision
     * @return The budget for the new task
     */
    static BudgetValue revise(final TruthValue tTruth, final TruthValue bTruth, final TruthValue truth, final boolean feedbackToLinks, final org.opennars.control.DerivationContext nal) {
        final float difT = truth.getExpDifAbs(tTruth);
        final Task task = nal.getCurrentTask();
        task.decPriority(1 - difT);
        task.decDurability(1 - difT);
        if (feedbackToLinks) {
            final TaskLink tLink = nal.getCurrentTaskLink();
            tLink.decPriority(1 - difT);
            tLink.decDurability(1 - difT);
            final TermLink bLink = nal.getCurrentBeliefLink();
            final float difB = truth.getExpDifAbs(bTruth);
            bLink.decPriority(1 - difB);
            bLink.decDurability(1 - difB);
        }
        final float dif = truth.getConfidence() - max(tTruth.getConfidence(), bTruth.getConfidence());
        final float priority = or(dif, task.getPriority());
        final float durability = aveAri(dif, task.getDurability());
        final float quality = truthToQuality(truth);
        
        /*
        if (priority < 0) {
            memory.nar.output(ERR.class, 
                    new IllegalStateException("BudgetValue.revise resulted in negative priority; set to 0"));
            priority = 0;
        }
        if (durability < 0) {
            memory.nar.output(ERR.class, 
                    new IllegalStateException("BudgetValue.revise resulted in negative durability; set to 0; aveAri(dif=" + dif + ", task.getDurability=" + task.getDurability() +") = " + durability));
            durability = 0;
        }
        if (quality < 0) {
            memory.nar.output(ERR.class, 
                    new IllegalStateException("BudgetValue.revise resulted in negative quality; set to 0"));
            quality = 0;
        }
        */
        
        return new BudgetValue(priority, durability, quality, nal.narParameters);
    }

    /**
     * Update a belief
     *
     * @param task The task containing new belief
     * @param bTruth Truth value of the previous belief
     * @return Budget value of the updating task
     */
    public static BudgetValue update(final Task task, final TruthValue bTruth, Parameters narParameters) {
        final TruthValue tTruth = task.sentence.truth;
        final float dif = tTruth.getExpDifAbs(bTruth);
        final float priority = or(dif, task.getPriority());
        final float durability = aveAri(dif, task.getDurability());
        final float quality = truthToQuality(bTruth);
        return new BudgetValue(priority, durability, quality, narParameters);
    }

    /* ----------------------- Links ----------------------- */
    /**
     * Distribute the budget of a task among the links to it
     *
     * @param b The original budget
     * @param n Number of links
     * @return Budget value for each link
     */
    public static BudgetValue distributeAmongLinks(final BudgetValue b, final int n, Parameters narParameters) {
        final float priority = (float) (b.getPriority() / sqrt(n));
        return new BudgetValue(priority, b.getDurability(), b.getQuality(), narParameters);
    }

    public enum Activating {
        Max, TaskLink
    }
    
    
    /* ----------------------- Concept ----------------------- */
    /**
     * Activate a concept by an incoming TaskLink
     *
     * @param receiver The budget receiving the activation
     * @param amount The budget for the new item
     */
    public static void activate(final BudgetValue receiver, final BudgetValue amount, final Activating mode) {
        switch (mode) {
            case Max:
                BudgetFunctions.merge(receiver, amount);
                break;
            case TaskLink:                
                final float oldPri = receiver.getPriority();
                receiver.setPriority( or(oldPri, amount.getPriority()) );
                receiver.setDurability( aveAri(receiver.getDurability(), amount.getDurability()) );
                receiver.setQuality( receiver.getQuality() );
                break;
        }
        
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
    public static float applyForgetting(final BudgetValue budget, final float forgetCycles, final float relativeThreshold) {
        float quality = budget.getQuality() * relativeThreshold;      // re-scaled quality
        final float p = budget.getPriority() - quality;                     // priority above quality
        if (p > 0) {
            quality += p * pow(budget.getDurability(), 1.0 / (forgetCycles * p));
        }    // priority Durability
        budget.setPriority(quality);
        return quality;
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
    public static BudgetValue forward(final TruthValue truth, final org.opennars.control.DerivationContext nal) {
        return budgetInference(truthToQuality(truth), 1, nal);
    }

    /**
     * Backward inference result and adjustment, stronger case
     *
     * @param truth The truth value of the belief deriving the conclusion
     * @param nal Reference to the memory
     * @return The budget value of the conclusion
     */
    public static BudgetValue backward(final TruthValue truth, final org.opennars.control.DerivationContext nal) {
        return budgetInference(truthToQuality(truth), 1, nal);
    }

    /**
     * Backward inference result and adjustment, weaker case
     *
     * @param truth The truth value of the belief deriving the conclusion
     * @param nal Reference to the memory
     * @return The budget value of the conclusion
     */
    public static BudgetValue backwardWeak(final TruthValue truth, final org.opennars.control.DerivationContext nal) {
        return budgetInference(w2c(1, nal.narParameters) * truthToQuality(truth), 1, nal);
    }

    /* ----- Task derivation in CompositionalRules and StructuralRules ----- */
    /**
     * Forward inference with CompoundTerm conclusion
     *
     * @param truth The truth value of the conclusion
     * @param content The content of the conclusion
     * @param nal Reference to the memory
     * @return The budget of the conclusion
     */
    public static BudgetValue compoundForward(final TruthValue truth, final Term content, final org.opennars.control.DerivationContext nal) {
        final float complexity = (content == null) ? nal.narParameters.COMPLEXITY_UNIT : nal.narParameters.COMPLEXITY_UNIT*content.getComplexity();
        return budgetInference(truthToQuality(truth), complexity, nal);
    }

    /**
     * Backward inference with CompoundTerm conclusion, stronger case
     *
     * @param content The content of the conclusion
     * @param nal Reference to the memory
     * @return The budget of the conclusion
     */
    public static BudgetValue compoundBackward(final Term content, final org.opennars.control.DerivationContext nal) {
        return budgetInference(1, content.getComplexity()*nal.narParameters.COMPLEXITY_UNIT, nal);
    }

    /**
     * Backward inference with CompoundTerm conclusion, weaker case
     *
     * @param content The content of the conclusion
     * @param nal Reference to the memory
     * @return The budget of the conclusion
     */
    public static BudgetValue compoundBackwardWeak(final Term content, final org.opennars.control.DerivationContext nal) {
        return budgetInference(w2c(1, nal.narParameters), content.getComplexity()*nal.narParameters.COMPLEXITY_UNIT, nal);
    }

    /**
     * Get the current activation level of a concept.
     *
     * @param t The Term naming a concept
     * @return the priority value of the concept
     */
    public static float conceptActivation(final Memory mem, final Term t) {
        final Concept c = mem.concept(t);
        return (c == null) ? 0f : c.getPriority();
    }
    
    /**
     * Common processing for all inference step
     *
     * @param qual Quality of the inference
     * @param complexity Syntactic complexity of the conclusion
     * @param nal Reference to the memory
     * @return Budget of the conclusion task
     */
    private static BudgetValue budgetInference(final float qual, final float complexity, final org.opennars.control.DerivationContext nal) {
        Item t = nal.getCurrentTaskLink();
        if (t == null) {
            t = nal.getCurrentTask();
        }
        float priority = t.getPriority();
        float durability = t.getDurability() / complexity;
        final float quality = qual / complexity;
        final TermLink bLink = nal.getCurrentBeliefLink();
        if (bLink != null) {
            priority = or(priority, bLink.getPriority());
            durability = and(durability, bLink.getDurability());
            final float targetActivation = conceptActivation(nal.memory, bLink.target);
            bLink.incPriority(or(quality, targetActivation));
            bLink.incDurability(quality);
        }
        return new BudgetValue(priority, durability, quality, nal.narParameters);
    }

    @Deprecated static BudgetValue solutionEval(final Sentence problem, final Sentence solution, final Task task, final Memory memory) {
        throw new IllegalStateException("Moved to TemporalRules.java");
    }    

    public static BudgetValue budgetTermLinkConcept(final Concept c, final BudgetValue taskBudget, final TermLink termLink) {
        return taskBudget.clone();
    }

}
