package org.opennars.derivation;

import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.TruthValue;
import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.interfaces.Timable;
import org.opennars.language.Conjunction;
import org.opennars.language.Interval;
import org.opennars.language.Term;
import org.opennars.main.Parameters;

import java.util.*;

public class InstructionsAndContext {
    public static void startIdxA(Context ctx) {
        ctx.idxA=-1;
    }

    public static void startIdxB(Context ctx) {
        ctx.idxB=-1;
    }

    public static void checkEndA(Context ctx) {
        ctx.flag = ctx.idxA >= ((Conjunction)ctx.a.term).term.length;
    }

    public static void checkEndB(Context ctx) {
        ctx.flag = ctx.idxB >= ((Conjunction)ctx.b.term).term.length;
    }

    public static void jmp(long offset, Context ctx) {
        ctx.ip += offset;
    }

    public static void scanNextA(Context ctx) {
        ctx.idxA++;
        for(;;) {
            Conjunction conj = ((Conjunction)ctx.a.term);
            if( ctx.idxA >= conj.term.length ) {
                break;
            }
            if( ctx.idxA == 0 ) {
                ctx.termA = new DerivationProcessor.TermWithOccurrenceTime(conj.term[ctx.idxA], ctx.a.stamp.getOccurrenceTime());
                break;
            }
            if( conj.term[ctx.idxA] instanceof Interval) {
                ctx.termA = new DerivationProcessor.TermWithOccurrenceTime(ctx.termA.term, ctx.termA.occurrenceTime + ((Interval)conj.term[ctx.idxA]).time);
            }
            else {
                ctx.termA = new DerivationProcessor.TermWithOccurrenceTime(conj.term[ctx.idxA], ctx.termA.occurrenceTime);
                break;
            }
            ctx.idxA++;
        }
    }

    public static void scanNextB(Context ctx) {
        ctx.idxB++;
        for(;;) {
            Conjunction conj = ((Conjunction)ctx.b.term);
            if( ctx.idxB >= conj.term.length ) {
                break;
            }
            if( ctx.idxB == 0 ) {
                ctx.termB = new DerivationProcessor.TermWithOccurrenceTime(conj.term[ctx.idxB], ctx.b.stamp.getOccurrenceTime());
                break;
            }
            if( conj.term[ctx.idxB] instanceof Interval ) {
                ctx.termB = new DerivationProcessor.TermWithOccurrenceTime(ctx.termB.term, ctx.termB.occurrenceTime + ((Interval)conj.term[ctx.idxB]).time);
            }
            else {
                ctx.termB = new DerivationProcessor.TermWithOccurrenceTime(conj.term[ctx.idxB], ctx.termB.occurrenceTime);
                break;
            }
            ctx.idxB++;
        }
    }

    public static void storeSeqSortedA(Context ctx) {
        insertTermSortedByOccurrentTime(ctx.byOccurrenceTimeSortedList, ctx.termA);
    }

    public static void storeSeqSortedB(Context ctx) {
        insertTermSortedByOccurrentTime(ctx.byOccurrenceTimeSortedList, ctx.termB);
    }

    public static void readA(Context ctx) {
        ctx.termA = new DerivationProcessor.TermWithOccurrenceTime(ctx.a.term, ctx.a.getOccurenceTime());
    }

    public static void readB(Context ctx) {
        ctx.termB = new DerivationProcessor.TermWithOccurrenceTime(ctx.b.term, ctx.b.getOccurenceTime());
    }

    public static void calcTruthUnionAB(Context ctx) {
        ctx.resultTruth = TruthFunctions.union(ctx.a.truth, ctx.b.truth, ctx.reasonerParameters);
    }

    public static void calcTruthIdA(Context ctx) {
        ctx.resultTruth = ctx.a.truth;
    }

    public static Sentence m_writeConjunction(Context ctx) {
        List<Term> resultTerms = new ArrayList<>();

        // add terms with intervals between
        for(int iTermWithTimeIdx=0; iTermWithTimeIdx<ctx.byOccurrenceTimeSortedList.size()-1; iTermWithTimeIdx++) {
            DerivationProcessor.TermWithOccurrenceTime iTermWithTime = ctx.byOccurrenceTimeSortedList.get(iTermWithTimeIdx);
            DerivationProcessor.TermWithOccurrenceTime nextTermWithTime = ctx.byOccurrenceTimeSortedList.get(iTermWithTimeIdx+1);

            resultTerms.add(iTermWithTime.term);
            long occurrenceTimeDiff = nextTermWithTime.occurrenceTime - iTermWithTime.occurrenceTime;
            if(occurrenceTimeDiff > 0) {
                resultTerms.add(new Interval(occurrenceTimeDiff));
            }
        }

        // add last one
        Term lastTerm = ctx.byOccurrenceTimeSortedList.get(ctx.byOccurrenceTimeSortedList.size()-1).term;
        resultTerms.add(lastTerm);

        Term[] resultTermsAsArr = resultTerms.toArray(new Term[resultTerms.size()]);
        Term conj = Conjunction.make(resultTermsAsArr, TemporalRules.ORDER_FORWARD, false);

        long occurrenceTimeOfFirstEvent = ctx.byOccurrenceTimeSortedList.get(0).occurrenceTime;

        Stamp stamp = new Stamp(ctx.a.stamp, ctx.b.stamp, ctx.time.time(), ctx.reasonerParameters);

        Sentence createdSentence = new Sentence(
            conj, '.', ctx.resultTruth, stamp
        );
        createdSentence.stamp.setOccurrenceTime(occurrenceTimeOfFirstEvent);

        ctx.derivedSentences.add(createdSentence);
        return createdSentence;
    }

    public static Sentence m_writeWindowedConjunction(Context ctx) {
        // events which are put into buckets
        // the buckets contain parallel occuring events in their windows
        Map<Long, List<DerivationProcessor.TermWithOccurrenceTime>> quantizedParallelEvents = new HashMap<>();

        long windowQuantization = 80;
        { // put events into buckets
            for (DerivationProcessor.TermWithOccurrenceTime iTerm : ctx.byOccurrenceTimeSortedList) {
                long quantizedBucketTime = (iTerm.occurrenceTime / windowQuantization) * windowQuantization;

                List<DerivationProcessor.TermWithOccurrenceTime> termsOfBucket;
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
            List<Map.Entry<Long, List<DerivationProcessor.TermWithOccurrenceTime>>> sortedQuantizedParallelEvents = new ArrayList<>();
            sortedQuantizedParallelEvents.addAll(quantizedParallelEvents.entrySet());
            Collections.sort(sortedQuantizedParallelEvents, new Comparator<Map.Entry<Long, List<DerivationProcessor.TermWithOccurrenceTime>>>(){
                public int compare(Map.Entry<Long, List<DerivationProcessor.TermWithOccurrenceTime>> s1, Map.Entry<Long, List<DerivationProcessor.TermWithOccurrenceTime>> s2){
                    if( s1.getKey() == s2.getKey()) {
                        return 0;
                    }
                    return s1.getKey() > s2.getKey() ? 1 : -1;
                }});


            List<Term> resultTerms = new ArrayList<>();

            // add terms with intervals between
            for(int iTermWithTimeIdx=0; iTermWithTimeIdx<sortedQuantizedParallelEvents.size()-1; iTermWithTimeIdx++) {
                long thisWindowQuantizedTime = sortedQuantizedParallelEvents.get(iTermWithTimeIdx).getKey();
                List<DerivationProcessor.TermWithOccurrenceTime> thisWindowParallelTerms = sortedQuantizedParallelEvents.get(iTermWithTimeIdx).getValue();
                long nextWindowQuantizedTime = sortedQuantizedParallelEvents.get(iTermWithTimeIdx+1).getKey();
                List<DerivationProcessor.TermWithOccurrenceTime> nextWindowParallelTerms = sortedQuantizedParallelEvents.get(iTermWithTimeIdx+1).getValue();

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
            List<DerivationProcessor.TermWithOccurrenceTime> lastParallel = sortedQuantizedParallelEvents.get(sortedQuantizedParallelEvents.size()-1).getValue();
            Term lastTerm = buildConcurrentConjunction(lastParallel);
            resultTerms.add(lastTerm);

            // build sentence

            Term[] resultTermsAsArr = resultTerms.toArray(new Term[resultTerms.size()]);
            Term conj = Conjunction.make(resultTermsAsArr, TemporalRules.ORDER_FORWARD, false);

            long occurrenceTimeOfFirstEvent = ctx.byOccurrenceTimeSortedList.get(0).occurrenceTime;

            Stamp stamp;
            if(ctx.b != null) {
                stamp = new Stamp(ctx.a.stamp, ctx.b.stamp, ctx.time.time(), ctx.reasonerParameters);
            }
            else { // case when b is null because a is the only argument
                stamp = ctx.a.stamp;
            }

            Sentence createdSentence = new Sentence(
                conj, '.', ctx.resultTruth, stamp
            );
            createdSentence.stamp.setOccurrenceTime(occurrenceTimeOfFirstEvent);

            ctx.derivedSentences.add(createdSentence);
            return createdSentence;
        }
    }


    ///////////////////
    // helper methods

    private static void insertTermSortedByOccurrentTime(List<DerivationProcessor.TermWithOccurrenceTime> byOccurrenceTimeSortedList, DerivationProcessor.TermWithOccurrenceTime inserted) {
        //System.out.println("insert " + inserted.term);

        for(int idx=0;idx<byOccurrenceTimeSortedList.size();idx++) {
            DerivationProcessor.TermWithOccurrenceTime item = byOccurrenceTimeSortedList.get(idx);
            if(item.occurrenceTime > inserted.occurrenceTime) {
                byOccurrenceTimeSortedList.add(idx, inserted);
                return;
            }
        }
        byOccurrenceTimeSortedList.add(inserted);
    }

    private static Term buildConcurrentConjunction(List<DerivationProcessor.TermWithOccurrenceTime> terms) {
        if(terms.size() == 1) { // special case - just one is not parallel
            return terms.get(0).term;
        }

        Term[] arr = new Term[terms.size()];
        for(int i=0;i<arr.length;i++) {
            arr[i] = terms.get(i).term;
        }
        return Conjunction.make(arr, TemporalRules.ORDER_CONCURRENT);
    }


    public static class Context {
        public int idxA = -1; // index in premise A
        public int idxB = -1; // index in premise B

        public boolean flag = false;

        // keep track of result terms sorted by occurence time
        public List<DerivationProcessor.TermWithOccurrenceTime> byOccurrenceTimeSortedList = new ArrayList<>();

        public DerivationProcessor.TermWithOccurrenceTime termA = null;
        public DerivationProcessor.TermWithOccurrenceTime termB = null;

        public TruthValue resultTruth = null;

        public int ip=0; // instruction pointer

        // parameters
        public Sentence a;
        public Sentence b;
        public List<Sentence> derivedSentences;
        public Timable time;
        public Parameters reasonerParameters;
    }
}
