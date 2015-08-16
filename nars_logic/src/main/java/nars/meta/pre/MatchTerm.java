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
abstract public class MatchTerm extends PreCondition {
    private final Term pattern;

    public MatchTerm(Term pattern) {
        this.pattern = pattern;
    }

    @Override
    public final boolean test(final RuleMatch m) {

        final Premise premise = m.premise;

        Term t = getTerm(premise);
        if (t == null) return false;

        return Variables.findSubstitute(Symbols.VAR_PATTERN,
                pattern, t,
                m.assign, m.waste, premise.getRandom());
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
