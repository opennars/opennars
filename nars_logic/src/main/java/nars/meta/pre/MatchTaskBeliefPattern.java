package nars.meta.pre;

import nars.Op;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.meta.TaskRule;
import nars.premise.Premise;
import nars.term.Term;
import nars.term.Variable;

/**
 * Created by me on 8/15/15.
 */
public class MatchTaskBeliefPattern extends PreCondition {

    public final PairMatchingProduct pattern;
    //private final Term taskPattern, beliefPattern;
    private final boolean allowNullBelief;
    private final String id;

    public MatchTaskBeliefPattern(Term taskPattern, Term beliefPattern, TaskRule rule) {

        this.pattern = new PairMatchingProduct(taskPattern, beliefPattern);

        if (beliefPattern.structure() == 0) {

            // if nothing else in the rule involves this term
            // which will be a singular VAR_PATTERN variable
            // then allow null
            if (beliefPattern.op()!= Op.VAR_PATTERN)
                throw new RuntimeException("not what was expected");

            allowNullBelief = (rule.countOccurrences(pattern) == 1);

        }
        else {
            allowNullBelief = false;
        }

        /*System.out.println( Long.toBinaryString(
                        pStructure) + " " + pattern
        );*/

        this.id = getClass().getSimpleName() + "[" + pattern.toStringCompact() + "]";
    }

    @Override
    public final boolean test(final RuleMatch m) {

//        if (!allowNullBelief && m.premise.getBelief() == null)
//            return false;

        PairMatchingProduct p = m.taskBelief;
        if (!m.taskBelief.substitutesMayExist(pattern)) {
            return false;
        }

        return subst(m, p);
    }

    final protected boolean subst(final RuleMatch m, final PairMatchingProduct t) {
        return m.get(pattern, t);
    }

    @Override
    public String toString() {
        return id;
    }



    @Override
    public boolean isEarly() {
        return true;
    }
}
