package nars.nal.meta.post;

import nars.nal.RuleMatch;
import nars.nal.meta.pre.PreCondition3Output;
import nars.nal.nal3.SetTensional;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class Differ extends PreCondition3Output {

    public Differ(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {

        if (Unite.invalid(a,b,c))
            return false;

        //ok both are extensional sets or intensional sets, build difference
        SetTensional A = (SetTensional) a;
        SetTensional B = (SetTensional) b;

        return Unite.createSetAndAddToSubstitutes(m, a, c, SetTensional.difference(A, B));
    }
}
