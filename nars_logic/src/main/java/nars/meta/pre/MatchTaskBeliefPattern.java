package nars.meta.pre;

import nars.Global;
import nars.Op;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class MatchTaskBeliefPattern extends PreCondition {

    public final PairMatchingProduct pattern;
    private final String id;


    public MatchTaskBeliefPattern(PairMatchingProduct pattern) {

        this.pattern = pattern;
        Term beliefPattern = pattern.term(1);

        //if (Global.DEBUG) {
            if (beliefPattern.structure() == 0) {

                // if nothing else in the rule involves this term
                // which will be a singular VAR_PATTERN variable
                // then allow null
                if (beliefPattern.op() != Op.VAR_PATTERN)
                    throw new RuntimeException("not what was expected");

            }
        //}

        /*System.out.println( Long.toBinaryString(
                        pStructure) + " " + pattern
        );*/

        this.id = getClass().getSimpleName() + "[" + pattern.toStringCompact() + "]";
    }

    //TODO this caching is not thread-safe yet
    @Override
    public final boolean test(final RuleMatch m) {

        final PairMatchingProduct tb = m.taskBelief;

        if (!tb.substitutesMayExist(pattern)) {
            return false;
        }

        return subst(m, tb);
    }

    final protected boolean subst(final RuleMatch m, final PairMatchingProduct t) {
        //TODO parameterize the power by budget
        return m.next(pattern, t, Global.UNIFICATION_POWER);
    }

    @Override
    public String toString() {
        return id;
    }


}
