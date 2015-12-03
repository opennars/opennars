package nars.term.visit;

import nars.term.Term;

/** return value false causes the recursion to terminate */
@FunctionalInterface
public interface TermPredicate {

    boolean test(Term t);

}
