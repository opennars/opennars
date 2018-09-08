/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.inference;

import java.util.ArrayList;
import java.util.Collections;
import org.opennars.control.DerivationContext;
import org.opennars.entity.*;
import org.opennars.io.Symbols;
import org.opennars.language.*;
import org.opennars.main.MiscFlags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.opennars.inference.TruthFunctions.*;
import static org.opennars.language.Terms.reduceComponents;
import org.opennars.storage.Memory;

/**
 * Compound term composition and decomposition rules, with two premises.
 * <p>
 * New compound terms are introduced only in forward inference, while
 * decompositional rules are also used in backward inference
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public final class CompositionalRules {

    /* -------------------- intersections and differences -------------------- */
    /**
     * {&lt;S ==&gt; M&gt;, &lt;P ==&gt; M&gt;} |- <br>
     * {&lt;(S|P) ==&gt; M&gt;, &lt;(S&amp;P) ==&gt; M&gt;, &lt;(S-P) ==&gt; M&gt;, &lt;(P-S) ==&gt; M&gt;}
     *
     * @param taskContent The first premise
     * @param beliefContent The second premise
     * @param index The location of the shared term
     * @param nal Reference to the memory
     */
    static void composeCompound(final Statement taskContent, final Statement beliefContent, final int index, final DerivationContext nal) {
        if ((!nal.getCurrentTask().sentence.isJudgment()) || (taskContent.getClass() != beliefContent.getClass())) {
            return;
        }   
        final Term componentT = taskContent.term[1 - index];
        final Term componentB = beliefContent.term[1 - index];
        final Term componentCommon = taskContent.term[index];
        final int order1 = taskContent.getTemporalOrder();
        final int order2 = beliefContent.getTemporalOrder();
        final int order = TemporalRules.composeOrder(order1, order2);
        if (order == TemporalRules.ORDER_INVALID) {
            return;
        } 
        if ((componentT instanceof CompoundTerm) && ((CompoundTerm) componentT).containsAllTermsOf(componentB)) {
            decomposeCompound((CompoundTerm) componentT, componentB, componentCommon, index, true, order, nal);
            return;
        } else if ((componentB instanceof CompoundTerm) && ((CompoundTerm) componentB).containsAllTermsOf(componentT)) {
            decomposeCompound((CompoundTerm) componentB, componentT, componentCommon, index, false, order, nal);
            return;
        }
        final TruthValue truthT = nal.getCurrentTask().sentence.truth;
        final TruthValue truthB = nal.getCurrentBelief().truth;
        final TruthValue truthOr = union(truthT, truthB, nal.narParameters);
        final TruthValue truthAnd = intersection(truthT, truthB, nal.narParameters);
        TruthValue truthDif = null;
        Term termOr = null;
        Term termAnd = null;
        Term termDif = null;
        if (index == 0) {
            if (taskContent instanceof Inheritance) {
                termOr = IntersectionInt.make(componentT, componentB);
                termAnd = IntersectionExt.make(componentT, componentB);
                if (truthB.isNegative()) {
                    if (!truthT.isNegative()) {
                        termDif = DifferenceExt.make(componentT, componentB);
                        truthDif = intersection(truthT, negation(truthB, nal.narParameters), nal.narParameters);
                    }
                } else if (truthT.isNegative()) {
                    termDif = DifferenceExt.make(componentB, componentT);
                    truthDif = intersection(truthB, negation(truthT, nal.narParameters), nal.narParameters);
                }
            } else if (taskContent instanceof Implication) {
                termOr = Disjunction.make(componentT, componentB);
                termAnd = Conjunction.make(componentT, componentB);
            }
            if(!(componentT.cloneDeep().equals(componentB.cloneDeep()))) {
                processComposed(taskContent, componentCommon, termOr, order, truthOr, nal);
                processComposed(taskContent, componentCommon, termAnd, order, truthAnd, nal);
            }
            processComposed(taskContent, componentCommon, termDif, order, truthDif, nal);
        } else {    // index == 1
            if (taskContent instanceof Inheritance) {
                termOr = IntersectionExt.make(componentT, componentB);
                termAnd = IntersectionInt.make(componentT, componentB);
                if (truthB.isNegative()) {
                    if (!truthT.isNegative()) {
                        termDif = DifferenceInt.make(componentT, componentB);
                        truthDif = intersection(truthT, negation(truthB, nal.narParameters), nal.narParameters);
                    }
                } else if (truthT.isNegative()) {
                    termDif = DifferenceInt.make(componentB, componentT);
                    truthDif = intersection(truthB, negation(truthT, nal.narParameters), nal.narParameters);
                }
            } else if (taskContent instanceof Implication) {
                termOr = Conjunction.make(componentT, componentB);
                termAnd = Disjunction.make(componentT, componentB);
            }
            
            if(!(componentT.cloneDeep().equals(componentB.cloneDeep()))) {
                processComposed(taskContent, termOr, componentCommon, order, truthOr, nal);
                processComposed(taskContent, termAnd, componentCommon, order, truthAnd, nal);
            }
            processComposed(taskContent, termDif, componentCommon, order, truthDif, nal);
        }
    }

    /**
     * Finish composing implication term
     *
     * @param statement Type of the contentInd
     * @param subject Subject of contentInd
     * @param predicate Predicate of contentInd
     * @param truth TruthValue of the contentInd
     * @param nal Reference to the memory
     */
    private static void processComposed(final Statement statement, final Term subject, final Term predicate, final int order, final TruthValue truth, final DerivationContext nal) {
        if ((subject == null) || (predicate == null)) {
            return;
        }
        final Term content = Statement.make(statement, subject, predicate, order);
        if ((content == null) || statement == null || content.equals(statement) || content.equals(nal.getCurrentBelief().term)) {
            return;
        }
        final BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.doublePremiseTask(content, truth, budget, false, false); //(allow overlap) but not needed here, isn't detachment, this one would be even problematic from control perspective because its composition
    }

    /**
     * {&lt;(S|P) ==&gt; M&gt;, &lt;P ==&gt; M&gt;} |- &lt;S ==&gt; M&gt;
     *
     * @param term1 The other term in the contentInd
     * @param index The location of the shared term: 0 for subject, 1 for predicate
     * @param compoundTask Whether the implication comes from the task
     * @param nal Reference to the memory
     */
    private static void decomposeCompound(final CompoundTerm compound, final Term component, final Term term1, final int index, final boolean compoundTask, final int order, final DerivationContext nal) {

        if ((compound instanceof Statement) || (compound instanceof ImageExt) || (compound instanceof ImageInt)) {
            return;
        }
        Term term2 = reduceComponents(compound, component, nal.mem());
        if (term2 == null) {
            return;
        }
        
        long delta = 0;
        while ((term2 instanceof Conjunction) && (((CompoundTerm) term2).term[0] instanceof Interval)) {
            final Interval interval = (Interval) ((CompoundTerm) term2).term[0];
            delta += interval.time;
            term2 = ((CompoundTerm)term2).setComponent(0, null, nal.mem());
        }
        
        final Task task = nal.getCurrentTask();
        final Sentence sentence = task.sentence;
        final Sentence belief = nal.getCurrentBelief();
        final Statement oldContent = (Statement) task.getTerm();

        final TruthValue v1 = compoundTask ? sentence.truth : belief.truth;
        final TruthValue v2 = compoundTask ? belief.truth : sentence.truth;

        final Term content = Statement.make(oldContent, index == 0 ? term1 : term2, index == 0 ? term2 : term1, order);
        if (content == null) {
            return;
        }

        TruthValue truth = null;
        if (index == 0) {
            if (oldContent instanceof Inheritance) {
                truth = lookupTruthOrNull(v1, v2, nal.narParameters,
                    compound instanceof IntersectionExt,               EnumType.REDUCECONJUNCTION,
                    compound instanceof IntersectionInt,                       EnumType.REDUCEDISJUNCTION,
                    compound instanceof SetInt && component instanceof SetInt, EnumType.REDUCECONJUNCTION,
                    compound instanceof SetExt && component instanceof SetExt, EnumType.REDUCEDISJUNCTION);

                if (truth == null && compound instanceof DifferenceExt) {
                    if (compound.term[0].equals(component)) {
                        truth = reduceDisjunction(v2, v1, nal.narParameters);
                    } else {
                        truth = reduceConjunctionNeg(v1, v2, nal.narParameters);
                    }
                }
            } else if (oldContent instanceof Implication) {
                if (compound instanceof Conjunction) {
                    truth = reduceConjunction(v1, v2, nal.narParameters);
                } else if (compound instanceof Disjunction) {
                    truth = reduceDisjunction(v1, v2, nal.narParameters);
                }
            }
        } else {
            if (oldContent instanceof Inheritance) {
                truth = lookupTruthOrNull(v1, v2, nal.narParameters,
                    compound instanceof IntersectionInt,               EnumType.REDUCECONJUNCTION,
                    compound instanceof IntersectionExt,                       EnumType.REDUCEDISJUNCTION,
                    compound instanceof SetExt && component instanceof SetExt, EnumType.REDUCECONJUNCTION,
                    compound instanceof SetInt && component instanceof SetInt, EnumType.REDUCEDISJUNCTION);

                if( truth == null && compound instanceof DifferenceInt ) {
                    if (compound.term[1].equals(component)) {
                        truth = reduceDisjunction(v2, v1, nal.narParameters);
                    } else {
                        truth = reduceConjunctionNeg(v1, v2, nal.narParameters);
                    }
                }
            } else if (oldContent instanceof Implication) {
                if (compound instanceof Disjunction) {
                    truth = reduceConjunction(v1, v2, nal.narParameters);
                } else if (compound instanceof Conjunction) {
                    truth = reduceDisjunction(v1, v2, nal.narParameters);
                }
            }
        }
        if (truth != null) {
            final BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
            if (delta != 0) {
                long baseTime = task.sentence.getOccurenceTime();
                if (baseTime != Stamp.ETERNAL) {
                    baseTime += delta;
                    nal.getTheNewStamp().setOccurrenceTime(baseTime);
                }
            }
            nal.doublePremiseTask(content, truth, budget, false, true); //(allow overlap), a form of detachment
        }
    }

    /**
     * {(||, S, P), P} |- S {(&amp;&amp;, S, P), P} |- S
     *
     * @param compoundTask Whether the implication comes from the task
     * @param nal Reference to the memory
     */
    static void decomposeStatement(final CompoundTerm compound, final Term component, final boolean compoundTask, final int index, final DerivationContext nal) {
        final boolean isTemporalConjunction = (compound instanceof Conjunction) && !((Conjunction) compound).isSpatial;
        if (isTemporalConjunction && (compound.getTemporalOrder() == TemporalRules.ORDER_FORWARD) && (index != 0)) {
            return;
        }
        long occurrence_time = nal.getCurrentTask().sentence.getOccurenceTime();
        if(isTemporalConjunction && (compound.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
            if(!nal.getCurrentTask().sentence.isEternal() && compound.term[index + 1] instanceof Interval) {
                final long shift_occurrence = ((Interval)compound.term[index + 1]).time;
                occurrence_time = nal.getCurrentTask().sentence.getOccurenceTime() + shift_occurrence;
            }
        }

        final Task task = nal.getCurrentTask();
        final Sentence taskSentence = task.sentence;
        final Sentence belief = nal.getCurrentBelief();
        final Term content = reduceComponents(compound, component, nal.mem());
        if (content == null) {
            return;
        }
        TruthValue truth = null;
        BudgetValue budget;
        if (taskSentence.isQuestion() || taskSentence.isQuest()) {
            budget = BudgetFunctions.compoundBackward(content, nal);
            nal.getTheNewStamp().setOccurrenceTime(occurrence_time);
            nal.doublePremiseTask(content, truth, budget, false, false);
            // special inference to answer conjunctive questions with query variables
            if (taskSentence.term.hasVarQuery()) {
                final Concept contentConcept = nal.mem().concept(content);
                if (contentConcept == null) {
                    return;
                }
                final Sentence contentBelief = contentConcept.getBelief(nal, task);
                if (contentBelief == null) {
                    return;
                }

                final Task contentTask = new Task(contentBelief, task.budget, Task.EnumType.DERIVED);

                nal.setCurrentTask(contentTask);
                final Term conj = Conjunction.make(component, content);
                truth = intersection(contentBelief.truth, belief.truth, nal.narParameters);
                budget = BudgetFunctions.compoundForward(truth, conj, nal);
                nal.getTheNewStamp().setOccurrenceTime(occurrence_time);
                nal.doublePremiseTask(conj, truth, budget, false, false);
            }
        } else {
            final TruthValue v1 = compoundTask ? taskSentence.truth : belief.truth;
            final TruthValue v2 = compoundTask ? belief.truth : taskSentence.truth;

            if (compound instanceof Conjunction || compound instanceof Disjunction) {
                if (taskSentence.isGoal() && !compoundTask) {
                    return;
                }
            } else {
                return;
            }

            if (compound instanceof Conjunction) {
                if (taskSentence.isGoal()) {
                    truth = intersection(v1, v2, nal.narParameters);
                } else { // isJudgment
                    truth = reduceConjunction(v1, v2, nal.narParameters);
                }
            } else {
                if (taskSentence.isGoal()) {
                    truth = reduceConjunction(v2, v1, nal.narParameters);
                } else {  // isJudgment
                    truth = reduceDisjunction(v1, v2, nal.narParameters);
                }
            }

            budget = BudgetFunctions.compoundForward(truth, content, nal);
        }
        nal.getTheNewStamp().setOccurrenceTime(occurrence_time);
        nal.doublePremiseTask(content, truth, budget, false, false);
    }

    
    /* --------------- rules used for variable introduction --------------- */
    /**
     * Introduce a dependent variable in an outer-layer conjunction
     * <br>
     * {&lt;S --&gt; P1&gt;, &lt;S --&gt; P2&gt;} |- (&amp;&amp;, &lt;#x --&gt; P1&gt;, &lt;#x --&gt; P2&gt;)
     *
     * @param taskContent The first premise &lt;M --&gt; S&gt;
     * @param beliefContent The second premise &lt;M --&gt; P&gt;
     * @param index The location of the shared term: 0 for subject, 1 for
     * predicate
     * @param nal Reference to the memory
     */
    public static void introVarOuter(final Statement taskContent, final Statement beliefContent, final int index, final DerivationContext nal) {

        if (!(taskContent instanceof Inheritance)) {
            return;
        }
        
        Term term11 = taskContent.getSubject();
        Term term21 = beliefContent.getSubject();
        Term term12 = taskContent.getPredicate();
        Term term22 = beliefContent.getPredicate();
        Statement state1 = Inheritance.make(term11, term12);
        Statement state2 = Inheritance.make(term21, term22);
        final TruthValue truthT = nal.getCurrentTask().sentence.truth;
        final TruthValue truthB = nal.getCurrentBelief().truth;
        if ((truthT == null) || (truthB == null)) {
            if(MiscFlags.DEBUG) {
                System.out.println("ERROR: Belief with null truth value. (introVarOuter)");
            }
            return;
        }
        for(boolean subjectIntroduction : new boolean[]{true, false}) {
            Set<Term> contents = CompositionalRules.introduceVariables(nal, Implication.make(state1, state2),subjectIntroduction);
            for(Term content : contents) {
                TruthValue truth = induction(truthT, truthB, nal.narParameters);
                BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
                nal.doublePremiseTask(content, truth.clone(), budget.clone(), false, false);
            }

            contents = CompositionalRules.introduceVariables(nal, Implication.make(state2, state1), subjectIntroduction);
             for(Term content : contents) {
                TruthValue truth = induction(truthB, truthT, nal.narParameters);
                BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
                nal.doublePremiseTask(content, truth.clone(), budget.clone(), false, false);
            }

            contents = CompositionalRules.introduceVariables(nal, Equivalence.make(state1, state2), subjectIntroduction);
             for(Term content : contents) {
                TruthValue truth = comparison(truthT, truthB, nal.narParameters);
                BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
                nal.doublePremiseTask(content, truth.clone(), budget.clone(), false, false);
            }

            contents = CompositionalRules.introduceVariables(nal, Conjunction.make(state1, state2), subjectIntroduction);
            for(Term content : contents) {
                TruthValue truth = intersection(truthT, truthB, nal.narParameters);
                BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
                nal.doublePremiseTask(content, truth.clone(), budget.clone(), false, false);
            }
        }
    }

    /**
     * {&lt;M --&gt; S&gt;, &lt;C ==&gt; &lt;M --&gt; P&gt;&gt;} |- &lt;(&amp;&amp;, &lt;#x --&gt; S&gt;, C) ==&gt; &lt;#x --&gt; P&gt;&gt;
     * <br>
     * {&lt;M --&gt; S&gt;, (&amp;&amp;, C, &lt;M --&gt; P&gt;)} |- (&amp;&amp;, C, &lt;&lt;#x --&gt; S&gt; ==&gt; &lt;#x --&gt; P&gt;&gt;)
     *
     * @param oldCompound The whole contentInd of the first premise, Implication
     * or Conjunction
     * @param nal Reference to the memory
     */
    static boolean introVarInner(final Statement premise1, final Statement premise2, final CompoundTerm oldCompound, final DerivationContext nal) {
        final Task task = nal.getCurrentTask();
        final Sentence taskSentence = task.sentence;
        if (!taskSentence.isJudgment() || (premise1.getClass() != premise2.getClass()) || oldCompound.containsTerm(premise1)) {
            return false;
        }
        final Sentence belief = nal.getCurrentBelief();
        
        boolean b1 = false, b2 = false;
        
        {
            Term content = Conjunction.make(premise1, oldCompound);
            if (!(content instanceof CompoundTerm)) {
                return false;  
            }
            for(boolean subjectIntro : new boolean[]{true, false}) {
                Set<Term> conts = introduceVariables(nal, content, subjectIntro);
                for(Term cont : conts) {
                    final TruthValue truth = intersection(taskSentence.truth, belief.truth, nal.narParameters);
                    final BudgetValue budget = BudgetFunctions.forward(truth, nal);
                    b1 |= (nal.doublePremiseTask(cont, truth, budget, false, false))!=null;
                }
            }
        }

        {
            Term content = Implication.make(premise1, oldCompound);
            if ((content == null) || (!(content instanceof CompoundTerm))) {
                return false;
            }
            for(boolean subjectIntro : new boolean[]{true, false}) {
                Set<Term> conts = introduceVariables(nal, content, subjectIntro);
                for(Term cont : conts) {
                    final TruthValue truth;
                    if (premise1.equals(taskSentence.term)) {
                        truth = induction(belief.truth, taskSentence.truth, nal.narParameters);
                    } else {
                        truth = induction(taskSentence.truth, belief.truth, nal.narParameters);
                    }
                    final BudgetValue budget = BudgetFunctions.forward(truth, nal);
                    b2 |= nal.doublePremiseTask(cont, truth, budget, false, false)!=null;
                }
            }
        }
        
        return b1 || b2;
    }

    /*
     * The other inversion (abduction) should also be studied:
     * IN: <<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>.
     * IN: <(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>.
     * OUT: <lock1 --> lock>.
     * http://code.google.com/p/open-nars/issues/detail?id=40&can=1
     */
    public static void eliminateVariableOfConditionAbductive(final int figure, final Sentence sentence, final Sentence belief, final DerivationContext nal) {
        Statement T1 = (Statement) sentence.term;
        Statement T2 = (Statement) belief.term;

        Term S1 = T2.getSubject();
        Term S2 = T1.getSubject();
        Term P1 = T2.getPredicate();
        Term P2 = T1.getPredicate();

        final Map<Term, Term>
            res1 = new HashMap<>(),
            res2 = new HashMap<>(),
            res3 = new HashMap<>(),
            res4 = new HashMap<>();

        if (figure == 21) {
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, P1, S2, res1, res2);
        }
        else if (figure == 12) {
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, S1, P2, res1, res2);
        }
        else if (figure == 11) {
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, S1, S2, res1, res2);
        }
        else if (figure == 22) {
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, P1, P2, res1, res2);
        }

        // this part is independent, the rule works if it unifies
        T1 = (Statement) T1.applySubstitute(res2);
        if(T1==null) {
            return;
        }
        T2 = (Statement) T2.applySubstitute(res1);
        if(T2==null) {
            return;
        }


        if (figure == 21) {
            // update the variables because T1 and T2 may have changed
            S1 = T2.getSubject();
            P2 = T1.getPredicate();

            eliminateVariableOfConditionAbductiveTryCrossUnification(sentence, belief, nal, S1, P2, res3, res4);
        }
        else if (figure == 12) {
            // update the variables because T1 and T2 may have changed
            S2 = T1.getSubject();
            P1 = T2.getPredicate();

            eliminateVariableOfConditionAbductiveTryCrossUnification(sentence, belief, nal, S2, P1, res3, res4);
        }
        else if (figure == 11) {
            // update the variables because T1 and T2 may have changed
            P1 = T2.getPredicate();
            P2 = T1.getPredicate();

            eliminateVariableOfConditionAbductiveTryCrossUnification(sentence, belief, nal, P1, P2, res3, res4);
        }
        else if (figure == 22) {
            // update the variables because T1 and T2 may have changed
            S1 = T2.getSubject();
            S2 = T1.getSubject();

            if (S1 instanceof Conjunction) {
                //try to unify S2 with a component
                for (final Term s1 : ((CompoundTerm) S1).term) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, S2, res3, res4)) {
                        for (Term s2 : ((CompoundTerm) S1).term) {
                            if (!(s2 instanceof CompoundTerm)) {
                                continue;
                            }
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(s2==null || s2.hasVarIndep()) {
                                continue;
                            }
                            if (s2!=null && !s2.equals(s1) && (sentence.truth != null) && (belief.truth != null)) {
                                final TruthValue truth = abduction(sentence.truth, belief.truth, nal.narParameters);
                                final BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
                                nal.doublePremiseTask(s2, truth, budget, false, false);
                            }
                        }
                    }
                }
            }
            if (S2 instanceof Conjunction) {
                //try to unify S1 with a component
                for (final Term s1 : ((CompoundTerm) S2).term) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, S1, res3, res4)) {
                        for (Term s2 : ((CompoundTerm) S2).term) {
                            if (!(s2 instanceof CompoundTerm)) {
                                continue;
                            }

                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(s2==null || s2.hasVarIndep()) {
                                continue;
                            }
                            if (s2!=null && !s2.equals(s1) && (sentence.truth != null) && (belief.truth != null)) {
                                final TruthValue truth = abduction(sentence.truth, belief.truth, nal.narParameters);
                                final BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
                                nal.doublePremiseTask(s2, truth, budget, false, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void eliminateVariableOfConditionAbductiveTryCrossUnification(Sentence sentence, Sentence belief, DerivationContext nal, Term s1, Term p2, Map<Term, Term> res3, Map<Term, Term> res4) {
        if (s1 instanceof Conjunction) {
            //try to unify P2 with a component
            eliminateVariableOfConditionAbductiveTryUnification1(sentence, belief, nal, p2, (CompoundTerm) s1, res3, res4);
        }
        if (p2 instanceof Conjunction) {
            //try to unify S1 with a component
            eliminateVariableOfConditionAbductiveTryUnification1(sentence, belief, nal, s1, (CompoundTerm) p2, res3, res4);
        }
    }

    private static void eliminateVariableOfConditionAbductiveTryUnification1(Sentence sentence, Sentence belief, DerivationContext nal, Term p1, CompoundTerm p2, Map<Term, Term> res3, Map<Term, Term> res4) {
        for (final Term s1 : p2.term) {
            res3.clear();
            res4.clear(); //here the dependent part matters, see example of Issue40
            if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, p1, res3, res4)) {
                eliminateVariableOfConditionAbductiveInner1(sentence, belief, nal, p2, res3, s1);
            }
        }
    }

    private static void eliminateVariableOfConditionAbductiveInner1(Sentence sentence, Sentence belief, DerivationContext nal, CompoundTerm s1, Map<Term, Term> res3, Term s12) {
        for (Term s2 : s1.term) {
            if (!(s2 instanceof CompoundTerm)) {
                continue;
            }
            s2 = ((CompoundTerm) s2).applySubstitute(res3);
            if(s2==null || s2.hasVarIndep()) {
                continue;
            }
            if (!s2.equals(s12) && (sentence.truth != null) && (belief.truth != null)) {
                final TruthValue truth = abduction(sentence.truth, belief.truth, nal.narParameters);
                final BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
                nal.doublePremiseTask(s2, truth, budget, false, false);
            }
        }
    }

    static void IntroVarSameSubjectOrPredicate(final Sentence originalMainSentence, final Sentence subSentence, final Term component, final Term content, final int index, final DerivationContext nal) {
        final Term T1 = originalMainSentence.term;
        if (!(T1 instanceof CompoundTerm) || !(content instanceof CompoundTerm)) {
            return;
        }
        CompoundTerm T = (CompoundTerm) T1;
        CompoundTerm T2 = (CompoundTerm) content;
        
        if ((component instanceof Inheritance && content instanceof Inheritance)
                || (component instanceof Similarity && content instanceof Similarity)) {
            //CompoundTerm result = T;
            if (component.equals(content)) {
                return; //wouldnt make sense to create a conjunction here, would contain a statement twice
            }

            if (((Statement) component).getPredicate().equals(((Statement) content).getPredicate()) && !(((Statement) component).getPredicate() instanceof Variable)) {

                CompoundTerm zw = (CompoundTerm) T.term[index];
                final Conjunction res = (Conjunction) Conjunction.make(zw, T2);
                T = (CompoundTerm) T.setComponent(index, res, nal.mem());
            } else if (((Statement) component).getSubject().equals(((Statement) content).getSubject()) && !(((Statement) component).getSubject() instanceof Variable)) {

                CompoundTerm zw = (CompoundTerm) T.term[index];
                final Conjunction res = (Conjunction) Conjunction.make(zw, T2);
                T = (CompoundTerm) T.setComponent(index, res, nal.mem());
            }
            
            if(T == null) {
                return;
            }
            final TruthValue truth = induction(originalMainSentence.truth, subSentence.truth, nal.narParameters);
            for(boolean subjectIntro : new boolean[]{true, false}) {
                Set<Term> conts = introduceVariables(nal, T, subjectIntro);
                for(Term cont : conts) {
                    final BudgetValue budget = BudgetFunctions.compoundForward(truth, cont, nal);
                    nal.doublePremiseTask(cont, truth.clone(), budget.clone(), false, false);
                }
            }
        }
    }
    
    
    /**
     * The power set, from João Silva, https://stackoverflow.com/questions/1670862/obtaining-a-powerset-of-a-set-in-java
     * 
     * @param <T>
     * @param originalSet
     * @return 
     */
    public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }       
        return sets;
    }
    
    /**
     * Introduction of variables that appear either within subjects or within predicates and more than once
     * 
     * @param implicationEquivalenceOrJunction
     * @param subject
     * @return 
     */
    public static Set<Term> introduceVariables(DerivationContext nal, Term implicationEquivalenceOrJunction, boolean subject) {
        HashSet<Term> result = new HashSet<Term>();
        boolean validForIntroduction =  implicationEquivalenceOrJunction instanceof Conjunction ||
                                        implicationEquivalenceOrJunction instanceof Disjunction ||
                                        implicationEquivalenceOrJunction instanceof Equivalence ||
                                        implicationEquivalenceOrJunction instanceof Implication;
        if(!validForIntroduction) {
            return result;
        }
        final Map<Term,Term> app = new HashMap<>();
        Set<Term> candidates = new HashSet<>();
        if(implicationEquivalenceOrJunction instanceof Implication || implicationEquivalenceOrJunction instanceof Equivalence) {
            addVariableCandidates(candidates, ((Statement)implicationEquivalenceOrJunction).getSubject(),   subject);
            addVariableCandidates(candidates, ((Statement)implicationEquivalenceOrJunction).getPredicate(), subject);
        }
        if(implicationEquivalenceOrJunction instanceof Conjunction || implicationEquivalenceOrJunction instanceof Disjunction) {
            addVariableCandidates(candidates, implicationEquivalenceOrJunction, subject);
        }
        Map<Term,Integer> termCounts = implicationEquivalenceOrJunction.countTermRecursively(null);
        int k = 0;
        for(Term t : candidates) {
            if(termCounts.getOrDefault(t, 0) > 1) {
                //ok it appeared as subject or predicate but appears in the Conjunction more than once
                //=> introduce a dependent variable for it!
                String varType = "#";
                if(implicationEquivalenceOrJunction instanceof Implication || implicationEquivalenceOrJunction instanceof Equivalence) {
                    Statement imp = (Statement) implicationEquivalenceOrJunction;
                    if(imp.getSubject().containsTermRecursively(t) && imp.getPredicate().containsTermRecursively(t)) {
                        varType = "$";
                    }
                }
                Variable introVar = new Variable(varType + "ind" + k);
                app.put(t, introVar);
                k++;
            }
        }
        
        List<Term> shuffledVariables = new ArrayList<Term>();
        for(Term t : app.keySet()) {
            shuffledVariables.add(t);
        }
        Collections.shuffle(shuffledVariables, Memory.randomNumber);
        HashSet<Term> selected = new HashSet<Term>();
        int i = 1;
        for(Term t : shuffledVariables) {
            selected.add(t);
            if(Math.pow(2.0, i) > nal.narParameters.VARIABLE_INTRODUCTION_COMBINATIONS_MAX) {
                break;
            }
            i++;
        }
        Set<Set<Term>> powerset = powerSet(selected);
        for(Set<Term> combo : powerset) {
            Map<Term,Term> mapping = new HashMap<>();
            for(Term vIntro : combo) {
                mapping.put(vIntro, app.get(vIntro));
            }
            if(mapping.size() > 0) {
                result.add(((CompoundTerm)implicationEquivalenceOrJunction).applySubstitute(mapping));
            }
        }
        return result;
    }

    /**
     * Add the variable candidates that appear as subjects and predicates
     * 
     * @param candidates manipulated set of candidates
     * @param side
     * @param subject
     */
    public static void addVariableCandidates(Set<Term> candidates, Term side, boolean subject) {
        boolean junction = (side instanceof Conjunction || side instanceof Disjunction);
        int n = junction ? ((CompoundTerm) side).size() : 1;
        for(int i=0; i<n; i++) {
            // we found an Inheritance
            Term t = null;
            if(i<n) {
                if(junction) {
                    t = ((CompoundTerm) side).term[i];
                } else {
                    t = side;
                }
            }
            if(t instanceof Inheritance || t instanceof Similarity) {
                Inheritance inh = (Inheritance) t;
                Term subjT = inh.getSubject();
                Term predT = inh.getPredicate();
                boolean addSubject = subject || subjT instanceof ImageInt; //also allow for images due to equivalence transform
                Set<Term> removals = new HashSet<Term>();
                if(addSubject && !subjT.hasVar()) {
                    Set<Term> ret = CompoundTerm.addComponentsRecursively(subjT, null);
                    for(Term ct : ret) {
                        if(ct instanceof Image) {
                            removals.add(((Image) ct).term[((Image) ct).relationIndex]);
                        }
                        candidates.add(ct);
                    }
                }
                boolean addPredicate = !subject || predT instanceof ImageExt; //also allow for images due to equivalence transform
                if(addPredicate && !predT.hasVar()) {
                    Set<Term> ret = CompoundTerm.addComponentsRecursively(predT, null);
                    for(Term ct : ret) {
                        if(ct instanceof Image) {
                            removals.add(((Image) ct).term[((Image) ct).relationIndex]);
                        }
                        candidates.add(ct);
                    }
                }
                for(Term remove : removals) { //but do not introduce variables for image relation, only if they appear as product
                    candidates.remove(remove);
                }
            }
        }
    }
}
