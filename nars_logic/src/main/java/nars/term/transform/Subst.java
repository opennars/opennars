package nars.term.transform;

import nars.term.Term;

import java.util.Map;


public interface Subst {
    void clear();

    boolean next(Term x, Term y, int power);

    void putXY(Term x, Term y);

    Term resolve(Term t, Substitution s);

    Map<Term,Term> xy();
    Map<Term,Term> yx();

}
