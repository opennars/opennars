package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.nal.nal3.SetInt;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class IntSet extends PreCondition1 {

    public IntSet(Term var1) {
        super(var1);
    }

    @Override
    public final boolean test(final RuleMatch m, final Term a) {
        return (a instanceof SetInt);
    }

}
