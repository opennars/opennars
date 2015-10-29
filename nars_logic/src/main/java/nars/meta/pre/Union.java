package nars.meta.pre;

import nars.Global;
import nars.meta.RuleMatch;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal3.SetTensional;
import nars.term.Compound;
import nars.term.Term;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by me on 8/15/15.
 */
public class Union extends PreCondition3Output {

    public Union(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {

        if (Union.invalid(a,b,c)) return false;

        //ok both are extensional sets or intensional sets, build difference
        SetTensional A = (SetTensional) a;
        SetTensional B = (SetTensional) b;

        List<Term> terms = Global.newArrayList(A.volume() + B.volume());
        Collections.addAll(terms, A.terms());
        Collections.addAll(terms, B.terms());

        return createSetAndAddToSubstitutes(m, a, c, terms);
    }

    public static boolean createSetAndAddToSubstitutes(RuleMatch m, Term a, Term c, Collection<Term> termsArray) {
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

        m.map1.put(c, res);

        return true;
    }

    public static boolean invalid(Term a, Term b, Term c) {
        return  c==null ||
                !((a instanceof SetTensional) &&
                (a.op()==b.op()));
    }
}
