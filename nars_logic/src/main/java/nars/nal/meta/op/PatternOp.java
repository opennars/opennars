package nars.nal.meta.op;

import nars.nal.PremiseMatch;
import nars.nal.meta.BooleanCondition;
import nars.term.transform.FindSubst;

/**
 * Created by me on 12/1/15.
 */
public abstract class PatternOp extends BooleanCondition<PremiseMatch> {
    public abstract boolean run(FindSubst ff);

    @Override
    public final boolean booleanValueOf(PremiseMatch m) {
        return run(m);
    }
}
