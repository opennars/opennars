package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.term.Term;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by me on 8/15/15.
 */
public class Substitute extends PreCondition2 {

    public Substitute(Term arg1, Term arg2) {
        super(arg1, arg2);
    }

    public Map<Term,Term> Inp = new HashMap<Term,Term>();
    public Map<Term,Term> Outp = new HashMap<Term,Term>();

    public Term b=null;
    @Override
    public boolean test(RuleMatch m, Term a, Term b) {

        this.b=b;
        Outp = new HashMap<Term,Term>();
        //Term M = b; //this one got substituted, but with what?
        //Term with = m.assign.get(M); //with what assign assigned it to (the match between the rule and the premises)
        //args[0] now encodes a variable which we want to replace with what M was assigned to
        //(relevant for variable elimination rules)
        if (b!=null) {

           // m.map2.put(this.arg1, b); //map2!
            Term eventual_substituted_by_match = Inp.get(this.arg1);
            if(eventual_substituted_by_match == null) {
                eventual_substituted_by_match = this.arg1;
            }
            Outp.put(eventual_substituted_by_match,b);
            Inp = new HashMap<Term,Term>();
            return true;
        }
        Inp = new HashMap<Term,Term>();
        return false;
    }

    public HashMap<Term,Term> GetRegularSubs() {
        HashMap<Term,Term> ret = new HashMap<Term,Term>();
        ret.put(this.arg1, b);
        return ret;
    }
}
