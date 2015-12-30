package nars.nal.meta.pre;

import nars.nal.PremiseMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class NotEqual extends PreCondition2 {

    /** commutivity: sort the terms */
    public static NotEqual make(Term a, Term b) {
        return a.compareTo(b) <= 0 ? new NotEqual(a, b) : new NotEqual(b, a);
    }

    NotEqual(Term var1, Term var2) {
        super(var1, var2);
    }

    @Override
    public final boolean test(PremiseMatch m, Term a, Term b) {
        return (a != null) && (b != null) && !a.equals(b);
    }

}
