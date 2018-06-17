/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.inference;

import org.opennars.control.DerivationContext;
import org.opennars.entity.*;
import org.opennars.io.Symbols;
import org.opennars.language.*;
import org.opennars.main.MiscFlags;

import java.util.HashMap;
import java.util.Map;

import static org.opennars.inference.TruthFunctions.*;
import static org.opennars.language.Terms.reduceComponents;

/**
 * Compound term composition and decomposition rules, with two premises.
 * <p>
 * New compound terms are introduced only in forward inference, while
 * decompositional rules are also used in backward inference
 */
public final class CompositionalRules {

    /* -------------------- intersections and differences -------------------- */
    /**
     * {<S ==> M>, <P ==> M>} |- {<(S|P) ==> M>, <(S&P) ==> M>, <(S-P) ==>
     * M>,
     * <(P-S) ==> M>}
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
     * {<(S|P) ==> M>, <P ==> M>} |- <S ==> M>
     *
     * @param term1 The other term in the contentInd
     * @param index The location of the shared term: 0 for subject, 1 for
     * predicate
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
     * {(||, S, P), P} |- S {(&&, S, P), P} |- S
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
     * Introduce a dependent variable in an outer-layer conjunction {<S --> P1>,
     * <S --> P2>} |- (&&, <#x --> P1>, <#x --> P2>)
     *
     * @param taskContent The first premise <M --> S>
     * @param beliefContent The second premise <M --> P>
     * @param index The location of the shared term: 0 for subject, 1 for
     * predicate
     * @param nal Reference to the memory
     */
    public static void introVarOuter(final Statement taskContent, final Statement beliefContent, final int index, final DerivationContext nal) {

        if (!(taskContent instanceof Inheritance)) {
            return;
        }

        final Variable varInd1 = new Variable("$varInd1");
        final Variable varInd2 = new Variable("$varInd2");
        
        Term term11dependent=null, term12dependent=null, term21dependent=null, term22dependent=null;

        Term term11 = index == 0 ? varInd1 : taskContent.getSubject();
        Term term21 = index == 0 ? varInd1 : beliefContent.getSubject();
        Term term12 = index == 0 ? taskContent.getPredicate() : varInd1;
        Term term22 = index == 0 ? beliefContent.getPredicate() : varInd1;

        if (index == 0) {
            term12dependent=term12;
            term22dependent=term22;
        } else {
            term11dependent=term11;
            term21dependent=term21;
        }

        Term commonTerm = null;
        final Map<Term, Term> subs = new HashMap<>();

        // comment to firstIsImage and secondIsSameImage:
        // Because if we have <{a} --> P> and <{a} --> C>
        // then we want to introduce (&&,<{#1} -- P>,<{#1} --> C>).
        // so we have to indeed check that
        // they are equal set types (not seeing [a] and {a} as same)

        // TODO< findCommonTermPredicate and findCommonSubject are actually symmetric to each other -> merge them with a enum >

        if (index == 0) {
            if (term12 instanceof ImageExt) {
                boolean firstIsImage = term22 instanceof ImageExt;
                boolean secondIsSameImage = true;

                commonTerm = findCommonTermPredicate(term12, term22, commonTerm, firstIsImage, secondIsSameImage);

                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term12 = ((CompoundTerm) term12).applySubstitute(subs);
                    term22 = applySubstituteIfCompoundTerm(varInd2, term22, subs);
                }
            }
            if (commonTerm==null && term22 instanceof ImageExt) {
                boolean firstIsImage = term12 instanceof ImageExt;
                boolean secondIsSameImage = true;

                commonTerm = findCommonTermPredicate(term22, term12, commonTerm, firstIsImage, secondIsSameImage);
                
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term22 = ((CompoundTerm) term22).applySubstitute(subs);
                    term12 = applySubstituteIfCompoundTerm(varInd2, term12, subs);
                }
            }
        } else {
            if (term21 instanceof ImageInt) {
                boolean firstIsImage = true;
                boolean secondIsSameImage = term11 instanceof ImageInt;

                commonTerm = findCommonSubject(term11, term21, commonTerm, firstIsImage, secondIsSameImage);
                
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term21 = ((CompoundTerm) term21).applySubstitute(subs);
                    term11 = applySubstituteIfCompoundTerm(varInd2, term11, subs);
                }
            }
            if (commonTerm==null && term11 instanceof ImageInt) {
                boolean firstIsImage = true;
                boolean secondIsSameImage = term21 instanceof ImageInt;

                commonTerm = findCommonSubject(term21, term11, commonTerm, firstIsImage, secondIsSameImage);
                
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term11 = ((CompoundTerm) term11).applySubstitute(subs);
                    term21 = applySubstituteIfCompoundTerm(varInd2, term21, subs);
                }
            }
        }

        Statement state1 = Inheritance.make(term11, term12);
        Statement state2 = Inheritance.make(term21, term22);
        Term content = Implication.make(state1, state2);
        if (content == null) {
            return;
        }

        final TruthValue truthT = nal.getCurrentTask().sentence.truth;
        final TruthValue truthB = nal.getCurrentBelief().truth;
        if ((truthT == null) || (truthB == null)) {
            if(MiscFlags.DEBUG) {
                System.out.println("ERROR: Belief with null truth value. (introVarOuter)");
            }
            return;
        }

        TruthValue truth = induction(truthT, truthB, nal.narParameters);
        BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.doublePremiseTask(content, truth, budget, false, false);
        content = Implication.make(state2, state1);
        truth = induction(truthB, truthT, nal.narParameters);
        budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.doublePremiseTask(content, truth, budget, false, false);
        content = Equivalence.make(state1, state2);
        truth = comparison(truthT, truthB, nal.narParameters);
        budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.doublePremiseTask(content, truth, budget, false, false);

        final Variable varDep = new Variable("#varDep");
        if (index == 0) {
            state1 = Inheritance.make(varDep, term12dependent);
            state2 = Inheritance.make(varDep, term22dependent);
        } else {
            state1 = Inheritance.make(term11dependent, varDep);
            state2 = Inheritance.make(term21dependent, varDep);
        }
        
        if ((state1==null) || (state2 == null)) {
            return;
        }
        if(state1.cloneDeep().equals(state2.cloneDeep())) {
            return;
        }
        content = Conjunction.make(state1, state2);
        truth = intersection(truthT, truthB, nal.narParameters);
        budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.doublePremiseTask(content, truth, budget, false, false);
    }

    private static Term applySubstituteIfCompoundTerm(final Variable varInd2, final Term term22, Map<Term, Term> subs) {
        return term22 instanceof CompoundTerm ? ((CompoundTerm)term22).applySubstitute(subs) : varInd2;
    }

    private static Term findCommonSubject(final Term containedTest, Term tested, final Term commonTerm, final boolean firstIsImage, final boolean secondIsSameImage) {
        Term resultCommonTerm = commonTerm;

        if (tested.containsTermRecursively(containedTest)) {
            resultCommonTerm = containedTest;
        }

        if(secondIsSameImage && resultCommonTerm == null) {
            resultCommonTerm = retCommonTerm(containedTest, tested, firstIsImage);
        }
        return resultCommonTerm;
    }

    private static Term findCommonTermPredicate(Term tested, Term containedTest, Term commonTerm, boolean firstIsImage, boolean secondIsSameImage) {
        Term resultCommonTerm = commonTerm;

        if (tested.containsTermRecursively(containedTest)) {
            resultCommonTerm = containedTest;
        }

        if(secondIsSameImage && resultCommonTerm == null) {
            resultCommonTerm = retCommonTerm(tested, containedTest, firstIsImage);
        }
        return resultCommonTerm;
    }

    private static Term retCommonTerm(Term term12, Term term22, boolean firstIsImage) {
        Term commonTerm;
        commonTerm = ((Image) term12).getTheOtherComponent();
        if(!(term22.containsTermRecursively(commonTerm))) {
            commonTerm=null;
        }
        if (firstIsImage && ((commonTerm == null) || !(term22).containsTermRecursively(commonTerm))) {
            commonTerm = ((Image) term22).getTheOtherComponent();
            if ((commonTerm == null) || !(term12).containsTermRecursively(commonTerm)) {
                commonTerm = null;
            }
        }
        return commonTerm;
    }

    /**
     * {<M --> S>, <C ==> <M --> P>>} |- <(&&, <#x --> S>, C) ==> <#x --> P>>
     * {<M --> S>, (&&, C, <M --> P>)} |- (&&, C, <<#x --> S> ==> <#x --> P>>)
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
        
        final Term subject1 = premise1.getSubject();
        final Term subject2 = premise2.getSubject();
        final Term predicate1 = premise1.getPredicate();
        final Term predicate2 = premise2.getPredicate();
        final Term commonTerm1;
        final Term commonTerm2;
        if (subject1.equals(subject2)) {
            commonTerm1 = subject1;
            commonTerm2 = secondCommonTerm(predicate1, predicate2, 0);
        } else if (predicate1.equals(predicate2)) {
            commonTerm1 = predicate1;
            commonTerm2 = secondCommonTerm(subject1, subject2, 0);
        } else {
            return false;
        }
        
        final Sentence belief = nal.getCurrentBelief();
        final Map<Term, Term> substitute = new HashMap<>();
        
        boolean b1 = false, b2 = false;
        
        {
            final Variable varDep2 = new Variable("#varDep2");


            Term content = Conjunction.make(premise1, oldCompound);

            if (!(content instanceof CompoundTerm))
                return false;           

            substitute.put(commonTerm1, varDep2);

            content = ((CompoundTerm)content).applySubstitute(substitute);

            final TruthValue truth = intersection(taskSentence.truth, belief.truth, nal.narParameters);
            final BudgetValue budget = BudgetFunctions.forward(truth, nal);

            b1 = (nal.doublePremiseTask(content, truth, budget, false, false))!=null;
        }

        substitute.clear();

        {
            final Variable varInd1 = new Variable("$varInd1");
            final Variable varInd2 = new Variable("$varInd2");

            substitute.put(commonTerm1, varInd1);

            if (commonTerm2 != null) {
                substitute.put(commonTerm2, varInd2);
            }


            Term content = Implication.make(premise1, oldCompound);

            if ((content == null) || (!(content instanceof CompoundTerm))) {
                return false;
            }

            content = ((CompoundTerm)content).applySubstituteToCompound(substitute);

            final TruthValue truth;
            
            if (premise1.equals(taskSentence.term)) {
                truth = induction(belief.truth, taskSentence.truth, nal.narParameters);
            } else {
                truth = induction(taskSentence.truth, belief.truth, nal.narParameters);
            }

            final BudgetValue budget = BudgetFunctions.forward(truth, nal);

            b2 = nal.doublePremiseTask(content, truth, budget, false, false)!=null;
        }
        
        return b1 || b2;
    }

    /**
     * Introduce a second independent variable into two terms with a common
     * component
     *
     * @param term1 The first term
     * @param term2 The second term
     * @param index The index of the terms in their statement
     */
    private static Term secondCommonTerm(final Term term1, final Term term2, final int index) {
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
        } else if ((term1 instanceof ImageInt) && (term2 instanceof ImageInt)) {
            commonTerm = ((ImageInt) term1).getTheOtherComponent();
            if ((commonTerm == null) || !term2.containsTermRecursively(commonTerm)) {
                commonTerm = ((ImageInt) term2).getTheOtherComponent();
                if ((commonTerm == null) || !term1.containsTermRecursively(commonTerm)) {
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
    public static void eliminateVariableOfConditionAbductive(final int figure, final Sentence sentence, final Sentence belief, final DerivationContext nal) {
        Statement T1 = (Statement) sentence.term;
        Statement T2 = (Statement) belief.term;

        Term S1 = T2.getSubject();
        Term S2 = T1.getSubject();
        Term P1 = T2.getPredicate();
        Term P2 = T1.getPredicate();

        final Map<Term, Term> res1 = new HashMap<>();
        final Map<Term, Term> res2 = new HashMap<>();
        final Map<Term, Term> res3 = new HashMap<>();
        final Map<Term, Term> res4 = new HashMap<>();

        if (figure == 21) {

            res1.clear();
            res2.clear();
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, P1, S2, res1, res2); //this part is 
            T1 = (Statement) T1.applySubstitute(res2); //independent, the rule works if it unifies
            if(T1==null) {
                return;
            }
            T2 = (Statement) T2.applySubstitute(res1);
            if(T2==null) {
                return;
            }

            //update the variables because T1 and T2 may have changed
            S1 = T2.getSubject();
            P2 = T1.getPredicate();

            eliminateVariableOfConditionAbductiveTryCrossUnification(sentence, belief, nal, S1, P2, res3, res4);
        }
        else if (figure == 12) {

            res1.clear();
            res2.clear();
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, S1, P2, res1, res2); //this part is 
            T1 = (Statement) T1.applySubstitute(res2); //independent, the rule works if it unifies
            if(T1==null) {
                return;
            }
            T2 = (Statement) T2.applySubstitute(res1);
            if(T2==null) {
                return;
            }

            //update the variables because T1 and T2 may have changed
            S2 = T1.getSubject();
            P1 = T2.getPredicate();

            eliminateVariableOfConditionAbductiveTryCrossUnification(sentence, belief, nal, S2, P1, res3, res4);
        }
        else if (figure == 11) {

            res1.clear();
            res2.clear();
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, S1, S2, res1, res2); //this part is 
            T1 = (Statement) T1.applySubstitute(res2); //independent, the rule works if it unifies
            if(T1==null) {
                return;
            }
            T2 = (Statement) T2.applySubstitute(res1);
            if(T2==null) {
                return;
            }
            P1 = T2.getPredicate();
            P2 = T1.getPredicate(); //update the variables because T1 and T2 may have changed

            eliminateVariableOfConditionAbductiveTryCrossUnification(sentence, belief, nal, P1, P2, res3, res4);
        }
        else if (figure == 22) {

            res1.clear();
            res2.clear();
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, P1, P2, res1, res2); //this part is 
            T1 = (Statement) T1.applySubstitute(res2); //independent, the rule works if it unifies
            if(T1==null) {
                return;
            }
            T2 = (Statement) T2.applySubstitute(res1);
            if(T2==null) {
                return;
            }

            //update the variables because T1 and T2 may have changed
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
            final Variable depIndVar1 = new Variable("#depIndVar1");
            final Variable depIndVar2 = new Variable("#depIndVar2");

            if (((Statement) component).getPredicate().equals(((Statement) content).getPredicate()) && !(((Statement) component).getPredicate() instanceof Variable)) {

                CompoundTerm zw = (CompoundTerm) T.term[index];
                zw = (CompoundTerm) zw.setComponent(1, depIndVar1, nal.mem());
                T2 = (CompoundTerm) T2.setComponent(1, depIndVar1, nal.mem());
                final Conjunction res = (Conjunction) Conjunction.make(zw, T2);
                T = (CompoundTerm) T.setComponent(index, res, nal.mem());
            } else if (((Statement) component).getSubject().equals(((Statement) content).getSubject()) && !(((Statement) component).getSubject() instanceof Variable)) {

                CompoundTerm zw = (CompoundTerm) T.term[index];
                zw = (CompoundTerm) zw.setComponent(0, depIndVar2, nal.mem());
                T2 = (CompoundTerm) T2.setComponent(0, depIndVar2, nal.mem());
                final Conjunction res = (Conjunction) Conjunction.make(zw, T2);
                T = (CompoundTerm) T.setComponent(index, res, nal.mem());
            }
            final TruthValue truth = induction(originalMainSentence.truth, subSentence.truth, nal.narParameters);
            final BudgetValue budget = BudgetFunctions.compoundForward(truth, T, nal);
            nal.doublePremiseTask(T, truth, budget, false, false);
        }
    }
}
