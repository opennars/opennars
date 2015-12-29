package nars.util.graph;

import nars.NAR;
import nars.concept.Concept;
import nars.util.data.ConceptMap;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * Abstract graph model of inter-Concept dynamics
 */
public abstract class ConceptGraph<E> extends ConceptMap implements EdgeFactory<Concept,E> {

    public final Graph<Concept,E> graph;

    protected ConceptGraph(NAR nar) {
        this(nar, true);
    }

    protected ConceptGraph(NAR nar, boolean directed) {
        super(nar);
        graph = directed ? new DefaultDirectedGraph<>(this) : new AsUndirectedGraph(new DefaultDirectedGraph<>(this));

    }

    @Override
    public abstract boolean contains(Concept c);

    @Override
    protected boolean onConceptForget(Concept c) {
        return graph.removeVertex(c);
    }

    @Override
    protected boolean onConceptActive(Concept c) {
        return true;
    }



    /** must override this to use graph.addEdge(source,target) method */
    @Override
    public E createEdge(Concept source, Concept target) {
        return null;
    }

    public E addEdge(Concept source, Concept target) {
        graph.addVertex(source);
        graph.addVertex(target);

        E existing = graph.getEdge(source, target);
        if (existing!=null)
            return existing;

        E e = createEdge(source, target);
        graph.addEdge(source, target, e);
        return e;
    }

    public void removeEdge(E e) {
        graph.removeEdge(e);
    }

    @Override
    public String toString() {
        return graph.toString();
    }
}
