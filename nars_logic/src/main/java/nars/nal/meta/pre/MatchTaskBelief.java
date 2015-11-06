package nars.nal.meta.pre;

import nars.Global;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TaskBeliefPair;
import nars.term.Term;


public class MatchTaskBelief extends PreCondition {

    public final TaskBeliefPair pattern;

    final String id;

    public MatchTaskBelief(TaskBeliefPair pattern) {

        this.pattern = pattern;
        Term beliefPattern = pattern.term(1);

        //if (Global.DEBUG) {
            if (beliefPattern.structure() == 0) {

                // if nothing else in the rule involves this term
                // which will be a singular VAR_PATTERN variable
                // then allow null
//                if (beliefPattern.op() != Op.VAR_PATTERN)
//                    throw new RuntimeException("not what was expected");

            }
        //}

        /*System.out.println( Long.toBinaryString(
                        pStructure) + " " + pattern
        );*/

        this.id = getClass().getSimpleName() + '[' + pattern.toStringCompact() + ']';

    }

    //TODO this caching is not thread-safe yet
    @Override
    public final boolean test(final RuleMatch m) {

        final TaskBeliefPair tb = m.taskBelief;

        final TaskBeliefPair pattern = this.pattern;

        //if (!tb.substitutesMayExistParanoid(pattern)) {
        if (!tb.substitutesMayExistFast(pattern)) {
            return false;
        }

        return subst(pattern, m, tb);
    }

    final static boolean subst(TaskBeliefPair pattern, final RuleMatch m, final TaskBeliefPair t) {
        //TODO parameterize the power by budget
        return m.next(pattern, t, Global.UNIFICATION_POWER);
    }

    @Override
    final public String toString() {
        return id;
    }


}
