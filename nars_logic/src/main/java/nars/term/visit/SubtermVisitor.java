package nars.term.visit;

import nars.term.Term;

/**
 * TODO make a lighter-weight version which supplies only the 't' argument
 * TODO subclass BiConsumer<Term,Term> ?
 */
@FunctionalInterface
public interface SubtermVisitor {

    void visit(Term t, Term superterm);
}
