package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.nal.nal7.AbstractInterval;
import nars.premise.Premise;
import nars.term.Atom;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class TimeOffset extends PreCondition1 {

    private final boolean positive;
    int direction;

    public TimeOffset(Term arg1, Term operator, boolean positive) {
        super(arg1);

        if (operator.equals(forwardImpl))
            direction = +1;
        else if (operator.equals(backImpl))
            direction = -1;
        else
            direction = 0;

        this.positive = positive;

    }

    @Override
    public boolean test(RuleMatch m, Term arg1) {

        if (arg1 == null) return false;

        long s = positive ? +1 : -1;
        m.occurence_shift += s * timeOffsetForward(arg1, m.premise);
        //m.occurence_shift += s * timeOffsetForward(arg2, m.premise);
        return true;
    }

    final static Atom forwardImpl = Atom.the("\"==/>\"");
    final static Atom backImpl = Atom.the("\"==\\>\"");


    long timeOffsetForward(final Term arg, final Premise nal) {

        if (arg instanceof AbstractInterval) {
            return ((AbstractInterval) arg).cycles(nal.memory());
        }
        else /*if (arg instanceof Atom)*/ {
            int dir = direction;
            if (dir!=0) {
                int d = nal.duration();
                return d * dir; /* -1, 0, or +1 */
            }
            return 0;
        }
    }

    @Override
    public String toString() {
        return (positive ? "Pos" : "Neg") + super.toString() + "[" + direction + "]";
    }
}
