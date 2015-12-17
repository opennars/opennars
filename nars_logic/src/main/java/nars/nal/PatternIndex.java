package nars.nal;

import nars.MapIndex;
import nars.Op;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.TermMetadata;
import nars.term.TermVector;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.match.Ellipsis;
import nars.term.transform.FindSubst;

import java.util.HashMap;

/**
 * Created by me on 12/7/15.
 */
public class PatternIndex extends MapIndex {

    public PatternIndex() {
        super(new HashMap(1024));
    }



    @Override
    protected <T extends Term> Compound<T> compileCompound(Compound<T> x, TermContainer subs) {

        /*if (!(x instanceof AbstractCompoundPattern)) {
            if (x instanceof Compound) {
                new VariableDependencies((Compound) x, Op.VAR_PATTERN);
            }

            //variable analysis
        }*/


        ///** only compile top-level terms, not their subterms */
        //if (!(x instanceof AbstractCompoundPattern)) {


        if (!(x instanceof TermMetadata)) {
//            if (!Ellipsis.hasEllipsis(x)) {
            if (!x.isCommutative()) {
                return new AbstractCompoundPattern(x, (TermVector) subs);
//                    return new LinearCompoundPattern(x, (TermVector) subs);
//                } else {
//                    return new CommutiveCompoundPattern(x, (TermVector) subs);
            }
//            }
        }
        //}

        return super.compileCompound(x, subs);
    }


//    public static class VariableDependencies extends DirectedAcyclicGraph<Term,String> {
//
//
//        public Op type;
//
//        /* primary ==> secondary */
//        protected void dependency(Term primary, Term secondary) {
//            addVertex(primary);
//            addVertex(secondary);
//            try {
//                addDagEdge(primary, secondary, "d" + edgeSet().size()+1);
//            } catch (CycleFoundException e) {
//                //System.err.println(e);
//            }
//        }
//
//
//        public static class PatternVariableIndex extends VarPattern {
//
//            public final Compound parent;
//            public final int index;
//
//            public PatternVariableIndex(String id, Compound parent, int index) {
//                super(id);
//                this.parent = parent;
//                this.index = index; //first index
//            }
//            public PatternVariableIndex(Variable v, Compound parent) {
//                this(v.id, parent, parent.indexOf(v));
//            }
//
//            public String toString() {
//                return super.toString() + " @ " + parent + " index " + index;
//            }
//        }
//
//        public static class RematchedPatternVariableIndex extends PatternVariableIndex {
//
//            public RematchedPatternVariableIndex(PatternVariableIndex i) {
//                super(i.id + "_", i.parent, i.index);
//            }
//        }
//
//
//        final Map<Variable,PatternVariableIndex> variables = Global.newHashMap();
//
//        public VariableDependencies(Compound c, Op varType) {
//            super(null);
//
//            this.type = varType;
//
//            c.recurseTerms( (s, p) -> {
//                boolean existed = !addVertex(s);
//
//                if (p == null)
//                    return; //top level
//
//                addVertex(p);
//
//                //if (t instanceof Compound) {
//
//                //compoundIn.put((Compound)p, (Compound)t);
//
//                if (s.op(varType)) {
//                    if (existed) {
//                        PatternVariableIndex s0 = variables.get(s);
//                        s = new RematchedPatternVariableIndex(s0); //shadow variable dependent
//                        //dependency(s0, s); //variable re-use after first match
//
//                        //compound depends on existing variable
//                        dependency(s0, p);
//                        dependency(p, s);
//                    } else {
//                        //variable depends on existing compound
//                        PatternVariableIndex ss = new PatternVariableIndex((Variable) s, (Compound) p);
//                        variables.put((Variable) s, ss);
//                        dependency(p, ss);
//                    }
//                }
//                else {
//                    if (s.isCommutative()) {
//                        //term is commutive
//                        //delay commutive terms to the 2nd stage
//                        dependency(Op.Imdex, s);
//                    } else {
//
//                        //term depends on existing compound
//                        dependency(p, s);
//                    }
//                }
////                }
////                else {
////                    if (!t.op(varType)) return;
////
////                    varIn.put((Variable) t, (Compound) p);
////                    compHas.put((Compound)p, (Variable)t);
////
////                    try {
////                        addDagEdge(p, t,  "requries(" + t + "," + p + ")");
////                    } catch (Exception e1) {
////                        System.err.println(e1);
////                    }
////                }
//            });
//
//            Term last = null;
//            //DepthFirstIterator ii = new DepthFirstIterator(this, c);
//            Iterator ii = iterator(); //topological
//            while (ii.hasNext()) last = (Term) ii.next();
//
//            //second stage as a shadow node
//            dependency(last, Op.Imdex);
//
//
//
//        }
//    }
//
    static final class AbstractCompoundPattern extends GenericCompound {


        public final int sizeCached;
        public final int volCached;
        public final int structureCachedWithoutVars;
        public final Term[] termsCached;
        protected final boolean ellipsis;
        private final boolean commutative;

        public AbstractCompoundPattern(Compound seed, TermVector subterms) {
            super(seed.op(), subterms, seed.relation());

            sizeCached = seed.size();
            structureCachedWithoutVars =
                    seed.structure() & ~(Op.VariableBits);
                    //seed.structure() & ~(Op.VAR_PATTERN.bit());

            this.ellipsis = Ellipsis.hasEllipsis(this);
            this.volCached = seed.volume();
            this.termsCached = subterms.terms();
            this.commutative = isCommutative();
        }

        @Override
        public Term[] terms() {
            return termsCached;
        }

    @Override
        public boolean match(Compound y, FindSubst subst) {
            if (!prematch(y)) return false;


            if (!ellipsis) {
                if (commutative) {
                    return subst.matchPermute(this, y);
                } else {
                    return matchLinear(y, subst);
                }
            } else {
                return subst.matchCompoundWithEllipsis(this, y);
            }

        }

        final public boolean prematch(Compound y) {
            int yStructure = y.structure();
            if ((yStructure | structureCachedWithoutVars) != yStructure)
                return false;

            if (!ellipsis) {
                if (sizeCached != y.size())
                    return false;
            }

            if (volCached > y.volume())
                return false;

            return relation == y.relation();
        }

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
