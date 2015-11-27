package nars.term.transform;

import nars.Op;
import nars.term.Compound;
import nars.term.Term;

import java.util.Map;

/**
 * Created by me on 11/27/15.
 */
abstract public class Frame {

    protected final Op type;

    /** current y-term being matched against */
    protected Term y;

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

    protected int power;

    public Frame(final Op type, Map<Term, Term> xy, Map<Term, Term> yx) {
        this.type = type;
        this.yx = yx;
        this.xy = xy;
    }

    abstract boolean match(final Term X, final Term Y);
    abstract boolean matchCompound(final Compound X, final Compound Y);


    public final void clear() {
        xy.clear();
        yx.clear();
        y = null;
        xyChanged = yxChanged = false;
    }

}
