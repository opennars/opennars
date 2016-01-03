package nars.nal.op;

import nars.term.Term;
import nars.term.compile.TermBuilder;
import nars.term.compound.Compound;

/**
 * Created by me on 12/12/15.
 */
public abstract class BinaryTermOperator extends ImmediateTermTransform {

    @Override public final Term function(Compound x, TermBuilder i) {
        if (x.size()<2)
            throw new RuntimeException(this + " requires >= 2 args");

        return apply(x.term(0), x.term(1), i);
    }

    public abstract Term apply(Term a, Term b, TermBuilder i);
}
