package nars.concept;

import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.truth.Truth;
import nars.truth.Truthed;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static nars.nal.nal7.TemporalRules.solutionQuality;

/**
 * A model storing, ranking, and projecting beliefs or goals (tasks with TruthValue).
 * It should iterate in top-down order (highest ranking first)
 */
public interface BeliefTable extends TaskTable {




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

    /**
     * projects to a new task at a given time
     * was: getTask(q, now, getBeliefs()).  Does not affect the table itself */
    public Task project(Task t, long now);

    default public Task project(final Task t) {
        return project(t, Stamp.TIMELESS);
    }


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
        return top(new Ranker() {
            @Override
            public float rank(Task t, float bestToBeat) {
                return solutionQuality(query, t, now);
            }
        });
    }
    default public Task top(boolean hasQueryVar, final long now, long occTime, Truth truth) {
        //return top( (t, b) -> solutionQuality(hasQueryVar, occTime, t, truth, now) );
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

    public interface Ranker extends Function<Task,Float> {
        /** returns a number producing a score or relevancy number for a given Task
         * @param bestToBeat current best score, which the ranking can use to decide to terminate early
         * @return a score value, or NaN to exclude that result
         * */
        public float rank(Task t, float bestToBeat);


        default float rank(Task t) {
            return rank(t, Float.MIN_VALUE);
        }

        @Override default Float apply(Task t) {
            return rank(t);
        }
    }

    default public Task top(Ranker r) {

        float s = Float.MIN_VALUE;
        Task b = null;

        final int n = size();
        for (Task t : this) {
            float x = r.rank(t, s);
            if (x > s) {
                s = x;
                b = t;
            }
        }

        return b;
    }







//    default public Task top(boolean eternal, boolean nonEternal) {
//
//    }




    /** temporary until goal is separated into goalEternal, goalTemporal */
    @Deprecated default public Task getStrongestTask(final List<Task> table, final boolean eternal, final boolean temporal) {
        for (Task t : table) {
            boolean e = t.isEternal();
            if (e && eternal) return t;
            if (!e && temporal) return t;
        }
        return null;
    }

    public static Sentence getStrongestSentence(List<Task> table) {
        Task t = getStrongestTask(table);
        if (t!=null) return t.sentence;
        return null;
    }

    public static Task getStrongestTask(List<Task> table) {
        if (table == null) return null;
        if (table.isEmpty()) return null;
        return table.get(0);
    }




//    public Sentence getSentence(final Sentence query, long now, final List<Task>... lists) {
//        Task t = getTask(query, now, lists);
//        if (t == null) return null;
//        return t.sentence;
//    }
}
