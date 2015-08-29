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
package nars.nal;

import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.concept.BeliefTable;
import nars.concept.Concept;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.*;
import nars.nal.nal4.Image;
import nars.nal.nal4.ImageExt;
import nars.nal.nal4.ImageInt;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Disjunction;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.premise.Premise;
import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.*;
import nars.term.transform.Substitution;
import nars.truth.Truth;

import java.util.Map;
import java.util.Random;

import static nars.term.Terms.compoundOrNull;
import static nars.term.Terms.reduceComponents;
import static nars.truth.TruthFunctions.*;


/**
 * Compound term composition and decomposition rules, with two premises.
 * <p>
 * New compound terms are introduced only in forward logic, while
 * decompositional rules are also used in backward logic
 */
public final class CompositionalRules {




    /* -------------------- intersections and differences -------------------- */

    //Descriptive variable names are used during debugging;
    //but otherwise should be $1, $2, #1, #2 to help avoid the need to rename during certain normalizations
    final static Variable varInd1 = new Variable("$i");
    final static Variable varInd2 = new Variable("$j");
    final static Variable varDep = new Variable("#d");
    final static Variable varDep2 = new Variable("#e");
    final static Variable depIndVar1 = new Variable("#f");
    final static Variable depIndVar2 = new Variable("#g");

    /**
     * {<S ==> M>, <P ==> M>} |- {<(S|P) ==> M>, <(S&P) ==> M>, <(S-P) ==>
     * M>,
     * <(P-S) ==> M>}
     *
     * @param taskSentence The first premise
     * @param belief       The second premise
     * @param index        The location of the shared term
     * @param nal          Reference to the memory
     */
    static void composeCompound(final Statement taskContent, final Statement beliefContent, final int index, final NAL nal) {


        final Task taskBelief = nal.getTask();

        if ((!taskBelief.isJudgment()) || (!(taskContent.operator() == beliefContent.operator()))) {
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
        if ((componentT instanceof Compound) && ((Compound) componentT).containsAllTermsOf(componentB)) {
            decomposeCompound((Compound) componentT, componentB, componentCommon, index, true, order, nal);
            return;
        } else if ((componentB instanceof Compound) && ((Compound) componentB).containsAllTermsOf(componentT)) {
            decomposeCompound((Compound) componentB, componentT, componentCommon, index, false, order, nal);
            return;
        }
        final Truth truthT = taskBelief.getTruth();
        final Truth truthB = nal.getBelief().getTruth();
        final Truth truthOr = union(truthT, truthB);
        final Truth truthAnd = intersection(truthT, truthB);
        Truth truthDif = null;
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
                        truthDif = intersection(truthT, negation(truthB));
                    }
                } else if (truthT.isNegative()) {
                    termDif = DifferenceExt.make(componentB, componentT);
                    truthDif = intersection(truthB, negation(truthT));
                }
            } else if (taskContent instanceof Implication) {
                termOr = Disjunction.make(componentT, componentB);
                termAnd = Conjunction.make(componentT, componentB);
            }
            processComposed(taskContent, componentCommon, termOr, order, truthOr, nal);
            processComposed(taskContent, componentCommon, termAnd, order, truthAnd, nal);
            processComposed(taskContent, componentCommon, termDif, order, truthDif, nal);
        } else {    // index == 1
            if (taskContent instanceof Inheritance) {
                termOr = IntersectionExt.make(componentT, componentB);
                termAnd = IntersectionInt.make(componentT, componentB);
                if (truthB.isNegative()) {
                    if (!truthT.isNegative()) {
                        termDif = DifferenceInt.make(componentT, componentB);
                        truthDif = intersection(truthT, negation(truthB));
                    }
                } else if (truthT.isNegative()) {
                    termDif = DifferenceInt.make(componentB, componentT);
                    truthDif = intersection(truthB, negation(truthT));
                }
            } else if (taskContent instanceof Implication) {
                termOr = Conjunction.make(componentT, componentB);
                termAnd = Disjunction.make(componentT, componentB);
            }
            processComposed(taskContent, termOr, componentCommon, order, truthOr, nal);
            processComposed(taskContent, termAnd, componentCommon, order, truthAnd, nal);
            processComposed(taskContent, termDif, componentCommon, order, truthDif, nal);
        }
    }

    /**
     * Finish composing implication term
     *
     * @param premise1  Type of the contentInd
     * @param subject   Subject of contentInd
     * @param predicate Predicate of contentInd
     * @param truth     TruthValue of the contentInd
     * @param memory    Reference to the memory
     */
    private static Task processComposed(final Statement statement, final Term subject, final Term predicate, final int order, final Truth truth, final Premise p) {
        if ((subject == null) || (predicate == null)) {
            return null;
        }
        Statement content = Statement.make(statement, subject, predicate, order);
        if ((content == null) || statement == null /*|| (!(content instanceof Compound))*/
                || content.equals(statement.getTerm()) || content.equals(p.getBelief().getTerm())) {
            return null;
        }

        return p.deriveDouble(content, truth,
                BudgetFunctions.compoundForward(truth, content, p),
                false, true);
    }

    /* till this general code is ready, the easier solution
    public static void FindSame(Term a, Term b, HashMap<Term, Term> subs) {
        if(b.containsTermRecursively(a)) {
            
        }
    }*/
    
    /* --------------- rules used for variable introduction --------------- */
    // forward logic only?

    /**
     * {<(S|P) ==> M>, <P ==> M>} |- <S ==> M>
     *
     * @param implication     The implication term to be decomposed
     * @param componentCommon The part of the implication to be removed
     * @param term1           The other term in the contentInd
     * @param index           The location of the shared term: 0 for subject, 1 for
     *                        predicate
     * @param compoundTask    Whether the implication comes from the task
     * @param nal             Reference to the memory
     * @return whether a double premise decomposition was derived
     */
    private static Task decomposeCompound(final Compound compound, Term component, Term term1, int index, boolean compoundTask, int order, NAL nal) {

        if ((compound instanceof Statement) || (compound instanceof Image)) {
            return null;
        }
        Term term2 = reduceComponents(compound, component, nal.memory);
        if (term2 == null) {
            return null;
        }

        final Task task = nal.getTask();
        final Sentence belief = nal.getBelief();
        final Statement oldContent = (Statement) task.getTerm();

        Truth v1, v2;
        if (compoundTask) {
            v1 = task.getTruth();
            v2 = belief.getTruth();
        } else {
            v1 = belief.getTruth();
            v2 = task.getTruth();
        }
        Truth truth = null;
        Compound content;
        if (index == 0) {
            content = Statement.make(oldContent, term1, term2, order);
            if (content == null) {
                return null;
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
            content = Statement.make(oldContent, term2, term1, order);
            if (content == null) {
                return null;
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
            return nal.deriveDouble(content, truth,
                    BudgetFunctions.compoundForward(truth, content, nal),
                    false, true);
        }
        return null; //probably should never reach here
    }

    /**
     * {(||, S, P), P} |- S {(&&, S, P), P} |- S
     *  @param implication     The implication term to be decomposed
     * @param componentCommon The part of the implication to be removed
     * @param compoundTask    Whether the implication comes from the task
     * @param nal             Reference to the memory
     */
    static Task decomposeStatement(Compound compound, Term component, boolean compoundTask, int index, NAL nal) {
        if ((compound instanceof Conjunction) && (compound.getTemporalOrder() == TemporalRules.ORDER_FORWARD) && (index != 0)) {
            return null;
        }

        final Task task = nal.getTask();
        final Task belief = nal.getBelief();

        final boolean isQ = task.isQuestion() || task.isQuest();

        TaskSeed nonCyclic = nal.newDoublePremise(task, belief);
        if (nonCyclic==null) {
            if (!isQ) //dont skip the query var section is isQ true
                return null;
        }
        else {
            nonCyclic.punctuation(task.getPunctuation());
        }



        Compound content = compoundOrNull(reduceComponents(compound, component, nal.memory));
        if (content == null)
            return null;

        if (isQ) {

            if (nonCyclic!=null) {
                nal.deriveDouble(nonCyclic.term(content)
                                .truth(null)
                                .budget(BudgetFunctions.compoundBackward(content, nal))
                );
            }
            //nal.deriveDouble(content, null /*truth*/, budget, false, false);

            // special logic to answer conjunctive questions with query variables
            if (task.getTerm().hasVarQuery()) {

                Concept contentConcept = nal.memory.concept(content);
                if (contentConcept == null) {
                    return null;
                }

                BeliefTable table = task.isQuestion() ? contentConcept.getBeliefs() : contentConcept.getGoals();

                final long now = nal.time();

                Task contentTask = table.top(task, now);
                if (contentTask == null) {
                    return null;
                }

                TaskSeed qd = nal.newDoublePremise(contentTask, belief);
                if (qd != null) {

                    Compound conj = Sentence.termOrNull(Conjunction.make(component, content));

                    if (conj != null) {
                        Truth truth;

                        nal.deriveDouble(qd.term(conj)
                            .punctuation(contentTask.getPunctuation())
                            .truth(truth = intersection(contentTask.getTruth(), belief.getTruth()))
                            .budget(BudgetFunctions.compoundForward(truth, conj, nal)),
                            false
                        );

                        //nal.deriveDouble(conj, contentTask.getPunctuation(), truth, budget, contentTask, belief, false, false);
                    }
                }

            }
        } else {
            Truth v1, v2;
            if (compoundTask) {
                v1 = task.getTruth();
                v2 = belief.getTruth();
            } else {
                v1 = belief.getTruth();
                v2 = task.getTruth();
            }

            Truth truth;
            if (compound instanceof Conjunction) {
                if (task.isGoal()) {
                    if (compoundTask) {
                        truth = intersection(v1, v2);
                    } else {
                        return null;
                    }
                } else { // isJudgment
                    truth = reduceConjunction(v1, v2);
                }
            } else if (compound instanceof Disjunction) {
                if (task.isGoal()) {
                    if (compoundTask) {
                        truth = reduceConjunction(v2, v1);
                    } else {
                        return null;
                    }
                } else {  // isJudgment
                    truth = reduceDisjunction(v1, v2);

                }
            } else {
                return null;
            }


            if (truth!=null) {
                return nal.deriveDouble(nonCyclic.term(content)
                                .truth(truth)
                                .budget(BudgetFunctions.compoundForward(truth, content, nal))
                );
            }

        }

        return null;


    }



    /**
     * Introduce a dependent variable in an outer-layer conjunction {<S --> P1>,
     * <S --> P2>} |- (&&, <#x --> P1>, <#x --> P2>)
     *
     * @param taskContent   The first premise <M --> S>
     * @param beliefContent The second premise <M --> P>
     * @param index         The location of the shared term: 0 for subject, 1 for
     *                      predicate
     * @param p           Reference to the memory
     */
    public static void introVarOuter(final Statement taskContent, final Statement beliefContent, final int index, final Premise p) {

        if (!(taskContent instanceof Inheritance)) {
            return;
        }

        if (Stamp.overlapping( p.getTask(), p.getBelief() ))
            return;

        Truth truthT = p.getTask().getTruth();
        Truth truthB = p.getBelief().getTruth();
        if ((truthT == null) || (truthB == null)) {
            return;
        }

        Term term11dependent=null, term12dependent=null, term21dependent=null, term22dependent=null;
        Term term11, term12, term21, term22, commonTerm = null;
        Map<Term, Term> subs = Global.newHashMap();

        //TODO convert the following two large if statement blocks into a unified function because they are nearly identical
        if (index == 0) {
            term11 = varInd1;
            term21 = varInd1;
            term12 = taskContent.getPredicate();
            term22 = beliefContent.getPredicate();

            term12dependent=term12;
            term22dependent=term22;

            if (term12 instanceof ImageExt) {

                if ((/*(ImageExt)*/term12).containsTermRecursivelyOrEquals((term22))) {
                    commonTerm = term22;
                }


                if(commonTerm == null /*&& term12 instanceof ImageExt*/) {
                    commonTerm = ((ImageExt) term12).getTheOtherComponent();
                    if ((commonTerm == null) || (!(term22.containsTermRecursivelyOrEquals(commonTerm)))) {
                        commonTerm=null;
                    }
                    if (term22 instanceof ImageExt && ((commonTerm == null) || !(term22).containsTermRecursivelyOrEquals(commonTerm))) {
                        commonTerm = ((ImageExt) term22).getTheOtherComponent();
                        if ((commonTerm == null) || !term12.containsTermRecursivelyOrEquals(commonTerm)) {
                            commonTerm = null;
                        }
                    }
                }

                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term12 = ((Compound) term12).applySubstitute(subs);
                    if (!(term22 instanceof Compound)) {
                        term22 = varInd2;
                    } else {
                        term22 = ((Compound) term22).applySubstitute(subs);
                    }
                }
            }

            if (commonTerm==null && term22 instanceof ImageExt) {

                if ((/*(ImageExt)*/term22).containsTermRecursivelyOrEquals(term12)) {
                    commonTerm = term12;
                }

                if(commonTerm == null /*&& term22 instanceof ImageExt*/) {
                    commonTerm = ((ImageExt) term22).getTheOtherComponent();
                    if (commonTerm == null || (!(term12.containsTermRecursivelyOrEquals(commonTerm)))) {
                        commonTerm=null;
                    }
                    if (term12 instanceof ImageExt && ((commonTerm == null) || !(term12).containsTermRecursivelyOrEquals(commonTerm))) {
                        commonTerm = ((ImageExt) term12).getTheOtherComponent();
                        if ((commonTerm == null) || !(term22).containsTermRecursivelyOrEquals(commonTerm)) {
                            commonTerm = null;
                        }
                    }
                }

                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);

                    Substitution ss = new Substitution(subs);
                    term22 = ((Compound) term22).applySubstitute(ss);
                    if(!(term12 instanceof Compound)) {
                        term12 = varInd2;
                    } else {
                        term12 = ((Compound) term12).applySubstitute(ss);
                    }
                }
            }

        } else {
            term11 = taskContent.getSubject();
            term21 = beliefContent.getSubject();
            term12 = varInd1;
            term22 = varInd1;

            term11dependent=term11;
            term21dependent=term21;

            if (term21 instanceof ImageInt) {

                if ((/*(ImageInt)*/term21).containsTermRecursivelyOrEquals((term11))) {
                    commonTerm = term11;
                }

                if(term11 instanceof ImageInt && commonTerm == null /*&& term21 instanceof ImageInt*/) {
                    commonTerm = ((ImageInt) term11).getTheOtherComponent();
                    if ((commonTerm == null) || (!(term21.containsTermRecursivelyOrEquals(commonTerm)))) {
                        commonTerm=null;
                    }
                    if ((commonTerm == null) || !(term21).containsTermRecursivelyOrEquals(commonTerm)) {
                        commonTerm = ((ImageInt) term21).getTheOtherComponent();
                        if ((commonTerm == null) || !(term11).containsTermRecursivelyOrEquals(commonTerm)) {
                            commonTerm = null;
                        }
                    }
                }


                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);

                    Substitution ss = new Substitution(subs);

                    term21 = ((Compound) term21).applySubstitute(ss);
                    if (!(term11 instanceof Compound)) {
                        term11 = varInd2;
                    } else {
                        term11 = ((Compound) term11).applySubstitute(ss);
                    }
                }
            }

            if (commonTerm==null && term11 instanceof ImageInt) {

                if ((/*(ImageInt)*/term11).containsTermRecursivelyOrEquals(term21)) {
                    commonTerm = term21;
                }

                if(term21 instanceof ImageInt && commonTerm == null /*&& term11 instanceof ImageInt -- already checked */) {
                    commonTerm = ((ImageInt) term21).getTheOtherComponent();
                    if ((commonTerm == null) || (!(term11.containsTermRecursivelyOrEquals(commonTerm)))) {
                        commonTerm=null;
                    }
                    if ((commonTerm == null) || !(term11).containsTermRecursivelyOrEquals(commonTerm)) {
                        commonTerm = ((ImageInt) term11).getTheOtherComponent();
                        if ((commonTerm == null) || !(term21).containsTermRecursivelyOrEquals(commonTerm)) {
                            commonTerm = null;
                        }
                    }
                }

                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);

                    Substitution ss = new Substitution(subs);
                    term11 = ((Compound) term11).applySubstitute(ss);
                    if(!(term21 instanceof Compound)) {
                        term21 = varInd2;
                    } else {
                        term21 = ((Compound) term21).applySubstitute(ss);
                    }
                }
            }
        }


        Statement state1 = Inheritance.make(term11, term12);
        Statement state2 = Inheritance.make(term21, term22);
        Compound content = compoundOrNull(Implication.makeTerm(state1, state2)).normalized();
        if (content == null) {
            return;
        }


        char punc = (p.getTask().getPunctuation());

        {
            Truth truth = induction(truthT, truthB);
            if (truth!=null) {
                p.deriveDouble(p.newTask(content, punc)
                        .parent(p.getTask(), p.getBelief())
                        .truth(truth)
                        .budget(BudgetFunctions.compoundForward(truth, content, p)));
                //nal.deriveDouble(content, truth, budget, false, false);
            }
        }

        {
            Compound ct = compoundOrNull(Implication.makeTerm(state2, state1));
            if (ct != null) {
                Truth truth = induction(truthB, truthT);
                if (truth!=null) {
                    p.deriveDouble(p.newTask(ct, punc)
                                    .parent(p.getTask(), p.getBelief())
                                    .truth(truth)
                                    .budget(BudgetFunctions.compoundForward(truth, ct, p))
                    );
                }
                //truth = induction(truthB, truthT);
                //budget = BudgetFunctions.compoundForward(truth, ct, nal);
                //nal.deriveDouble(ct, truth, budget, false, false);
            }
        }

        {
            Compound ct = compoundOrNull(Equivalence.makeTerm(state1, state2));
            if (ct != null) {
                Truth truth;
                p.deriveDouble(p.newTask(ct, punc)
                                .parent(p.getTask(), p.getBelief())
                                .truth(truth = comparison(truthT, truthB))
                                .budget(BudgetFunctions.compoundForward(truth, ct, p))
                );
//                truth = comparison(truthT, truthB);
//                budget = BudgetFunctions.compoundForward(truth, ct, nal);
//                nal.deriveDouble(ct, truth, budget, false, false);
            }
        }

        if (index == 0) {
            state1 = Inheritance.make(varDep, term12dependent);
            state2 = Inheritance.make(varDep, term22dependent);
        } else {
            state1 = Inheritance.make(term11dependent, varDep);
            state2 = Inheritance.make(term21dependent, varDep);
        }

        if ((state1 == null) || (state2 == null))
            return;

        Compound ct = compoundOrNull(Conjunction.make(state1, state2));
        if (ct != null) {
            Truth truth;
            p.deriveDouble(p.newTask(ct, punc)
                            .parent(p.getTask(), p.getBelief())
                            .truth(truth = intersection(truthT, truthB))
                            .budget(BudgetFunctions.compoundForward(truth, ct, p))
            );
//            truth = intersection(truthT, truthB);
//            budget = BudgetFunctions.compoundForward(truth, ct, nal);
//            nal.deriveDouble(ct, truth, budget, false, false);
        }
    }

    /**
     * {<M --> S>, <C ==> <M --> P>>} |- <(&&, <#x --> S>, C) ==> <#x --> P>>
     * {<M --> S>, (&&, C, <M --> P>)} |- (&&, C, <<#x --> S> ==> <#x --> P>>)
     *
     * @param taskContent   The first premise directly used in internal induction,
     *                      <M --> S>
     * @param beliefContent The componentCommon to be used as a premise in
     *                      internal induction, <M --> P>
     * @param oldCompound   The whole contentInd of the first premise, Implication
     *                      or Conjunction
     * @param p           Reference to the memory
     *
     */
    static Task introVarInner(Statement premise1, Statement premise2, Compound oldCompound, Premise p) {
        final Task task = p.getTask();


        if (!task.isJudgment() || (!(premise1.operator() == premise2.operator())) || oldCompound.containsTerm(premise1)) {
            return null;
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
            return null;
        }

        final Task belief = p.getBelief();
        Map<Term, Term> substitute = Global.newHashMap();


        Task b = null;



        {


            Term content = compoundOrNull(Conjunction.make(premise1, oldCompound));
            if (content != null) {

                substitute.put(commonTerm1, varDep2);

                Compound ct = compoundOrNull(((Compound) content).applySubstitute(substitute));
                if (ct != null) {
                    Truth truth = intersection(task.getTruth(), belief.getTruth());
                    Budget budget = BudgetFunctions.forward(truth, p);

                    b = p.deriveDouble(ct, task.getPunctuation(), truth, budget, task, belief, false, false);
                }
            }
        }


        {

            substitute.clear();

            substitute.put(commonTerm1, varInd1);

            if (commonTerm2 != null) {
                substitute.put(commonTerm2, varInd2);
            }


            Compound content = compoundOrNull(Implication.makeTerm(premise1, oldCompound));
            if (content != null) {

                Compound ct = compoundOrNull(content.applySubstituteToCompound(substitute));

                Truth truth;

                if (premise1.equals(task.getTerm())) {
                    truth = induction(belief.getTruth(), task.getTruth());
                } else {
                    truth = induction(task.getTruth(), belief.getTruth());
                }

                if (truth != null) {
                    Budget budget = BudgetFunctions.forward(truth, p);
                    Task c = p.deriveDouble(ct, truth, budget, false, false);
                }

            }
        }

        return b;
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
        /*if (index == 0)*/ {
            if ((term1 instanceof ImageExt) && (term2 instanceof ImageExt)) {
                commonTerm = ((ImageExt) term1).getTheOtherComponent();
                if ((commonTerm == null) || !term2.containsTermRecursivelyOrEquals(commonTerm)) {
                    commonTerm = ((ImageExt) term2).getTheOtherComponent();
                    if ((commonTerm == null) || !term1.containsTermRecursivelyOrEquals(commonTerm)) {
                        commonTerm = null;
                    }
                }
            }
        }
        /*else*/ if ((term1 instanceof ImageInt) && (term2 instanceof ImageInt)) {
            //System.err.println("secondCommonTerm: this condition was never possible, and may not be.  but allowing it in case it does..");
            commonTerm = ((ImageInt) term1).getTheOtherComponent();
            if ((commonTerm == null) || !term2.containsTermRecursivelyOrEquals(commonTerm)) {
                commonTerm = ((ImageInt) term2).getTheOtherComponent();
                if ((commonTerm == null) || !term1.containsTermRecursivelyOrEquals(commonTerm)) {
                    commonTerm = null;
                }
            }
        }
        return commonTerm;
    }

    /*
    The other inversion (abduction) should also be studied:
 IN: <<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>.
 IN: <(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>.
OUT: <lock1 --> lock>.
    http://code.google.com/p/open-nars/issues/detail?id=40&can=1
    */
    public static void eliminateVariableOfConditionAbductive(final int figure, final Task<Statement> sentence, final Sentence<Statement> belief, final NAL nal) {
        final Random m = nal.memory.random;

        if (belief.isQuestOrQuestion() || sentence.isQuestOrQuestion())
            return;

        Statement T1 = sentence.getTerm();
        Statement T2 = belief.getTerm();
        final Truth stu = sentence.getTruth();

        Term S1 = T2.getSubject();
        Term S2 = T1.getSubject();

        Term P1 = T2.getPredicate();
        Term P2 = T1.getPredicate();

        Map<Term, Term> res1 = Global.newHashMap();
        Map<Term, Term> res2 = Global.newHashMap();



        if (figure == 21) {
            res1.clear();
            res2.clear();

            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, P1, S2, res1, res2, m); //this part is

            T1 = (Statement) T1.applySubstitute(res2); //independent, the rule works if it unifies
            if (T1 == null)
                return;


            T2 = (Statement) T2.applySubstitute(res1);
            if (T2 == null)
                return;


            S1 = T2.getSubject();
            S2 = T1.getSubject();
            P1 = T2.getPredicate();
            P2 = T1.getPredicate();

            if (S1 instanceof Conjunction) {
                assymUnifyComponents(belief, m, stu, (Compound) S1, P2, nal);
            }

            if (P2 instanceof Conjunction) {
                assymUnifyComponents(belief, m, stu, (Compound) P2, S1, nal);
            }
        }

        if (figure == 12) {

            res1.clear();
            res2.clear();

            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, S1, P2, res1, res2, m); //this part is

            T1 = (Statement) T1.applySubstitute(res2); //independent, the rule works if it unifies
            if (T1 == null) {
                return;
            }

            T2 = (Statement) T2.applySubstitute(res1);
            if (T2 == null) {
                return;
            }

            S1 = T2.getSubject();
            S2 = T1.getSubject();
            P1 = T2.getPredicate();
            P2 = T1.getPredicate();

            if (S2 instanceof Conjunction) {
                assymUnifyComponents(belief, m, stu, (Compound) S2, P1, nal);
            }

            if (P1 instanceof Conjunction) {
                assymUnifyComponents(belief, m, stu, (Compound) P1, S2, nal);
            }
        }

        if (figure == 11) {
            res1.clear();
            res2.clear();

            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, S1, S2, res1, res2, m); //this part is

            T1 = (Statement) T1.applySubstitute(res2); //independent, the rule works if it unifies
            if (T1 == null)
                return;

            T2 = (Statement) T2.applySubstitute(res1);
            if (T2 == null)
                return;


            P1 = T2.getPredicate();
            P2 = T1.getPredicate();

            if (P1 instanceof Conjunction) {
                assymUnifyComponents(belief, m, stu, (Compound) P1, P2, nal);
            }

            if (P2 instanceof Conjunction) {
                assymUnifyComponents(belief, m, stu, (Compound) P2, P1, nal);
            }
        }

        if (figure == 22) {
            res1.clear();
            res2.clear();

            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, P1, P2, res1, res2, m); //this part is

            T1 = (Statement) T1.applySubstitute(res2); //independent, the rule works if it unifies
            if (T1 == null)
                return;

            T2 = (Statement) T2.applySubstitute(res1);
            if (T2 == null)
                return;

            S1 = T2.getSubject();
            S2 = T1.getSubject();

            if (S1 instanceof Conjunction) {
                assymUnifyComponents(belief, m, stu, (Compound) S1, S2, nal);
            }

            if (S2 instanceof Conjunction) {
                assymUnifyComponents(belief, m, stu, (Compound) S2, S1, nal);
            }
        }
    }

    /** try to unify S1 with a component */
    protected static void assymUnifyComponents(Sentence<Statement> belief, Random m, Truth t, Compound a, Term b, Premise p) {
        final Map<Term, Term> r1 = Global.newHashMap();
        final Map<Term, Term> r2 = Global.newHashMap();

        //try to unify P1 with a component
        final Term[] aComponents = a.term;

        for (final Term a1 : aComponents) {

            //here the dependent part matters, see example of Issue40
            if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, a1, b, r1, r2, m)) {
                for (Term a2 : aComponents) {

                    if (a2 == a1) continue; //this seems safe

                    assymAssymSubst(belief, p, t, r1, a1, a2);
                }
            }

            r1.clear();
            r2.clear();
        }
    }

    /** truth should not be null */
    protected static void assymAssymSubst(Sentence<Statement> belief, Premise p, final Truth truth, Map<Term, Term> res3, Term s1, Term s2) {
        if (!(s2 instanceof Compound)) {
            return;
        }

        s2 = ((Compound) s2).applySubstitute(res3);
        if (s2 == null || s2.hasVarIndep() || !(s2 instanceof Compound)) {
            return;
        }
        if (!s2.equals(s1)) {
            assymAssymDerive(belief, p, truth, s2);
        }
    }

    protected static void assymAssymDerive(Sentence<Statement> belief, Premise p, Truth stu, Term s2) {
        Truth truth = abduction(stu, belief.getTruth());
        if (truth!=null) {
            Budget budget = BudgetFunctions.compoundForward(truth, s2, p);
            p.deriveDouble((Compound) s2, truth, budget, false, false);
        }
        else {
            //this should not occurr, but if it does we will know we can prune this path
            if (Global.DEBUG)
                throw new RuntimeException("this case which invalidates the entire process could have been detected earlier by checking for analytic truth values of belief.truth and truth");
        }
    }

    static Task introVarSameSubjectOrPredicate(final Task originalMainSentenceTask, final Task subSentence, final Term component, final Term content, final int index, final Premise p) {

        if (!(content instanceof Compound)) {
            return null;
        }

        Compound T = originalMainSentenceTask.getTerm();
        Compound T2 = (Compound) content;


        if ((component instanceof Inheritance && content instanceof Inheritance)
                || (component instanceof Similarity && content instanceof Similarity)) {
            //CompoundTerm result = T;
            if (component.equals(content)) {
                return null; //wouldnt make sense to create a conjunction here, would contain a statement twice
            }


            if (((Statement) component).getPredicate().equals(((Statement) content).getPredicate()) && !(((Statement) component).getPredicate() instanceof Variable)) {

                Compound zw = (Compound) T.term[index];

                zw = (Compound) zw.cloneReplacingSubterm(1, depIndVar1);
                if (zw == null) return null;

                T2 = (Compound) T2.cloneReplacingSubterm(1, depIndVar1);
                if (T2 == null) return null;

                Conjunction res = (Conjunction) Conjunction.make(zw, T2);

                T = (Compound) T.cloneReplacingSubterm(index, res);

            } else if (((Statement) component).getSubject().equals(((Statement) content).getSubject()) && !(((Statement) component).getSubject() instanceof Variable)) {

                Compound zw = (Compound) T.term[index];

                zw = (Compound) zw.cloneReplacingSubterm(0, depIndVar2);
                if (zw == null) return null;

                T2 = (Compound) T2.cloneReplacingSubterm(0, depIndVar2);
                if (T2 == null) return null;

                Conjunction res = (Conjunction) Conjunction.make(zw, T2);
                T = (Compound) T.cloneReplacingSubterm(index, res);
            }
            Truth truth = induction(originalMainSentenceTask.getTruth(), subSentence.getTruth());
            if (truth!=null) {
                Budget budget = BudgetFunctions.compoundForward(truth, T, p);
                return p.deriveDouble(T, originalMainSentenceTask.getPunctuation(),
                        truth, budget, originalMainSentenceTask, subSentence, false, false);
            }

        }
        return null;
    }



}
