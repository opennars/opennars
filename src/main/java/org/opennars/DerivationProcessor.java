package org.opennars;

import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.TruthValue;
import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.interfaces.Timable;
import org.opennars.language.*;
import org.opennars.main.Parameters;

import java.util.*;

public class DerivationProcessor {

    // process a program on two sentences for some temporal inference to derive a conclusion
    // /param condA condition
    // /param condB condition
    // /param program program to get processed instruction by instruction
    // /param a premise a
    // /param b premise b
    public static Sentence processProgramForTemporal(String condA, String condB, Instr[] program, Sentence a, Sentence b, List<Sentence> derivedSentences, Timable time, Parameters reasonerParameters) {
        if (!checkCondition(condA, a) || !checkCondition(condB, b)) {
            return null; // ignore because any condition didn't match up
        }

        int idxA = -1; // index in premise A
        int idxB = -1; // index in premise B

        boolean flag = false;

        // keep track of result terms sorted by occurence time
        List<TermWithOccurrenceTime> byOccurrenceTimeSortedList = new ArrayList<>();

        TermWithOccurrenceTime termA = null;
        TermWithOccurrenceTime termB = null;

        TruthValue resultTruth = null;


        // interpret program instruction by instruction and keep track of indices in premises

        int ip=0; // instruction pointer
        for(;;) {
            if(ip>=program.length) {
                break;
            }
            Instr currentInstr = program[ip];
            ip++;

            if(currentInstr.mnemonic.equals("startIdxA")) {
                idxA=-1;
            }
            else if(currentInstr.mnemonic.equals("startIdxB")) {
                idxB=-1;
            }
            else if(currentInstr.mnemonic.equals("checkEndA")) {
                flag = idxA >= ((Conjunction)a.term).term.length;
            }
            else if(currentInstr.mnemonic.equals("checkEndB")) {
                flag = idxB >= ((Conjunction)b.term).term.length;
            }
            else if(currentInstr.mnemonic.equals("jmpTrue")) {
                if(flag) {
                    String labelName = currentInstr.arg0;

                    // search for label
                    for(int idx=0;idx<program.length;idx++) {
                        if(program[idx].mnemonic.equals("label") && program[idx].arg0.equals(labelName)) {
                            ip = idx;
                            break;
                        }
                    }
                }
            }
            else if(currentInstr.mnemonic.equals("jmp")) {
                ip += currentInstr.arg0Int;
            }
            else if(currentInstr.mnemonic.equals("scanNextA")) {
                idxA++;
                for(;;) {
                    Conjunction conj = ((Conjunction)a.term);
                    if( idxA >= conj.term.length ) {
                        break;
                    }
                    if( idxA == 0 ) {
                        termA = new TermWithOccurrenceTime(conj.term[idxA], a.stamp.getOccurrenceTime());
                        break;
                    }
                    if( conj.term[idxA] instanceof Interval ) {
                        termA = new TermWithOccurrenceTime(termA.term, termA.occurrenceTime + ((Interval)conj.term[idxA]).time);
                    }
                    else {
                        termA = new TermWithOccurrenceTime(conj.term[idxA], termA.occurrenceTime);
                        break;
                    }
                    idxA++;
                }
            }
            else if(currentInstr.mnemonic.equals("scanNextB")) {
                idxB++;
                for(;;) {
                    Conjunction conj = ((Conjunction)b.term);
                    if( idxB >= conj.term.length ) {
                        break;
                    }
                    if( idxB == 0 ) {
                        termB = new TermWithOccurrenceTime(conj.term[idxB], b.stamp.getOccurrenceTime());
                        break;
                    }
                    if( conj.term[idxB] instanceof Interval ) {
                        termB = new TermWithOccurrenceTime(termB.term, termB.occurrenceTime + ((Interval)conj.term[idxB]).time);
                    }
                    else {
                        termB = new TermWithOccurrenceTime(conj.term[idxB], termB.occurrenceTime);
                        break;
                    }
                    idxB++;
                }
            }
            else if(currentInstr.mnemonic.equals("storeSeqSortedA")) {
                insertTermSortedByOccurrentTime(byOccurrenceTimeSortedList, termA);
            }
            else if(currentInstr.mnemonic.equals("storeSeqSortedB")) {
                insertTermSortedByOccurrentTime(byOccurrenceTimeSortedList, termB);
            }

            else if(currentInstr.mnemonic.equals("readA")) {
                termA = new TermWithOccurrenceTime(a.term, a.getOccurenceTime());
            }
            else if(currentInstr.mnemonic.equals("readB")) {
                termB = new TermWithOccurrenceTime(b.term, b.getOccurenceTime());
            }

            else if(currentInstr.mnemonic.equals("calcTruthUnionAB")) {
                resultTruth = TruthFunctions.union(a.truth, b.truth, reasonerParameters);
            }
            else if(currentInstr.mnemonic.equals("calcTruthIdA")) {
                resultTruth = a.truth;
            }

            // MACRO to write out the sorted list as a conjunction
            else if(currentInstr.mnemonic.equals("m_writeConjuction")) {
                List<Term> resultTerms = new ArrayList<>();

                // add terms with intervals between
                for(int iTermWithTimeIdx=0; iTermWithTimeIdx<byOccurrenceTimeSortedList.size()-1; iTermWithTimeIdx++) {
                    TermWithOccurrenceTime iTermWithTime = byOccurrenceTimeSortedList.get(iTermWithTimeIdx);
                    TermWithOccurrenceTime nextTermWithTime = byOccurrenceTimeSortedList.get(iTermWithTimeIdx+1);

                    resultTerms.add(iTermWithTime.term);
                    long occurrenceTimeDiff = nextTermWithTime.occurrenceTime - iTermWithTime.occurrenceTime;
                    if(occurrenceTimeDiff > 0) {
                        resultTerms.add(new Interval(occurrenceTimeDiff));
                    }
                }

                // add last one
                Term lastTerm = byOccurrenceTimeSortedList.get(byOccurrenceTimeSortedList.size()-1).term;
                resultTerms.add(lastTerm);

                Term[] resultTermsAsArr = resultTerms.toArray(new Term[resultTerms.size()]);
                Term conj = Conjunction.make(resultTermsAsArr, TemporalRules.ORDER_FORWARD, false);

                long occurrenceTimeOfFirstEvent = byOccurrenceTimeSortedList.get(0).occurrenceTime;

                Stamp stamp = new Stamp(a.stamp, b.stamp, time.time(), reasonerParameters);

                Sentence createdSentence = new Sentence(
                    conj, '.', resultTruth, stamp
                );
                createdSentence.stamp.setOccurrenceTime(occurrenceTimeOfFirstEvent);

                derivedSentences.add(createdSentence);
                return createdSentence;
            }
            else if(currentInstr.mnemonic.equals("m_writeWindowedConjuction")) {
                // events which are put into buckets
                // the buckets contain parallel occuring events in their windows
                Map<Long, List<TermWithOccurrenceTime>> quantizedParallelEvents = new HashMap<>();

                long windowQuantization = 80;
                { // put events into buckets
                    for (TermWithOccurrenceTime iTerm : byOccurrenceTimeSortedList) {
                        long quantizedBucketTime = (iTerm.occurrenceTime / windowQuantization) * windowQuantization;

                        List<TermWithOccurrenceTime> termsOfBucket;
                        if (quantizedParallelEvents.containsKey(quantizedBucketTime)) {
                            termsOfBucket = quantizedParallelEvents.get(quantizedBucketTime);
                        }
                        else {
                            termsOfBucket = new ArrayList<>();
                        }
                        termsOfBucket.add(iTerm);
                        quantizedParallelEvents.put(quantizedBucketTime, termsOfBucket);
                    }
                }

                { // build sequence with parallel events - seperated by intervals
                    List<Map.Entry<Long, List<TermWithOccurrenceTime>>> sortedQuantizedParallelEvents = new ArrayList<>();
                    sortedQuantizedParallelEvents.addAll(quantizedParallelEvents.entrySet());
                    Collections.sort(sortedQuantizedParallelEvents, new Comparator<Map.Entry<Long, List<TermWithOccurrenceTime>>>(){
                        public int compare(Map.Entry<Long, List<TermWithOccurrenceTime>> s1,Map.Entry<Long, List<TermWithOccurrenceTime>> s2){
                            if( s1.getKey() == s2.getKey()) {
                                return 0;
                            }
                            return s1.getKey() > s2.getKey() ? 1 : -1;
                        }});


                    List<Term> resultTerms = new ArrayList<>();

                    // add terms with intervals between
                    for(int iTermWithTimeIdx=0; iTermWithTimeIdx<sortedQuantizedParallelEvents.size()-1; iTermWithTimeIdx++) {
                        long thisWindowQuantizedTime = sortedQuantizedParallelEvents.get(iTermWithTimeIdx).getKey();
                        List<TermWithOccurrenceTime> thisWindowParallelTerms = sortedQuantizedParallelEvents.get(iTermWithTimeIdx).getValue();
                        long nextWindowQuantizedTime = sortedQuantizedParallelEvents.get(iTermWithTimeIdx+1).getKey();
                        List<TermWithOccurrenceTime> nextWindowParallelTerms = sortedQuantizedParallelEvents.get(iTermWithTimeIdx+1).getValue();

                        Term concurrentConj = buildConcurrentConjunction(thisWindowParallelTerms);
                        resultTerms.add(concurrentConj);

                        // compute time difference - is based on size of lists - can be exact if window size is 1 and 1 - can not be exact if it is not 1 and 1
                        long occurenceTimeDiff = 0;
                        if (thisWindowParallelTerms.size() <= 1 && nextWindowParallelTerms.size() <= 1) {
                            occurenceTimeDiff = nextWindowParallelTerms.get(0).occurrenceTime - thisWindowParallelTerms.get(0).occurrenceTime;
                        }
                        else {
                            occurenceTimeDiff = nextWindowQuantizedTime - thisWindowQuantizedTime;
                        }

                        if(occurenceTimeDiff > 0) {
                            resultTerms.add(new Interval(occurenceTimeDiff));
                        }
                    }

                    // add last one
                    List<TermWithOccurrenceTime> lastParallel = sortedQuantizedParallelEvents.get(sortedQuantizedParallelEvents.size()-1).getValue();
                    Term lastTerm = buildConcurrentConjunction(lastParallel);
                    resultTerms.add(lastTerm);

                    // build sentence

                    Term[] resultTermsAsArr = resultTerms.toArray(new Term[resultTerms.size()]);
                    Term conj = Conjunction.make(resultTermsAsArr, TemporalRules.ORDER_FORWARD, false);

                    long occurrenceTimeOfFirstEvent = byOccurrenceTimeSortedList.get(0).occurrenceTime;

                    Stamp stamp;
                    if(b != null) {
                        stamp = new Stamp(a.stamp, b.stamp, time.time(), reasonerParameters);
                    }
                    else {
                        stamp = a.stamp;
                    }

                    Sentence createdSentence = new Sentence(
                        conj, '.', resultTruth, stamp
                    );
                    createdSentence.stamp.setOccurrenceTime(occurrenceTimeOfFirstEvent);

                    derivedSentences.add(createdSentence);
                    return createdSentence;
                }
            }
            else if(currentInstr.mnemonic.equals("label")) {
                // ignore
            }
            else {
                int here = 5;
            }
        }

        return null; // program was invalid if we are here - ignore
    }

    private static Term buildConcurrentConjunction(List<TermWithOccurrenceTime> terms) {
        if(terms.size() == 1) { // special case - just one is not parallel
            return terms.get(0).term;
        }

        Term[] arr = new Term[terms.size()];
        for(int i=0;i<arr.length;i++) {
            arr[i] = terms.get(i).term;
        }
        return Conjunction.make(arr, TemporalRules.ORDER_CONCURRENT);
    }

    private static void insertTermSortedByOccurrentTime(List<TermWithOccurrenceTime> byOccurrenceTimeSortedList, TermWithOccurrenceTime inserted) {
        //System.out.println("insert " + inserted.term);

        for(int idx=0;idx<byOccurrenceTimeSortedList.size();idx++) {
            TermWithOccurrenceTime item = byOccurrenceTimeSortedList.get(idx);
            if(item.occurrenceTime > inserted.occurrenceTime) {
                byOccurrenceTimeSortedList.add(idx, inserted);
                return;
            }
        }
        byOccurrenceTimeSortedList.add(inserted);
    }

    public static class TermWithOccurrenceTime {
        public Term term;
        public long occurrenceTime;

        public TermWithOccurrenceTime(Term term, long occurrenceTime) {
            this.term = term;
            this.occurrenceTime = occurrenceTime;
        }
    }

    // encoding of condition
    // "F" temporal forward implication
    // "S" temporal sequence
    // "E" single event
    // "" always true
    private static boolean checkCondition(String condition, Sentence sentence) {
        if(condition.equals("")) {
            return true;
        }

        Term term = sentence.term;

        if(condition.equals("F")) {
            return term instanceof Implication && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;
        }
        else if(condition.equals("S")) {
            return term instanceof Conjunction && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;
        }
        else if(condition.equals("E")) {
            if(sentence.isEternal()) {
                return false;
            }

            return term instanceof Similarity || term instanceof Inheritance;
        }

        return false; // not defined - default value!
    }


    // instructions:
    // startIdxA : set idx of a at the beginning
    ///// startIdxB : set idx of b at the beginning
    // scanNextA : scan for next non interval in a and keep track of occurence time
    ///// incIdxB : increment idx of b
    // storeSeqSortedA : store event at current idx of a into the by time sorted list of events

    // checkEndA : check if idx is at end


    public static class Instr {
        public Instr(String mnemonic) {
            this.mnemonic = mnemonic;
            arg0 = null;
            arg0Int=0;
        }
        public Instr(String mnemonic, String arg0) {
            this.mnemonic = mnemonic;
            this.arg0 = arg0;
            arg0Int=0;
        }
        public Instr(String mnemonic, long arg0) {
            this.mnemonic = mnemonic;
            this.arg0Int = arg0;
            this.arg0 = null;
        }
        public final String mnemonic;
        public final String arg0;
        public final long arg0Int;
    }

    public static Instr[] programCombineSequenceAndEvent;
    public static Instr[] programCombineEventAndEvent;
    public static Instr[] programTranslateSequenceToWindowedSequence; // translate sequence in a to a sequences with parallel windowed events


    static {
        { // program to combine a sequences and a event into one ordered sequence
            programCombineSequenceAndEvent = new Instr[]{
                new Instr("label", "label_readA"),// label: read A

                new Instr("startIdxA"),

                new Instr("scanNextA"),
                new Instr("checkEndA"),
                new Instr("jmpTrue", "label_readB"),

                new Instr("storeSeqSortedA"),

                new Instr("jmp", -5),


                new Instr("label", "label_readB"),// label: read B

                new Instr("readB"),
                new Instr("storeSeqSortedB"),

                //new Instr("startIdxB"),

                //new Instr("checkEndB"),
                //new Instr("jmpTrue", "label_buildConclusion"),

                //new Instr("scanNextB"),
                //new Instr("storeSeqSortedB"),

                //new Instr("jmp", -5),


                new Instr("label", "label_buildConclusion"),// label: build conclusion


                new Instr("calcTruthUnionAB"), // compute the truth by taking the union of input A and input B

                // read out result array and write to conclusion sequence conjunction
                new Instr("m_writeConjuction")
            };
        }



        { // program to combine a event and a event into one ordered sequence
            programCombineEventAndEvent = new Instr[]{
                new Instr("readA"),
                new Instr("storeSeqSortedA"),

                new Instr("readB"),
                new Instr("storeSeqSortedB"),

                new Instr("calcTruthUnionAB"), // compute the truth by taking the union of input A and input B

                // read out result array and write to conclusion sequence conjunction
                new Instr("m_writeConjuction")
            };
        }

        { // program to combine a sequences and a event into one ordered sequence
            programTranslateSequenceToWindowedSequence = new Instr[]{
                new Instr("label", "label_readA"),// label: read A

                new Instr("startIdxA"),

                new Instr("scanNextA"),
                new Instr("checkEndA"),
                new Instr("jmpTrue", "label_buildConclusion"),

                new Instr("storeSeqSortedA"),

                new Instr("jmp", -5),

                new Instr("label", "label_buildConclusion"),// label: build conclusion


                new Instr("calcTruthIdA"), // compute the truth by taking the truth of input A

                // read out result array and write to conclusion sequence conjunction
                new Instr("m_writeWindowedConjuction")
            };
        }

    }
}
