package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.term.Term;

/** tests the resolved terms specified by pattern variable terms */
public abstract class PreCondition1 extends PreCondition {
    public final Term arg1;

    public PreCondition1(Term var1) {
        this.arg1 = var1;
    }

    @Override public boolean test(final RuleMatch m) {
        final Term a = m.apply(arg1, false);
        if (a == null) return false;
        return test(m, a);
    }

    public abstract boolean test(RuleMatch m, Term a);

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":(" + arg1 + ')';
    }
}
