package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.meta.TaskRule;
import nars.meta.pre.PreCondition2;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class TimeOffset extends PreCondition2 {

    private final boolean positive;

    public TimeOffset(Term arg1, Term arg2, boolean positive) {
        super(arg1, arg2);
        this.positive = positive;
    }

    @Override
    public boolean test(RuleMatch m, Term arg1, Term arg2) {
        long s = positive ? +1 : -1;
        m.occurence_shift += s * TaskRule.timeOffsetForward(arg1, m.premise);
        m.occurence_shift += s * TaskRule.timeOffsetForward(arg2, m.premise);
        return true;
    }

    @Override
    public String toString() {
        return (positive ? "Pos" : "Neg") + super.toString();
    }
}
