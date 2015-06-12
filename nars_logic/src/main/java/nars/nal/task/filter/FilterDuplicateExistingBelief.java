package nars.nal.task.filter;

import nars.nal.NAL;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.Truth;
import nars.nal.concept.Concept;
import nars.nal.task.TaskSeed;

import java.util.Arrays;

/**
 * Prevent a duplicate belief from entering the system again
 */
public class FilterDuplicateExistingBelief implements DerivationFilter {

    public final static String DUPLICATE = "Duplicate";

    public FilterDuplicateExistingBelief() {

        }

    @Override public String reject(final NAL nal, final TaskSeed task, final boolean solution, final boolean revised, final boolean single, final Sentence currentBelief, final Task currentTask) {
        if (!task.isJudgment())
            return null; //only process judgments

        //equality:
        //  1. term (given because it is looking up in concept)
        //  2. truth
        //  3. occurrence time
        //  4. evidential set

        final Concept c = nal.memory.concept(task.getTerm());
        if (c == null)
            return null; //concept doesnt even exist so this is not a duplciate of anything

        if (!c.hasBeliefs())
            return null; //no beliefs exist at this concept



        final float conf = task.getTruth().getConfidence();

        for (Task t : c.getBeliefs()) {
            Truth tt = t.getTruth();

//            /* terminatesthe search after confidence drops below the task's */
//            if (tt.getConfidence() < conf)
//                return null;

            if (!tt.equals(task.getTruth()))
                return null; //different truth value
            if (t.getOccurrenceTime()!=task.getOccurrenceTime())
                return null; //differnt occurence time
            if (!Arrays.equals(t.getEvidentialSet(), task.getEvidentialSet()))
                return null; //differnt evidence
        }

        return DUPLICATE;
    }

}
