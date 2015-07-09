package nars.concept;

import nars.Memory;
import nars.nal.nal7.TemporalRules;
import nars.process.NAL;
import nars.task.Task;
import nars.truth.Truth;
import nars.truth.TruthWave;
import nars.truth.Truthed;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;

import static nars.nal.UtilityFunctions.or;
import static nars.nal.nal7.TemporalRules.solutionQuality;
import static nars.nal.nal7.TemporalRules.solutionQualityMatchingOrder;

/**
 * A model storing, ranking, and projecting beliefs or goals (tasks with TruthValue).
 * It should iterate in top-down order (highest ranking first)
 */
public interface BeliefTable extends TaskTable {


    Ranker BeliefConfidenceOrOriginality = new Ranker() {

        @Override
        public float rank(Task belief, float bestToBeat) {
            final float confidence = belief.getTruth().getConfidence();
            final float originality = belief.getOriginality();
            return or(confidence, originality);
        }
    };

    /** attempt to insert a task.
     *
     * @param c the concept in which this occurrs
     * @param nal
     * @return:
     *      the input value that was inserted, if it was added to the table
     *      a previous stored task if this was a duplicate (table unchanged)
     *      a new belief created from older ones which serves as a revision of what was input, if it was added to the table
     *
     */
    public Task add(Task input, Ranker r, Concept c, NAL nal);

    default Task add(Task input, Concept c, NAL nal) {
        return add(input, getRank(), c, nal);
    }

    /** the default rank used when adding and other operations where rank is unspecified */
    public Ranker getRank();

//    /**
//     * projects to a new task at a given time
//     * was: getTask(q, now, getBeliefs()).  Does not affect the table itself */
//    public Task project(Task t, long now);

    /*default public Task project(final Task t) {
        return project(t, Stamp.TIMELESS);
    }*/


    /**
     * get a random belief, weighted by their sentences confidences
     */
    default public Task getBeliefRandomByConfidence(boolean eternal, Random rng) {

        if (isEmpty()) return null;

        float totalConfidence = getConfidenceSum();
        float r = rng.nextFloat() * totalConfidence;


        for (Task x : this) {
            r -= x.getTruth().getConfidence();
            if (r < 0)
                return x;
        }

        return null;
    }


    default public float getConfidenceSum() {
        return getConfidenceSum(this);
    }

    public static float getConfidenceSum(Iterable<? extends Truthed> beliefs) {
        float t = 0;
        for (final Truthed s : beliefs)
            t += s.getTruth().getConfidence();
        return t;
    }

    public static float getMeanFrequency(Collection<? extends Truthed> beliefs) {
        if (beliefs.isEmpty()) return 0.5f;

        float t = 0;
        for (final Truthed s : beliefs)
            t += s.getTruth().getFrequency();
        return t / beliefs.size();
    }

    default public Task top(final Task query, final long now) {

        final Task top = top();
        if (top == null) return null;

        if (!TemporalRules.matchingOrder(query, top.getTerm())) {
            return null;
        }

        if (size() == 1)
            return top;


        return top(new Ranker() {
            @Override
            public float rank(Task t, float bestToBeat) {
                return solutionQualityMatchingOrder(query, t, now);
            }
        });
    }
    default public Task top(boolean hasQueryVar, final long now, long occTime, Truth truth) {

        if (isEmpty()) return null;

        return top(new Ranker() {
            @Override
            public float rank(Task t, float bestToBeat) {
                return solutionQuality(hasQueryVar, occTime, t, truth, now);
            }
        });
    }

//    /**
//     * Select a belief value or desire value for a given query
//     *
//     * @param query The query to be processed
//     * @param list  The list of beliefs or goals to be used
//     * @return The best candidate selected
//     */
//    public static Task getTask(final Sentence query, long now, final List<Task>... lists) {
//        float currentBest = 0;
//        float beliefQuality;
//        Task candidate = null;
//
//        for (List<Task> list : lists) {
//            if (list.isEmpty()) continue;
//
//            int lsv = list.size();
//            for (int i = 0; i < lsv; i++) {
//                Task judg = list.get(i);
//                beliefQuality = solutionQuality(query, judg.sentence, now);
//                if (beliefQuality > currentBest) {
//                    currentBest = beliefQuality;
//                    candidate = judg;
//                }
//            }
//        }
//
//        return candidate;
//    }



    /** get the top-ranking belief/goal, selecting either eternal or temporal beliefs, or both  */
    public Task top(boolean eternal, boolean temporal);

    /** get the top-ranking belief/goal */
    default public Task top() {
        return top(true, true);
    }

    /** the truth v alue of the topmost element, or null if there is none */
    default public Truth topTruth() {
        if (isEmpty()) return null;
        return top().getTruth();
    }

    default void print(PrintStream out) {
        for (Task t : this) {
            System.out.println(t + " " + t.getHistory());
        }
    }

    default public TruthWave getWave() {
        return new TruthWave(this);
    }


    /** computes the truth/desire as an aggregate of projections of all
     * beliefs to current time
     */
    default public float getMeanProjectedExpectation(final long time) {
        float d = 0;

        int size = size();
        if (size == 0) return 0;

        for (Task t : this) {
            d += t.projectionTruthQuality(time, time, false) * t.truth.getExpectation();
        }

        //System.out.println(top().getTruth().getExpectation() + " " + d/size + " (" + size + ")");

        return d/size;
    }

    public interface Ranker extends Function<Task,Float> {
        /** returns a number producing a score or relevancy number for a given Task
         * @param bestToBeat current best score, which the ranking can use to decide to terminate early
         * @return a score value, or Float.MIN_VALUE to exclude that result
         * */
        public float rank(Task t, float bestToBeat);


        default float rank(Task t) {
            return rank(t, Float.MIN_VALUE);
        }

        @Override default Float apply(Task t) {
            return rank(t);
        }

    }

    @FunctionalInterface
    public interface RankBuilder {
        public Ranker get(Concept c, boolean /*true*/ beliefOrGoal /*false*/);
    }

    /** allowed to return null. must evaluate all items in case the final one is the
     *  only item that does not have disqualifying rank (MIN_VALUE)
     * */
    default public Task top(Ranker r) {

        float s = Float.MIN_VALUE;
        Task b = null;

        for (Task t : this) {
            float x = r.rank(t, s);
            if (x > s) {
                s = x;
                b = t;
            }
        }

        return b;
    }

    default public Task topRanked() {
        return top(getRank());
    }








    public static class BeliefConfidenceAndCurrentTime implements Ranker {

        private final Concept concept;

        /** controls dropoff rate, measured in durations */
        float relevanceWindow = 0.9f;
        float temporalityFactor = 1f;



        public BeliefConfidenceAndCurrentTime(Concept c) {
            this.concept = c;
        }

        /** if returns c itself, this is a 1:1 linear mapping of confidence to starting
         * score before penalties applied. this could also be a curve to increase
         * or decrease the apparent relevance of certain confidence amounts.
         * @return value >=0, <=1
         */
        public float confidenceScore(final float c) {
            return c;
        }

        @Override
        public float rank(Task t, float bestToBeat) {
            float r = confidenceScore(t.getTruth().getConfidence());

            if (!t.isEternal()) {

                final long now = concept.getMemory().time();
                float dur = t.getDuration();
                float durationsToNow = Math.abs(t.getOccurrenceTime() - now) / dur;


                //float agePenalty = (1f - 1f / (1f + (durationsToNow / relevanceWindow))) * temporalityFactor;
                float agePenalty = (durationsToNow / relevanceWindow) * temporalityFactor;
                r -= agePenalty; // * temporalityFactor;
            }

            float unoriginalityPenalty = 1f - t.getOriginality();
            r -= unoriginalityPenalty * 1;

            return r;
        }

    }


//    default public Task top(boolean eternal, boolean nonEternal) {
//
//    }




//    /** temporary until goal is separated into goalEternal, goalTemporal */
//    @Deprecated default public Task getStrongestTask(final List<Task> table, final boolean eternal, final boolean temporal) {
//        for (Task t : table) {
//            boolean e = t.isEternal();
//            if (e && eternal) return t;
//            if (!e && temporal) return t;
//        }
//        return null;
//    }
//
//    public static Sentence getStrongestSentence(List<Task> table) {
//        Task t = getStrongestTask(table);
//        if (t!=null) return t.sentence;
//        return null;
//    }
//
//    public static Task getStrongestTask(List<Task> table) {
//        if (table == null) return null;
//        if (table.isEmpty()) return null;
//        return table.get(0);
//    }

//    /**
//     * Determine the rank of a judgment by its quality and originality (stamp
//     * baseLength), called from Concept
//     *
//     * @param s The judgment to be ranked
//     * @return The rank of the judgment, according to truth value only
//     */
    /*public float rank(final Task s, final long now) {
        return rankBeliefConfidenceTime(s, now);
    }*/


//    public Sentence getSentence(final Sentence query, long now, final List<Task>... lists) {
//        Task t = getTask(query, now, lists);
//        if (t == null) return null;
//        return t.sentence;
//    }
}
