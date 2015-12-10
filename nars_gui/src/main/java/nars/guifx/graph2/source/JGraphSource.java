package nars.guifx.graph2.source;

import nars.guifx.graph2.GraphSource;
import nars.guifx.graph2.TermNode;
import nars.term.Termed;
import org.jgrapht.DirectedGraph;

import java.util.function.Consumer;

/**
 * Source from jgrapht graphs
 */
public abstract class JGraphSource<V extends Termed, E> extends GraphSource<V, TermNode<V>, E> {


    DirectedGraph<V, E> graph;


    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public JGraphSource(DirectedGraph<V, E> initialGraph) {
        graph = initialGraph;
    }


    @Override
    public void forEachOutgoingEdgeOf(V src, Consumer<V> eachTarget) {
        graph.outgoingEdgesOf(src).forEach(edge ->
                eachTarget.accept(getTargetVertex(edge)));
    }


    public void setGraph(DirectedGraph<V, E> initialGraph) {
        graph = initialGraph;
        updateGraph();
    }


    @Override
    public void updateGraph() {

        if (!isReady())
            return;

        if (graph == null) return;

        if (canUpdate()) {

//            if (graph == null) {
//                //setvertices empty array?
//                return;
//            }

            grapher.setVertices(graph.vertexSet());
        }
    }

}
