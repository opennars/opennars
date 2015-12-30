package nars.nal.meta.pre;

import nars.nal.PremiseMatch;
import nars.nal.meta.BooleanCondition;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public abstract class PreCondition3 extends BooleanCondition {
    public Term arg1=null, arg2=null, arg3=null;


    /** no arguments should be null */
    protected PreCondition3(Term var1, Term var2, Term var3) {
        arg1 = var1;
        arg2 = var2;
        arg3 = var3;
    }

    @Override
    public boolean eval(PremiseMatch m) {
        //these should not resolve to null
        Term a = m.apply(arg1);
        if (a == null) return false;
        Term b = m.apply(arg2);
        if (b == null) return false;
        Term c = m.apply(arg3);
        if (c == null) return false;

        return test(m, a, b, c);
    }

    public abstract boolean test(PremiseMatch m, Term a, Term b, Term c);

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + arg1 + ',' + arg2 + ',' + arg3 + ']';
    }
}
