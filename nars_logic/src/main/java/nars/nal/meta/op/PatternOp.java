package nars.nal.meta.op;

import nars.nal.PremiseMatch;
import nars.nal.meta.AtomicBooleanCondition;
import nars.term.transform.FindSubst;

/**
 * Created by me on 12/1/15.
 */
public abstract class PatternOp extends AtomicBooleanCondition<PremiseMatch> {
    public abstract boolean run(FindSubst ff);

    @Override
    public final boolean booleanValueOf(PremiseMatch m) {
        return run(m);
    }
}
