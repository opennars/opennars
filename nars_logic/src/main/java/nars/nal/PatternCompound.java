package nars.nal;

import nars.Op;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.TermVector;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.match.EllipsisTransform;
import nars.term.transform.FindSubst;

public final class PatternCompound extends GenericCompound {

    public final int sizeCached;
    public final int volCached;
    public final int structureCached;
    public final Term[] termsCached;
    protected final boolean ellipsis;

    private final boolean commutative;
    private final boolean effectivelyCommutative;

    private final boolean ellipsisTransform;


    public PatternCompound(Compound seed) {
        this(seed, (TermVector) seed.subterms());
    }

    public PatternCompound(Compound seed, TermVector subterms) {
        super(seed.op(), seed.relation(), subterms);

        sizeCached = seed.size();
        structureCached =
                //seed.structure() & ~(Op.VariableBits);
                seed.structure() & ~(Op.VAR_PATTERN.bit());

        this.ellipsis = seed.hasEllipsis();
        this.ellipsisTransform = hasEllipsisTransform(this);
        this.volCached = seed.volume();
        this.termsCached = subterms.terms();
        this.commutative = isCommutative();
        this.effectivelyCommutative = isCommutative() && (size() > 1);
    }

    public static boolean hasEllipsisTransform(TermContainer x) {
        int xs = x.size();
        for (int i = 0; i < xs; i++)
            if (x.term(i) instanceof EllipsisTransform) return true;
        return false;
    }

    @Override
    public boolean hasEllipsis() {
        return ellipsis;
    }

    @Override
    public Term[] terms() {
        return termsCached;
    }

    @Override
    public final int structure() {
        return structureCached;
    }

    @Override public boolean match(Compound y, FindSubst subst) {
        return canMatch(y) && (!ellipsis ?
                ((effectivelyCommutative) ?
                        subst.matchPermute(this, y) :
                        subst.matchLinear(this, y)) :
                subst.matchCompoundWithEllipsis(this, y));
    }

    public final boolean canMatch(Compound y) {

        int yStructure = y.structure();
        if ((yStructure | structureCached) != yStructure)
            return false;

        if (!ellipsis) {
            if (sizeCached != y.size())
                return false;
        }

        if (volCached > y.volume())
            return false;


        if (!ellipsisTransform) {
            if (relation != y.relation())
                return false;
        }

        return true;
    }

}
/**
 * Created by me on 12/26/15.
 */ //    public static class VariableDependencies extends DirectedAcyclicGraph<Term,String> {
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
