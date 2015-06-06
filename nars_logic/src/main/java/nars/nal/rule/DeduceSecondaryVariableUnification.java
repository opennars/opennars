package nars.nal.rule;

import nars.Events;
import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.*;
import nars.nal.concept.Concept;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Disjunction;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.process.ConceptProcess;
import nars.nal.stamp.Stamp;
import nars.nal.stamp.Stamper;
import nars.nal.task.TaskSeed;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import static nars.nal.Terms.reduceUntilLayer2;
import static nars.nal.Terms.unwrapNegation;
import static nars.nal.TruthFunctions.*;
import static nars.nal.nal7.TemporalRules.ORDER_INVALID;
import static nars.nal.nal7.TemporalRules.ORDER_NONE;

/**
 * Because of the re-use of temporary collections, each thread must have its own
 * instance of this class.
 */
public class DeduceSecondaryVariableUnification extends ConceptFireTaskTerm {

    //TODO decide if f.currentBelief needs to be checked for null like it was originally

    //these are intiailized further into the first cycle below. afterward, they are clear() and re-used for subsequent cycles to avoid reallocation cost
    ArrayList<Term> terms_dependent = null;
    ArrayList<Term> terms_independent = null;
    Map<Term, Term> Values = null;
    /*Map<Term, Term> Values2 = null;
    Map<Term, Term> Values3 = null;
    Map<Term, Term> Values4 = null;*/
    Map<Term, Term> smap = null;

    private static void dedSecondLayerVariableUnificationTerms(final NAL nal, Task task, Sentence second_belief, Stamper s, ArrayList<Term> terms_dependent, Truth truth, Truth t1, Truth t2, boolean strong) {


        final Sentence taskSentence = task.sentence;

        final int tds = terms_dependent.size();
        for (int i = 0; i < tds; i++) {

            final Term result = Sentence.termOrNull(terms_dependent.get(i));
            if (result == null) {
                //changed this from return to continue,
                //to allow processing terms_dependent when it has > 1 items
                continue;
            }

            char mark = Symbols.JUDGMENT;
            if (task.sentence.isGoal()) {
                if (strong) {
                    truth = desireInd(t1, t2);
                } else {
                    truth = desireDed(t1, t2);
                }
                mark = Symbols.GOAL;
            }


            Budget budget = BudgetFunctions.compoundForward(truth, result, nal);


            long occ = taskSentence.getOccurrenceTime();
            if (!second_belief.isEternal()) {
                occ = second_belief.getOccurrenceTime();
            }

            final Stamper sx = new Stamper(
                    s.getEvidentialBase(),
                    nal.time(),
                    occ,
                    nal.memory.duration()
            );

            Sentence newSentence = new Sentence(result, mark, truth, sx);


            Task dummy = new Task(second_belief, budget, task, null);
            Task newTask = new Task(newSentence, budget, dummy, second_belief);

            nal.setCurrentBelief(taskSentence);

            if (nal.deriveTask(nal.newTask(newSentence).parent(task), false, false, dummy, false)!=null) {

                nal.memory.logic.DED_SECOND_LAYER_VARIABLE_UNIFICATION_TERMS.hit();

            }


        }
    }

    /*
    The current NAL-6 misses another way to introduce a second variable by induction:
  IN: <<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>.
  IN: <lock1 --> lock>.
OUT: <(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>.
    http://code.google.com/p/open-nars/issues/detail?id=40&can=1
    */

    private static Map<Term, Term> newVariableSubstitutionMap() {
        //TODO give appropraite size
        return Global.newHashMap();
    }

    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {
        final Task task = taskLink.getTarget();
        final Sentence taskSentence = taskLink.getSentence();

        // to be invoked by the corresponding links
        if (dedSecondLayerVariableUnification(task, f)) {
            //unification ocurred, done reasoning in this cycle if it's judgment
            if (taskSentence.isJudgment())
                return false;
        }
        return true;
    }

    public boolean dedSecondLayerVariableUnification(final Task task, final NAL nal) {

        final Random r = nal.memory.random;

        final Sentence taskSentence = task.sentence;

        if (taskSentence == null || taskSentence.isQuestion() || taskSentence.isQuest()) {
            return false;
        }

        Term firstTerm = taskSentence.term;

        if (!firstTerm.hasVar()) {
            return false;
        }

        //lets just allow conjunctions, implication and equivalence for now
        if (!((firstTerm instanceof Disjunction || firstTerm instanceof Conjunction || firstTerm instanceof Equivalence || firstTerm instanceof Implication))) {
            return false;
        }


        boolean unifiedAnything = false;

        int remainingUnifications = 1;

        for (int k = 0; k < Global.DED_SECOND_UNIFICATION_ATTEMPTS; k++) {
            Concept secondConcept = nal.memory.concepts.nextConcept(new Predicate<Concept>() {

                @Override
                public boolean test(Concept concept) {
                    //prevent unification with itself
                    if (concept.getTerm().equals(firstTerm)) return false;
                    if (!concept.hasBeliefs()) return false;

                    return true;
                }

            }, Global.DED_SECOND_UNIFICATION_DEPTH);

            if (secondConcept == null) {
                //no more concepts, stop
                break;
            }

            Term secTerm = secondConcept.getTerm();

            Sentence second_belief = secondConcept.getStrongestBelief().sentence;
            //getBeliefRandomByConfidence(task.sentence.isEternal());

            Truth truthSecond = second_belief.truth;

            if (terms_dependent == null) {
                final int initialTermListSize = 8;
                terms_dependent = new ArrayList<>(initialTermListSize);
                terms_independent = new ArrayList<>(initialTermListSize);

                //TODO use one Map<Term, Term[]> instead of 4 Map<Term,Term> (values would be 4-element array)
                Values = newVariableSubstitutionMap();
                /*Values2 = newVariableSubstitutionMap();
                Values3 = newVariableSubstitutionMap();
                Values4 = newVariableSubstitutionMap();*/
                smap = newVariableSubstitutionMap();
            }

            //we have to select a random belief
            terms_dependent.clear();
            terms_independent.clear();

            //ok, we have selected a second concept, we know the truth value of a belief of it, lets now go through taskterms term
            //for two levels, and remember the terms which unify with second
            Term[] components_level1 = ((Compound) firstTerm).term;
            Term secterm_unwrap = unwrapNegation(secTerm);

            for (final Term T1 : components_level1) {
                Term T1_unwrap = unwrapNegation(T1);
                Values.clear(); //we are only interested in first variables

                smap.clear();

                if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, T1_unwrap, secterm_unwrap, Values, smap, r)) {

                    Compound ctaskterm_subs = (Compound) firstTerm;
                    ctaskterm_subs = ctaskterm_subs.applySubstituteToCompound(Values);
                    Term taskterm_subs = reduceUntilLayer2(ctaskterm_subs, secTerm, nal.memory);
                    if (taskterm_subs != null && !(Variables.indepVarUsedInvalid(taskterm_subs))) {
                        terms_dependent.add(taskterm_subs);
                    }
                }

                Values.clear(); //we are only interested in first variables
                smap.clear();

                if (Variables.findSubstitute(Symbols.VAR_INDEPENDENT, T1_unwrap, secterm_unwrap, Values, smap, r)) {
                    Compound ctaskterm_subs = (Compound) firstTerm;
                    ctaskterm_subs = ctaskterm_subs.applySubstituteToCompound(Values);
                    Term taskterm_subs = reduceUntilLayer2(ctaskterm_subs, secTerm, nal.memory);
                    if (taskterm_subs != null && !(Variables.indepVarUsedInvalid(taskterm_subs))) {

                        terms_independent.add(taskterm_subs);
                    }
                }

                if (!((T1_unwrap instanceof Implication) || (T1_unwrap instanceof Equivalence) || (T1_unwrap instanceof Conjunction) || (T1_unwrap instanceof Disjunction))) {
                    continue;
                }

                if (T1_unwrap instanceof Compound) {
                    Term[] components_level2 = ((Compound) T1_unwrap).term;

                    for (final Term T2 : components_level2) {
                        Term T2_unwrap = unwrapNegation(T2);

                        Values.clear(); //we are only interested in first variables
                        smap.clear();

                        if (Variables.findSubstitute(Symbols.VAR_DEPENDENT, T2_unwrap, secterm_unwrap, Values, smap, r)) {
                            //terms_dependent_compound_terms.put(Values3, (CompoundTerm)T1_unwrap);
                            Compound ctaskterm_subs = (Compound) firstTerm;
                            ctaskterm_subs = ctaskterm_subs.applySubstituteToCompound(Values);
                            Term taskterm_subs = reduceUntilLayer2(ctaskterm_subs, secTerm, nal.memory);
                            if (taskterm_subs != null && !(Variables.indepVarUsedInvalid(taskterm_subs))) {
                                terms_dependent.add(taskterm_subs);
                            }
                        }

                        Values.clear(); //we are only interested in first variables
                        smap.clear();

                        if (Variables.findSubstitute(Symbols.VAR_INDEPENDENT, T2_unwrap, secterm_unwrap, Values, smap, r)) {
                            //terms_independent_compound_terms.put(Values4, (CompoundTerm)T1_unwrap);
                            Compound ctaskterm_subs = (Compound) firstTerm;
                            ctaskterm_subs = ctaskterm_subs.applySubstituteToCompound(Values);
                            Term taskterm_subs = reduceUntilLayer2(ctaskterm_subs, secTerm, nal.memory);
                            if (taskterm_subs != null && !(Variables.indepVarUsedInvalid(taskterm_subs))) {
                                terms_independent.add(taskterm_subs);
                            }
                        }
                    }
                }
            }

            if (taskSentence.truth == null)
                throw new RuntimeException("Task sentence truth must be non-null: " + taskSentence);


            final Stamper stamp = new Stamper(taskSentence.stamp, second_belief.stamp, nal.time(), taskSentence.getOccurrenceTime());

            if (!terms_dependent.isEmpty()) {
                dedSecondLayerVariableUnificationTerms(nal, task,
                        second_belief, stamp, terms_dependent,
                        anonymousAnalogy(taskSentence.truth, truthSecond),
                        taskSentence.truth, truthSecond, false);
            }

            if (!terms_independent.isEmpty()) {
                dedSecondLayerVariableUnificationTerms(nal, task,
                        second_belief, stamp, terms_independent,
                        deduction(taskSentence.truth, truthSecond),
                        taskSentence.truth, truthSecond, true);
            }

            final int dur = nal.memory.duration();

            final int termsIndependent = terms_independent.size();
            for (int i = 0; i < termsIndependent; i++) {

                Term result = Sentence.termOrNull(terms_independent.get(i));

                if (result == null) {
                    //changed from return to continue to allow furhter processing
                    continue;
                }

                Truth truth;

                char mark = Symbols.JUDGMENT;
                if (taskSentence.isGoal()) {
                    truth = TruthFunctions.desireInd(taskSentence.truth, truthSecond);
                    mark = Symbols.GOAL;
                } else {
                    truth = deduction(taskSentence.truth, truthSecond);
                }
                
                int order = taskSentence.getTemporalOrder();
                int side = 1;
                boolean eternal = true;
                long time = Stamp.ETERNAL;
                if ((order != ORDER_NONE) && (order!=ORDER_INVALID) && (!taskSentence.isGoal()) && (!taskSentence.isQuest())) {
                    long baseTime = second_belief.getOccurrenceTime();
                    if (baseTime == Stamp.ETERNAL) {
                        baseTime = nal.time();
                    }
                    long inc = order * dur;
                    time = (side == 0) ? baseTime+inc : baseTime-inc;
                    eternal = false;
                    //nal.getTheNewStamp().setOccurrenceTime(time);
                }

                Budget budget = BudgetFunctions.compoundForward(truth, result, nal);


                //same as above?

                long occ = taskSentence.getOccurrenceTime();
                if (!second_belief.isEternal()) {
                    occ = second_belief.getOccurrenceTime();
                }


                Sentence newSentence = new Sentence(result, mark, truth, new Stamper(taskSentence, second_belief.stamp, nal.time(), occ));


                Task dummy = new Task(second_belief, budget, task, null);
                Task newTask = new Task(newSentence, budget, task, null);

                nal.setCurrentBelief(taskSentence);

                if (null!=nal.deriveTask(new TaskSeed(nal.memory, newTask), false, false, dummy, true /* allow overlap */)) {

                    nal.emit(Events.ConceptUnification.class, newTask, firstTerm, secondConcept, second_belief);
                    nal.memory.logic.DED_SECOND_LAYER_VARIABLE_UNIFICATION.hit();

                    unifiedAnything = true;

                }

            }

            remainingUnifications--;

            if (remainingUnifications == 0) {
                break;
            }

        }

        return unifiedAnything;
    }

}
