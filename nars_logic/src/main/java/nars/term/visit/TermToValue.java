package nars.term.visit;

import nars.term.Term;

/**
 * TODO make a lighter-weight version which supplies only the 't' argument
 */
@FunctionalInterface
public interface TermToValue<V>  {
    V apply(Term nextTerm, V prevValue);
}
