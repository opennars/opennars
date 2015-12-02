package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class NotEqual extends PreCondition2 {

    /** commutivity: sort the terms */
    public static NotEqual make(Term a, Term b) {
        if (a.compareTo(b) <= 0) return new NotEqual(a, b);
        else                     return new NotEqual(b, a);
    }

    NotEqual(Term var1, Term var2) {
        super(var1, var2);
    }

    @Override
    final public boolean test(final RuleMatch m, final Term a, final Term b) {
        if ((a == null) || (b == null)) return false;
        return !a.equals(b);
    }

}
