package nars.term.transform;

import nars.Op;
import nars.nal.meta.TermPattern;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.truth.Truth;
import nars.util.version.VersionMap;
import nars.util.version.Versioned;
import nars.util.version.Versioning;

import java.util.LinkedHashMap;
import java.util.Random;


public abstract class Subst extends Versioning {


    public final Random random;

    protected final Op type;


    abstract boolean match(final Term X, final Term Y);
    abstract boolean matchCompound(final Compound X, final Compound Y);
    abstract boolean matchPermute(Compound X, Compound Y);

    /** matches when x is of target variable type */
    abstract boolean matchXvar(Variable x, Term y);

    /** standard matching */
    public abstract boolean next(Term x, Term y, int power);

    /** compiled matching */
    public abstract boolean next(TermPattern x, Term y, int power);

    public abstract void putXY(Term x, Term y);
    public abstract void putYX(Term x, Term y);


    //public abstract Term resolve(Term t, Substitution s);

    public final VersionMap<Term,Term> xy;
    public final VersionMap<Term,Term> yx;

    /** current "y"-term being matched against */
    public final Versioned<Term> term;

    /** parent, if in subterms */
    public final Versioned<Compound> parent;

    public final Versioned<Integer> power;

    public final VersionMap<Term,Term> secondary;
    public final Versioned<Integer> occurrenceShift;
    public final Versioned<Truth> truth;
    public final Versioned<Character> punct;
    public final Versioned<Term> derived;


    public Subst(Random random, Op type) {
        this.random = random;
        this.type = type;

        xy = new VersionMap(this, new LinkedHashMap());
        yx = new VersionMap(this, new LinkedHashMap());
        term = new Versioned(this);
        parent = new Versioned(this);
        power = new Versioned(this);

        secondary = new VersionMap(this, new LinkedHashMap<>());
        occurrenceShift = new Versioned(this);
        truth = new Versioned(this);
        punct = new Versioned(this);
        derived = new Versioned(this);
    }

    public void clear() {
//        term = null;
//        parent = null;
//        power = 0;
        revert(0);
    }

    public Term getXY(Term t) {
        return xy.get(t);
    }

    public Term getYX(Term t) {
        return yx.get(t);
    }

    @Override
    public String toString() {
        return "subst:{" +
                "now:" + now() +
                ", type:" + type +
                ", term:" + term +
                ", parent:" + parent +
                //"random:" + random +
                ", power:" + power +
                ", xy:" + xy +
                (derived.get()!=null ? (", derived:" + derived) : "")+
                (truth.get()!=null ? (", truth:" + truth) : "")+
                (!secondary.isEmpty() ? (", secondary:" + secondary) : "")+
                (occurrenceShift.get()!=null ? (", occShift:" + occurrenceShift) : "")+
                ", yx:" + yx +
                '}';
    }



}
