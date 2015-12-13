package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;

public class differ extends BinaryTermOperator/*implements BinaryOperator<Term>*/ {

    @Override
    public Term apply(Term a, Term b) {
        return TermContainer.difference(
            a.op(), (Compound) a, (Compound) b
        );
    }
}
