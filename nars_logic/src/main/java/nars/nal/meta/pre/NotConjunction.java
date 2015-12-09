package nars.nal.meta.pre;

import nars.Op;
import nars.nal.RuleMatch;
import nars.term.Term;


public final class NotConjunction extends PreCondition1 {

    public NotConjunction(final Term arg1) {
        super(arg1);
    }

    @Override
    public boolean test(final RuleMatch m, final Term arg1) {

        if (arg1 == null) return false;

        //TODO use a bitvector to test Op membership in this set
        //  and then abstract this to a generic Precondition
        //  that can be used for allowing (+) or denying (-)
        //  other sets of Ops

        final Op o = arg1.op();
        switch (o) {
            case CONJUNCTION:
            case SEQUENCE:
            case PARALLEL:
                return false;
        }
        return true;
    }

}
