package nars.meta.pre;

import nars.premise.Premise;
import nars.term.Term;

/** constructed for preconditoin term 1 */
final public class MatchBeliefTerm extends MatchFirstTermWithTerm {
    public MatchBeliefTerm(Term pattern) {
        super(pattern);
    }

    @Override protected final Term getTerm(Premise p) {
        return p.getBelief().getTerm();
    }
}
