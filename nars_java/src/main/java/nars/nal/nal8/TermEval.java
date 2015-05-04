package nars.nal.nal8;

import nars.nal.term.Term;

/**
 * Able to evaluate a tuple of terms into a term
 * EXPERIMENTAL
 */
public interface TermEval {


    /** Term[] is the argument tuple.  returns a value Term, or null if non-evaluable */
    public Object function(Term[] x);

}
