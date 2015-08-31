package nars.task.filter;

import nars.concept.Concept;
import nars.premise.Premise;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Compound;
import nars.truth.Truth;

import java.util.Arrays;

/**
 * Prevent a duplicate belief from entering the system again
 */
public class FilterDuplicateExistingBelief implements DerivationFilter {

    public final static String DUPLICATE = "Duplicate";



    @Override public final String reject(final Premise nal, final TaskSeed task, final boolean solution, final boolean revised) {


        //only process non-solution judgments
        if (solution || !task.isJudgment())
            return VALID;

        Compound taskTerm = task.getTerm();


        //equality:
        //  1. term (given because it is looking up in concept)
        //  2. truth
        //  3. occurrence time
        //  4. evidential set

        final Concept c = nal.concept(taskTerm);
        if ((c == null) || //concept doesnt even exist so this is not a duplciate of anything
                (!c.hasBeliefs())) //no beliefs exist at this concept
            return VALID;

        for (Task t : c.getBeliefs()) {

            final Truth tt = t.getTruth();

            if (

                    //different truth value
                    (!tt.equals(task.getTruth()))
                            ||

                    //differnt occurence time
                    (t.getOccurrenceTime()!=task.getOccurrenceTime())
                            ||

                    //differnt evidence
                    (!Arrays.equals(t.getEvidence(), task.getEvidence()))

                )
                return VALID;
        }

        return DUPLICATE;
    }

}
