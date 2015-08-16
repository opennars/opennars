package nars.meta.pre;

import nars.Symbols;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.premise.Premise;
import nars.term.Term;
import nars.term.Variables;

/**
 * Created by me on 8/15/15.
 */
abstract public class MatchFirstTermWithTerm extends PreCondition {
    private final Term pattern;

    public MatchFirstTermWithTerm(Term pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean test(RuleMatch m) {

        final Premise premise = m.premise;

        return Variables.findSubstitute(Symbols.VAR_PATTERN,
                pattern, getTerm(premise),
                m.assign, m.waste, premise.getRandom());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + pattern.toStringCompact() + "]";
    }

    protected abstract Term getTerm(Premise p);
}
