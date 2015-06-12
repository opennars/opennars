package nars.nal.task.filter;

import nars.nal.NAL;
import nars.nal.Sentence;
import nars.nal.Task;
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

    @Override public String reject(NAL nal, TaskSeed task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        if (!task.isJudgment())
            return null; //only process judgments

        //equality:
        //  1. term (given because it is looking up in concept)
        //  2. truth
        //  3. occurrence time
        //  4. evidential set

        Concept c = nal.memory.concept(task.getTerm());
        if (c == null)
            return null; //concept doesnt even exist so this is not a duplciate of anything

        if (!c.hasBeliefs())
            return null; //no beliefs exist at this concept

        /* TODO make this faster by terminating the search after confidence drops below the task's */
        for (Task t : c.getBeliefs()) {
            if (!t.getTruth().equals(task.getTruth()))
                return null; //different truth value
            if (t.getOccurrenceTime()!=task.getOccurrenceTime())
                return null; //differnt occurence time
            if (!Arrays.equals(t.getEvidentialSet(), task.getEvidentialSet()))
                return null; //differnt evidence
        }

        return DUPLICATE;
    }

}
