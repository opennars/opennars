package nars.term.transform;

import nars.Op;
import nars.nal.meta.TermPattern;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.util.version.VersionMap;
import nars.util.version.Versioned;
import nars.util.version.Versioning;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;


public abstract class Subst extends Versioning {


    public final Random random;

    protected final Op type;


    abstract boolean match(final Term X, final Term Y);

    /** matches when x is of target variable type */
    abstract boolean matchXvar(Variable x, Term y);

    /** standard matching */
    public abstract boolean next(Term x, Term y, int power);

    /** compiled matching */
    public abstract boolean next(TermPattern x, Term y, int power);

    public abstract void putXY(Term x, Term y);
    public abstract void putYX(Term x, Term y);


    //public abstract Term resolve(Term t, Substitution s);

    public final Map<Term,Term> xy;
    public final Map<Term,Term> yx;

    /** current "y"-term being matched against */
    public final Versioned<Term> term;

    /** parent, if in subterms */
    public final Versioned<Compound> parent;

    /** unification power available at start of current branch */
    public final Versioned<Integer> branchPower;

    /** unification power remaining in the current branch */
    int power;

    /** current power divisor which divides power to
     *  limits the # of permutations
     *  that can be tried, or the # of subterms that can be compared
     */
    int powerDivisor;




    public Subst(Random random, Op type) {
        this.random = random;
        this.type = type;

        xy = new VarCachedVersionMap(this, new LinkedHashMap());
        yx = new VarCachedVersionMap(this, new LinkedHashMap());
        term = new Versioned(this);
        parent = new Versioned(this);
        branchPower = new Versioned(this);

    }

    public void setPower(int startPower) {
        this.power = startPower;
        this.powerDivisor = 1;
    }


    public void clear() {
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
                ", yx:" + yx +
                '}';
    }



    public final void goSubterm(int index) {
        Term pp = parent.get().term(index);
        /*if (pp == null)
            throw new RuntimeException("null subterm");*/
        term.set( pp );
    }

    /** match a range of subterms of Y */
    public Term[] collect(Compound y, int from, int to) {
        final int s = to-from;
        Term[] m = new Term[s];
        for (int i = 0; i < s; i++) {
            int k = i+from;
            m[i] = y.term(k);
        }

        return m;
    }

    public static final class VarCachedVersionMap extends VersionMap<Term, Term> {
        public VarCachedVersionMap(Subst context, Map<Term,Versioned<Term>> map) {
            super(context, map);
        }

        @Override
        public final boolean cache(Term key) {
            //since these should always be normalized variables, they will not exceed a predictable range of entries (ex: $1, $2, .. $n)
            return key instanceof Variable;
        }
    }
}
