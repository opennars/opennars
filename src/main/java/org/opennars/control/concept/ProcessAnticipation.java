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
package org.opennars.control.concept;

import java.util.*;

import org.opennars.control.DerivationContext;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.entity.TaskLink;
import org.opennars.entity.TermLink;
import org.opennars.entity.TruthValue;
import org.opennars.inference.RuleTables;
import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.io.events.OutputHandler;
import org.opennars.language.*;
import org.opennars.main.Nar;
import org.opennars.main.Parameters;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.operator.mental.Anticipate;

import static org.opennars.inference.UtilityFunctions.c2w;
import static org.opennars.inference.UtilityFunctions.w2c;

/**
 *
 * @author Patrick Hammer
 */
public class ProcessAnticipation {

    private static Term extractSeq(Conjunction term) {
        Term[] arr = new Term[term.term.length/2];
        for(int i=0;i<term.term.length/2;i++) {
            arr[i] = term.term[i*2];
        }

        if(arr.length == 1) {
            return arr[0];
        }
        return Conjunction.make(arr, term.temporalOrder, term.isSpatial);
    }

    private static Conjunction quantizeSeq(Conjunction term, int quantization) {
        Term[] arr = new Term[term.term.length];
        for(int i=0;i<term.term.length;i++) {
            arr[i] = term.term[i];

            if (!(term.term[i] instanceof Interval)) {
                continue;
            }

            Interval interval = (Interval)term.term[i];
            long intervalTime = interval.time;
            // quanitze
            intervalTime = (intervalTime / quantization) * quantization;

            arr[i] = new Interval(intervalTime);
        }

        return (Conjunction)Conjunction.make(arr, term.temporalOrder, term.isSpatial);
    }

    private static Term extractSeqQuantized(Conjunction term, final int quantization) {
        Conjunction quantized = quantizeSeq(term, quantization);

        int cutoff = 0; // we want to cutt of the last interval
        if (quantized.term[quantized.term.length-1] instanceof Interval) {
            cutoff = 1;
        }

        Term[] arr = new Term[quantized.term.length-cutoff];
        for(int i=0;i<(quantized.term.length-cutoff);i++) {
            arr[i] = quantized.term[i];
        }

        if(arr.length == 1) {
            return arr[0]; // return term if it is the only content of the conjunction
        }
        return Conjunction.make(arr, term.temporalOrder, term.isSpatial);
    }

    private static Interval retLastInterval(Conjunction term) {
        if (term.term[term.term.length-1] instanceof Interval) {
            return (Interval)term.term[term.term.length-1];
        }
        return null;
    }

    private static long sumOfIntervalsExceptLastOne(Conjunction term) {
        long sum = 0;
        for(int i=0;i<term.term.length-1;i++) {
            if (!(term.term[i] instanceof Interval)) {
                continue;
            }

            Interval interval = (Interval) term.term[i];
            long intervalTime = interval.time;
            sum += intervalTime;
        }
        return sum;
    }

    private static Concept getConceptOfConditional(Term conditional, final DerivationContext nal) {


        Concept conceptOfConditional = null;

        if (((Conjunction)conditional).term.length > 2) {
            Term conditionalWithoutIntervals = extractSeq((Conjunction)conditional);

            conceptOfConditional = nal.memory.concepts.get(conditionalWithoutIntervals);
            if (conceptOfConditional == null) {
                conceptOfConditional = nal.memory.conceptualize(new BudgetValue(1.0f, 0.98f, 1.0f, nal.narParameters), conditionalWithoutIntervals);
            }
        }
        else if(((Conjunction)conditional).term.length == 2) {
            Term firstConditional = ((Conjunction)conditional).term[0];

            conceptOfConditional = nal.memory.concepts.get(firstConditional);
        }

        return conceptOfConditional;
    }

    private static void keepCovariantTableUnderAikr(Concept c, Parameters reasonerParameters) {
        // keep under AIKR by limiting memory
        // heuristic: we kick out the item with the lowest number of events

        // TODO< use latest time of modification as heuristic - is much better >

        if (c.covariantPredictions2.size() <= reasonerParameters.COVARIANCE_TABLE_ENTRIES) {
            return;
        }

        int idxWithLowest = 0;
        long lowestCounter = c.covariantPredictions2.get(0).dist.n;
        for(int idx=0;idx<c.covariantPredictions2.size();idx++) {
            Concept.Predicted iPredicted = c.covariantPredictions2.get(idx);
            if (iPredicted.dist.n < lowestCounter) {
                lowestCounter = iPredicted.dist.n;
                idxWithLowest = idx;
            }
        }
        c.covariantPredictions2.remove(idxWithLowest);
    }

    public static void addCovariantAnticipationEntry(Implication impl, final DerivationContext nal) {


        //Term conditional = ((CompoundTerm)impl.getSubject());//.applySubstitute(substitution);
        //Term conditioned = impl.getPredicate();

        //Term conditionalWithQuantizedIntervals = extractSeqQuantized((Conjunction)conditional, nal.narParameters.COVARIANCE_QUANTIZATION);

        //Concept conceptOfConditional = getConceptOfConditional(conditional, nal);


        //if (conceptOfConditional == null) {
        //    return; // TODO
        //}

        Term conditionalWithVars = impl.getSubject();
        Term conditionalWithQuantizedIntervalsWithVars = extractSeqQuantized((Conjunction)conditionalWithVars, nal.narParameters.COVARIANCE_QUANTIZATION);
        Term conditionedWithVars = impl.getPredicate();

        Set<Term> targets = new LinkedHashSet<>();
        {
            //add to all components, unless it doesn't have vars
            Map<Term, Integer> ret = CompoundTerm.replaceIntervals(impl).countTermRecursively(null);
            for(Term r : ret.keySet()) {
                targets.add(r);
            }
        }


        //System.out.println("addCovariantAnticipationEntry()");

        //if (conditionalWithQuantizedIntervalsWithVars.toString().equals("(&/,(--,<{switch0} --> [on]>),+0,(^go-to,{SELF},{light1}))")) {
        //    int here = 5;
        //}

        //System.out.println("   cond  = " + conditionalWithQuantizedIntervalsWithVars);
        //System.out.println("   eff   = " + conditionedWithVars);

        for(Term iTarget : targets) { // iterate over sub-terms
            final Concept targetConcept = nal.memory.concept(iTarget);
            if (targetConcept == null) { // target concept does not exist
                continue;
            }

            //System.out.println("  c=" + iTarget);


            synchronized (targetConcept) {
                Concept.Predicted matchingCovarianceEntry = null;
                { // search for matching conditional in (co)variances

                    // TODO< speed up when finalized >
                    for(int idx=0;idx<targetConcept.covariantPredictions2.size();idx++) {
                        Concept.Predicted iPredicted = targetConcept.covariantPredictions2.get(idx);
                        if (iPredicted.conditionalTerm.equals(conditionalWithQuantizedIntervalsWithVars) && iPredicted.conditionedTerm.equals(conditionedWithVars)) {
                            matchingCovarianceEntry = iPredicted;
                            break;
                        }
                    }
                }

                boolean found = matchingCovarianceEntry != null;
                if (found) {
                    Interval lastInterval = retLastInterval((Conjunction)conditionalWithVars);
                    if (lastInterval == null) {
                        continue; // doesn't have a interval at the last place - return
                    }
                    float timeDelta = lastInterval.time;

                    matchingCovarianceEntry.dist.next(timeDelta);
                }
                else {
                    keepCovariantTableUnderAikr(targetConcept, nal.narParameters);

                    // add entry

                    Interval lastInterval = retLastInterval((Conjunction)conditionalWithVars);
                    if (lastInterval == null) {
                        continue; // doesn't have a interval at the last place - return
                    }
                    float timeDelta = lastInterval.time;

                    Concept.Predicted newPredicted = new Concept.Predicted(conditionalWithQuantizedIntervalsWithVars, conditionedWithVars, timeDelta);
                    targetConcept.covariantPredictions2.add(newPredicted);
                }
            }
        }

        /* OLD DEPRECATED CODE

        synchronized(conceptOfConditional) {
            Map<Term, Concept.Predicted> predicted = null;
            // retrieve or create predicted map
            // retrieval is done by matching variables (if possible)
            {
                //if(conditionalWithQuantizedIntervals.hasVar()) {
                //    int debug5 = 1;
                //}

                boolean containsKey = conceptOfConditional.covariantPredictions.containsKey(conditionalWithQuantizedIntervals);
                boolean anyUnified = false;
                if (containsKey) {
                    predicted = conceptOfConditional.covariantPredictions.get(conditionalWithQuantizedIntervals);
                }
                /* dead code because it is never the case
                else {
                    for (final Map.Entry<Term, Map<Term, Concept.Predicted>> iEntry : conceptOfConditional.covariantPredictions.entrySet()) {
                        Map[] unifier = new LinkedHashMap[]{new LinkedHashMap<Term,Term>(), new LinkedHashMap<Term,Term>()};
                        boolean foundSubstitute = Variables.findSubstitute(nal.memory.randomNumber, Symbols.VAR_INDEPENDENT, conditionalWithQuantizedIntervals, iEntry.getKey(), unifier);
                        if (foundSubstitute) {

                            anyUnified = true;
                            break;
                        }
                    }
                }
                *


                if( !containsKey && !anyUnified ) {
                    predicted = new HashMap<>();
                    conceptOfConditional.covariantPredictions.put(conditionalWithQuantizedIntervals.cloneDeep(), predicted);
                }
            }


            // add to predicted
            {
                boolean found = predicted.containsKey(conditioned);
                if (found) {
                    // add
                    Interval lastInterval = retLastInterval((Conjunction) conditional);
                    if (lastInterval == null) {
                        return; // doesn't have a interval at the last place - return
                    }
                    float timeDelta = lastInterval.time;
                    predicted.get(conditioned).dist.next(timeDelta);
                }
                else {
                    Interval lastInterval = retLastInterval((Conjunction) conditional);
                    if (lastInterval == null) {
                        return; // doesn't have a interval at the last place - return
                    }
                    float timeDelta = lastInterval.time;
                    Concept.Predicted newPredicted = new Concept.Predicted(timeDelta);


                    // keep under AIKR by limiting memory
                    // heuristic: we kick out the item with the lowest number of events
                    {
                        if (predicted.size() > nal.narParameters.COVARIANCE_TABLE_ENTRIES) {
                            Map.Entry<Term, Concept.Predicted> entryWithLowest = null;

                            for(Map.Entry<Term, Concept.Predicted> iPredictedEntry : predicted.entrySet()) {
                                if(entryWithLowest == null) {
                                    entryWithLowest = iPredictedEntry;
                                }

                                if(entryWithLowest.getValue().dist.n < iPredictedEntry.getValue().dist.n) {
                                    entryWithLowest = iPredictedEntry;
                                }
                            }

                            predicted.remove(entryWithLowest.getKey());
                        }
                    }

                    predicted.put(conditioned, newPredicted);
                }
            }
        }
        */
    }

    public static AnticipationTimes anticipationEstimateMinAndMaxTimes(final DerivationContext nal, final Sentence mainSentence, Map<Term,Term> substitution) {
        return anticipationEstimateMinAndMaxTimes(nal, (Implication)mainSentence.term, substitution, mainSentence.getOccurenceTime());
    }

    public static AnticipationTimes anticipationEstimateMinAndMaxTimes(final DerivationContext nal, final Implication impl, Map<Term,Term> substitution, long occurenceTime) {
        //Implication impl = (Implication)mainSentence.term;

        if (false){ // debug
            boolean isPredictiveBySeq =
                impl instanceof Implication &&
                    impl.getTemporalOrder() == TemporalRules.ORDER_FORWARD &&
                    ((Implication)impl).getSubject() instanceof CompoundTerm &&
                    ((CompoundTerm)((Implication)impl).getSubject()).term.length > 2;
            if (isPredictiveBySeq) {
                System.out.println("ProcessAnticipation: call anticipate for term=" + impl);

                int debugHere = 5;
            }
        }



        Concept implConcept = nal.memory.conceptualize(new BudgetValue(1.0f, 0.98f, 1.0f, nal.narParameters), CompoundTerm.replaceIntervals(impl));

        Term conditionalWithVars = impl.getSubject();
        Term conditionalWithQuantizedIntervalsWithVars = extractSeqQuantized((Conjunction)conditionalWithVars, nal.narParameters.COVARIANCE_QUANTIZATION);
        Term conditionedWithVars = impl.getPredicate();


        Concept.Predicted matchingCovarianceEntry = null;
        { // search for matching conditional in (co)variances

            // TODO< speed up when finalized >
            for(int idx=0;idx<implConcept.covariantPredictions2.size();idx++) {
                Concept.Predicted iPredicted = implConcept.covariantPredictions2.get(idx);
                if (iPredicted.conditionalTerm.equals(conditionalWithQuantizedIntervalsWithVars) && iPredicted.conditionedTerm.equals(conditionedWithVars)) {
                    matchingCovarianceEntry = iPredicted;
                    break;
                }
            }
        }

        boolean found = matchingCovarianceEntry != null;
        if( !found) {
            return null; // we can't estimate a covariance if we don't know anything about it
        }
        else if (matchingCovarianceEntry.dist.n <= 1) {
            return null; // to few samples to make a meaningful estimation!
        }


        float timeOffset = 0, timeWindowHalf = 0;
        { // sample from distribution
            float mean = (float) matchingCovarianceEntry.dist.mean;
            float variance = (float) matchingCovarianceEntry.dist.calcVariance();

            float scaledVariance = variance * nal.narParameters.COVARIANCE_WINDOW;
            timeWindowHalf = scaledVariance * 0.5f;
            timeOffset = mean + sumOfIntervalsExceptLastOne((Conjunction) conditionalWithVars);
        }

        // debug
        {
            if (false && matchingCovarianceEntry.dist.calcVariance() > 0.00001) {

                boolean isPredictiveBySeq =
                    impl instanceof Implication &&
                        impl.getTemporalOrder() == TemporalRules.ORDER_FORWARD &&
                        ((Implication)impl).getSubject() instanceof CompoundTerm &&
                        ((CompoundTerm)((Implication)impl).getSubject()).term.length > 2;
                if (isPredictiveBySeq) {
                    System.out.println("ProcessAnticipation.anticipationEstimateMinAndMaxTimes(): successfull call anticipate for term=" + impl + " (" + (timeOffset - timeWindowHalf) + ";" + (timeOffset + timeWindowHalf) + ")");

                    int debugHere = 5;
                }
            }
        }


        // assert timeOffset != 0 and timeWindowHalf != 0

        AnticipationTimes result = new AnticipationTimes();
        result.occurrenceTime = occurenceTime;
        result.timeWindow = timeWindowHalf * 2.0f;
        result.timeOffset = timeOffset;

        if(true) {
            System.out.println("anticipationEstimateMinAndMaxTimes()");
            System.out.println("   term = " + impl);
            System.out.println("   ===> timeWindow=" + result.timeWindow);
        }

        return result;









        /* OLD DEPRECATED CODE

        Term substConditional = ((CompoundTerm)impl.getSubject());
        Term substConditioned = impl.getPredicate();

        Term substConditionalWithoutIntervals = extractSeq((Conjunction)substConditional);
        Term substConditionalWithQuantizedIntervals = extractSeqQuantized((Conjunction)substConditional, nal.narParameters.COVARIANCE_QUANTIZATION);

        List<Concept> conceptsToSearch = new ArrayList<>();


        if(mainSentence.term.hasVar()) {
            Term conditional = ((Implication)(mainSentence.term)).getSubject();
            Concept conceptOfConditional = getConceptOfConditional(conditional, nal);
            if (conceptOfConditional != null) {
                conceptsToSearch.add(conceptOfConditional);
            }
        }

        {
            Concept conceptOfConditional = getConceptOfConditional(substConditional, nal);
            if (conceptOfConditional != null) {
                conceptsToSearch.add(conceptOfConditional);
            }
        }

        if (conceptsToSearch.size() == 0) {
            return null;
        }

        for(final Concept iConceptOfConditional : conceptsToSearch) {

            synchronized (iConceptOfConditional) {

                Map<Term, Concept.Predicted> predicted = null;

                boolean useDefaultEstimation = false;

                // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
                // TODO TODO TODO   work out how and when to use variables and when to substitute etc
                // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
                // TODO< when concept of conditional contains variable then use not subst one >
                if( iConceptOfConditional.covariantPredictions.containsKey(substConditionalWithQuantizedIntervals) ) {
                    predicted = iConceptOfConditional.covariantPredictions.get(substConditionalWithQuantizedIntervals);
                }
                else {
                    useDefaultEstimation = true;
                    //predicted = new ArrayList<>();
                    //conceptOfConditional.covariantPredictions.put(conditionalWithoutIntervals.cloneDeep(), predicted);
                }

                if (!useDefaultEstimation) {
                    boolean found = false;
                    Concept.Predicted matchingPredicted = null; // predicted which matches to the predicted term

                    found = predicted.containsKey(substConditioned);
                    if(found) {
                        matchingPredicted = predicted.get(substConditioned);
                    }


                    if (!found) {
                        useDefaultEstimation = true;
                    }
                    if (found) {
                        if (matchingPredicted.dist.n <= 1) {
                            return null; // to few samples for any usable anticipation
                            // one is definitly to less because it is a almost infinitisimal time for a proto-AGI!
                        }

                        { // sample from distribution
                            float mean = (float) matchingPredicted.dist.mean;
                            float variance = (float) matchingPredicted.dist.calcVariance();

                            float scaledVariance = variance * nal.narParameters.COVARIANCE_WINDOW;
                            timeWindowHalf = scaledVariance * 0.5f;
                            timeOffset = mean + sumOfIntervalsExceptLastOne((Conjunction) substConditional);
                        }

                        // debug
                        {
                            if (false && matchingPredicted.dist.calcVariance() > 0.00001) {

                                boolean isPredictiveBySeq =
                                    impl instanceof Implication &&
                                        impl.getTemporalOrder() == TemporalRules.ORDER_FORWARD &&
                                        ((Implication)impl).getSubject() instanceof CompoundTerm &&
                                        ((CompoundTerm)((Implication)impl).getSubject()).term.length > 2;
                                if (isPredictiveBySeq) {
                                    System.out.println("ProcessAnticipation.anticipationEstimateMinAndMaxTimes(): successfull call anticipate for term=" + impl + " (" + (timeOffset - timeWindowHalf) + ";" + (timeOffset + timeWindowHalf) + ")");

                                    int debugHere = 5;
                                }
                            }
                        }
                    }
                }

                if(useDefaultEstimation) {
                    return null; // we don't support default estimation anymore!

                    /*
                    // estimate min and max with standard OpenNARS interval estimation
                    timeOffset = (retLastInterval((Conjunction)conditional)).time;
                    timeWindowHalf = timeOffset * nal.narParameters.ANTICIPATION_TOLERANCE;
                    *
                }
            }

            break; // processing the first concept is enough
        }


        // assert timeOffset != 0 and timeWindowHalf != 0

        AnticipationTimes result = new AnticipationTimes();
        result.timeWindow = timeWindowHalf * 2.0f;
        result.timeOffset = timeOffset;

        if(false) {
            System.out.println("anticipationEstimateMinAndMaxTimes()");
            System.out.println("   term = " + impl);
            System.out.println("   mainSentence.term = " + mainSentence.term);
            System.out.println("   ===> timeWindow=" + result.timeWindow);
        }

        return result;
        */
    }

    /* commented because not used
    public static void anticipateEstimate(final DerivationContext nal, final Sentence mainSentence, final BudgetValue budget,
                                          final float priority, Map<Term,Term> substitution) {
        anticipateEstimate(nal, mainSentence, budget, priority, substitution, null);
    }
     */

    public static void anticipateEstimate(final DerivationContext nal, final Sentence mainSentence, final BudgetValue budget,
                                           final float priority, Map<Term,Term> substitution, AnticipationTimes anticipationTimes) {


        System.out.println("ProcessAnticipation:estimate() ENTRY");

        if (anticipationTimes == null) { // if we need to estimate the anticipation time from the sentence
            if (mainSentence.isEternal()) {
                return; // is actually not allow and a BUG when we land here
                // for now it's fine to just return
            }

            anticipationTimes = anticipationEstimateMinAndMaxTimes(nal, (Implication)mainSentence.term, substitution, mainSentence.getOccurenceTime());
            if (anticipationTimes == null) {
                return;
            }

            System.out.println("ProcessAnticipation:estimate() successfully estimated min/max");
        }





        long occurenceTime = anticipationTimes.occurrenceTime;
        float timeOffset = anticipationTimes.timeOffset;
        float timeWindowHalf = anticipationTimes.timeWindow * 0.5f;

        // assert timeOffset != 0 and timeWindowHalf != 0


        long mintime = (long) Math.max(occurenceTime, (occurenceTime + timeOffset - timeWindowHalf - 1));
        long maxtime = (long) (occurenceTime + timeOffset + timeWindowHalf + 1);

        if (maxtime < 0) {
            int debug6 = 5; // must never happen!
        }

        int debugHere = 5;

        //System.out.println("call anticipate for term=" + mainSentence.term + " (" + (timeOffset-timeWindowHalf) + ";" + (timeOffset+timeWindowHalf) + ")");

        anticipate(nal, mainSentence.term, mintime, maxtime, priority, substitution);
    }

    public static void anticipate(final DerivationContext nal, final Term term,
                                  final long mintime, final long maxtime, final float priority, Map<Term,Term> substitution) {
        //derivation was successful and it was a judgment event

        if (maxtime < 0) {
            int debug6 = 5; // must never happen!
        }

        //System.out.println("anticipate() " + mainSentence);

        final Stamp stamp = new Stamp(nal.time, nal.memory);
        stamp.setOccurrenceTime(Stamp.ETERNAL);
        float eternalized_induction_confidence = nal.memory.narParameters.ANTICIPATION_CONFIDENCE;
        final Sentence s = new Sentence(
            term,
            '.',
            new TruthValue(0.0f, eternalized_induction_confidence, nal.narParameters),
            stamp);
        final Task t = new Task(s, new BudgetValue(0.99f,0.1f,0.1f, nal.narParameters), Task.EnumType.DERIVED); //Budget for one-time processing

        System.out.println("anticipate() " + t.sentence.term);

        Term specificAnticipationTerm = ((CompoundTerm)((Statement) term).getPredicate()).applySubstitute(substitution);
        final Concept c = nal.memory.concept(specificAnticipationTerm); //put into consequence concept
        if(c != null /*&& mintime > nal.memory.time()*/ && c.observable && (term instanceof Implication || term instanceof Equivalence) &&
            term.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {

            Concept.AnticipationEntry toDelete = null;
            Concept.AnticipationEntry toInsert = new Concept.AnticipationEntry(priority, t, mintime, maxtime);
            boolean fullCapacity = c.anticipations.size() >= nal.narParameters.ANTICIPATIONS_PER_CONCEPT_MAX;
            //choose an element to replace with the new, in case that we are already at full capacity
            if(fullCapacity) {
                for(Concept.AnticipationEntry entry : c.anticipations) {
                    if(priority > entry.negConfirmationPriority /*|| t.getPriority() > c.negConfirmation.getPriority() */) {
                        //prefer to replace one that is more far in the future, takes longer to be disappointed about
                        if(toDelete == null || entry.negConfirm_abort_maxtime > toDelete.negConfirm_abort_maxtime) {
                            toDelete = entry;
                        }
                    }
                }
            }
            //we were at full capacity but there was no item that can be replaced with the new one
            if(fullCapacity && toDelete == null) {
                return;
            }
            if(toDelete != null) {
                c.anticipations.remove(toDelete);
            }
            c.anticipations.add(toInsert);
            final Statement impOrEqu = (Statement) toInsert.negConfirmation.sentence.term;
            final Concept ctarget = nal.memory.concept(impOrEqu.getPredicate());
            if(ctarget != null) {
                Operator anticipate_op = ((Anticipate)c.memory.getOperator("^anticipate"));
                if(anticipate_op != null && anticipate_op instanceof Anticipate) {
                    ((Anticipate)anticipate_op).anticipationFeedback(impOrEqu.getPredicate(), null, c.memory, nal.time);
                }
            }
            nal.memory.emit(OutputHandler.ANTICIPATE.class, specificAnticipationTerm); //disappoint/confirm printed anyway
        }

    }

    /**
     * Process outdated anticipations within the concept,
     * these which are outdated generate negative feedback
     *
     * @param narParameters The reasoner parameters
     * @param concept The concept which potentially outdated anticipations should be processed
     */
    public static void maintainDisappointedAnticipations(final Parameters narParameters, final Concept concept, final Nar nar) {
        //here we can check the expiration of the feedback:
        List<Concept.AnticipationEntry> confirmed = new ArrayList<>();
        List<Concept.AnticipationEntry> disappointed = new ArrayList<>();
        for(Concept.AnticipationEntry entry : concept.anticipations) {
            if(entry.negConfirmation == null || nar.time() <= entry.negConfirm_abort_maxtime) {
                continue;
            }
            //at first search beliefs for input tasks:
            boolean gotConfirmed = false;
            if(narParameters.RETROSPECTIVE_ANTICIPATIONS) {
                for(final TaskLink tl : concept.taskLinks) { //search for input in tasklinks (beliefs alone can not take temporality into account as the eternals will win)
                    final Task t = tl.targetTask;

                    final boolean isExpectationAboveThreshold = t.sentence.truth.getExpectation() > concept.memory.narParameters.DEFAULT_CONFIRMATION_EXPECTATION;

                    if(t!= null && t.sentence.isJudgment() && t.isInput() && !t.sentence.isEternal() && isExpectationAboveThreshold) {
                        if (CompoundTerm.replaceIntervals(t.sentence.term).equals(CompoundTerm.replaceIntervals(concept.getTerm()))) {
                            if(t.sentence.getOccurenceTime() >= entry.negConfirm_abort_mintime && t.sentence.getOccurenceTime() <= entry.negConfirm_abort_maxtime) {
                                confirmed.add(entry);
                                gotConfirmed = true;
                                break;
                            }
                        }
                    }
                }
            }
            if(!gotConfirmed) {
                disappointed.add(entry);
            }
        }
        //confirmed by input, nothing to do
        if(confirmed.size() > 0) {
            concept.memory.emit(OutputHandler.CONFIRM.class,concept.getTerm());
        }
        concept.anticipations.removeAll(confirmed);
        //not confirmed and time is out, generate disappointment
        if(disappointed.size() > 0) {
            concept.memory.emit(OutputHandler.DISAPPOINT.class,concept.getTerm());
        }
        for(Concept.AnticipationEntry entry : disappointed) {
            final Term term = entry.negConfirmation.getTerm();

            { // debug
                boolean isPredictiveBySeq =
                    term instanceof Implication &&
                        term.getTemporalOrder() == TemporalRules.ORDER_FORWARD &&
                        ((Implication)term).getSubject() instanceof CompoundTerm &&
                        ((CompoundTerm)((Implication)term).getSubject()).term.length > 2;
                if (isPredictiveBySeq) {
                    long currentTime = nar.time();

                    System.out.println("ProcessAnticipation: disappoint for term=" + term + " (" + (currentTime-entry.negConfirm_abort_mintime) + "-" + (currentTime-entry.negConfirm_abort_maxtime) + ")");

                    int debugHere = 5;
                }
            }

            TruthValue defaultTruth = calcDefaultTruth(term, narParameters);
            TruthValue eternalizedDefaultTruth = TruthFunctions.eternalize(defaultTruth, narParameters);

            int numberOfIntervals = 1; // TODO< count >
            float ANTICIPATION_NEG_FACTOR = 0.85f;

            // fix up confidence
            // This is engineered this way because the truth of the orginal statement (which was the basis of the anticipated event)
            // is not known - and it was modified with projection.
            // So we somehow need to "simulate" the truth loss from the projection.
            // we do this by scaling the weight of the truth
            float w = c2w(eternalizedDefaultTruth.getConfidence(), narParameters);
            w *= (float)Math.pow(ANTICIPATION_NEG_FACTOR, numberOfIntervals);
            float c = w2c(w, narParameters);

            final TruthValue truth = new TruthValue(0.0f, c, narParameters); // frequency of negative confirmation is 0.0

            if (defaultTruth != null) {
                final Sentence sentenceForNewTask = new Sentence(
                    term,
                    Symbols.JUDGMENT_MARK,
                    truth,
                    new Stamp(nar, nar.memory, Tense.Eternal));
                final BudgetValue budget = new BudgetValue(0.99f, 0.1f, 0.1f, nar.narParameters);
                final Task t = new Task(sentenceForNewTask, budget, Task.EnumType.DERIVED);

                concept.memory.inputTask(nar, t, false);
            }

            concept.anticipations.remove(entry);
        }
    }


    // computes "default" truth of term if all terms have default confidence
    // return null if truth can be ignored
    private static TruthValue calcDefaultTruth(final Term term, final Parameters reasonerParameters) {
        if (term instanceof Interval || term instanceof Variable) {
            return null; // ignore for truth-value computation
        }
        else if(term instanceof Inheritance) {
            return new TruthValue(1.0f, reasonerParameters.DEFAULT_JUDGMENT_CONFIDENCE, reasonerParameters);
        }
        else if ((term instanceof Implication) && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
            Implication termAsCompound = (Implication)term;
            final TruthValue tvOfSubject = calcDefaultTruth(termAsCompound.getSubject(), reasonerParameters);
            final TruthValue tvOfPredicate = calcDefaultTruth(termAsCompound.getPredicate(), reasonerParameters);
            return TruthFunctions.induction(tvOfSubject, tvOfPredicate, reasonerParameters);
        }
        else if (term instanceof Conjunction) {
            final CompoundTerm termAsCompound = (CompoundTerm)term;

            if (termAsCompound.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                TruthValue tv = new TruthValue(1.0f, reasonerParameters.DEFAULT_JUDGMENT_CONFIDENCE, reasonerParameters);
                for(int idx=1;idx<termAsCompound.term.length;idx++) {
                    final TruthValue componentTv = calcDefaultTruth(termAsCompound.term[idx], reasonerParameters);
                    if (componentTv == null) {
                        continue; // ignored for truth-value computation
                    }

                    tv = TruthFunctions.intersection(tv, componentTv, reasonerParameters);
                }
                return tv;
            }
            else {
                return null; // TODO
            }
        }

        // TODO< return default for known variations so that we land only here if we haven't implemented a variation
        return new TruthValue(1.0f, reasonerParameters.DEFAULT_JUDGMENT_CONFIDENCE, reasonerParameters);
    }

    /**
     * Whether a processed judgement task satisfies the anticipations within concept
     *
     * @param task The judgement task be checked
     * @param concept The concept that is processed
     * @param nal The derivation context
     */
    public static void confirmAnticipation(Task task, Concept concept, final DerivationContext nal) {
        final boolean satisfiesAnticipation = task.isInput() && !task.sentence.isEternal();
        final boolean isExpectationAboveThreshold = task.sentence.truth.getExpectation() > nal.narParameters.DEFAULT_CONFIRMATION_EXPECTATION;
        List<Concept.AnticipationEntry> confirmed = new ArrayList<>();
        for(Concept.AnticipationEntry entry : concept.anticipations) {
            if(satisfiesAnticipation && isExpectationAboveThreshold && task.sentence.getOccurenceTime() > entry.negConfirm_abort_mintime) {

                if (entry.negConfirmation.sentence.term.toString().length() > 60) {
                    System.out.println("confirm (neg)anticipation " + entry.negConfirmation);

                    int debugHere = 5;

                }

                confirmed.add(entry);
            }
        }
        if(confirmed.size() > 0) {
            nal.memory.emit(OutputHandler.CONFIRM.class, concept.getTerm());
        }
        concept.anticipations.removeAll(confirmed);
    }

    /**
     * Fire predictictive inference based on beliefs that are known to the concept's neighbours
     *
     * @param judgementTask judgement task
     * @param concept concept that is processed
     * @param nal derivation context
     * @param time used to retrieve current time
     * @param tasklink coresponding tasklink
     */
    public static void firePredictions(final Task judgementTask, final Concept concept, final DerivationContext nal, Timable time, TaskLink tasklink) {
        if(!judgementTask.sentence.isEternal() && judgementTask.isInput() && judgementTask.sentence.isJudgment()) {
            for(TermLink tl : concept.termLinks) {
                Term term = tl.getTarget();
                Concept tc = nal.memory.concept(term);
                if(tc != null && !tc.beliefs.isEmpty() && term instanceof Implication) {
                    Implication imp = (Implication) term;
                    if(imp.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                        Term precon = imp.getSubject();
                        Term component = precon;
                        if(precon instanceof Conjunction) {
                            Conjunction conj = (Conjunction) imp.getSubject();
                            if(conj.getTemporalOrder() == TemporalRules.ORDER_FORWARD && conj.term.length == 2 && conj.term[1] instanceof Interval) {
                                component = conj.term[0]; //(&/,a,+i), so use a
                            }
                        }
                        if(CompoundTerm.replaceIntervals(concept.getTerm()).equals(CompoundTerm.replaceIntervals(component))) {
                            //trigger inference of the task with the belief
                            DerivationContext cont = new DerivationContext(nal.memory, nal.narParameters, time);
                            cont.setCurrentTask(judgementTask); //a
                            cont.setCurrentBeliefLink(tl); // a =/> b
                            cont.setCurrentTaskLink(tasklink); // a
                            cont.setCurrentConcept(concept); //a
                            cont.setCurrentTerm(concept.getTerm()); //a
                            RuleTables.reason(tasklink, tl, cont); //generate b
                        }
                    }
                }
            }
        }
    }

    public static class AnticipationTimes {
        public float timeOffset;
        public float timeWindow;
        public long occurrenceTime;
    }
}
