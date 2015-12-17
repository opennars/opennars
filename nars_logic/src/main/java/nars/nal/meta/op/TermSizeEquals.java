package nars.nal.meta.op;

import nars.term.Term;

/**
 * Created by me on 12/17/15.
 */
public final class TermSizeEquals extends MatchOp {
    public final int size;

    public TermSizeEquals(int size) {
        this.size = size;
    }

    @Override
    public boolean match(Term t) {
        return t.size() == size;
    }

    @Override
    public String toString() {
        return "size=" + size;
    }
}
