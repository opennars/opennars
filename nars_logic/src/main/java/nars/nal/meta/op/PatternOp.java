package nars.nal.meta.op;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.term.transform.FindSubst;

/**
 * Created by me on 12/1/15.
 */
public abstract class PatternOp extends PreCondition {
    abstract public boolean run(FindSubst ff);

    @Override
    public final boolean test(RuleMatch ruleMatch) {
        return run(ruleMatch);
    }
}
