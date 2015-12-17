package nars.nal.meta.op;

import nars.term.Term;
import nars.term.compound.Compound;

/**
 * Imdex == image index
 */
public final class ImageIndexEquals extends MatchOp {
    public final int index;

    public ImageIndexEquals(int index) {
        this.index = index;
    }

    @Override
    public boolean match(Term t) {
        return ((Compound) t).relation() == index;
    }

    @Override
    public String toString() {
        return "imdex:" + index;
    }
}
