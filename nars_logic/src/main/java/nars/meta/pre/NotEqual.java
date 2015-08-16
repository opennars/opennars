package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.meta.pre.PreCondition2;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class NotEqual extends PreCondition2 {
    public NotEqual(Term var1, Term var2) {
        super(var1, var2);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b) {
        return !a.equals(b);
    }

    @Override
    public boolean isEarly() {
        return true;
    }
}
