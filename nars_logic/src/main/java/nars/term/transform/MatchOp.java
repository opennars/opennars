package nars.term.transform;

import nars.term.Term;

/**
 * Created by me on 12/1/15.
 */
public abstract class MatchOp extends PatternOp {

    /**
     * if match not successful, does not cause the execution to
     * terminate but instead sets the frame's match flag
     */
    abstract public boolean match(Term f);

    @Override
    public final boolean run(Subst ff) {
//            if (ff.power < 0) {
//                return false;
//            }
        return match(ff.term.get());
    }

}
