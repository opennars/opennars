package nars.nal;

import nars.Op;
import nars.term.Term;
import nars.term.TermVector;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.match.Ellipsis;
import nars.term.transform.FindSubst;

abstract public class PatternCompound extends GenericCompound {


    public final int sizeCached;
    public final int volCached;
    public final int structureCachedWithoutVars;
    public final Term[] termsCached;
    protected final boolean ellipsis;
    private final boolean commutative;
    private final boolean ellipsisTransform;


    public PatternCompound(Compound seed) {
        this(seed, (TermVector) seed.subterms());
    }

    public PatternCompound(Compound seed, TermVector subterms) {
        super(seed.op(), subterms, seed.relation());

        sizeCached = seed.size();
        structureCachedWithoutVars =
                //seed.structure() & ~(Op.VariableBits);
                seed.structure() & ~(Op.VAR_PATTERN.bit());

        this.ellipsis = Ellipsis.hasEllipsis(this);
        this.ellipsisTransform = Ellipsis.hasEllipsisTransform(this);
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

            if (commutative && y.size() > 1) {
                return subst.matchPermute(this, y);
            }

            return matchLinear(y, subst);

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
