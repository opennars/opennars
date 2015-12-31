package nars.nal.meta.pre;

import nars.nal.PremiseMatch;
import nars.nal.meta.AtomicBooleanCondition;
import nars.term.Term;

/** tests the resolved terms specified by pattern variable terms */
public abstract class PreCondition2 extends AtomicBooleanCondition<PremiseMatch> {
    public final Term arg1, arg2;
    private final String str;

    protected PreCondition2(Term var1, Term var2) {
        arg1 = var1;
        arg2 = var2;
        str = getClass().getSimpleName() + ":(" + arg1 + ',' + arg2 + ')';
    }

    @Override
    public boolean booleanValueOf(PremiseMatch m) {
        return test(m,
                m.apply(arg1),
                m.apply(arg2));
    }

    public abstract boolean test(PremiseMatch m, Term a, Term b);

    @Override
    public final String toString() {
        return str;
    }
}
