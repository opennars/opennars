package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class NotEqual extends PreCondition2 {

    public NotEqual(Term var1, Term var2) {
        super(var1, var2);
    }

    @Override
    final public boolean test(final RuleMatch m, final Term a, final Term b) {
        if (a == null) return false;
        return !a.equals(b);
    }

    /*@Override
    public boolean isEarly() {
        return true;
    }*/
}
