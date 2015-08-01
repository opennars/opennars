package nars.term.transform;

import nars.term.Term;

/**
 * Created by me on 4/25/15.
 */
@FunctionalInterface
public interface TermVisitor {
    public void visit(Term t, Term superterm);
}
