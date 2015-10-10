package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.term.Term;

import java.util.Map;

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

        //the rule match context stores the Inp and Outp. not in this class.
        //no preconditions should store any state
        Map<Term, Term> Inp = m.Inp;// Global.newHashMap();

        if (b!=null) {

            Map<Term,Term> Outp = m.Outp; //Global.newHashMap();

            Outp.put(Inp.get(this.arg1),b);
            Inp.clear();

            return true;
        }
        Inp.clear(); //new HashMap<Term,Term>();
        return false;
    }

//    public HashMap<Term,Term> GetRegularSubs() {
//        HashMap<Term,Term> ret = new HashMap<Term,Term>();
//        ret.put(this.arg1, b);
//        return ret;
//    }
}
