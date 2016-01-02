package nars.nal.meta.pre;

import nars.Premise;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.task.Task;

/**
 * After(%X,%Y) Means that
 * %X is after %Y
 * TODO use less confusing terminology and order convention
 */
public class ConcurrentOrOneEternal extends PreCondition {

    public static final ConcurrentOrOneEternal the = new ConcurrentOrOneEternal();

    protected ConcurrentOrOneEternal() {
        super();
    }

    @Override
    public String toString() {
        return "after";
    }

    @Override
    public final boolean test(RuleMatch m) {
        final Premise premise = m.premise;

        final Task task = premise.getTask();
        final Task belief = premise.getBelief();

        if(task == null || belief == null) {
            return false;
        }

        boolean concurrent_or_one_eternal = task.isEternal() || belief.isEternal() || !(task.startsAfter(belief) || belief.startsAfter(task));
        return concurrent_or_one_eternal;
    }
}
