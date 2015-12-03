package nars.nal.meta.post;

import nars.nal.RuleMatch;
import nars.nal.meta.pre.PreCondition3Output;
import nars.nal.nal3.SetTensional;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class Intersect extends PreCondition3Output {

    public Intersect(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {

        if (Unite.invalid(a,b,c))
            return false;

        //ok both are extensional sets or intensional sets, build intersection, not difference
        return Unite.createSetAndAddToSubstitutes(m, a, c,
            SetTensional.intersect(
                (SetTensional) a,(SetTensional) b
            )
        );
    }
}
