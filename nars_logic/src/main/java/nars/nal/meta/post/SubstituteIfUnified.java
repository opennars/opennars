package nars.nal.meta.post;

import nars.Global;
import nars.Op;
import nars.nal.RuleMatch;
import nars.term.Atom;
import nars.term.Term;
import nars.term.transform.FindSubst;
import nars.term.transform.Subst;

/**
 * Created by me on 8/15/15.
 */
public class SubstituteIfUnified extends Substitute {

    final static Atom INDEP_VAR = Atom.the("$", true);
    final static Atom QUERY_VAR = Atom.the("?", true);
    final static Atom DEP_VAR = Atom.the("#", true);

    private final Op type;
    private final transient String id;

    /**
     *
     * @param varType type for variable unification condition
     * @param x original term
     * @param y replacement term
     */
    public SubstituteIfUnified(Term varType, Term x, Term y) {
        super(x, y);

        if (varType.equals(QUERY_VAR))  {
            type = Op.VAR_QUERY;
        } else if (varType.equals(INDEP_VAR)) {
            type = Op.VAR_INDEPENDENT;
        } else if (varType.equals(DEP_VAR)) {
            type = Op.VAR_DEPENDENT;
        } else {
            throw new RuntimeException("invalid variable type: " + varType);
        }

        this.id = getClass().getSimpleName() + ":(" +
                type + "_," + x + ',' + y + ')';
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    protected boolean substitute(RuleMatch m, Term a, Term b) {

//        Map<Term, Term> left = m.sub2.left;
//        Map<Term, Term> right = m.sub2.right;

        Subst sub = new FindSubst(type, m.premise.getRandom());

        final boolean result;
        if (sub.next(a, b, Global.UNIFICATION_POWER)) {

            m.secondary.putAll(sub.xy);
            result = true;
        }
        else {
            result = false;
        }

        return result;
    }

//    @Override
//    public boolean test(RuleMatch m, Term b, Term c) {
//
//        //Term M = b; //this one got substituted, but with what?
//        //Term with = m.assign.get(M); //with what assign assigned it to (the match between the rule and the premises)
//        //args[0] now encodes a variable which we want to replace with what M was assigned to
//        //(relevant for variable elimination rules)
//
//        //the rule match context stores the Inp and outp. not in this class.
//        //no preconditions should store any state
//
//        if (b == null || c == null) {
//            return false;
//        }
//
//        Map<Variable, Term> outp = m.outp;
//
//
//
//
//        outp.putAll(Left);
//
//        return true;
//
//    }

//    public HashMap<Term,Term> GetRegularSubs() {
//        HashMap<Term,Term> ret = new HashMap<Term,Term>();
//        ret.put(this.arg1, b);
//        return ret;
//    }
}
