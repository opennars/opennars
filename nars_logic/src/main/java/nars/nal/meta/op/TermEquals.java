package nars.nal.meta.op;

import nars.term.Term;

/**
 * Created by me on 12/17/15.
 */
public final class TermEquals extends MatchOp {
    public final Term a;

    public TermEquals(Term a) {
        this.a = a;
    }

    @Override
    public boolean match(Term t) {
        return a.equals(t);
    }

    @Override
    public String toString() {
        return "=" + a;
    }
}
