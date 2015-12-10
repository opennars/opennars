package nars.nal;

import nars.MapIndex;
import nars.Op;
import nars.nal.meta.match.Ellipsis;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.TermVector;
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
    protected <T extends Term> Compound<T> compileCompound(Compound<T> x, TermContainer subs) {

        if (!Ellipsis.hasEllipsis(x)) {

            if (!x.isCommutative()) {
                return new LinearCompoundPattern(x, (TermVector) subs);
            } else {
                return new CommutiveCompoundPattern(x, (TermVector) subs);
            }

        }
        return super.compileCompound(x, subs);
    }

    abstract static class AbstractCompoundPattern extends GenericCompound {


        public final int sizeCached;
        public final int volCached;
        public final int structureCachedWithoutVars;
        public final Term[] termsCached;

        public AbstractCompoundPattern(Compound seed, TermVector subterms) {
            super(seed.op(), subterms, seed.relation());

            sizeCached = seed.size();
            structureCachedWithoutVars =
                    seed.structure() & ~(Op.VariableBits);
            volCached = seed.volume();
            this.termsCached = subterms.terms();
        }

        final public boolean prematch(Compound y) {
            int yStructure = y.structure();
            if ((yStructure | structureCachedWithoutVars) != yStructure)
                return false;

            if (sizeCached != y.size())
                return false;
            if (volCached > y.volume())
                return false;

            if (relation != y.relation())
                return false;
            return true;
        }

    }

    /** non-commutive simple compound which can match subterms in any order, but this order is prearranged optimally */
    static final class LinearCompoundPattern extends AbstractCompoundPattern {

        private final int[] heuristicOrder;
        private final int[] shuffleOrder;

        public LinearCompoundPattern(Compound seed, TermVector subterms) {
            super(seed, subterms);
            heuristicOrder = getSubtermOrder(terms());
            shuffleOrder = heuristicOrder.clone();
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
            if (!prematch(y)) return false;
            return matchLinear(y, subst);
        }


        @Override
        public boolean matchLinear(TermContainer y, FindSubst subst) {

            //int[] o = this.heuristicOrder;
            int[] o =
                    //shuffle(shuffleOrder, subst.random);
                    shuffle(shuffleOrder, subst.random);

            Term[] x = termsCached;
            for (int i = 0; i < x.length; i++) {
                i = o[i]; //remap to the specific sequence
                if (!subst.match(x[i], y.term(i)))
                    return false;
            }
            return true;
        }

        static int[] shuffle(int[] shuffleOrder, Random random) {
            nars.util.data.array.Arrays.shuffle(
                shuffleOrder,
                random
            );
            return shuffleOrder;
        }

    }

    /** commutive simple compound which can match subterms in any order, but this order is prearranged optimally */
    static final class CommutiveCompoundPattern extends AbstractCompoundPattern {

        public CommutiveCompoundPattern(Compound seed, TermVector subterms) {
            super(seed, subterms );
        }

        @Override
        public boolean match(Compound y, FindSubst subst) {
            if (!prematch(y)) return false;
            return subst.matchPermute(this, y);
        }

    }


}
