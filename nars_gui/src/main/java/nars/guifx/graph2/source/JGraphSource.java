package nars.guifx.graph2.source;

import nars.NAR;
import nars.guifx.graph2.ConceptsSource;
import nars.term.Termed;
import org.jgrapht.DirectedGraph;

import java.util.function.Consumer;

/**
 * Source from jgrapht graphs
 */
public abstract class JGraphSource extends ConceptsSource {


    DirectedGraph<Termed, Termed> graph;


    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public JGraphSource(NAR nar, DirectedGraph<Termed, Termed> initialGraph) {
        super(nar);
        graph = initialGraph;
    }


    @Override
    public void forEachOutgoingEdgeOf(Termed src, Consumer eachTarget) {
        graph.outgoingEdgesOf(src).forEach(edge ->
                eachTarget.accept(getTargetVertex(edge)));
    }


    public void setGraph(DirectedGraph<Termed, Termed> initialGraph) {
        graph = initialGraph;
        updateGraph();
    }



    @Override
    public void commit() {
        System.out.println("commit: " + graph);
        if (graph == null) return;

        //            if (graph == null) {
//                //setvertices empty array?
//                return;
//            }

        grapher.setVertices(graph.vertexSet());
    }

}
