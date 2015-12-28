package nars.nal.op;

import com.gs.collections.api.set.MutableSet;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;

public class intersect extends BinaryTermOperator {

    @Override public Term apply(Term a, Term b) {
        MutableSet<Term> var = TermContainer.intersect(
            (Compound) a, (Compound) b
        );
        return ((Compound)a).clone(var.toArray(new Term[var.size()]));
    }

}
