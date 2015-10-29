package nars.meta.pre;

import nars.Global;
import nars.meta.RuleMatch;
import nars.nal.nal3.SetTensional;
import nars.term.Term;

import java.util.List;

/**
 * Created by me on 8/15/15.
 */
public class Difference extends PreCondition3Output {

    public Difference(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {

        if (Union.invalid(a,b,c))
            return false;

        //ok both are extensional sets or intensional sets, build difference
        SetTensional A = (SetTensional) a;
        SetTensional B = (SetTensional) b;

        List<Term> terms = Global.newArrayList();
        for(Term t: A.terms()) { //set difference
            boolean include = true;
            for(Term t2 : B.terms()) {
                if(t.equals(t2)) {
                    include=false;
                    break;
                }
            }
            if(include) {
                terms.add(t);
            }
        }

        if (terms.isEmpty()) return false;

        return Union.createSetAndAddToSubstitutes(m, a, c, terms);
    }
}
