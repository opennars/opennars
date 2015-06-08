package spangraph;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Uses java.util.LinkedHashMap to implement adjacency
 */
public class ConcurrentHashMapGraph extends MapGraph {

    protected ConcurrentHashMapGraph() {
        super();

        init();
    }

    protected ConcurrentHashMapGraph(String globalGraphID) {
        super(globalGraphID);
    }

    @Override
    protected Map newEdgeMap() {
        return new ConcurrentHashMap<>();
    }

    @Override
    protected Map newVertexMap() {
        return new ConcurrentHashMap<>();
    }
}
