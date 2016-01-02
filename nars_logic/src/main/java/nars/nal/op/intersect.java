package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compile.TermIndex;

public class intersect extends BinaryTermOperator {

    @Override public Term apply(Term a, Term b, TermIndex i) {
        return i.newTerm(a.op(), TermContainer.intersect(
                (TermContainer) a, (TermContainer) b
        ));
    }

}
