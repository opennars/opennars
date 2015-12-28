package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;

public class differ extends BinaryTermOperator/*implements BinaryOperator<Term>*/ {

    @Override
    public Term apply(Term a, Term b, TermIndex i) {
        return i.term(a.op(), TermContainer.difference(
                (Compound) a, (Compound) b
        ));
    }
}
