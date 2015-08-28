package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 8/18/15.
 */
public class TaskTermMinVolume extends PreCondition {

    final int minVolume;

    public TaskTermMinVolume(final int minVolume) {
        this.minVolume = minVolume;
    }

    @Override
    public boolean test(RuleMatch ruleMatch) {
        final Task b = getTask(ruleMatch.premise);
        if (b == null) return false;
        final Term t = b.getTerm();
        return t.volume() >= minVolume;
    }

    protected Task getTask(final Premise p) {
        return p.getTask();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + minVolume + "]";
    }


}
