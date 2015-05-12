package nars.rl.example;

import nars.NAR;
import nars.nal.concept.Concept;
import nars.util.index.ConceptMap;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * Abstract graph model of inter-Concept dynamics
 */
abstract public class ConceptGraph<E> extends ConceptMap implements EdgeFactory<Concept,E> {

    public final DirectedGraph<Concept,E> graph = new DefaultDirectedGraph<Concept, E>(this);

    public ConceptGraph(NAR nar) {
        super(nar);
    }

    @Override
    abstract public boolean contains(Concept c);

    @Override
    protected boolean onConceptForget(Concept c) {
        return graph.removeVertex(c);
    }

    @Override
    protected boolean onConceptActive(Concept c) {
        return true;
    }


    @Override
    abstract public E createEdge(Concept source, Concept target);

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

}
