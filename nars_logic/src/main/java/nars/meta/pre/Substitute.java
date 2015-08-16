package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.meta.pre.PreCondition2;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class Substitute extends PreCondition2 {

    public Substitute(Term arg1, Term arg2) {
        super(arg1, arg2);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b) {
        //Term M = b; //this one got substituted, but with what?
        //Term with = m.assign.get(M); //with what assign assigned it to (the match between the rule and the premises)
        //args[0] now encodes a variable which we want to replace with what M was assigned to
        //(relevant for variable elimination rules)
        if (b!=null) {
            m.precondsubs.put(this.arg1, b);
            return true;
        }
        return false;
    }
}
