package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public abstract class PreCondition1Output extends PreCondition1 {

    protected PreCondition1Output(Term var1) {
        super(var1);
    }

    @Override
    public final boolean eval(RuleMatch m) {
        return test(m, arg1);
    }
}
