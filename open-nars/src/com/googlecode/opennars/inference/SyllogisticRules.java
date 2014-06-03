/*
 * SyllogisticRules.java
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

import java.util.*;

import com.googlecode.opennars.entity.*;
import com.googlecode.opennars.language.*;
import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.*;


/**
 * Syllogisms: Inference rules based on the transitivity of the relation.
 */
public class SyllogisticRules {
    
	private Memory memory;
	private BudgetFunctions budgetfunctions;
	
	public SyllogisticRules(Memory memory) {
		this.memory = memory;
		this.budgetfunctions = new BudgetFunctions(memory);
	}
	
    /* --------------- rules used in both first-order inference and higher-order inference --------------- */
    
    /**
     * {<S ==> M>, <M ==> P>} |- {<S ==> P>, <P ==> S>}
     * @param term1 Subject of the first new task
     * @param term2 Predicate of the first new task
     * @param sentence The first premise
     * @param belief The second premise
     */
    void dedExe(Term term1, Term term2, Sentence sentence, Judgement belief) {
        if (Statement.invalidStatement(term1, term2))
            return;
        CompoundTerm.TemporalOrder order1 = sentence.getContent().getTemporalOrder();
        CompoundTerm.TemporalOrder order2 = belief.getContent().getTemporalOrder();
        CompoundTerm.TemporalOrder order = CompoundTerm.temporalInference(order1, order2);
        if (order == CompoundTerm.TemporalOrder.UNSURE)
            return;
        Statement content1 = Statement.make((Statement) sentence.getContent(), term1, term2, order, this.memory);
        Statement content2 = Statement.make((Statement) sentence.getContent(), term2, term1, CompoundTerm.temporalReverse(order), this.memory);
        TruthValue value1 = sentence.getTruth();
        TruthValue value2 = belief.getTruth();
        TruthValue truth1 = null;
        TruthValue truth2 = null;
        BudgetValue budget1, budget2;
        Task task = this.memory.currentTask;
        if (sentence instanceof Question) {
            budget1 = this.budgetfunctions.backwardWeak(value2);
            budget2 = this.budgetfunctions.backwardWeak(value2);
        } else {
            if (sentence instanceof Goal) {
                truth1 = TruthFunctions.desireWeak(value1, value2);
                truth2 = TruthFunctions.desireWeak(value1, value2);
            } else {
                truth1 = TruthFunctions.deduction(value1, value2);
                truth2 = TruthFunctions.exemplification(value1, value2);
            }
            budget1 = this.budgetfunctions.forward(truth1);
            budget2 = this.budgetfunctions.forward(truth2);
        }
        this.memory.doublePremiseTask(budget1, content1, truth1);
        this.memory.doublePremiseTask(budget2, content2, truth2);
    }
    
    /**
     * {<M ==> S>, <M ==> P>} |- {<S ==> P>, <P ==> S>, <S <=> P>}
     * @param term1 Subject of the first new task
     * @param term2 Predicate of the first new task
     * @param sentence The first premise
     * @param belief The second premise
     * @param figure Locations of the shared term in premises
     */
    void abdIndCom(Term term1, Term term2, Sentence sentence, Judgement belief, int figure) {
        if (Statement.invalidStatement(term1, term2))
            return;
        Statement st1 = (Statement) sentence.getContent();
        Statement st2 = (Statement) belief.getContent();
        CompoundTerm.TemporalOrder order1 = st1.getTemporalOrder();
        CompoundTerm.TemporalOrder order2 = st2.getTemporalOrder();
        CompoundTerm.TemporalOrder order;
        if (figure == 11)
            order = CompoundTerm.temporalInference(order1, CompoundTerm.temporalReverse(order2));
        else
            order = CompoundTerm.temporalInference(CompoundTerm.temporalReverse(order1), order2);
        if (order == CompoundTerm.TemporalOrder.UNSURE)
            return;
        Statement statement1 = Statement.make(st1, term1, term2, order, this.memory);
        Statement statement2 = Statement.make(st1, term2, term1, CompoundTerm.temporalReverse(order), this.memory);
        Statement statement3 = Statement.makeSym(st1, term1, term2, order, this.memory);
        TruthValue truth1 = null;
        TruthValue truth2 = null;
        TruthValue truth3 = null;
        BudgetValue budget1, budget2, budget3;
        TruthValue value1 = sentence.getTruth();
        TruthValue value2 = belief.getTruth();
        if (sentence instanceof Question) {
            budget1 = this.budgetfunctions.backward(value2);
            budget2 = this.budgetfunctions.backwardWeak(value2);
            budget3 = this.budgetfunctions.backward(value2);
        } else {
            if (sentence instanceof Goal) {
                truth1 = TruthFunctions.desireStrong(value1, value2);
                truth2 = TruthFunctions.desireWeak(value2, value1);
                truth3 = TruthFunctions.desireStrong(value1, value2);
            } else {
                truth1 = TruthFunctions.abduction(value1, value2);
                truth2 = TruthFunctions.abduction(value2, value1);
                truth3 = TruthFunctions.comparison(value1, value2);
            }
            budget1 = this.budgetfunctions.forward(truth1);
            budget2 = this.budgetfunctions.forward(truth2);
            budget3 = this.budgetfunctions.forward(truth3);
        }
        this.memory.doublePremiseTask(budget1, statement1, truth1);
        this.memory.doublePremiseTask(budget2, statement2, truth2);
        this.memory.doublePremiseTask(budget3, statement3, truth3);
        if (statement1.isConstant()) {
            this.memory.doublePremiseTask(budget1, introVarInd(statement1, st2, st1, figure), truth1);
            this.memory.doublePremiseTask(budget2, introVarInd(statement2, st1, st2, figure), truth2);
            this.memory.doublePremiseTask(budget3, introVarInd(statement3, st1, st2, figure), truth3);
        }
    }
    
    /**
     * {<S ==> P>, <M <=> P>} |- <S ==> P>
     * @param term1 Subject of the new task
     * @param term2 Predicate of the new task
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     * @param figure Locations of the shared term in premises
     */
    void analogy(Term term1, Term term2, Sentence asym, Sentence sym, int figure) {
        if (Statement.invalidStatement(term1, term2))
            return;
        Statement asymSt = (Statement) asym.getContent();
        Statement symSt = (Statement) sym.getContent();
        CompoundTerm.TemporalOrder order1 = asymSt.getTemporalOrder();
        CompoundTerm.TemporalOrder order2 = symSt.getTemporalOrder();
        CompoundTerm.TemporalOrder order;
        switch (figure) {
            case 11:
            case 12:
                order = CompoundTerm.temporalInferenceWithFigure(order2, order1, figure);
                break;
            case 21:
            case 22:
                order = CompoundTerm.temporalInferenceWithFigure(order1, order2, figure);
                break;
            default:
                return;
        }
        if (order == CompoundTerm.TemporalOrder.UNSURE)
            return;
        Term content = Statement.make(asymSt, term1, term2, order, this.memory);
        TruthValue truth = null;
        BudgetValue budget;
        Sentence sentence = this.memory.currentTask.getSentence();
        CompoundTerm taskTerm = (CompoundTerm) sentence.getContent();
        if (sentence instanceof Question) {
            if (taskTerm.isCommutative())
                budget = this.budgetfunctions.backwardWeak(asym.getTruth());
            else
                budget = this.budgetfunctions.backward(sym.getTruth());
        } else {
            if (sentence instanceof Goal) {
                if (taskTerm.isCommutative())
                    truth = TruthFunctions.desireWeak(asym.getTruth(), sym.getTruth());
                else
                    truth = TruthFunctions.desireStrong(asym.getTruth(), sym.getTruth());
            } else
                truth = TruthFunctions.analogy(asym.getTruth(), sym.getTruth());
            budget = this.budgetfunctions.forward(truth);
        }
        this.memory.doublePremiseTask(budget, content, truth);
    }
    
    /**
     * {<S <=> M>, <M <=> P>} |- <S <=> P>
     * @param term1 Subject of the new task
     * @param term2 Predicate of the new task
     * @param belief The first premise
     * @param sentence The second premise
     * @param figure Locations of the shared term in premises
     */
    void resemblance(Term term1, Term term2, Judgement belief, Sentence sentence, int figure) {
        if (Statement.invalidStatement(term1, term2))
            return;
        Statement st1 = (Statement) belief.getContent();
        Statement st2 = (Statement) sentence.getContent();
        CompoundTerm.TemporalOrder order1 = st1.getTemporalOrder();
        CompoundTerm.TemporalOrder order2 = st2.getTemporalOrder();
        CompoundTerm.TemporalOrder order = CompoundTerm.temporalInferenceWithFigure(order1, order2, figure);
        if (order == CompoundTerm.TemporalOrder.UNSURE)
            return;
        Term statement = Statement.make(st1, term1, term2, order, this.memory);
        TruthValue truth = null;
        BudgetValue budget;
        Task task = this.memory.currentTask;
        if (sentence instanceof Question) {
            budget = this.budgetfunctions.backward(belief.getTruth());
        } else {
            if (sentence instanceof Goal)
                truth = TruthFunctions.desireStrong(sentence.getTruth(), belief.getTruth());
            else
                truth = TruthFunctions.resemblance(belief.getTruth(), sentence.getTruth());
            budget = this.budgetfunctions.forward(truth);
        }
        this.memory.doublePremiseTask(budget, statement, truth);
    }
    
    /* --------------- rules used only in conditional inference --------------- */
    
    /**
     * {<S ==> P>, S} |- P
     * @param statement The premise statement
     * @param compound The compound containing the shared component, can be null
     * @param compoundTask Whether the component comes in the task
     * @param side The location of the shared term in the statement
     */
    void detachment(Statement statement, CompoundTerm compound, boolean compoundTask, int side) {
        if (!(statement instanceof Implication) && !(statement instanceof Equivalence)) // necessary?
            return;
        Sentence sentence = this.memory.currentTask.getSentence();
        Judgement belief = this.memory.currentBelief;
        TruthValue value1, value2;
        if (compoundTask) {
            value2 = sentence.getTruth();
            value1 = belief.getTruth();
        } else {
            value1 = sentence.getTruth();
            value2 = belief.getTruth();
        }
        TruthValue truth = null;
        BudgetValue budget;
        if (sentence instanceof Question) {
            if (statement instanceof Equivalence)
                budget = this.budgetfunctions.backward(belief.getTruth());
            else if (side == 0)
                budget = this.budgetfunctions.backwardWeak(belief.getTruth());
            else
                budget = this.budgetfunctions.backward(belief.getTruth());
        } else {
            if (sentence instanceof Goal) {
                if (statement instanceof Equivalence)
                    truth = TruthFunctions.desireStrong(value1, value2);
                else if (side == 0)
                    truth = TruthFunctions.desireInd(value1, value2);
                else
                    truth = TruthFunctions.desireDed(value1, value2);
            } else {
                if (statement instanceof Equivalence)
                    truth = TruthFunctions.analogy(value1, value2);
                else if (side == 0)
                    truth = TruthFunctions.deduction(value1, value2);
                else
                    truth = TruthFunctions.abduction(value1, value2);
            }
            budget = this.budgetfunctions.forward(truth);
        }
        Term content = statement.componentAt(1 - side);
        if (!content.isConstant())
            return;
        if ((compound != null) && (compound instanceof Tense)){
            CompoundTerm.TemporalOrder order1 = compound.getTemporalOrder();
            CompoundTerm.TemporalOrder order2 = statement.getTemporalOrder();
            CompoundTerm.TemporalOrder order;
            if (side == 0)
                order = CompoundTerm.temporalInference(order1, order2);
            else
                order = CompoundTerm.temporalInference(order1, CompoundTerm.temporalReverse(order2));
            if (order == CompoundTerm.TemporalOrder.UNSURE)
//                order = order1;
                return;
            content = Tense.make(content, order, this.memory);
        }
        this.memory.doublePremiseTask(budget, content, truth);
    }
    
    /**
     * {<(&&, S1, S2) ==> P>, <M ==> S1>} |- <(&&, M, S2) ==> P>
     * @param premise1 The conditional premise
     * @param index The location of the shared term in the condition of premise1
     * @param premise2 The premise which, or part of which, appears in the condition of premise1
     * @param side The location of the shared term in premise2: 0 for subject, 1 for predicate, -1 for the whole term
     */
    void conditionalDedInd(Implication premise1, short index, Term premise2, int side) {
        Task task = this.memory.currentTask;
        Sentence sentence = task.getSentence();
        Judgement belief = this.memory.currentBelief;
        Conjunction oldCondition = (Conjunction) premise1.getSubject();
        CompoundTerm.TemporalOrder order1 = premise1.getTemporalOrder();
        CompoundTerm.TemporalOrder order2 = premise2.getTemporalOrder();
        boolean deduction = (side != 0);
        if (!(order1 == order2))                                    // to be refined to cover tense
            if (!(deduction && ((side == -1) || (index == 0))))     // don't replace middle conditions
                return;
        Term newComponent = null;
        if (side == 0)
            newComponent = ((Statement) premise2).getPredicate();
        else if (side == 1)
            newComponent = ((Statement) premise2).getSubject();
        Term newCondition = CompoundTerm.replaceComponent(oldCondition, index, newComponent, this.memory);
        Term content = Statement.make(premise1, newCondition, premise1.getPredicate(), order1, this.memory);
        if (content == null)
            return;
        TruthValue value1 = sentence.getTruth();
        TruthValue value2 = belief.getTruth();
        TruthValue truth = null;
        BudgetValue budget;
        boolean conditionalTask = (Variable.findSubstitute(Variable.VarType.INDEPENDENT, premise2, belief.getContent()) != null);
        if (sentence instanceof Question) {
            budget = this.budgetfunctions.backwardWeak(value2);
        } else {
            if (sentence instanceof Goal) {
                if (conditionalTask)
                    truth = TruthFunctions.desireWeak(value1, value2);
                else if (deduction)
                    truth = TruthFunctions.desireInd(value1, value2);
                else
                    truth = TruthFunctions.desireDed(value1, value2);
                budget = this.budgetfunctions.forward(truth);
            } else {
                if (deduction)
                    truth = TruthFunctions.deduction(value1, value2);
                else if (conditionalTask)
                    truth = TruthFunctions.induction(value2, value1);
                else
                    truth = TruthFunctions.induction(value1, value2);
            }
            budget = this.budgetfunctions.forward(truth);
        }
        this.memory.doublePremiseTask(budget, content, truth);
    }
    
    /**
     * {<(&&, S1, S2) ==> P>, <(&&, S1, S3) ==> P>} |- <S2 ==> S3>
     * @param cond1 The condition of the first premise
     * @param cond2 The condition of the second premise
     * @param st1 The first premise
     * @param st2 The second premise
     * @return Whether there are derived tasks
     */
    boolean conditionalAbd(Term cond1, Term cond2, Statement st1, Statement st2) {
        if (!(st1 instanceof Implication) || !(st2 instanceof Implication))
            return false;
        if (!(cond1 instanceof Conjunction) && !(cond2 instanceof Conjunction))
            return false;
        CompoundTerm.TemporalOrder order1 = st1.getTemporalOrder();
        CompoundTerm.TemporalOrder order2 = st2.getTemporalOrder();
        if (order1 != CompoundTerm.temporalReverse(order2))
            return false;
        Term term1 = null;
        Term term2 = null;
        if (cond1 instanceof Conjunction) {
            term1 = CompoundTerm.reduceComponents((Conjunction) cond1, cond2, this.memory);
        }
        if (cond2 instanceof Conjunction) {
            term2 = CompoundTerm.reduceComponents((Conjunction) cond2, cond1, this.memory);
        }
        if ((term1 == null) && (term2 == null))
            return false;
        Task task = this.memory.currentTask;
        Sentence sentence = task.getSentence();
        Judgement belief = this.memory.currentBelief;
        TruthValue value1 = sentence.getTruth();
        TruthValue value2 = belief.getTruth();
        boolean keepOrder = (Variable.findSubstitute(Variable.VarType.INDEPENDENT, st1, task.getContent()) != null);
        Term content;
        TruthValue truth = null;
        BudgetValue budget;
        if (term1 != null) {
            if (term2 != null)
                content = Statement.make(st2, term2, term1, order2, this.memory);
            else
                content = term1;
            if (sentence instanceof Question) {
                budget = this.budgetfunctions.backwardWeak(value2);
            } else {
                if (sentence instanceof Goal) {
                    if (keepOrder)
                        truth = TruthFunctions.desireDed(value1, value2);
                    else
                        truth = TruthFunctions.desireInd(value1, value2);
                } else
                    truth = TruthFunctions.abduction(value2, value1);
                budget = this.budgetfunctions.forward(truth);
            }
            this.memory.doublePremiseTask(budget, content, truth);
        }
        if (term2 != null) {
            if (term1 != null)
                content = Statement.make(st1, term1, term2, order1, this.memory);
            else
                content = term2;
            if (sentence instanceof Question) {
                budget = this.budgetfunctions.backwardWeak(value2);
            } else {
                if (sentence instanceof Goal) {
                    if (keepOrder)
                        truth = TruthFunctions.desireDed(value1, value2);
                    else
                        truth = TruthFunctions.desireInd(value1, value2);
                } else
                    truth = TruthFunctions.abduction(value1, value2);
                budget = this.budgetfunctions.forward(truth);
            }
            this.memory.doublePremiseTask(budget, content, truth);
        }
        return true;
    }
    
    /* --------------- rules used for independent variable introduction --------------- */
    
    /**
     * {<C ==> <M --> P>>, <M --> S>} |- <(&&,C,<#x --> S>) ==> <#x --> P>>
     * @param compound The compound statement: Implication or Conjunction
     * @param component The component to be used as a premise in internal induction
     * @param premise The second premise, directly used in internal induction
     */
    void introVarIndInner(CompoundTerm compound, Term component, Term premise) {
        Task task = this.memory.currentTask;
        Sentence sentence = task.getSentence();
        if (!sentence.isJudgment())
            return;
        if (!(component instanceof Statement))
            return;
        if (component.getClass() != premise.getClass())
            return;
        Variable v1 = new Variable(Symbols.VARIABLE_TAG + "0");
        Variable v2 = new Variable(Symbols.VARIABLE_TAG + "0");
        Statement premise1 = (Statement) premise;
        Statement premise2 = (Statement) component;
        CompoundTerm.TemporalOrder order1 = premise1.getTemporalOrder();
        CompoundTerm.TemporalOrder order2 = premise2.getTemporalOrder();
        if (order1 != CompoundTerm.temporalReverse(order2))
            return;
        int figure;
        if (premise1.getSubject().equals(premise2.getSubject()))
            figure = 11;
        else if (premise1.getPredicate().equals(premise2.getPredicate()))
            figure = 22;
        else
            return;
        Statement innerContent = introVarInd(null, premise1, premise2, figure);
        if (innerContent == null)
            return;
        Sentence belief = this.memory.currentBelief;
        Term content;
        if (compound instanceof Implication) {
            content = Statement.make((Statement) compound, compound.componentAt(0), innerContent, this.memory);
        } else if (compound instanceof Conjunction) {
            content = CompoundTerm.replaceComponent(compound, component, innerContent, this.memory);
        } else
            return;
        TruthValue truth;
        if (premise.equals(sentence.getContent()))
            truth = TruthFunctions.abduction(sentence.getTruth(), belief.getTruth());
        else
            truth = TruthFunctions.abduction(belief.getTruth(), sentence.getTruth());
        BudgetValue budget = this.budgetfunctions.forward(truth);
        this.memory.doublePremiseTask(budget, content, truth);
    }
    
    /**
     * {<M --> S>, <M --> P>} |- <<#x --> S> ==> <#x --> P>>
     * @param temp The template for the new task <S --> P>
     * @param premise1 The first premise <M --> P>
     * @param premise2 The second premise <M --> P>
     * @param figure The figure indicating the location of the shared term
     */
    private Statement introVarInd(Statement temp, Statement premise1, Statement premise2, int figure) {
        Statement state1, state2;
        Variable v1 = new Variable(Symbols.VARIABLE_TAG + "0");
        Variable v2 = new Variable(Symbols.VARIABLE_TAG + "0");
        if (figure == 11) {
            state1 = Statement.make(premise1, v1, premise1.getPredicate(), this.memory);
            state2 = Statement.make(premise2, v2, premise2.getPredicate(), this.memory);
        } else {
            state1 = Statement.make(premise1, premise1.getSubject(), v1, this.memory);
            state2 = Statement.make(premise2, premise2.getSubject(), v2, this.memory);
        }
        Statement content;
        if ((temp == null) || !temp.isCommutative())
            content = Implication.make(state1, state2, this.memory);
        else
            content = Equivalence.make(state1, state2, this.memory);
        return content;
    }
}
