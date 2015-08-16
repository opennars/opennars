package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.term.Term;

/** tests the resolved terms specified by pattern variable terms */
abstract public class PreCondition1 extends PreCondition {
    public final Term arg1;

    public PreCondition1(Term var1) {
        this.arg1 = var1;
    }
    @Override public boolean test(RuleMatch m) {
        //these should not resolve to null
        Term a = m.resolve(arg1);
        return test(m, a);
    }

    abstract public boolean test(RuleMatch m, Term a);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + arg1 + "]";
    }
}
