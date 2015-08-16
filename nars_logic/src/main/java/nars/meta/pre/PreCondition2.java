package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.term.Term;

/** tests the resolved terms specified by pattern variable terms */
abstract public class PreCondition2 extends PreCondition {
    public final Term arg1, arg2;

    public PreCondition2(Term var1, Term var2) {
        this.arg1 = var1;
        this.arg2 = var2;
    }
    @Override public boolean test(RuleMatch m) {
        //these should not resolve to null
        Term a = m.resolve(arg1);
        Term b = m.resolve(arg2);
        return test(m, a, b);
    }

    abstract public boolean test(RuleMatch m, Term a, Term b);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + arg1 + "," + arg2 + "]";
    }
}
