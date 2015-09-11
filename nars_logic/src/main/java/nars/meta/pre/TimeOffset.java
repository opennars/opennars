package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.Interval;
import nars.premise.Premise;
import nars.term.Atom;
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
        m.occurence_shift += s * timeOffsetForward(arg1, m.premise);
        m.occurence_shift += s * timeOffsetForward(arg2, m.premise);
        return true;
    }

    final static Atom forwardImpl = Atom.the("\"==/>\"");
    final static Atom backImpl = Atom.the("\"==\\>\"");


    static long timeOffsetForward(final Term arg, final Premise nal) {

        if (arg instanceof AbstractInterval) {
            return ((Interval) arg).cycles(nal.memory());
        }
        else if (arg instanceof Atom) {
            if (arg.equals(forwardImpl))
                return nal.duration();
            else if (arg.equals(backImpl))
                return -nal.duration();
        }

        return 0;
    }

    @Override
    public String toString() {
        return (positive ? "Pos" : "Neg") + super.toString();
    }
}
