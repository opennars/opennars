package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.meta.TaskRule;
import nars.premise.Premise;
import nars.term.Term;
import nars.term.Variable;

/**
 * Created by me on 8/15/15.
 */
abstract public class MatchTerm extends PreCondition {

    private final Term pattern;
    private final int minVolume;
    private final int pStructure;
    private final boolean allowNull;

    public MatchTerm(Term pattern, TaskRule rule) {

        this.pattern = pattern;

        int pHash = pattern.structure();
        this.minVolume = pattern.volume();
        this.pStructure = pHash;// & ~(1<< Op.VAR_PATTERN.ordinal()); VAR_PATTERN ordinal should not even be included in the substrcture 32 bits

        if (pStructure == 0) {

            // if nothing else in the rule involves this term
            // which will be a singular VAR_PATTERN variable
            // then allow null
            if (((Variable)pattern).getType()!='%')
                throw new RuntimeException("not what was expected");

            allowNull = (rule.countOccurrences(pattern) == 1);

        }
        else {
            allowNull = false;
        }

        /*System.out.println( Long.toBinaryString(
                        pStructure) + " " + pattern
        );*/
    }

    @Override
    public final boolean test(final RuleMatch m) {

        final Term t = getTerm(m.premise);

        if (t == null) {
            return allowNull;
        }


        if ((t.volume() < minVolume) || (t.impossibleStructure(pStructure))) {
            //if (t.impossibleSubStructure(pStructure)) {
            //System.err.println("impossible: " + t + " in " + pattern);
            return false;
        }

        return subst(m, t);
    }

    final protected boolean subst(final RuleMatch m, final Term t) {
        return m.get(pattern, t);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + pattern.toStringCompact() + "]";
    }

    protected abstract Term getTerm(Premise p);

    @Override
    public boolean isEarly() {
        return true;
    }
}
