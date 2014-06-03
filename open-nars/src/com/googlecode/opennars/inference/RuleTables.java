/*
 * RuleTables.java
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

/**
 * Table of inference rules, indexed by the CompositionLinks for the task and the belief.
 * Used for indirective processing of a task
 */
public class RuleTables {
    
	private Memory memory;
	
	private SyllogisticRules syllogisticrules;
	private StructuralRules structuralrules;
	private MatchingRules matchingrules;
	private CompositionalRules compositionalrules;
	
	public RuleTables(Memory memory) {
		this.memory = memory;
		this.syllogisticrules = new SyllogisticRules(memory);
		this.structuralrules = new StructuralRules(memory);
		this.matchingrules = new MatchingRules(memory);
		this.compositionalrules = new CompositionalRules(memory);
	}
	
	public MatchingRules getMatchingRules() {
		return this.matchingrules;
	}
	
    /* ----- inferences with two composition links ----- */
    
    public  void reason(TaskLink tLink, TermLink bLink) {
        Task task = memory.currentTask;
        Term taskTerm = (Term) task.getContent().clone();         // cloning for substitution
        Term beliefTerm = (Term) bLink.getTarget().clone();       // cloning for substitution
        Concept beliefConcept = memory.termToConcept(beliefTerm);
        Judgement belief = null;
        if (beliefConcept != null)
            belief = beliefConcept.getBelief(task);
        memory.currentBelief = belief;  // may be null
        if ((belief != null) && (Variable.findSubstitute(Variable.VarType.QUERY, taskTerm, beliefTerm) != null))
            matchingrules.match(task, belief);
        short tIndex = tLink.getIndex(0);
        short bIndex = bLink.getIndex(0);
        switch(tLink.getType()) {
            case TermLink.SELF:
                switch(bLink.getType()) {
                    case TermLink.COMPONENT:
                        compoundAndSelf((CompoundTerm) taskTerm, beliefTerm, true);
                        break;
                    case TermLink.COMPOUND:
                        compoundAndSelf((CompoundTerm) beliefTerm, taskTerm, false);
                        break;
                    case TermLink.COMPONENT_STATEMENT:  // detachment
                        if (belief != null)
                            syllogisticrules.detachment((Statement) taskTerm, null, true, bIndex);
                        break;
                    case TermLink.COMPOUND_STATEMENT:   // detachment
                        if (belief != null)
                            syllogisticrules.detachment((Statement) beliefTerm, null, false, bIndex); //task, beliefTerm);
                        break;
                    case TermLink.COMPONENT_CONDITION:
                        if (belief != null)
                            syllogisticrules.conditionalDedInd((Implication) taskTerm, bIndex, beliefTerm, tIndex);
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null)
                            syllogisticrules.conditionalDedInd((Implication) beliefTerm, bIndex, taskTerm, tIndex);
                        break;
                    default:
                }
                break;
            case TermLink.COMPOUND:
                switch(bLink.getType()) {
                    case TermLink.COMPOUND:
                        compoundAndCompound((CompoundTerm) taskTerm, tIndex, (CompoundTerm) beliefTerm, bIndex);
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        compoundAndStatement((CompoundTerm) taskTerm, tIndex, (Statement) beliefTerm, bIndex, beliefTerm);
                        break;
                    default:
                }
                break;
            case TermLink.COMPOUND_STATEMENT:
                switch(bLink.getType()) {
                    case TermLink.COMPONENT:
                        componentAndStatement((CompoundTerm) memory.currentTerm, bIndex, (Statement) taskTerm, tIndex);
                        break;
                    case TermLink.COMPOUND:
                        compoundAndStatement((CompoundTerm) beliefTerm, bIndex, (Statement) taskTerm, tIndex, beliefTerm);
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null)
                            syllogisms(tLink, bLink, taskTerm, beliefTerm);
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null)
                            conditionalDedIndWithVar((Implication) beliefTerm, bIndex, (Statement) taskTerm, tIndex);
                        break;
                    default:
                }
                break;
            case TermLink.COMPOUND_CONDITION:
                switch(bLink.getType()) {
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null)
                            conditionalDedIndWithVar((Implication) taskTerm, tIndex, (Statement) beliefTerm, bIndex);
                        break;
                    default:
                }
                break;
            default:
                // to be revised to cover all types
        }
    }
    
    /* ----- syllogistic inferences ----- */
    
    /**
     * Meta-table of syllogistic rules, indexed by the content classes of the sentence and the belief
     */
    private  void syllogisms(TaskLink tLink, TermLink bLink, Term taskTerm, Term beliefTerm) {
        Sentence sentence = memory.currentTask.getSentence();
        Judgement belief = memory.currentBelief;
        int figure;
        if (taskTerm instanceof Inheritance) {
            if (beliefTerm instanceof Inheritance) {
                figure = indexToFigure(tLink, bLink);
                asymmetricAsymmetric(sentence, belief, figure);
            } else if (beliefTerm instanceof Similarity) {
                figure = indexToFigure(tLink, bLink);
                asymmetricSymmetric(sentence, belief, figure);
            } else
                detachmentWithVar((Statement) beliefTerm, false, bLink.getIndex(0), (Statement) taskTerm, belief);
        } else if (taskTerm instanceof Similarity) {
            if (beliefTerm instanceof Inheritance) {
                figure = indexToFigure(bLink, tLink);
                asymmetricSymmetric(belief, sentence, figure);
            } else if (beliefTerm instanceof Similarity) {
                figure = indexToFigure(bLink, tLink);
                symmetricSymmetric(belief, sentence, figure);
            }
        } else if (taskTerm instanceof Implication) {
            if (beliefTerm instanceof Implication) {
                figure = indexToFigure(tLink, bLink);
                asymmetricAsymmetric(sentence, belief, figure);
            } else if (beliefTerm instanceof Equivalence) {
                figure = indexToFigure(tLink, bLink);
                asymmetricSymmetric(sentence, belief, figure);
            } else if (beliefTerm instanceof Inheritance)
                detachmentWithVar((Statement) taskTerm, true, tLink.getIndex(0), (Statement) beliefTerm, belief);
            // or intro 2nd ind var
        } else if (taskTerm instanceof Equivalence) {
            if (beliefTerm instanceof Implication) {
                figure = indexToFigure(bLink, tLink);
                asymmetricSymmetric(belief, sentence, figure);
            } else if (beliefTerm instanceof Equivalence) {
                figure = indexToFigure(bLink, tLink);
                symmetricSymmetric(belief, sentence, figure);
            } else if (beliefTerm instanceof Inheritance)
                detachmentWithVar((Statement) taskTerm, true, tLink.getIndex(0), (Statement) beliefTerm, belief);
        }
    }
    
    private  int indexToFigure(TermLink link1, TermLink link2) {
        return (link1.getIndex(0) + 1) * 10 + (link2.getIndex(0) + 1);    // valid value: 11, 12, 21, 22
    }
    
    /**
     * Syllogistic rules whose both premises are on the same asymmetric relation
     */
    private  void asymmetricAsymmetric(Sentence sentence, Judgement belief, int figure) {
        Statement s1 = (Statement) sentence.cloneContent();
        Statement s2 = (Statement) belief.cloneContent();
        Term t1, t2;
        switch (figure) {
            case 11:    // induction
                if (Variable.unify(Variable.VarType.INDEPENDENT, s1.getSubject(), s2.getSubject(), s1, s2)) {
                    t1 = s2.getPredicate();
                    t2 = s1.getPredicate();
                    syllogisticrules.abdIndCom(t1, t2, sentence, belief, figure);
                    compositionalrules.composeCompound(sentence, belief, 0);
                }
                break;
            case 12:    // deduction
                if (Variable.unify(Variable.VarType.INDEPENDENT, s1.getSubject(), s2.getPredicate(), s1, s2)) {
                    t1 = s2.getSubject();
                    t2 = s1.getPredicate();
                    if (Variable.unify(Variable.VarType.QUERY, t1, t2, s1, s2))
                        matchingrules.matchReverse();
                    else
                        syllogisticrules.dedExe(t1, t2, sentence, belief);
                }
                break;
            case 21:    // exemplification
                if (Variable.unify(Variable.VarType.INDEPENDENT, s1.getPredicate(), s2.getSubject(), s1, s2)) {
                    t1 = s1.getSubject();
                    t2 = s2.getPredicate();
                    if (Variable.unify(Variable.VarType.QUERY, t1, t2, s1, s2))
                        matchingrules.matchReverse();
                    else
                        syllogisticrules.dedExe(t1, t2, sentence, belief);
                }
                break;
            case 22:    // abduction
                if (Variable.unify(Variable.VarType.INDEPENDENT, s1.getPredicate(), s2.getPredicate(), s1, s2)) {
                    t1 = s1.getSubject();
                    t2 = s2.getSubject();
                    if (! syllogisticrules.conditionalAbd(t1, t2, s1, s2)) {         // if conditional abduction, skip the following
                        syllogisticrules.abdIndCom(t1, t2, sentence, belief, figure);
                        compositionalrules.composeCompound(sentence, belief, 1);
                    }
                }
                break;
            default:
        }
    }
    
    /**
     * Syllogistic rules whose first premise is on an asymmetric relation, and the second on a symmetric relation
     */
    private  void asymmetricSymmetric(Sentence asym, Sentence sym, int figure) {
        Statement asymSt = (Statement) asym.cloneContent();
        Statement symSt = (Statement) sym.cloneContent();
        Term t1, t2;
        switch (figure) {
            case 11:
                if (Variable.unify(Variable.VarType.INDEPENDENT, asymSt.getSubject(), symSt.getSubject(), asymSt, symSt)) {
                    t1 = asymSt.getPredicate();
                    t2 = symSt.getPredicate();
                    if (Variable.unify(Variable.VarType.QUERY, t1, t2, asymSt, symSt))
                        matchingrules.matchAsymSym(asym, sym, figure); //task, belief, order1 - order2, false);
                    else
                        syllogisticrules.analogy(t2, t1, asym, sym, figure);
                }
                break;
            case 12:
                if (Variable.unify(Variable.VarType.INDEPENDENT, asymSt.getSubject(), symSt.getPredicate(), asymSt, symSt)) {
                    t1 = asymSt.getPredicate();
                    t2 = symSt.getSubject();
                    if (Variable.unify(Variable.VarType.QUERY, t1, t2, asymSt, symSt))
                        matchingrules.matchAsymSym(asym, sym, figure); //task, belief, order1 - order2, false);
                    else
                        syllogisticrules.analogy(t2, t1, asym, sym, figure);
                }
                break;
            case 21:
                if (Variable.unify(Variable.VarType.INDEPENDENT, asymSt.getPredicate(), symSt.getSubject(), asymSt, symSt)) {
                    t1 = asymSt.getSubject();
                    t2 = symSt.getPredicate();
                    if (Variable.unify(Variable.VarType.QUERY, t1, t2, asymSt, symSt))
                        matchingrules.matchAsymSym(asym, sym, figure); //task, belief, order1 - order2, false);
                    else
                        syllogisticrules.analogy(t1, t2, asym, sym, figure);
                }
                break;
            case 22:
                if (Variable.unify(Variable.VarType.INDEPENDENT, asymSt.getPredicate(), symSt.getPredicate(), asymSt, symSt)) {
                    t1 = asymSt.getSubject();
                    t2 = symSt.getSubject();
                    if (Variable.unify(Variable.VarType.QUERY, t1, t2, asymSt, symSt))
                        matchingrules.matchAsymSym(asym, sym, figure); //task, belief, order1 - order2, false);
                    else
                        syllogisticrules.analogy(t1, t2, asym, sym, figure);
                }
                break;
        }
    }
    
    /**
     * Syllogistic rules whose both premises are on the same symmetric relation
     */
    private  void symmetricSymmetric(Judgement belief, Sentence sentence, int figure) {
        Statement s1 = (Statement) belief.cloneContent();
        Statement s2 = (Statement) sentence.cloneContent();
        switch (figure) {
            case 11:
                if (Variable.unify(Variable.VarType.INDEPENDENT, s1.getSubject(), s2.getSubject(), s1, s2))
                    syllogisticrules.resemblance(s1.getPredicate(), s2.getPredicate(), belief, sentence, figure);
                break;
            case 12:
                if (Variable.unify(Variable.VarType.INDEPENDENT, s1.getSubject(), s2.getPredicate(), s1, s2))
                    syllogisticrules.resemblance(s1.getPredicate(), s2.getSubject(), belief, sentence, figure);
                break;
            case 21:
                if (Variable.unify(Variable.VarType.INDEPENDENT, s1.getPredicate(), s2.getSubject(), s1, s2))
                    syllogisticrules.resemblance(s1.getSubject(), s2.getPredicate(), belief, sentence, figure);
                break;
            case 22:
                if (Variable.unify(Variable.VarType.INDEPENDENT, s1.getPredicate(), s2.getPredicate(), s1, s2))
                    syllogisticrules.resemblance(s1.getSubject(), s2.getSubject(), belief, sentence, figure);
                break;
        }
    }
    
    /* ----- conditional inferences ----- */
    
    private  void detachmentWithVar(Statement statement, boolean compoundTask, int index, CompoundTerm compound, Sentence belief) {
        Term component = statement.componentAt(index);
        Term inh = (compound instanceof Tense) ? compound.componentAt(0) : compound;
        if ((component instanceof Inheritance) && (belief != null)) {
            if (component.isConstant())
                syllogisticrules.detachment(statement, compound, compoundTask, index);
            else if (Variable.unify(Variable.VarType.INDEPENDENT, component, inh, statement, compound))
                syllogisticrules.detachment(statement, compound, compoundTask, index);
            else if ((statement instanceof Implication) && (memory.currentTask.getSentence().isJudgment())) {
                syllogisticrules.introVarIndInner(statement, statement.getPredicate(), inh);    // tense???
                compositionalrules.introVarDepInner(statement, statement.getPredicate(), inh);  // tense???
            }
        }
    }
    
    private  void conditionalDedIndWithVar(Implication conditional, short index, Statement statement, short side) {
        CompoundTerm condition = (CompoundTerm) conditional.getSubject();
        Term component = condition.componentAt(index);
        Term component2 = null;
        if (statement instanceof Inheritance)
            component2 = statement;
        else if (statement instanceof Implication)
            component2 = statement.componentAt(side);
        if ((component2 != null) && Variable.unify(Variable.VarType.INDEPENDENT, component, component2, conditional, statement))
            syllogisticrules.conditionalDedInd(conditional, index, statement, -1);
    }
    
    /* ----- structural inferences ----- */
    
    private  void compoundAndSelf(CompoundTerm compound, Term component, boolean compoundTask) {
        if ((compound instanceof Conjunction) || (compound instanceof Disjunction)) {
            if (memory.currentBelief != null)
                compositionalrules.decomposeStatement(compound, component, compoundTask);
            else if (compound.containComponent(component))
                structuralrules.structuralCompound(compound, component, compoundTask);
        } else if ((compound instanceof Negation) && !memory.currentTask.isStructual()) {
            if (compoundTask)
                structuralrules.transformNegation(((Negation) compound).componentAt(0));
            else
                structuralrules.transformNegation(compound);
        }
    }
    
    private  void compoundAndCompound(CompoundTerm taskTerm, int tIndex, CompoundTerm beliefTerm, int bIndex) {
        if (taskTerm.getClass() == beliefTerm.getClass()) {
            if (taskTerm.size() > beliefTerm.size())
                compoundAndSelf(taskTerm, beliefTerm, true);
            else if (taskTerm.size() < beliefTerm.size())
                compoundAndSelf(beliefTerm, taskTerm, false);
        }
    }
    
    private  void compoundAndStatement(CompoundTerm compound, short index, Statement statement, short side, Term beliefTerm) {
        Term component = compound.componentAt(index);
        Task task = memory.currentTask;
        if (component.getClass() ==  statement.getClass()) {
            if ((compound instanceof Conjunction) && (memory.currentBelief != null)) {
                if (Variable.unify(Variable.VarType.DEPENDENT, component, statement, compound, statement))
                    compositionalrules.abdVarDepOuter(compound, component, statement.equals(beliefTerm));
                else if (task.getSentence().isJudgment()) {
                    syllogisticrules.introVarIndInner(compound, component, statement);
                    compositionalrules.introVarDepInner(compound, component, statement);
                }
            }
        } else {
            if (compound instanceof Tense) {
                if (component instanceof Inheritance)
                    detachmentWithVar(statement, statement.equals(beliefTerm), side, compound, memory.currentBelief);
                else {
                    Sentence belief = memory.currentBelief;
                    if (belief != null)
                        syllogisticrules.detachment(statement, compound, statement.equals(beliefTerm), side);
                }
            } else if (!task.isStructual() && task.getSentence().isJudgment()) {
                if (statement instanceof Inheritance) {
                    structuralrules.structuralCompose1(compound, index, statement);
                    if (!(compound instanceof SetExt) && !(compound instanceof SetInt))
                        structuralrules.structuralCompose2(compound, index, statement, side);    // {A --> B, A @ (A&C)} |- (A&C) --> (B&C)
                } else if (statement instanceof Similarity)
                    structuralrules.structuralCompose2(compound, index, statement, side);    // {A <-> B, A @ (A&C)} |- (A&C) <-> (B&C)
            }
        }
    }
    
    private  void componentAndStatement(CompoundTerm compound, short index, Statement statement, short side) {
        if (!memory.currentTask.isStructual()) {
            if (statement instanceof Inheritance) {
                structuralrules.structuralDecompose1(compound, index, statement, side);
                if (!(compound instanceof SetExt) && !(compound instanceof SetInt))
                    structuralrules.structuralDecompose2(statement);   // {(C-B) --> (C-A), A @ (C-A)} |- A --> B
                else
                    structuralrules.transformSetRelation(compound, statement, side);
            } else if (statement instanceof Similarity) {
                structuralrules.structuralDecompose2(statement);   // {(C-B) --> (C-A), A @ (C-A)} |- A --> B
                if ((compound instanceof SetExt) || (compound instanceof SetInt))
                    structuralrules.transformSetRelation(compound, statement, side);
            } else if ((statement instanceof Implication) && (compound instanceof Negation))
                structuralrules.contraposition(statement);
        }
    }
    
    /* ----- inference with one composition link ----- */
    
    public  void transformTask(Task task,  TaskLink tLink) {          // move to StructuralRules???
        CompoundTerm content = (CompoundTerm) task.getContent().clone();
        short[] indices = tLink.getIndices();
        Term inh = null;
        if (indices.length == 2) {
            inh = content;
        } else if (indices.length == 3) {
            if ((content instanceof Implication) && (content.componentAt(0) instanceof Conjunction))
                inh = ((CompoundTerm) content.componentAt(0)).componentAt(indices[0]);
            else
                inh = content.componentAt(indices[0]);
        }
        if (inh instanceof Inheritance)
            structuralrules.transformProductImage((Inheritance)inh, content, indices, task);
    }
    
}
