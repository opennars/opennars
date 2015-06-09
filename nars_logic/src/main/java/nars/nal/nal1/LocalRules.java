/*
 * LocalRules.java
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
 * but WITHOUT ANY WARRANTY; without even the abduction warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.nal.nal1;

import nars.Events.Answer;
import nars.Memory;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.io.out.Output;
import nars.Symbols;
import nars.nal.*;
import nars.nal.nal2.NAL2;
import nars.nal.nal7.TemporalRules;
import nars.nal.stamp.Stamper;
import nars.nal.term.Compound;
import nars.nal.term.Statement;
import nars.nal.term.Term;

import java.util.Arrays;


/**
 * Directly process a task by a oldBelief, with only two Terms in both. In
 * matching, the new task is compared with an existing direct Task in that
 * Concept, to carry out:
 * <p>
 *   revision: between judgments or goals on non-overlapping evidence; 
 *   satisfy: between a Sentence and a Question/Goal; 
 *   merge: between items of the same type and stamp; 
 *   conversion: between different inheritance relations.
 */
public class LocalRules {


    /**
     * Check whether two sentences can be used in revision
     *
     * @param s1 The first sentence
     * @param s2 The second sentence
     * @return If revision is possible between the two sentences
     */
    public static boolean revisible(final Sentence s1, final Sentence s2) {
        if (s1.isRevisible())
            if (s1.equalTerms(s2))
                if (TemporalRules.matchingOrder(s1.getTemporalOrder(), s2.getTemporalOrder()))
                    return true;

        return false;
    }


    /**
     * Belief revision
     * <p>
     * called from Concept.reviseTable and match
     *
     * @param newBelief The new belief in task
     * @param oldBelief The previous belief with the same content
     * @param feedbackToLinks Whether to send feedback to the links
     * @param memory Reference to the memory
     */
    public static Task revision(final Sentence newBelief, final Sentence oldBelief, final boolean feedbackToLinks, final NAL nal) {
        Stamper stamp = nal.newStampIfNotOverlapping(newBelief, oldBelief);
        if (stamp == null) return null;

        final Task t = nal.getCurrentTask();

        Truth newBeliefTruth = newBelief.truth;
        Truth oldBeliefTruth = oldBelief.truth;
        Truth truth = TruthFunctions.revision(newBeliefTruth, oldBeliefTruth);
        Budget budget = BudgetFunctions.revise(newBeliefTruth, oldBeliefTruth, truth, nal);

        Task revised = nal.deriveTask(nal.newTask(newBelief.term)
            .punctuation(t.sentence.punctuation)
                .truth(truth)
                .stamp(stamp)
                .budget(budget)
                .parent(t, nal.getCurrentBelief()),
                true, false, t, false);

        if (revised!=null)
            nal.memory.logic.BELIEF_REVISION.hit();

        return revised;
    }


    public static Task tryRevision(final Task newBelief, Sentence oldBelief, final boolean feedbackToLinks, final NAL nal) {
        Stamper stamp = nal.newStampIfNotOverlapping(newBelief.sentence, oldBelief);
        if (stamp == null) return null;

        final Task t = nal.getCurrentTask();

        Truth newBeliefTruth = newBelief.getTruth();
        Truth oldBeliefTruth = oldBelief.projection(nal.time(), newBelief.getOccurrenceTime());
        Truth truth = TruthFunctions.revision(newBeliefTruth, oldBeliefTruth);
        Budget budget = BudgetFunctions.revise(newBeliefTruth, oldBeliefTruth, truth, nal);

        Task revised = nal.deriveTask(nal.newTask(newBelief.getTerm())
                        .punctuation(t.sentence.punctuation)
                        .truth(truth)
                        .stamp(stamp)
                        .budget(budget)
                        .parent(t, nal.getCurrentBelief()),
                true, false, t, false);

        if (revised!=null)
            nal.memory.logic.BELIEF_REVISION.hit();

        return revised;
    }

    /**
     * Check if a Sentence provide a better answer to a Question or Goal
     *
     * @param belief The proposed answer
     * @param task The task to be processed
     * @param memory Reference to the memory
     */
    public static boolean trySolution(Sentence belief, final Task task, final NAL nal) {

        if (belief == null) return false;


        Sentence problem = task.sentence;
        Memory memory = nal.memory;
        
        if (!TemporalRules.matchingOrder(problem, belief)) {
            //System.out.println("Unsolved: Temporal order not matching");
            //memory.emit(Unsolved.class, task, belief, "Non-matching temporal Order");
            return false;
        }

        final long now = memory.time();
        Sentence oldBest = task.getBestSolution();
        float newQ = TemporalRules.solutionQuality(problem, belief, now);
        if (oldBest != null) {
            float oldQ = TemporalRules.solutionQuality(problem, oldBest, now);
            if (oldQ >= newQ) {
                if (problem.isGoal()) {
                    memory.emotion.happy(oldQ, task, nal);
                }
                //System.out.println("Unsolved: Solution of lesser quality");
                //memory.emit(Unsolved.class, task, belief, "Lower quality");
                return false;
            }
        }
        
        Term content = belief.term;
        if (content.hasVarIndep()) {
            Term u[] = new Term[] { content, problem.term };
            
            boolean unified = Variables.unify(Symbols.VAR_INDEPENDENT, u, nal.memory.random);
            if (!unified) return false;

            content = u[0];

            belief = belief.clone((Compound)content);
            if (belief == null) {
                throw new RuntimeException("Unification invalid: " + Arrays.toString(u) + " while cloning into " + belief);
                //return false;
            }


        }

        task.setBestSolution(memory, belief);

        memory.logic.SOLUTION_BEST.set(task.getPriority());

        if (problem.isGoal()) {
            memory.emotion.happy(newQ, task, nal);
        }
        
        Budget budget = TemporalRules.solutionEval(problem, belief, task, nal);

            
        //Solution Activated
        if(task.sentence.punctuation==Symbols.QUESTION || task.sentence.punctuation==Symbols.QUEST) {
            if(task.isInput()) { //only show input tasks as solutions
                memory.emit(Answer.class, task, belief);
            } else {
                memory.emit(Output.class, task, belief);   //solution to quests and questions can be always showed
            }
        } else {
            memory.emit(Output.class, task, belief);   //goal things only show silence related
        }


        /*memory.output(task);

        //only questions and quests get here because else output is spammed
        if(task.sentence.isQuestion() || task.sentence.isQuest()) {
            memory.emit(Solved.class, task, belief);
        } else {
            memory.emit(Output.class, task, belief);
        }*/

        //nal.addSolution(nal.getCurrentTask(), budget, belief, task);
        nal.addSolution(task, budget, belief, task);

        return true;


    }


    /* -------------------- same terms, difference relations -------------------- */
    /**
     * The task and belief match reversely
     *
     * @param nal Reference to the memory
     */
    public static void matchReverse(final NAL nal) {
        Task task = nal.getCurrentTask();
        Sentence belief = nal.getCurrentBelief();
        Sentence sentence = task.sentence;
        if (TemporalRules.matchingOrder(sentence.getTemporalOrder(), TemporalRules.reverseOrder(belief.getTemporalOrder()))) {
            if (sentence.isJudgment()) {
                NAL2.inferToSym(sentence, belief, nal);
            } else {
                conversion(nal);
            }
        }
    }

    /**
     * Inheritance/Implication matches Similarity/Equivalence
     *
     * @param asym A Inheritance/Implication sentence
     * @param sym A Similarity/Equivalence sentence
     * @param figure location of the shared term
     * @param nal Reference to the memory
     */
    public static void matchAsymSym(final Sentence asym, final Sentence sym, int figure, final NAL nal) {
        if (nal.getCurrentTask().sentence.isJudgment()) {
            inferToAsym(asym, sym, nal);
        } else {
            convertRelation(nal);
        }
    }

    /* -------------------- two-premise logic rules -------------------- */

    /**
     * {<S <-> P>, <P --> S>} |- <S --> P> Produce an Inheritance/Implication
     * from a Similarity/Equivalence and a reversed Inheritance/Implication
     *
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     * @param nal Reference to the memory
     */
    private static void inferToAsym(Sentence asym, Sentence sym, NAL nal) {
        Statement statement = (Statement) asym.term;
        Term sub = statement.getPredicate();
        Term pre = statement.getSubject();
        
        Statement content = Statement.make(statement, sub, pre, statement.getTemporalOrder());
        if (content == null) return;
        
        Truth truth = TruthFunctions.reduceConjunction(sym.truth, asym.truth);
        Budget budget = BudgetFunctions.forward(truth, nal);
        nal.doublePremiseTask(content, truth, budget,
                nal.newStamp(asym, sym),
                false, false);
    }

    /* -------------------- one-premise logic rules -------------------- */
    /**
     * {<P --> S>} |- <S --> P> Produce an Inheritance/Implication from a
     * reversed Inheritance/Implication
     *
     * @param nal Reference to the memory
     */
    private static void conversion(final NAL nal) {
        Truth truth = TruthFunctions.conversion(nal.getCurrentBelief().truth);
        Budget budget = BudgetFunctions.forward(truth, nal);
        convertedJudgment(truth, budget, nal);
    }

    /**
     * {<S --> P>} |- <S <-> P> {<S <-> P>} |- <S --> P> Switch between
     * Inheritance/Implication and Similarity/Equivalence
     *
     * @param nal Reference to the memory
     */
    private static void convertRelation(final NAL nal) {
        Truth truth = nal.getCurrentBelief().truth;
        if (((Compound) nal.getCurrentTask().getTerm()).isCommutative()) {
            truth = TruthFunctions.abduction(truth, 1.0f);
        } else {
            truth = TruthFunctions.deduction(truth, 1.0f);
        }
        Budget budget = BudgetFunctions.forward(truth, nal);
        convertedJudgment(truth, budget, nal);
    }

    /**
     * Convert judgment into different relation
     * <p>
     * called in MatchingRules
     *
     * @param budget The budget value of the new task
     * @param truth The truth value of the new task
     * @param nal Reference to the memory
     */
    private static void convertedJudgment(final Truth newTruth, final Budget newBudget, final NAL nal) {
        Statement content = (Statement) nal.getCurrentTask().getTerm();
        Statement beliefContent = (Statement) nal.getCurrentBelief().term;
        int order = TemporalRules.reverseOrder(beliefContent.getTemporalOrder());
        final Term subjT = content.getSubject();
        final Term predT = content.getPredicate();
        final Term subjB = beliefContent.getSubject();
        final Term predB = beliefContent.getPredicate();
        Term otherTerm;

        if (subjT.hasVarQuery() && predT.hasVarQuery()) {
            throw new RuntimeException("both subj and pred have query; this case is not implemented yet (if it ever occurrs)");
        }
        else if (subjT.hasVarQuery()) {
            otherTerm = (predT.equals(subjB)) ? predB : subjB;
            content = Statement.make(content, otherTerm, predT, order);
        }
        else if (predT.hasVarQuery()) {
            otherTerm = (subjT.equals(subjB)) ? predB : subjB;
            content = Statement.make(content, subjT, otherTerm, order);
        }
        
        if (content != null)
            nal.singlePremiseTask(content, Symbols.JUDGMENT, newTruth, newBudget);
    }

    
}
