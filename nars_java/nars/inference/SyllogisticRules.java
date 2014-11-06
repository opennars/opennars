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
package nars.inference;

import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.core.control.NAL;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import static nars.inference.TemporalRules.ORDER_BACKWARD;
import static nars.inference.TemporalRules.ORDER_FORWARD;
import static nars.inference.TemporalRules.ORDER_INVALID;
import static nars.inference.TemporalRules.ORDER_NONE;
import static nars.inference.TemporalRules.abdIndComOrder;
import static nars.inference.TemporalRules.analogyOrder;
import static nars.inference.TemporalRules.dedExeOrder;
import static nars.inference.TemporalRules.resemblanceOrder;
import static nars.inference.TemporalRules.reverseOrder;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Equivalence;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Terms;
import static nars.language.Terms.reduceComponents;
import nars.language.Variables;


/**
 * Syllogisms: Inference rules based on the transitivity of the relation.
 */
public final class SyllogisticRules {

    /* --------------- rules used in both first-tense inference and higher-tense inference --------------- */
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
    static void dedExe(Term term1, Term term2, Sentence sentence, Sentence belief, NAL nal) {
        if (Statement.invalidStatement(term1, term2)) {
            return;
        }
        int order1 = sentence.content.getTemporalOrder();
        int order2 = belief.content.getTemporalOrder();
        int order = dedExeOrder(order1, order2);
        if (order == ORDER_INVALID) {
            return;
        }
        TruthValue value1 = sentence.truth;
        TruthValue value2 = belief.truth;
        TruthValue truth1 = null;
        TruthValue truth2 = null;
        BudgetValue budget1, budget2;
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
        Statement content = (Statement) sentence.content;
        Statement content1 = Statement.make(content, term1, term2, order);
        Statement content2 = Statement.make(content, term2, term1, reverseOrder(order));
        
        if ((content1 == null) || (content2 == null))
            return;
        
        nal.doublePremiseTask(content1, truth1, budget1,false);
        nal.doublePremiseTask(content2, truth2, budget2,false);
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
    static void abdIndCom(final Term term1, final Term term2, final Sentence sentence1, final Sentence sentence2, final int figure, final NAL nal) {
        if (Statement.invalidStatement(term1, term2) || Statement.invalidPair(term1, term2)) {
            return;
        }
        int order1 = sentence1.content.getTemporalOrder();
        int order2 = sentence2.content.getTemporalOrder();
        int order = abdIndComOrder(order1, order2);
        if (order == ORDER_INVALID) {
            return;
        }
        Statement taskContent = (Statement) sentence1.content;
        TruthValue truth1 = null;
        TruthValue truth2 = null;
        TruthValue truth3 = null;
        BudgetValue budget1, budget2, budget3;
        TruthValue value1 = sentence1.truth;
        TruthValue value2 = sentence2.truth;
        if (sentence1.isQuestion()) {
            budget1 = BudgetFunctions.backward(value2, nal);
            budget2 = BudgetFunctions.backwardWeak(value2, nal);
            budget3 = BudgetFunctions.backward(value2, nal);
        } else if (sentence1.isQuest()) {
            budget1 = BudgetFunctions.backwardWeak(value2, nal);
            budget2 = BudgetFunctions.backward(value2, nal);
            budget3 = BudgetFunctions.backwardWeak(value2, nal);            
        } else {
            if (sentence1.isGoal()) {
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
                
        nal.doublePremiseTask(
                Statement.make(taskContent, term1, term2, order), 
                    truth1, budget1,false);
        nal.doublePremiseTask(
                Statement.make(taskContent, term2, term1, reverseOrder(order)), 
                    truth2, budget2,false);
        nal.doublePremiseTask(
                Statement.makeSym(taskContent, term1, term2, order), 
                    truth3, budget3,false);
        
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
    static void analogy(Term subj, Term pred, Sentence asym, Sentence sym, int figure, NAL nal) {
        if (Statement.invalidStatement(subj, pred)) {
            return;
        }
        int order1 = asym.content.getTemporalOrder();
        int order2 = sym.content.getTemporalOrder();
        int order = analogyOrder(order1, order2, figure);
        if (order == ORDER_INVALID) {
            return;
        } else if (figure < 20) {
            order = reverseOrder(order);
        }
        Statement st = (Statement) asym.content;
        TruthValue truth = null;
        BudgetValue budget;
        Sentence sentence = nal.getCurrentTask().sentence;
        CompoundTerm taskTerm = (CompoundTerm) sentence.content;
        if (sentence.isQuestion() || sentence.isQuest()) {
            if (taskTerm.isCommutative()) {
                budget = BudgetFunctions.backwardWeak(asym.truth, nal);
            } else {
                budget = BudgetFunctions.backward(sym.truth, nal);
            }
        } else {
            if (sentence.isGoal()) {
                if (taskTerm.isCommutative()) {
                    truth = TruthFunctions.desireWeak(asym.truth, sym.truth);
                } else {
                    truth = TruthFunctions.desireStrong(asym.truth, sym.truth);
                }
            } else {
                truth = TruthFunctions.analogy(asym.truth, sym.truth);
            }
            
            budget = BudgetFunctions.forward(truth, nal);
        }
        
        nal.mem().logic.ANALOGY.commit();
        nal.doublePremiseTask( Statement.make(st, subj, pred, order), truth, budget,false);
    }

    /**
     * {<S <=> M>, <M <=> P>} |- <S <=> P>
     *
     * @param term1 Subject of the new task
     * @param term2 Predicate of the new task
     * @param belief The first premise
     * @param sentence The second premise
     * @param figure Locations of the shared term in premises
     * @param nal Reference to the memory
     */
    static void resemblance(Term term1, Term term2, Sentence belief, Sentence sentence, int figure, NAL nal) {
        if (Statement.invalidStatement(term1, term2)) {
            return;
        }
        int order1 = belief.content.getTemporalOrder();
        int order2 = sentence.content.getTemporalOrder();
        int order = resemblanceOrder(order1, order2, figure);
        if (order == ORDER_INVALID) {
            return;
        }
        Statement st = (Statement) belief.content;
        TruthValue truth = null;
        BudgetValue budget;
        if (sentence.isQuestion() || sentence.isQuest()) {
            budget = BudgetFunctions.backward(belief.truth, nal);
        } else {
            if (sentence.isGoal()) {
                truth = TruthFunctions.desireStrong(sentence.truth, belief.truth);
            } else {
                truth = TruthFunctions.resemblance(belief.truth, sentence.truth);
            }            
            budget = BudgetFunctions.forward(truth, nal);
        }
        
        nal.doublePremiseTask( Statement.make(st, term1, term2, order), truth, budget,false );
    }

    /* --------------- rules used only in conditional inference --------------- */
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
    static void detachment(Sentence mainSentence, Sentence subSentence, int side, NAL nal) {
        Statement statement = (Statement) mainSentence.content;
        if (!(statement instanceof Implication) && !(statement instanceof Equivalence)) {
            return;
        }
        Term subject = statement.getSubject();
        Term predicate = statement.getPredicate();
        Term content;
        Term term = subSentence.content;
        if ((side == 0) && term.equals(subject)) {
            content = predicate;
        } else if ((side == 1) && term.equals(predicate)) {
            content = subject;
        } else {
            return;
        }
        if ((content instanceof Statement) && ((Statement) content).invalid()) {
            return;
        }
        
        Sentence taskSentence = nal.getCurrentTask().sentence;
        Sentence beliefSentence = nal.getCurrentBelief();
        
        if (beliefSentence == null)
            return;
        
        int order = statement.getTemporalOrder();
        if ((order != ORDER_NONE) && (order!=ORDER_INVALID) && (!taskSentence.isGoal()) && (!taskSentence.isQuest())) {
            long baseTime = subSentence.getOccurenceTime();
            if (baseTime == Stamp.ETERNAL) {
                baseTime = nal.getTime();
            }
            long inc = order * nal.mem().param.duration.get();
            long time = (side == 0) ? baseTime+inc : baseTime-inc;
            nal.getTheNewStamp().setOccurrenceTime(time);
        }

        
        TruthValue beliefTruth = beliefSentence.truth;
        TruthValue truth1 = mainSentence.truth;
        TruthValue truth2 = subSentence.truth;
        TruthValue truth = null;
        BudgetValue budget;
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
                } else if (side == 0) {
                    truth = TruthFunctions.desireInd(truth1, truth2);
                } else {
                    truth = TruthFunctions.desireDed(truth1, truth2);
                }
            } else { // isJudgment
                if (statement instanceof Equivalence) {
                    truth = TruthFunctions.analogy(truth2, truth1);
                } else if (side == 0) {
                    truth = TruthFunctions.deduction(truth1, truth2);
                } else {
                    truth = TruthFunctions.abduction(truth2, truth1);
                }
            }
            budget = BudgetFunctions.forward(truth, nal);
        }
        if(!Variables.indepVarUsedInvalid(content)) {
            nal.doublePremiseTask(content, truth, budget,false);
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
    static void conditionalDedInd(Implication premise1, short index, Term premise2, int side, NAL nal) {
        Task task = nal.getCurrentTask();
        Sentence taskSentence = task.sentence;
        Sentence belief = nal.getCurrentBelief();
        boolean deduction = (side != 0);
        boolean conditionalTask = Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, premise2, belief.content);
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
            return;
        }
        Conjunction oldCondition = (Conjunction) subj;

        
        int index2 = Terms.indexOf(oldCondition.term,commonComponent);
        if (index2 >= 0) {
            index = (short) index2;
        } else {
            Term[] u = new Term[] { premise1, premise2 };            
            boolean match = Variables.unify(Symbols.VAR_INDEPENDENT, oldCondition.term[index], commonComponent, u);
            premise1 = (Implication) u[0]; premise2 = u[1];
            
            if (!match && (commonComponent.getClass() == oldCondition.getClass())) {
            
                CompoundTerm compoundCommonComponent = ((CompoundTerm) commonComponent);
                
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
        int conjunctionOrder = subj.getTemporalOrder();
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
        Term content;
        
        long delta = 0;
        final Interval.AtomicDuration duration = nal.mem().param.duration;
        
        if (newCondition != null) {
             if (newCondition instanceof Interval) {
                 content = premise1.getPredicate();
                 delta = ((Interval) newCondition).getTime(duration);
             } else if ((newCondition instanceof Conjunction) && (((CompoundTerm) newCondition).term[0] instanceof Interval)) {
                 Interval interval = (Interval) ((CompoundTerm) newCondition).term[0];
                 delta = interval.getTime(duration);
                 newCondition = ((CompoundTerm)newCondition).setComponent(0, null, nal.mem());
                 content = Statement.make(premise1, newCondition, premise1.getPredicate(), premise1.getTemporalOrder());
             } else {
                 content = Statement.make(premise1, newCondition, premise1.getPredicate(), premise1.getTemporalOrder());
             }
               
        } else {
            content = premise1.getPredicate();
        }
        
        if (content == null)
            return;        
        
        if (delta != 0) {
            long baseTime = (belief.content instanceof Implication) ?
                taskSentence.getOccurenceTime() : belief.getOccurenceTime();
            if (baseTime == Stamp.ETERNAL) {
                baseTime = nal.getTime();
            }
            baseTime += delta;
            nal.getTheNewStamp().setOccurrenceTime(baseTime);
        }
        
        TruthValue truth1 = taskSentence.truth;
        TruthValue truth2 = belief.truth;
        TruthValue truth = null;
        BudgetValue budget;
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
        nal.doublePremiseTask(content, truth, budget,false);
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
    static void conditionalAna(Equivalence premise1, short index, Term premise2, int side, NAL nal) {
        Task task = nal.getCurrentTask();
        Sentence taskSentence = task.sentence;
        Sentence belief = nal.getCurrentBelief();
        boolean conditionalTask = Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, premise2, belief.content);
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
            return;
        }
        Conjunction oldCondition = (Conjunction) tm;

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
        int conjunctionOrder = oldCondition.getTemporalOrder();
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
        Term content;
        if (newCondition != null) {
            content = Statement.make(premise1, newCondition, premise1.getPredicate(), premise1.getTemporalOrder());
        } else {
            content = premise1.getPredicate();
        }
        
        if (content == null)
            return;
        
        TruthValue truth1 = taskSentence.truth;
        TruthValue truth2 = belief.truth;
        TruthValue truth = null;
        BudgetValue budget;
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
        nal.doublePremiseTask(content, truth, budget,false);
    }

    /**
     * {<(&&, S2, S3) ==> P>, <(&&, S1, S3) ==> P>} |- <S1 ==> S2>
     *
     * @param cond1 The condition of the first premise
     * @param cond2 The condition of the second premise
     * @param taskContent The first premise
     * @param st2 The second premise
     * @param nal Reference to the memory
     * @return Whether there are derived tasks
     */
    static boolean conditionalAbd(Term cond1, Term cond2, Statement st1, Statement st2, NAL nal) {
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
        Term term2 = null;
//        if ((cond1 instanceof Conjunction) && !Variable.containVarDep(cond1.getName())) {
        if (cond1 instanceof Conjunction) {
            term1 = reduceComponents((CompoundTerm) cond1, cond2, nal.mem());
        }
//        if ((cond2 instanceof Conjunction) && !Variable.containVarDep(cond2.getName())) {
        if (cond2 instanceof Conjunction) {
            term2 = reduceComponents((CompoundTerm) cond2, cond1, nal.mem());
        }
        if ((term1 == null) && (term2 == null)) {
            return false;
        }
        Task task = nal.getCurrentTask();
        Sentence sentence = task.sentence;
        Sentence belief = nal.getCurrentBelief();
        TruthValue value1 = sentence.truth;
        TruthValue value2 = belief.truth;
        Term content;
        
        boolean keepOrder = Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, st1, task.getContent());
        
        TruthValue truth = null;
        BudgetValue budget;
        
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
            nal.doublePremiseTask(content, truth, budget,false);
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
            nal.doublePremiseTask(content, truth, budget,false);
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
    static void elimiVarDep(CompoundTerm compound, Term component, boolean compoundTask, NAL nal) {
        Term content = reduceComponents(compound, component, nal.mem());
        if ((content == null) || ((content instanceof Statement) && ((Statement) content).invalid())) {
            return;
        }
        Task task = nal.getCurrentTask();
        Sentence sentence = task.sentence;
        Sentence belief = nal.getCurrentBelief();
        TruthValue v1 = sentence.truth;
        TruthValue v2 = belief.truth;
        TruthValue truth = null;
        BudgetValue budget;
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
        nal.doublePremiseTask(content, truth, budget,false);
    }
}
