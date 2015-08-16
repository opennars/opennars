package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.nal.nal7.CyclesInterval;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class MeasureTime extends AbstractMeasureTime {

    public MeasureTime(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    protected boolean test(RuleMatch m, Term a, Term b, long time1, long time2, Term c) {
        long time = time1 - time2;
        if (time < 0) {
            return false;
        }

        m.assign.put(c,
                CyclesInterval.make(time, m.premise.getMemory())); // I:=+8 for example

        return true;
    }
}
