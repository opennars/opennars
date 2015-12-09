package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class Equal extends PreCondition2 {

    /** commutivity: sort the terms */
    public static Equal make(Term a, Term b) {
        if (a.compareTo(b) <= 0) return new Equal(a, b);
        else                     return new Equal(b, a);
    }

    Equal(Term var1, Term var2) {
        super(var1, var2);
    }

    @Override
    public final boolean test(final RuleMatch m, final Term a, final Term b) {
        if ((a == null) || (b == null)) return false;
        return a.equals(b);
    }

}
