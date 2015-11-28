package nars.term.transform;

import nars.term.Term;

import java.util.Map;


public interface Subst {

    /** reset to a starting, empty state */
    void clear();

    /** standard matching */
    boolean next(Term x, Term y, int power);

    /** compiled matching */
    boolean next(FindSubst.TermPattern x, Term y, int power);

    void putXY(Term x, Term y);

    default Term resolve(Term t) {
        return resolve(t, new Substitution());
    }

    Term resolve(Term t, Substitution s);

    //TODO hide these maps and allow access to their data through specific methods, because these need not be implemented as Map's
    @Deprecated Map<Term,Term> xy();
    @Deprecated Map<Term,Term> yx();

    Subst clone();
}
