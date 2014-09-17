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
 * but WITHOUT ANY WARRANTY; without even the abduction warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.inference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import nars.core.Memory;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import static nars.inference.TruthFunctions.abduction;
import static nars.inference.TruthFunctions.anonymousAnalogy;
import static nars.inference.TruthFunctions.comparison;
import static nars.inference.TruthFunctions.deduction;
import static nars.inference.TruthFunctions.induction;
import static nars.inference.TruthFunctions.intersection;
import static nars.inference.TruthFunctions.negation;
import static nars.inference.TruthFunctions.reduceConjunction;
import static nars.inference.TruthFunctions.reduceConjunctionNeg;
import static nars.inference.TruthFunctions.reduceDisjunction;
import static nars.inference.TruthFunctions.union;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.DifferenceExt;
import nars.language.DifferenceInt;
import nars.language.Disjunction;
import nars.language.Equivalence;
import nars.language.ImageExt;
import nars.language.ImageInt;
import nars.language.Implication;
import nars.language.Inheritance;
import nars.language.IntersectionExt;
import nars.language.IntersectionInt;
import nars.language.SetExt;
import nars.language.SetInt;
import nars.language.Similarity;
import nars.language.Statement;
import nars.language.Term;
import static nars.language.Terms.ReduceTillLayer2;
import static nars.language.Terms.reduceComponents;
import static nars.language.Terms.unwrapNegation;
import nars.language.Variable;
import nars.language.Variables;

/**
 * Compound term composition and decomposition rules, with two premises.
 * <p>
 * New compound terms are introduced only in forward inference, while
 * decompositional rules are also used in backward inference
 */
public final class CompositionalRules {
        
    /* --------------- questions which contain answers which are of no value for NARS but need to be answered --------------- */
    /**
     * {(&&,A,B,...)?, A,B} |- {(&&,A,B)} {(&&,A,_components_1_)?,
     * (&&,_part_of_components_1_),A} |- {(&&,A,_part_of_components_1_,B)} and
     * also the case where both are conjunctions, all components need to be
     * subterm of the question-conjunction in order for the subterms of both
     * conjunctions to be collected together.
     *
     * @param sentence The first premise
     * @param belief The second premise
     * @param memory Reference to the memory
     */
    static void dedConjunctionByQuestion(final Sentence sentence, final Sentence belief, final Memory memory) {
        if(sentence==null || belief==null || !sentence.isJudgment() || !belief.isJudgment()) {
            return;
        }
        
        final Term term1 = sentence.content;
        final boolean term1ContainVar = term1.containVar();
        final boolean term1Conjunction = term1 instanceof Conjunction;

        if (term1Conjunction)
            if (term1ContainVar)
                return;
                    
        final Term term2 = belief.content;
        final boolean term2ContainVar = term2.containVar();
        final boolean term2Conjunction = term2 instanceof Conjunction;

        if (term2Conjunction)
            if (term2ContainVar)
                return;
        
        for (final Concept concept : memory.getConcepts()) {

            final List<Task> questions = concept.questions;
            
            for (int i = 0; i < questions.size(); i++) {
                final Task question = questions.get(i);                                    

                //if(question==null) { assert(false); continue; }

                Sentence qu=question.sentence;

                //if(qu==null) { assert(false); continue; }

                final Term pcontent = qu.content;
                if (!(pcontent instanceof Conjunction))
                    continue;
                
                final Conjunction ctpcontent = (Conjunction)pcontent;
                if (ctpcontent.containVar())
                    continue;
                

                if (!term1Conjunction && !term2Conjunction) {
                    if(!ctpcontent.containsTerm(term1) || !ctpcontent.containsTerm(term2)) {
                        continue;
                    }
                }
                else {
                    if (term1Conjunction) {
                        if(!term2Conjunction && !ctpcontent.containsTerm(term2))
                            continue;                    
                        if (!ctpcontent.containsAllTermsOf(term1))
                            continue;                        
                    }

                    if (term2Conjunction) {
                        if (!term1Conjunction && !ctpcontent.containsTerm(term1))
                            continue;                    
                        if (!ctpcontent.containsAllTermsOf(term2))
                            continue;
                    }
                }

                
                Term conj = Conjunction.make(term1, term2, memory);

                /*
                since we already checked for term1 and term2 having a variable, the result
                will not have a variable
                
                if (Variables.containVarDepOrIndep(conj.name()))
                    continue;
                */

                TruthValue truthT = memory.getCurrentTask().sentence.truth;
                TruthValue truthB = memory.getCurrentBelief().truth;
                /*if(truthT==null || truthB==null) {
                    //continue; //<- should this be return and not continue?
                    return;
                }*/
                
                memory.logic.DED_CONJUNCTION_BY_QUESTION.commit();                

                TruthValue truthAnd = intersection(truthT, truthB);
                BudgetValue budget = BudgetFunctions.compoundForward(truthAnd, conj, memory);
                memory.doublePremiseTask(conj, truthAnd, budget);
                break;
            }
        }

    }
    
    /* -------------------- intersections and differences -------------------- */
    /**
     * {<S ==> M>, <P ==> M>} |- {<(S|P) ==> M>, <(S&P) ==> M>, <(S-P) ==> M>,
     * <(P-S) ==> M>}
     *
     * @param taskSentence The first premise
     * @param belief The second premise
     * @param index The location of the shared term
     * @param memory Reference to the memory
     */
    static void composeCompound(final Statement taskContent, final Statement beliefContent, final int index, final Memory memory) {
        if ((!memory.getCurrentTask().sentence.isJudgment()) || (taskContent.getClass() != beliefContent.getClass())) {
            return;
        }
        final Term componentT = taskContent.term[1 - index];
        final Term componentB = beliefContent.term[1 - index];
        final Term componentCommon = taskContent.term[index];
        int order1 = taskContent.getTemporalOrder();
        int order2 = beliefContent.getTemporalOrder();
        int order = TemporalRules.composeOrder(order1, order2);
        if (order == TemporalRules.ORDER_INVALID) {
            return;
        }
        if ((componentT instanceof CompoundTerm) && ((CompoundTerm) componentT).containsAllTermsOf(componentB)) {
            decomposeCompound((CompoundTerm) componentT, componentB, componentCommon, index, true, order, memory);
            return;
        } else if ((componentB instanceof CompoundTerm) && ((CompoundTerm) componentB).containsAllTermsOf(componentT)) {
            decomposeCompound((CompoundTerm) componentB, componentT, componentCommon, index, false, order, memory);
            return;
        }
        final TruthValue truthT = memory.getCurrentTask().sentence.truth;
        final TruthValue truthB = memory.getCurrentBelief().truth;
        final TruthValue truthOr = union(truthT, truthB);
        final TruthValue truthAnd = intersection(truthT, truthB);
        TruthValue truthDif = null;
        Term termOr = null;
        Term termAnd = null;
        Term termDif = null;
        if (index == 0) {
            if (taskContent instanceof Inheritance) {
                termOr = IntersectionInt.make(componentT, componentB, memory);
                termAnd = IntersectionExt.make(componentT, componentB, memory);
                if (truthB.isNegative()) {
                    if (!truthT.isNegative()) {
                        termDif = DifferenceExt.make(componentT, componentB, memory);
                        truthDif = intersection(truthT, negation(truthB));
                    }
                } else if (truthT.isNegative()) {
                    termDif = DifferenceExt.make(componentB, componentT, memory);
                    truthDif = intersection(truthB, negation(truthT));
                }
            } else if (taskContent instanceof Implication) {
                termOr = Disjunction.make(componentT, componentB, memory);
                termAnd = Conjunction.make(componentT, componentB, memory);
            }
            processComposed(taskContent, componentCommon.clone(), termOr, order, truthOr, memory);
            processComposed(taskContent, componentCommon.clone(), termAnd, order, truthAnd, memory);
            processComposed(taskContent, componentCommon.clone(), termDif, order, truthDif, memory);
        } else {    // index == 1
            if (taskContent instanceof Inheritance) {
                termOr = IntersectionExt.make(componentT, componentB, memory);
                termAnd = IntersectionInt.make(componentT, componentB, memory);
                if (truthB.isNegative()) {
                    if (!truthT.isNegative()) {
                        termDif = DifferenceInt.make(componentT, componentB, memory);
                        truthDif = intersection(truthT, negation(truthB));
                    }
                } else if (truthT.isNegative()) {
                    termDif = DifferenceInt.make(componentB, componentT, memory);
                    truthDif = intersection(truthB, negation(truthT));
                }
            } else if (taskContent instanceof Implication) {
                termOr = Conjunction.make(componentT, componentB, memory);
                termAnd = Disjunction.make(componentT, componentB, memory);
            }
            processComposed(taskContent, termOr, componentCommon.clone(), order, truthOr, memory);
            processComposed(taskContent, termAnd, componentCommon.clone(), order, truthAnd, memory);
            processComposed(taskContent, termDif, componentCommon.clone(), order, truthDif, memory);
        }
    }

    /**
     * Finish composing implication term
     *
     * @param premise1 Type of the contentInd
     * @param subject Subject of contentInd
     * @param predicate Predicate of contentInd
     * @param truth TruthValue of the contentInd
     * @param memory Reference to the memory
     */
    private static void processComposed(Statement statement, Term subject, Term predicate, int order, TruthValue truth, Memory memory) {
        if ((subject == null) || (predicate == null)) {
            return;
        }
        Term content = Statement.make(statement, subject, predicate, order, memory);
        if ((content == null) || statement==null || content.equals(statement) || content.equals(memory.getCurrentBelief().content)) {
            return;
        }
        BudgetValue budget = BudgetFunctions.compoundForward(truth, content, memory);
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * {<(S|P) ==> M>, <P ==> M>} |- <S ==> M>
     *
     * @param implication The implication term to be decomposed
     * @param componentCommon The part of the implication to be removed
     * @param term1 The other term in the contentInd
     * @param index The location of the shared term: 0 for subject, 1 for
     * predicate
     * @param compoundTask Whether the implication comes from the task
     * @param memory Reference to the memory
     */
    private static void decomposeCompound(CompoundTerm compound, Term component, Term term1, int index, boolean compoundTask, int order, Memory memory) {
        
        if ((compound instanceof Statement) || (compound instanceof ImageExt) || (compound instanceof ImageInt)) {
            return;
        }
        Term term2 = reduceComponents(compound, component, memory);
        if (term2 == null) {
            return;
        }
        Task task = memory.getCurrentTask();
        Sentence sentence = task.sentence;
        Sentence belief = memory.getCurrentBelief();
        Statement oldContent = (Statement) task.getContent();
        TruthValue v1,
                v2;
        if (compoundTask) {
            v1 = sentence.truth;
            v2 = belief.truth;
        } else {
            v1 = belief.truth;
            v2 = sentence.truth;
        }
        TruthValue truth = null;
        Term content;
        if (index == 0) {
            content = Statement.make(oldContent, term1, term2, order, memory);
            if (content == null) {
                return;
            }
            if (oldContent instanceof Inheritance) {
                if (compound instanceof IntersectionExt) {
                    truth = reduceConjunction(v1, v2);
                } else if (compound instanceof IntersectionInt) {
                    truth = reduceDisjunction(v1, v2);
                } else if ((compound instanceof SetInt) && (component instanceof SetInt)) {
                    truth = reduceConjunction(v1, v2);
                } else if ((compound instanceof SetExt) && (component instanceof SetExt)) {
                    truth = reduceDisjunction(v1, v2);
                } else if (compound instanceof DifferenceExt) {
                    if (compound.term[0].equals(component)) {
                        truth = reduceDisjunction(v2, v1);
                    } else {
                        truth = reduceConjunctionNeg(v1, v2);
                    }
                }
            } else if (oldContent instanceof Implication) {
                if (compound instanceof Conjunction) {
                    truth = reduceConjunction(v1, v2);
                } else if (compound instanceof Disjunction) {
                    truth = reduceDisjunction(v1, v2);
                }
            }
        } else {
            content = Statement.make(oldContent, term2, term1, order, memory);
            if (content == null) {
                return;
            }
            if (oldContent instanceof Inheritance) {
                if (compound instanceof IntersectionInt) {
                    truth = reduceConjunction(v1, v2);
                } else if (compound instanceof IntersectionExt) {
                    truth = reduceDisjunction(v1, v2);
                } else if ((compound instanceof SetExt) && (component instanceof SetExt)) {
                    truth = reduceConjunction(v1, v2);
                } else if ((compound instanceof SetInt) && (component instanceof SetInt)) {
                    truth = reduceDisjunction(v1, v2);
                } else if (compound instanceof DifferenceInt) {
                    if (compound.term[1].equals(component)) {
                        truth = reduceDisjunction(v2, v1);
                    } else {
                        truth = reduceConjunctionNeg(v1, v2);
                    }
                }
            } else if (oldContent instanceof Implication) {
                if (compound instanceof Disjunction) {
                    truth = reduceConjunction(v1, v2);
                } else if (compound instanceof Conjunction) {
                    truth = reduceDisjunction(v1, v2);
                }
            }
        }
        if (truth != null) {
            BudgetValue budget = BudgetFunctions.compoundForward(truth, content, memory);
            memory.doublePremiseTask(content, truth, budget);
        }
    }

    /**
     * {(||, S, P), P} |- S {(&&, S, P), P} |- S
     *
     * @param implication The implication term to be decomposed
     * @param componentCommon The part of the implication to be removed
     * @param compoundTask Whether the implication comes from the task
     * @param memory Reference to the memory
     */
    static void decomposeStatement(CompoundTerm compound, Term component, boolean compoundTask, int index, Memory memory) {
        if ((compound instanceof Conjunction) && (compound.getTemporalOrder() == TemporalRules.ORDER_FORWARD) && (index != 0)) {
            return;
        }
        
        Task task = memory.getCurrentTask();
        Sentence taskSentence = task.sentence;
        Sentence belief = memory.getCurrentBelief();
        Term content = reduceComponents(compound, component, memory);
        if (content == null) {
            return;
        }
        TruthValue truth = null;
        BudgetValue budget;
        if (taskSentence.isQuestion() || taskSentence.isQuest()) {
            budget = BudgetFunctions.compoundBackward(content, memory);
            memory.doublePremiseTask(content, truth, budget);
            // special inference to answer conjunctive questions with query variables
            if (Variables.containVarQuery(taskSentence.content.name())) {
                Concept contentConcept = memory.concept(content);
                if (contentConcept == null) {
                    return;
                }
                Sentence contentBelief = contentConcept.getBelief(task);
                if (contentBelief == null) {
                    return;
                }
                Task contentTask = new Task(contentBelief, task.budget);
                memory.setCurrentTask(contentTask);
                Term conj = Conjunction.make(component, content, memory);
                truth = intersection(contentBelief.truth, belief.truth);
                budget = BudgetFunctions.compoundForward(truth, conj, memory);
                memory.doublePremiseTask(conj, truth, budget);
            }        
        } else {
            TruthValue v1, v2;
            if (compoundTask) {
                v1 = taskSentence.truth;
                v2 = belief.truth;
            } else {
                v1 = belief.truth;
                v2 = taskSentence.truth;
            }
            if (compound instanceof Conjunction) {
                if (taskSentence.isGoal()) {
                    if (compoundTask) {
                        truth = intersection(v1, v2);
                    } else {
                        return;
                    }
                } else { // isJudgment
                    truth = reduceConjunction(v1, v2);
                }
                } else if (compound instanceof Disjunction) {
                if (taskSentence.isGoal()) {
                    if (compoundTask) {
                        truth = reduceConjunction(v2, v1);
                    } else {
                        return;
                    }
                } else {  // isJudgment
                        truth = reduceDisjunction(v1, v2);
                    }
                } else {
                return;
            }
            budget = BudgetFunctions.compoundForward(truth, content, memory);
        }
        memory.doublePremiseTask(content, truth, budget);
    }
    
    
    static final Variable varInd1 = new Variable("$varInd1");
    static final Variable varInd2 = new Variable("$varInd2");
    static final Variable varDep = new Variable("#varDep");
    static final Variable varDep2 = new Variable("#varDep2");
    static final Variable depIndVar1 = new Variable("#depIndVar1");
    static final Variable depIndVar2 = new Variable("#depIndVar2");    

    /* --------------- rules used for variable introduction --------------- */
    // forward inference only?
    /**
     * Introduce a dependent variable in an outer-layer conjunction {<S --> P1>,
     * <S --> P2>} |- (&&, <#x --> P1>, <#x --> P2>)
     *
     * @param taskContent The first premise <M --> S>
     * @param beliefContent The second premise <M --> P>
     * @param index The location of the shared term: 0 for subject, 1 for
     * predicate
     * @param memory Reference to the memory
     */
    public static void introVarOuter(final Statement taskContent, final Statement beliefContent, final int index, final Memory memory) {
        
        if (!(taskContent instanceof Inheritance)) {
            return;
        }
        
        
        
        Term term11, term12, term21, term22, commonTerm;
        HashMap<Term, Term> subs = new HashMap<>();
        if (index == 0) {
            term11 = varInd1;
            term21 = varInd1;
            term12 = taskContent.getPredicate();
            term22 = beliefContent.getPredicate();
            if ((term12 instanceof ImageExt) && (term22 instanceof ImageExt)) {
                commonTerm = ((ImageExt) term12).getTheOtherComponent();
                if ((commonTerm == null) || !(/*(ImageExt)*/term22).containsTermRecursively(commonTerm)) {
                    commonTerm = ((ImageExt) term22).getTheOtherComponent();
                    if ((commonTerm == null) || !(/*(ImageExt)*/term12).containsTermRecursively(commonTerm)) {
                        commonTerm = null;
                    }
                }
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term12 = ((CompoundTerm) term12).applySubstitute(subs);
                    term22 = ((CompoundTerm) term22).applySubstitute(subs);
                }
            }
        } else {
            term11 = taskContent.getSubject();
            term21 = beliefContent.getSubject();
            term12 = varInd1;
            term22 = varInd1;
            if ((term11 instanceof ImageInt) && (term21 instanceof ImageInt)) {
                commonTerm = ((ImageInt) term11).getTheOtherComponent();
                if ((commonTerm == null) || !(/*(ImageInt)*/term21).containsTermRecursively(commonTerm)) {
                    commonTerm = ((ImageInt) term21).getTheOtherComponent();
                    if ((commonTerm == null) || !(/*(ImageInt)*/term11).containsTermRecursively(commonTerm)) {
                        commonTerm = null;
                    }
                }
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term11 = ((CompoundTerm) term11).applySubstitute(subs);
                    term21 = ((CompoundTerm) term21).applySubstitute(subs);
                }
            }
        }
        Statement state1 = Inheritance.make(term11, term12, memory);
        Statement state2 = Inheritance.make(term21, term22, memory);
        Term content = Implication.make(state1, state2, memory);
        if (content == null) {
            return;
        }
        
        TruthValue truthT = memory.getCurrentTask().sentence.truth;
        TruthValue truthB = memory.getCurrentBelief().truth;
        /*
        if (truthT == null)
            throw new RuntimeException("CompositionalRules.introVarOuter: current task has null truth: " + memory.getCurrentTask());
        
        if (truthB == null)
            throw new RuntimeException("CompositionalRules.introVarOuter: current belief has null truth: " + memory.getCurrentBelief());
        */
        if ((truthT==null) || (truthB == null))
            return;
        
        TruthValue truth = induction(truthT, truthB);
        BudgetValue budget = BudgetFunctions.compoundForward(truth, content, memory);
        memory.doublePremiseTask(content, truth, budget);
        content = Implication.make(state2, state1, memory);
        truth = induction(truthB, truthT);
        budget = BudgetFunctions.compoundForward(truth, content, memory);
        memory.doublePremiseTask(content, truth, budget);
        content = Equivalence.make(state1, state2, memory);
        truth = comparison(truthT, truthB);
        budget = BudgetFunctions.compoundForward(truth, content, memory);
        memory.doublePremiseTask(content, truth, budget);

        if (index == 0) {
            state1 = Inheritance.make(varDep, term12, memory);
            state2 = Inheritance.make(varDep, term22, memory);
        } else {
            state1 = Inheritance.make(term11, varDep, memory);
            state2 = Inheritance.make(term21, varDep, memory);
        }
        content = Conjunction.make(state1, state2, memory);
        truth = intersection(truthT, truthB);
        budget = BudgetFunctions.compoundForward(truth, content, memory);
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * {<M --> S>, <C ==> <M --> P>>} |- <(&&, <#x --> S>, C) ==> <#x --> P>>
     * {<M --> S>, (&&, C, <M --> P>)} |- (&&, C, <<#x --> S> ==> <#x --> P>>)
     *
     * @param taskContent The first premise directly used in internal induction,
     * <M --> S>
     * @param beliefContent The componentCommon to be used as a premise in
     * internal induction, <M --> P>
     * @param oldCompound The whole contentInd of the first premise, Implication
     * or Conjunction
     * @param memory Reference to the memory
     */
    static void introVarInner(Statement premise1, Statement premise2, CompoundTerm oldCompound, Memory memory) {
        Task task = memory.getCurrentTask();
        Sentence taskSentence = task.sentence;
        if (!taskSentence.isJudgment() || (premise1.getClass() != premise2.getClass()) || oldCompound.containsTerm(premise1)) {
            return;
        }
        Term subject1 = premise1.getSubject();
        Term subject2 = premise2.getSubject();
        Term predicate1 = premise1.getPredicate();
        Term predicate2 = premise2.getPredicate();
        Term commonTerm1, commonTerm2;
        if (subject1.equals(subject2)) {
            commonTerm1 = subject1;
            commonTerm2 = secondCommonTerm(predicate1, predicate2, 0);
        } else if (predicate1.equals(predicate2)) {
            commonTerm1 = predicate1;
            commonTerm2 = secondCommonTerm(subject1, subject2, 0);
        } else {
            return;
        }
        Sentence belief = memory.getCurrentBelief();
        
        HashMap<Term, Term> substitute = new HashMap<>();
        substitute.put(commonTerm1, varDep2);
        CompoundTerm content = (CompoundTerm) Conjunction.make(premise1, oldCompound, memory);
        content = content.applySubstitute(substitute);
        TruthValue truth = intersection(taskSentence.truth, belief.truth);
        BudgetValue budget = BudgetFunctions.forward(truth, memory);
        memory.doublePremiseTask(content, truth, budget);
        
        substitute.clear();        
        
        substitute.put(commonTerm1, varInd1);
        if (commonTerm2 != null) {
            substitute.put(commonTerm2, varInd2);
        }
        content = Implication.make(premise1, oldCompound, memory);
        if (content == null)
            return;
        content = content.applySubstitute(substitute);
        if (premise1.equals(taskSentence.content)) {
            truth = induction(belief.truth, taskSentence.truth);
        } else {
            truth = induction(taskSentence.truth, belief.truth);
        }
        budget = BudgetFunctions.forward(truth, memory);
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * Introduce a second independent variable into two terms with a common
     * component
     *
     * @param term1 The first term
     * @param term2 The second term
     * @param index The index of the terms in their statement
     */
    private static Term secondCommonTerm(Term term1, Term term2, int index) {
        Term commonTerm = null;
        if (index == 0) {
            if ((term1 instanceof ImageExt) && (term2 instanceof ImageExt)) {
                commonTerm = ((ImageExt) term1).getTheOtherComponent();
                if ((commonTerm == null) || !term2.containsTermRecursively(commonTerm)) {
                    commonTerm = ((ImageExt) term2).getTheOtherComponent();
                    if ((commonTerm == null) || !term1.containsTermRecursively(commonTerm)) {
                        commonTerm = null;
                    }
                }
            }
        } else {
            if ((term1 instanceof ImageInt) && (term2 instanceof ImageInt)) {
                commonTerm = ((ImageInt) term1).getTheOtherComponent();
                if ((commonTerm == null) || !term2.containsTermRecursively(commonTerm)) {
                    commonTerm = ((ImageInt) term2).getTheOtherComponent();
                    if ((commonTerm == null) || !term1.containsTermRecursively(commonTerm)) {
                        commonTerm = null;
                    }
                }
            }
        }
        return commonTerm;
    }
    
    public static void eliminateVariableOfConditionAbductive(final int figure, final Sentence sentence, final Sentence belief, final Memory memory) {
        Statement T1 = (Statement)sentence.content.clone();
        Statement T2 = (Statement)belief.content.clone();

        Term S1 = T2.getSubject();
        Term S2 = T1.getSubject();
        Term P1 = T2.getPredicate();
        Term P2 = T1.getPredicate();
        
        HashMap<Term,Term> res1=new HashMap<>();
        HashMap<Term,Term> res2=new HashMap<>();
        HashMap<Term,Term> res3=new HashMap<>();
        HashMap<Term,Term> res4=new HashMap<>();
        
        if(figure==21) {
            res1.clear();
            res2.clear();
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, P1, S2, res1, res2); //this part is 
            T1 = (Statement)T1.applySubstitute(res2); //independent, the rule works if it unifies
            T2 = (Statement)T2.applySubstitute(res1);            
            S1 = T2.getSubject(); S2 = T1.getSubject(); P1 = T2.getPredicate(); P2 = T1.getPredicate(); //update the variables because T1 and T2 may have changed
            
            if(S1 instanceof Conjunction) {
                //try to unify P2 with a component
                for(final Term s1 : ((CompoundTerm)S1).cloneTerms()) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if(Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, P2, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)S1).cloneTerms()) {
                            if (!(s2 instanceof CompoundTerm)) continue;
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
            if(P2 instanceof Conjunction) {
                //try to unify S1 with a component
                for(final Term s1 : ((CompoundTerm)P2).cloneTerms()) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if(Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, S1, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)P2).cloneTerms()) {
                            if (!(s2 instanceof CompoundTerm)) continue;
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
        }
        
        if(figure==12) {
            res1.clear();
            res2.clear();
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, S1, P2, res1, res2); //this part is 
            T1 = (Statement)T1.applySubstitute(res2); //independent, the rule works if it unifies
            T2 = (Statement)T2.applySubstitute(res1);            
            S1 = T2.getSubject(); S2 = T1.getSubject(); P1 = T2.getPredicate(); P2 = T1.getPredicate(); //update the variables because T1 and T2 may have changed
            
            if(S2 instanceof Conjunction) {
                //try to unify P1 with a component
                for(final Term s1 : ((CompoundTerm)S2).cloneTerms()) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if(Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, P1, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)S2).cloneTerms()) {
                            if (!(s2 instanceof CompoundTerm)) continue;
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
            if(P1 instanceof Conjunction) {
                //try to unify S2 with a component
                for(final Term s1 : ((CompoundTerm)P1).cloneTerms()) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if(Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, S2, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)P1).cloneTerms()) {
                            if (!(s2 instanceof CompoundTerm)) continue;
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
        }
        
        if(figure==11) {
            res1.clear();
            res2.clear();
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, S1, S2, res1, res2); //this part is 
            T1 = (Statement)T1.applySubstitute(res2); //independent, the rule works if it unifies
            T2 = (Statement)T2.applySubstitute(res1);            
            S1 = T2.getSubject(); S2 = T1.getSubject(); P1 = T2.getPredicate(); P2 = T1.getPredicate(); //update the variables because T1 and T2 may have changed
            
            if(P1 instanceof Conjunction) {
                //try to unify P2 with a component
                for(final Term s1 : ((CompoundTerm)P1).cloneTerms()) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if(Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, P2, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)P1).cloneTerms()) {
                            if (!(s2 instanceof CompoundTerm)) continue;                            
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if ((!s2.equals(s1)) && (sentence.truth!=null) && (belief.truth!=null)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
            if(P2 instanceof Conjunction) {
                //try to unify P1 with a component
                for(final Term s1 : ((CompoundTerm)P2).cloneTerms()) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if(Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, P1, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)P2).cloneTerms()) {
                            if (!(s2 instanceof CompoundTerm)) continue;
                            
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
        }
        
        if(figure==22) {
            res1.clear();
            res2.clear();
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, P1, P2, res1, res2); //this part is 
            T1 = (Statement)T1.applySubstitute(res2); //independent, the rule works if it unifies
            T2 = (Statement)T2.applySubstitute(res1);            
            S1 = T2.getSubject(); S2 = T1.getSubject(); P1 = T2.getPredicate(); P2 = T1.getPredicate(); //update the variables because T1 and T2 may have changed
            
            if(S1 instanceof Conjunction) {
                //try to unify S2 with a component
                for(final Term s1 : ((CompoundTerm)S1).cloneTerms()) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if(Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, S2, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)S1).cloneTerms()) {
                            if (!(s2 instanceof CompoundTerm)) continue;
                            
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
            if(S2 instanceof Conjunction) {
                //try to unify S1 with a component
                for(final Term s1 : ((CompoundTerm)S2).cloneTerms()) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if(Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, S1, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)S2).cloneTerms()) {
                            if (!(s2 instanceof CompoundTerm)) continue;
                            
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
        }
    }
    
    static void IntroVarSameSubjectOrPredicate(final Sentence originalMainSentence, final Sentence subSentence, final Term component, final Term content, final int index, final Memory memory) {
        Term T1 = originalMainSentence.content;
        if (!(T1 instanceof CompoundTerm) || !(content instanceof CompoundTerm)) {
            return;
        }
        CompoundTerm T = (CompoundTerm) T1;
        CompoundTerm T2 = (CompoundTerm) content.clone();
        if ((component instanceof Inheritance && content instanceof Inheritance)
                || (component instanceof Similarity && content instanceof Similarity)) {
            //CompoundTerm result = T;
            if (component.equals(content)) {
                return; //wouldnt make sense to create a conjunction here, would contain a statement twice
            }
            if (((Statement) component).getPredicate().equals(((Statement) content).getPredicate()) && !(((Statement) component).getPredicate() instanceof Variable)) {
                
                CompoundTerm zw = (CompoundTerm) T.term[index].clone();
                zw = (CompoundTerm) zw.setComponent(1, depIndVar1, memory);
                T2 = (CompoundTerm) T2.setComponent(1, depIndVar1, memory);
                Conjunction res = (Conjunction) Conjunction.make(zw, T2, memory);
                T = (CompoundTerm) T.setComponent(index, res, memory);
            } else if (((Statement) component).getSubject().equals(((Statement) content).getSubject()) && !(((Statement) component).getSubject() instanceof Variable)) {
                
                CompoundTerm zw = (CompoundTerm) T.term[index].clone();
                zw = (CompoundTerm) zw.setComponent(0, depIndVar2, memory);
                T2 = (CompoundTerm) T2.setComponent(0, depIndVar2, memory);
                Conjunction res = (Conjunction) Conjunction.make(zw, T2, memory);
                T = (CompoundTerm) T.setComponent(index, res, memory);
            }
            TruthValue truth = induction(originalMainSentence.truth, subSentence.truth);
            BudgetValue budget = BudgetFunctions.compoundForward(truth, T, memory);
            memory.doublePremiseTask(T, truth, budget);
        }
    }

    public static boolean sEqualsP(final Term T) {
        if(T instanceof Statement) {
            Statement st=(Statement) T;
            if(st.getSubject().equals(st.getPredicate())) {
                return true;
            }
        }
        return false;
    }
    
    static boolean dedSecondLayerVariableUnification(final Task task, final Memory memory) {
        
        
        Sentence taskSentence=task.sentence;
        
        if(taskSentence==null || taskSentence.isQuestion()) {
            return false;
        }
        
        Term taskterm=taskSentence.content;
        
        if (!(taskterm instanceof CompoundTerm)) {
            return false;
        }
        
        //lets just allow conjunctions, implication and equivalence for now
        if (!((taskterm instanceof Disjunction || taskterm instanceof Conjunction || taskterm instanceof Equivalence || taskterm instanceof Implication)))
            return false;
        
        if (!taskterm.containVar()) {
            return false;
        }           


        
        boolean unifiedAnything = false;
        int remainingUnifications = 1; //memory.param.variableUnificationLayer2_MaxUnificationsPerCycle.get();

        int maxUnificationAttempts = 1; //memory.param.variableUnificationLayer2_ConceptAttemptsPerCycle.get();

        //these are intiailized further into the first cycle below. afterward, they are clear() and re-used for subsequent cycles to avoid reallocation cost
        ArrayList<CompoundTerm> terms_dependent = null;
        ArrayList<CompoundTerm> terms_independent = null;
        HashMap<Term, Term> Values = null; 
        HashMap<Term, Term> Values2 = null; 
        HashMap<Term, Term> Values3 = null;
        HashMap<Term, Term> Values4 = null;
        HashMap<Term, Term> smap = null;

        for (int k = 0; k < maxUnificationAttempts; k++ ) {               
            Concept second = memory.sampleNextConcept();

            if(second==null) {
                //no more concepts, stop
                break;
            }

            //prevent unification with itself
            if (second.term.equals(taskterm))
                continue;

            Term secterm=second.term;
            if(second.beliefs.isEmpty()) {
                continue;
            }

            if (memory.getRecorder().isActive()) {
                memory.getRecorder().append(" * Selected Concept (For Second Layer Unification): " + second.term);
            }            


            Sentence second_belief = second.beliefs.get(Memory.randomNumber.nextInt(second.beliefs.size()));

            //explaination(patrick): it is not completely valid to do temporal induction only on sequence of occured events
            //there are rare situations which demand that it is also done in a overarching manner
            //if(taskSentence.getOccurenceTime()!=Stamp.ETERNAL && second_belief.getOccurenceTime()!=Stamp.ETERNAL)
            //{
            //    TemporalRules.temporalInduction(taskSentence, second_belief, memory);
            //}

            TruthValue truthSecond=second_belief.truth;

            if (terms_dependent == null) {
                final int initialTermListSize = 8;                    
                terms_dependent=new ArrayList<>(initialTermListSize);
                terms_independent=new ArrayList<>(initialTermListSize);
                Values = newVariableSubstitutionMap(); 
                Values2 = newVariableSubstitutionMap(); 
                Values3 = newVariableSubstitutionMap();
                Values4 = newVariableSubstitutionMap();
                smap = newVariableSubstitutionMap();
            }

            //we have to select a random belief
            terms_dependent.clear();
            terms_independent.clear();

            //ok, we have selected a second concept, we know the truth value of a belief of it, lets now go through taskterms term
            //for two levels, and remember the terms which unify with second
            Term[] components_level1 = ((CompoundTerm)taskterm).term;            
            Term secterm_unwrap=unwrapNegation(secterm);



            for(final Term T1 : components_level1) {
                Term T1_unwrap=unwrapNegation(T1);
                Values.clear(); //we are only interested in first variables

                smap.clear();

                if(Variables.findSubstitute(Symbols.VAR_DEPENDENT, T1_unwrap, secterm_unwrap,Values,smap)) {
                    CompoundTerm taskterm_subs=((CompoundTerm)taskterm);
                    taskterm_subs = taskterm_subs.applySubstitute(Values);
                    taskterm_subs=ReduceTillLayer2(taskterm_subs,secterm,memory);
                    if(taskterm_subs!=null && !(Variables.indepVarUsedInvalid(taskterm_subs))) {
                        terms_dependent.add(taskterm_subs);
                    }
                }


                Values2.clear(); //we are only interested in first variables
                smap.clear();

                if(Variables.findSubstitute(Symbols.VAR_INDEPENDENT, T1_unwrap, secterm_unwrap,Values2, smap)) {
                    CompoundTerm taskterm_subs=((CompoundTerm)taskterm.clone());
                    taskterm_subs = taskterm_subs.applySubstitute(Values2);
                    taskterm_subs=ReduceTillLayer2(taskterm_subs,secterm,memory);
                    if(taskterm_subs!=null && !(Variables.indepVarUsedInvalid(taskterm_subs))) {

                        terms_independent.add(taskterm_subs);
                    }
                }

                if(!((T1_unwrap instanceof Implication) || (T1_unwrap instanceof Equivalence) || (T1_unwrap instanceof Conjunction) || (T1_unwrap instanceof Disjunction))) {
                    continue;
                }

                if(T1_unwrap instanceof CompoundTerm) {
                    Term[] components_level2 = ((CompoundTerm)T1_unwrap).term;

                    for(final Term T2 : components_level2) {
                        Term T2_unwrap=unwrapNegation(T2).clone(); 

                        Values3.clear(); //we are only interested in first variables
                        smap.clear();

                        if(Variables.findSubstitute(Symbols.VAR_DEPENDENT, T2_unwrap, secterm_unwrap,Values3, smap)) {
                            //terms_dependent_compound_terms.put(Values3, (CompoundTerm)T1_unwrap);
                            CompoundTerm taskterm_subs=((CompoundTerm)taskterm.clone());
                            taskterm_subs = taskterm_subs.applySubstitute(Values3);
                            taskterm_subs=ReduceTillLayer2(taskterm_subs,secterm,memory);
                            if(taskterm_subs!=null && !(Variables.indepVarUsedInvalid(taskterm_subs))) {
                                terms_dependent.add(taskterm_subs);
                            }
                        }

                        Values4.clear(); //we are only interested in first variables
                        smap.clear();

                        if(Variables.findSubstitute(Symbols.VAR_INDEPENDENT, T2_unwrap, secterm_unwrap,Values4, smap)) {
                            //terms_independent_compound_terms.put(Values4, (CompoundTerm)T1_unwrap);
                            CompoundTerm taskterm_subs=((CompoundTerm)taskterm.clone());
                            taskterm_subs = taskterm_subs.applySubstitute(Values4);
                            taskterm_subs = ReduceTillLayer2(taskterm_subs,secterm,memory);
                            if(taskterm_subs!=null && !(Variables.indepVarUsedInvalid(taskterm_subs))) {
                                terms_independent.add(taskterm_subs);
                            }
                        }
                    }
                }
            }

            Stamp ss = new Stamp(taskSentence.stamp, second_belief.stamp,memory.getTime());

            dedSecondLayerVariableUnificationTerms(memory, taskSentence, task, 
                    second_belief, ss, terms_dependent, 
                    anonymousAnalogy(taskSentence.truth, truthSecond), 
                    taskSentence.truth, truthSecond, false);

            dedSecondLayerVariableUnificationTerms(memory, taskSentence, task, 
                    second_belief, ss, terms_independent, 
                    deduction(taskSentence.truth, truthSecond), 
                    taskSentence.truth, truthSecond, true);

            final int termsIndependent = terms_independent.size();
            for(int i=0;i<termsIndependent;i++) {
                Term result = terms_independent.get(i);
                TruthValue truth = deduction(taskSentence.truth, truthSecond);

                char mark=Symbols.JUDGMENT_MARK;
                if(taskSentence.isGoal() || second_belief.isGoal()) {
                    truth = TruthFunctions.abduction(taskSentence.truth, truthSecond);
                    mark=Symbols.GOAL_MARK;
                }

                Stamp useEvidentalBase=new Stamp(taskSentence.stamp, second_belief.stamp,memory.getTime());
                Sentence newSentence = new Sentence(result, mark, truth, 
                        new Stamp(taskSentence.stamp, memory.getTime(), useEvidentalBase) );                

                BudgetValue budget = BudgetFunctions.compoundForward(truth, newSentence.content, memory);
                if(sEqualsP(newSentence.content)) {
                    //changed from return to continue to allow furhter processing
                    continue;
                }

                Task newTask = new Task(newSentence, budget, task, null);
                Task dummy = new Task(second_belief, budget, task, null);



                memory.setCurrentBelief(taskSentence);
                memory.setCurrentTask(dummy);

                memory.logic.DED_SECOND_LAYER_VARIABLE_UNIFICATION.commit();
                memory.derivedTask(newTask, false, false, taskSentence, second_belief);

                unifiedAnything = true;
            }

            remainingUnifications--;

            if (remainingUnifications == 0)
                break;

        }

        return unifiedAnything;
    }
    

    private static void dedSecondLayerVariableUnificationTerms(Memory memory, Sentence taskSentence, Task task, Sentence second_belief, Stamp s, ArrayList<CompoundTerm> terms_dependent, TruthValue truth, TruthValue t1, TruthValue t2, boolean strong) {
        
            Stamp sx = new Stamp(taskSentence.stamp, memory.getTime(), s);
                        
            for(int i=0;i<terms_dependent.size();i++) {
                final CompoundTerm result = terms_dependent.get(i);
               
                char mark=Symbols.JUDGMENT_MARK;
                if(task.sentence.isGoal() || second_belief.isGoal()) {
                    if(strong) {
                        truth=abduction(t1,t2);
                    }
                    else {
                        truth=intersection(t1,t2);
                    }
                    mark=Symbols.GOAL_MARK;
                }

                Sentence newSentence=new Sentence(result, mark, truth, sx);                     
                BudgetValue budget = BudgetFunctions.compoundForward(truth, newSentence.content, memory);
                
                //check this after compoundForward, seems the earliest it can exit loop
                if(sEqualsP(newSentence.content)) {
                    //changed this from return to continue, 
                    //to allow processing terms_dependent when it has > 1 items
                    continue;
                }
                
                Task newTask = new Task(newSentence, budget, task, null);
                Task dummy = new Task(second_belief, budget, task, null);

                memory.setCurrentBelief(taskSentence);
                memory.setCurrentTask(dummy);
                
                memory.logic.DED_SECOND_LAYER_VARIABLE_UNIFICATION_TERMS.commit();
                memory.derivedTask(newTask, false, false, taskSentence, second_belief);
            }    
    }

    private static HashMap<Term, Term> newVariableSubstitutionMap() {
        //TODO give appropraite size
        return new HashMap();
    }
}
