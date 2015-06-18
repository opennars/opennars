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
 * but WITHOUT ANY WARRANTY; without even the abduction warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.nal.nal5;

import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.*;
import nars.nal.nal2.Similarity;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.TemporalRules;
import nars.nal.stamp.Stamp;
import nars.nal.term.Compound;
import nars.nal.term.Statement;
import nars.nal.term.Term;

import java.util.Random;

import static nars.nal.Terms.reduceComponents;
import static nars.nal.nal7.TemporalRules.*;


/**
 * Syllogisms: Inference rules based on the transitivity of the relation.
 */
public final class SyllogisticRules {

    /* --------------- rules used in both first-tense logic and higher-tense logic --------------- */
    /**
     * <pre>
     * {<S ==> M>, <M ==> P>} |- {<S ==> P>, <P ==> S>}
     * </pre>
     *
     * @param term1 Subject of the first new task
     * @param term2 Predicate of the first new task
     * @param sentence The first premise
     * @param belief The second premise
     * @param nal Reference to the memory
     */
    public static void dedExe(Term term1, Term term2, Task<Statement> sentence, Sentence belief, NAL nal) {
        if (Statement.invalidStatement(term1, term2)) {
            return;
        }
        int order1 = sentence.
                getTemporalOrder();
        int order2 = belief.term.getTemporalOrder();
        int order =  dedExeOrder(order1, order2);
        if (order == ORDER_INVALID) {
            return;
        }
        Truth value1 = sentence.getTruth();
        Truth value2 = belief.truth;
        Truth truth1 = null;
        Truth truth2 = null;
        Budget budget1, budget2;
        if (sentence.isQuestion()) {
            budget1 = BudgetFunctions.backwardWeak(value2, nal);
            budget2 = BudgetFunctions.backwardWeak(value2, nal);       
        } else if (sentence.isQuest()) {
             budget1 = BudgetFunctions.backward(value2, nal);
             budget2 = BudgetFunctions.backward(value2, nal);
        } else {
            if (sentence.isGoal()) {
                truth1 = TruthFunctions.desireWeak(value1, value2);
                truth2 = TruthFunctions.desireWeak(value1, value2);
            } else { 
                // isJudgment
                truth1 = TruthFunctions.deduction(value1, value2);
                truth2 = TruthFunctions.exemplification(value1, value2);
            }

            budget1 = BudgetFunctions.forward(truth1, nal);
            budget2 = BudgetFunctions.forward(truth2, nal);
        }
        Statement content = sentence.getTerm();
        Statement content1 = Statement.make(content, term1, term2, order);
        Statement content2 = Statement.make(content, term2, term1, reverseOrder(order));
        
        if ((content1 == null) || (content2 == null))
            return;

        //final Stamper stamp = nal.newStamp(sentence, belief);
        nal.deriveDouble(content1, sentence.getPunctuation(), truth1, budget1, sentence, belief, false, false);
        nal.deriveDouble(content2, sentence.getPunctuation(), truth2, budget2, sentence, belief, false, false);
    }

    /**
     * {<M ==> S>, <M ==> P>} |- {<S ==> P>, <P ==> S>, <S <=> P>}
     *
     * @param term1 Subject of the first new task
     * @param term2 Predicate of the first new task
     * @param sentence1 The first premise
     * @param sentence2 The second premise
     * @param figure Locations of the shared term in premises --- can be
     * removed?
     * @param nal Reference to the memory
     */
    public static void abdIndCom(final Term term1, final Term term2, final Task sentence1, final Sentence sentence2, final int figure, final NAL nal) {
        if (Statement.invalidStatement(term1, term2) || Statement.invalidPair(term1, term2)) {
            return;
        }
        int order1 = sentence1.getTerm().getTemporalOrder();
        int order2 = sentence2.term.getTemporalOrder();
        int order = abdIndComOrder(order1, order2);
        if (order == ORDER_INVALID) {
            return;
        }
        Statement taskContent = (Statement) sentence1.getTerm();
        Truth truth1 = null;
        Truth truth2 = null;
        Truth truth3 = null;
        Budget budget1, budget2, budget3;
        Truth value1 = sentence1.getTruth();
        Truth value2 = sentence2.truth;

        char p = sentence1.getPunctuation();
        if (p == Symbols.QUESTION) {
            budget1 = BudgetFunctions.backward(value2, nal);
            budget2 = BudgetFunctions.backwardWeak(value2, nal);
            budget3 = BudgetFunctions.backward(value2, nal);
        } else if (p == Symbols.QUEST) {
            budget1 = BudgetFunctions.backwardWeak(value2, nal);
            budget2 = BudgetFunctions.backward(value2, nal);
            budget3 = BudgetFunctions.backwardWeak(value2, nal);            
        } else {
            if (p == Symbols.GOAL) {
                truth1 = TruthFunctions.desireStrong(value1, value2);
                truth2 = TruthFunctions.desireWeak(value2, value1);
                truth3 = TruthFunctions.desireStrong(value1, value2);
            } else { 
                // isJudgment
                truth1 = TruthFunctions.abduction(value1, value2);
                truth2 = TruthFunctions.abduction(value2, value1);
                truth3 = TruthFunctions.comparison(value1, value2);
            }

            budget1 = BudgetFunctions.forward(truth1, nal);
            budget2 = BudgetFunctions.forward(truth2, nal);
            budget3 = BudgetFunctions.forward(truth3, nal);
        }

        if (order != ORDER_INVALID) {
            //final Stamper stamp = nal.newStamp(sentence1, sentence2);

            {
                Statement s = Statement.make(taskContent, term1, term2, order);
                if (s != null)
                    nal.deriveDouble(s, p, truth1, budget1, sentence1, sentence2, false, false);
            }

            {
                Statement s = Statement.make(taskContent, term2, term1, reverseOrder(order));
                if (s != null)
                    nal.deriveDouble(s, p, truth2, budget2, sentence1, sentence2, false, false);
            }

            {
                Statement s = Terms.makeSymStatement(taskContent, term1, term2, order);
                if (s != null)
                    nal.deriveDouble(s, p, truth3, budget3, sentence1, sentence2, false, false);
            }


            if(Global.BREAK_NAL_HOL_BOUNDARY && order1==order2 && isHigherOrderStatement(taskContent) && isHigherOrderStatement(sentence2.term)) { //
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
                    truth3=new DefaultTruth(truth3);
           /* nal.doublePremiseTask(
                Statement.make(NativeOperator.INHERITANCE, term1, term2),
                    truth1, budget1.clone(),false, false);
            nal.doublePremiseTask(
                Statement.make(NativeOperator.INHERITANCE, term2, term1),
                    truth2, budget2.clone(),false, false);*/
                Statement s = Similarity.make(term1, term2);
                if (s!=null)
                    nal.deriveDouble(s, p, truth3, budget3, sentence1, sentence2, false, false);
            }

        }


    }

    public static boolean isHigherOrderStatement(Term t) { //==> <=>
        return (t instanceof Equivalence) || (t instanceof Implication);
    }


    /**
     * {<S ==> P>, <M <=> P>} |- <S ==> P>
     *
     * @param subj Subject of the new task
     * @param pred Predicate of the new task
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     * @param figure Locations of the shared term in premises
     * @param nal Reference to the memory
     */
    public static boolean analogy(Term subj, Term pred, Task asym, Sentence sym, int figure, NAL nal) {
        if (Statement.invalidStatement(subj, pred)) {
            return false;
        }


        int order1 = asym.getTerm().getTemporalOrder();
        int order2 = sym.term.getTemporalOrder();
        int order = analogyOrder(order1, order2, figure);
        if (order == ORDER_INVALID)
            return false;

        Truth atru = asym.getTruth();

        Statement st = (Statement) asym.getTerm();
        Truth truth = null;
        Budget budget;
        Sentence sentence = nal.getCurrentTask().sentence;
        Compound taskTerm = sentence.term;
        if (sentence.isQuestion() || sentence.isQuest()) {
            if (taskTerm.isCommutative()) {
                if(atru==null) { //a question for example
                    return false;
                }
                budget = BudgetFunctions.backwardWeak(atru, nal);
            } else {
                if(sym.truth==null) { //a question for example
                    return false;
                }
                budget = BudgetFunctions.backward(sym.truth, nal);
            }
        } else {
            if (sentence.isGoal()) {
                if (taskTerm.isCommutative()) {
                    truth = TruthFunctions.desireWeak(atru, sym.truth);
                } else {
                    truth = TruthFunctions.desireStrong(atru, sym.truth);
                }
            } else {
                truth = TruthFunctions.analogy(atru, sym.truth);
            }
            
            budget = BudgetFunctions.forward(truth, nal);
        }

        Compound statement = Sentence.termOrNull(Statement.make(st, subj, pred, order));
        if (statement == null) return false;

        nal.deriveDouble(statement, asym.getPunctuation(), truth, budget,
                asym, sym, false, true);

        nal.memory.logic.ANALOGY.hit();
        return true;
    }

    /**
     * {<S <=> M>, <M <=> P>} |- <S <=> P>
     *  @param term1 Subject of the new task
     * @param term2 Predicate of the new task
     * @param sentence The second premise
     * @param belief The first premise
     * @param figure Locations of the shared term in premises
     * @param nal Reference to the memory
     */
    public static boolean resemblance(Term term1, Term term2, Task<Statement> sentence, Sentence<Statement> belief, int figure, NAL nal) {
        if (Statement.invalidStatement(term1, term2)) {
            return false;
        }
        int order1 = belief.getTerm().getTemporalOrder();
        int order2 = sentence.getTerm().getTemporalOrder();
        int order = resemblanceOrder(order1, order2, figure);
        if (order == ORDER_INVALID) {
            return false;
        }
        Statement st = belief.getTerm();
        Truth truth = null;
        Budget budget;
        if (sentence.isQuestion() || sentence.isQuest()) {
            budget = BudgetFunctions.backward(belief.getTruth(), nal);
        } else {
            if (sentence.isGoal()) {
                truth = TruthFunctions.desireStrong(sentence.getTruth(), belief.getTruth());
            } else {
                truth = TruthFunctions.resemblance(belief.getTruth(), sentence.getTruth());
            }
            budget = BudgetFunctions.forward(truth, nal);
        }

        boolean beliefTermHO = isHigherOrderStatement(belief.getTerm());
        boolean sentenceTermHO = isHigherOrderStatement(sentence.getTerm());
        boolean eitherHigherOrder = (beliefTermHO || sentenceTermHO);
        boolean bothHigherOrder = (beliefTermHO && sentenceTermHO);

        if (!bothHigherOrder && eitherHigherOrder) {
            if (beliefTermHO) {
                order = belief.getTerm().getTemporalOrder();
            } else if (sentenceTermHO) {
                order = sentence.getTerm().getTemporalOrder();
            }
        }

        //Stamper sb = nal.newStamp(belief, sentence);

        Statement s = Statement.make(eitherHigherOrder ? NALOperator.EQUIVALENCE : NALOperator.SIMILARITY, term1, term2, true, order);
        //if(!Terms.equalSubTermsInRespectToImageAndProduct(term2, term2))
        boolean s1 = null!=nal.deriveDouble(s, sentence.getPunctuation(), truth, budget, sentence, belief, false, true), s2 = false;
        // nal.doublePremiseTask( Statement.make(st, term1, term2, order), truth, budget,false, true );

        if (Global.BREAK_NAL_HOL_BOUNDARY && !sentence.getTerm().hasVarIndep() && (st instanceof Equivalence) &&
                order1 == order2 &&
                beliefTermHO && sentenceTermHO) {

            Budget budget1 = null, budget2 = null, budget3 = null;
            Truth truth1 = null, truth2 = null, truth3 = null;
            Truth value1 = sentence.getTruth();
            Truth value2 = belief.getTruth();

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
                    truth3 = TruthFunctions.desireStrong(value1, value2);
                } else {
                    // isJudgment
                   /* truth1 = TruthFunctions.abduction(value1, value2);
                    truth2 = TruthFunctions.abduction(value2, value1);*/
                    truth3 = TruthFunctions.comparison(value1, value2);
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
            s2 = null!=nal.deriveDouble(
                    Statement.make(NALOperator.SIMILARITY, term1, term2, true, TemporalRules.ORDER_NONE),
                    sentence.getPunctuation(),
                    truth3, budget3.clone(), sentence, belief, false, false);


        }


        return s1 || s2;

        //-----
        //Original conclusion:
//        Statement nst = Statement.make(st, term1, term2, order).normalized();
//        if (nst == null) return false;
//
//        return nal.doublePremiseTask( nst, truth, budget,
//                sb,
//                false, true );

    }

    /* --------------- rules used only in conditional logic --------------- */
    /**
     * {<<M --> S> ==> <M --> P>>, <M --> S>} |- <M --> P> {<<M --> S> ==> <M
     * --> P>>, <M --> P>} |- <M --> S> {<<M --> S> <=> <M --> P>>, <M --> S>}
     * |- <M --> P> {<<M --> S> <=> <M --> P>>, <M --> P>} |- <M --> S>
     *
     * @param mainSentence The implication/equivalence premise
     * @param subSentence The premise on part of s1
     * @param side The location of s2 in s1
     * @param nal Reference to the memory
     */
    public static void detachment(Task<Statement> mainSentence, Sentence subSentence, int side, NAL nal) {
        Statement statement = mainSentence.getTerm();
        if (!(statement instanceof Implication) && !(statement instanceof Equivalence)) {
            return;
        }
        Term subject = statement.getSubject();
        Term predicate = statement.getPredicate();
        Term term = subSentence.term;

        final Compound content;
        if ((side == 0) && term.equals(subject)) {
            content = Terms.compoundOrNull(predicate);
        } else if ((side == 1) && term.equals(predicate)) {
            content = Terms.compoundOrNull(subject);
        } else
            return;

        if (content == null || (content instanceof Statement) && ((Statement) content).invalid())
            return;

        final Sentence taskSentence = nal.getCurrentTask().sentence;
        final Sentence beliefSentence = nal.getCurrentBelief();
        
        if (beliefSentence == null)
            return;

        final boolean temporalReasoning = nal.nal(7);

        //Stamper st = null;
        long occ;
        int order = statement.getTemporalOrder();

        if ((temporalReasoning)

            && ((statement.getTemporalOrder() != ORDER_NONE) && (order != ORDER_INVALID) && (!taskSentence.isGoal()) && (!taskSentence.isQuest() /*&& (!taskSentence.isQuestion()*/))) {
                long baseTime = subSentence.getOccurrenceTime();
                if (baseTime == Stamp.ETERNAL) {
                    baseTime = nal.time();
                }
                long inc = order * nal.memory.duration();
                long occurTime = (side == 0) ? baseTime + inc : baseTime - inc;
                //st = nal.newStamp(mainSentence, subSentence, occurTime);
                occ = occurTime;
        }
        else {
            //new stamp, inferring occurence time from the sentences
            //st = nal.newStamp(mainSentence, subSentence);
            occ = NAL.inferOccurenceTime(mainSentence.sentence, subSentence);
        }

        Truth beliefTruth = beliefSentence.truth;
        Truth truth1 = mainSentence.getTruth();
        Truth truth2 = subSentence.truth;
        Truth truth = null;
        Budget budget;

        boolean strong = false;

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
            if (taskSentence.isGoal()) {
                if (statement instanceof Equivalence) {
                    truth = TruthFunctions.desireStrong(truth1, truth2);
                    strong = true;
                } else if (side == 0) {
                    truth = TruthFunctions.desireInd(truth1, truth2);
                } else {
                    truth = TruthFunctions.desireDed(truth1, truth2);
                    strong = true;
                }
            } else { // isJudgment
                if (statement instanceof Equivalence) {
                    truth = TruthFunctions.analogy(truth2, truth1);
                    strong = true;
                } else if (side == 0) {
                    truth = TruthFunctions.deduction(truth1, truth2);
                    strong = true;
                } else {
                    truth = TruthFunctions.abduction(truth2, truth1);
                }
            }
            budget = BudgetFunctions.forward(truth, nal);
        }
        if(!Variables.indepVarUsedInvalid(content)) {
            nal.deriveDouble(content, mainSentence.getPunctuation(), truth, budget,
                    mainSentence, subSentence,
                    false, strong);
        }
    }

    /**
     * {<(&&, S1, S2, S3) ==> P>, S1} |- <(&&, S2, S3) ==> P> {<(&&, S2, S3) ==>
     * P>, <S1 ==> S2>} |- <(&&, S1, S3) ==> P> {<(&&, S1, S3) ==> P>, <S1 ==>
     * S2>} |- <(&&, S2, S3) ==> P>
     *
     * @param premise1 The conditional premise
     * @param index The location of the shared term in the condition of premise1
     * @param premise2 The premise which, or part of which, appears in the
     * condition of premise1
     * @param side The location of the shared term in premise2: 0 for subject, 1
     * for predicate, -1 for the whole term
     * @param nal Reference to the memory
     */
    public static Task conditionalDedInd(Implication premise1, short index, Term premise2, int side, NAL nal) {
        Task task = nal.getCurrentTask();
        final Sentence taskSentence = task.sentence;
        final Sentence belief = nal.getCurrentBelief();
        boolean deduction = (side != 0);
        boolean conditionalTask = Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, premise2, belief.term, nal.memory.random);
        Term commonComponent;
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
        
        Term subj = premise1.getSubject();

        if (!(subj instanceof Conjunction)) {
            return null;
        }
        final Conjunction oldCondition = (Conjunction) subj;

        
        int index2 = Terms.indexOf(oldCondition.term,commonComponent);
        if (index2 >= 0) {
            index = (short) index2;
        } else {
            Term[] u = new Term[] { premise1, premise2 };            
            boolean match = Variables.unify(Symbols.VAR_INDEPENDENT, oldCondition.term[index], commonComponent, u, nal.memory.random);
            premise1 = (Implication) u[0]; premise2 = u[1];
            
            if (!match && (Terms.equalType(commonComponent, oldCondition, true))) {
            
                Compound compoundCommonComponent = ((Compound) commonComponent);
                
                if ((oldCondition.term.length > index) && (compoundCommonComponent.term.length > index)) { // assumption: { was missing
                    u = new Term[] { premise1, premise2 };
                    match = Variables.unify(Symbols.VAR_INDEPENDENT, 
                            oldCondition.term[index], 
                            compoundCommonComponent.term[index], 
                            u, nal.memory.random);
                    premise1 = (Implication) u[0]; premise2 = u[1];
                }
                
            }
            if (!match) {
                return null;
            }
        }
        int conjunctionOrder = subj.getTemporalOrder();
        if (conjunctionOrder == ORDER_FORWARD) {
            if (index > 0) {
                return null;
            }
            if ((side == 0) && (premise2.getTemporalOrder() == ORDER_FORWARD)) {
                return null;
            }
            if ((side == 1) && (premise2.getTemporalOrder() == ORDER_BACKWARD)) {
                return null;
            }
        }
        Term newCondition;
        if (oldCondition.equals(commonComponent)) {
            newCondition = null;
        } else {
            newCondition = oldCondition.cloneReplacingSubterm(index, newComponent);
        }
        final Term content;
        
        long delta = 0;
        final int duration = nal.memory.duration();
        
        if (newCondition != null) {
             if (newCondition instanceof AbstractInterval) {
                 content = premise1.getPredicate();
                 delta = ((AbstractInterval) newCondition).cycles(nal.memory);
             } else if ((newCondition instanceof Conjunction) && (((Compound) newCondition).term[0] instanceof AbstractInterval)) {
                 AbstractInterval interval = (AbstractInterval) ((Compound) newCondition).term[0];
                 delta = interval.cycles(nal.memory);
                 newCondition = ((Compound)newCondition).cloneReplacingSubterm(0, null);
                 content = Statement.make(premise1, newCondition, premise1.getPredicate(), premise1.getTemporalOrder());
             } else {
                 content = Statement.make(premise1, newCondition, premise1.getPredicate(), premise1.getTemporalOrder());
             }
               
        } else {
            content = premise1.getPredicate();
        }
        
        if ((content == null) || (!(content instanceof Compound)))
            return null;


        long occ;
        if (nal.nal(7) && (delta != 0)) {

            long baseTime = (belief.term instanceof Implication) ?
                taskSentence.getOccurrenceTime() : belief.getOccurrenceTime();

            if (baseTime == Stamp.ETERNAL) {
                baseTime = nal.time();
            }

            if(premise1.getTemporalOrder()== TemporalRules.ORDER_CONCURRENT) {
                //https://groups.google.com/forum/#!topic/open-nars/ZfCM416Dx1M - Interval Simplification
                return null;
            }

            baseTime += delta;

            long occurTime = baseTime;
            occ = occurTime;
            //sb = nal.newStamp(taskSentence, belief, occurTime); //     //TemporalRules.applyExpectationOffset(nal.memory, premise1, occurTime)),
        }
        else {
            occ = Stamp.ETERNAL; //should this be NAL.inferTime..
            //sb = nal.newStamp(taskSentence, belief, Stamp.ETERNAL);
        }
        
        Truth truth1 = taskSentence.truth;
        Truth truth2 = belief.truth;
        Truth truth = null;
        Budget budget;
        if (taskSentence.isQuestion() || taskSentence.isQuest()) {
            budget = BudgetFunctions.backwardWeak(truth2, nal);
        } else {
            if (taskSentence.isGoal()) {
                if (conditionalTask) {
                    truth = TruthFunctions.desireWeak(truth1, truth2);
                } else if (deduction) {
                    truth = TruthFunctions.desireInd(truth1, truth2);
                } else {
                    truth = TruthFunctions.desireDed(truth1, truth2);
                }
            } else {
               if (deduction) {
                    truth = TruthFunctions.deduction(truth1, truth2);
                } else if (conditionalTask) {
                    truth = TruthFunctions.induction(truth2, truth1);
                } else {
                    truth = TruthFunctions.induction(truth1, truth2);
                }
            }
            budget = BudgetFunctions.forward(truth, nal);
        }

        return nal.deriveDouble((Compound) content, task.getPunctuation(), truth, budget,
                task, belief, false, deduction);
    }

    /**
     * {<(&&, S1, S2, S3) <=> P>, S1} |- <(&&, S2, S3) <=> P> {<(&&, S2, S3) <=> P>,
     * <S1 ==> S2>} |- <(&&, S1, S3) <=> P> {<(&&, S1, S3) <=> P>, <S1 ==>
     *
     * @param premise1 The equivalence premise
     * @param index The location of the shared term in the condition of premise1
     * @param premise2 The premise which, or part of which, appears in the
     * condition of premise1
     * @param side The location of the shared term in premise2: 0 for subject, 1
     * for predicate, -1 for the whole term
     * @param nal Reference to the memory
     */
    public static boolean conditionalAna(Equivalence premise1, short index, Term premise2, int side, NAL nal) {
        final Random r = nal.memory.random;

        Task task = nal.getCurrentTask();
        final Sentence taskSentence = task.sentence;
        final Sentence belief = nal.getCurrentBelief();
        boolean conditionalTask = Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, premise2, belief.term, r);
        Term commonComponent;
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

        Term tm = premise1.getSubject();
        if (!(tm instanceof Conjunction)) {
            return false;
        }
        Conjunction oldCondition = (Conjunction) tm;

        Term[] u = new Term[] { premise1, premise2 };
        boolean match = Variables.unify(Symbols.VAR_DEPENDENT, oldCondition.term[index], commonComponent, u, r);
        premise1 = (Equivalence) u[0]; premise2 = u[1];
        
        if (!match && (Terms.equalType(commonComponent, oldCondition))) {
            u = new Term[] { premise1, premise2 };
            match = Variables.unify(Symbols.VAR_DEPENDENT, oldCondition.term[index], ((Compound) commonComponent).term[index], u, r);
            premise1 = (Equivalence) u[0]; premise2 = u[1];
        }
        if (!match) {
            return false;
        }
        int conjunctionOrder = oldCondition.getTemporalOrder();
        if (conjunctionOrder == ORDER_FORWARD) {
            if (index > 0) {
                return false;
            }
            if ((side == 0) && (premise2.getTemporalOrder() == ORDER_FORWARD)) {
                return false;
            }
            if ((side == 1) && (premise2.getTemporalOrder() == ORDER_BACKWARD)) {
                return false;
            }
        }
        Term newCondition;
        if (oldCondition.equals(commonComponent)) {
            newCondition = null;
        } else {
            newCondition = oldCondition.cloneReplacingSubterm(index, newComponent);
        }
        final Compound content;
        if (newCondition != null) {
            content = Statement.make(premise1, newCondition, premise1.getPredicate(), premise1.getTemporalOrder());
            if (content == null)
                return false;
        } else {
            Term p = premise1.getPredicate();
            if (p instanceof Compound)
                content = (Compound)p;
            else
                return false;
        }

        Truth truth1 = taskSentence.truth;
        Truth truth2 = belief.truth;
        Truth truth = null;
        Budget budget;
        if (taskSentence.isQuestion() || taskSentence.isQuest()) {
            budget = BudgetFunctions.backwardWeak(truth2, nal);
        } else {
           if (taskSentence.isGoal()) {
                if (conditionalTask) {
                    truth = TruthFunctions.desireWeak(truth1, truth2);
                } else {
                    truth = TruthFunctions.desireDed(truth1, truth2);
                }
 
            } else {
                if (conditionalTask) {
                    truth = TruthFunctions.comparison(truth1, truth2);
                } else {
                    truth = TruthFunctions.analogy(truth1, truth2);
                }
            }
            budget = BudgetFunctions.forward(truth, nal);
        }

        nal.deriveDouble(content, task.getPunctuation(), truth, budget,
                task, belief,
                false, !conditionalTask);

        return true;
    }

    /**
     * {<(&&, S2, S3) ==> P>, <(&&, S1, S3) ==> P>} |- <S1 ==> S2>
     *
     * @param cond1 The condition of the first premise
     * @param cond2 The condition of the second premise
     * @param st2 The second premise
     * @param nal Reference to the memory
     * @return Whether there are derived tasks
     */
    public static boolean conditionalAbd(Term cond1, Term cond2, Statement st1, Statement st2, NAL nal) {
        if (!(st1 instanceof Implication) || !(st2 instanceof Implication)) {
            return false;
        }
        if (!(cond1 instanceof Conjunction) && !(cond2 instanceof Conjunction)) {
            return false;
        }
        int order1 = st1.getTemporalOrder();
        int order2 = st2.getTemporalOrder();
        if (order1 != reverseOrder(order2)) {
            return false;
        }
        Term term1 = null;
        //        if ((cond1 instanceof Conjunction) && !Variable.containVarDep(cond1.getName())) {
        if (cond1 instanceof Conjunction) {
            term1 = reduceComponents((Compound) cond1, cond2, nal.memory);
        }
//        if ((cond2 instanceof Conjunction) && !Variable.containVarDep(cond2.getName())) {
        Term term2 = null;
        if (cond2 instanceof Conjunction) {
            term2 = reduceComponents((Compound) cond2, cond1, nal.memory);
        }
        if ((term1 == null) && (term2 == null)) {
            return false;
        }
        Task task = nal.getCurrentTask();
        final Sentence sentence = task.sentence;
        final Sentence belief = nal.getCurrentBelief();
        Truth value1 = sentence.truth;
        Truth value2 = belief.truth;
        Term content;
        
        boolean keepOrder = Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, st1, task.getTerm(), nal.memory.random);
        
        Truth truth = null;
        Budget budget;
        
        //is this correct because the second term2!=null condition may overwrite the first
        //should the inner if be: if (term2 == null) then it makes sense.
        
        if (term1 != null) {
            if (term2 != null) {
                content = Statement.make(st2, term2, term1, st2.getTemporalOrder());
            } else {
                content = term1;
                if(content.hasVarIndep()) {
                    return false;
                }
            }
            if (content instanceof Compound) {

                if (sentence.isQuestion() || sentence.isQuest()) {
                    budget = BudgetFunctions.backwardWeak(value2, nal);
                } else {
                    if (sentence.isGoal()) {
                        if (keepOrder) {
                            truth = TruthFunctions.desireDed(value1, value2);
                        } else {
                            truth = TruthFunctions.desireInd(value1, value2);
                        }
                    } else { // isJudgment
                        truth = TruthFunctions.abduction(value2, value1);
                    }
                    budget = BudgetFunctions.forward(truth, nal);
                }

                nal.deriveDouble((Compound) content, task.getPunctuation(), truth, budget,
                        task, belief, false, false);
            }
        }
        
        if (term2 != null) {
            if (term1 != null) {
                content = Statement.make(st1, term1, term2, st1.getTemporalOrder());
            } else {
                content = term2;
                if(content.hasVarIndep()) {
                    return false;
                }
            }
            if (content instanceof Compound) {
                if (sentence.isQuestion() || sentence.isQuest()) {

                    budget = BudgetFunctions.backwardWeak(value2, nal);
                } else {
                    if (sentence.isGoal()) {
                        if (keepOrder) {
                            truth = TruthFunctions.desireDed(value1, value2);
                        } else {
                            truth = TruthFunctions.desireInd(value1, value2);
                        }
                    } else { // isJudgment
                        truth = TruthFunctions.abduction(value1, value2);
                    }
                    budget = BudgetFunctions.forward(truth, nal);
                }
                nal.deriveDouble((Compound) content, task.getPunctuation(), truth, budget, task, belief, false, false);
            }
        }
        
        return true;
    }

    /**
     * {(&&, <#x() --> S>, <#x() --> P>>, <M --> P>} |- <M --> S>
     *
     * @param compound The compound term to be decomposed
     * @param component The part of the compound to be removed
     * @param compoundTask Whether the compound comes from the task
     * @param nal Reference to the memory
     */
    public static void elimiVarDep(Compound compound, Term component, boolean compoundTask, NAL nal) {
        Term content = reduceComponents(compound, component, nal.memory);
        if ((content == null) || (!(content instanceof Compound)) || ((content instanceof Statement) && ((Statement) content).invalid())) {
            return;
        }
        Task task = nal.getCurrentTask();

        final Sentence sentence = task.sentence;
        final Sentence belief = nal.getCurrentBelief();

        Truth v1 = sentence.truth;
        Truth v2 = belief.truth;
        Truth truth = null;
        Budget budget;
        if (sentence.isQuestion() || sentence.isQuest()) {
            budget = (compoundTask ? BudgetFunctions.backward(v2, nal) : BudgetFunctions.backwardWeak(v2, nal));
        } else {
            if (sentence.isGoal()) {
                truth = (compoundTask ? TruthFunctions.desireDed(v1, v2) : TruthFunctions.desireInd(v1, v2));  // to check
            } else {
                truth = (compoundTask ? TruthFunctions.anonymousAnalogy(v1, v2) : TruthFunctions.anonymousAnalogy(v2, v1));
            }
            budget = BudgetFunctions.compoundForward(truth, content, nal);
        }
        nal.deriveDouble((Compound) content, task.getPunctuation(), truth, budget,
                task, belief, false, false);
    }
}
