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

import com.gs.collections.impl.tuple.Tuples;
import nars.Global;
import nars.Memory;
import nars.Op;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.nal2.NAL2;
import nars.nal.nal7.TemporalRules;
import nars.premise.Premise;
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
import nars.truth.ProjectedTruth;
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
     * @param newBelief The first sentence
     * @param oldBelief The second sentence
     * @return If revision is possible between the two sentences
     */
    public static boolean revisible(final Sentence newBelief, final Sentence oldBelief) {
        if (newBelief.isRevisible())
            if (newBelief.equalTerms(oldBelief))
                if (TemporalRules.matchingOrder(newBelief.getTemporalOrder(), oldBelief.getTemporalOrder())) {
                    return true;
                }

        return false;
    }

    /**
     * avoids term comparison if the two inputs are already known to have equal terms
     */
    public static boolean revisibleTermsAlreadyEqual(final Sentence newBelief, final Sentence oldBelief) {
        if (newBelief.isRevisible()) {
            if (TemporalRules.matchingOrder(newBelief.getTemporalOrder(), oldBelief.getTemporalOrder()))
                return true;
        }

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

     */
    public static Task revision(final Task newBelief, final Task oldBelief, final boolean feedbackToLinks, final NAL nal) {
        //Stamper stamp = nal.newStampIfNotOverlapping(newBelief, oldBelief);
        //if (stamp == null) return null;

        if (Stamp.overlapping(newBelief, oldBelief))
            return null;

        Truth newBeliefTruth = newBelief.getTruth();
        Truth oldBeliefTruth = oldBelief.getTruth();
        Truth truth = TruthFunctions.revision(newBeliefTruth, oldBeliefTruth);
        Budget budget = BudgetFunctions.revise(newBeliefTruth, oldBeliefTruth, truth, nal);

        Task revised = nal.derive(nal.newTask(newBelief.getTerm())
                        .punctuation(newBelief.getPunctuation())
                        .truth(truth)
                        .budget(budget)
                        .parent(newBelief),
                true);

        if (revised != null)
            nal.memory().logic.BELIEF_REVISION.hit();

        return revised;
    }


    public static <T extends Compound> Task<T> tryRevision(final Task<T> newBelief, final Task<T> oldBelief, final boolean feedbackToLinks, final Premise nal) {
        //Stamper stamp = nal.newStampIfNotOverlapping(newBelief.sentence, oldBelief);
        //if (stamp == null) return null;

        if (newBelief.equals(oldBelief))
            return null;

        if (Stamp.overlapping(newBelief, oldBelief))
            return null;

        Truth newBeliefTruth = newBelief.getTruth();
        ProjectedTruth oldBeliefTruth = oldBelief.projection(nal.time(), newBelief.getOccurrenceTime());
        Truth truth = TruthFunctions.revision(newBeliefTruth, oldBeliefTruth);
        Budget budget = BudgetFunctions.revise(newBeliefTruth, oldBeliefTruth, truth, nal);


        Task<T> revised = nal.derive( nal.newTask(newBelief.getTerm())
                        .punctuation(newBelief.getPunctuation())
                        .truth(truth)
                        .budget(budget)
                        .parent(newBelief, oldBelief)
                        .reason("Revision"
                                //+Arrays.toString(newBelief.getEvidentialSet()) + ":" +
                                //Arrays.toString(oldBelief.getEvidentialSet())
                        )
                    ,true).normalized(); //.normalized();

        if (revised != null) {
            nal.memory().logic.BELIEF_REVISION.hit();
        }

        return revised;
    }

    public static Task trySolution(Task belief, final Task questionTask, final Premise nal) {
        if (belief == null) return null;

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
    public static Task trySolution(Task belief, final Truth projectedTruth, final Task question, final Premise nal) {

        if (belief == null) return null;


        final Task inputBelief = belief;

        Memory memory = nal.memory();
        Term content = belief.getTerm();

        if (!TemporalRules.matchingOrder(question, belief)) {
            //System.out.println("Unsolved: Temporal order not matching");
            //memory.emit(Unsolved.class, task, belief, "Non-matching temporal Order");
            return null;
        }


        final long now = memory.time();

        /** temporary for comparing the result before unification and after */
        //float newQ0 = TemporalRules.solutionQuality(question, belief, projectedTruth, now);

        if (content.hasVarIndep() && !content.equals(question.getTerm())) {

            Term u[] = new Term[]{content, question.getTerm()};

            boolean unified = Variables.unify(Op.VAR_INDEPENDENT, u, nal.getRandom());

            if (unified) {

                content = u[0];

                belief = belief.clone((Compound) content, projectedTruth, false);
                if (belief == null) {
                    throw new RuntimeException("Unification invalid: " + Arrays.toString(u) + " while cloning into " + belief);
                }

                //float newQ1 = TemporalRules.solutionQuality(question, belief, projectedTruth, now);
                //System.err.println(" before unf: " + newQ0 + " , after " + newQ1);
                //System.err.println();
            }
        } else {
            belief = belief.clone(projectedTruth, false);
        }

        float newQ = TemporalRules.solutionQuality(question, belief, projectedTruth, now);
//        if (newQ == 0) {
//            memory.emotion.happy(0, questionTask, nal);
//            return null;
//        }


        Sentence oldBest = question.getBestSolution();
        if (oldBest != null) {
            float oldQ = TemporalRules.solutionQuality(question, oldBest, now);
            if (oldQ >= newQ) {
                //if (question.isGoal()) {
                    //memory.emotion.happy(oldQ, question, nal);
                //}
                //System.out.println("Unsolved: Solution of lesser quality");
                //memory.emit(Unsolved.class, task, belief, "Lower quality");
                return null;
            }
        }


        question.setBestSolution(memory, belief);

        memory.logic.SOLUTION_BEST.set(newQ);

        //if (question.isGoal()) {
            memory.emotion.happy(newQ, question, nal);
        //}

        Budget budget = TemporalRules.solutionEval(question, belief, question, nal);


        /*memory.output(task);

        //only questions and quests get here because else output is spammed
        if(task.sentence.isQuestion() || task.sentence.isQuest()) {
            memory.emit(Solved.class, task, belief);
        } else {
            memory.emit(Output.class, task, belief);
        }*/

        //nal.addSolution(nal.getCurrentTask(), budget, belief, task);

        //.reason(currentTask.getHistory())

        if (belief!=inputBelief) {
            //it was either unified and/or projected:
            /*belief = nal.addNewTask(nal.newTask(belief.getTerm(), belief.getPunctuation())
                            .truth(belief.getTruth())
                            .budget(budget)
                            .parent(belief) //.parent(questionTask, questionTask.getParentBelief())
                            .occurr(belief.getOccurrenceTime())

                            .solution(belief),
                    "Adjusted Solution",
                    true, false, false);*/
            nal.nar().input(belief);
        }
        else {
            belief.getBudget().accumulate(budget);

            if (Global.DEBUG_TASK_LOG_SOLUTION)
                belief.logUnrepeated("Solution" /*Global.DEBUG ? "Solution " + question : */);
        }

        question.getBudget().decPriority(budget.getPriority());

        //Solution Activated
//        if (question.isQuestOrQuestion()) {
//            //if (questionTask.isInput()) { //only show input tasks as solutions
            memory.eventAnswer.emit(Tuples.twin(belief, question));
//        } else {
//            memory.eventAnswer.emit(Tuples.twin(belief, question));
//        }

        return belief;
    }


    /* -------------------- same terms, difference relations -------------------- */

    /**
     * The task and belief match reversely
     *
     * @param p Reference to the memory
     */
    public static Task matchReverse(final Premise p) {
        final Task task = p.getTask();
        final Task belief = p.getBelief();

        if (TemporalRules.matchingOrder(task.getTemporalOrder(), TemporalRules.reverseOrder(belief.getTemporalOrder()))) {
            if (task.isJudgment()) {
                return NAL2.inferToSym(task, belief, p);
            } else {
                return conversion(p);
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
     * @param p    Reference to the memory
     */
    public static void matchAsymSym(final Task asym, final Task sym, int figure, final Premise p) {
        if (p.getTask().isJudgment()) {
            inferToAsym(asym, sym, p);
        } else {
            convertRelation(p);
        }
    }

    /* -------------------- two-premise logic rules -------------------- */

    /**
     * {<S <-> P>, <P --> S>} |- <S --> P> Produce an Inheritance/Implication
     * from a Similarity/Equivalence and a reversed Inheritance/Implication
     *
     * @param asym The asymmetric premise
     * @param sym  The symmetric premise
     * @param p  Reference to the memory
     */
    private static Task inferToAsym(Task asym, Task sym, Premise p) {
        TaskSeed s = p.newDoublePremise(asym, sym);
        if (s == null)
            return null;

        Statement statement = (Statement) asym.getTerm();
        Term sub = statement.getPredicate();
        Term pre = statement.getSubject();

        Statement content = Statement.make(statement, sub, pre, statement.getTemporalOrder());
        if (content == null) return null;

        Truth truth = TruthFunctions.reduceConjunction(sym.getTruth(), asym.getTruth());


            return p.deriveDouble(
                    s.term(content)
                            .punctuation(asym.getPunctuation())
                            .truth(truth)
                            .budget(BudgetFunctions.forward(truth, p)),
                    false);



    }

    /* -------------------- one-premise logic rules -------------------- */

    /**
     * {<P --> S>} |- <S --> P> Produce an Inheritance/Implication from a
     * reversed Inheritance/Implication
     *
     * @param p Reference to the memory
     */
    private static Task conversion(final Premise p) {
        Truth truth = TruthFunctions.conversion(p.getBelief().getTruth());
        Budget budget = BudgetFunctions.forward(truth, p);
        return convertedJudgment(truth, budget, p);
    }

    /**
     * {<S --> P>} |- <S <-> P> {<S <-> P>} |- <S --> P> Switch between
     * Inheritance/Implication and Similarity/Equivalence
     *
     * @param p Reference to the memory
     */
    private static Task convertRelation(final Premise p) {
        final Truth beliefTruth = p.getBelief().getTruth();
        final AnalyticTruth truth;
        if ((p.getTask().getTerm()).isCommutative()) {
            truth = TruthFunctions.abduction(beliefTruth, 1.0f);
        } else {
            truth = TruthFunctions.deduction(beliefTruth, 1.0f);
        }
        if (truth != null) {
            Budget budget = BudgetFunctions.forward(truth, p);
            return convertedJudgment(truth, budget, p);
        }
        return null;
    }

    /**
     * Convert judgment into different relation
     * <p>
     * called in MatchingRules
     *
     * @param budget The budget value of the new task
     * @param truth  The truth value of the new task
     * @param p    Reference to the memory
     */
    private static Task convertedJudgment(final Truth newTruth, final Budget newBudget, final Premise p) {
        Statement content = (Statement) p.getTask().getTerm();
        Statement beliefContent = (Statement) p.getBelief().getTerm();
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
            return p.deriveSingle(content, Symbols.JUDGMENT, newTruth, newBudget);

        return null;
    }


}
