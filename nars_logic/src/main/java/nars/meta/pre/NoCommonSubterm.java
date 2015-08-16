package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.meta.pre.PreCondition2;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;

/**
 * Created by me on 8/15/15.
 */
public class NoCommonSubterm extends PreCondition2 {
    public NoCommonSubterm(Term arg1, Term arg2) {
        super(arg1, arg2);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b) {
        //TODO this will only compare the first level of subterms
        //for recursive, we will need a stronger test
        //but we should decide if recursive is actually necessary
        //and create alternate noCommonSubterm and noCommonRecursiveSubterm
        //preconditions to be entirely clear

        if ((a instanceof Compound) && (b instanceof Compound))
            if (Terms.shareAnySubTerms((Compound) a, (Compound) b))
                return false;

        return true;
    }

    @Override
    public boolean isEarly() {
        return true;
    }
}
