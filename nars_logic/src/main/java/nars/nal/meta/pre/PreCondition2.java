package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.term.Term;

/** tests the resolved terms specified by pattern variable terms */
abstract public class PreCondition2 extends PreCondition {
    public final Term arg1, arg2;
    private final String str;

    public PreCondition2(Term var1, Term var2) {
        this.arg1 = var1;
        this.arg2 = var2;
        this.str = getClass().getSimpleName() + ":(" + arg1 + ',' + arg2 + ')';
    }

    @Override public final boolean test(RuleMatch m) {
        return test(m,
                m.apply(arg1, false),
                m.apply(arg2, false));
    }

    abstract public boolean test(RuleMatch m, Term a, Term b);

    @Override
    public final String toString() {
        return str;
    }
}
