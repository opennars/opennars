package nars.meta.pre;

import com.gs.collections.api.set.MutableSet;
import com.gs.collections.impl.factory.Sets;
import nars.Global;
import nars.meta.RuleMatch;
import nars.nal.nal3.SetTensional;
import nars.term.Term;

import java.util.Collections;
import java.util.Set;

/**
 * Created by me on 8/15/15.
 */
public class Intersection extends PreCondition3Output {

    public Intersection(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {

        if (Union.invalid(a,b,c))
            return false;
//        if(a==null || b==null || c==null ||
//                !((a instanceof SetTensional) && (a.op()==b.op()))
////                ( !((a instanceof SetExt) && (b instanceof SetExt)) &&
////                  !((a instanceof SetInt) && (b instanceof SetInt)))
//                ) {
//            return false;
//        }

        //ok both are extensional sets or intensional sets, build intersection, not difference
        SetTensional A = (SetTensional) a;
        SetTensional B = (SetTensional) b;

        Set<Term> aa = Global.newHashSet(A.length());
        Collections.addAll(aa, A.terms());
        Set<Term> bb = Global.newHashSet(B.length());
        Collections.addAll(bb, B.terms());

        MutableSet<Term> terms = Sets.intersect(bb,aa);
        return Union.createSetAndAddToSubstitutes(m, a, c, terms);
    }
}
