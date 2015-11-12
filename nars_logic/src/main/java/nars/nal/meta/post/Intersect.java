package nars.nal.meta.post;

import com.gs.collections.api.set.MutableSet;
import com.gs.collections.impl.factory.Sets;
import nars.Global;
import nars.nal.RuleMatch;
import nars.nal.meta.pre.PreCondition3Output;
import nars.nal.nal3.SetTensional;
import nars.term.Term;

import java.util.Collections;
import java.util.Set;

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

        Set<Term> aa = Global.newHashSet(A.size());
        Collections.addAll(aa, A.terms());
        Set<Term> bb = Global.newHashSet(B.size());
        Collections.addAll(bb, B.terms());

        MutableSet<Term> terms = Sets.intersect(bb,aa);
        if (terms.isEmpty()) return false;

        return Unite.createSetAndAddToSubstitutes(m, a, c, terms);
    }
}
