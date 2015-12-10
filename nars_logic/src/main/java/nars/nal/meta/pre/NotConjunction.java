package nars.nal.meta.pre;

import nars.Op;
import nars.nal.RuleMatch;
import nars.term.Term;


public final class NotConjunction extends PreCondition1 {

    public NotConjunction(Term arg1) {
        super(arg1);
    }

    @Override
    public final boolean test(RuleMatch m, Term arg1) {

        return (arg1!=null) &&
               !arg1.op().isA(Op.ConjunctivesBits);
    }

}
