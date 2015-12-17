package nars.nal.meta.op;

import nars.Op;
import nars.term.Term;

/**
 * Created by me on 12/17/15.
 */
public final class TermOpEquals extends MatchOp {
    public final Op type;

    public TermOpEquals(Op type) {
        this.type = type;
    }

    @Override
    public boolean match(Term t) {
        return t.op() == type;
    }

    @Override
    public String toString() {
        return type.toString(); /* + " "*/
    }
}
