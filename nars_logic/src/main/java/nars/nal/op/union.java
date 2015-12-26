package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.Terms;
import nars.term.compound.Compound;

public class union extends BinaryTermOperator {
    
    @Override public Term apply(Term a, Term b) {
        return ((Compound)a).clone( TermContainer.union(
                (Compound) a, (Compound) b
        ).toArray(Terms.Empty));
    }

}
