package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.nal.nal7.CyclesInterval;
import nars.nal.nal7.Temporal;
import nars.premise.Premise;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class MeasureTime extends AbstractMeasureTime {

    public MeasureTime(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    protected boolean testEvents(RuleMatch m, Term a, Term b, Term target) {
        Premise p = m.premise;

        long time = Temporal.between(p.getTask(), p.getBelief());
        if (time < 0) {
            return false;
        }

        CyclesInterval interval = CyclesInterval.make(time);
        m.xy.put(target, interval );

        return true;
    }
}
