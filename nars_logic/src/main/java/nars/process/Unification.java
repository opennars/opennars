package nars.process;

import nars.Global;
import nars.Op;
import nars.nal.nal7.TemporalRules;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.term.transform.FindSubst;

import java.util.Map;
import java.util.Random;

import static nars.nal.nal1.LocalRules.trySolution;

/**
 * Created by me on 9/28/15.
 */
public class Unification {

    /* -------------------- same contents -------------------- */
    /**
     * The task and belief have the same content
     * <p>
     * called in RuleTables.reason
     *
     * @param task The task
     * @param belief The belief
     */
    public static boolean match(final Task task, final Task belief, final NAL nal) {

        if (task.isQuestion() || task.isGoal()) {
            if (TemporalRules.matchingOrder(task, belief)) {
                Term[] u = new Term[] { task.getTerm(), belief.getTerm() };
                if (unify(Op.VAR_QUERY, u, nal.getRandom())) {
                    return trySolution(belief, task, nal)!=null;
                }
            }
        }
        return false;
    }

    /**
     * To unify two terms
     *
     * @param varType The varType of variable that can be substituted
     * @param t    The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     */
    public static boolean unify(final Op varType, final Term[] t, final Random random) {
        final Map<Term, Term> map[] = new Map[2]; //begins empty: null,null

        final boolean hasSubs = new FindSubst(varType, map[0], map[1], random).next(t[0], t[1], Global.UNIFICATION_POWER);
        if (hasSubs) {
            final Term a = applySubstituteAndRenameVariables(((Compound) t[0]), map[0]);
            if (a == null) return false;

            final Term b = applySubstituteAndRenameVariables(((Compound) t[1]), map[1]);
            if (b == null) return false;


            if(t[0] instanceof Variable && t[0].hasVarQuery() && (a.hasVarIndep() || a.hasVarDep()) ) {
                return false;
            }
            if(t[1] instanceof Variable && t[1].hasVarQuery() && (b.hasVarIndep() || b.hasVarDep()) ) {
                return false;
            }

            //only set the values if it will return true, otherwise if it returns false the callee can expect its original values untouched
            t[0] = a;
            t[1] = b;
            return true;
        }
        return false;
    }

    /**
     * appliesSubstitute and renameVariables, resulting in a cloned object,
     * will not change this instance
     */
    public static Term applySubstituteAndRenameVariables(final Compound t, final Map<Term, Term> subs) {
        if ((subs == null) || (subs.isEmpty())) {
            //no change needed
            return t;
        }

        Term r = t.applySubstitute(subs);

        if (r == null) return null;

        if (r.equals(t)) return t;

        return r;
    }
}
