package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
abstract public class PreCondition1Output extends PreCondition1 {

    public PreCondition1Output(Term var1) {
        super(var1);
    }

    @Override
    public final boolean test(RuleMatch m) {
        return test(m, arg1);
    }
}
