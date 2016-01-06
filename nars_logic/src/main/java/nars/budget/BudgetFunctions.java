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

import nars.NAR;
import nars.bag.BLink;
import nars.nal.UtilityFunctions;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.term.Termed;
import nars.term.Termlike;
import nars.truth.Truth;

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
	 * @param t
	 *            The truth value of a judgment
	 * @return The quality of the judgment, according to truth value only
	 */
	public static float truthToQuality(Truth t) {
		float exp = t.getExpectation();
		return Math.max(exp, (1.0f - exp) * 0.75f);
	}

	/**
	 * Evaluate the quality of a revision, then de-prioritize the premises
	 * 
	 * @param tTruth
	 *            The truth value of the judgment in the task of the premise
	 * @param oldBelief
	 *            The truth value of the previously existing belief
	 * @param conclusion
	 *            The truth value of the conclusion of revision
	 * @return The budget for the new task
	 */
	public static Budget revise(Truth tTruth, Task oldBelief, Truth conclusion,
			Budget tb) {

		Truth bTruth = oldBelief.getTruth();
		float difT = conclusion.getExpDifAbs(tTruth);

		tb.andPriority(1.0f - difT);
		tb.andDurability(1.0f - difT);

		oldBelief.onRevision(conclusion);

		float dif = conclusion.getConfidence()
				- Math.max(tTruth.getConfidence(), bTruth.getConfidence());
		if (dif < 0) {
			// throw new RuntimeException("Revision fault: " + oldBelief +
			// " more confident than concluded: " + conclusion);
			System.err.println("Revision fault: " + oldBelief
					+ " more confident than concluded: " + conclusion);
		}

		float priority = or(dif, tb.getPriority());
		float durability = aveAri(dif, tb.getDurability());
		float quality = truthToQuality(conclusion);

		/*
		 * if (priority < 0) { memory.nar.output(ERR.class, new
		 * RuntimeException(
		 * "BudgetValue.revise resulted in negative priority; set to 0"));
		 * priority = 0; } if (durability < 0) { memory.nar.output(ERR.class,
		 * new RuntimeException(
		 * "BudgetValue.revise resulted in negative durability; set to 0; aveAri(dif="
		 * + dif + ", task.getDurability=" + task.getDurability() +") = " +
		 * durability)); durability = 0; } if (quality < 0) {
		 * memory.nar.output(ERR.class, new RuntimeException(
		 * "BudgetValue.revise resulted in negative quality; set to 0"));
		 * quality = 0; }
		 */

		return new UnitBudget(priority, durability, quality);
	}

	// /**
	// * Update a belief
	// *
	// * @param task The task containing new belief
	// * @param bTruth Truth value of the previous belief
	// * @return Budget value of the updating task
	// */
	// static Budget update(final Task task, final Truth bTruth) {
	// final Truth tTruth = task.getTruth();
	// final float dif = tTruth.getExpDifAbs(bTruth);
	// final float priority = or(dif, task.getPriority());
	// final float durability = aveAri(dif, task.getDurability());
	// final float quality = truthToQuality(bTruth);
	// return new Budget(priority, durability, quality);
	// }

	// /* ----------------------- Links ----------------------- */
	// /**
	// * Distribute the budget of a task among the links to it
	// *
	// * @param b The original budget
	// * @param factor to scale dur and qua
	// * @return Budget value for each tlink
	// */
	// public static UnitBudget clonePriorityMultiplied(Budgeted b, float
	// factor) {
	// float newPriority = b.getPriority() * factor;
	// return new UnitBudget(newPriority, b.getDurability(), b.getQuality());
	// }

	// /**
	// */
	// public static void activate(final Budget receiver, final Budget amount,
	// Activating mode) {
	// activate(receiver, amount, mode, 1f);
	// }

	// /* ---------------- Bag functions, on all Items ------------------- */
	// /**
	// * Decrease Priority after an item is used, called in Bag.
	// * After a constant time, p should become d*p. Since in this period, the
	// * item is accessed c*p times, each time p-q should multiple d^(1/(c*p)).
	// * The intuitive meaning of the parameter "forgetRate" is: after this
	// number
	// * of times of access, priority 1 will become d, it is a system parameter
	// * adjustable in run time.
	// *
	// * @param budget The previous budget value
	// * @param forgetCycles The budget for the new item
	// * @param relativeThreshold The relative threshold of the bag
	// */
	// @Deprecated public static float forgetIterative(Budget budget, float
	// forgetCycles, float relativeThreshold) {
	// float newPri = budget.getQuality() * relativeThreshold; // re-scaled
	// quality
	// float dp = budget.getPriority() - newPri; // priority above quality
	// if (dp > 0) {
	// newPri += (float) (dp * pow(budget.getDurability(), 1.0f / (forgetCycles
	// * dp)));
	// } // priority Durability
	// budget.setPriority(newPri);
	// return newPri;
	// }
	//
	//
	//
	// /** forgetting calculation for real-time timing */
	// public static float forgetPeriodic(Budget budget, float forgetPeriod /*
	// cycles */, float minPriorityForgettingCanAffect, long currentTime) {
	//
	// float currentPriority = budget.getPriority();
	// long forgetDelta = budget.setLastForgetTime(currentTime);
	// if (forgetDelta == 0) {
	// return currentPriority;
	// }
	//
	// minPriorityForgettingCanAffect *= budget.getQuality();
	//
	// if (currentPriority < minPriorityForgettingCanAffect) {
	// //priority already below threshold, don't decrease any further
	// return currentPriority;
	// }
	//
	// float forgetProportion = forgetDelta / forgetPeriod;
	// if (forgetProportion <= 0) return currentPriority;
	//
	// //more durability = slower forgetting; durability near 1.0 means
	// forgetting will happen slowly, near 0.0 means will happen at a max rate
	// forgetProportion *= (1.0f - budget.getDurability());
	//
	// float newPriority = forgetProportion > 1.0f ?
	// minPriorityForgettingCanAffect : currentPriority * (1.0f -
	// forgetProportion) + minPriorityForgettingCanAffect * (forgetProportion);
	//
	//
	// budget.setPriority(newPriority);
	//
	// return newPriority;
	//
	//
	// /*if (forgetDelta > 0)
	// System.out.println("  " + currentPriority + " -> " +
	// budget.getPriority());*/
	//
	// }

	/*
	 * public final static float abs(final float a, final float b) { float c =
	 * (a - b); return (c >= 0) ? c : -c; }
	 */

	/* ----- Task derivation in LocalRules and SyllogisticRules ----- */
	/**
	 * Forward logic result and adjustment
	 * 
	 * @param truth
	 *            The truth value of the conclusion
	 * @return The budget value of the conclusion
	 */
	public static Budget forward(Truth truth, ConceptProcess nal) {
		return budgetInference(truthToQuality(truth), 1, nal);
	}

	/**
	 * Backward logic result and adjustment, stronger case
	 * 
	 * @param truth
	 *            The truth value of the belief deriving the conclusion
	 * @param nal
	 *            Reference to the memory
	 * @return The budget value of the conclusion
	 */
	public static Budget backward(Truth truth, ConceptProcess nal) {
		return budgetInference(truthToQuality(truth), 1, nal);
	}

	/**
	 * Backward logic result and adjustment, weaker case
	 * 
	 * @param truth
	 *            The truth value of the belief deriving the conclusion
	 * @param nal
	 *            Reference to the memory
	 * @return The budget value of the conclusion
	 */
	public static Budget backwardWeak(Truth truth, ConceptProcess nal) {
		return budgetInference(w2c(1) * truthToQuality(truth), 1, nal);
	}

	/* ----- Task derivation in CompositionalRules and StructuralRules ----- */

	public static Budget compoundForward(Budget target, Truth truth,
			Termed content, ConceptProcess nal) {
		int complexity = content.term().complexity();
		return budgetInference(target, truthToQuality(truth), complexity, nal);
	}

	/**
	 * Backward logic with CompoundTerm conclusion, stronger case
	 * 
	 * @param content
	 *            The content of the conclusion
	 * @return The budget of the conclusion
	 */
	public static Budget compoundBackward(Termed content, ConceptProcess nal) {
		return budgetInference(1.0f, content.term().complexity(), nal);
	}

	/**
	 * Backward logic with CompoundTerm conclusion, weaker case
	 * 
	 * @param content
	 *            The content of the conclusion
	 * @param nal
	 *            Reference to the memory
	 * @return The budget of the conclusion
	 */
	public static Budget compoundBackwardWeak(Termlike content,
			ConceptProcess nal) {
		return budgetInference(w2c(1), content.complexity(), nal);
	}

	static Budget budgetInference(float qual, int complexity, ConceptProcess nal) {
		return budgetInference(new UnitBudget(), qual, complexity, nal);
	}

	/**
	 * Common processing for all logic step
	 * 
	 * @param qual
	 *            Quality of the logic
	 * @param complexity
	 *            Syntactic complexity of the conclusion
	 * @param nal
	 *            Reference to the memory
	 * @return Budget of the conclusion task
	 */
	static Budget budgetInference(Budget target, float qual, int complexity,
			ConceptProcess nal) {
		float complexityFactor = complexity > 1 ?

		// sqrt factor (experimental)
		// (float) (1f / Math.sqrt(Math.max(1, complexity))) //experimental,
		// reduces dur and qua by sqrt of complexity (more slowly)

				// linear factor (original)
				(1.0f / Math.max(1, complexity))

				: 1.0f;

		return budgetInference(target, qual, complexityFactor, nal);
	}

	static Budget budgetInference(Budget target, float qual, float complexityFactor, ConceptProcess nal) {

        BLink<Task> taskLink = nal.taskLink;

        Budget t =
            (taskLink !=null) ? taskLink :  nal.getTask().getBudget();


        float priority = t.getPriority();
        float durability = t.getDurability() * complexityFactor;
        final float quality = qual * complexityFactor;

        BLink<Termed> termLink = nal.termLink;
        if (termLink!=null) {
            priority = or(priority, termLink.getPriority());
            durability = and(durability, termLink.getDurability()); //originaly was 'AND'

            NAR nar = nal.nar;
            final float targetActivation = nar.conceptPriority(termLink.get(), 0);
            float sourceActivation = 1.0f;
            if(taskLink!=null) {
                sourceActivation = nar.conceptPriority(taskLink.get().term(), 0);
            }
            if (targetActivation >= 0) {
                //https://groups.google.com/forum/#!topic/open-nars/KnUA43B6iYs
                termLink.orPriority(or(quality, and(sourceActivation,targetActivation)));
                //was
                //termLink.orPriority(or(quality, targetActivation));
                termLink.orDurability(quality);
            }
        }

        return target.budget(priority, durability, quality);


        /* ORIGINAL: https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/inference/BudgetFunctions.java
            Item t = memory.currentTaskLink;
            if (t == null) {
                t = memory.currentTask;
            }
            float priority = t.getPriority();
            float durability = t.getDurability() / complexity;
            float quality = qual / complexity;
            TermLink termLink = memory.currentBeliefLink;
            if (termLink != null) {
                priority = or(priority, termLink.getPriority());
                durability = and(durability, termLink.getDurability());
                float targetActivation = memory.getConceptActivation(termLink.getTarget());
                termLink.incPriority(or(quality, targetActivation));
                termLink.incDurability(quality);
            }
            return new BudgetValue(priority, durability, quality);
         */
    }

	// may be more efficient than the for-loop version above, for 2 params
	public static float aveAri(float a, float b) {
		return (a + b) / 2.0f;
	}
}
