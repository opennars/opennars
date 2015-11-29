package nars.term.transform;

import nars.Op;
import nars.nal.meta.TermPattern;
import nars.term.Term;

import java.util.Map;
import java.util.Random;


public abstract class Subst extends Frame {

    public Subst(Random random, Op type, Map<Term, Term> xy, Map<Term, Term> yx) {
        super(random, type, xy, yx);
    }

    /** standard matching */
    public abstract boolean next(Term x, Term y, int power);

    /** compiled matching */
    public abstract boolean next(TermPattern x, Term y, int power);

    public abstract void putXY(Term x, Term y);

    final public Term resolve(Term t) {
        return resolve(t, new Substitution());
    }

    public abstract Term resolve(Term t, Substitution s);

    //TODO hide these maps and allow access to their data through specific methods, because these need not be implemented as Map's
    abstract @Deprecated Map<Term,Term> xy();
    abstract @Deprecated Map<Term,Term> yx();

}
