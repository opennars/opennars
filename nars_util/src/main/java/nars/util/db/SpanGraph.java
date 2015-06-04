package nars.util.db;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import java.util.Map;

/**
 * Blueprints Graph interface with adjacency implemented by Infinispan collections
 */
public class SpanGraph<X> extends MapGraph<X> {

    public final InfiniPeer peer;
    private final String globalID;

    public SpanGraph(String id, InfiniPeer peer) {
        super(id, peer.userID); //local ID
        this.globalID = id; //ID as known on the network
        this.peer = peer;
        init();
    }

    protected <Y> Map<X, Y> newElementMap(String suffix) {
        return peer.the(globalID + '_' + suffix);
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
