package nars.nal.meta.pre;

import nars.Op;
import nars.nal.RuleMatch;
import nars.term.Term;


final public class NotImplOrEquiv extends PreCondition1 {

    public NotImplOrEquiv(final Term arg1) {
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
            case IMPLICATION:
            case IMPLICATION_AFTER:
            case IMPLICATION_BEFORE:
            case IMPLICATION_WHEN:
            case EQUIVALENCE:
            case EQUIVALENCE_AFTER:
            case EQUIVALENCE_WHEN:
                return false;
        }
        return true;
    }

}
