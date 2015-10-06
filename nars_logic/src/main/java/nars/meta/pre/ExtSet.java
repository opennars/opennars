package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.nal.nal3.SetExt;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class ExtSet extends PreCondition1 {

    public ExtSet(Term var1) {
        super(var1);
    }

    @Override
    final public boolean test(final RuleMatch m, final Term a) {
        return (a instanceof SetExt);
    }

}
