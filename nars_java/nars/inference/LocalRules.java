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

import java.util.Arrays;
import nars.core.Events.Solved;
import nars.core.Events.Unsolved;
import nars.core.Memory;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import static nars.inference.TemporalRules.matchingOrder;
import static nars.inference.TemporalRules.reverseOrder;
import nars.io.Output;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Equivalence;
import nars.language.Inheritance;
import nars.language.Similarity;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Variables;


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
    public static boolean match(final Task task, final Sentence belief, final NAL nal) {
        Sentence sentence = task.sentence;
        
        if (sentence.isJudgment()) {
            if (revisible(sentence, belief)) {
                return revision(sentence, belief, true, nal);
            }
        } else {
            if (matchingOrder(sentence, belief)) {
                Term[] u = new Term[] { sentence.content, belief.content };
                if (Variables.unify(Symbols.VAR_QUERY, u)) {
                    trySolution(belief, task, nal);
                }
            }
        }
        return false;
    }

    /**
     * Check whether two sentences can be used in revision
     *
     * @param s1 The first sentence
     * @param s2 The second sentence
     * @return If revision is possible between the two sentences
     */
    public static boolean revisible(final Sentence s1, final Sentence s2) {
        return (s1.getRevisible() && 
                s1.equalsContent(s2) && 
                matchingOrder(s1.getTemporalOrder(), s2.getTemporalOrder()));
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
    public static boolean revision(final Sentence newBelief, final Sentence oldBelief, final boolean feedbackToLinks, final NAL nal) {
        if (newBelief.content==null) return false;
        
        TruthValue newTruth = newBelief.truth;
        TruthValue oldTruth = oldBelief.truth;
        TruthValue truth = TruthFunctions.revision(newTruth, oldTruth);
        BudgetValue budget = BudgetFunctions.revise(newTruth, oldTruth, truth, feedbackToLinks, nal);
        
        if (budget.aboveThreshold()) {
            if (nal.doublePremiseTaskRevised(newBelief.content, truth, budget)) {
                nal.mem().logic.BELIEF_REVISION.commit();
                return true;
            }
        }
        
       return false;
    }


    /**
     * Check if a Sentence provide a better answer to a Question or Goal
     *
     * @param belief The proposed answer
     * @param task The task to be processed
     * @param memory Reference to the memory
     */
    public static boolean trySolution(Sentence belief, final Task task, final NAL nal) {
        Sentence problem = task.sentence;
        Memory memory = nal.mem();
        
        if (!TemporalRules.matchingOrder(problem.getTemporalOrder(), belief.getTemporalOrder())) {
            //System.out.println("Unsolved: Temporal order not matching");
            memory.emit(Unsolved.class, task, belief, "Non-matching temporal Order");
            return false;
        }
        
        Sentence oldBest = task.getBestSolution();
        float newQ = TemporalRules.solutionQuality(problem, belief, memory);
        if (oldBest != null) {
            float oldQ = TemporalRules.solutionQuality(problem, oldBest, memory);
            if (oldQ >= newQ) {
                if (problem.isGoal()) {
                    memory.emotion.adjustHappy(oldQ, task.getPriority());
                }
                //System.out.println("Unsolved: Solution of lesser quality");
                memory.emit(Unsolved.class, task, belief, "Lower quality");               
                return false;
            }
        }
        
        Term content = belief.content;
        if (content.hasVarIndep()) {
            Term u[] = new Term[] { content, problem.content };
            
            boolean unified = Variables.unify(Symbols.VAR_INDEPENDENT, u);            
            content = u[0];
            
            belief = belief.clone(content);
            
            if ((!unified) || (content == null)) {
                throw new RuntimeException("Unification invalid: " + Arrays.toString(u));
            }
            
            Stamp st = new Stamp(belief.stamp, memory.time());
            st.chainAdd(belief.content);
        }

        task.setBestSolution(belief);
        
        memory.logic.SOLUTION_BEST.commit(task.getPriority());

        if (problem.isGoal()) {
            memory.emotion.adjustHappy(newQ, task.getPriority());
        }
        
        BudgetValue budget = TemporalRules.solutionEval(problem, belief, task, nal);
        if ((budget != null) && budget.aboveThreshold()) {                       
            
            //Solution Activated
            if(task.sentence.punctuation==Symbols.QUESTION_MARK || task.sentence.punctuation==Symbols.QUEST_MARK) {
                memory.emit(Solved.class, task, belief);   //solution to quests and questions can be always showed   
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
                        
            nal.addTask(nal.getCurrentTask(), budget, belief, task.getParentBelief());
            return true;
        }
        else {
            memory.emit(Unsolved.class, task, belief, "Insufficient budget");
        }
        return false;
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
        if (matchingOrder(sentence.getTemporalOrder(), reverseOrder(belief.getTemporalOrder()))) {
            if (sentence.isJudgment()) {
                inferToSym(sentence, belief, nal);
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

    /* -------------------- two-premise inference rules -------------------- */
    /**
     * {<S --> P>, <P --> S} |- <S <-> p> Produce Similarity/Equivalence from a
     * pair of reversed Inheritance/Implication
     *
     * @param judgment1 The first premise
     * @param judgment2 The second premise
     * @param nal Reference to the memory
     */
    private static void inferToSym(Sentence judgment1, Sentence judgment2, NAL nal) {
        Statement s1 = (Statement) judgment1.content;
        Term t1 = s1.getSubject();
        Term t2 = s1.getPredicate();
        Term content;
        if (s1 instanceof Inheritance) {
            content = Similarity.make(t1, t2);
        } else {
            content = Equivalence.make(t1, t2, s1.getTemporalOrder());
        }
        TruthValue value1 = judgment1.truth;
        TruthValue value2 = judgment2.truth;
        TruthValue truth = TruthFunctions.intersection(value1, value2);
        BudgetValue budget = BudgetFunctions.forward(truth, nal);
        nal.doublePremiseTask(content, truth, budget,false);
    }

    /**
     * {<S <-> P>, <P --> S>} |- <S --> P> Produce an Inheritance/Implication
     * from a Similarity/Equivalence and a reversed Inheritance/Implication
     *
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     * @param nal Reference to the memory
     */
    private static void inferToAsym(Sentence asym, Sentence sym, NAL nal) {
        Statement statement = (Statement) asym.content;
        Term sub = statement.getPredicate();
        Term pre = statement.getSubject();
        
        Statement content = Statement.make(statement, sub, pre, statement.getTemporalOrder());
        if (content == null) return;
        
        TruthValue truth = TruthFunctions.reduceConjunction(sym.truth, asym.truth);
        BudgetValue budget = BudgetFunctions.forward(truth, nal);
        nal.doublePremiseTask(content, truth, budget,false);
    }

    /* -------------------- one-premise inference rules -------------------- */
    /**
     * {<P --> S>} |- <S --> P> Produce an Inheritance/Implication from a
     * reversed Inheritance/Implication
     *
     * @param nal Reference to the memory
     */
    private static void conversion(final NAL nal) {
        TruthValue truth = TruthFunctions.conversion(nal.getCurrentBelief().truth);
        BudgetValue budget = BudgetFunctions.forward(truth, nal);
        convertedJudgment(truth, budget, nal);
    }

    /**
     * {<S --> P>} |- <S <-> P> {<S <-> P>} |- <S --> P> Switch between
     * Inheritance/Implication and Similarity/Equivalence
     *
     * @param nal Reference to the memory
     */
    private static void convertRelation(final NAL nal) {
        TruthValue truth = nal.getCurrentBelief().truth;
        if (((CompoundTerm) nal.getCurrentTask().getContent()).isCommutative()) {
            truth = TruthFunctions.abduction(truth, 1.0f);
        } else {
            truth = TruthFunctions.deduction(truth, 1.0f);
        }
        BudgetValue budget = BudgetFunctions.forward(truth, nal);
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
    private static void convertedJudgment(final TruthValue newTruth, final BudgetValue newBudget, final NAL nal) {
        Statement content = (Statement) nal.getCurrentTask().getContent();
        Statement beliefContent = (Statement) nal.getCurrentBelief().content;
        int order = TemporalRules.reverseOrder(beliefContent.getTemporalOrder());
        final Term subjT = content.getSubject();
        final Term predT = content.getPredicate();
        final Term subjB = beliefContent.getSubject();
        final Term predB = beliefContent.getPredicate();
        Term otherTerm;
        if (subjT.hasVarQuery()) {
            otherTerm = (predT.equals(subjB)) ? predB : subjB;
            content = Statement.make(content, otherTerm, predT, order);
        }
        if (predT.hasVarQuery()) {
            otherTerm = (subjT.equals(subjB)) ? predB : subjB;
            content = Statement.make(content, subjT, otherTerm, order);
        }
        
        if (content == null) return;
        
        nal.singlePremiseTask(content, Symbols.JUDGMENT_MARK, newTruth, newBudget);
    }

    
}
