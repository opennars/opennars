package nars.util.graph;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import nars.core.Memory;
import nars.core.NAR;
import nars.logic.NALOperator;
import nars.logic.entity.Concept;
import nars.logic.entity.Term;
import nars.logic.entity.TermLink;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.util.ArrayUnenforcedSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides a parameterizable Graph interface view across a Memory's Concept's TermLinks
 */
abstract public class TermGraph /* extends AbstractReaction */ implements DirectedGraph<Concept,TermLink> {


    public static class ParameterizedTermGraph extends TermGraph {

        private final Predicate<Term> includeTerm;
        private final boolean forward;
        private final boolean reverse;

        public ParameterizedTermGraph(NAR nar, NALOperator theOperator, boolean forward, boolean reverse) {
            this(nar, new Predicate<Term>() {
                @Override public boolean apply(Term term) {
                    return term.operator() == theOperator;
                }
            }, forward, reverse);
        }

        public ParameterizedTermGraph(NAR nar, Predicate<Term> includeTerm, boolean forward, boolean reverse) {
            super(nar);
            this.includeTerm = includeTerm;
            this.forward = forward;
            this.reverse = reverse;
        }

        @Override
        public boolean include(Concept c, TermLink l, boolean forwardDirection) {

            if ((forwardDirection && forward && l.isForward()) || ((!forwardDirection) && reverse && l.isReverse()))
                return (includeTerm.apply(l.getTerm()));

            return false;
        }
    }

    public final Memory memory;

    public TermGraph(NAR nar) {
        this(nar.memory);
    }

    public TermGraph(Memory memory) {
        super();
        this.memory = memory;
    }

    abstract public boolean include(Concept c, TermLink l, boolean asOutgoing);

    @Override
    public int inDegreeOf(Concept concept) {
        return getEdgeCount(concept, false);
    }

    @Override
    public Set<TermLink> incomingEdgesOf(Concept concept) {
        return getEdgeSet(concept, false);
    }

    @Override
    public int outDegreeOf(Concept concept) {
        return getEdgeCount(concept, true);
    }

    protected Iterator<TermLink> iterateEdges(Concept concept, boolean asOutgoing) {
        return Iterators.filter(concept.termLinks.iterator(), new Predicate<TermLink>() {
            @Override public boolean apply(TermLink t) {
                return include(concept, t, asOutgoing);
            }
        });
    }

    protected int getEdgeCount(Concept concept, boolean asOutgoing) {
        final int[] count = {0};
        concept.termLinks.iterator().forEachRemaining(t -> {
            if (include(concept, t, asOutgoing)) {
                count[0]++;
            }
        });
        return count[0];
    }

    protected Set<TermLink> getEdgeSet(Concept concept, boolean asOutgoing) {
        final Set<TermLink>[] e = new Set[1]; // new ArrayUnenforcedSet<>();

        concept.termLinks.iterator().forEachRemaining(t -> {
            if (include(concept, t, asOutgoing)) {
                if (e[0] == null) e[0] = new ArrayUnenforcedSet(); //lazily allocate as necessary
                e[0].add(t);
            }
        });

        if (e[0] == null) return Collections.EMPTY_SET;

        return e[0];
    }

    @Override
    public Set<TermLink> outgoingEdgesOf(Concept concept) {
        return getEdgeSet(concept, true);
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
        Set<TermLink> incomingToB = incomingEdgesOf(b);
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
