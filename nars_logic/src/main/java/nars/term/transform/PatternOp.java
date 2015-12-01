package nars.term.transform;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;

/**
 * Created by me on 12/1/15.
 */
public abstract class PatternOp extends PreCondition {
    abstract boolean run(Frame ff);

    @Override
    public final boolean test(RuleMatch ruleMatch) {
        return run(ruleMatch.subst);
    }
}
