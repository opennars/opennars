package org.opennars.control;

import org.opennars.entity.*;
import org.opennars.inference.DeriverHelpers;
import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.language.*;
import org.opennars.main.Nar;
import org.opennars.main.Parameters;
import org.opennars.operator.Operation;
import org.opennars.storage.Memory;

import java.util.*;

import static java.lang.Long.max;
import static java.lang.Math.abs;
import static java.lang.Math.min;

/*
TODO< filter nonsense where implications are children of the left side of the root implication >

TrieDeriver.derive()
   a=<{SELF} --> [good]>. %1.00;0.90% [6703]
   b=<(&/,<{SELF} --> [good]>,+110) =/> <(&/,(^Right,{SELF}),+50) =/> <{left} --> [on]>>>. %1.00;0.29% [6781]
derived = <(&/,<{SELF} --> [good]>,+78) =/> <(&/,<{SELF} --> [good]>,+110) =/> <(&/,(^Right,{SELF}),+50) =/> <{left} --> [on]>>>>. %1.00;0.21% [6703]
derived = <(&/,<{SELF} --> [good]>,+78) =/> <(&/,<{SELF} --> [good]>,+110) =/> <(&/,(^Right,{SELF}),+50) =/> <{left} --> [on]>>>>. %1.00;0.21% [6703]
 */

public class TemporalControl {
    public double heatUp = 0.05; // config



    public boolean immediateProcessEvent(Task task, DerivationContext ctx) {
        if ((""+task.sentence.term).contains("(^")) {
            int here = 5;
        }

        heatupInputEvent(task);
        checkAddToEligibilityTrace(task);
        return true;
    }

    public void checkAddToEligibilityTrace(Task task) {
        if (!isValidTask(task, "")) {
            return;
        }

        // we need to add it to the eligibility trace
        eligibilityTrace.addEvent(task);
    }

    public boolean heatupInputEvent(Task task) {
        if (!isValidTask(task, "goal")) {
            return false;
        }

        if (!termWithHeatByTerm.containsKey(task.sentence.term)) {
            ConceptWithSalience createdConceptWithSalience = new ConceptWithSalience(task.sentence.term);
            termWithHeatByTerm.put(task.sentence.term, createdConceptWithSalience);
            sortedByHeat.add(createdConceptWithSalience);
        }

        termWithHeatByTerm.get(task.sentence.term).salience += heatUp;

        return true;
    }

    /**
     *
     * @param task
     * @param allowedHint allow additional, ex: "" or "goal"
     * @return
     */
    private boolean isValidTask(Task task, String allowedHint) {
        if (task.getTerm() == null || task.budget == null /*|| !task.isElemOfSequenceBuffer()*/) {
            return false;
        }

        {
            boolean allowed = false;
            if (task.sentence.isJudgment()) {
                allowed = true;
            }

            if (allowedHint.equals("goal") && task.sentence.isGoal()) {
                allowed = true;
            }

            if (!allowed) {
                return false;
            }
        }

        if (task.sentence.isEternal() || !task.isInput()) {
            return false;
        }

        // ignore NAL-9 ops
        if (("" + task.sentence).contains("^anticipate")) {
            return false; // because anticipate events are not useful to reason about
        }
        // we need to limit it just for testing and because it derives a lot of nonsense with to much complexity
        if (("" + task.sentence).contains("^believe") || ("" + task.sentence).contains("^want")) {
            //return false;
        }

        return true;
    }


    public void cooldown() {
        for(ConceptWithSalience iElement : termWithHeatByTerm.values()) {
            iElement.salience *= 0.98; // config
        }
    }

    public void update(long wallclockTime) {
        double eligibilityTraceDecayFactor = 0.001; // config

        eligibilityTrace.updateDecay(wallclockTime, eligibilityTraceDecayFactor);

        updateMemory();
    }

    public void updateMemory() {
        eligibilityTrace.limitMemory();
        limitMemory();

        updateSalienceMass();
    }

    private double salienceMass = 0.0;

    public void updateSalienceMass() {
        salienceMass = 0.0;

        for(ConceptWithSalience iElement : termWithHeatByTerm.values()) {
            salienceMass += iElement.salience;
        }
    }

    public void limitMemory() {
        Collections.sort(sortedByHeat, (s1, s2) -> s1.salience < s2.salience ? 1 : -1);

        // limit memory

        int maxSize = 500; // config

        while(sortedByHeat.size() > maxSize) {
            ConceptWithSalience current = sortedByHeat.get(maxSize);
            termWithHeatByTerm.remove(current.sentenceTerm);
            sortedByHeat.remove(maxSize);
        }


        int here = 5;
    }

    /**
     * returns the most recent events
     * @return
     */
    // is used in decision making only
    public List<Task> retSeqCurrent() {
        List<Task> mostRecentEvents = new ArrayList<>();


        int traceMostRecentEventHorizonItems = 2; // config - how many most recent items from the "event trace" are taken into account
                                                  // has a low value because pong seems to have issues with high values (like for ex 30)

        for(int idx=Math.max(eligibilityTrace.eligibilityTrace.size()-traceMostRecentEventHorizonItems, 0);idx<eligibilityTrace.eligibilityTrace.size();idx++) {
            EligibilityTrace.EligibilityTraceItem eventTraceItem = eligibilityTrace.eligibilityTrace.get(idx);
            mostRecentEvents.addAll(eventTraceItem.events);
        }

        return mostRecentEvents;
    }

    public void generalInferenceGenerateTemporalConclusions(Nar nar, Memory mem, long time, Parameters narParameters) {
        // select events which happened recently
        TaskPair sentencePair = generalInferenceSampleSentence(mem);
        if (sentencePair == null) {
            return; // we need two events to reason about
        }
        Task
            eventA = sentencePair.a,
            eventB = sentencePair.b,
            eventMiddle = sentencePair.middleEvent;

        if (eventA == null || eventB == null) {
            return; // we need two events to reason about
        }

        if (eventA.sentence.term.equals(eventB.sentence.term)) {
            return; // no need to reason about the same event happening at the same time
        }

        // must not overlap
        if (Stamp.baseOverlap(eventA.sentence.stamp, eventB.sentence.stamp)) {
            return;
        }


        // debugging
        if(DEBUG_TEMPORALCONTROL) System.out.println("select event pair a = "+eventA);
        if(DEBUG_TEMPORALCONTROL) System.out.println("select event pair b = "+eventB);
        if(DEBUG_TEMPORALCONTROL) System.out.println("select event pair middle = "+eventMiddle);

        List<Sentence> conclusionSentences = new ArrayList<>();

        { // preprocess and stuff it into deriver
            Sentence premiseEventASentence = eventA.sentence;
            Sentence premiseEventBSentence = eventB.sentence;

            if (eventMiddle != null) {
                // TODO< do we randomly select if we want to use the middle event? >

                // build sequence of eventA and middle event
                if (!Stamp.baseOverlap(premiseEventASentence.stamp, eventMiddle.sentence.stamp)) {
                    premiseEventASentence = buildSequence(premiseEventASentence, eventMiddle.sentence, time, narParameters);

                    if (!(premiseEventBSentence.term instanceof Operation)) {
                        int here42 = 6;
                    }

                    int debug42 = 6;
                }
            }


            // stuff it all into the deriver
            mem.trieDeriver.derive(premiseEventASentence, premiseEventBSentence, conclusionSentences, time, narParameters);
        }

        if (conclusionSentences.size() > 0) {
            int here = 6;
        }

        // transform conclusions into valid forms for OpenNARS
        for (int idx=0;idx<conclusionSentences.size();idx++) {
            Term conclusionTerm = conclusionSentences.get(idx).term;

            Implication impl = (Implication)conclusionTerm;

            if (impl == null) {
                continue;
            }

            if (!checkImplSeqForm(impl)) {
                continue; // can't fold, this is fine
            }

            Term subj = foldSeqFormOfImplSeqIntoSeqRec(impl.term[0]);
            Term pred = impl.term[1];

            conclusionTerm = new Implication(new Term[]{subj, pred}, impl.getTemporalOrder());

            if (!(""+conclusionTerm).equals(""+conclusionSentences.get(idx).term)) {
                int here42 = 6;
            }

            conclusionSentences.set(idx, overrideTermOfSentence(conclusionSentences.get(idx), conclusionTerm));
        }

        // filter out not allowed conclusions (for OpenNARS)

        for(int idx=conclusionSentences.size()-1;idx>=0;idx--) {
            Term conclusionTerm = conclusionSentences.get(idx).term;

            boolean accept = true; // is the conclusion accepted

            { // check for "X =/> (&/, ...)"
                if (
                    ( conclusionTerm instanceof Implication && conclusionTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD )
                ) {

                    Term rootImplPred = ((Implication)conclusionTerm).term[1];

                    if (
                        ( (rootImplPred instanceof CompoundTerm && (rootImplPred.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) )
                    ) {
                        accept = false; // ignored
                    }
                }
            }

            {
                if (
                    ( conclusionTerm instanceof Implication && conclusionTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD )
                ) {

                    Term rootImplSubj = ((Implication)conclusionTerm).term[0];
                    Term rootImplPred = ((Implication)conclusionTerm).term[1];

                    // don't allow ops as predicate of the impl
                    if (rootImplPred instanceof Operation) {
                        accept = false;
                    }

                    /* commented because we have to allow for (&/, X, Z) =/> Y for prediction
                    { // only allow "<(&/, A..., OP) =/> X>
                        if (
                            ( (rootImplSubj instanceof CompoundTerm && (rootImplSubj.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) )
                        ) {
                            CompoundTerm seq = (CompoundTerm)rootImplSubj;

                            Term lastTermOfSeq = null;
                            // scan for last term of seq
                            for(int idxSeq=seq.term.length-1;idxSeq>=0;idxSeq--) {
                                if (!(seq.term[idxSeq] instanceof Interval)) {
                                    lastTermOfSeq = seq.term[idxSeq];
                                    break;
                                }
                            }

                            if (!(lastTermOfSeq instanceof Operation)) {
                                accept = false; // ignored
                            }

                            // TODO< check for to complicated sequences >
                        }
                    }
                    */

                    { // disallow "<(&/, OP, +T) =/> X>
                        if (
                            ( (rootImplSubj instanceof CompoundTerm && (rootImplSubj.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) )
                        ) {
                            CompoundTerm seq = (CompoundTerm)rootImplSubj;

                            if (seq.term.length == 2 && seq.term[0] instanceof Operation && seq.term[1] instanceof Interval) {
                                accept = false;
                            }
                        }
                    }
                }
            }

            {
                if (
                    ( conclusionTerm instanceof Implication && conclusionTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD )
                ) {

                    Term rootImplSubj = ((Implication)conclusionTerm).term[0];
                    Term rootImplPred = ((Implication)conclusionTerm).term[1];

                    // don't allow ops as predicate of the impl
                    if (rootImplPred instanceof Operation) {
                        accept = false;
                    }

                    { // disallow "<(&/, X...) =/> X> with a length > 4
                        if (
                            ( (rootImplSubj instanceof CompoundTerm && (rootImplSubj.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) )
                        ) {
                            CompoundTerm seq = (CompoundTerm)rootImplSubj;

                            if (seq.term.length > 4) {
                                accept = false;
                            }
                        }
                    }
                }
            }


            // TESTING TESTING TESTING
            // FILTER FOR TESTING IN PONG
            {
                if ((""+conclusionTerm).contains("<(&/,(^")) {
                    accept = false; // don't allow op op nonsense
                }
            }

            if (!accept) {
                // remove
                conclusionSentences.remove(idx);
            }
        }


        // debugging
        for (Sentence iDerivedConclusion : conclusionSentences) {
            System.out.println("derived after transform = " + iDerivedConclusion.toString(nar, true));

            if ((""+iDerivedConclusion).contains("=/> <{SELF} --> [good]>>.")) {
                int debug42 = 6;
            }
        }

        if (conclusionSentences.size() > 0) {
            int here42 = 6;
        }

        final DerivationContext nal = new DerivationContext(mem, narParameters, nar);

        // add results to memory
        {
            for(Sentence iConclusionSentence : conclusionSentences) {
                BudgetValue budget = new BudgetValue(0.9f, 0.5f, 0.5f, nal.narParameters);

                // we need to eternalize the sentence
                // TODO< do it the proper way with calculation of TV >
                Stamp eternalizedStamp = iConclusionSentence.stamp.clone();
                eternalizedStamp.setEternal();
                Sentence eternalizedSentence = new Sentence(iConclusionSentence.term, iConclusionSentence.punctuation, iConclusionSentence.truth, eternalizedStamp);

                Task createdTask = new Task(
                    eternalizedSentence,
                    budget,
                    Task.EnumType.DERIVED
                );

                mem.addNewTask(createdTask, "Derived");
            }
        }


        // add results to trace
        for(Sentence iDerivedSentence : conclusionSentences) {
            // TODO< derive budget somehow >
            BudgetValue budget = new BudgetValue(0.9f, 0.5f, 0.5f, nal.narParameters);

            // we need to create a task for the sentence
            Task createdTask = new Task(
                iDerivedSentence,
                budget,
                Task.EnumType.DERIVED
            );

            eligibilityTrace.addEvent(createdTask);
        }
    }

    private static Sentence overrideTermOfSentence(Sentence sentence, Term term) {
        Sentence result = new Sentence(term, sentence.punctuation, sentence.truth, sentence.stamp);
        result.setRevisible(sentence.getRevisible());
        result.producedByTemporalInduction = sentence.producedByTemporalInduction;
        return result;
    }

    private static Sentence buildSequence(Sentence a, Sentence b, long time, Parameters narParameters) {
        assert a.getOccurenceTime() < b.getOccurenceTime();

        if (!(a.getOccurenceTime() < b.getOccurenceTime())) {
            int here = 5;
        }

        long occTimeDiff = b.getOccurenceTime() - a.getOccurenceTime();

        Term conclusionTerm = DeriverHelpers.make("&/",a.term,new Interval(occTimeDiff),b.term);
        Stamp stamp = new Stamp(a.stamp, b.stamp, time, narParameters); // merge stamps
        TruthValue tv = TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INDUCTION, a.truth, b.truth, narParameters);
        return new Sentence(conclusionTerm, '.', tv, stamp);
    }

    private static class TaskPair {
        public Task a;
        public Task b;

        public Task middleEvent = null; // event between the two events

        public TaskPair(Task a, Task b) {
            this.a = a;
            this.b = b;
        }
    }

    public boolean DEBUG_TEMPORALCONTROL = false;

    private TaskPair generalInferenceSampleSentence(Memory mem) {


        double selectedSalience = mem.randomNumber.nextDouble() * salienceMass;

        double salienceAccu = 0.0;
        for(ConceptWithSalience iConceptWithSalience : sortedByHeat) {
            salienceAccu += iConceptWithSalience.salience;
            if(salienceAccu > selectedSalience) {
                Concept primarySelectedConcept = mem.concept(iConceptWithSalience.sentenceTerm);
                if (primarySelectedConcept == null) {
                    return null;
                }

                {
                    List<EligibilityTrace.EligibilityTraceItem> etItemsByTerm = eligibilityTrace.eligibilityTraceItemsByTerm.get(""+primarySelectedConcept.term);

                    if (etItemsByTerm == null) {
                        return null; // no events to sample from
                    }

                    if ((""+primarySelectedConcept.term).equals("<{SELF} --> [good]>")) {
                        int herehb = 55; // debug
                    }

                    // we need to compute the mass for a fair sampling
                    double decayMass = 0.0;
                    for (EligibilityTrace.EligibilityTraceItem iItem : etItemsByTerm) {
                        decayMass += iItem.decay;
                    }

                    if(DEBUG_TEMPORALCONTROL) System.out.println("decay mass of " + primarySelectedConcept.term + " = " + decayMass);

                    { // sample primary item
                        double selectedMass = mem.randomNumber.nextDouble() * decayMass;

                        double massAccu = 0.0;
                        EligibilityTrace.EligibilityTraceItem selectedPrimaryEtItem = null;
                        for (EligibilityTrace.EligibilityTraceItem iItem : etItemsByTerm) {
                            massAccu += iItem.decay;
                            if (massAccu > selectedMass) {
                                selectedPrimaryEtItem = iItem;
                                break;
                            }
                        }

                        if (selectedPrimaryEtItem == null) {
                            return null;
                        }



                        List<Task> primarySelectionCandidateEvents = new ArrayList<>();

                        // filter for all events where the term is equal to the term of the primary selected concept
                        for (Task iEvent : selectedPrimaryEtItem.events) {
                            if (iEvent.sentence.term.equals(primarySelectedConcept.term)) {
                                primarySelectionCandidateEvents.add(iEvent);
                            }
                        }


                        int idx = mem.randomNumber.nextInt(primarySelectionCandidateEvents.size());
                        Task selectedPrimaryEvent = primarySelectionCandidateEvents.get(idx);


                        { // select secondary event
                            // TODO< compute neightbor salience with the multiplication of the salience of neightbor ET items with the exp based kernel >

                            long primaryEventOccTime = selectedPrimaryEvent.sentence.getOccurenceTime();

                            Integer primaryEventIdx = eligibilityTrace.calcIdxOfItemWithClosestTime(primaryEventOccTime);

                            if(DEBUG_TEMPORALCONTROL) System.out.println("primary event idx = " + primaryEventIdx);
                            if(DEBUG_TEMPORALCONTROL) System.out.println("primary event time = " + primaryEventOccTime);

                            double decayFactorSecondary = 0.01; // config


                            if (primaryEventIdx == null) {
                                // ignore if it is null
                            }
                            else {
                                int neightborEventWindowSize = 500; // config

                                int neightborMinIdx = primaryEventIdx - neightborEventWindowSize;
                                int neightborMaxIdx = primaryEventIdx + neightborEventWindowSize;
                                neightborMinIdx = Math.max(neightborMinIdx, 0);
                                neightborMaxIdx = min(neightborMaxIdx, eligibilityTrace.eligibilityTrace.size());


                                // compute accumated mass
                                double windowedSalienceAccu = 0.0;
                                for(int idx2=neightborMinIdx;idx2<neightborMaxIdx;idx2++) {
                                    EligibilityTrace.EligibilityTraceItem traceItem = eligibilityTrace.eligibilityTrace.get(idx2);

                                    long timeDiff = abs(primaryEventOccTime - traceItem.retOccurenceTime());
                                    double expWindow = Math.exp(-timeDiff * decayFactorSecondary); // config

                                    double mulSalience = expWindow * traceItem.decay; // multiply salience with window to get "windowed" salience

                                    windowedSalienceAccu += mulSalience;
                                }

                                if(DEBUG_TEMPORALCONTROL) System.out.println("secondary mass accu = " + windowedSalienceAccu);


                                // sample
                                double selectedSalience2 = mem.randomNumber.nextDouble() * windowedSalienceAccu;
                                double selectionMassAccu = 0.0;

                                EligibilityTrace.EligibilityTraceItem secondaryTraceItem = null;

                                Task middleEvent = null; // event in the middle between the two events

                                float middleEventMustBeOpPropability = 0.5f; // config
                                boolean middleEventMustBeOp = mem.randomNumber.nextFloat() > middleEventMustBeOpPropability; // must the middle event be an op or can it be a normal event?

                                for(int idx2=neightborMinIdx;idx2<neightborMaxIdx;idx2++) {
                                    EligibilityTrace.EligibilityTraceItem traceItem = eligibilityTrace.eligibilityTrace.get(idx2);

                                    long timeDiff = abs(primaryEventOccTime - traceItem.retOccurenceTime());
                                    double expWindow = Math.exp(-timeDiff * decayFactorSecondary); // config

                                    double mulSalience = expWindow * traceItem.decay; // multiply salience with window to get "windowed" salience

                                    selectionMassAccu += mulSalience;

                                    if (selectionMassAccu > selectedSalience2) {
                                        int secondaryEventIdx = idx2;

                                        if(DEBUG_TEMPORALCONTROL) System.out.println("secondary event idx = " + secondaryEventIdx);
                                        if(DEBUG_TEMPORALCONTROL) System.out.println("secondary event time = " + traceItem.retOccurenceTime());

                                        { // choose random middle event
                                            // TODO< do we need to bias it to choosing ops here? >

                                            int idxDiff = abs(secondaryEventIdx - primaryEventIdx);
                                            if (idxDiff > 1) {
                                                // collect possible middle candidate events which are ops
                                                // must be ops because OpenNARS prefers "(&/, A, OP) =/> B"
                                                List<Task> middleEventCandidates = new ArrayList<>();
                                                for(int idx3=min(primaryEventIdx, secondaryEventIdx)+1;idx3<max(primaryEventIdx, secondaryEventIdx);idx3++) {
                                                    EligibilityTrace.EligibilityTraceItem additionalEventTraceItem = eligibilityTrace.eligibilityTrace.get(idx3);

                                                    // filter for events which are ops
                                                    for(Task iEvent : additionalEventTraceItem.events) {
                                                        if (!middleEventMustBeOp || iEvent.sentence.term instanceof Operation) {
                                                            middleEventCandidates.add(iEvent);
                                                        }
                                                    }
                                                }

                                                // select middle candidate event
                                                if (middleEventCandidates.size() > 0) {
                                                    int middleEventCandidateIdx = mem.randomNumber.nextInt(middleEventCandidates.size());
                                                    middleEvent = middleEventCandidates.get(middleEventCandidateIdx);
                                                }

                                                /* commented because old way which is not biased towards OPS

                                                int additionalEventIdx = min(primaryEventIdx, secondaryEventIdx)+1 + mem.randomNumber.nextInt(idxDiff-1);

                                                EligibilityTrace.EligibilityTraceItem additionalEventTraceItem = eligibilityTrace.eligibilityTrace.get(additionalEventIdx);

                                                if (additionalEventTraceItem.events.size() > 0) {
                                                    // select random event as middle event

                                                    int eventIdx = mem.randomNumber.nextInt(additionalEventTraceItem.events.size());
                                                    middleEvent = additionalEventTraceItem.events.get(eventIdx);


                                                    // check if it is an op, ignore it if this is not the case
                                                    if (!(middleEvent.term instanceof Operation)) {
                                                        middleEvent = null;
                                                    }
                                                }
                                                */
                                            }
                                        }

                                        secondaryTraceItem = traceItem;
                                        break;
                                    }

                                }

                                if (secondaryTraceItem == null) {
                                    return null; // can't find a secondary traceitem to select for
                                }


                                // now we need to select the secondary event from the trace item
                                // TODO< should we ensure that it is not an operation ? >

                                //  select random event as secondary
                                if (secondaryTraceItem.events.size() == 0) {
                                    return null; // shouldn't happen
                                }

                                {
                                    int secondaryEventIdx = mem.randomNumber.nextInt(secondaryTraceItem.events.size());
                                    Task selectedSecondaryEvent = secondaryTraceItem.events.get(secondaryEventIdx);

                                    // sort events
                                    Task eventA = selectedPrimaryEvent;
                                    Task eventB = selectedSecondaryEvent;

                                    if (eventA.sentence.getOccurenceTime() > eventB.sentence.getOccurenceTime()) { // do we need to swap?
                                        Task temp = eventA;
                                        eventA = eventB;
                                        eventB = temp;
                                    }

                                    TaskPair sentencePair = new TaskPair(eventA, eventB);
                                    sentencePair.middleEvent = middleEvent;

                                    if (middleEvent != null) {
                                        int here = 5;
                                    }

                                    return sentencePair;

                                }
                            }


                        }
                    }
                }

            }
        }

        return null;
    }

    // is in "(&/, A...) =/> B" form?
    static private boolean checkImplSeqForm(Term term) {
        if (!(term instanceof Implication) || term.getTemporalOrder() != TemporalRules.ORDER_FORWARD) {
            return false;
        }

        Term rootConjSubj = ((Implication)term).term[0]; // fetch "(&/, A...)"

        return rootConjSubj instanceof Conjunction && (rootConjSubj.getTemporalOrder() == TemporalRules.ORDER_FORWARD);
    }


    // recursivly fold terms of the form  (&/, A...) =/> B into (&/, A..., B)
    // does handle sequences fine
    static private Term foldSeqFormOfImplSeqIntoSeqRec(Term term) {
        if (term instanceof Conjunction && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
            // is in "(&/ A...)" form

            Conjunction seq = (Conjunction)term;

            Term[] transformed = new Term[seq.term.length];
            for(int idx=0;idx<seq.term.length;idx++) {
                transformed[idx] = foldSeqFormOfImplSeqIntoSeqRec(seq.term[idx]);
            }

            return Conjunction.make(transformed, seq.temporalOrder);
        }
        else if (checkImplSeqForm(term)) {
            // is in "(&/, A...) =/> B" form

            return foldImplSeqIntoSeqRec(term);
        }

        return term; // can't transform
    }

    // folds (&/, A...) =/> B into (&/, A..., B) recursivly
    static private Term foldImplSeqIntoSeqRec(Term term) {
        if ((""+term).contains("=/>")) {
            int debug42 = 6;
        }

        if (!checkImplSeqForm(term)) {
            return term; // we don't need to transform it
        }

        Term rootConjSubj = ((Implication)term).term[0]; // fetch "(&/, A...)"

        Term[] resultTerms = new Term[((CompoundTerm) rootConjSubj).term.length + 1];
        // call recursivly
        for(int idx=0;idx<((CompoundTerm) rootConjSubj).term.length;idx++) {
            Term iteratedTerm = ((CompoundTerm) rootConjSubj).term[idx];
            resultTerms[idx] = foldSeqFormOfImplSeqIntoSeqRec(iteratedTerm);
        }

        resultTerms[resultTerms.length-1] = ((Implication)term).term[1]; // "B" is last

        Term result = Conjunction.make(resultTerms, TemporalRules.ORDER_FORWARD);
        return result;
    }



    public static class ConceptWithSalience {
        public Term sentenceTerm;

        public double salience = 0;

        public ConceptWithSalience(Term sentenceTerm) {
            this.sentenceTerm = sentenceTerm;
        }
    }

    public Map<Term, ConceptWithSalience> termWithHeatByTerm = new HashMap<>();

    public List<ConceptWithSalience> sortedByHeat = new ArrayList<>();

    public EligibilityTrace eligibilityTrace = new EligibilityTrace();

    public static class EligibilityTrace {

        public int maxLength = 10000; // config

        public List<EligibilityTraceItem> eligibilityTrace = new ArrayList<>(); // sorted by occurence time of events of the items
        public Map<Long, EligibilityTraceItem> eligibilityTraceItemsByTime = new HashMap<>();

        // by string of term
        public Map<String, List<EligibilityTraceItem>> eligibilityTraceItemsByTerm = new HashMap<>();

        /**
         * tries to add the event to the trace, checks if it already exists and ignores it if so
         *
         * @param event
         */
        public void addEvent(Task event) {
            assert !event.sentence.isEternal() : "must be event";

            if (event.sentence.isEternal()) {
                int debugMe = 42;
            }

            if (hasItemByOccurenceTime(event.sentence.getOccurenceTime())) {
                EligibilityTraceItem item = retItemByOccurenceTime(event.sentence.getOccurenceTime());
                for(final Task iEventOfItem : item.events) {
                    if (iEventOfItem.sentence.equals(event.sentence)) {
                        return; // don't add it if it already exists
                    }
                }

                item.events.add(event);

                updateEtItemByTerm(event.sentence.term, item);
            }
            else {
                // doesn't exist, we need to create a eligibility trace item and add it
                EligibilityTraceItem createdItem = new EligibilityTraceItem(event);
                addItem(createdItem);
            }
        }

        public Integer calcIdxOfItemWithClosestTime(long occTime) {
            // binary search for index to put in

            int idxMin = 0;
            int idxMax = eligibilityTrace.size()-1;


            for(;;) {
                //System.out.println("---");

                //System.out.println("min " + idxMin);
                //System.out.println("max " + idxMax);

                //System.out.println("min t = " + eligibilityTrace.get(idxMin).retOccurenceTime());
                //System.out.println("max t = " + eligibilityTrace.get(idxMax).retOccurenceTime());
                //System.out.println("ins t = " + item.retOccurenceTime());

                int here = 5;

                if(idxMin == idxMax-1) {
                    return null; // didn't find item with the exact occurence time
                }

                int idxMiddle = idxMin + (idxMax-idxMin)/2;

                long timeMiddle = eligibilityTrace.get(idxMiddle).retOccurenceTime();
                if (timeMiddle < occTime) {
                    idxMin = idxMiddle;
                    continue;
                }
                else if(timeMiddle > occTime) {
                    idxMax = idxMiddle;
                    continue;
                }
                else {
                    return idxMiddle;
                }
            }
        }


        public void addItem(EligibilityTraceItem item) {
            // put into map
            eligibilityTraceItemsByTime.put(item.retOccurenceTime(), item);

            // update ET for all terms
            // TODO< do recursivly >
            for(Task iTask : item.events) {
                updateEtItemByTerm(iTask.sentence.term, item);
            }



            // put into trace
            if (eligibilityTrace.size() == 0) {
                eligibilityTrace.add(item);
            }
            else if(eligibilityTrace.get(eligibilityTrace.size()-1).retOccurenceTime() < item.retOccurenceTime()) {
                eligibilityTrace.add(item);
            }
            else {
                // binary search for index to put in

                int idxMin = 0;
                int idxMax = eligibilityTrace.size()-1;


                for(;;) {
                    //System.out.println("---");

                    //System.out.println("min " + idxMin);
                    //System.out.println("max " + idxMax);

                    //System.out.println("min t = " + eligibilityTrace.get(idxMin).retOccurenceTime());
                    //System.out.println("max t = " + eligibilityTrace.get(idxMax).retOccurenceTime());
                    //System.out.println("ins t = " + item.retOccurenceTime());

                    int here = 5;

                    if(idxMin == idxMax-1) {
                        eligibilityTrace.add(idxMin+1, item); // +1 is important to insert it behind it!
                        break;
                    }

                    int idxMiddle = idxMin + (idxMax-idxMin)/2;

                    long timeMiddle = eligibilityTrace.get(idxMiddle).retOccurenceTime();
                    if (timeMiddle < item.retOccurenceTime()) {
                        idxMin = idxMiddle;
                        continue;
                    }
                    else if(timeMiddle > item.retOccurenceTime()) {
                        idxMax = idxMiddle;
                        continue;
                    }
                    else {
                        return; // internal error - found item with the same time
                    }
                }
            }


        }

        public boolean hasItemByOccurenceTime(long time) {
            return eligibilityTraceItemsByTime.containsKey(time);
        }

        // null values must not be relied upon
        public EligibilityTraceItem retItemByOccurenceTime(long time) {
            return eligibilityTraceItemsByTime.get(time);
        }

        public void limitMemory() {
            while(eligibilityTrace.size() > maxLength) {
                { // remove ET item by term
                    EligibilityTraceItem item = eligibilityTrace.get(0);

                    // TODO< recurse recursivly >
                    for(Task iEvent : item.events) { // iterate over all terms for removal
                        removeEtItemByTerm(iEvent.sentence.term, item);
                    }
                }


                long timeToRemove = eligibilityTrace.get(0).retOccurenceTime();
                eligibilityTraceItemsByTime.remove(timeToRemove);
                eligibilityTrace.remove(0);
            }
        }

        public void updateDecay(long wallclockTime, double decayFactor) {
            for(EligibilityTraceItem iItem : eligibilityTrace) {
                iItem.updateDecay(wallclockTime, decayFactor);
            }
        }

        /**
         * adds the term to the lookup table of the ET-items by term
         * is not recursive
         * @param term
         * @param item
         */
        public void updateEtItemByTerm(Term term, EligibilityTraceItem item) {
            if (eligibilityTraceItemsByTerm.containsKey(""+term)) {
                List<EligibilityTraceItem> items = eligibilityTraceItemsByTerm.get(""+term);

                // search for term, return if found because we don't need to add the term
                for(EligibilityTraceItem iItem : items) {
                    if (iItem.equals(item)) {
                        return; // we don't want to add the same item
                    }
                }
            }
            else {
                List<EligibilityTraceItem> items = new ArrayList<>();
                eligibilityTraceItemsByTerm.put(""+term, items);
            }

            // add it if we are here
            List<EligibilityTraceItem> arr = eligibilityTraceItemsByTerm.get(""+term);
            arr.add(item);

            if (arr.size() >= 2) {
                int debugMe = 5;
            }

            eligibilityTraceItemsByTerm.remove(""+term);
            eligibilityTraceItemsByTerm.put(""+term, arr);

            int here = 5;
        }

        public void removeEtItemByTerm(Term term, EligibilityTraceItem item) {
            if (!eligibilityTraceItemsByTerm.containsKey(term)) {
                return; // we can safely ignore it
            }

            eligibilityTraceItemsByTerm.get(term).remove(item);
            if (eligibilityTraceItemsByTerm.get(term).size() == 0) {
                eligibilityTraceItemsByTerm.remove(term); // we can remove it
            }
        }

        public static class EligibilityTraceItem {
            public List<Task> events = new ArrayList<>(); // concurrent events

            public double decay = 1.0;

            public EligibilityTraceItem(Task event) {
                assert !event.sentence.isEternal();

                if (event.sentence.isEternal()) {
                    int debugMe = 42;
                }

                this.events.add(event);
            }

            public long retOccurenceTime() {
                return events.get(0).sentence.getOccurenceTime();
            }

            public void updateDecay(long wallclockTime, double decayFactor) {
                long diff = wallclockTime - retOccurenceTime();
                decay = Math.exp(-diff*decayFactor);
            }
        }
    }
}
