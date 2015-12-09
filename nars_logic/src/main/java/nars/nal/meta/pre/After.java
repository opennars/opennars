package nars.nal.meta.pre;

import nars.Premise;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.task.Temporal;

/**
 * After(%X,%Y) Means that
 * %X is after %Y
 * TODO use less confusing terminology and order convention
 */
public class After extends PreCondition {

    public static final After the = new After();

    protected After() {
    }

    @Override
    public String toString() {
        return "After";
    }

    @Override
    public final boolean test(RuleMatch m) {
        Premise premise = m.premise;

        if (!premise.isEvent())
            return false;

        return ((Temporal) premise.getTask()).startsAfter(
            (Temporal) premise.getBelief()
        );
    }
}
