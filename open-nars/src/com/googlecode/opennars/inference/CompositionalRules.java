/*
 * CompositionalRules.java
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
import com.googlecode.opennars.main.*;
import com.googlecode.opennars.parser.*;

/**
 * Compound term composition and decomposition rules, with two premises.
 * <p>
 * Forward inference only, except the last rule (abdDepOuter) can also be used backward.
 */
public class CompositionalRules {
	
	private Memory memory;
	private BudgetFunctions budgetfunctions;
	
	public CompositionalRules(Memory memory) {
		this.memory = memory;
		this.budgetfunctions = new BudgetFunctions(memory);
	}
    
    /* -------------------- intersections and differences -------------------- */
    
    /**
     * {<S ==> M>, <P ==> M>} |- {<(S|P) ==> M>, <(S&P) ==> M>, <(S-P) ==> M>, <(P-S) ==> M>}
     * 
     * @param sentence The first premise
     * @param belief The second premise
     * @param index The location of the shared term
     */
     void composeCompound(Sentence sentence, Judgement belief, int index) {
        if (!sentence.isJudgment())
            return;                             // forward only
        Statement content1 = (Statement) sentence.getContent();
        Statement content2 = (Statement) belief.getContent();
        if (content1.getClass() != content2.getClass())
            return;
        if (content1.getTemporalOrder() != content2.getTemporalOrder())
            return;
        Term component1, component2;
        component1 = content1.componentAt(1 - index);
        component2 = content2.componentAt(1 - index);
        Term component = content1.componentAt(index);
        if ((component1 instanceof CompoundTerm) && ((CompoundTerm) component1).containAllComponents(component2)) {
            decomposeCompound((CompoundTerm) component1, component2, component, index, true);
            return;
        } else if ((component2 instanceof CompoundTerm) && ((CompoundTerm) component2).containAllComponents(component1)) {
            decomposeCompound((CompoundTerm) component2, component1, component, index, false);
            return;
        }
        Term t1 = null;
        Term t2 = null;
        Term t3 = null;
        Term t4 = null;
        TruthValue v1 = sentence.getTruth();
        TruthValue v2 = belief.getTruth();
        if (index == 0) {
            if (content1 instanceof Inheritance) {
                t1 = IntersectionInt.make(component1, component2, this.memory);
                t2 = IntersectionExt.make(component1, component2, this.memory);
                t3 = DifferenceExt.make(component1, component2, this.memory);
                t4 = DifferenceExt.make(component2, component1, this.memory);
            } else if (content1 instanceof Implication) {
                t1 = Disjunction.make(component1, component2, this.memory);
                t2 = Conjunction.make(component1, component2, this.memory);
                t3 = Conjunction.make(component1, Negation.make(component2, this.memory), this.memory);
                t4 = Conjunction.make(component2, Negation.make(component1, this.memory), this.memory);
            }
            processComposed(content1, component, t1, TruthFunctions.union(v1, v2));
            processComposed(content1, component, t2, TruthFunctions.intersection(v1, v2));
            processComposed(content1, component, t3, TruthFunctions.difference(v1, v2));
            processComposed(content1, component, t4, TruthFunctions.difference(v2, v1));
            if (content1.isConstant())
                introVarDepOuter(content1, content2, index);
        } else {
            if (content1 instanceof Inheritance) {
                t1 = IntersectionExt.make(component1, component2, this.memory);
                t2 = IntersectionInt.make(component1, component2, this.memory);
                t3 = DifferenceInt.make(component1, component2, this.memory);
                t4 = DifferenceInt.make(component2, component1, this.memory);
            } else if (content1 instanceof Implication) {
                t1 = Conjunction.make(component1, component2, this.memory);
                t2 = Disjunction.make(component1, component2, this.memory);
                t3 = Disjunction.make(component1, Negation.make(component2, this.memory), this.memory);
                t4 = Disjunction.make(component2, Negation.make(component1, this.memory), this.memory);
            }
            processComposed(content1, t1, component, TruthFunctions.union(v1, v2));
            processComposed(content1, t2, component, TruthFunctions.intersection(v1, v2));
            processComposed(content1, t3, component, TruthFunctions.difference(v1, v2));
            processComposed(content1, t4, component, TruthFunctions.difference(v2, v1));
            if (content1.isConstant())
                introVarDepOuter(content1, content2, index);
        }
    }
    
    /**
     * Finish composing compound term
     * @param statement Type of the content
     * @param subject Subject of content
     * @param predicate Predicate of content
     * @param truth TruthValue of the content
     */
    private  void processComposed(Statement statement, Term subject, Term predicate, TruthValue truth) {
        if ((subject == null) || (predicate == null)) 
            return;
        Term content = Statement.make(statement, subject, predicate, this.memory);
        if ((content == null) || content.equals(statement) || content.equals(this.memory.currentBelief.getContent()))
            return;
        BudgetValue budget = this.budgetfunctions.compoundForward(truth, content);
        this.memory.doublePremiseTask(budget, content, truth);
    }
    
    /**
     * {<(S|P) ==> M>, <P ==> M>} |- <S ==> M>
     * @param compound The compound term to be decomposed
     * @param component The part of the compound to be removed
     * @param term1 The other term in the content
     * @param index The location of the shared term: 0 for subject, 1 for predicate
     * @param compoundTask Whether the compound comes from the task
     */
    private  void decomposeCompound(CompoundTerm compound, Term component, Term term1, int index, boolean compoundTask) {
        Term term2 = CompoundTerm.reduceComponents(compound, component, this.memory);
        if (term2 == null)
            return;
        Task task = this.memory.currentTask;
        Sentence sentence = task.getSentence();
        Judgement belief = this.memory.currentBelief;
        Statement oldContent = (Statement) task.getContent();
        TruthValue v1, v2;
        if (compoundTask) {
            v1 = sentence.getTruth();
            v2 = belief.getTruth();
        } else {
            v1 = belief.getTruth();
            v2 = sentence.getTruth();
        }
        TruthValue truth = null;
        Term content;
        if (index == 0) {
            content = Statement.make(oldContent, term1, term2, this.memory);
            if (content == null)
                return;
            if (oldContent instanceof Inheritance) {
                if (compound instanceof IntersectionExt) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if (compound instanceof IntersectionInt) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                } else if ((compound instanceof SetInt) && (component instanceof SetInt)) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if ((compound instanceof SetExt) && (component instanceof SetExt)) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                } else if (compound instanceof DifferenceExt) {
                    if (compound.componentAt(0).equals(component)) {
                        truth = TruthFunctions.reduceDisjunction(v2, v1);
                    } else {
                        truth = TruthFunctions.reduceConjunctionNeg(v1, v2);
                    }
                }
            } else if (oldContent instanceof Implication) {
                if (compound instanceof Conjunction) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if (compound instanceof Disjunction) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                }
            }
        } else {
            content = Statement.make(oldContent, term2, term1, this.memory);
            if (content == null)
                return;
            if (oldContent instanceof Inheritance) {
                if (compound instanceof IntersectionInt) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if (compound instanceof IntersectionExt) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                } else if ((compound instanceof SetExt) && (component instanceof SetExt)) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if ((compound instanceof SetInt) && (component instanceof SetInt)) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                } else if (compound instanceof DifferenceInt) {
                    if (compound.componentAt(1).equals(component)) {
                        truth = TruthFunctions.reduceDisjunction(v2, v1);
                    } else {
                        truth = TruthFunctions.reduceConjunctionNeg(v1, v2);
                    }
                }
            } else if (oldContent instanceof Implication) {
                if (compound instanceof Disjunction) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if (compound instanceof Conjunction) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                }
            }
        }
        BudgetValue budget = this.budgetfunctions.compoundForward(truth, content);
        this.memory.doublePremiseTask(budget, content, truth);
    }
        
    /**
     * {(||, S, P), P} |- S
     * @param compound The compound term to be decomposed
     * @param component The part of the compound to be removed
     * @param compoundTask Whether the compound comes from the task
     */
     void decomposeStatement(CompoundTerm compound, Term component, boolean compoundTask) {
        Task task = this.memory.currentTask;
        Sentence sentence = task.getSentence();
        if (!sentence.isJudgment())
            return;
        Judgement belief = this.memory.currentBelief;
        Term content = CompoundTerm.reduceComponents(compound, component, this.memory);
        if (content == null)
            return;
        TruthValue v1, v2;
        if (compoundTask) {
            v1 = sentence.getTruth();
            v2 = belief.getTruth();
        } else {
            v1 = belief.getTruth();
            v2 = sentence.getTruth();
        }        
        TruthValue truth = null;
        if (compound instanceof Conjunction) {
            if (sentence instanceof Goal) {
                if (compoundTask)
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                else
                    return;
            } else if (sentence instanceof Judgement)
                truth = TruthFunctions.reduceConjunction(v1, v2);
        } else if (compound instanceof Disjunction) {
            if (sentence instanceof Goal) {
                if (compoundTask)
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                else
                    return;
            } else if (sentence instanceof Judgement)
                truth = TruthFunctions.reduceDisjunction(v1, v2);
        } else
            return;
        BudgetValue budget = this.budgetfunctions.compoundForward(truth, content);
        this.memory.doublePremiseTask(budget, content, truth);
    }
    
    /* ---------------- dependent variable and conjunction ---------------- */
    
    /**
     * {<M --> S>, <M --> P>} |- (&&, <#x() --> S>, <#x() --> P>>
     * @param premise1 The first premise <M --> P>
     * @param premise2 The second premise <M --> P>
     * @param index The location of the shared term: 0 for subject, 1 for predicate
     */
    private  Conjunction introVarDep(Statement premise1, Statement premise2, int index) {
        Statement state1, state2;
        Variable v1 = new Variable(Symbols.VARIABLE_TAG + "0()");
        Variable v2 = new Variable(Symbols.VARIABLE_TAG + "0()");
        if (index == 0) {
            state1 = Statement.make(premise1, v1, premise1.getPredicate(), this.memory);
            state2 = Statement.make(premise2, v2, premise2.getPredicate(), this.memory);
        } else {
            state1 = Statement.make(premise1, premise1.getSubject(), v1, this.memory);
            state2 = Statement.make(premise2, premise2.getSubject(), v2, this.memory);
        }
        Conjunction content = (Conjunction) Conjunction.make(state1, state2, this.memory);
        return content;
    }

    /**
     * Introduce a dependent variable in an outer-layer conjunction
     * @param premise1 The first premise <M --> S>
     * @param premise2 The second premise <M --> P>
     * @param index The location of the shared term: 0 for subject, 1 for predicate
     */
    private  void introVarDepOuter(Statement premise1, Statement premise2, int index) {
        Term content = introVarDep(premise1, premise2, index);
        TruthValue v1 = this.memory.currentTask.getSentence().getTruth();
        TruthValue v2 = this.memory.currentBelief.getTruth();
        TruthValue truth = TruthFunctions.intersection(v1, v2);
        BudgetValue budget = this.budgetfunctions.compoundForward(truth, content);
        this.memory.doublePremiseTask(budget, content, truth);
    }
    
    /**
     * Introduce a dependent variable in an inner-layer conjunction
     * @param compound The compound containing the first premise
     * @param component The first premise <M --> S>
     * @param premise The second premise <M --> P>
     */
     void introVarDepInner(CompoundTerm compound, Term component, Term premise) {
        if (!(component instanceof Statement) || !(component.getClass() == premise.getClass()))
            return;
        Statement premise1 = (Statement) premise;
        Statement premise2 = (Statement) component;
        int index;
        if (premise1.getSubject().equals(premise2.getSubject()))
            index = 0;
        else if (premise1.getPredicate().equals(premise2.getPredicate()))
            index = 1;
        else
            return;
        Term innerContent = introVarDep(premise1, premise2, index);
        if (innerContent == null)
            return;
        Task task = this.memory.currentTask;
        Sentence sentence = task.getSentence();
        Judgement belief = this.memory.currentBelief;
        Term content = task.getContent();
        if (compound instanceof Implication)
            content = Statement.make((Statement) content, compound.componentAt(0), innerContent, this.memory);
        else if (compound instanceof Conjunction)
            content = CompoundTerm.replaceComponent(compound, component, innerContent, this.memory);
        TruthValue truth = null;
        if (sentence instanceof Goal)
            truth = TruthFunctions.intersection(belief.getTruth(), sentence.getTruth());    // to be revised
        else if (sentence instanceof Judgement)
            truth = TruthFunctions.intersection(belief.getTruth(), sentence.getTruth());
        else
            return; // don't do it for questions
        BudgetValue budget = this.budgetfunctions.compoundForward(truth, content);
        this.memory.doublePremiseTask(budget, content, truth);
    }

    /**
     * {(&&, <#x() --> S>, <#x() --> P>>, <M --> P>} |- <M --> S>
     * @param compound The compound term to be decomposed
     * @param component The part of the compound to be removed
     * @param compoundTask Whether the compound comes from the task
     */
     void abdVarDepOuter(CompoundTerm compound, Term component, boolean compoundTask) {
        Term content = CompoundTerm.reduceComponents(compound, component, this.memory);
        Task task = this.memory.currentTask;
        Sentence sentence = task.getSentence();
        Judgement belief = this.memory.currentBelief;
        TruthValue v1 = sentence.getTruth();
        TruthValue v2 = belief.getTruth();
        TruthValue truth = null;
        BudgetValue budget;
        if (sentence instanceof Question)
            budget = (compoundTask ? this.budgetfunctions.backward(v2) : this.budgetfunctions.backwardWeak(v2));
        else {
            if (sentence instanceof Goal)
                truth = (compoundTask ? TruthFunctions.desireStrong(v1, v2) : TruthFunctions.desireWeak(v1, v2));
            else
                truth = (compoundTask ? TruthFunctions.existAnalogy(v1, v2) : TruthFunctions.existAnalogy(v2, v1));
            budget = this.budgetfunctions.compoundForward(truth, content);
        }
        this.memory.doublePremiseTask(budget, content, truth);
    }
}
