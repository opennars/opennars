package nars.nal.meta.pre;

import nars.Premise;
import nars.nal.RuleMatch;
import nars.nal.nal7.CyclesInterval;
import nars.nal.nal7.Tense;
import nars.term.Term;
import nars.term.Variable;

/**
 * Created by me on 8/15/15.
 */
public class MeasureTime extends AbstractMeasureTime {

    public MeasureTime(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    /**
     * HACK this ignores the parameters 'a' and 'b'
     * because this rule only appears once we hardcode
     * what it means.  The third parameter specifies the
     * pattern variable where interval
     * term representing the time difference will be
     * substituted.
     */
    @Override protected boolean testEvents(RuleMatch m, Term a, Term b, Term target) {
        Premise p = m.premise;

        int time = Tense.between(p.getTask(), p.getBelief());
        if (time < 0) {
            return false;
        }

        CyclesInterval interval = CyclesInterval.make(time);
        m.xy.put((Variable)target, interval );

        return true;
    }
}
