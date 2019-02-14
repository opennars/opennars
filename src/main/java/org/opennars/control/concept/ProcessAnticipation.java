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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.opennars.interfaces.Timable;
import org.opennars.io.events.OutputHandler;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Conjunction;
import org.opennars.language.Equivalence;
import org.opennars.language.Implication;
import org.opennars.language.Interval;
import org.opennars.language.Statement;
import org.opennars.language.Term;
import org.opennars.main.Parameters;
import org.opennars.operator.Operator;
import org.opennars.operator.mental.Anticipate;

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

    private static Interval retLastInterval(Conjunction term) {
        if (term.term[term.term.length-1] instanceof Interval) {
            return (Interval)term.term[term.term.length-1];
        }
        return null;
    }

    private static Concept getConceptOfConditional(Term conditional, final DerivationContext nal) {
        Term conditionalWithoutIntervals = extractSeq((Conjunction)conditional);

        Concept conceptOfConditional = null;

        if (((Conjunction)conditional).term.length == 4) {
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

    public static void addCovariantAnticipationEntry(Implication impl, final DerivationContext nal) {


        Term conditional = ((CompoundTerm)impl.getSubject());//.applySubstitute(substitution);
        Term conditioned = impl.getPredicate();

        Term conditionalWithoutIntervals = extractSeq((Conjunction)conditional);

        Concept conceptOfConditional = getConceptOfConditional(conditional, nal);


        if (conceptOfConditional == null) {
            return; // TODO
        }

        synchronized(conceptOfConditional) {
            List<Concept.Predicted> predicted;

            if( conceptOfConditional.covariantPredictions.containsKey(conditionalWithoutIntervals) ) {
                predicted = conceptOfConditional.covariantPredictions.get(conditionalWithoutIntervals);
            }
            else {
                predicted = new ArrayList<>();
                conceptOfConditional.covariantPredictions.put(conditionalWithoutIntervals.cloneDeep(), predicted);
            }

            // add to predicted
            {
                boolean found = false;

                for (Concept.Predicted iPredicted : predicted) {
                    if (iPredicted.term.equals(conditioned)) {
                        found = true;

                        // add
                        Interval lastInterval = retLastInterval((Conjunction) conditional);
                        if (lastInterval == null) {
                            return; // doesn't have a interval at the last place - return
                        }
                        float timeDelta = lastInterval.time;
                        iPredicted.dist.next(timeDelta);

                        break;
                    }
                }

                if (!found) {
                    Interval lastInterval = retLastInterval((Conjunction) conditional);
                    if (lastInterval == null) {
                        return; // doesn't have a interval at the last place - return
                    }
                    float timeDelta = lastInterval.time;
                    Concept.Predicted newPredicted = new Concept.Predicted(conditioned, timeDelta);
                    predicted.add(newPredicted);

                    // keep under AIKR by limiting memory
                    // heuristic: we kick out the item with the lowest number of events
                    {
                        int COVARIANCETABLE_ENTRIES = 300;
                        if (predicted.size() > COVARIANCETABLE_ENTRIES) {
                            int idxWithLowest = 0;

                            // size()-1 because we don't want to kick out the added item
                            for(int idx=0;idx<predicted.size()-1;idx++) {
                                if( predicted.get(idx).dist.n < predicted.get(idxWithLowest).dist.n ) {
                                    idxWithLowest = idx;
                                }
                            }

                            predicted.remove(idxWithLowest);
                        }
                    }
                }
            }
        }

    }

    public static void anticipateEstimate(final DerivationContext nal, final Sentence mainSentence, final BudgetValue budget,
                                           final float priority, Map<Term,Term> substitution) {

        float timeOffset, timeWindowHalf;

        Implication impl = (Implication)mainSentence.term;

        Term conditional = ((CompoundTerm)impl.getSubject()).applySubstitute(substitution);
        Term conditioned = impl.getPredicate();

        Term conditionalWithoutIntervals = extractSeq((Conjunction)conditional);

        Concept conceptOfConditional = getConceptOfConditional(conditional, nal);
        synchronized (conceptOfConditional) {

            /*
            if(((Conjunction)conditional).term.length == 2) {
                Term firstConditional = ((Conjunction)conditional).term[0];

                conceptOfConditional = nal.memory.concepts.get(firstConditional);
            }
            */



        /*
        if (((Conjunction)conditional).term.length == 4) {
            conceptOfConditional = nal.memory.concepts.get(conditionalWithoutIntervals);
            if (conceptOfConditional == null) {
                conceptOfConditional = nal.memory.conceptualize(new BudgetValue(1.0f, 0.98f, 1.0f, nal.narParameters), conditionalWithoutIntervals);
            }
        }
        else if(((Conjunction)conditional).term.length == 2) {
            Term firstConditional = ((Conjunction)conditional).term[0];

            conceptOfConditional = nal.memory.concepts.get(firstConditional);
            if(conceptOfConditional == null) {
                // estimate min and max with standard OpenNARS interval estimation
                timeOffset = ((Interval) ((Conjunction) conditional).term[1]).time;
                timeWindowHalf = timeOffset * nal.narParameters.ANTICIPATION_TOLERANCE;
            }
        }
        */

            if (conceptOfConditional == null) {
                return; // TODO
            }

            //Term firstConditional = ((Conjunction)conditional).term[0];

        /*
        conceptOfConditional = nal.memory.concepts.get(firstConditional);
        if(conceptOfConditional == null) {
            // estimate min and max with standard OpenNARS interval estimation
            timeOffset = ((Interval) ((Conjunction)conditional).term[1]).time;
            timeWindowHalf = timeOffset * nal.narParameters.ANTICIPATION_TOLERANCE;
        }
        else {
            */
            List<Concept.Predicted> predicted;

            if( conceptOfConditional.covariantPredictions.containsKey(conditionalWithoutIntervals) ) {
                predicted = conceptOfConditional.covariantPredictions.get(conditionalWithoutIntervals);
            }
            else {
                predicted = new ArrayList<>();
                conceptOfConditional.covariantPredictions.put(conditionalWithoutIntervals.cloneDeep(), predicted);
            }

            // add to predicted
            {

                boolean found = false;
                Concept.Predicted matchingPredicted = null; // predicted which matches to the predicted term

                for(Concept.Predicted iPredicted : predicted) {
                    if(iPredicted.term.equals(conditioned)) {
                        matchingPredicted = iPredicted;
                        found = true;

                        // add
                        float timeDelta = (retLastInterval((Conjunction)conditional)).time;
                        iPredicted.dist.next(timeDelta);

                        break;
                    }
                }

                if(found) {
                    { // sample from distribution
                        float mean = (float)matchingPredicted.dist.mean;
                        float variance = (float)matchingPredicted.dist.calcVariance();

                        float scaledVariance = variance * 0.7f; // TODO< make parameter >
                        timeWindowHalf = scaledVariance * 0.5f;
                        timeOffset = mean;
                    }

                    // debug
                    {
                        if(matchingPredicted.dist.calcVariance() > 0.00001) {
                            System.out.println("call anticipate for term=" + mainSentence.term + " (" + (timeOffset-timeWindowHalf) + ";" + (timeOffset+timeWindowHalf) + ")");
                        }
                    }
                }
                else {
                    // estimate min and max with standard OpenNARS interval estimation
                    timeOffset = (retLastInterval((Conjunction)conditional)).time;
                    timeWindowHalf = timeOffset * nal.narParameters.ANTICIPATION_TOLERANCE;
                }

                /*
                if(!found) {
                    float timeDelta = (retLastInterval((Conjunction)conditional)).time;
                    Concept.Predicted newPredicted = new Concept.Predicted(conditioned, timeDelta);
                    predicted.add(newPredicted);

                    matchingPredicted = newPredicted;
                }*/



            }
        }


        long mintime = (long) Math.max(mainSentence.getOccurenceTime(), (mainSentence.getOccurenceTime() + timeOffset - timeWindowHalf - 1));
        long maxtime = (long) (mainSentence.getOccurenceTime() + timeOffset + timeWindowHalf + 1);

        int debugHere = 5;

        //System.out.println("call anticipate for term=" + mainSentence.term + " (" + (timeOffset-timeWindowHalf) + ";" + (timeOffset+timeWindowHalf) + ")");

        anticipate(nal, mainSentence, budget, mintime, maxtime, priority, substitution);
    }

    public static void anticipate(final DerivationContext nal, final Sentence mainSentence, final BudgetValue budget,
                                  final long mintime, final long maxtime, final float priority, Map<Term,Term> substitution) {
        //derivation was successful and it was a judgment event

        //System.out.println("anticipate() " + mainSentence);

        if (mainSentence.toString().contains("^")) {
            int debug5 = 5;
        }

        final Stamp stamp = new Stamp(nal.time, nal.memory);
        stamp.setOccurrenceTime(Stamp.ETERNAL);
        float eternalized_induction_confidence = nal.memory.narParameters.ANTICIPATION_CONFIDENCE;
        final Sentence s = new Sentence(
            mainSentence.term,
            mainSentence.punctuation,
            new TruthValue(0.0f, eternalized_induction_confidence, nal.narParameters),
            stamp);
        final Task t = new Task(s, new BudgetValue(0.99f,0.1f,0.1f, nal.narParameters), Task.EnumType.DERIVED); //Budget for one-time processing
        Term specificAnticipationTerm = ((CompoundTerm)((Statement) mainSentence.term).getPredicate()).applySubstitute(substitution);
        final Concept c = nal.memory.concept(specificAnticipationTerm); //put into consequence concept
        if(c != null /*&& mintime > nal.memory.time()*/ && c.observable && (mainSentence.getTerm() instanceof Implication || mainSentence.getTerm() instanceof Equivalence) &&
            mainSentence.getTerm().getTemporalOrder() == TemporalRules.ORDER_FORWARD) {


            /*
            { // maintain covariance
                Term conditionalTerm = ((CompoundTerm)((Statement) mainSentence.term).getSubject()).applySubstitute(substitution);
                Term conditionalTermWithZeroIntervals = zeroIntervals((Conjunction) conditionalTerm);

                final int quantization = 10;
                int[] quantizedIntervals =retQuantizedIntervals((Conjunction)conditionalTerm, quantization);

                Concept.Covariant covariant;

                if( c.covariantPredictions.containsKey(conditionalTermWithZeroIntervals) ) {
                    covariant = c.covariantPredictions.get(conditionalTermWithZeroIntervals);
                }
                else {
                    covariant = new Concept.Covariant();
                    c.covariantPredictions.put(conditionalTermWithZeroIntervals.clone(), covariant);
                }

                { // increment counter
                    if (!covariant.covariantCounter.containsKey(new Concept.Covariant.IntArr(quantizedIntervals))) {
                        covariant.covariantCounter.put(new Concept.Covariant.IntArr(quantizedIntervals), 0);
                    }

                    int counter = covariant.covariantCounter.get(new Concept.Covariant.IntArr(quantizedIntervals));
                    counter++;
                    covariant.covariantCounter.put(new Concept.Covariant.IntArr(quantizedIntervals), counter);

                    System.out.println(conditionalTermWithZeroIntervals + "   " +  conv2Str(quantizedIntervals) + " " + counter);
                }
            }*/



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
     * @param time The time
     */
    public static void maintainDisappointedAnticipations(final Parameters narParameters, final Concept concept, final Timable time) {
        //here we can check the expiration of the feedback:
        List<Concept.AnticipationEntry> confirmed = new ArrayList<>();
        List<Concept.AnticipationEntry> disappointed = new ArrayList<>();
        for(Concept.AnticipationEntry entry : concept.anticipations) {
            if(entry.negConfirmation == null || time.time() <= entry.negConfirm_abort_maxtime) {
                continue;
            }
            //at first search beliefs for input tasks:
            boolean gotConfirmed = false;
            if(narParameters.RETROSPECTIVE_ANTICIPATIONS) {
                for(final TaskLink tl : concept.taskLinks) { //search for input in tasklinks (beliefs alone can not take temporality into account as the eternals will win)
                    final Task t = tl.targetTask;
                    if(t!= null && t.sentence.isJudgment() && t.isInput() && !t.sentence.isEternal() && t.sentence.truth.getExpectation() > concept.memory.narParameters.DEFAULT_CONFIRMATION_EXPECTATION &&
                        CompoundTerm.replaceIntervals(t.sentence.term).equals(CompoundTerm.replaceIntervals(concept.getTerm()))) {
                        if(t.sentence.getOccurenceTime() >= entry.negConfirm_abort_mintime && t.sentence.getOccurenceTime() <= entry.negConfirm_abort_maxtime) {
                            confirmed.add(entry);
                            gotConfirmed = true;
                            break;
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
            concept.memory.inputTask(time, entry.negConfirmation, false);
            concept.anticipations.remove(entry);
        }
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
}
