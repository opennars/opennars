package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.premise.Premise;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
abstract public class MatchTerm extends PreCondition {
    private final Term pattern;
    private final int pStructure;

    public MatchTerm(Term pattern) {

        this.pattern = pattern;

        int pHash = pattern.subtermStructure();
        this.pStructure = pHash;// & ~(1<< Op.VAR_PATTERN.ordinal()); VAR_PATTERN ordinal should not even be included in the substrcture 32 bits

        /*System.out.println( Long.toBinaryString(
                        pStructure) + " " + pattern
        );*/
    }

    @Override
    public final boolean test(final RuleMatch m) {

        final Premise premise = m.premise;

        Term t = getTerm(premise);
        if (t == null) return false;

        if (pStructure!=0 && t.impossibleSubStructure(pStructure)) {
            //if (t.impossibleSubStructure(pStructure)) {
            //System.err.println("impossible: " + t + " in " + pattern);
            return false;
        }


        if (t.volume() < pattern.volume()) return false;

        //TODO check structural hash impossibility, with VAR_PATTERN bit removed

        /*return Variables.findSubstitute(Symbols.VAR_PATTERN,
                pattern, t,
                m.assign, m.waste, premise.getRandom());*/
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
