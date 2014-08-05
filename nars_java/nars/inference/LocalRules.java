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
package nars.inference;

import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Output.OUT;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Equivalence;
import nars.language.Inheritance;
import nars.language.Similarity;
import nars.language.Statement;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import static nars.language.Terms.*;
import nars.language.Variables;
import nars.storage.Memory;


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

    /* -------------------- same contents -------------------- */
    /**
     * The task and belief have the same content
     * <p>
     * called in RuleTables.reason
     *
     * @param task The task
     * @param belief The belief
     * @param memory Reference to the memory
     */
    public static void match(final Task task, final Sentence belief, final Memory memory) {
        Sentence sentence = (Sentence) task.sentence.clone();
        if (TemporalRules.matchingOrder(sentence.getTemporalOrder(), belief.getTemporalOrder())) {
            if (sentence.isJudgment()) {
                if (revisible(sentence, belief)) {
                    revision(sentence, belief, true, memory);
                }
            } else if (Variables.unify(Symbols.VAR_QUERY, sentence.content, (Term) belief.content.clone())) {
                trySolution(belief, task, memory);
            }
        }
    }

    /**
     * Check whether two sentences can be used in revision
     *
     * @param s1 The first sentence
     * @param s2 The second sentence
     * @return If revision is possible between the two sentences
     */
    public static boolean revisible(final Sentence s1, final Sentence s2) {
        return (s1.content.equals(s2.content) && s1.getRevisible()
                && TemporalRules.matchingOrder(s1.getTemporalOrder(), s2.getTemporalOrder()));
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
    public static void revision(final Sentence newBelief, final Sentence oldBelief, final boolean feedbackToLinks, final Memory memory) {
        TruthValue newTruth = newBelief.truth;
        TruthValue oldTruth = oldBelief.truth;
        TruthValue truth = TruthFunctions.revision(newTruth, oldTruth);
        BudgetValue budget = BudgetFunctions.revise(newTruth, oldTruth, truth, feedbackToLinks, memory);
        Term content = newBelief.content;
        memory.doublePremiseTaskRevised(content, truth, budget);
    }

    @Deprecated public static float solutionQuality(final Sentence problem, final Sentence solution, Memory memory) {
        //moved to BudgetFunctions.java
        throw new RuntimeException("Moved to TemporalRules.java");
    }
    
    /**
     * Check if a Sentence provide a better answer to a Question or Goal
     *
     * @param belief The proposed answer
     * @param task The task to be processed
     * @param memory Reference to the memory
     */
    public static void trySolution(Sentence belief, final Task task, final Memory memory) {
        Sentence problem = task.sentence;
        if (TemporalRules.matchingOrder(problem.getTemporalOrder(), belief.getTemporalOrder())) {
            Sentence oldBest = task.getBestSolution();
            float newQ = TemporalRules.solutionQuality(problem, belief, memory);
            if (oldBest != null) {
                float oldQ = TemporalRules.solutionQuality(problem, oldBest, memory);
                if (oldQ >= newQ) {
                    return;
                }
            }
            Term content = belief.cloneContent();
            if (Variables.containVarIndep(content.getName())) {
                Variables.unify(Symbols.VAR_INDEPENDENT, content, problem.cloneContent());
                belief = belief.clone(content);
                Stamp st = new Stamp(belief.stamp, memory.getTime());
                st.addToChain(belief.content);
            }
            task.setBestSolution(belief);
            if (task.isInput()) {    // moved from Sentence
                memory.output(OUT.class, belief);
            }
            BudgetValue budget = TemporalRules.solutionEval(problem, belief, task, memory);
            if ((budget != null) && budget.aboveThreshold()) {
                memory.activatedTask(budget, belief, task.getParentBelief());
            }
        }
    }


    /* -------------------- same terms, difference relations -------------------- */
    /**
     * The task and belief match reversely
     *
     * @param memory Reference to the memory
     */
    public static void matchReverse(final Memory memory) {
        Task task = memory.getCurrentTask();
        Sentence belief = memory.getCurrentBelief();
        Sentence sentence = task.sentence;
        if (TemporalRules.matchingOrder(sentence.getTemporalOrder(),
                TemporalRules.reverseOrder(belief.getTemporalOrder()))) {
            if (sentence.isJudgment()) {
                inferToSym(sentence, belief, memory);
            } else {
                conversion(memory);
            }
        }
    }

    /**
     * Inheritance/Implication matches Similarity/Equivalence
     *
     * @param asym A Inheritance/Implication sentence
     * @param sym A Similarity/Equivalence sentence
     * @param figure location of the shared term
     * @param memory Reference to the memory
     */
    public static void matchAsymSym(final Sentence asym, final Sentence sym, int figure, final Memory memory) {
        if (memory.getCurrentTask().sentence.isJudgment()) {
            inferToAsym(asym, sym, memory);
        } else {
            convertRelation(memory);
        }
    }

    /* -------------------- two-premise inference rules -------------------- */
    /**
     * {<S --> P>, <P --> S} |- <S <-> p> Produce Similarity/Equivalence from a
     * pair of reversed Inheritance/Implication
     *
     * @param judgment1 The first premise
     * @param judgment2 The second premise
     * @param memory Reference to the memory
     */
    private static void inferToSym(Sentence judgment1, Sentence judgment2, Memory memory) {
        Statement s1 = (Statement) judgment1.content;
        Term t1 = s1.getSubject();
        Term t2 = s1.getPredicate();
        Term content;
        if (s1 instanceof Inheritance) {
            content = Similarity.make(t1, t2, memory);
        } else {
            content = Equivalence.make(t1, t2, s1.getTemporalOrder(), memory);
        }
        TruthValue value1 = judgment1.truth;
        TruthValue value2 = judgment2.truth;
        TruthValue truth = TruthFunctions.intersection(value1, value2);
        BudgetValue budget = BudgetFunctions.forward(truth, memory);
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * {<S <-> P>, <P --> S>} |- <S --> P> Produce an Inheritance/Implication
     * from a Similarity/Equivalence and a reversed Inheritance/Implication
     *
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     * @param memory Reference to the memory
     */
    private static void inferToAsym(Sentence asym, Sentence sym, Memory memory) {
        Statement statement = (Statement) asym.content;
        Term sub = statement.getPredicate();
        Term pre = statement.getSubject();
        Statement content = Statement.make(statement, sub, pre, statement.getTemporalOrder(), memory);
        TruthValue truth = TruthFunctions.reduceConjunction(sym.truth, asym.truth);
        BudgetValue budget = BudgetFunctions.forward(truth, memory);
        memory.doublePremiseTask(content, truth, budget);
    }

    /* -------------------- one-premise inference rules -------------------- */
    /**
     * {<P --> S>} |- <S --> P> Produce an Inheritance/Implication from a
     * reversed Inheritance/Implication
     *
     * @param memory Reference to the memory
     */
    private static void conversion(final Memory memory) {
        TruthValue truth = TruthFunctions.conversion(memory.getCurrentBelief().truth);
        BudgetValue budget = BudgetFunctions.forward(truth, memory);
        convertedJudgment(truth, budget, memory);
    }

    /**
     * {<S --> P>} |- <S <-> P> {<S <-> P>} |- <S --> P> Switch between
     * Inheritance/Implication and Similarity/Equivalence
     *
     * @param memory Reference to the memory
     */
    private static void convertRelation(final Memory memory) {
        TruthValue truth = memory.getCurrentBelief().truth;
        if (((CompoundTerm) memory.getCurrentTask().getContent()).isCommutative()) {
            truth = TruthFunctions.abduction(truth, 1.0f);
        } else {
            truth = TruthFunctions.deduction(truth, 1.0f);
        }
        BudgetValue budget = BudgetFunctions.forward(truth, memory);
        convertedJudgment(truth, budget, memory);
    }

    /**
     * Convert judgment into different relation
     * <p>
     * called in MatchingRules
     *
     * @param budget The budget value of the new task
     * @param truth The truth value of the new task
     * @param memory Reference to the memory
     */
    private static void convertedJudgment(final TruthValue newTruth, final BudgetValue newBudget, final Memory memory) {
        Statement content = (Statement) memory.getCurrentTask().getContent();
        Statement beliefContent = (Statement) memory.getCurrentBelief().content;
        int order = TemporalRules.reverseOrder(beliefContent.getTemporalOrder());
        final Term subjT = content.getSubject();
        final Term predT = content.getPredicate();
        final Term subjB = beliefContent.getSubject();
        final Term predB = beliefContent.getPredicate();
        Term otherTerm;
        if (Variables.containVarQuery(subjT.getName())) {
            otherTerm = (predT.equals(subjB)) ? predB : subjB;
            content = Statement.make(content, otherTerm, predT, order, memory);
        }
        if (Variables.containVarQuery(predT.getName())) {
            otherTerm = (subjT.equals(subjB)) ? predB : subjB;
            content = Statement.make(content, subjT, otherTerm, order, memory);
        }
        memory.singlePremiseTask(content, Symbols.JUDGMENT_MARK, newTruth, newBudget);
    }
    
    /** Add plausibility estimation */
    public static void decisionMaking(Task task, Concept concept) {
         Term content = concept.term;
         TruthValue desireValue = concept.getDesire();
         if (desireValue.getExpectation() < Parameters.DECISION_THRESHOLD) {
             return;
         }
         if (!(content instanceof Operation)) {
             return;
         }
         Operator oper = ((Operation) content).getOperator();
         oper.call(task, concept.memory);
         task.setPriority(0);
    }    
}
