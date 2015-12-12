package nars.guifx.graph2.source;

import nars.guifx.graph2.GraphSource;
import nars.term.Termed;
import org.jgrapht.DirectedGraph;

import java.util.function.Consumer;

/**
 * Source from jgrapht graphs
 */
public abstract class JGraphSource extends GraphSource {


    DirectedGraph<Termed, Termed> graph;


    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public JGraphSource(DirectedGraph<Termed, Termed> initialGraph) {
        graph = initialGraph;
    }


    @Override
    public void forEachOutgoingEdgeOf(Termed src, Consumer<Termed> eachTarget) {
        graph.outgoingEdgesOf(src).forEach(edge ->
                eachTarget.accept(getTargetVertex(edge)));
    }


    public void setGraph(DirectedGraph<Termed, Termed> initialGraph) {
        graph = initialGraph;
        updateGraph();
    }


    @Override
    public void updateGraph() {

        System.out.println(isReady() + " " + canUpdate() + " " + graph);

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
