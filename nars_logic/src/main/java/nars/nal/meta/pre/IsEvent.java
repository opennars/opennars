package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;

/**
 * Created by me on 8/15/15.
 */
public class IsEvent extends PreCondition {

    public static final PreCondition the = new IsEvent();

    protected IsEvent() {
        super();
    }

    @Override public final String toString() {
        return "IsEvent";
    }

    @Override
    public final boolean test(final RuleMatch m) {
        return m.premise.isTaskAndBeliefEvent();
    }

//    public static boolean isTemporal(Term a, Task task) {
//        return a.equals(task.getTerm()) && !Tense.isEternal(task.getOccurrenceTime());
//    }
}
