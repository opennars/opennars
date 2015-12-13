package nars.term.transform;

import nars.Op;
import nars.term.Term;

/**
 * Created by me on 12/13/15.
 */
public final class NotOpConstraint implements MatchConstraint {

    private final int op;

    public NotOpConstraint(Op o) {
        this(o.bit());
    }
    public NotOpConstraint(int opVector) {
        this.op = opVector;
    }

    @Override
    public boolean invalid(Term assignee, Term value, FindSubst f) {
        return value.op().isA(op);
    }
    @Override
    public String toString() {
        return "op!=" + op;
    }
}

