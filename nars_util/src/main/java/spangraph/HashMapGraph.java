package spangraph;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Uses java.util.LinkedHashMap to implement adjacency
 */
public class HashMapGraph extends MapGraph {

    protected HashMapGraph() {
        super();

        init();
    }

    protected HashMapGraph(String globalGraphID) {
        super(globalGraphID);
    }

    @Override
    protected Map newEdgeMap() {
        return new LinkedHashMap<>();
    }

    @Override
    protected Map newVertexMap() {
        return new LinkedHashMap<>();
    }
}
