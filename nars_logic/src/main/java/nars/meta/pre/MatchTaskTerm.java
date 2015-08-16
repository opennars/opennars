package nars.meta.pre;

import nars.premise.Premise;
import nars.term.Term;

/** for use with precondtiion term 0 */
final public class MatchTaskTerm extends MatchTerm {
    public MatchTaskTerm(Term pattern) {
        super(pattern);
    }

    @Override protected final Term getTerm(Premise p) {
        return p.getTask().getTerm();
    }
}
