package nars.task;

import nars.budget.Budget;
import nars.premise.Premise;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.truth.Truth;

/** dummy task useful for recording the known components of an aborted derivation */
public class TaskGhost extends DefaultTask {

    public TaskGhost(Compound term, char punctuation, Truth truth, Budget b, long occurr, Premise reason) {
        super(term, punctuation, truth, b, reason.getTask(), reason.getBelief(), null);

        if (occurr!= Stamp.TIMELESS)
            setOccurrenceTime(occurr);

    }

}
