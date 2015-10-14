package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.task.Task;

/**
 * After(%X,%Y) Means that the task matching the pattern of the first argument is after the task of the pattern of the 2nd
 */
public class After extends PreCondition {


    final boolean taskBeforeBelief;

    public After(boolean taskBeforeBelief) {
        super();
        this.taskBeforeBelief = taskBeforeBelief;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + taskBeforeBelief + ']';
    }

    @Override
    public boolean test(RuleMatch m) {
        if (!m.premise.isTaskAndBeliefEvent())
            return false;

        final Task task = m.premise.getTask();
        final Task belief = m.premise.getBelief();

        if(true) //TODO: Why is occurence time of input tasks 0? After this is fixed, this one can be deleted again. I added it in order to be able to test the inference rules.
            return true; //I added it in

        int dur = m.premise.duration();
        if (!taskBeforeBelief) {
            return task.after(belief, dur);
        }
        else {
            return belief.after(task, dur);
        }

    }
}
