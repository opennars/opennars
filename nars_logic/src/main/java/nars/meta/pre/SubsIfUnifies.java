package nars.meta.pre;

import nars.Global;
import nars.Op;
import nars.Symbols;
import nars.meta.RuleMatch;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.FindSubst;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by me on 8/15/15.
 */
public class SubsIfUnifies extends PreCondition3 {

    public SubsIfUnifies(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {

        //Term M = b; //this one got substituted, but with what?
        //Term with = m.assign.get(M); //with what assign assigned it to (the match between the rule and the premises)
        //args[0] now encodes a variable which we want to replace with what M was assigned to
        //(relevant for variable elimination rules)

        //the rule match context stores the Inp and Outp. not in this class.
        //no preconditions should store any state
        Map<Term, Term> Inp = m.Inp;// Global.newHashMap();

        if(b!=null && c!=null) {

            Map<Term,Term> Outp = m.Outp;
            Op type = Op.VAR_INDEPENDENT;

            if(a.toString().contains("$")) {
                type = Op.VAR_INDEPENDENT;
            }

            if(a.toString().contains("?")) {
                type = Op.VAR_QUERY;
            }

            Map<Term,Term> Left = new HashMap<Term,Term>();
            Map<Term,Term> Right = new HashMap<Term,Term>();
            FindSubst sub = new FindSubst(type, Left, Right, new Random());

            if(!sub.next(b,c,Global.UNIFICATION_POWER)) {
                return false;
            }

            Outp.putAll(Left);
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
