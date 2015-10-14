package nars.meta.pre;

import nars.Global;
import nars.Op;
import nars.meta.RuleMatch;
import nars.term.Atom;
import nars.term.Term;
import nars.term.transform.FindSubst;

import java.util.Map;

/**
 * Created by me on 8/15/15.
 */
public class SubsIfUnifies extends PreCondition3 {

    final Atom INDEP_VAR = Atom.the("$", true);
    final Atom QUERY_VAR = Atom.the("?", true);

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

        if (b == null || c == null) {
            Inp.clear();
            return false;
        }

        Map<Term, Term> Outp = m.Outp;



        final Op type;
        if (a.equals(QUERY_VAR))  {
            type = Op.VAR_QUERY;
        } else if (a.equals(INDEP_VAR)) {
            type = Op.VAR_INDEPENDENT;
        } else {
            type = Op.VAR_DEPENDENT;
        }

        Map<Term, Term> Left = Global.newHashMap(0);
        Map<Term, Term> Right = Global.newHashMap(0);
        FindSubst sub = new FindSubst(type, Left, Right, m.premise.getRandom());

        if (!sub.next(b, c, Global.UNIFICATION_POWER)) {
            return false;
        }

        Outp.putAll(Left);
        Inp.clear();

        return true;

    }

//    public HashMap<Term,Term> GetRegularSubs() {
//        HashMap<Term,Term> ret = new HashMap<Term,Term>();
//        ret.put(this.arg1, b);
//        return ret;
//    }
}
