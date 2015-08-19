package nars.meta.pre;

import nars.Op;
import nars.meta.RuleMatch;
import nars.premise.Premise;
import nars.task.Task;

/**
 * Created by me on 8/18/15.
 */
public class TaskTermType extends BeliefTermType {

    public TaskTermType(Op o) {
        super(o);
    }

    @Override
    protected Task getTask(final Premise p) {
        return p.getTask();
    }

}
