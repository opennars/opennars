package nars.util.db;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import java.util.Map;

/**
 * Blueprints Graph interface with adjacency implemented by Infinispan collections
 */
public class SpanGraph<X> extends MapGraph<X> {

    public final InfiniPeer peer;

    public SpanGraph(String id, InfiniPeer peer) {
        super(id);
        this.peer = peer;
        init();
    }

    protected <Y> Map<X, Y> newElementMap(String suffix) {
        return peer.the(id + '_' + suffix);
    }

    @Override
    protected Map<X, Edge> newEdgeMap() {
        return newElementMap("e");
    }

    @Override
    protected Map<X, Vertex> newVertexMap() {
        return newElementMap("v");
    }

}
