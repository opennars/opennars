/*
 * MatchingRules.java
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
import com.googlecode.opennars.language.*;
import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.*;

/**
 * Directly process a task by a belief, with only two Terms in both
 */
public class MatchingRules {
	
	private Memory memory;
	private BudgetFunctions budgetfunctions;
	
	public MatchingRules(Memory memory) {
		this.memory = memory;
		this.budgetfunctions = new BudgetFunctions(memory);
	}
    
    /* -------------------- same contents -------------------- */
    
    // the task and belief match each other
    // forward inference only
    // called only for Figure 0 of syllogism, in ThreeTermRules
    public  void match(Task task, Judgement belief) {
        Sentence sentence = task.getSentence();
        if (sentence.isJudgment())
            revision(task, belief, true);
        else
            trySolution(sentence, belief, null);
    }
    
    // to be rewritten
    public  void update(Task newBelief, Judgement oldBelief) {
//        Base label = Base.make(newBelief.getBase(), oldBelief.getBase());
//        if ((label == null) || (label.length() == 0))
//            return;
//        Task updatedNewBelief = (Task) newBelief.clone();
//        Task updatedOldBelief = (Task) oldBelief.clone();
//        Term content = oldBelief.getContent();
//        Term toPast = Past.make(content);
//        updatedOldBelief.setContent(toPast);
//        updatedNewBelief.setBase(label);
//        TruthValue truth = TruthFunctions.revision(newBelief.getTruth(), oldBelief.getTruth());
//        float confidence = truth.getConfidence();
//        updatedNewBelief.setConfidence(confidence);
//        BudgetValue usage = this.budgetfunctions.update(newBelief, oldBelief);
//        updatedNewBelief.setBudget(usage);
//        this.memory.derivedTask(updatedNewBelief);
//        this.memory.derivedTask(updatedOldBelief);
    }
    
    // called from Concept (direct) and match (indirect)
    public  void revision(Task task, Judgement belief, boolean feedbackToLinks) {
        Judgement judgement = (Judgement) task.getSentence();
        TruthValue tTruth = judgement.getTruth();
        TruthValue bTruth = belief.getTruth();
        TruthValue truth = TruthFunctions.revision(tTruth, bTruth);
        BudgetValue budget = this.budgetfunctions.revise(tTruth, bTruth, truth, task, feedbackToLinks);
        Term content = judgement.getContent();
        this.memory.doublePremiseTask(budget, content, truth);
    }

    /**
     * Check if a Judgement provide a better answer to a Question
     * @param task The task to be processed
     */
    public  void trySolution(Sentence problem, Judgement belief, Task task) {
        Judgement oldBest = problem.getBestSolution();
        if (betterSolution(belief, oldBest, problem)) {
            problem.setBestSolution(belief);
            BudgetValue budget = this.budgetfunctions.solutionEval(problem, belief, task);
            if (budget != null)
                this.memory.activatedTask(budget, belief, problem.isInput());
        }
    }
    
    // s1 is a better answer to q than s2 is
    private  boolean betterSolution(Judgement newSol, Judgement oldSol, Sentence problem) {
        if (oldSol == null)
            return true;
        else
            return (newSol.solutionQuality(problem) > oldSol.solutionQuality(problem));
    }

    /* -------------------- same terms, difference relations -------------------- */
    
    // the task and belief match each other reversely
    // forward inference only
    // called only for Figure 5 of syllogism
    public  void matchReverse() {
        Task task = this.memory.currentTask;
        Judgement belief = this.memory.currentBelief;
        if (task.getContent().getTemporalOrder() != CompoundTerm.temporalReverse(belief.getContent().getTemporalOrder()))
            return;
        Sentence sentence = task.getSentence();
        if (sentence.isJudgment())
            inferToSym((Judgement) sentence, belief);
        else
            conversion();
    }
    
    // Inheritance matches Similarity
    // forward inference only
    // called from ThreeTermRules only
    public  void matchAsymSym(Sentence asym, Sentence sym, int figure) { // (Task task, Sentence belief, int order, boolean inhSim) {
        CompoundTerm.TemporalOrder order1 = asym.getContent().getTemporalOrder();
        CompoundTerm.TemporalOrder order2 = sym.getContent().getTemporalOrder();
        CompoundTerm.TemporalOrder order = CompoundTerm.temporalInferenceWithFigure(order1, order2, figure);
        if (order == CompoundTerm.TemporalOrder.UNSURE)
            return;
        if (this.memory.currentTask.getSentence().isJudgment())
            inferToAsym((Judgement) asym, (Judgement) sym, order);
        else {
            convertRelation();
        }
    }

    /* -------------------- two-premise inference rules -------------------- */
    
    /**
     * Produce Similarity/Equivalence from a pire of reversed Inheritance/Implication
     * @param judgement1 The first premise
     * @param judgement2 The second premise
     */
    private  void inferToSym(Judgement judgement1, Judgement judgement2) {
        Statement s1 = (Statement) judgement1.getContent();
        Statement s2 = (Statement) judgement2.getContent();
        Term t1 = s1.getSubject();
        Term t2 = s1.getPredicate();
        Term content;
        if (s1 instanceof Inheritance)
            content = Similarity.make(t1, t2, this.memory);
        else if (s1 instanceof ImplicationAfter)
            content = EquivalenceAfter.make(t1, t2, this.memory);
        else if (s1 instanceof ImplicationBefore)
            content = EquivalenceAfter.make(t2, t1, this.memory);
        else
            content = Equivalence.make(t1, t2, this.memory);
        TruthValue value1 = judgement1.getTruth();
        TruthValue value2 = judgement2.getTruth();
        TruthValue truth = TruthFunctions.intersection(value1, value2);
        BudgetValue budget = this.budgetfunctions.forward(truth);
        this.memory.doublePremiseTask(budget, content, truth);
    }
    
    /**
     * Produce an Inheritance/Implication from a Similarity/Equivalence and a reversed Inheritance/Implication
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     */
    private  void inferToAsym(Judgement asym, Judgement sym, CompoundTerm.TemporalOrder order) {
        Statement statement = (Statement) asym.getContent();
        Term sub = statement.getPredicate();
        Term pre = statement.getSubject();
        Statement content = Statement.make(statement, sub, pre, order, this.memory);
        TruthValue truth = TruthFunctions.reduceConjunction(sym.getTruth(), asym.getTruth());
        BudgetValue budget = this.budgetfunctions.forward(truth);
        this.memory.doublePremiseTask(budget, content, truth);
    }

    /* -------------------- one-premise inference rules -------------------- */
    
    /**
     * Produce an Inheritance/Implication from a reversed Inheritance/Implication
     */
    private  void conversion() {
        TruthValue truth = TruthFunctions.conversion(this.memory.currentBelief.getTruth());
        BudgetValue budget = this.budgetfunctions.forward(truth);
        this.memory.singlePremiseTask(truth, budget);
    }
    
    // switch between Inheritance/Implication and Similarity/Equivalence
    private  void convertRelation() {
        TruthValue truth = this.memory.currentBelief.getTruth();
        if (((Statement) this.memory.currentTask.getContent()).isCommutative())
            truth = TruthFunctions.implied(truth);
        else
            truth = TruthFunctions.implying(truth);
        BudgetValue budget = this.budgetfunctions.forward(truth);
        this.memory.singlePremiseTask(truth, budget);
    }
}
