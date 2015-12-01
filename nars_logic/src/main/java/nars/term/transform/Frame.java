package nars.term.transform;

import nars.Op;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;

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

    public Compound parent; //parent, if in subterms

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


    abstract boolean match(final Term X, final Term Y);
    abstract boolean matchCompound(final Compound X, final Compound Y);
    abstract boolean matchPermute(Compound X, Compound Y);

    /** matches when x is of target variable type */
    abstract boolean matchXvar(Variable x, Term y);


    public void clear() {
        xy.clear();
        yx.clear();
        y = null;
        parent = null;
        xyChanged = yxChanged = false;
    }

    public void copyTo(Frame m) {
        m.xy.clear(); m.xy.putAll(xy);
        m.yx.clear(); m.yx.putAll(yx);
        m.y = y;
        m.parent = parent;
        m.xyChanged = false; //xyChanged;
        m.yxChanged = false; //yxChanged;
        m.power = power;
    }


    @Override
    public String toString() {
        return "Frame{" +
                "random=" + random +
                ", type=" + type +
                ", y=" + y +
                ", parent=" + parent +
                ", xy=" + xy +
                ", xyChanged=" + xyChanged +
                ", yx=" + yx +
                ", yxChanged=" + yxChanged +
                ", power=" + power +
                '}';
    }

}
