package nars.util.graph.experimental;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import nars.AbstractMemory;
import nars.NAR;
import nars.Op;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.term.Term;
import org.jgrapht.EdgeFactory;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.util.ArrayUnenforcedSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides a parameterizable Graph interface view across a Memory's Concept's TermLinks
 * TODO not complete yet
 */
abstract class TermGraph /* extends AbstractReaction */ implements UndirectedGraph<Concept,TermLink> {


    public static class ParameterizedTermGraph extends TermGraph {

        private final Predicate<Term> includeTerm;
        private final boolean sub;
        private final boolean supr;

        public ParameterizedTermGraph(NAR nar, Op theOperator, boolean sub, boolean supr) {
            this(nar, new Predicate<Term>() {
                @Override public boolean apply(Term term) {
                    return term.op() == theOperator;
                }
            }, sub, supr);
        }

        public ParameterizedTermGraph(NAR nar, Predicate<Term> includeTerm, boolean sub, boolean supr) {
            super(nar);
            this.includeTerm = includeTerm;
            this.sub = sub;
            this.supr = supr;
        }

        @Override
        public boolean include(Concept c, TermLink l, boolean towardsSubterm) {

            if (l.toSelfOrTransform()) return false;

            boolean isFor = l.toSubTerm();
            boolean isReverse = l.toSuperTerm();
            if ((!supr && isReverse)) return false;
            if ((!sub && isFor)) return false;

            if (isReverse && towardsSubterm) return false;
            if (isFor && !towardsSubterm) return false;

            return (includeTerm.apply(l.getTerm()));
        }
    }

    public final AbstractMemory memory;

    public TermGraph(NAR nar) {
        this(nar.memory);
    }

    public TermGraph(AbstractMemory memory) {
        super();
        this.memory = memory;
    }

    @Override
    public int degreeOf(Concept concept) {
        return 0;
    }


    abstract public boolean include(Concept c, TermLink l, boolean towardsSubterm);

//    @Override
//    public int inDegreeOf(Concept concept) {
//        return getEdgeCount(concept, false);
//    }
//
//    @Override
//    public Set<TermLink> incomingEdgesOf(Concept concept) {
//        return getEdgeSet(concept, false);
//    }
//
//    @Override
//    public int outDegreeOf(Concept concept) {
//        return getEdgeCount(concept, true);
//    }
//
    protected Iterator<TermLink> iterateEdges(Concept concept, boolean asOutgoing) {
        return Iterators.filter(concept.getTermLinks().iterator(), new Predicate<TermLink>() {
            @Override public boolean apply(TermLink t) {
                return include(concept, t, asOutgoing);
            }
        });
    }
//
//    protected int getEdgeCount(Concept concept, boolean asOutgoing) {
//        final int[] count = {0};
//        concept.termLinks.iterator().forEachRemaining(t -> {
//            if (include(concept, t, asOutgoing)) {
//                count[0]++;
//            }
//        });
//        return count[0];
//    }
//@Override
//public Set<TermLink> outgoingEdgesOf(Concept concept) {
//    return getEdgeSet(concept, true);
//}

    protected Set<TermLink> getEdgeSet(Concept concept, boolean towardsSubterm) {
        final Set<TermLink>[] e = new Set[1]; // new ArrayUnenforcedSet<>();

        concept.getTermLinks().iterator().forEachRemaining(t -> {
            if (include(concept, t, towardsSubterm)) {
                if (e[0] == null) e[0] = new ArrayUnenforcedSet(); //lazily allocate as necessary
                e[0].add(t);
            }
        });

        if (e[0] == null) return Collections.EMPTY_SET;

        return e[0];
    }


    @Override
    public Set<TermLink> getAllEdges(Concept from, Concept to) {
        TermLink e = getEdge(from, to);
        if (e == null) return Collections.EMPTY_SET;

        Set<TermLink> a = new ArrayUnenforcedSet(1);
        a.add(e);
        return a;
    }

    @Override
    public TermLink getEdge(Concept a, Concept b) {
        Set<TermLink> incomingToB = edgesOf(b);
        Iterator<TermLink> outgoingFromA = iterateEdges(a, true);
        while (outgoingFromA.hasNext()) {
            TermLink x = outgoingFromA.next();
            if (incomingToB.contains(x))
                return x;
        }
        return null;
    }

    @Override
    public EdgeFactory<Concept, TermLink> getEdgeFactory() {
        return null;
    }

    @Override
    public TermLink addEdge(Concept concept, Concept v1) {
        return null;
    }

    @Override
    public boolean addEdge(Concept concept, Concept v1, TermLink termLink) {
        return false;
    }

    @Override
    public boolean addVertex(Concept concept) {
        return false;
    }

    @Override
    public boolean containsEdge(Concept concept, Concept v1) {
        return false;
    }

    @Override
    public boolean containsEdge(TermLink termLink) {
        return false;
    }

    @Override
    public boolean containsVertex(Concept concept) {
        return false;
    }

    @Override
    public Set<TermLink> edgeSet() {
        return null;
    }

    @Override
    public Set<TermLink> edgesOf(Concept concept) {
        return null;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends TermLink> collection) {
        return false;
    }

    @Override
    public Set<TermLink> removeAllEdges(Concept concept, Concept v1) {
        return null;
    }

    @Override
    public boolean removeAllVertices(Collection<? extends Concept> collection) {
        return false;
    }

    @Override
    public TermLink removeEdge(Concept concept, Concept v1) {
        return null;
    }

    @Override
    public boolean removeEdge(TermLink termLink) {
        return false;
    }

    @Override
    public boolean removeVertex(Concept concept) {
        return false;
    }

    @Override
    public Set<Concept> vertexSet() {
        return null;
    }

    @Override
    public Concept getEdgeSource(TermLink termLink) {
        return null;
    }

    @Override
    public Concept getEdgeTarget(TermLink termLink) {
        return null;
    }

    @Override
    public double getEdgeWeight(TermLink termLink) {
        return 0;
    }
}
