package nars.process;

import nars.Global;
import nars.Op;
import nars.nal.nal1.LocalRules;
import nars.nal.nal7.Temporal;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.FindSubst;

import java.util.Map;
import java.util.Random;


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
     * @param question The task
     * @param solution The belief
     * @return null if no match
     */
    public static Task match(final Task question, final Task solution, final NAL nal) {

        if (question.isQuestion() || question.isGoal()) {
            if (Temporal.matchingOrder(question, solution)) {
                Term[] u = new Term[]{question.getTerm(), solution.getTerm()};
                if (unify(Op.VAR_QUERY, u, nal.getRandom())) {
                    return LocalRules.trySolution(question, solution, nal);
                }
            }
        }

        return solution;
    }

    /**
     * To unify two terms
     *
     * @param varType The varType of variable that can be substituted
     * @param t       The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     * <p>
     * only sets the values if it will return true, otherwise if it returns false the callee can expect its original values untouched
     */
    public static boolean unify(final Op varType, final Term[] t, final Random random) {


        final FindSubst f = new FindSubst(varType, random);
        final boolean hasSubs = f.next(t[0], t[1], Global.UNIFICATION_POWER);
        if (!hasSubs) return false;

        //TODO combine these two blocks to use the same sub-method

        final Term a = t[0];
        Term aa = a;

        if (a instanceof Compound) {
            aa = applySubstituteAndRenameVariables(((Compound) a), f.map1);
            if (aa == null) return false;

            final Op aaop = aa.op();
            if (a.op() == Op.VAR_QUERY && (aaop == Op.VAR_INDEPENDENT || aaop == Op.VAR_DEPENDENT))
                return false;

        }

        final Term b = t[1];
        Term bb = b;

        if (b instanceof Compound) {
            bb = applySubstituteAndRenameVariables(((Compound) b), f.map2);
            if (bb == null) return false;

            final Op bbop = bb.op();
            if (b.op() == Op.VAR_QUERY && (bbop == Op.VAR_INDEPENDENT || bbop == Op.VAR_DEPENDENT))
                return false;
        }

        t[0] = aa;
        t[1] = bb;

        return true;
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

        return t.applySubstitute(subs);
    }

}
