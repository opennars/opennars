package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;

public class union extends BinaryTermOperator {
    
    @Override public Term apply(Term a, Term b, TermIndex i) {
        return i.newTerm(a.op(), TermContainer.union(
                (Compound) a, (Compound) b
        ));
    }

}
