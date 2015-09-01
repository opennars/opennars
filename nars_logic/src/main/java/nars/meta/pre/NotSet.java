package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.meta.pre.PreCondition2;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class NotSet extends PreCondition1 {

    public NotSet(Term var1) {
        super(var1);
    }

    @Override
    final public boolean test(final RuleMatch m, final Term a) {
        return !(a instanceof SetExt || a instanceof SetInt);
    }

}
