package nars.term.transform;

import nars.Op;
import nars.term.Compound;
import nars.term.Term;

import java.util.Map;
import java.util.Random;

/**
 * Created by me on 11/27/15.
 */
abstract public class Frame {

    public final Random random;

    protected final Op type;

    /** current y-term being matched against */
    public Term y;

    protected Compound parent; //parent, if in subterms

    /**
     * X var -> Y term mapping
     */
    protected final Map<Term, Term> xy;
    protected boolean xyChanged = false;

    /**
     * Y var -> X term mapping
     */
    protected final Map<Term, Term> yx;
    protected boolean yxChanged = false;

    public int power;

    public Frame(final Random random, final Op type, Map<Term, Term> xy, Map<Term, Term> yx) {
        this.random = random;
        this.type = type;
        this.yx = yx;
        this.xy = xy;
    }

    /** copy constructor */
    Frame(final Random random, Op type, Map<Term, Term> xy, Map<Term, Term> yx, Term y, Compound parent, boolean xyChanged, boolean yxChanged, int power) {
        this.random = random;
        this.type = type;
        this.xy = xy;
        this.yx = yx;
        this.y = y;
        this.parent = parent;
        this.xyChanged = xyChanged;
        this.yxChanged = yxChanged;
        this.power = power;
    }


    abstract boolean match(final Term X, final Term Y);
    abstract boolean matchCompound(final Compound X, final Compound Y);


    public final void clear() {
        xy.clear();
        yx.clear();
        y = null;
        parent = null;
        xyChanged = yxChanged = false;


    }

}
