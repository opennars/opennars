package nars.nal.meta.pre;

import nars.nal.RuleMatch;
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
    public final boolean test(RuleMatch m, Term a) {
        return (a instanceof SetExt);
    }

}
