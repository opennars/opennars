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

import java.util.HashMap;
import org.opennars.main.Parameters;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.control.DerivationContext;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import static org.opennars.inference.TruthFunctions.comparison;
import static org.opennars.inference.TruthFunctions.induction;
import static org.opennars.inference.TruthFunctions.intersection;
import static org.opennars.inference.TruthFunctions.negation;
import static org.opennars.inference.TruthFunctions.reduceConjunction;
import static org.opennars.inference.TruthFunctions.reduceConjunctionNeg;
import static org.opennars.inference.TruthFunctions.reduceDisjunction;
import static org.opennars.inference.TruthFunctions.union;
import org.opennars.io.Symbols;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Conjunction;
import org.opennars.language.DifferenceExt;
import org.opennars.language.DifferenceInt;
import org.opennars.language.Disjunction;
import org.opennars.language.Equivalence;
import org.opennars.language.ImageExt;
import org.opennars.language.ImageInt;
import org.opennars.language.Implication;
import org.opennars.language.Inheritance;
import org.opennars.language.IntersectionExt;
import org.opennars.language.IntersectionInt;
import org.opennars.language.SetExt;
import org.opennars.language.SetInt;
import org.opennars.language.Similarity;
import org.opennars.language.Statement;
import org.opennars.language.Term;
import static org.opennars.language.Terms.reduceComponents;
import org.opennars.language.Variable;
import org.opennars.language.Variables;
import static org.opennars.inference.TruthFunctions.abduction;
import org.opennars.language.Interval;

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
     * @param taskSentence The first premise
     * @param belief The second premise
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
        int order1 = taskContent.getTemporalOrder();
        int order2 = beliefContent.getTemporalOrder();
        int order = TemporalRules.composeOrder(order1, order2);
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
        final TruthValue truthOr = union(truthT, truthB);
        final TruthValue truthAnd = intersection(truthT, truthB);
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
     * @param premise1 Type of the contentInd
     * @param subject Subject of contentInd
     * @param predicate Predicate of contentInd
     * @param truth TruthValue of the contentInd
     * @param memory Reference to the memory
     */
    private static void processComposed(final Statement statement, final Term subject, final Term predicate, final int order, final TruthValue truth, final DerivationContext nal) {
        if ((subject == null) || (predicate == null)) {
            return;
        }
        Term content = Statement.make(statement, subject, predicate, order);
        if ((content == null) || statement == null || content.equals(statement) || content.equals(nal.getCurrentBelief().term)) {
            return;
        }
        BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.doublePremiseTask(content, truth, budget, false, false); //(allow overlap) but not needed here, isn't detachment, this one would be even problematic from control perspective because its composition
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
     * @param nal Reference to the memory
     */
    private static void decomposeCompound(CompoundTerm compound, Term component, Term term1, int index, boolean compoundTask, int order, DerivationContext nal) {

        if ((compound instanceof Statement) || (compound instanceof ImageExt) || (compound instanceof ImageInt)) {
            return;
        }
        Term term2 = reduceComponents(compound, component, nal.mem());
        if (term2 == null) {
            return;
        }
        
        long delta = 0;
        while ((term2 instanceof Conjunction) && (((CompoundTerm) term2).term[0] instanceof Interval)) {
            Interval interval = (Interval) ((CompoundTerm) term2).term[0];
            delta += interval.time;
            term2 = ((CompoundTerm)term2).setComponent(0, null, nal.mem());
        }
        
        Task task = nal.getCurrentTask();
        Sentence sentence = task.sentence;
        Sentence belief = nal.getCurrentBelief();
        Statement oldContent = (Statement) task.getTerm();
        TruthValue v1, v2;
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
            content = Statement.make(oldContent, term1, term2, order);
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
            content = Statement.make(oldContent, term2, term1, order);
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
            BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
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
     * @param implication The implication term to be decomposed
     * @param componentCommon The part of the implication to be removed
     * @param compoundTask Whether the implication comes from the task
     * @param nal Reference to the memory
     */
    static void decomposeStatement(CompoundTerm compound, Term component, boolean compoundTask, int index, DerivationContext nal) {
        boolean isTemporalConjunction = (compound instanceof Conjunction) && !((Conjunction) compound).isSpatial;
        if (isTemporalConjunction && (compound.getTemporalOrder() == TemporalRules.ORDER_FORWARD) && (index != 0)) {
            return;
        }
        long occurrence_time = nal.getCurrentTask().sentence.getOccurenceTime();
        if(isTemporalConjunction && (compound.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
            if(!nal.getCurrentTask().sentence.isEternal() && compound.term[index + 1] instanceof Interval) {
                long shift_occurrence = ((Interval)compound.term[index + 1]).time;
                occurrence_time = nal.getCurrentTask().sentence.getOccurenceTime() + shift_occurrence;
            }
        }

        Task task = nal.getCurrentTask();
        Sentence taskSentence = task.sentence;
        Sentence belief = nal.getCurrentBelief();
        Term content = reduceComponents(compound, component, nal.mem());
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
                Concept contentConcept = nal.mem().concept(content);
                if (contentConcept == null) {
                    return;
                }
                Sentence contentBelief = contentConcept.getBelief(nal, task);
                if (contentBelief == null) {
                    return;
                }
                Task contentTask = new Task(contentBelief, task.budget, false);
                nal.setCurrentTask(contentTask);
                Term conj = Conjunction.make(component, content);
                truth = intersection(contentBelief.truth, belief.truth);
                budget = BudgetFunctions.compoundForward(truth, conj, nal);
                nal.getTheNewStamp().setOccurrenceTime(occurrence_time);
                nal.doublePremiseTask(conj, truth, budget, false, false);
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

        Variable varInd1 = new Variable("$varInd1");
        Variable varInd2 = new Variable("$varInd2");
        
        Term term11dependent=null, term12dependent=null, term21dependent=null, term22dependent=null;
        Term term11, term12, term21, term22, commonTerm = null;
        HashMap<Term, Term> subs = new HashMap<>();
        if (index == 0) {
            term11 = varInd1;
            term21 = varInd1;
            term12 = taskContent.getPredicate();
            term22 = beliefContent.getPredicate();
            term12dependent=term12;
            term22dependent=term22;
            if (term12 instanceof ImageExt) {

                if ((/*(ImageExt)*/term12).containsTermRecursively(term22)) {
                    commonTerm = term22;
                }
                
                if(commonTerm == null && term12 instanceof ImageExt) {
                    commonTerm = ((ImageExt) term12).getTheOtherComponent();
                    if(!(term22.containsTermRecursively(commonTerm))) {
                        commonTerm=null;
                    }
                    if (term22 instanceof ImageExt && ((commonTerm == null) || !(term22).containsTermRecursively(commonTerm))) {
                        commonTerm = ((ImageExt) term22).getTheOtherComponent();
                        if ((commonTerm == null) || !(term12).containsTermRecursively(commonTerm)) {
                            commonTerm = null;
                        }
                    }
                }
                
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term12 = ((CompoundTerm) term12).applySubstitute(subs);
                    if(!(term22 instanceof CompoundTerm)) {
                        term22 = varInd2;
                    } else {
                        term22 = ((CompoundTerm) term22).applySubstitute(subs);
                    }
                }
            }
            if (commonTerm==null && term22 instanceof ImageExt) {
                
                if ((/*(ImageExt)*/term22).containsTermRecursively(term12)) {
                    commonTerm = term12;
                }
                
                if(commonTerm == null && term22 instanceof ImageExt) {
                    commonTerm = ((ImageExt) term22).getTheOtherComponent();
                    if(!(term12.containsTermRecursively(commonTerm))) {
                        commonTerm=null;
                    }
                    if (term12 instanceof ImageExt && ((commonTerm == null) || !(term12).containsTermRecursively(commonTerm))) {
                        commonTerm = ((ImageExt) term12).getTheOtherComponent();
                        if ((commonTerm == null) || !(term22).containsTermRecursively(commonTerm)) {
                            commonTerm = null;
                        }
                    }
                }
                
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term22 = ((CompoundTerm) term22).applySubstitute(subs);
                    if(!(term12 instanceof CompoundTerm)) {
                        term12 = varInd2;
                    } else {
                        term12 = ((CompoundTerm) term12).applySubstitute(subs);
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
                
                if ((/*(ImageInt)*/term21).containsTermRecursively(term11)) {
                    commonTerm = term11;
                }
                
                if(term11 instanceof ImageInt && commonTerm == null && term21 instanceof ImageInt) {
                    commonTerm = ((ImageInt) term11).getTheOtherComponent();
                    if(!(term21.containsTermRecursively(commonTerm))) {
                        commonTerm=null;
                    }
                    if ((commonTerm == null) || !(term21).containsTermRecursively(commonTerm)) {
                        commonTerm = ((ImageInt) term21).getTheOtherComponent();
                        if ((commonTerm == null) || !(term11).containsTermRecursively(commonTerm)) {
                            commonTerm = null;
                        }
                    }
                }
                
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term21 = ((CompoundTerm) term21).applySubstitute(subs);
                    if(!(term11 instanceof CompoundTerm)) {
                        term11 = varInd2;
                    } else {
                        term11 = ((CompoundTerm) term11).applySubstitute(subs);
                    }
                }
            }
            if (commonTerm==null && term11 instanceof ImageInt) {
                
                if ((/*(ImageInt)*/term11).containsTermRecursively(term21)) {
                    commonTerm = term21;
                }
                
                if(term21 instanceof ImageInt && commonTerm == null && term11 instanceof ImageInt) {
                    commonTerm = ((ImageInt) term21).getTheOtherComponent();
                    if(!(term11.containsTermRecursively(commonTerm))) {
                        commonTerm=null;
                    }
                    if ((commonTerm == null) || !(term11).containsTermRecursively(commonTerm)) {
                        commonTerm = ((ImageInt) term11).getTheOtherComponent();
                        if ((commonTerm == null) || !(term21).containsTermRecursively(commonTerm)) {
                            commonTerm = null;
                        }
                    }
                }
                
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    term11 = ((CompoundTerm) term11).applySubstitute(subs);
                    if(!(term21 instanceof CompoundTerm)) {
                        term21 = varInd2;
                    } else {
                        term21 = ((CompoundTerm) term21).applySubstitute(subs);
                    }
                }
            }
        }
        Statement state1 = Inheritance.make(term11, term12);
        Statement state2 = Inheritance.make(term21, term22);
        Term content = Implication.make(state1, state2);
        if (content == null) {
            return;
        }

        TruthValue truthT = nal.getCurrentTask().sentence.truth;
        TruthValue truthB = nal.getCurrentBelief().truth;
        if ((truthT == null) || (truthB == null)) {
            if(Parameters.DEBUG) {
                System.out.println("ERROR: Belief with null truth value. (introVarOuter)");
            }
            return;
        }

        TruthValue truth = induction(truthT, truthB);
        BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.doublePremiseTask(content, truth, budget, false, false);
        content = Implication.make(state2, state1);
        truth = induction(truthB, truthT);
        budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.doublePremiseTask(content, truth, budget, false, false);
        content = Equivalence.make(state1, state2);
        truth = comparison(truthT, truthB);
        budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.doublePremiseTask(content, truth, budget, false, false);

        Variable varDep = new Variable("#varDep");
        if (index == 0) {
            state1 = Inheritance.make(varDep, term12dependent);
            state2 = Inheritance.make(varDep, term22dependent);
        } else {
            state1 = Inheritance.make(term11dependent, varDep);
            state2 = Inheritance.make(term21dependent, varDep);
        }
        
        if ((state1==null) || (state2 == null))
            return;
        
        content = Conjunction.make(state1, state2);
        truth = intersection(truthT, truthB);
        budget = BudgetFunctions.compoundForward(truth, content, nal);
        nal.doublePremiseTask(content, truth, budget, false, false);
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
     * @param nal Reference to the memory
     */
    static boolean introVarInner(Statement premise1, Statement premise2, CompoundTerm oldCompound, DerivationContext nal) {
        Task task = nal.getCurrentTask();
        Sentence taskSentence = task.sentence;
        if (!taskSentence.isJudgment() || (premise1.getClass() != premise2.getClass()) || oldCompound.containsTerm(premise1)) {
            return false;
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
            return false;
        }
        
        Sentence belief = nal.getCurrentBelief();
        HashMap<Term, Term> substitute = new HashMap<>();
        
        boolean b1 = false, b2 = false;
        
        {
            Variable varDep2 = new Variable("#varDep2");


            Term content = Conjunction.make(premise1, oldCompound);

            if (!(content instanceof CompoundTerm))
                return false;           

            substitute.put(commonTerm1, varDep2);

            content = ((CompoundTerm)content).applySubstitute(substitute);

            TruthValue truth = intersection(taskSentence.truth, belief.truth);
            BudgetValue budget = BudgetFunctions.forward(truth, nal);

            b1 = (nal.doublePremiseTask(content, truth, budget, false, false))!=null;
        }

        substitute.clear();

        {
            Variable varInd1 = new Variable("$varInd1");
            Variable varInd2 = new Variable("$varInd2");

            substitute.put(commonTerm1, varInd1);

            if (commonTerm2 != null) {
                substitute.put(commonTerm2, varInd2);
            }


            Term content = Implication.make(premise1, oldCompound);

            if ((content == null) || (!(content instanceof CompoundTerm))) {
                return false;
            }

            content = ((CompoundTerm)content).applySubstituteToCompound(substitute);

            TruthValue truth;
            
            if (premise1.equals(taskSentence.term)) {
                truth = induction(belief.truth, taskSentence.truth);
            } else {
                truth = induction(taskSentence.truth, belief.truth);
            }

            BudgetValue budget = BudgetFunctions.forward(truth, nal);

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

        HashMap<Term, Term> res1 = new HashMap<>();
        HashMap<Term, Term> res2 = new HashMap<>();
        HashMap<Term, Term> res3 = new HashMap<>();
        HashMap<Term, Term> res4 = new HashMap<>();

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
            S1 = T2.getSubject();
            S2 = T1.getSubject();
            P1 = T2.getPredicate();
            P2 = T1.getPredicate(); //update the variables because T1 and T2 may have changed

            if (S1 instanceof Conjunction) {
                //try to unify P2 with a component
                for (final Term s1 : ((CompoundTerm) S1).term) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, P2, res3, res4)) {
                        for (Term s2 : ((CompoundTerm) S1).term) {
                            if (!(s2 instanceof CompoundTerm)) {
                                continue;
                            }
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(s2==null || s2.hasVarIndep()) {
                                continue;
                            }
                            if (!s2.equals(s1) && (sentence.truth != null) && (belief.truth != null)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
                                nal.doublePremiseTask(s2, truth, budget, false, false);
                            }
                        }
                    }
                }
            }
            if (P2 instanceof Conjunction) {
                //try to unify S1 with a component
                for (final Term s1 : ((CompoundTerm) P2).term) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, S1, res3, res4)) {
                        for (Term s2 : ((CompoundTerm) P2).term) {
                            if (!(s2 instanceof CompoundTerm)) {
                                continue;
                            }
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(s2==null || s2.hasVarIndep()) {
                                continue;
                            }
                            if (!s2.equals(s1) && (sentence.truth != null) && (belief.truth != null)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
                                nal.doublePremiseTask(s2, truth, budget, false, false);
                            }
                        }
                    }
                }
            }
        }

        if (figure == 12) {
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
            S1 = T2.getSubject();
            S2 = T1.getSubject();
            P1 = T2.getPredicate();
            P2 = T1.getPredicate(); //update the variables because T1 and T2 may have changed

            if (S2 instanceof Conjunction) {
                //try to unify P1 with a component
                for (final Term s1 : ((CompoundTerm) S2).term) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, P1, res3, res4)) {
                        for (Term s2 : ((CompoundTerm) S2).term) {
                            if (!(s2 instanceof CompoundTerm)) {
                                continue;
                            }
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(s2==null || s2.hasVarIndep()) {
                                continue;
                            }
                            if (!s2.equals(s1) && (sentence.truth != null) && (belief.truth != null)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
                                nal.doublePremiseTask(s2, truth, budget, false, false);
                            }
                        }
                    }
                }
            }
            if (P1 instanceof Conjunction) {
                //try to unify S2 with a component
                for (final Term s1 : ((CompoundTerm) P1).term) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, S2, res3, res4)) {
                        for (Term s2 : ((CompoundTerm) P1).term) {
                            if (!(s2 instanceof CompoundTerm)) {
                                continue;
                            }
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(s2==null || s2.hasVarIndep()) {
                                continue;
                            }
                            if (!s2.equals(s1) && (sentence.truth != null) && (belief.truth != null)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
                                nal.doublePremiseTask(s2, truth, budget, false, false);
                            }
                        }
                    }
                }
            }
        }

        if (figure == 11) {
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
            S1 = T2.getSubject();
            S2 = T1.getSubject();
            P1 = T2.getPredicate();
            P2 = T1.getPredicate(); //update the variables because T1 and T2 may have changed

            if (P1 instanceof Conjunction) {
                //try to unify P2 with a component
                for (final Term s1 : ((CompoundTerm) P1).term) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, P2, res3, res4)) {
                        for (Term s2 : ((CompoundTerm) P1).term) {
                            if (!(s2 instanceof CompoundTerm)) {
                                continue;
                            }
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(s2==null || s2.hasVarIndep()) {
                                continue;
                            }
                            if ((!s2.equals(s1)) && (sentence.truth != null) && (belief.truth != null)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
                                nal.doublePremiseTask(s2, truth, budget, false, false);
                            }
                        }
                    }
                }
            }
            if (P2 instanceof Conjunction) {
                //try to unify P1 with a component
                for (final Term s1 : ((CompoundTerm) P2).term) {
                    res3.clear();
                    res4.clear(); //here the dependent part matters, see example of Issue40
                    if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, s1, P1, res3, res4)) {
                        for (Term s2 : ((CompoundTerm) P2).term) {
                            if (!(s2 instanceof CompoundTerm)) {
                                continue;
                            }
                            s2 = ((CompoundTerm) s2).applySubstitute(res3);
                            if(s2==null || s2.hasVarIndep()) {
                                continue;
                            }
                            if (!s2.equals(s1) && (sentence.truth != null) && (belief.truth != null)) {
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
                                nal.doublePremiseTask(s2, truth, budget, false, false);
                            }
                        }
                    }
                }
            }
        }

        if (figure == 22) {
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
            S1 = T2.getSubject();
            S2 = T1.getSubject();
            P1 = T2.getPredicate();
            P2 = T1.getPredicate(); //update the variables because T1 and T2 may have changed

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
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
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
                                TruthValue truth = abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, nal);
                                nal.doublePremiseTask(s2, truth, budget, false, false);
                            }
                        }
                    }
                }
            }
        }
    }

    static void IntroVarSameSubjectOrPredicate(final Sentence originalMainSentence, final Sentence subSentence, final Term component, final Term content, final int index, final DerivationContext nal) {
        Term T1 = originalMainSentence.term;
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
            Variable depIndVar1 = new Variable("#depIndVar1");
            Variable depIndVar2 = new Variable("#depIndVar2");

            if (((Statement) component).getPredicate().equals(((Statement) content).getPredicate()) && !(((Statement) component).getPredicate() instanceof Variable)) {

                CompoundTerm zw = (CompoundTerm) T.term[index];
                zw = (CompoundTerm) zw.setComponent(1, depIndVar1, nal.mem());
                T2 = (CompoundTerm) T2.setComponent(1, depIndVar1, nal.mem());
                Conjunction res = (Conjunction) Conjunction.make(zw, T2);
                T = (CompoundTerm) T.setComponent(index, res, nal.mem());
            } else if (((Statement) component).getSubject().equals(((Statement) content).getSubject()) && !(((Statement) component).getSubject() instanceof Variable)) {

                CompoundTerm zw = (CompoundTerm) T.term[index];
                zw = (CompoundTerm) zw.setComponent(0, depIndVar2, nal.mem());
                T2 = (CompoundTerm) T2.setComponent(0, depIndVar2, nal.mem());
                Conjunction res = (Conjunction) Conjunction.make(zw, T2);
                T = (CompoundTerm) T.setComponent(index, res, nal.mem());
            }
            TruthValue truth = induction(originalMainSentence.truth, subSentence.truth);
            BudgetValue budget = BudgetFunctions.compoundForward(truth, T, nal);
            nal.doublePremiseTask(T, truth, budget, false, false);
        }
    }
}
