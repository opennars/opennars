package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.nal.nal3.SetTensional;
import nars.term.Term;

import java.util.ArrayList;

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

        ArrayList<Term> terms = new ArrayList<Term>();
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
        return Union.createSetAndAddToSubstitutes(m, a, c, terms);
    }
}
