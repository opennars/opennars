/*
 * StructuralRules.java
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

import java.util.List;
import nars.core.Memory;
import nars.core.Parameters;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.DifferenceExt;
import nars.language.DifferenceInt;
import nars.language.Equivalence;
import nars.language.ImageExt;
import nars.language.ImageInt;
import nars.language.Implication;
import nars.language.Inheritance;
import nars.language.IntersectionExt;
import nars.language.IntersectionInt;
import nars.language.Negation;
import nars.language.Product;
import nars.language.SetExt;
import nars.language.SetInt;
import nars.language.Similarity;
import nars.language.Statement;
import nars.language.Term;

/**
 * Single-premise inference rules involving compound terms. Input are one
 * sentence (the premise) and one TermLink (indicating a component)
 */
public final class StructuralRules {

   

    /* -------------------- transform between compounds and term -------------------- */
    /**
     * {<S --> P>, S@(S&T)} |- <(S&T) --> (P&T)> {<S --> P>, S@(M-S)} |- <(M-P)
     * --> (M-S)>
     *
     * @param compound The compound term
     * @param index The location of the indicated term in the compound
     * @param statement The premise
     * @param side The location of the indicated term in the premise
     * @param nal Reference to the memory
     */
    static void structuralCompose2(CompoundTerm compound, short index, Statement statement, short side, NAL nal) {
        final Memory mem = nal.mem();
        
        if (compound.equals(statement.term[side])) {
            return;
        }
        /*if (!memory.getCurrentTask().sentence.isJudgment() || (compound.size() == 1)) {
            return; // forward inference only
        }*/
        Term sub = statement.getSubject();
        Term pred = statement.getPredicate();
        List<Term> components = compound.getTermList();
        if (((side == 0) && components.contains(pred)) || ((side == 1) && components.contains(sub))) {
            return;
        }
        if (side == 0) {
            if (components.contains(sub)) {
                sub = compound;
                components.set(index, pred);
                pred = mem.term(compound, components);
            }
        } else {
            if (components.contains(pred)) {
                components.set(index, sub);
                sub = mem.term(compound, components);
                pred = compound;
            }
        }
        
        if ((sub == null) || (pred == null))
            return;
        
        Statement content;
        int order = statement.getTemporalOrder();
        if (switchOrder(compound, index)) {
            content = Statement.make(statement, pred, sub, TemporalRules.reverseOrder(order));
        } else {
            content = Statement.make(statement, sub, pred, order);
        }
        
        if (content == null)
            return;
        
        Sentence sentence = nal.getCurrentTask().sentence;
        TruthValue truth = TruthFunctions.deduction(sentence.truth, nal.memory.param.reliance.floatValue());
        BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.singlePremiseTask(content, truth, budget);
    }

    /**
     * {<(S*T) --> (P*T)>, S@(S*T)} |- <S --> P>
     *
     * @param statement The premise
     * @param nal Reference to the memory
     */
    static void structuralDecompose2(Statement statement, int index, NAL nal) {
        Term subj = statement.getSubject();
        Term pred = statement.getPredicate();
        if (subj.getClass() != pred.getClass()) {
            return;
        }
        
        if (!(subj instanceof Product) && !(subj instanceof SetExt) && !(subj instanceof SetInt)) {
            return; // no abduction on other compounds for now, but may change in the future
        }
        
        CompoundTerm sub = (CompoundTerm) subj;
        CompoundTerm pre = (CompoundTerm) pred;
        if (sub.size() != pre.size() || sub.size() <= index) {
            return;
        }
        
        Term t1 = sub.term[index];
        Term t2 = pre.term[index];
        Statement content;
        int order = statement.getTemporalOrder();
        if (switchOrder(sub, (short) index)) {
            content = Statement.make(statement, t2, t1, TemporalRules.reverseOrder(order));
        } else {
            content = Statement.make(statement, t1, t2, order);
        }
        if (content == null) {
            return;
        }
        Task task = nal.getCurrentTask();
        Sentence sentence = task.sentence;
        TruthValue truth = sentence.truth;
        BudgetValue budget;
        if (sentence.isQuestion() || sentence.isQuest()) {
            budget = BudgetFunctions.compoundBackward(content, nal);
        } else {
            budget = BudgetFunctions.compoundForward(truth, content, nal);
        }
        nal.singlePremiseTask(content, truth, budget);
    }

    /**
     * List the cases where the direction of inheritance is revised in
     * conclusion
     *
     * @param compound The compound term
     * @param index The location of focus in the compound
     * @return Whether the direction of inheritance should be revised
     */
    private static boolean switchOrder(CompoundTerm compound, short index) {
        return ((((compound instanceof DifferenceExt) || (compound instanceof DifferenceInt)) && (index == 1))
                || ((compound instanceof ImageExt) && (index != ((ImageExt) compound).relationIndex))
                || ((compound instanceof ImageInt) && (index != ((ImageInt) compound).relationIndex)));
    }

    /**
     * {<S --> P>, P@(P|Q)} |- <S --> (P|Q)>
     *
     * @param compound The compound term
     * @param index The location of the indicated term in the compound
     * @param statement The premise
     * @param nal Reference to the memory
     */
    static void structuralCompose1(CompoundTerm compound, short index, Statement statement, NAL nal) {
        if (!nal.getCurrentTask().sentence.isJudgment()) {
            return;     // forward inference only
        }
        Term component = compound.term[index];
        Task task = nal.getCurrentTask();
        Sentence sentence = task.sentence;
        int order = sentence.getTemporalOrder();
        TruthValue truth = sentence.truth;
        
        final float reliance = nal.memory.param.reliance.floatValue();
        TruthValue truthDed = TruthFunctions.deduction(truth, reliance);
        TruthValue truthNDed = TruthFunctions.negation(TruthFunctions.deduction(truth, reliance));
        
        Term subj = statement.getSubject();
        Term pred = statement.getPredicate();
        
        if (component.equals(subj)) {
            if (compound instanceof IntersectionExt) {
                structuralStatement(compound, pred, order, truthDed, nal);
            } else if (compound instanceof IntersectionInt) {
            } else if ((compound instanceof DifferenceExt) && (index == 0)) {
                structuralStatement(compound, pred, order, truthDed, nal);
            } else if (compound instanceof DifferenceInt) {
                if (index == 0) {
                } else {
                    structuralStatement(compound, pred, order, truthNDed, nal);
                }
            }
        } else if (component.equals(pred)) {
            if (compound instanceof IntersectionExt) {
            } else if (compound instanceof IntersectionInt) {
                structuralStatement(subj, compound, order, truthDed, nal);
            } else if (compound instanceof DifferenceExt) {
                if (index == 0) {
                } else {
                    structuralStatement(subj, compound, order, truthNDed, nal);
                }
            } else if ((compound instanceof DifferenceInt) && (index == 0)) {
                structuralStatement(subj, compound, order, truthDed, nal);
            }
        }
    }

    /**
     * {<(S|T) --> P>, S@(S|T)} |- <S --> P> {<S --> (P&T)>, P@(P&T)} |- <S --> P>
     *
     * @param compound The compound term
     * @param index The location of the indicated term in the compound
     * @param statement The premise
     * @param nal Reference to the memory
     */
    static void structuralDecompose1(CompoundTerm compound, short index, Statement statement, NAL nal) {
//        if (!memory.getCurrentTask().sentence.isJudgment()) {
//            return;
//        }
        if(index >= compound.term.length) {
            return;
        }
        Term component = compound.term[index];
        Task task = nal.getCurrentTask();
        Sentence sentence = task.sentence;
        int order = sentence.getTemporalOrder();
        TruthValue truth = sentence.truth;
        
        if (truth == null) {
            return;
        }
        
        final float reliance = nal.memory.param.reliance.floatValue();
        TruthValue truthDed = TruthFunctions.deduction(truth, reliance);
        TruthValue truthNDed = TruthFunctions.negation(TruthFunctions.deduction(truth, reliance));
        
        Term subj = statement.getSubject();
        Term pred = statement.getPredicate();
        if (compound.equals(subj)) {
            if (compound instanceof IntersectionInt) {
                structuralStatement(component, pred, order, truthDed, nal);
            } else if ((compound instanceof SetExt) && (compound.size() > 1)) {
                structuralStatement(SetExt.make(component), pred, order, truthDed, nal);
            } else if (compound instanceof DifferenceInt) {
                if (index == 0) {
                    structuralStatement(component, pred, order, truthDed, nal);
                } else {
                    structuralStatement(component, pred, order, truthNDed, nal);
                }
            }
        } else if (compound.equals(pred)) {
            if (compound instanceof IntersectionExt) {
                structuralStatement(subj, component, order, truthDed, nal);
            } else if ((compound instanceof SetInt) && (compound.size() > 1)) {
                structuralStatement(subj, SetInt.make(component), order, truthDed, nal);
            } else if (compound instanceof DifferenceExt) {
                if (index == 0) {
                    structuralStatement(subj, component, order, truthDed, nal);
                } else {
                    structuralStatement(subj, component, order, truthNDed, nal);
                }
            }
        }
    }

    /**
     * Common final operations of the above two methods
     *
     * @param subject The subject of the new task
     * @param predicate The predicate of the new task
     * @param truth The truth value of the new task
     * @param nal Reference to the memory
     */
    private static void structuralStatement(Term subject, Term predicate, int order, TruthValue truth, NAL nal) {
        Task task = nal.getCurrentTask();
        Term oldContent = task.getContent();
        if (oldContent instanceof Statement) {
            Statement content = Statement.make((Statement) oldContent, subject, predicate, order);
            if (content != null) {
                BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
                nal.singlePremiseTask(content, truth, budget);
            }
        }
    }

    /* -------------------- set transform -------------------- */
    /**
     * {<S --> {P}>} |- <S <-> {P}>
     *
     * @param compound The set compound
     * @param statement The premise
     * @param side The location of the indicated term in the premise
     * @param nal Reference to the memory
     */
    static void transformSetRelation(CompoundTerm compound, Statement statement, short side, NAL nal) {
        if (compound.size() > 1) {
            return;
        }
        if (statement instanceof Inheritance) {
            if (((compound instanceof SetExt) && (side == 0)) || ((compound instanceof SetInt) && (side == 1))) {
                return;
            }
        }
        Term sub = statement.getSubject();
        Term pre = statement.getPredicate();
        Statement content;
        if (statement instanceof Inheritance) {
            content = Similarity.make(sub, pre);
        } else {
            if (((compound instanceof SetExt) && (side == 0)) || ((compound instanceof SetInt) && (side == 1))) {
                content = Inheritance.make(pre, sub);
            } else {
                content = Inheritance.make(sub, pre);
            }
        }
        if (content == null) {
            return;
        }

        Task task = nal.getCurrentTask();
        Sentence sentence = task.sentence;
        TruthValue truth = sentence.truth;
        BudgetValue budget;
        if (sentence.isJudgment()) {
            budget = BudgetFunctions.compoundForward(truth, content, nal);
        } else {
            budget = BudgetFunctions.compoundBackward(content, nal);
        }
        nal.singlePremiseTask(content, truth, budget);
    }

    /* -------------------- products and images transform -------------------- */
    /**
     * Equivalent transformation between products and images {<(*, S, M) --> P>,
     * S@(*, S, M)} |- <S --> (/, P, _, M)> {<S --> (/, P, _, M)>, P@(/, P, _,
     * M)} |- <(*, S, M) --> P> {<S --> (/, P, _, M)>, M@(/, P, _, M)} |- <M -->
     * (/, P, S, _)>
     *
     * @param inh An Inheritance statement
     * @param oldContent The whole content
     * @param indices The indices of the TaskLink
     * @param task The task
     * @param memory Reference to the memory
     */
    static void transformProductImage(Inheritance inh, CompoundTerm oldContent, short[] indices, NAL nal) {
        final Memory memory = nal.mem();
        Term subject = inh.getSubject();
        Term predicate = inh.getPredicate();
        if (inh.equals(oldContent)) {
            if (subject instanceof CompoundTerm) {
                transformSubjectPI((CompoundTerm) subject, predicate, nal);
            }
            if (predicate instanceof CompoundTerm) {
                transformPredicatePI(subject, (CompoundTerm) predicate, nal);
            }
            return;
        }
        short index = indices[indices.length - 1];
        short side = indices[indices.length - 2];
        
        Term compT = inh.term[side];
        if (!(compT instanceof CompoundTerm))
            return;
        CompoundTerm comp = (CompoundTerm)compT;
        
        if (comp instanceof Product) {
            if (side == 0) {
                subject = comp.term[index];
                predicate = ImageExt.make((Product) comp, inh.getPredicate(), index);
            } else {
                subject = ImageInt.make((Product) comp, inh.getSubject(), index);
                predicate = comp.term[index];
            }
        } else if ((comp instanceof ImageExt) && (side == 1)) {
            if (index == ((ImageExt) comp).relationIndex) {
                subject = Product.make(comp, inh.getSubject(), index);
                predicate = comp.term[index];
            } else {
                subject = comp.term[index];
                predicate = ImageExt.make((ImageExt) comp, inh.getSubject(), index);
            }
        } else if ((comp instanceof ImageInt) && (side == 0)) {
            if (index == ((ImageInt) comp).relationIndex) {
                subject = comp.term[index];
                predicate = Product.make(comp, inh.getPredicate(), index);
            } else {
                subject = ImageInt.make((ImageInt) comp, inh.getPredicate(), index);
                predicate = comp.term[index];
            }
        } else {
            return;
        }
        
        Inheritance newInh = Inheritance.make(subject, predicate);
        if (newInh == null)
            return;
        
        CompoundTerm content = null;
        if (indices.length == 2) {
            content = newInh;
        } else if ((oldContent instanceof Statement) && (indices[0] == 1)) {
            content = Statement.make((Statement) oldContent, oldContent.term[0], newInh, oldContent.getTemporalOrder());
        } else {
            Term[] componentList;
            Term condition = oldContent.term[0];
            if (((oldContent instanceof Implication) || (oldContent instanceof Equivalence)) && (condition instanceof Conjunction)) {
                componentList = ((CompoundTerm) condition).cloneTerms();
                componentList[indices[1]] = newInh;
                Term newCond = memory.term((CompoundTerm) condition, componentList);
                content = Statement.make((Statement) oldContent, newCond, ((Statement) oldContent).getPredicate(), oldContent.getTemporalOrder());
            } else {
                componentList = oldContent.cloneTerms();
                componentList[indices[0]] = newInh;
                if (oldContent instanceof Conjunction) {
                    Term newContent = memory.term(oldContent, componentList);
                    if (!(newContent instanceof CompoundTerm))
                        return;
                    content = (CompoundTerm)newContent;
                } else if ((oldContent instanceof Implication) || (oldContent instanceof Equivalence)) {
                    content = Statement.make((Statement) oldContent, componentList[0], componentList[1], oldContent.getTemporalOrder());
                }
            }
        }
        
        if (content == null)
            return;
        
        Sentence sentence = nal.getCurrentTask().sentence;
        TruthValue truth = sentence.truth;
        BudgetValue budget;
        if (sentence.isQuestion() || sentence.isQuest()) {
            budget = BudgetFunctions.compoundBackward(content, nal);
        } else {
            budget = BudgetFunctions.compoundForward(truth, content, nal);
        }
        
        nal.singlePremiseTask(content, truth, budget);
    }

    /**
     * Equivalent transformation between products and images when the subject is
     * a compound {<(*, S, M) --> P>, S@(*, S, M)} |- <S --> (/, P, _, M)> {<S
     * --> (/, P, _, M)>, P@(/, P, _, M)} |- <(*, S, M) --> P> {<S --> (/, P, _,
     * M)>, M@(/, P, _, M)} |- <M --> (/, P, S, _)>
     *
     * @param subject The subject term
     * @param predicate The predicate term
     * @param nal Reference to the memory
     */
    private static void transformSubjectPI(CompoundTerm subject, Term predicate, NAL nal) {
        TruthValue truth = nal.getCurrentTask().sentence.truth;
        BudgetValue budget;
        Inheritance inheritance;
        Term newSubj, newPred;
        if (subject instanceof Product) {
            Product product = (Product) subject;
            for (short i = 0; i < product.size(); i++) {
                newSubj = product.term[i];
                newPred = ImageExt.make(product, predicate, i);
                inheritance = Inheritance.make(newSubj, newPred);
                if (inheritance != null) {
                    if (truth == null) {
                        budget = BudgetFunctions.compoundBackward(inheritance, nal);
                    } else {
                        budget = BudgetFunctions.compoundForward(truth, inheritance, nal);
                    }
                    nal.singlePremiseTask(inheritance, truth, budget);
                }
            }
        } else if (subject instanceof ImageInt) {
            ImageInt image = (ImageInt) subject;
            int relationIndex = image.relationIndex;
            for (short i = 0; i < image.size(); i++) {
                if (i == relationIndex) {
                    newSubj = image.term[relationIndex];
                    newPred = Product.make(image, predicate, relationIndex);
                } else {
                    newSubj = ImageInt.make(image, predicate, i);
                    newPred = image.term[i];
                }
                inheritance = Inheritance.make(newSubj, newPred);
                if (inheritance != null) {
                    if (truth == null) {
                        budget = BudgetFunctions.compoundBackward(inheritance, nal);
                    } else {
                        budget = BudgetFunctions.compoundForward(truth, inheritance, nal);
                    }
                    nal.singlePremiseTask(inheritance, truth, budget);
                }
            }
        }
    }

    /**
     * Equivalent transformation between products and images when the predicate
     * is a compound {<(*, S, M) --> P>, S@(*, S, M)} |- <S --> (/, P, _, M)>
     * {<S --> (/, P, _, M)>, P@(/, P, _, M)} |- <(*, S, M) --> P> {<S --> (/,
     * P, _, M)>, M@(/, P, _, M)} |- <M --> (/, P, S, _)>
     *
     * @param subject The subject term
     * @param predicate The predicate term
     * @param nal Reference to the memory
     */
    private static void transformPredicatePI(Term subject, CompoundTerm predicate, NAL nal) {
        TruthValue truth = nal.getCurrentTask().sentence.truth;
        BudgetValue budget;
        Inheritance inheritance;
        Term newSubj, newPred;
        if (predicate instanceof Product) {
            Product product = (Product) predicate;
            for (short i = 0; i < product.size(); i++) {
                newSubj = ImageInt.make(product, subject, i);
                newPred = product.term[i];
                inheritance = Inheritance.make(newSubj, newPred);
                if (inheritance != null) {
                    if (truth == null) {
                        budget = BudgetFunctions.compoundBackward(inheritance, nal);
                    } else {
                        budget = BudgetFunctions.compoundForward(truth, inheritance, nal);
                    }
                    nal.singlePremiseTask(inheritance, truth, budget);
                }
            }
        } else if (predicate instanceof ImageExt) {
            ImageExt image = (ImageExt) predicate;
            int relationIndex = image.relationIndex;
            for (short i = 0; i < image.size(); i++) {
                if (i == relationIndex) {
                    newSubj = Product.make(image, subject, relationIndex);
                    newPred = image.term[relationIndex];
                } else {
                    newSubj = image.term[i];
                    newPred = ImageExt.make(image, subject, i);
                }
                inheritance = Inheritance.make(newSubj, newPred);
                if (inheritance != null) { // jmv <<<<<
                    if (truth == null) {
                        budget = BudgetFunctions.compoundBackward(inheritance, nal);
                    } else {
                        budget = BudgetFunctions.compoundForward(truth, inheritance, nal);
                    }
                    nal.singlePremiseTask(inheritance, truth, budget);
                }
            }
        }
    }


    /* --------------- Disjunction and Conjunction transform --------------- */
    /**
     * {(&&, A, B), A@(&&, A, B)} |- A, or answer (&&, A, B)? using A {(||, A,
     * B), A@(||, A, B)} |- A, or answer (||, A, B)? using A
     *
     * @param compound The premise
     * @param component The recognized component in the premise
     * @param compoundTask Whether the compound comes from the task
     * @param nal Reference to the memory
     */
    static boolean structuralCompound(CompoundTerm compound, Term component, boolean compoundTask, int index, NAL nal) {
        if (component.hasVar()) {
            return false;
        }
        
        if ((compound instanceof Conjunction) && (compound.getTemporalOrder() == TemporalRules.ORDER_FORWARD) && (index != 0)) {
            return false;
        }        
        
        final Term content = compoundTask ? component : compound;
        
        
        Task task = nal.getCurrentTask();

        Sentence sentence = task.sentence;
        TruthValue truth = sentence.truth;

        final float reliance = nal.memory.param.reliance.floatValue();

        BudgetValue budget;
        if (sentence.isQuestion() || sentence.isQuest()) {
            budget = BudgetFunctions.compoundBackward(content, nal);
        } else {  // need to redefine the cases
            if ((sentence.isJudgment()) == (compoundTask == (compound instanceof Conjunction))) {
                truth = TruthFunctions.deduction(truth, reliance);
            } else if (sentence.isGoal()) {
                truth = TruthFunctions.deduction(truth, reliance);
            }else {
                TruthValue v1, v2;
                v1 = TruthFunctions.negation(truth);
                v2 = TruthFunctions.deduction(v1, reliance);
                truth = TruthFunctions.negation(v2);
            }
            budget = BudgetFunctions.forward(truth, nal);
        }
        if (content instanceof CompoundTerm)
            return nal.singlePremiseTask((CompoundTerm)content, truth, budget);
        else
            return false;
    }

    /* --------------- Negation related rules --------------- */
    /**
     * {A, A@(--, A)} |- (--, A)
     *
     * @param content The premise
     * @param nal Reference to the memory
     */
    public static void transformNegation(CompoundTerm content, NAL nal) {
        Task task = nal.getCurrentTask();
        Sentence sentence = task.sentence;
        TruthValue truth = sentence.truth;

        BudgetValue budget;
        
        if (sentence.isJudgment() || sentence.isGoal()) {
            truth = TruthFunctions.negation(truth);
            budget = BudgetFunctions.compoundForward(truth, content, nal);
        } else {
            budget = BudgetFunctions.compoundBackward(content, nal);
        }
        nal.singlePremiseTask(content, truth, budget);
    }

    /**
     * {<A ==> B>, A@(--, A)} |- <(--, B) ==> (--, A)>
     *
     * @param statement The premise
     * @param memory Reference to the memory
     */
    protected static boolean contraposition(final Statement statement, final Sentence sentence, final NAL nal) {
        Memory memory = nal.mem();
        memory.logic.CONTRAPOSITION.commit(statement.complexity);
        
        Term subj = statement.getSubject();
        Term pred = statement.getPredicate();
        
        Statement content = Statement.make(statement, 
                Negation.make(pred), 
                Negation.make(subj), 
                TemporalRules.reverseOrder(statement.getTemporalOrder()));                
        
        if (content == null) return false;
        
        TruthValue truth = sentence.truth;
        BudgetValue budget;
        if (sentence.isQuestion() || sentence.isQuest()) {
            if (content instanceof Implication) {
                budget = BudgetFunctions.compoundBackwardWeak(content, nal);
            } else {
                budget = BudgetFunctions.compoundBackward(content, nal);
            }
            return nal.singlePremiseTask(content, Symbols.QUESTION_MARK, truth, budget);
        } else {
            if (content instanceof Implication) {
                truth = TruthFunctions.contraposition(truth);
            }
            budget = BudgetFunctions.compoundForward(truth, content, nal);
            return nal.singlePremiseTask(content, Symbols.JUDGMENT_MARK, truth, budget);
        }
    }
}
