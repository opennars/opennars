package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compile.TermBuilder;
import nars.term.compound.Compound;

public class differ extends BinaryTermOperator/*implements BinaryOperator<Term>*/ {

    @Override
    public Term apply(Term a, Term b, TermBuilder i) {
        //TODO construct TermSet directly
        return i.newTerm(a.op(), TermContainer.difference(
                (Compound) a, (Compound) b
        ));
    }
}
