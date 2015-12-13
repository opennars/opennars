package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;

public class union extends BinaryTermOperator {
    
    @Override public Term apply(Term a, Term b) {
        return TermContainer.union(
            a.op(), (Compound) a, (Compound) b
        );
    }

}
