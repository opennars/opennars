package nars.term.transform;

import nars.Op;
import nars.nal.meta.TermPattern;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.Truth;
import nars.util.version.VersionMap;
import nars.util.version.Versioned;

import java.util.LinkedHashMap;
import java.util.Random;


public abstract class Subst extends Frame {



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

    public final Versioned<Substitution> secondary;
    public final Versioned<Integer> occurrenceShift;
    public final Versioned<Truth> truth;
    public final Versioned<Character> punct;
    public final Versioned<Term> derived;


    public final Term term() { return term.get(); }
    public final Compound parent() { return parent.get(); }

    public Subst(Random random, Op type) {
        super(random, type);
        xy = new VersionMap(this, new LinkedHashMap());
        yx = new VersionMap(this, new LinkedHashMap());
        term = new Versioned(this);
        parent = new Versioned(this);
        power = new Versioned(this);
        secondary = new Versioned(this);
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
                (secondary.get()!=null ? (", secondary:" + secondary) : "")+
                (occurrenceShift.get()!=null ? (", occShift:" + occurrenceShift) : "")+
                ", yx:" + yx +
                '}';
    }



}
