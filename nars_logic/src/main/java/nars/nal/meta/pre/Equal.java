package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class Equal extends PreCondition2 {

    public Equal(Term var1, Term var2) {
        super(var1, var2);
    }

    @Override
    final public boolean test(final RuleMatch m, final Term a, final Term b) {
        if ((a == null) || (b == null)) return false;
        return a.equals(b);
    }

}
