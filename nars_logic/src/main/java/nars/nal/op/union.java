package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.TermSet;
import nars.term.compile.TermBuilder;

public class union extends BinaryTermOperator {
    
    @Override public Term apply(Term a, Term b, TermBuilder i) {
        return i.newTerm(a.op(), TermSet.union(
                (TermContainer) a, (TermContainer) b
        ));
    }

}
