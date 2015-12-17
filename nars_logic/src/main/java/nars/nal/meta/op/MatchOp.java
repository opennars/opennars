package nars.nal.meta.op;

import nars.term.Term;
import nars.term.transform.FindSubst;

/**
 * Created by me on 12/1/15.
 */
public abstract class MatchOp extends PatternOp {

    /**
     * if match not successful, does not cause the execution to
     * terminate but instead sets the frame's match flag
     */
    public abstract boolean match(Term f);

    @Override
    public final boolean run(FindSubst ff) {
//            if (ff.power < 0) {
//                return false;
//            }
        return match(ff.term.get());
    }

}
