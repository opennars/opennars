package nars.nal.meta.post;

import nars.nal.RuleMatch;
import nars.nal.meta.pre.PreCondition3Output;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal3.SetTensional;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;
import nars.term.Variable;

import java.util.Set;

/**
 * Created by me on 8/15/15.
 */
public class Unite extends PreCondition3Output {

    public Unite(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {

        if (Unite.invalid(a,b,c)) return false;

        //ok both are extensional sets or intensional sets, build difference
        SetTensional A = (SetTensional) a;
        SetTensional B = (SetTensional) b;

        return createSetAndAddToSubstitutes(m, a, c, Terms.concat( A.terms(), B.terms() ));
    }

    public static boolean createSetAndAddToSubstitutes(RuleMatch m, Term a, Term c, Set<Term> terms) {
        if (terms.isEmpty()) return false;

        return createSetAndAddToSubstitutes(m, a, c, terms.toArray(new Term[terms.size()]) );
    }

    public static boolean createSetAndAddToSubstitutes(RuleMatch m, Term a, Term c, Term[] termsArray) {
        final Compound res;

        if(a instanceof SetExt) {
            res = SetExt.make(termsArray);
        }
        else {
            res = SetInt.make(termsArray);
        }

        if(res==null)
            throw new RuntimeException("this condition should have been trapped earlier");
            //return false;

        m.secondary.get().putXY((Variable)c, res);

        return true;
    }

    public static boolean invalid(Term a, Term b, Term c) {
        return  c==null ||
                !((a instanceof SetTensional) &&
                (a.op()==b.op()));
    }


}
