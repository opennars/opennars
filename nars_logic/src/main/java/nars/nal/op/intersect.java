package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compile.TermBuilder;

public class intersect extends BinaryTermOperator {

    @Override public Term apply(Term a, Term b, TermBuilder i) {
        return i.newTerm(a.op(), TermContainer.intersect(
                (TermContainer) a, (TermContainer) b
        ));
    }

}
