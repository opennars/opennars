package nars.nal.meta.post;

import nars.nal.RuleMatch;
import nars.nal.meta.pre.PreCondition3Output;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;

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
        return Unite.createSetAndAddToSubstitutes(m, a, c,
            TermContainer.difference((Compound)a, (Compound)b)
        );
    }
}
