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
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.io.out.Output;
import nars.nal.nal2.NAL2;
import nars.nal.nal7.TemporalRules;
import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.Statement;
import nars.term.Term;
import nars.term.Variables;
import nars.truth.AnalyticTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.Arrays;


/**
 * Directly process a task by a oldBelief, with only two Terms in both. In
 * matching, the new task is compared with an existing direct Task in that
 * Concept, to carry out:
 * <p>
 * revision: between judgments or goals on non-overlapping evidence;
 * satisfy: between a Sentence and a Question/Goal;
 * merge: between items of the same type and stamp;
 * conversion: between different inheritance relations.
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
     * @param newBelief       The new belief in task
     * @param oldBelief       The previous belief with the same content
     * @param feedbackToLinks Whether to send feedback to the links
     * @param memory          Reference to the memory
     */
    public static Task revision(final Task newBelief, final Sentence oldBelief, final boolean feedbackToLinks, final NAL nal) {
        //Stamper stamp = nal.newStampIfNotOverlapping(newBelief, oldBelief);
        //if (stamp == null) return null;

        if (Stamp.evidentialSetOverlaps(newBelief, oldBelief))
            return null;

        Truth newBeliefTruth = newBelief.getTruth();
        Truth oldBeliefTruth = oldBelief.truth;
        Truth truth = TruthFunctions.revision(newBeliefTruth, oldBeliefTruth);
        Budget budget = BudgetFunctions.revise(newBeliefTruth, oldBeliefTruth, truth, nal);

        Task revised = nal.derive(nal.newTask(newBelief.getTerm())
                        .punctuation(newBelief.getPunctuation())
                        .truth(truth)
                        .budget(budget)
                        .parent(newBelief),
                true, true);

        if (revised != null)
            nal.memory.logic.BELIEF_REVISION.hit();

        return revised;
    }


    public static Task tryRevision(final Task newBelief, Task oldBeliefTask, final boolean feedbackToLinks, final NAL nal) {
        //Stamper stamp = nal.newStampIfNotOverlapping(newBelief.sentence, oldBelief);
        //if (stamp == null) return null;

        if (newBelief.equals(oldBeliefTask))
            return null;

        if (Stamp.evidentialSetOverlaps(newBelief, oldBeliefTask))
            return null;

        Sentence oldBelief = oldBeliefTask.sentence;

        Truth newBeliefTruth = newBelief.getTruth();
        Truth oldBeliefTruth = oldBelief.projection(nal.time(), newBelief.getOccurrenceTime());
        Truth truth = TruthFunctions.revision(newBeliefTruth, oldBeliefTruth);
        Budget budget = BudgetFunctions.revise(newBeliefTruth, oldBeliefTruth, truth, nal);

        Task revised = nal.derive(nal.newTask(newBelief.getTerm())
                        .punctuation(newBelief.getPunctuation())
                        .truth(truth)
                        .budget(budget)
                        .parent(oldBeliefTask),
                true, true);

        if (revised != null)
            nal.memory.logic.BELIEF_REVISION.hit();

        return revised;
    }

    public static Task trySolution(Task belief, final Task questionTask, final NAL nal) {
        return trySolution(belief, belief.getTruth(), questionTask, nal);
    }

    /**
     * Check if a Sentence provide a better answer to a Question or Goal
     *
     * @param belief       The proposed answer
     * @param questionTask The task to be processed
     * @param memory       Reference to the memory
     * @return the projected Task, or the original Task
     */
    public static Task trySolution(Task belief, final Truth projectedTruth, final Task questionTask, final NAL nal) {

        if (belief == null) return null;


        Sentence question = questionTask.sentence;
        Memory memory = nal.memory;

        if (!TemporalRules.matchingOrder(question, belief)) {
            //System.out.println("Unsolved: Temporal order not matching");
            //memory.emit(Unsolved.class, task, belief, "Non-matching temporal Order");
            return null;
        }


        final long now = memory.time();
        Sentence oldBest = questionTask.getBestSolution();
        float newQ = TemporalRules.solutionQuality(question, belief, projectedTruth, now);
        if (oldBest != null) {
            float oldQ = TemporalRules.solutionQuality(question, oldBest, now);
            if (oldQ >= newQ) {
                if (question.isGoal()) {
                    memory.emotion.happy(oldQ, questionTask, nal);
                }
                //System.out.println("Unsolved: Solution of lesser quality");
                //memory.emit(Unsolved.class, task, belief, "Lower quality");
                return null;
            }
        }

        Term content = belief.getTerm();
        if (content.hasVarIndep()) {
            Term u[] = new Term[]{content, question.getTerm()};

            boolean unified = Variables.unify(Symbols.VAR_INDEPENDENT, u, nal.memory.random);
            if (!unified) return null;

            content = u[0];

            belief = belief.clone((Compound) content, projectedTruth);
            if (belief == null) {
                throw new RuntimeException("Unification invalid: " + Arrays.toString(u) + " while cloning into " + belief);
                //return false;
            }
        }
        else {
            belief = belief.clone(projectedTruth);
        }

        questionTask.setBestSolution(memory, belief);

        memory.logic.SOLUTION_BEST.set(questionTask.getPriority());

        if (question.isGoal()) {
            memory.emotion.happy(newQ, questionTask, nal);
        }

        Budget budget = TemporalRules.solutionEval(question, belief, questionTask, nal);

            



        /*memory.output(task);

        //only questions and quests get here because else output is spammed
        if(task.sentence.isQuestion() || task.sentence.isQuest()) {
            memory.emit(Solved.class, task, belief);
        } else {
            memory.emit(Output.class, task, belief);
        }*/

        //nal.addSolution(nal.getCurrentTask(), budget, belief, task);
        Task solutionTask = nal.addSolution(questionTask, budget, belief, questionTask);
        if (solutionTask != null) {
            //Solution Activated
            if (questionTask.sentence.punctuation == Symbols.QUESTION || questionTask.sentence.punctuation == Symbols.QUEST) {
                //if (questionTask.isInput()) { //only show input tasks as solutions
                memory.emit(Answer.class, solutionTask, questionTask);
            /*} else {
                memory.emit(Output.class, task, belief);   //solution to quests and questions can be always showed
            }*/
            } else {
                memory.emit(Output.class, solutionTask, questionTask);   //goal things only show silence related
            }

        }

        return belief;


    }


    /* -------------------- same terms, difference relations -------------------- */

    /**
     * The task and belief match reversely
     *
     * @param nal Reference to the memory
     */
    public static Task matchReverse(final NAL nal) {
        Task task = nal.getCurrentTask();
        Sentence belief = nal.getCurrentBelief();
        Sentence sentence = task.sentence;
        if (TemporalRules.matchingOrder(sentence.getTemporalOrder(), TemporalRules.reverseOrder(belief.getTemporalOrder()))) {
            if (sentence.isJudgment()) {
                return NAL2.inferToSym(task, belief, nal);
            } else {
                return conversion(nal);
            }
        }
        return null;
    }

    /**
     * Inheritance/Implication matches Similarity/Equivalence
     *
     * @param asym   A Inheritance/Implication sentence
     * @param sym    A Similarity/Equivalence sentence
     * @param figure location of the shared term
     * @param nal    Reference to the memory
     */
    public static void matchAsymSym(final Task asym, final Sentence sym, int figure, final NAL nal) {
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
     *  @param asym The asymmetric premise
     * @param sym  The symmetric premise
     * @param nal  Reference to the memory
     */
    private static Task inferToAsym(Task asym, Sentence sym, NAL nal) {
        TaskSeed s = nal.newDoublePremise(asym, sym);
        if (s == null)
            return null;

        Statement statement = (Statement) asym.getTerm();
        Term sub = statement.getPredicate();
        Term pre = statement.getSubject();

        Statement content = Statement.make(statement, sub, pre, statement.getTemporalOrder());
        if (content == null) return null;

        AnalyticTruth truth = TruthFunctions.reduceConjunction(sym.truth, asym.getTruth());

        if (truth!=null) {
            return nal.deriveDouble(
                    s.term(content)
                            .punctuation(asym.getPunctuation())
                            .truth(truth)
                            .budget(BudgetFunctions.forward(truth, nal)),
                    false, false);
        }

        return null;
    }

    /* -------------------- one-premise logic rules -------------------- */

    /**
     * {<P --> S>} |- <S --> P> Produce an Inheritance/Implication from a
     * reversed Inheritance/Implication
     *
     * @param nal Reference to the memory
     */
    private static Task conversion(final NAL nal) {
        Truth truth = TruthFunctions.conversion(nal.getCurrentBelief().truth);
        Budget budget = BudgetFunctions.forward(truth, nal);
        return convertedJudgment(truth, budget, nal);
    }

    /**
     * {<S --> P>} |- <S <-> P> {<S <-> P>} |- <S --> P> Switch between
     * Inheritance/Implication and Similarity/Equivalence
     *
     * @param nal Reference to the memory
     */
    private static Task convertRelation(final NAL nal) {
        final Truth beliefTruth = nal.getCurrentBelief().truth;
        final AnalyticTruth truth;
        if ((nal.getCurrentTask().getTerm()).isCommutative()) {
            truth = TruthFunctions.abduction(beliefTruth, 1.0f);
        } else {
            truth = TruthFunctions.deduction(beliefTruth, 1.0f);
        }
        if (truth!=null) {
            Budget budget = BudgetFunctions.forward(truth, nal);
            return convertedJudgment(truth, budget, nal);
        }
        return null;
    }

    /**
     * Convert judgment into different relation
     * <p>
     * called in MatchingRules
     *  @param budget The budget value of the new task
     * @param truth  The truth value of the new task
     * @param nal    Reference to the memory
     */
    private static Task convertedJudgment(final Truth newTruth, final Budget newBudget, final NAL nal) {
        Statement content = (Statement) nal.getCurrentTask().getTerm();
        Statement beliefContent = (Statement) nal.getCurrentBelief().getTerm();
        int order = TemporalRules.reverseOrder(beliefContent.getTemporalOrder());
        final Term subjT = content.getSubject();
        final Term predT = content.getPredicate();
        final Term subjB = beliefContent.getSubject();
        final Term predB = beliefContent.getPredicate();
        Term otherTerm;

        if (subjT.hasVarQuery() && predT.hasVarQuery()) {
            //System.err.println("both subj and pred have query; this case is not implemented yet (if it ever occurrs)");
            //throw new RuntimeException("both subj and pred have query; this case is not implemented yet (if it ever occurrs)");
        } else if (subjT.hasVarQuery()) {
            otherTerm = (predT.equals(subjB)) ? predB : subjB;
            content = Statement.make(content, otherTerm, predT, order);
        } else if (predT.hasVarQuery()) {
            otherTerm = (subjT.equals(subjB)) ? predB : subjB;
            content = Statement.make(content, subjT, otherTerm, order);
        }

        if (content != null)
            return nal.deriveSingle(content, Symbols.JUDGMENT, newTruth, newBudget);

        return null;
    }


}
