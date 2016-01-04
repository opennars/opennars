package nars.nal;

import nars.index.MapIndex;
import nars.term.TermVector;
import nars.term.Termed;
import nars.term.compound.Compound;

import java.util.HashMap;

/**
 * Created by me on 12/7/15.
 */
public class PatternIndex extends MapIndex {

    public PatternIndex() {
        super(new HashMap(1024),new HashMap(1024));
    }


    @Override
    public Termed makeCompound(Compound x) {

        /*if (!(x instanceof AbstractCompoundPattern)) {
            if (x instanceof Compound) {
                new VariableDependencies((Compound) x, Op.VAR_PATTERN);
            }

            //variable analysis
        }*/


        ///** only compile top-level terms, not their subterms */
        //if (!(x instanceof AbstractCompoundPattern)) {

        if (x instanceof PremiseRule) {
            return new PremiseRule((Compound)x.term(0), (Compound)x.term(1));
        }

//        if (!(x instanceof TermMetadata)) {
////            if (!Ellipsis.hasEllipsis(x)) {
////            if (!x.isCommutative()) {

        return new PatternCompound(x,
            (TermVector) internSub(x.subterms())
        );

//                    return new LinearCompoundPattern(x, (TermVector) subs);
//                } else {
//                    return new CommutiveCompoundPattern(x, (TermVector) subs);
//            }
//            }
//        }
//        //}
//
//        return super.compileCompound(x);
    }




    //
//    /** non-commutive simple compound which can match subterms in any order, but this order is prearranged optimally */
//    static final class LinearCompoundPattern extends AbstractCompoundPattern {
//
//        private final int[] dependencyOrder;
//        //private final int[] heuristicOrder;
//        //private final int[] shuffleOrder;
//
//        public LinearCompoundPattern(Compound seed, TermVector subterms) {
//            super(seed, subterms);
//            dependencyOrder = getDependencyOrder(seed);
//            //heuristicOrder = getSubtermOrder(terms());
//            //shuffleOrder = heuristicOrder.clone();
//        }
//
//        private int[] getDependencyOrder(Compound seed) {
//            int ss = seed.size();
//
//
//            IntArrayList li = new IntArrayList();
//            List<Term> l = Global.newArrayList();
//            VariableDependencies d = new VariableDependencies(seed, Op.VAR_PATTERN);
//            Iterator ii = d.iterator();
//            while (ii.hasNext() && l.size() < ss) {
//                Term y = (Term) ii.next();
//                int yi = seed.indexOf(y);
//                if (yi!=-1) {
//                    l.add(y);
//                    li.add(yi);
//                }
//            }
//            if (li.size()!=ss) {
//                throw new RuntimeException("dependency fault");
//            }
//
//            //System.out.println(seed + " :: " + l + " " + li);
//            return li.toArray();
//        }
//
//        /** subterm match priority heuristic of
//         *  the amount of predicted effort or specificty
//         *  of matching a subterm
//         *  (lower is earlier) */
//        public static int subtermPriority(Term x) {
////            boolean isEllipsis = x instanceof Ellipsis;
////            boolean hasEllipsis =
////                    (x instanceof Compound) ?
////                        Ellipsis.containsEllipsis((Compound)x) : false;
//
//            int s = x.volume() + 1;
//
////            if (isEllipsis)
////                s += 200;
////            if (hasEllipsis)
////                s += 150;
//
//            if (x.isCommutative())
//                s *= s;
//
//            return s;
//        }
//
//        private static int[] getSubtermOrder(Term[] terms) {
//            Integer[] x = new Integer[terms.length];
//            for (int i = 0; i < terms.length; i++)
//                x[i] = i;
//            Arrays.sort(x,  (Integer a, Integer b) -> Integer.compare(
//                subtermPriority(terms[a]),
//                subtermPriority(terms[b])
//            ));
//            int[] y = new int[terms.length];
//            for (int i = 0; i < terms.length; i++) {
//                y[i] = x[i];
//            }
//            return y;
//        }
//
//        @Override
//        public boolean match(Compound y, FindSubst subst) {
//            if (!prematch(y)) return false;
//
//
//            if (!ellipsis) {
//                return matchLinear(y, subst);
//            } else {
//                return subst.matchCompoundWithEllipsis(this, y);
//            }
//
//        }
//
//
//        @Override
//        public boolean matchLinear(TermContainer y, FindSubst subst) {
//
//            //int[] o = this.heuristicOrder;
//            /*int[] o =
//                    //shuffle(shuffleOrder, subst.random);
//                    shuffle(shuffleOrder, subst.random);*/
//
//            //int[] o = dependencyOrder;
//
//            Term[] x = termsCached;
//            for (int i = 0; i < x.length; i++) {
//                //i = o[i]; //remap to the specific sequence
//                if (!subst.match(x[i], y.term(i)))
//                    return false;
//            }
//            return true;
//        }
//
//        static int[] shuffle(int[] shuffleOrder, Random random) {
//            nars.util.data.array.Arrays.shuffle(
//                shuffleOrder,
//                random
//            );
//            return shuffleOrder;
//        }
//
//    }
//
//    /** commutive simple compound which can match subterms in any order, but this order is prearranged optimally */
//    static final class CommutiveCompoundPattern extends AbstractCompoundPattern {
//
//        public CommutiveCompoundPattern(Compound seed, TermVector subterms) {
//            super(seed, subterms );
//        }
//
//        @Override
//        public boolean match(Compound y, FindSubst subst) {
//            if (!prematch(y))
//                return false;
//
//            if (!ellipsis) {
//                return subst.matchPermute(this, y);
//            } else {
//                return subst.matchCompoundWithEllipsis(this, y);
//            }
//
//        }
//
//    }
//

}
