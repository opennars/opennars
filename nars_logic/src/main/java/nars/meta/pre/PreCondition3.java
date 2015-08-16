package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
abstract public class PreCondition3 extends PreCondition {
    public final Term arg1, arg2, arg3;

    /** no arguments should be null */
    public PreCondition3(Term var1, Term var2, Term var3) {
        super();
        this.arg1 = var1;
        this.arg2 = var2;
        this.arg3 = var3;
    }

    @Override
    public boolean test(RuleMatch m) {
        //these should not resolve to null
        Term a = m.resolve(arg1);
        Term b = m.resolve(arg2);
        Term c = m.resolve(arg3);
        if (c!=null)
            return test(m, a, b, c);
        return false;
    }

    abstract public boolean test(RuleMatch m, Term a, Term b, Term c);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + arg1 + "," + arg2 + "," + arg3 + "]";
    }
}
