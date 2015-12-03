package nars.term.transform;

import nars.Op;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;

import java.util.Random;

/**
 * Created by me on 11/27/15.
 */
abstract public class Frame {

    public final Random random;

    protected final Op type;

    /** current y-term being matched against */
    public Term term;

    public Compound parent; //parent, if in subterms

    public int power;

    public Frame(final Random random, final Op type) {
        this.random = random;
        this.type = type;
    }

    abstract boolean match(final Term X, final Term Y);
    abstract boolean matchCompound(final Compound X, final Compound Y);
    abstract boolean matchPermute(Compound X, Compound Y);

    /** matches when x is of target variable type */
    abstract boolean matchXvar(Variable x, Term y);


    public void clear() {
        term = null;
        parent = null;
        power = 0;
    }

    @Override
    public String toString() {
        return "frame:{" +
                //"random:" + random +
                "type:" + type +
                ", term:" + term +
                ", parent:" + parent +
                ", power:" + power +
                '}';
    }

}
