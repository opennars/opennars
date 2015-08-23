package nars.meta.pre;

import nars.link.TermLink;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.Term;

/** constructed for preconditoin term 1 */
final public class MatchBeliefTerm extends MatchTerm {
    public MatchBeliefTerm(Term pattern) {
        super(pattern);
    }

    @Override protected final Term getTerm(final Premise p) {

        final TermLink tl = p.getTermLink();
        if (tl == null) return null;

        return tl.getTerm();
    }
}
