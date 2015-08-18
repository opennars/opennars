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
package nars.budget;

import nars.AbstractMemory;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.UtilityFunctions;
import nars.premise.Premise;
import nars.process.ConceptProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;
import nars.truth.Truth;

import static java.lang.Math.pow;


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
    public final static float truthToQuality(final Truth t) {
        if (t == null)
            throw new RuntimeException("truth null");
        final float exp = t.getExpectation();
        return Math.max(exp, (1f - exp) * 0.75f);
    }


    /**
     * Evaluate the quality of a revision, then de-prioritize the premises
     *
     * @param tTruth The truth value of the judgment in the task
     * @param bTruth The truth value of the belief
     * @param truth The truth value of the conclusion of revision
     * @return The budget for the new task
     */
    public static Budget revise(final Truth tTruth, final Truth bTruth, final Truth truth, final Premise p) {
        final float difT = truth.getExpDifAbs(tTruth);
        final Task task = p.getTask();
        task.getBudget().decPriority(1f - difT);
        task.getBudget().andDurability(1f - difT);

        boolean feedbackToLinks = (p instanceof ConceptProcess);
        if (feedbackToLinks) {
            Premise fc = p;
            TaskLink tLink = fc.getTaskLink();
            tLink.decPriority(1f - difT);
            tLink.andDurability(1f - difT);
            TermLink bLink = fc.getTermLink();
            final float difB = truth.getExpDifAbs(bTruth);
            bLink.decPriority(1f - difB);
            bLink.andDurability(1f - difB);
        }

        float dif = truth.getConfidence() - Math.max(tTruth.getConfidence(), bTruth.getConfidence());
        
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
        
        return new Budget(priority, durability, quality);
    }

    /**
     * Update a belief
     *
     * @param task The task containing new belief
     * @param bTruth Truth value of the previous belief
     * @return Budget value of the updating task
     */
    static Budget update(final Task task, final Truth bTruth) {
        final Truth tTruth = task.getTruth();
        final float dif = tTruth.getExpDifAbs(bTruth);
        final float priority = or(dif, task.getPriority());
        final float durability = aveAri(dif, task.getDurability());
        final float quality = truthToQuality(bTruth);
        return new Budget(priority, durability, quality);
    }

    /* ----------------------- Links ----------------------- */
    /**
     * Distribute the budget of a task among the links to it
     *
     * @param b The original budget
     * @param n Number of links
     * @return Budget value for each tlink
     */
    public static Budget divide(final Budget b, final float divisor) {
        final float newPriority = b.getPriority() / divisor;
        return new Budget(newPriority, b.getDurability(), b.getQuality());
    }


    public enum Activating {
        Classic, Accum, WTF
    }
    
    
//    /* ----------------------- Concept ----------------------- */
//    /**
//     * Activate a concept by an incoming TaskLink
//     *
//     *
//     * @param factor linear interpolation factor; 1.0: values are applied fully,  0: values are not applied at all
//     * @param receiver The budget receiving the activation
//     * @param amount The budget for the new item
//     */
//    public static void activate(final Budget receiver, final Budget amount, final Activating mode, final float factor) {
//        switch (mode) {
//            /*case Max:
//                receiver.max(amount);
//                break;*/
//
//            case Accum:
//                receiver.accumulate(amount);
//                break;
//
//            case Classic:
//                float priority = or(receiver.getPriority(), amount.getPriority());
//                float durability = aveAri(receiver.getDurability(), amount.getDurability());
//                receiver.setPriority(priority);
//                receiver.setDurability(durability);
//                break;
//
//            case WTF:
//
//                final float currentPriority = receiver.getPriority();
//                final float targetPriority = amount.getPriority();
//                /*receiver.setPriority(
//                        lerp(or(currentPriority, targetPriority),
//                                currentPriority,
//                                factor) );*/
//                float op = or(currentPriority, targetPriority);
//                if (op > currentPriority) op = lerp(op, currentPriority, factor);
//                receiver.setPriority( op );
//
//                final float currentDurability = receiver.getDurability();
//                final float targetDurability = amount.getDurability();
//                receiver.setDurability(
//                        lerp(aveAri(currentDurability, targetDurability),
//                                currentDurability,
//                                factor) );
//
//                //doesnt really change it:
//                //receiver.setQuality( receiver.getQuality() );
//
//                break;
//        }
//
//    }
//
//    /**
//     */
//    public static void activate(final Budget receiver, final Budget amount, Activating mode) {
//        activate(receiver, amount, mode, 1f);
//    }

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
    @Deprecated public static float forgetIterative(final Budget budget, final float forgetCycles, final float relativeThreshold) {
        float newPri = budget.getQuality() * relativeThreshold;      // re-scaled quality
        final float dp = budget.getPriority() - newPri;                     // priority above quality
        if (dp > 0) {
            newPri += (float) (dp * pow(budget.getDurability(), 1.0f / (forgetCycles * dp)));
        }    // priority Durability
        budget.setPriority(newPri);
        return newPri;
    }


    /** forgetting calculation for real-time timing */
    public static float forgetPeriodic(final Budget budget, final float forgetPeriod /* cycles */, float minPriorityForgettingCanAffect, final long currentTime) {

        final float currentPriority = budget.getPriority();
        final long forgetDelta = budget.setLastForgetTime(currentTime);
        if (forgetDelta == 0) {
            return currentPriority;
        }

        minPriorityForgettingCanAffect *= budget.getQuality();

        if (currentPriority < minPriorityForgettingCanAffect) {
            //priority already below threshold, don't decrease any further
            return currentPriority;
        }

        float forgetProportion = forgetDelta / forgetPeriod;
        if (forgetProportion <= 0) return currentPriority;

        //more durability = slower forgetting; durability near 1.0 means forgetting will happen slowly, near 0.0 means will happen at a max rate
        forgetProportion *= (1.0f - budget.getDurability());

        float newPriority;
        if (forgetProportion > 1.0f) {
            //forgetProportion = 1.0f;
            newPriority = minPriorityForgettingCanAffect;
        } else {
            newPriority = currentPriority * (1.0f - forgetProportion) + minPriorityForgettingCanAffect * (forgetProportion);
        }


        budget.setPriority(newPriority);

        return newPriority;


        /*if (forgetDelta > 0)
            System.out.println("  " + currentPriority + " -> " + budget.getPriority());*/
        
    }


    /*public final static float abs(final float a, final float b) {
        float c = (a - b);
        return (c >= 0) ? c : -c;
    }*/

    /* ----- Task derivation in LocalRules and SyllogisticRules ----- */
    /**
     * Forward logic result and adjustment
     *
     * @param truth The truth value of the conclusion
     * @return The budget value of the conclusion
     */
    public static Budget forward(final Truth truth, final Premise nal) {
        return budgetInference(truthToQuality(truth), 1, nal);
    }

    /**
     * Backward logic result and adjustment, stronger case
     *
     * @param truth The truth value of the belief deriving the conclusion
     * @param nal Reference to the memory
     * @return The budget value of the conclusion
     */
    public static Budget backward(final Truth truth, final Premise nal) {
        return budgetInference(truthToQuality(truth), 1, nal);
    }

    /**
     * Backward logic result and adjustment, weaker case
     *
     * @param truth The truth value of the belief deriving the conclusion
     * @param nal Reference to the memory
     * @return The budget value of the conclusion
     */
    public static Budget backwardWeak(final Truth truth, final Premise nal) {
        return budgetInference(w2c(1) * truthToQuality(truth), 1, nal);
    }

    /* ----- Task derivation in CompositionalRules and StructuralRules ----- */
    /**
     * Forward logic with CompoundTerm conclusion
     *
     * @param truth The truth value of the conclusion
     * @param content The content of the conclusion
     * @param nal Reference to the memory
     * @return The budget of the conclusion
     */
    public static Budget compoundForward(final Truth truth, final Term content, final Premise nal) {
        return compoundForward(new DirectBudget(), truth, content, nal);
    }

    public static Budget compoundForward(Budget target, final Truth truth, final Term content, final Premise nal) {
        final int complexity = (content == null) ? 1 : content.getComplexity();
        return budgetInference(target, truthToQuality(truth), complexity, nal);
    }

    /**
     * Backward logic with CompoundTerm conclusion, stronger case
     *
     * @param content The content of the conclusion
     * @return The budget of the conclusion
     */
    public static Budget compoundBackward(final Term content, final Premise nal) {
        return budgetInference(1, content.getComplexity(), nal);
    }

    /**
     * Backward logic with CompoundTerm conclusion, weaker case
     *
     * @param content The content of the conclusion
     * @param nal Reference to the memory
     * @return The budget of the conclusion
     */
    public static Budget compoundBackwardWeak(final Term content, final Premise nal) {
        return budgetInference(w2c(1), content.getComplexity(), nal);
    }

    private static Budget budgetInference(final float qual, final int complexity, final Premise nal) {
        return budgetInference(new Budget(), qual, complexity, nal );
    }
    /**
     * Common processing for all logic step
     *
     * @param qual Quality of the logic
     * @param complexity Syntactic complexity of the conclusion
     * @param nal Reference to the memory
     * @return Budget of the conclusion task
     */
    private static Budget budgetInference(Budget target, final float qual, final int complexity, final Premise nal) {
        final TaskLink nalTL = nal.getTaskLink();
        final Budget t = (nalTL !=null) ? nalTL :  nal.getTask().getBudget();

        float priority = t.getPriority();
        float durability = t.getDurability() / complexity;
        final float quality = qual / complexity;

        final TermLink bLink = nal.getTermLink();
        if (bLink!=null) {
            priority = or(priority, bLink.getPriority());
            durability = and(durability, bLink.getDurability());
            final float targetActivation = nal.conceptPriority(bLink.getTarget());
            bLink.orPriority(or(quality, targetActivation));
            bLink.orDurability(quality);
        }

        return target.set(priority, durability, quality);


        /* ORIGINAL: https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/inference/BudgetFunctions.java
            Item t = memory.currentTaskLink;
            if (t == null) {
                t = memory.currentTask;
            }
            float priority = t.getPriority();
            float durability = t.getDurability() / complexity;
            float quality = qual / complexity;
            TermLink bLink = memory.currentBeliefLink;
            if (bLink != null) {
                priority = or(priority, bLink.getPriority());
                durability = and(durability, bLink.getDurability());
                float targetActivation = memory.getConceptActivation(bLink.getTarget());
                bLink.incPriority(or(quality, targetActivation));
                bLink.incDurability(quality);
            }
            return new BudgetValue(priority, durability, quality);
         */
    }

    @Deprecated static Budget solutionEval(final Sentence problem, final Sentence solution, Task task, final AbstractMemory memory) {
        throw new RuntimeException("Moved to TemporalRules.java");
    }    

    public static Budget budgetTermLinkConcept(Concept c, Budget taskBudget, TermLink termLink) {
        return taskBudget.clone();
    }

}
