package nars.nal;

import nars.MapIndex;
import nars.Op;
import nars.nal.meta.match.Ellipsis;
import nars.nal.nal4.Image;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;
import nars.term.compound.CompoundN;
import nars.term.transform.FindSubst;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by me on 12/7/15.
 */
public class PatternIndex extends MapIndex {

    public PatternIndex() {
        super(new HashMap(1024));
    }

    @Override
    protected <T extends Term> Compound<T> compileCompound(Compound<T> c) {
        Compound x = super.compileCompound(c);
        if (x instanceof Compound) {
            if (!x.isCommutative() && !Ellipsis.hasEllipsis(x)) {
                if (!(x instanceof Image)) {
                    x = new LinearCompoundPattern(x);
                }
            }
        }
        return x;
    }

    /** non-commutive simple compound which can match subterms in any order, but this order is prearranged optimally */
    final static class LinearCompoundPattern extends CompoundN {

        private final Compound seed;
        private final Op op;
        private final int structureCached;
        private final int sizeCached;
        private final int volCached;
        private final int structureCachedWithoutVars;
        private final int[] subtermOrder;
        private final Term[] termsCached;

        public LinearCompoundPattern(Compound seed) {
            super((CompoundN)seed);
            this.seed = seed;
            this.op = seed.op();
            this.structureCached = seed.structure();
            this.structureCachedWithoutVars =
                seed.structure() & ~(Op.VARIABLE_BITS);

            this.termsCached = this.terms();
            this.sizeCached = seed.size();
            this.volCached = seed.volume();
            this.subtermOrder = getSubtermOrder(terms());
        }

        private static int[] getSubtermOrder(Term[] terms) {
            Integer[] x = new Integer[terms.length];
            for (int i = 0; i < terms.length; i++)
                x[i] = i;
            Arrays.sort(x, (Integer a, Integer b) -> {
                int volA = terms[a].volume();
                int volB = terms[b].volume();
                return Integer.compare(volA, volB);
            });
            int[] y = new int[terms.length];
            for (int i = 0; i < terms.length; i++) {
                y[i] = x[i];
            }
            return y;
        }

        @Override
        public String toString() {
            return seed.toStringCompact();
        }

        @Override
        public final boolean match(Compound y, FindSubst subst) {
            if (y.size() != sizeCached)
                return false;
            if (y.volume() < volCached)
                return false;
            final int yStructure = y.structure();
            if ((yStructure | structureCachedWithoutVars) != yStructure)
                return false;

            return matchLinear(y, subst);
        }

        @Override
        public boolean matchLinear(TermContainer y, FindSubst subst) {
            int[] o = this.subtermOrder;
            Term[] x = this.termsCached;
            for (int i = 0; i < o.length; i++) {
                if (!subst.match(x[i], y.term(i)))
                    return false;
            }
            return true;
        }


        @Override public int size() {
            return sizeCached;
        }

        @Override
        public int structure() {
            return structureCached;
        }


        @Override
        public Op op() {
            return op;
        }

        @Override
        public boolean isCommutative() {
            return false;
        }

        @Override
        public Term clone() {
            return seed.clone();
        }

        @Override
        public Term clone(Term[] replaced) {
            return seed.clone(replaced);
        }
    }
}
