package nars.nal;

import nars.MapIndex;
import nars.Op;
import nars.nal.meta.match.Ellipsis;
import nars.nal.nal4.Image;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.transform.FindSubst;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

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
    static final class LinearCompoundPattern extends GenericCompound {


        private final Op op;
        private final int sizeCached;
        private final int volCached;
        private final int structureCachedWithoutVars;
        private final int[] heuristicOrder;
        private final int[] shuffleOrder;
        private final Term[] termsCached;

        public LinearCompoundPattern(Compound seed) {
            super(seed.op(), seed.terms(),
                    (int)((seed instanceof Image) ? (((Image)seed).relation()) : 0)
            );
            this.op = seed.op();
            this.structureCachedWithoutVars =
                seed.structure() & ~(Op.VARIABLE_BITS);

            this.termsCached = this.terms();
            this.sizeCached = seed.size();
            this.volCached = seed.volume();
            this.heuristicOrder = getSubtermOrder(terms());
            this.shuffleOrder = heuristicOrder.clone();
        }

        /** subterm match priority heuristic of
         *  the amount of predicted effort or specificty
         *  of matching a subterm
         *  (lower is earlier) */
        public static int subtermPriority(Term x) {
//            boolean isEllipsis = x instanceof Ellipsis;
//            boolean hasEllipsis =
//                    (x instanceof Compound) ?
//                        Ellipsis.containsEllipsis((Compound)x) : false;

            int s = x.volume() + 1;

//            if (isEllipsis)
//                s += 200;
//            if (hasEllipsis)
//                s += 150;

            if (x.isCommutative())
                s *= s;

            return s;
        }

        private static int[] getSubtermOrder(Term[] terms) {
            Integer[] x = new Integer[terms.length];
            for (int i = 0; i < terms.length; i++)
                x[i] = i;
            Arrays.sort(x,  (Integer a, Integer b) -> Integer.compare(
                subtermPriority(terms[a]),
                subtermPriority(terms[b])
            ));
            int[] y = new int[terms.length];
            for (int i = 0; i < terms.length; i++) {
                y[i] = x[i];
            }
            return y;
        }



        @Override
        public boolean match(Compound y, FindSubst subst) {
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

            //int[] o = this.heuristicOrder;
            int[] o = shuffle(this.shuffleOrder, subst.random);

            Term[] x = this.termsCached;
            for (int i = 0; i < o.length; i++) {
                if (!subst.match(x[i], y.term(i)))
                    return false;
            }
            return true;
        }

        static int[] shuffle(int[] shuffleOrder, Random random) {
            nars.util.data.array.Arrays.shuffle(
                    shuffleOrder, random
            );
            return shuffleOrder;
        }


//        @Override public int size() {
//            return sizeCached;
//        }
//
//        @Override
//        public int structure() {
//            return structureCached;
//        }


//
//        @Override
//        public Op op() {
//            return op;
//        }
//
//        @Override
//        public boolean isCommutative() {
//            return false;
//        }

//
//        @Override
//        public Term clone(Term[] replaced) {
//            return seed.clone(replaced);
//        }
    }
}
