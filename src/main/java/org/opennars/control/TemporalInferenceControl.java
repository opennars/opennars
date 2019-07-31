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
package org.opennars.control;

import org.opennars.entity.*;
import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.io.Symbols;
import org.opennars.language.*;
import org.opennars.main.Nar;
import org.opennars.main.Parameters;
import org.opennars.operator.Operation;
import org.opennars.storage.Memory;

import javax.swing.plaf.nimbus.State;
import java.util.*;

import static java.lang.Long.max;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static org.opennars.inference.BudgetFunctions.truthToQuality;
import static org.opennars.inference.DeriverHelpers.calcSeqTime;

/**
 *
 * @author Patrick Hammer
 * @author Robert Wünsche
 */
public class TemporalInferenceControl {

    public double heatUp = 0.05; // config

    public double inferencesPerCycle = 0.15; // config

    public double novelityThreshold = 0.5;

    public int sortedByHeatMaxSize = 500; // config - memory size

    private DerivationFilter derivationFilter = new DerivationFilter();

    public boolean DEBUG_TEMPORALCONTROL = false;
    public boolean DEBUG_TEMPORALCONTROL_PREMISESELECTION = false;
    public boolean DEBUG_TEMPORALCONTROL_DERIVATIONS = true;


    public static List<Task> proceedWithTemporalInduction(final Sentence newEvent, final Sentence stmLast, final Task controllerTask, final DerivationContext nal, final boolean SucceedingEventsInduction, final boolean addToMemory, final boolean allowSequence) {
        
        if(SucceedingEventsInduction && !controllerTask.isElemOfSequenceBuffer()) { //todo refine, add directbool in task
            return null;
        }
        if (newEvent.isEternal() || !controllerTask.isInput()) {
            return null;
        }
        /*if (equalSubTermsInRespectToImageAndProduct(newEvent.term, stmLast.term)) {
            return false;
        }*/
        
        if(newEvent.punctuation!=Symbols.JUDGMENT_MARK || stmLast.punctuation!=Symbols.JUDGMENT_MARK)
            return null; //temporal inductions for judgements only
        
        nal.setTheNewStamp(newEvent.stamp, stmLast.stamp, nal.time.time());
        nal.setCurrentTask(controllerTask);

        final Sentence previousBelief = stmLast;
        nal.setCurrentBelief(previousBelief);

        final Sentence currentBelief = newEvent;

        //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
        return TemporalRules.temporalInduction(currentBelief, previousBelief, nal, SucceedingEventsInduction, addToMemory, allowSequence);
    }

    public boolean immediateProcessEvent(Task task, DerivationContext ctx) {
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

    private void addToHeat(Task task) {
        if (!termWithHeatByTerm.containsKey(task.sentence.term)) {
            ConceptWithSalience createdConceptWithSalience = new ConceptWithSalience(task);
            termWithHeatByTerm.put(task.sentence.term, createdConceptWithSalience);
            sortedByHeat.add(createdConceptWithSalience);
        }
    }

    public boolean heatupInputEvent(Task task) {
        if (!isValidTask(task, "goal")) {
            return false;
        }

        addToHeat(task);

        termWithHeatByTerm.get(task.sentence.term).lastInputTask = task; // update with last task because we only care about the last task

        boolean isNovel = termWithHeatByTerm.get(task.sentence.term).salience < novelityThreshold;
        if (isNovel) {
            // we treat novel events differently by boosting it
            termWithHeatByTerm.get(task.sentence.term).salience = heatUp * 1000;

            if (DEBUG_TEMPORALCONTROL) System.out.println("event trace : novel event: "+task.sentence.term);
        }
        else {
            termWithHeatByTerm.get(task.sentence.term).salience += heatUp;
        }

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
            return false;
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
        while(sortedByHeat.size() > sortedByHeatMaxSize) {
            ConceptWithSalience current = sortedByHeat.get(sortedByHeatMaxSize);
            termWithHeatByTerm.remove(current.lastInputTask.sentence.term);
            sortedByHeat.remove(sortedByHeatMaxSize);
        }
    }

    /**
     * returns the most recent events
     * @return
     */
    // is used in decision making only
    public List<Task> retSeqCurrent() {
        List<Task> resultEvents = new ArrayList<>();


        int traceMostRecentEventHorizonItems = 2; // config - how many most recent items from the "event trace" are taken into account
        // has a low value because pong seems to have issues with high values (like for ex 30)

        // commented because it is the old way which only takes the last n timesteps into account
        for(int idx=Math.max(eligibilityTrace.eligibilityTrace.size()-traceMostRecentEventHorizonItems, 0);idx<eligibilityTrace.eligibilityTrace.size();idx++) {
            EligibilityTrace.EligibilityTraceItem eventTraceItem = eligibilityTrace.eligibilityTrace.get(idx);
            resultEvents.addAll(eventTraceItem.events);
        }

        return resultEvents;
    }

    public void generalInferenceGenerateTemporalConclusions(Nar nar, Memory mem, long time, Parameters narParameters) {
        if (eligibilityTrace.eligibilityTrace.size() == 0) {
            return; // special case
            // necessary to not disturb declarative NAL tests
        }

        List<Sentence> conclusionSentences = new ArrayList<>();

        int nInferences = 0;
        if (inferencesPerCycle < 1.0) {
            nInferences += (mem.randomNumber.nextDouble() < inferencesPerCycle)?1:0;
        }
        else {
            nInferences = (int)inferencesPerCycle;
        }


        for(int iInference=0; iInference<nInferences; iInference++) {

            for(boolean onlyInputEvents:new boolean[]{false, true}) { // necessary to bias it to valid seqs of event/ops which are not to complicated
                // select events which happened recently
                TaskPair sentencePair = generalInferenceSampleSentence(mem, onlyInputEvents);
                if (sentencePair == null) {
                    continue; // we need two events to reason about
                }
                Task
                    eventA = sentencePair.a,
                    eventB = sentencePair.b,
                    eventMiddle = sentencePair.middleEvent;

                // eventMiddle = null; // DEBUG DEBUG DEBUG - disable codepath for middle event

                if (eventA == null || eventB == null) {
                    continue; // we need two events to reason about
                }

                if (isOp(eventA.sentence.term) && isOp(eventB.sentence.term)) {
                    continue; // ignore it because op related don't leads to useful derivations
                }


                // must not overlap
                if (Stamp.baseOverlap(eventA.sentence.stamp, eventB.sentence.stamp)) {
                    continue;
                }
                if (eventMiddle != null && (Stamp.baseOverlap(eventA.sentence.stamp, eventMiddle.sentence.stamp))) {
                    continue;
                }
                if (eventMiddle != null && (Stamp.baseOverlap(eventB.sentence.stamp, eventMiddle.sentence.stamp))) {
                    continue;
                }


                // restrict types of premise events to avoid deriving nonsense
                if (!isValidForInference2(eventA.sentence.term, true) || !isValidForInference2(eventB.sentence.term, false)) {
                    continue;
                }
                if (eventMiddle != null && !isValidForInference2(eventMiddle.sentence.term, false)) {
                    continue;
                }


                // debugging
                {
                    String strOfPair = "(~ "+eventA.sentence.toString(nar, false)+(eventMiddle != null ? " ~ "+eventMiddle.sentence.toString(nar, false) : "") + " ~ " + eventB.sentence.toString(nar, false)+" ~)";
                    if(DEBUG_TEMPORALCONTROL_PREMISESELECTION) System.out.println("DEBUG event trace  |||  select events "+strOfPair);
                }

                // stuff it into deriver
                derive(nar, mem, time, narParameters, conclusionSentences, eventA, eventMiddle, eventB);
            }
        }


        if (conclusionSentences.size() > 0) {
            int here = 6;
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

            { // disallow seq of two ops, because we don't need to derive it
                if (
                    ( (conclusionTerm instanceof CompoundTerm && (conclusionTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) )
                ) {
                    CompoundTerm seq = (CompoundTerm) conclusionTerm;

                    if (seq.term.length == 2 && seq.term[0] instanceof Operation && seq.term[1] instanceof Operation) {
                        accept = false;
                    } else if (seq.term.length == 3 && seq.term[0] instanceof Operation && seq.term[1] instanceof Interval && seq.term[2] instanceof Operation) {
                        accept = false;
                    }
                }
            }

            { // disallow implication, because it doesn't make any sense
                if (
                    ( conclusionTerm instanceof Implication && conclusionTerm.getTemporalOrder() == TemporalRules.ORDER_BACKWARD )
                ) {
                    Term rootImplSubj = ((Implication) conclusionTerm).term[0];

                    // don't allow ops as subject of the impl
                    if (isOp(rootImplSubj)) {
                        accept = false;
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
                    if (isOp(rootImplPred)) {
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

            if (true) { // disallow implication with a op because it doesn't make any sense


                // disallow ^op =/> x and x =/> ^op and ^op1 =/> ^op2
                // disallow ^op =\> x and x =\> ^op and ^op1 =\> ^op2
                // disallow ^op =|> x and x =|> ^op and ^op1 =|> ^op2
                if (
                    (conclusionTerm instanceof Implication)
                ) {
                    Term rootImplSubj = ((Implication) conclusionTerm).term[0];
                    Term rootImplPred = ((Implication) conclusionTerm).term[1];

                    if (false) { //conclusionTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD && isOp(rootImplSubj) && !isOp(rootImplPred)) {
                        // special case which we must allow for NAL8
                        // disabled because it's not necessary
                    }
                    else {
                        int opCount = 0;
                        opCount += checkIsOpOrSeqWithFirstOp(rootImplSubj, true) ? 1 : 0;
                        opCount += checkIsOpOrSeqWithFirstOp(rootImplPred, true) ? 1 : 0;

                        if (opCount >= 1) {
                            accept = false;
                        }
                    }
                }
                // disallow x </> ^op
                if (
                    (conclusionTerm instanceof Equivalence && conclusionTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD)
                ) {
                    Term rootImplSubj = ((Statement) conclusionTerm).term[0];
                    Term rootImplPred = ((Statement) conclusionTerm).term[1];


                    int opCount = 0;
                    opCount += checkIsOpOrSeqWithFirstOp(rootImplPred, true) ? 1 : 0;

                    if (opCount >= 1) {
                        accept = false;
                    }
                }
            }

            // don't allow anything with more than one op
            if (countOps(conclusionTerm) > 1) {
                accept = false; // we don't allow more than one op in a term because OpenNARS can't handle it currently
            }

            // don't allow seq =/> and seq </> and =\> seq where the op isn't the last element of the seq
            if ((conclusionTerm instanceof Implication || conclusionTerm instanceof Equivalence) && conclusionTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                Term rootImplSubj = ((Statement)conclusionTerm).term[0];

                if (
                    ( (rootImplSubj instanceof CompoundTerm) && rootImplSubj.getTemporalOrder() == TemporalRules.ORDER_FORWARD)
                ) {
                    CompoundTerm seq = (CompoundTerm)rootImplSubj;

                    boolean isLastOp = checkSeqIsLastOp(seq);
                    if (checkHasOp(seq) && !isLastOp) {
                        accept = false; // don't allow
                    }
                }
            }


            if (conclusionTerm instanceof Implication && conclusionTerm.getTemporalOrder() == TemporalRules.ORDER_BACKWARD) {
                Term rootImplSubj = ((Implication)conclusionTerm).term[0];
                Term rootImplPred = ((Implication)conclusionTerm).term[1];

                if (
                    ( (rootImplPred instanceof CompoundTerm) && rootImplPred.getTemporalOrder() == TemporalRules.ORDER_FORWARD)
                ) {
                    CompoundTerm seq = (CompoundTerm)rootImplPred;

                    boolean isLastOp = checkSeqIsLastOp(seq);
                    if (checkHasOp(seq) && !isLastOp) {
                        accept = false; // don't allow
                    }
                }
            }


            // HACK ATTENTION - only allow  seq =/> , seq =|> and seq =\>
            if (   (""+conclusionTerm).contains("<|>")) {
                int here = 5;
            }

            if (true && accept) {
                if (conclusionTerm instanceof Implication)
                {
                    assert conclusionTerm.getTemporalOrder() != TemporalRules.ORDER_NONE && conclusionTerm.getTemporalOrder() != TemporalRules.ORDER_INVALID;

                    Term rootImplSubj = ((Implication)conclusionTerm).term[0];
                    Term rootImplPred = ((Implication)conclusionTerm).term[1];

                    if (
                        ( (rootImplSubj instanceof Conjunction) && rootImplSubj.getTemporalOrder() == TemporalRules.ORDER_FORWARD)
                    ) {
                        Conjunction seq = (Conjunction) rootImplSubj;

                        if (seq.term.length > 4) {
                            accept = false;
                        }

                        if (seq.term.length == 2 && seq.term[1] instanceof Interval) {
                            accept = false; // don't allow (&/, a, +t) =/> b
                        }

                        // TODO< put into function called checkConjunction(), checks if a conjunction is valid if it doesn't contain any statements
                        for(Term i:seq.term) {
                            if (i instanceof Statement && i.getTemporalOrder() != TemporalRules.ORDER_NONE) {

                            //if (i instanceof Equivalence || i instanceof Similarity || i instanceof Implication) {
                                accept = false; // obviously nonsense
                            }
                        }
                    }
                    else {
                        accept = false;
                    }
                }
                else if (conclusionTerm instanceof Conjunction && conclusionTerm.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT) {
                    // allow
                }
                else {
                    //System.out.println("NOT ALLOWED " + conclusionTerm);

                    //accept = false;
                }
            }

            if (!accept) {
                // remove
                conclusionSentences.remove(idx);
            }
        }





        { // ATTENTION MECHANISM - limit result size
            int limitResultByConfCount = 200; // number of conclusions ordered by conf
            conclusionSentences = calcTopSentencesByConf(conclusionSentences, nar.memory.randomNumber, limitResultByConfCount);
        }

        { // identify unique conclusions , necessary to not overwhelm the deriver
            HashMap<Integer, Sentence> uniqueConclusions = new HashMap<>();

            for(Sentence iConclusion : conclusionSentences) {
                if (!uniqueConclusions.containsKey(iConclusion.hashCode())) {
                    uniqueConclusions.put(iConclusion.hashCode(), iConclusion);
                }
            }

            conclusionSentences.clear();
            conclusionSentences.addAll(uniqueConclusions.values());
        }

        { // filter to allow only new conclusions
            List<Sentence> filteredConclusions = new ArrayList<>();
            for(Sentence iConclusion : conclusionSentences) {

                //System.out.println("==");
                //System.out.println("   "+iConclusion);
                //System.out.println("   "+iConclusion.hashCode());

                String asStr = iConclusion.term.toString() + iConclusion.truth.toString() + strOfStamp2(iConclusion.stamp);
                int hash = asStr.hashCode();

                if (!derivationFilter.contains(hash)) {
                    derivationFilter.pushLifo(hash);
                    //System.out.println("doesn't contain "+hash);
                    filteredConclusions.add(iConclusion);
                }
            }
            conclusionSentences = filteredConclusions;
        }


        // debugging
        for (Sentence iDerivedConclusion : conclusionSentences) {
            boolean DEBUG_TEMPORALCONTROL_DERIVATIONS_SHOWSTAMPS = true;
            if(DEBUG_TEMPORALCONTROL_DERIVATIONS) {
                String a = ""+iDerivedConclusion.term.toString();

                int x = (a).indexOf("=/>");

                if (true || x != -1) {
                    System.out.println("DEBUG event trace  |||   derived after transform = " + iDerivedConclusion.toString(nar, DEBUG_TEMPORALCONTROL_DERIVATIONS_SHOWSTAMPS));
                }

            }
        }

        if (conclusionSentences.size() > 0) {
            int here42 = 6;
        }


        // add results to memory
        {
            for(Sentence iConclusionSentence : conclusionSentences) {
                { // add non-eternalized
                    final float complexity = narParameters.COMPLEXITY_UNIT*iConclusionSentence.term.getComplexity();
                    float quality = truthToQuality(iConclusionSentence.truth);
                    float durability = 1.0f / complexity;
                    float priority = 0.5f;

                    if(iConclusionSentence.term instanceof Implication && (""+(iConclusionSentence.term)).contains("^")) {
                        int here = 1;
                    }

                    /*if (isImplSeqLastOp(iConclusionSentence.term)) {
                        durability *= 2.0f; // boost because it is in a "special" representation
                        durability = Math.min(1.0f, durability);
                    }
                    else {
                        // punish - is necessary because else Pong doesn't work
                        quality *= 0.05f;
                        durability *= 0.05f;
                        priority *= 0.03f;
                    }*/
                    BudgetValue budget = new BudgetValue(priority, durability, quality, narParameters);

                    Task createdTask = new Task(
                        iConclusionSentence,
                        budget,
                        Task.EnumType.DERIVED
                    );

                    mem.addNewTask(createdTask, "Derived");
                }

                { // add eternalized
                    // we need to eternalize the sentence
                    TruthValue eternalizedTv = TruthFunctions.eternalize(iConclusionSentence.truth.clone(), narParameters);
                    Stamp eternalizedStamp = iConclusionSentence.stamp.clone();
                    eternalizedStamp.setEternal();
                    Sentence eternalizedSentence = new Sentence(iConclusionSentence.term, iConclusionSentence.punctuation, eternalizedTv, eternalizedStamp);


                    final float complexity = narParameters.COMPLEXITY_UNIT*iConclusionSentence.term.getComplexity();
                    float quality = truthToQuality(eternalizedTv);
                    float durability = 1.0f / complexity;
                    float priority = 0.5f;

                    /*
                    if (isImplSeqLastOp(iConclusionSentence.term)) {
                        durability *= 2.0f; // boost because it is in a "special" representation
                        durability = Math.min(1.0f, durability);
                    }
                    else {
                        // punish - is necessary because else Pong doesn't work
                        quality *= 0.1f;
                        durability *= 0.1f;
                        priority *= 0.1f;
                    }*/
                    BudgetValue budget = new BudgetValue(priority, durability, quality, narParameters);

                    Task createdTask = new Task(
                        eternalizedSentence,
                        budget,
                        Task.EnumType.DERIVED
                    );

                    mem.addNewTask(createdTask, "Derived");
                }

            }
        }


        // add results to trace
        for(Sentence iDerivedSentence : conclusionSentences) {
            boolean allowAddToTrace = false;

            boolean isSeq = iDerivedSentence.term instanceof Conjunction && iDerivedSentence.term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;
            boolean isPar = iDerivedSentence.term instanceof Conjunction && iDerivedSentence.term.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT;
            boolean isPredImpl = iDerivedSentence.term instanceof Implication && iDerivedSentence.term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;

            if (isSeq || isPar) {
                allowAddToTrace = true;
            }
            // we need to allow =/> too because we need to build more complex impl seqs
            if (isPredImpl) {
                allowAddToTrace = true;
            }

            if (!allowAddToTrace) {
                continue;
            }

            BudgetValue budget = new BudgetValue(0.9f, 0.5f, 0.5f, narParameters);

            // we need to create a task for the sentence
            Task createdTask = new Task(
                iDerivedSentence,
                budget,
                Task.EnumType.DERIVED
            );

            eligibilityTrace.addEvent(createdTask);
        }
    }

    static private String strOfStamp2(Stamp stamp) {
        final int estimatedInitialSize = 10 * stamp.baseLength;

        final StringBuilder buffer = new StringBuilder(estimatedInitialSize);
        if (!stamp.isEternal()) {
            buffer.append('|').append(stamp.getOccurrenceTime());
        }
        buffer.append(' ').append(Symbols.STAMP_STARTER).append(' ');
        for (int i = 0; i < stamp.baseLength; i++) {
            buffer.append(stamp.evidentialBase[i].toString());
            if (i < (stamp.baseLength - 1)) {
                buffer.append(Symbols.STAMP_SEPARATOR);
            }
        }
        buffer.append(Symbols.STAMP_CLOSER).append(' ');

        return buffer.toString();
    }

    static private boolean checkHasOp(CompoundTerm seq) {
        for(Term iComponent:seq.term) {
            if (isOp(iComponent)) {
                return true;
            }
        }

        return false;
    }

    /**
     * is the last non-interval element of a seq a op?
     * @param seq
     * @return
     */
    static private boolean checkSeqIsLastOp(CompoundTerm seq) {
        assert seq.term.length >= 2; // we assume valid seq

        // we need this loop to skip over the intervals from behind
        for(int idx2=seq.term.length-1;idx2 >= 0;idx2--) {
            if (seq.term[idx2] instanceof Interval) {
                continue; // we have to skip intervals
            }

            return isOp(seq.term[idx2]);
        }
        return false;
    }

    /**
     * recursivly count the number of ops of a temporal term
     * @param term
     * @return
     */
    static private int countOps(Term term) {
        if (isOp(term)) {
            return 1;
        }

        if (term instanceof Implication && (term.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT || term.getTemporalOrder() == TemporalRules.ORDER_FORWARD || term.getTemporalOrder() == TemporalRules.ORDER_BACKWARD)) {
            Implication impl = (Implication)term;
            return countOps(impl.term[0]) + countOps(impl.term[1]);
        }
        else if (term instanceof Equivalence && (term.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT || term.getTemporalOrder() == TemporalRules.ORDER_FORWARD || term.getTemporalOrder() == TemporalRules.ORDER_BACKWARD)) {
            Equivalence equ = (Equivalence)term;
            return countOps(equ.term[0]) + countOps(equ.term[1]);
        }
        else if (term instanceof CompoundTerm && (term.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT || term.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
            CompoundTerm comp = (CompoundTerm)term;
            int c = 0;
            for(Term iComponent : comp.term) {
                c += countOps(iComponent);
            }
            return c;
        }

        return 0;
    }

    // used to blacklist temporal relationships which don't make sense
    /**
     * checks if it is a op or a seq where the first element is a op
     * @param term
     * @param seqAllowed is a sequence allowed (as the term or the first component of a seq)
     * @return
     */
    static private boolean checkIsOpOrSeqWithFirstOp(Term term, boolean seqAllowed) {
        if (term instanceof CompoundTerm && term.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT) {
            for(Term iComponent : ((CompoundTerm)term).term) {
                if (checkIsOpOrSeqWithFirstOp(iComponent, false)) {
                    return true;
                }
            }

            return false;
        }

        if (seqAllowed && term instanceof CompoundTerm && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
            return checkIsOpOrSeqWithFirstOp(((CompoundTerm)term).term[0], seqAllowed); // we care only about first component
        }

        return isOp(term);
    }

    // checks if it is in the form (&/, a, ^OP) =/> b
    // ex: (&/, a, ^OP, +t) =/> b returns true
    static private boolean isImplSeqLastOp(Term term) {
        if (term instanceof Implication && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
            Term rootImplSubj = ((Implication)term).term[0];

            if (rootImplSubj instanceof CompoundTerm && rootImplSubj.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                return checkSeqIsLastOp((CompoundTerm)rootImplSubj);
            }
            return false;
        }

        return false;
    }

    // derive conclusions from premises
    private static void derive(Nar nar, Memory mem, long time, Parameters narParameters, List<Sentence> conclusionSentences, Task premiseEventA, Task premiseEventMiddle, Task premiseEventB) {
        Sentence usedPremiseEventASentence = premiseEventA.sentence;
        Sentence premiseEventBSentence = premiseEventB.sentence;

        // we assume that event B happens either at the same time or after event A
        assert premiseEventBSentence.stamp.getOccurrenceTime() >= premiseEventBSentence.stamp.getOccurrenceTime() : "assumption of order of events violated";

        if (premiseEventMiddle != null) /* check if we have three premises and derive the results with special rules */ {
            // build sequence of eventA and middle event
            if (Stamp.baseOverlap(usedPremiseEventASentence.stamp, premiseEventMiddle.sentence.stamp)) {
                return;
            }

            if (checkOverlapInclusive(usedPremiseEventASentence, premiseEventMiddle.sentence) || checkOverlapInclusive(premiseEventBSentence, premiseEventMiddle.sentence)) {
                return; // must not overlap
            }

            Term seqTerm;
            Stamp seqStamp;
            TruthValue seqTv;
            { // build (&/, a, t, m, t)
                assert usedPremiseEventASentence.getOccurenceTime() < premiseEventMiddle.sentence.getOccurenceTime();

                long occTimeDiff = premiseEventMiddle.sentence.getOccurenceTime() - usedPremiseEventASentence.getOccurenceTime() ;

                seqTerm = Conjunction.make(new Term[]{usedPremiseEventASentence.term,new Interval(occTimeDiff),premiseEventMiddle.sentence.term}, TemporalRules.ORDER_FORWARD);
                seqStamp = new Stamp(usedPremiseEventASentence.stamp, premiseEventMiddle.sentence.stamp, time, narParameters); // merge stamps
                seqTv = TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INTERSECTION, usedPremiseEventASentence.truth, premiseEventMiddle.sentence.truth, narParameters);
                usedPremiseEventASentence = new Sentence(seqTerm, '.', seqTv, seqStamp);
            }



            int here6 = 1;
        }


        // we have two premises or three


        //int r=1;
        //if (r==1) return; // avoid binary premise selection for TESTING

        // stuff it all into the deriver
        //commented because we are using    mem.trieDeriver.derive(premiseEventASentence, premiseEventBSentence, conclusionSentences, time, nal, narParameters);

        { // implementation using TemporalRules.temporalInduction()
            Sentence currentBelief = premiseEventBSentence;
            Sentence previousBelief = usedPremiseEventASentence;

            if (currentBelief.getOccurenceTime() == previousBelief.getOccurenceTime() && isDeclarative(currentBelief.term) && isDeclarative(previousBelief.term)) { // are both declarative events and at the same time? then we need to put it into the ifnerence because we want parallel events
            }
            else if (checkOverlapInclusive(premiseEventB.sentence, premiseEventA.sentence)) {
                return; // don't allow overlapping events
            }

            boolean isPreviousSeq = previousBelief.term instanceof Conjunction && previousBelief.term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;
            boolean isPreviousPar = previousBelief.term instanceof Conjunction && previousBelief.term.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT;
            boolean isPreviousDeclarative = previousBelief.term.getTemporalOrder() == TemporalRules.ORDER_NONE;//isDeclarative(previousBelief.term);
            if (!(isPreviousPar||isPreviousSeq||isPreviousDeclarative)) {
                return; // must be restricted to avoid wrong derivations
            }


            // it is necessary to update the nal-context
            final DerivationContext nal = new DerivationContext(mem, narParameters, nar);
            nal.setTheNewStamp(premiseEventB.sentence.stamp, premiseEventA.sentence.stamp, nal.time.time());
            nal.setCurrentTask(premiseEventB);
            nal.setCurrentBelief(premiseEventA.sentence);
            List<Task> derivationsFromTemporalInduction = TemporalRules.temporalInduction(currentBelief, previousBelief, nal, false, false, true);

            // map to List of sentences
            for (final Task iDerivedTask : derivationsFromTemporalInduction) {
                if (!iDerivedTask.sentence.stamp.isEternal()) { // must be event because event trace derives only events
                    synchronized (conclusionSentences) {
                        conclusionSentences.add(iDerivedTask.sentence);
                    }
                }
            }
        }
    }

    private static boolean checkOverlapInclusive(Sentence a, Sentence b) {

        long aEndTime = a.getOccurenceTime();
        long aStartTime = a.getOccurenceTime();
        if (a.term instanceof Conjunction && a.term.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
            aStartTime = getStartimeOfSeq(a);
        }

        long bEndTime = b.getOccurenceTime();
        long bStartTime = b.getOccurenceTime();
        if (b.term instanceof Conjunction && b.term.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
            bStartTime = getStartimeOfSeq(b);
        }


        if (aStartTime == bStartTime) {
            return true;
        }
        if (aStartTime == bEndTime) {
            return true;
        }
        if (aEndTime == bStartTime) {
            return true;
        }
        if (aEndTime == bEndTime) {
            return true;
        }


        if (aStartTime <= bStartTime && bStartTime <= aEndTime) { // is bStart in the interval?
            return true;
        }
        if (aStartTime <= bEndTime && bEndTime <= aEndTime) { // is bEnd in the interval?
            return true;
        }

        if (bStartTime <= aStartTime && aStartTime <= bEndTime) { // is aStart in the interval?
            return true;
        }
        if (bStartTime <= aEndTime && aEndTime <= bEndTime) { // is aEnd in the interval?
            return true;
        }


        return false;
    }

    static private long getStartimeOfSeq(Sentence seq) {
        long sum = 0;
        for(Term iComponent : ((Conjunction)seq.term).term) {
            if (iComponent instanceof Interval) {
                sum += ((Interval)iComponent).time;
            }
        }
        return seq.getOccurenceTime() - sum;
    }

    static private boolean isValidForInference2(Term term, boolean isFirstEvent) {
        boolean isTemporalImpl = term instanceof Implication && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;
        boolean isSeq = term instanceof Conjunction && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;
        if (isSeq && isFirstEvent) {
            return true; // special case
        }
        if (isTemporalImpl && !isFirstEvent) {
            return true; // special case for
            // <(*,Self,key001) --> reachable>. :|:
            // 11
            // <(^pick,key001) =/> <(*,Self,key001) --> hold>>. :|:
            // |-
            // <(&/,<(*,Self,key001) --> reachable>,+11,(^pick,key001)) =/> <(*,Self,key001) --> hold>>. :!11: %1.00;0.45%
        }

        return isValidForInference(term);
    }

    static private boolean isValidForInference(Term term) {
        if (term instanceof Conjunction && term.getTemporalOrder() == TemporalRules.ORDER_NONE) {
            return true; // parallel events are always valid for inference
        }

        boolean isTemporalConj = term instanceof Conjunction && term.getTemporalOrder() != TemporalRules.ORDER_NONE; // may be a seq or par
        boolean isTemporalImpl = term instanceof Implication && term.getTemporalOrder() != TemporalRules.ORDER_NONE;
        boolean isTemporalEquiv = term instanceof Equivalence && term.getTemporalOrder() != TemporalRules.ORDER_NONE;

        if (isTemporalConj || isTemporalImpl || isTemporalEquiv) {
            return false;
        }

        return true;
    }

    /**
     * check if it is a declarative event, defined as NAL6 or below
     */
    private static boolean isDeclarative(Term term) {
        return
            term instanceof Inheritance ||
                term instanceof Similarity ||
                (term instanceof Implication && term.getTemporalOrder() == TemporalRules.ORDER_NONE) ||
                (term instanceof Equivalence && term.getTemporalOrder() == TemporalRules.ORDER_NONE);
    }

    /**
     * check if it is a op or a NAL2 form of a op
     * @param term
     * @return
     */
    private static boolean isOp(Term term) {
        if( term instanceof Operation ) {
            return true;
        }

        // check case when X --> ^Y
        if (term instanceof Inheritance) {
            Inheritance inh = (Inheritance)term;
            if (inh.getPredicate() instanceof Term && inh.getPredicate().name().length()>0 && inh.getPredicate().name().charAt(0)=='^') {
                return true;
            }
        }
        // check case when X <-> ^Y or ^Y <-> X
        if (term instanceof Similarity) {
            Similarity sim = (Similarity)term;

            if (sim.getPredicate() instanceof Term && sim.getPredicate().name().length()>0 && sim.getPredicate().name().charAt(0)=='^') {
                return true;
            }
            if (sim.getSubject() instanceof Term && sim.getSubject().name().length()>0 && sim.getSubject().name().charAt(0)=='^') {
                return true;
            }
            int here = 5;
        }

        return false;
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





    private TaskPair generalInferenceSampleSentence(Memory mem, boolean onlyInputEvents) {
        double selectedSalience = mem.randomNumber.nextDouble() * salienceMass;

        // do we sample the secondary event uniformly?
        boolean enableUniformSecondarySampling = mem.randomNumber.nextDouble() > 0.2; // config

        // sample a far smaller window when sampling uniformly
        int neightborEventWindowSize = enableUniformSecondarySampling ? 5 : 500; // config

        double salienceAccu = 0.0;
        for(ConceptWithSalience iConceptWithSalience : sortedByHeat) {
            salienceAccu += iConceptWithSalience.salience;
            if(salienceAccu > selectedSalience) {
                Concept primarySelectedConcept = mem.concept(iConceptWithSalience.lastInputTask.sentence.term);
                if (primarySelectedConcept == null) {
                    return null;
                }

                {
                    String etItemKey = ""+primarySelectedConcept.term;
                    List<EligibilityTrace.EligibilityTraceItem> etItemsByTerm = eligibilityTrace.eligibilityTraceItemsByTerm.get(etItemKey);

                    if (etItemsByTerm == null) {
                        return null; // no events to sample from
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

                        ////////////////// filter for all events where the term is equal to the term of the primary selected concept

                        // filter for all events where the primary selected concept is contained in the term
                        // this is necessary to match for ex: (&/, a, b) to b
                        for (Task iEvent : selectedPrimaryEtItem.events) {
                            // commented because it is the old code   if (CompoundTerm.replaceIntervals(iEvent.sentence.term).equals(primarySelectedConcept.term)) {
                            if ((""+CompoundTerm.replaceIntervals(iEvent.sentence.term)).contains(""+primarySelectedConcept.term)) {
                                if (onlyInputEvents && !iEvent.isInput()) {
                                    continue;
                                }

                                primarySelectionCandidateEvents.add(iEvent);
                            }
                        }


                        Task selectedPrimaryEvent = selectTaskByConf(mem, primarySelectionCandidateEvents);

                        Task selectedSecondaryEvent = null;
                        Task middleEvent = null; // event in the middle between the two events

                        // it is sometimes necessary to sample from the same occurence time
                        boolean sampleForSameOccurenceTime = selectedPrimaryEtItem.events.size() > 1 && mem.randomNumber.nextDouble() < 0.3;

                        if (sampleForSameOccurenceTime) {
                            // filter
                            List<Task> otherPossibleEvents = new ArrayList<>();
                            for (Task iEvent : selectedPrimaryEtItem.events) {
                                if (!iEvent.sentence.term.equals(selectedPrimaryEvent.sentence.term)) {
                                    if (onlyInputEvents && !iEvent.isInput()) {
                                        continue;
                                    }

                                    otherPossibleEvents.add(iEvent);
                                }
                            }

                            if (otherPossibleEvents.size() > 0) {
                                selectedSecondaryEvent = selectTaskByConf(mem, otherPossibleEvents);
                            }
                        }
                        else { // select secondary event
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


                                int neightborMinIdx = primaryEventIdx - neightborEventWindowSize;
                                int neightborMaxIdx = primaryEventIdx + neightborEventWindowSize;
                                neightborMinIdx = Math.max(neightborMinIdx, 0);
                                neightborMaxIdx = min(neightborMaxIdx, eligibilityTrace.eligibilityTrace.size());


                                // compute accumulated mass
                                double windowedSalienceAccu = 0.0;
                                for(int idx2=neightborMinIdx;idx2<neightborMaxIdx;idx2++) {
                                    EligibilityTrace.EligibilityTraceItem traceItem = eligibilityTrace.eligibilityTrace.get(idx2);

                                    long timeDiff = abs(primaryEventOccTime - traceItem.retOccurenceTime());
                                    double expWindow = Math.exp(-timeDiff * decayFactorSecondary); // config

                                    double mulSalience = expWindow * traceItem.decay; // multiply salience with window to get "windowed" salience
                                    if (enableUniformSecondarySampling) { // do we sample uniformly?
                                        mulSalience = 1.0;
                                    }

                                    windowedSalienceAccu += mulSalience;
                                }

                                if(DEBUG_TEMPORALCONTROL) System.out.println("secondary mass accu = " + windowedSalienceAccu);


                                // sample
                                double selectedSalience2 = mem.randomNumber.nextDouble() * windowedSalienceAccu;
                                double selectionMassAccu = 0.0;

                                EligibilityTrace.EligibilityTraceItem secondaryTraceItem = null;

                                float middleEventMustBeOpPropability = 0.5f; // config
                                boolean middleEventMustBeOp = mem.randomNumber.nextFloat() > middleEventMustBeOpPropability; // must the middle event be an op or can it be a normal event?

                                for(int idx2=neightborMinIdx;idx2<neightborMaxIdx;idx2++) {
                                    EligibilityTrace.EligibilityTraceItem traceItem = eligibilityTrace.eligibilityTrace.get(idx2);

                                    long timeDiff = abs(primaryEventOccTime - traceItem.retOccurenceTime());
                                    double expWindow = Math.exp(-timeDiff * decayFactorSecondary); // config

                                    double mulSalience = expWindow * traceItem.decay; // multiply salience with window to get "windowed" salience
                                    if (enableUniformSecondarySampling) { // do we sample uniformly?
                                        mulSalience = 1.0;
                                    }

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
                                                            if (onlyInputEvents && !iEvent.isInput()) {
                                                                continue;
                                                            }

                                                            middleEventCandidates.add(iEvent);
                                                        }
                                                    }
                                                }

                                                // select middle candidate event
                                                if (middleEventCandidates.size() > 0) {
                                                    middleEvent = selectTaskByConf(mem, middleEventCandidates);
                                                }
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

                                //  select random event as secondary


                                List<Task> candidateEvents = new ArrayList<>();
                                for(Task iEvent : secondaryTraceItem.events) {
                                    if (onlyInputEvents && !iEvent.isInput()) {
                                        continue;
                                    }

                                    candidateEvents.add(iEvent);
                                }

                                if (candidateEvents.size() == 0) {
                                    return null; // shouldn't happen
                                }
                                selectedSecondaryEvent = selectTaskByConf(mem, candidateEvents);
                            }
                        }

                        if (selectedSecondaryEvent != null) {
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

                            return sentencePair;
                        }
                    }
                }

            }
        }

        return null;
    }

    /**
     * selects one Task by the relative conf of all
     * @param mem
     * @param candidateEvents
     * @return one selection
     */
    private Task selectTaskByConf(Memory mem, List<Task> candidateEvents) {
        assert candidateEvents.size() > 0 : "it is assumed that there are events to select";
        if (candidateEvents.size() == 0) {
            return null;
        }

        double confMass = 0.0;
        // sum up all conf
        for (final Task iCandidate : candidateEvents) {
            confMass += iCandidate.sentence.truth.getConfidence();
        }

        double chosenConfMass = mem.randomNumber.nextDouble() * confMass;

        // select by accumulation
        double accu = 0.0;
        for (final Task iCandidate : candidateEvents) {
            accu += iCandidate.sentence.truth.getConfidence();
            if (accu >= chosenConfMass) {
                return iCandidate;
            }
        }

        return candidateEvents.get(candidateEvents.size()-1); // return last one if no other won
    }


    public static class ConceptWithSalience {
        public Task lastInputTask;

        public double salience = 0;

        public ConceptWithSalience(Task lastInputTask) {
            this.lastInputTask = lastInputTask;
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

                if(idxMin >= idxMax) {
                    return null; // didn't find item with the exact occurence time
                }

                int idxMiddle = idxMin + (idxMax-idxMin)/2;

                long timeMiddle = eligibilityTrace.get(idxMiddle).retOccurenceTime();
                if (timeMiddle < occTime) {
                    // special handling to prevent infinite loop
                    if(idxMin == idxMiddle) {
                        idxMin = idxMin+1;
                    }
                    else  {
                        idxMin = idxMiddle;
                    }

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
            String etKey = ""+CompoundTerm.replaceIntervals(term);
            if (eligibilityTraceItemsByTerm.containsKey(etKey)) {
                List<EligibilityTraceItem> items = eligibilityTraceItemsByTerm.get(etKey);

                // search for term, return if found because we don't need to add the term
                for(EligibilityTraceItem iItem : items) {
                    if (iItem.equals(item)) {
                        return; // we don't want to add the same item
                    }
                }
            }
            else {
                List<EligibilityTraceItem> items = new ArrayList<>();
                eligibilityTraceItemsByTerm.put(etKey, items);
            }

            // add it if we are here
            List<EligibilityTraceItem> arr = eligibilityTraceItemsByTerm.get(etKey);
            arr.add(item);

            if (arr.size() >= 2) {
                int debugMe = 5;
            }

            eligibilityTraceItemsByTerm.remove(etKey);
            eligibilityTraceItemsByTerm.put(etKey, arr);

            int here = 5;
        }

        public void removeEtItemByTerm(Term term, EligibilityTraceItem item) {
            String etKey = ""+CompoundTerm.replaceIntervals(term);

            if (!eligibilityTraceItemsByTerm.containsKey(etKey)) {
                return; // we can safely ignore it
            }

            eligibilityTraceItemsByTerm.get(etKey).remove(item);
            if (eligibilityTraceItemsByTerm.get(etKey).size() == 0) {
                eligibilityTraceItemsByTerm.remove(etKey); // we can remove it
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

    // used to ignore already derived conclusions
    private static class DerivationFilter {
        public List<Integer> hashesOfLastConclusions = new ArrayList<>();

        public void pushLifo(int hash) {
            int maxLength = 100;

            hashesOfLastConclusions.add(hash);

            if (hashesOfLastConclusions.size() >= maxLength) {
                hashesOfLastConclusions.remove(0);
            }
        }

        public boolean contains(int hash) {
            for(int i=0;i<hashesOfLastConclusions.size();i++) {
                if (hashesOfLastConclusions.get(i) == hash) {
                    return true;
                }
            }
            return false;
        }
    }

    static private List<Sentence> calcTopSentencesByConf(List<Sentence> sentences, Random rng, int limit) {
        List<Sentence> res = new ArrayList<>();
        res.addAll(sentences);
        Collections.sort(res, (s1, s2) -> {
            boolean isS1SmallerThanS2 = false;

            if (s1.truth.getConfidence() == s2.truth.getConfidence()) {
                isS1SmallerThanS2 = rng.nextFloat() > 0.5f; // necessary to randomly sort results which have the same conf
            } else {
                isS1SmallerThanS2 = s1.truth.getConfidence() < s2.truth.getConfidence();
            }

            //return s1.truth.getConfidence() == s2.truth.getConfidence() ?
            //    0 :
            //    (s1.truth.getConfidence() < s2.truth.getConfidence() ? 1 : -1); });

            return isS1SmallerThanS2 ? 1 : -1;
        });

        while(res.size() > limit) {
            res.remove(limit);
        }
        return res;
    }
}
