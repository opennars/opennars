package nars.logic.nal8;

import nars.core.Memory;
import nars.logic.entity.Term;

/**
 * Able to evaluate a tuple of terms into a term
 * EXPERIMENTAL
 */
public interface TermEval {


    /** Term[] is the argument tuple.  returns a value Term, or null if non-evaluable */
    public Term function(Term[] x);

}
