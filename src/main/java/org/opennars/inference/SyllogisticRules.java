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

import java.util.HashMap;
import org.opennars.control.DerivationContext;
import org.opennars.entity.*;
import org.opennars.io.Symbols;
import org.opennars.io.Symbols.NativeOperator;
import org.opennars.language.*;

import java.util.List;
import org.opennars.control.concept.ProcessAnticipation;

import static org.opennars.inference.TemporalRules.*;
import static org.opennars.language.Terms.reduceComponents;

/**
 * Syllogisms: Inference rules based on the transitivity of the relation.
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public final class SyllogisticRules {

    /* --------------- rules used in both first-tense inference and higher-tense inference --------------- */
    /**
     * <pre>
     * {&lt;S ==&gt; M&gt;, &lt;M ==&gt; P&gt;} |- {&lt;S ==&gt; P&gt;, &lt;P ==&gt; S&gt;}
     * </pre>
     *
     * @param term1 Subject of the first new task
     * @param term2 Predicate of the first new task
     * @param sentence The first premise
     * @param belief The second premise
     * @param nal Reference to the memory
     */
    static void dedExe(final Term term1, final Term term2, final Sentence sentence, final Sentence belief, final DerivationContext nal) {
        if (Statement.invalidStatement(term1, term2)) {
            return;
        }
        final int order1 = sentence.term.getTemporalOrder();
        final int order2 = belief.term.getTemporalOrder();
        final int order = dedExeOrder(order1, order2);
        if (order == ORDER_INVALID) {
            return;
        }
        final TruthValue value1 = sentence.truth;
        final TruthValue value2 = belief.truth;
        TruthValue truth1 = null;
        TruthValue truth2 = null;
        final BudgetValue budget1;
        final BudgetValue budget2;

        if (!(sentence.isQuestion() || sentence.isQuest())) {
            if (sentence.isGoal()) {
                truth1 = TruthFunctions.desireWeak(value1, value2, nal.narParameters);
                truth2 = TruthFunctions.desireWeak(value1, value2, nal.narParameters);
            } else {
                // isJudgment
                truth1 = TruthFunctions.deduction(value1, value2, nal.narParameters);
                truth2 = TruthFunctions.exemplification(value1, value2, nal.narParameters);
            }
        }

        if (sentence.isQuestion()) {
            budget1 = BudgetFunctions.backwardWeak(value2, nal);
            budget2 = BudgetFunctions.backwardWeak(value2, nal);
        } else if (sentence.isQuest()) {
            budget1 = BudgetFunctions.backward(value2, nal);
            budget2 = BudgetFunctions.backward(value2, nal);
        } else {
            budget1 = BudgetFunctions.forward(truth1, nal);
            budget2 = BudgetFunctions.forward(truth2, nal);
        }

        final Statement content = (Statement) sentence.term;
        final Statement content1 = Statement.make(content, term1, term2, order);
        final Statement content2 = Statement.make(content, term2, term1, reverseOrder(order));
        
        if ((content1 == null) || (content2 == null))
            return;
        
        nal.doublePremiseTask(content1, truth1, budget1,false, false); //(allow overlap) but not needed here, isn't detachment
        nal.doublePremiseTask(content2, truth2, budget2,false, false);
    }

    /**
     * {&lt;M ==&gt; S&gt;, &lt;M ==&gt; P&gt;} |- {&lt;S ==&gt; P&gt;, &lt;P ==&gt; S&gt;, &lt;S &lt;=&gt; P&gt;}
     *
     * @param term1 Subject of the first new task
     * @param term2 Predicate of the first new task
     * @param sentence1 The first premise
     * @param sentence2 The second premise
     * @param figure Locations of the shared term in premises --- can be
     * removed?
     * @param nal Reference to the memory
     */
    static boolean abdIndCom(Term term1, Term term2, final Sentence sentence1, final Sentence sentence2, final int figure, final DerivationContext nal) {
        if (Statement.invalidStatement(term1, term2) || Statement.invalidPair(term1, term2)) {
            return false;
        }
        final int order1 = sentence1.term.getTemporalOrder();
        final int order2 = sentence2.term.getTemporalOrder();
        final int order = abdIndComOrder(order1, order2);
        
        final Statement taskContent = (Statement) sentence1.term;
        TruthValue truth1 = null;
        TruthValue truth2 = null;
        TruthValue truth3 = null;
        final BudgetValue budget1;
        final BudgetValue budget2;
        final BudgetValue budget3;
        final TruthValue value1 = sentence1.truth;
        final TruthValue value2 = sentence2.truth;

        if (sentence1.isGoal()) {
            truth1 = TruthFunctions.desireStrong(value1, value2, nal.narParameters); //P --> S
            truth2 = TruthFunctions.desireWeak(value2, value1, nal.narParameters); //S --> P
            truth3 = TruthFunctions.desireStrong(value1, value2, nal.narParameters); //S <-> P
        } else if( sentence1.isJudgment() ) {
            truth1 = TruthFunctions.abduction(value1, value2, nal.narParameters); //P --> S
            truth2 = TruthFunctions.abduction(value2, value1, nal.narParameters); //S --> P
            truth3 = TruthFunctions.comparison(value1, value2, nal.narParameters); //S <-> P
        }

        if (sentence1.isQuestion()) {
            budget1 = BudgetFunctions.backward(value2, nal);
            budget2 = BudgetFunctions.backwardWeak(value2, nal);
            budget3 = BudgetFunctions.backward(value2, nal);
        } else if (sentence1.isQuest()) {
            budget1 = BudgetFunctions.backwardWeak(value2, nal);
            budget2 = BudgetFunctions.backward(value2, nal);
            budget3 = BudgetFunctions.backwardWeak(value2, nal);            
        } else {
            budget1 = BudgetFunctions.forward(truth1, nal);
            budget2 = BudgetFunctions.forward(truth2, nal);
            budget3 = BudgetFunctions.forward(truth3, nal);
        }
        
        if(term1.imagination != null && term2.imagination != null) {
            final TruthValue T = term1.imagination.AbductionOrComparisonTo(term2.imagination, true);
            nal.doublePremiseTask(
                Statement.make(NativeOperator.SIMILARITY, term1, term2, TemporalRules.ORDER_NONE), 
                    T, BudgetFunctions.forward(T, nal),false, false);   
            final TruthValue T2 = term1.imagination.AbductionOrComparisonTo(term2.imagination, false);
            nal.doublePremiseTask(
                Statement.make(NativeOperator.INHERITANCE, term1, term2, TemporalRules.ORDER_NONE), 
                    T2, BudgetFunctions.forward(T2, nal),false, false);   
            final TruthValue T3 = term2.imagination.AbductionOrComparisonTo(term1.imagination, false);
            nal.doublePremiseTask(
                Statement.make(NativeOperator.INHERITANCE, term2, term1, TemporalRules.ORDER_NONE), 
                    T3, BudgetFunctions.forward(T3, nal),false, false);   
            return true; //no need for other syllogistic inference, it were sensational terms,
        }           //but it would not hurt to allow it either.. but why afford tasks that summarize
                    //so little evidence in comparison to the amount summarized by the array comparison.
        long occurrence_time2 = nal.getCurrentTask().sentence.getOccurenceTime();
        while (occurrence_time2!=Stamp.ETERNAL && (term2 instanceof Conjunction) && (((CompoundTerm) term2).term[0] instanceof Interval)) {
            final Interval interval = (Interval) ((CompoundTerm) term2).term[0];
            occurrence_time2 += interval.time;
            term2 = ((CompoundTerm)term2).setComponent(0, null, nal.mem());
        }
        long occurrence_time1 = nal.getCurrentTask().sentence.getOccurenceTime();
        while (occurrence_time1!=Stamp.ETERNAL && (term1 instanceof Conjunction) && (((CompoundTerm) term1).term[0] instanceof Interval)) {
            final Interval interval = (Interval) ((CompoundTerm) term1).term[0];
            occurrence_time1 += interval.time;
            term1 = ((CompoundTerm)term1).setComponent(0, null, nal.mem());
        }
        
        if (order != ORDER_INVALID) {
            nal.getTheNewStamp().setOccurrenceTime(occurrence_time1);
            nal.doublePremiseTask(
                    Statement.make(taskContent, term1, term2, order), 
                        truth1, budget1,false, false);
            nal.getTheNewStamp().setOccurrenceTime(occurrence_time2);
            nal.doublePremiseTask(
                    Statement.make(taskContent, term2, term1, reverseOrder(order)), 
                        truth2, budget2,false, false);
            nal.getTheNewStamp().setOccurrenceTime(occurrence_time1);
            nal.doublePremiseTask(
                    Statement.makeSym(taskContent, term1, term2, order), 
                        truth3, budget3,false, false);
        }
        if(nal.narParameters.BREAK_NAL_HOL_BOUNDARY && order1==order2 && taskContent.isHigherOrderStatement() && sentence2.term.isHigherOrderStatement()) { //
            /* Bridge to higher order statements:
            <a ==> c>.
            <b ==> c>.
            |-
            <a <-> b>. %F_cmp%
            <a --> b>. %F_abd%
            <b --> a>. %F_abd%
            */
          /*  if(truth1!=null) 
                truth1=truth1.clone();
            if(truth2!=null) 
                truth2=truth2.clone();*/
            if(truth3!=null) 
                truth3=truth3.clone();
           /* nal.doublePremiseTask(
                Statement.make(NativeOperator.INHERITANCE, term1, term2), 
                    truth1, budget1.clone(),false, false);
            nal.doublePremiseTask(
                Statement.make(NativeOperator.INHERITANCE, term2, term1), 
                    truth2, budget2.clone(),false, false);*/
            nal.doublePremiseTask(
                Statement.make(NativeOperator.SIMILARITY, term1, term2, TemporalRules.ORDER_NONE), 
                    truth3, budget3.clone(),false, false);
        }
        return false;
    }
    
    

    /**
     * {&lt;S ==&gt; P&gt;, &lt;M &lt;=&gt; P&gt;} |- &lt;S ==&gt; P&gt;
     *
     * @param subj Subject of the new task
     * @param pred Predicate of the new task
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     * @param figure Locations of the shared term in premises
     * @param nal Reference to the memory
     */
    static void analogy(final Term subj, final Term pred, final Sentence asym, final Sentence sym, final int figure, final DerivationContext nal) {
        if (Statement.invalidStatement(subj, pred)) {
            return;
        }
        final int order1 = asym.term.getTemporalOrder();
        final int order2 = sym.term.getTemporalOrder();
        final int order = analogyOrder(order1, order2, figure);
        if (order == ORDER_INVALID) {
            return;
        }
        final Statement st = (Statement) asym.term;
        TruthValue truth = null;
        final BudgetValue budget;
        final Sentence sentence = nal.getCurrentTask().sentence;
        final CompoundTerm taskTerm = (CompoundTerm) sentence.term;
        if (sentence.isQuestion() || sentence.isQuest()) {
            if (taskTerm.isCommutative()) {
                if(asym.truth==null) { //a question for example
                    return;
                }
                budget = BudgetFunctions.backwardWeak(asym.truth, nal);
            } else {
                if(sym.truth==null) { //a question for example
                    return;
                }
                budget = BudgetFunctions.backward(sym.truth, nal);
            }
        } else {
            if (sentence.isGoal()) {
                truth = TruthFunctions.lookupTruthFunctionByBoolAndCompute(taskTerm.isCommutative(), TruthFunctions.EnumType.DESIREWEAK, TruthFunctions.EnumType.DESIRESTRONG, asym.truth, sym.truth, nal.narParameters);
            } else {
                truth = TruthFunctions.analogy(asym.truth, sym.truth, nal.narParameters);
            }
            
            budget = BudgetFunctions.forward(truth, nal);
        }
        
        //nal.mem().logic.ANALOGY.commit();
        nal.doublePremiseTask( Statement.make(st, subj, pred, order), truth, budget,false, false); //(allow overlap) but not needed here, isn't detachment
    }

    /**
     * {&lt;S &lt;=&gt; M&gt;, &lt;&lt; &lt;=&gt; P&gt;} |- &lt;S &lt;=&gt; P&gt;
     *
     * @param term1 Subject of the new task
     * @param term2 Predicate of the new task
     * @param belief The first premise
     * @param sentence The second premise
     * @param figure Locations of the shared term in premises
     * @param nal Reference to the memory
     */
    static void resemblance(final Term term1, final Term term2, final Sentence belief, final Sentence sentence, final int figure, final DerivationContext nal) {
        if (Statement.invalidStatement(term1, term2)) {
            return;
        }
        final int order1 = belief.term.getTemporalOrder();
        final int order2 = sentence.term.getTemporalOrder();
        int order = resemblanceOrder(order1, order2, figure);
        if (order == ORDER_INVALID) {
            return;
        }
        final Statement st = (Statement) belief.term;
        TruthValue truth = null;
        final BudgetValue budget;
        if (!(sentence.isQuestion() || sentence.isQuest())) {
            if (sentence.isGoal()) {
                truth = TruthFunctions.desireStrong(sentence.truth, belief.truth, nal.narParameters);
            } else {
                // NOTE< this must be Judgement again ? >
                truth = TruthFunctions.resemblance(belief.truth, sentence.truth, nal.narParameters);
            }
        }

        if (sentence.isQuestion() || sentence.isQuest()) {
            budget = BudgetFunctions.backward(belief.truth, nal);
        } else {
            budget = BudgetFunctions.forward(truth, nal);
        }

        final boolean higherOrder=(belief.term.isHigherOrderStatement() || sentence.term.isHigherOrderStatement());
        final boolean bothHigherOrder=(belief.term.isHigherOrderStatement() && sentence.term.isHigherOrderStatement());
        if(!bothHigherOrder && higherOrder) {
            if(belief.term.isHigherOrderStatement()) {
                order=belief.term.getTemporalOrder();
            } 
            else
            if(sentence.term.isHigherOrderStatement()) {
                order=sentence.term.getTemporalOrder();
            }
        }
        final Statement s=Statement.make(higherOrder ? NativeOperator.EQUIVALENCE : NativeOperator.SIMILARITY, term1, term2, order);
        nal.doublePremiseTask( s, truth, budget,false, false); //(allow overlap) but not needed here, isn't detachment
        
        if(nal.narParameters.BREAK_NAL_HOL_BOUNDARY && !sentence.term.hasVarIndep() && (st instanceof Equivalence) && order1==order2 && belief.term.isHigherOrderStatement() && sentence.term.isHigherOrderStatement()) {
           
            final BudgetValue budget1=null;
            final BudgetValue budget2=null;
            BudgetValue budget3=null;
            final TruthValue truth1=null;
            final TruthValue truth2=null;
            TruthValue truth3=null;
            final TruthValue value1 = sentence.truth;
            final TruthValue value2 = belief.truth;
            
            if (sentence.isQuestion()) {
               /* budget1 = BudgetFunctions.backward(value2, nal);
                budget2 = BudgetFunctions.backwardWeak(value2, nal);*/
                budget3 = BudgetFunctions.backward(value2, nal);
            } else if (sentence.isQuest()) {
               /* budget1 = BudgetFunctions.backwardWeak(value2, nal);
                budget2 = BudgetFunctions.backward(value2, nal);*/
                budget3 = BudgetFunctions.backwardWeak(value2, nal);            
            } else {
                if (sentence.isGoal()) {
                  /*  truth1 = TruthFunctions.desireStrong(value1, value2);
                    truth2 = TruthFunctions.desireWeak(value2, value1);*/
                    truth3 = TruthFunctions.desireStrong(value1, value2, nal.narParameters);
                } else { 
                    // isJudgment
                   /* truth1 = TruthFunctions.abduction(value1, value2);
                    truth2 = TruthFunctions.abduction(value2, value1);*/
                    truth3 = TruthFunctions.comparison(value1, value2, nal.narParameters);
                }

                /*budget1 = BudgetFunctions.forward(truth1, nal);
                budget2 = BudgetFunctions.forward(truth2, nal);*/
                budget3 = BudgetFunctions.forward(truth3, nal);
            }
           
            /* Bridge to higher order statements:
            <b <=> k>.
            <b <=> c>.
            |-
            <k <-> c>. %F_cmp%
            */
           /* nal.doublePremiseTask(
                Statement.make(NativeOperator.INHERITANCE, term1, term2), 
                    truth1, budget1.clone(),false, false);
            nal.doublePremiseTask(
                Statement.make(NativeOperator.INHERITANCE, term2, term1), 
                    truth2, budget2.clone(),false, false);*/
            nal.doublePremiseTask(
                Statement.make(NativeOperator.SIMILARITY, term1, term2, TemporalRules.ORDER_NONE), 
                    truth3, budget3.clone(),false, false);
        }
    }

    /* --------------- rules used only in conditional inference --------------- */
    /**
     * {&lt;&lt;M --&gt; S&gt; ==&gt; &lt;M --&gt; P&gt;&gt;, &lt;M --&gt; S&gt;} |- &lt;M --&gt; P&gt;
     * <br>
     * {&lt;&lt;M --&gt; S&gt; ==&gt; &lt;M --&gt; P&gt;&gt;, &lt;M --&gt; P&gt;} |- &lt;M --&gt; S&gt;
     * <br>
     * {&lt;&lt;M --&gt; S&gt; &lt;=&gt; &lt;M --&gt; P&gt;&gt;, &lt;M --&gt; S&gt;} |- &lt;M --&gt; P&gt;
     * <br>
     * {&lt;&lt;M --&gt; S&gt; &lt;=&gt; &lt;M --&gt; P&gt;&gt;, &lt;M --&gt; P&gt;} |- &lt;M --&gt; S&gt;
     *
     * @param mainSentence The implication/equivalence premise
     * @param subSentence The premise on part of s1
     * @param side The location of s2 in s1
     * @param nal Reference to the memory
     */
    static void detachment(final Sentence mainSentence, final Sentence subSentence, final int side, final DerivationContext nal) {
        detachment(mainSentence, subSentence, side, true, nal);
    }
    static void detachment(final Sentence mainSentence, final Sentence subSentence, final int side, final boolean checkTermAgain, final DerivationContext nal) {
        final Statement statement = (Statement) mainSentence.term;
        if (!(statement instanceof Implication) && !(statement instanceof Equivalence)) {
            return;
        }
        final Term subject = statement.getSubject();
        final Term predicate = statement.getPredicate();
        final Term content;
        final Term term = subSentence.term;
        if ((side == 0) && (!checkTermAgain || term.equals(subject))) {
            content = predicate;
        } else if ((side == 1) && (!checkTermAgain || term.equals(predicate))) {
            content = subject;
        } else {
            return;
        }
        if ((content instanceof Statement) && ((Statement) content).invalid()) {
            return;
        }
        
        final Sentence taskSentence = nal.getCurrentTask().sentence;
        final Sentence beliefSentence = nal.getCurrentBelief();
        
        if (beliefSentence == null)
            return;
        
        final int order = statement.getTemporalOrder();
        long occurrence_time = nal.getCurrentTask().sentence.getOccurenceTime();
        if ((order != ORDER_NONE) && (order!=ORDER_INVALID)) {
            final long baseTime = subSentence.getOccurenceTime();
            if (baseTime != Stamp.ETERNAL) {
                final long inc = order * nal.narParameters.DURATION;
                occurrence_time = (side == 0) ? baseTime+inc : baseTime-inc;
            }
        }

        final TruthValue beliefTruth = beliefSentence.truth;
        final TruthValue truth1 = mainSentence.truth;
        final TruthValue truth2 = subSentence.truth;
        TruthValue truth = null;
        boolean strong = false;
        final BudgetValue budget;

        if (!(taskSentence.isQuestion() || taskSentence.isQuest())) {
            if (taskSentence.isGoal()) {
                strong = statement instanceof Equivalence || side != 0;
            } else { // isJudgment
                strong = statement instanceof Equivalence || side == 0;
            }
        }

        if (!(taskSentence.isQuestion() || taskSentence.isQuest())) {
            if (taskSentence.isGoal()) {
                if (statement instanceof Equivalence) {
                    truth = TruthFunctions.desireStrong(truth1, truth2, nal.narParameters);
                } else if (side == 0) {
                    truth = TruthFunctions.desireInd(truth1, truth2, nal.narParameters);
                } else {
                    truth = TruthFunctions.desireDed(truth1, truth2, nal.narParameters);
                }
            } else { // isJudgment
                if (statement instanceof Equivalence) {
                    truth = TruthFunctions.analogy(truth2, truth1, nal.narParameters);
                } else if (side == 0) {
                    truth = TruthFunctions.deduction(truth1, truth2, nal.narParameters);
                } else {
                    truth = TruthFunctions.abduction(truth2, truth1, nal.narParameters);
                }
            }
        }

        if (taskSentence.isQuestion()) {
            if (statement instanceof Equivalence) {
                budget = BudgetFunctions.backward(beliefTruth, nal);
            } else if (side == 0) {
                budget = BudgetFunctions.backwardWeak(beliefTruth, nal);
            } else {
                budget = BudgetFunctions.backward(beliefTruth, nal);
            }
        } else if (taskSentence.isQuest()) {
            if (statement instanceof Equivalence) {
                budget = BudgetFunctions.backwardWeak(beliefTruth, nal);
            } else if (side == 0) {
                budget = BudgetFunctions.backward(beliefTruth, nal);
            } else {
               budget = BudgetFunctions.backwardWeak(beliefTruth, nal);
            }
        } else {
            budget = BudgetFunctions.forward(truth, nal);
        }
        if(!Variables.indepVarUsedInvalid(content)) {
            final boolean allowOverlap = taskSentence.isJudgment() && strong;
            nal.getTheNewStamp().setOccurrenceTime(occurrence_time);
            nal.doublePremiseTask(content, truth, budget, false, allowOverlap); //(strong) when strong on judgement
        }
    }

    /**
     * {&lt;(&amp;&amp;, S1, S2, S3) ==&gt; P&gt;, S1} |- &lt;(&amp;&amp;, S2, S3) ==&gt; P&gt;
     * <br>
     * {&lt;(&amp;&amp;, S2, S3) ==&gt; P&gt;, &lt;S1 ==&gt; S2&gt;} |- &lt;(&amp;&amp;, S1, S3) ==&gt; P&gt;
     * <br>
     * {&lt;(&amp;&amp;, S1, S3) ==&gt; P&gt;, &lt;S1 ==&gt; S2&gt;} |- &lt;(&amp;&amp;, S2, S3) ==&gt; P&gt;
     *
     * @param premise1 The conditional premise
     * @param index The location of the shared term in the condition of premise1
     * @param premise2 The premise which, or part of which, appears in the
     * condition of premise1
     * @param side The location of the shared term in premise2: 0 for subject, 1
     * for predicate, -1 for the whole term
     * @param nal Reference to the memory
     */
    static void conditionalDedInd(final Sentence premise1Sentence, Implication premise1, short index, Term premise2, final int side, final DerivationContext nal) {
        final Task task = nal.getCurrentTask();
        final Sentence taskSentence = task.sentence;
        final Sentence belief = nal.getCurrentBelief();
        final boolean deduction = (side != 0);
        final boolean conditionalTask = Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, premise2, belief.term);
        final Term commonComponent;
        Term newComponent = null;
        if (side == 0 || side == 1) {
            Statement.EnumStatementSide sideOfCommonComponentAsEnum = side == 0 ? Statement.EnumStatementSide.SUBJECT : Statement.EnumStatementSide.PREDICATE;
            commonComponent = ((Statement)premise2).retBySide(sideOfCommonComponentAsEnum);
            newComponent = ((Statement)premise2).retBySide(Statement.retOppositeSide(sideOfCommonComponentAsEnum));
        }else {
            commonComponent = premise2;
        }
        
        final Term subj = premise1.getSubject();

        if (!(subj instanceof Conjunction)) {
            return;
        }
        final Conjunction oldCondition = (Conjunction) subj;
        
        final int index2 = Terms.indexOf(oldCondition.term,commonComponent);
        if (index2 >= 0) {
            index = (short) index2;
        } else {
            Term[] u = new Term[] { premise1, premise2 };            
            boolean match = Variables.unify(Symbols.VAR_INDEPENDENT, oldCondition.term[index], commonComponent, u);
            premise1 = (Implication) u[0]; premise2 = u[1];
            
            if (!match && (commonComponent.getClass() == oldCondition.getClass())) {

                final CompoundTerm compoundCommonComponent = ((CompoundTerm) commonComponent);
                
                if ((oldCondition.term.length > index) && (compoundCommonComponent.term.length > index)) { // assumption: { was missing
                    u = new Term[] { premise1, premise2 };
                    match = Variables.unify(Symbols.VAR_INDEPENDENT, 
                            oldCondition.term[index], 
                            compoundCommonComponent.term[index], 
                            u);
                    premise1 = (Implication) u[0]; premise2 = u[1];
                }
                
            }
            if (!match) {
                return;
            }
        }
        final int conjunctionOrder = subj.getTemporalOrder();
        if (conjunctionOrder == ORDER_FORWARD) {
            if (index > 0) {
                return;
            }
            if ((side == 0) && (premise2.getTemporalOrder() == ORDER_FORWARD)) {
                return;
            }
            if ((side == 1) && (premise2.getTemporalOrder() == ORDER_BACKWARD)) {
                return;
            }
        }
        Term newCondition;
        if (oldCondition.equals(commonComponent)) {
            newCondition = null;
        } else {
            newCondition = oldCondition.setComponent(index, newComponent, nal.mem());
        }
        final Term content;
        
        long delta = 0;
        long mintime = 0;
        long maxtime = 0;
        boolean predictedEvent = false;
        
        if (newCondition != null) {
             if (newCondition instanceof Interval) {
                content = premise1.getPredicate();
                delta = ((Interval) newCondition).time;
                if(taskSentence.getOccurenceTime() != Stamp.ETERNAL) {
                   mintime = taskSentence.getOccurenceTime() + ((Interval) newCondition).time - 1;
                   maxtime = taskSentence.getOccurenceTime() + ((Interval) newCondition).time + 2;
                   predictedEvent = true;
                }
             } else {
                while ((newCondition instanceof Conjunction) && (((CompoundTerm) newCondition).term[0] instanceof Interval)) {
                    final Interval interval = (Interval) ((CompoundTerm) newCondition).term[0];
                    delta += interval.time;
                    newCondition = ((CompoundTerm)newCondition).setComponent(0, null, nal.mem());
                }
                content = Statement.make(premise1, newCondition, premise1.getPredicate(), premise1.getTemporalOrder());
             }
               
        } else {
            content = premise1.getPredicate();
        }
        
        if (content == null)
            return;        
        
        long occurrence_time = nal.getCurrentTask().sentence.getOccurenceTime();
        if (delta != 0) {
            long baseTime = taskSentence.getOccurenceTime();
            if (baseTime != Stamp.ETERNAL) {
                baseTime += delta;
                occurrence_time = baseTime;
            }
        }
        
        final TruthValue truth1 = taskSentence.truth;
        final TruthValue truth2 = belief.truth;
        TruthValue truth = null;
        final BudgetValue budget;

        if (!(taskSentence.isQuestion() || taskSentence.isQuest())) {
            if (taskSentence.isGoal()) {
                if (conditionalTask) {
                    truth = TruthFunctions.desireWeak(truth1, truth2, nal.narParameters);
                } else if (deduction) {
                    truth = TruthFunctions.desireInd(truth1, truth2, nal.narParameters);
                } else {
                    truth = TruthFunctions.desireDed(truth1, truth2, nal.narParameters);
                }
            } else {
                if (deduction) {
                    truth = TruthFunctions.deduction(truth1, truth2, nal.narParameters);
                } else if (conditionalTask) {
                    truth = TruthFunctions.induction(truth2, truth1, nal.narParameters);
                } else {
                    truth = TruthFunctions.induction(truth1, truth2, nal.narParameters);
                }
            }
        }

        if (taskSentence.isQuestion() || taskSentence.isQuest()) {
            budget = BudgetFunctions.backwardWeak(truth2, nal);
        } else {
            budget = BudgetFunctions.forward(truth, nal);
        }
        
        nal.getTheNewStamp().setOccurrenceTime(occurrence_time);
        final List<Task> ret = nal.doublePremiseTask(content, truth, budget,false, taskSentence.isJudgment() && deduction); //(allow overlap) when deduction on judgment
        if(!nal.evidentalOverlap && ret != null && ret.size() > 0 && predictedEvent && taskSentence.isJudgment() && truth != null && 
            truth.getExpectation() > nal.narParameters.DEFAULT_CONFIRMATION_EXPECTATION && !premise1Sentence.stamp.alreadyAnticipatedNegConfirmation) {
            premise1Sentence.stamp.alreadyAnticipatedNegConfirmation = true;
            ProcessAnticipation.anticipate(nal, premise1Sentence, budget, mintime, maxtime, 1, new HashMap<Term,Term>());
        }
    }

    /**
     * {&lt;(&amp;&amp;, S1, S2, S3) &lt;=&gt; P&gt;, S1} |- &lt;(&amp;&amp;, S2, S3) &lt;=&gt; P&gt;
     * <br>
     * {&lt;(&amp;&amp;, S2, S3) &lt;=&gt; P&gt;, &lt;S1 ==&gt; S2&gt;} |- &lt;(&amp;&amp;, S1, S3) &lt;=&gt; P&gt;
     * <br>
     * {&lt;(&amp;&amp;, S1, S3) &lt;=&gt; P&gt;, &lt;S1 ==&gt;
     *
     * @param premise1 The equivalence premise
     * @param index The location of the shared term in the condition of premise1
     * @param premise2 The premise which, or part of which, appears in the condition of premise1
     * @param side The location of the shared term in premise2: 0 for subject, 1 for predicate, -1 for the whole term
     * @param nal Reference to the memory
     */
    static void conditionalAna(Equivalence premise1, final short index, Term premise2, final int side, final DerivationContext nal) {
        final Task task = nal.getCurrentTask();
        final Sentence taskSentence = task.sentence;
        final Sentence belief = nal.getCurrentBelief();
        final boolean conditionalTask = Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, premise2, belief.term);
        final Term commonComponent;
        Term newComponent = null;
        if (side == 0) {
            commonComponent = ((Statement) premise2).getSubject();
            newComponent = ((Statement) premise2).getPredicate();
        } else if (side == 1) {
            commonComponent = ((Statement) premise2).getPredicate();
            newComponent = ((Statement) premise2).getSubject();
        } else {
            commonComponent = premise2;
        }

        final Term tm = premise1.getSubject();
        if (!(tm instanceof Conjunction)) {
            return;
        }
        final Conjunction oldCondition = (Conjunction) tm;

        Term[] u = new Term[] { premise1, premise2 };
        boolean match = Variables.unify(Symbols.VAR_DEPENDENT, oldCondition.term[index], commonComponent, u);
        premise1 = (Equivalence) u[0]; premise2 = u[1];
        
        if (!match && (commonComponent.getClass() == oldCondition.getClass())) {
            u = new Term[] { premise1, premise2 };
            match = Variables.unify(Symbols.VAR_DEPENDENT, oldCondition.term[index], ((CompoundTerm) commonComponent).term[index], u);
            premise1 = (Equivalence) u[0]; premise2 = u[1];
        }
        if (!match) {
            return;
        }
        final int conjunctionOrder = oldCondition.getTemporalOrder();
        if (conjunctionOrder == ORDER_FORWARD) {
            if (index > 0) {
                return;
            }
            if ((side == 0) && (premise2.getTemporalOrder() == ORDER_FORWARD)) {
                return;
            }
            if ((side == 1) && (premise2.getTemporalOrder() == ORDER_BACKWARD)) {
                return;
            }
        }
        final Term newCondition;
        if (oldCondition.equals(commonComponent)) {
            newCondition = null;
        } else {
            newCondition = oldCondition.setComponent(index, newComponent, nal.mem());
        }
        final Term content;
        if (newCondition != null) {
            content = Statement.make(premise1, newCondition, premise1.getPredicate(), premise1.getTemporalOrder());
        } else {
            content = premise1.getPredicate();
        }
        
        if (content == null)
            return;
        
        final TruthValue truth1 = taskSentence.truth;
        final TruthValue truth2 = belief.truth;
        TruthValue truth = null;
        final BudgetValue budget;
        if (!(taskSentence.isQuestion() || taskSentence.isQuest())) {
            if (taskSentence.isGoal()) {
                truth = TruthFunctions.lookupTruthFunctionByBoolAndCompute(conditionalTask, TruthFunctions.EnumType.DESIREWEAK, TruthFunctions.EnumType.DESIREDED, truth1, truth2, nal.narParameters);
            } else {
                truth = TruthFunctions.lookupTruthFunctionByBoolAndCompute(conditionalTask, TruthFunctions.EnumType.COMPARISON, TruthFunctions.EnumType.ANALOGY, truth1, truth2, nal.narParameters);
            }
        }

        if (taskSentence.isQuestion() || taskSentence.isQuest()) {
            budget = BudgetFunctions.backwardWeak(truth2, nal);
        } else {
            budget = BudgetFunctions.forward(truth, nal);
        }

        nal.doublePremiseTask(content, truth, budget, false, taskSentence.isJudgment() && !conditionalTask); //(allow overlap) when !conditionalTask on judgment
    }

    /**
     * {&lt;(&amp;&amp;, S2, S3) ==&gt; P&gt;, &lt;(&amp;&amp;, S1, S3) ==&gt; P&gt;} |- &lt;S1 ==&gt; S2&gt;
     *
     * @param cond1 The condition of the first premise
     * @param cond2 The condition of the second premise
     * @param st1 The first premise
     * @param st2 The second premise
     * @param nal Reference to the memory
     * @return Whether there are derived tasks
     */
    static boolean conditionalAbd(final Term cond1, final Term cond2, final Statement st1, final Statement st2, final DerivationContext nal) {
        if (!(st1 instanceof Implication) || !(st2 instanceof Implication)) {
            return false;
        }
        if (!(cond1 instanceof Conjunction) && !(cond2 instanceof Conjunction)) {
            return false;
        }
        final int order1 = st1.getTemporalOrder();
        final int order2 = st2.getTemporalOrder();
        if (order1 != reverseOrder(order2)) {
            return false;
        }
        Term term1 = null;
        Term term2 = null;
        if (cond1 instanceof Conjunction) {
            term1 = reduceComponents((CompoundTerm) cond1, cond2, nal.mem());
        }
        if (cond2 instanceof Conjunction) {
            term2 = reduceComponents((CompoundTerm) cond2, cond1, nal.mem());
        }
        if ((term1 == null) && (term2 == null)) {
            return false;
        }
        final Task task = nal.getCurrentTask();
        final Sentence sentence = task.sentence;
        final Sentence belief = nal.getCurrentBelief();
        final TruthValue value1 = sentence.truth;
        final TruthValue value2 = belief.truth;

        final boolean keepOrder = Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, st1, task.getTerm());

        // we folded the logic to use loops for more compact code
        for (int loop=0;loop<2;loop++) {
            final boolean isFirstLoop = loop == 0;
            Term term1InLoop = isFirstLoop ? term1 : term2;
            Term term2InLoop = isFirstLoop ? term2 : term1;

            if (term1InLoop == null) {
                continue;
            }
            Term content;
            TruthValue truth = null;
            BudgetValue budget;

            if (term2InLoop != null) {
                content = Statement.make(isFirstLoop ? st2 : st1, term2InLoop, term1InLoop, isFirstLoop ? st2.getTemporalOrder() : st1.getTemporalOrder());
            } else {
                content = term1InLoop;
                if(content.hasVarIndep()) {
                    return false;
                }
            }

            if (sentence.isQuestion() || sentence.isQuest()) {
                budget = BudgetFunctions.backwardWeak(value2, nal);
            } else {
                if (sentence.isGoal()) {
                    truth = TruthFunctions.lookupTruthFunctionByBoolAndCompute(keepOrder, TruthFunctions.EnumType.DESIREDED, TruthFunctions.EnumType.DESIREIND, value1, value2, nal.narParameters);
                } else { // isJudgment
                    if (isFirstLoop) {
                        truth = TruthFunctions.abduction(value2, value1, nal.narParameters);
                    }
                    else {
                        truth = TruthFunctions.abduction(value1, value2, nal.narParameters);
                    }
                }
                budget = BudgetFunctions.forward(truth, nal);
            }
            nal.doublePremiseTask(content, truth, budget,false, false);
        }
        
        return true;
    }

    /**
     * {(&amp;&amp;, &lt;#x() --&gt; S&gt;, &lt;#x() --&gt; P&gt;&gt;, &lt;M --&gt; P&gt;} |- &lt;M --&gt; S&gt;
     *
     * @param compound The compound term to be decomposed
     * @param component The part of the compound to be removed
     * @param compoundTask Whether the compound comes from the task
     * @param nal Reference to the memory
     */
    static void elimiVarDep(final CompoundTerm compound, final Term component, final boolean compoundTask, final DerivationContext nal) {
        Term comp = null;
        for(final Term t : compound) {
            final Term[] unify = new Term[] { t, component };
            if(Variables.unify(Symbols.VAR_DEPENDENT, unify)) {
                comp = t;
                break;
            }
            if(Variables.unify(Symbols.VAR_QUERY, unify)) {
                comp = t;
                break;
            }
        }
        if(comp == null) {
            return;
        }
        final Term content = reduceComponents(compound, comp, nal.mem());
        if ((content == null) || ((content instanceof Statement) && ((Statement) content).invalid())) {
            return;
        }
        final Task task = nal.getCurrentTask();
        final Sentence sentence = task.sentence;
        final Sentence belief = nal.getCurrentBelief();
        final TruthValue v1 = sentence.truth;
        final TruthValue v2 = belief.truth;
        TruthValue truth = null;
        final BudgetValue budget;

        if (!(sentence.isQuestion() || sentence.isQuest())) {
            if (sentence.isGoal()) {
                truth = TruthFunctions.lookupTruthFunctionByBoolAndCompute(compoundTask, TruthFunctions.EnumType.DESIREDED, TruthFunctions.EnumType.DESIREIND, v1, v2, nal.narParameters);
            } else {
                truth = (compoundTask ? TruthFunctions.anonymousAnalogy(v1, v2, nal.narParameters) : TruthFunctions.anonymousAnalogy(v2, v1, nal.narParameters));
            }
        }

        if (sentence.isQuestion() || sentence.isQuest()) {
            budget = (compoundTask ? BudgetFunctions.backward(v2, nal) : BudgetFunctions.backwardWeak(v2, nal));
        } else {
            budget = BudgetFunctions.compoundForward(truth, content, nal);
        }

        nal.doublePremiseTask(content, truth, budget,false, false);
    }
}
