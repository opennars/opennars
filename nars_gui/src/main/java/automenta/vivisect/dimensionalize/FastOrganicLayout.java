package automenta.vivisect.dimensionalize;

import automenta.vivisect.graph.AbstractGraphVis;
import automenta.vivisect.graph.EdgeVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.VertexVis;
import org.jgrapht.Graph;

import java.util.Set;

/**
 * Fast organic layout algorithm, adapted from JGraph
 */
public class FastOrganicLayout<V, E> extends AbstractFastOrganicLayout<V,E, VertexVis<V, E>> implements GraphDisplay<V,E> {


    private AbstractGraphVis<V, E> graphVis;

    /**
     * Constructs a new fast organic layout for the specified graph.
     */
    public FastOrganicLayout() {
        super();

    }

    public VertexVis getDisplay(Graph<V, E> graph, V vertex) {
        return graphVis.getVertexDisplay(vertex);
    }

    @Override
    public void setPosition(VertexVis<V, E> vd, float x, float y) {
        vd.tx = x;
        vd.ty = y;
    }

    @Override
    public void movePosition(VertexVis<V, E> vd, float dx, float dy) {
        vd.tx += dx;
        vd.ty += dy;
    }

    @Override
    public Set<E> getEdges(Graph<V,E> graph, VertexVis<V, E> vd) {
        return vd.getEdges();
    }

    @Override
    public V getVertex(VertexVis<V, E> vd) {
        return vd.getVertex();
    }

    @Override
    public double getX(VertexVis<V, E> vd) {
        return vd.getX();
    }
    @Override
    public double getY(VertexVis<V, E> vd) {
        return vd.getY();
    }

    @Override
    public float getRadius(VertexVis<V, E> vd) {
        return vd.getRadius();
    }

    public boolean postUpdate(AbstractGraphVis<V,E> g) {
        Graph<V, E> graph = g.getGraph();
        this.graphVis = g;
        update(graph);
        return true;
    }


    public boolean preUpdate(AbstractGraphVis<V, E> g) {
        return true;
    }

    public void vertex(AbstractGraphVis<V, E> g, VertexVis<V, E> v) {
    }

    public void edge(AbstractGraphVis<V, E> g, EdgeVis<V, E> e) {
    }
}
