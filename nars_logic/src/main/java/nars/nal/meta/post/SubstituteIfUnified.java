package nars.nal.meta.post;

import nars.Op;
import nars.nal.RuleMatch;
import nars.term.Term;
import nars.term.atom.Atom;

/**
 * Created by me on 8/15/15.
 */
public class SubstituteIfUnified extends Substitute {

    static final Atom INDEP_VAR = Atom.the("$", true);
    static final Atom QUERY_VAR = Atom.the("?", true);
    static final Atom DEP_VAR = Atom.the("#", true);

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

        //noinspection IfStatementWithTooManyBranches
        if (varType.equals(QUERY_VAR))  {
            type = Op.VAR_QUERY;
        } else if (varType.equals(INDEP_VAR)) {
            type = Op.VAR_INDEP;
        } else if (varType.equals(DEP_VAR)) {
            type = Op.VAR_DEP;
        } else {
            throw new RuntimeException("invalid variable type: " + varType);
        }

        id = getClass().getSimpleName() + ":(\"" +
                type + "\"," + x + ',' + y + ')';
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    protected boolean substitute(RuleMatch m, Term a, Term b) {

//        FindSubst sub = new FindSubst(type, m.premise.getRandom());
//
//        boolean result;
//        if (sub.matchAll(a, b, Global.UNIFICATION_POWER)) {
//            m.secondary.putAll(sub.xy);
//            result = true;
//        }
//        else {
//            result = false;
//        }
//
//        return result;
        return false;
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
