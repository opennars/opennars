package nars.concept;

import nars.Memory;
import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.truth.Truth;
import nars.truth.Truthed;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import static nars.nal.nal7.TemporalRules.solutionQuality;

/**
 * A model storing, ranking, and projecting beliefs or goals (tasks with TruthValue).
 * It should iterate in top-down order (highest ranking first)
 */
public interface BeliefTable extends TaskTable {

    public float rank(final Task s, final long now);


    /** attempt to insert a task.
     *
     * @param c the concept in which this occurrs
     * @return:
     *      the input value that was inserted, if it was added to the table
     *      a previous stored task if this was a duplicate (table unchanged)
     *      a new belief created from older ones which serves as a revision of what was input, if it was added to the table
     *
     */
    public Task add(Task input, Concept c);

    @Deprecated /* TEMPORAR */ Task addGoal(Task input, Concept c);

    /**
     * matches existing, or projects to a new task
     * was: getTask(q, now, getBeliefs()) */
    public Task match(Task q, long now);



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


    /**
     * Select a belief value or desire value for a given query
     *
     * @param query The query to be processed
     * @param list  The list of beliefs or goals to be used
     * @return The best candidate selected
     */
    public static Task getTask(final Sentence query, long now, final List<Task>... lists) {
        float currentBest = 0;
        float beliefQuality;
        Task candidate = null;

        for (List<Task> list : lists) {
            if (list.isEmpty()) continue;

            int lsv = list.size();
            for (int i = 0; i < lsv; i++) {
                Task judg = list.get(i);
                beliefQuality = solutionQuality(query, judg.sentence, now);
                if (beliefQuality > currentBest) {
                    currentBest = beliefQuality;
                    candidate = judg;
                }
            }
        }

        return candidate;
    }

    public static Task getTask(boolean hasQueryVar, long now, long occTime, Truth truth, final List<Task>... lists) {
        float currentBest = 0;
        float beliefQuality;
        Task candidate = null;

        for (List<Task> list : lists) {
            if (list.isEmpty()) continue;

            int lsv = list.size();
            for (int i = 0; i < lsv; i++) {
                Task judg = list.get(i);
                beliefQuality = solutionQuality(hasQueryVar, occTime, judg.sentence, truth, now);
                if (beliefQuality > currentBest) {
                    currentBest = beliefQuality;
                    candidate = judg;
                }
            }
        }

        return candidate;
    }

    /** get the top-ranking belief/goal */
    public Task<Compound> top();

    /** the truth v alue of the topmost element, or null if there is none */
    default public Truth topTruth() {
        if (isEmpty()) return null;
        return top().getTruth();
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
