package nars.db;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.AbstractElement;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.AbstractNode;
import org.graphstream.graph.implementations.MultiGraph;


import java.security.AccessControlException;
import java.util.*;

/**
 * Implements a distributed GraphStream Multigraph with HazelGraph
 * Adapted from: https://raw.githubusercontent.com/graphstream/gs-core/master/src/org/graphstream/graph/implementations/AdjacencyListGraph.java
 */
public class HazelGraph extends MultiGraph {


    private HazelcastInstance hazel;

    public HazelGraph(String id, boolean strictChecking, boolean autoCreate) {
        this(id, strictChecking, autoCreate, DEFAULT_NODE_CAPACITY,
                DEFAULT_EDGE_CAPACITY);
    }

    public HazelGraph(String id) {
        this(id, true, false);
    }

    public HazelGraph(String id, boolean strictChecking, boolean autoCreate, int initialNodeCapacity, int initialEdgeCapacity) {
        super(id, strictChecking, autoCreate, initialNodeCapacity, initialEdgeCapacity);
    }

    @Override
    protected void init(int initialNodeCapacity, int initialEdgeCapacity) {
        Config cfg = new Config();

        //cfg.setProperty( "hazelcast.logging.type", "none" );
        cfg.setProperty("hazelcast.memcache.enabled", "false");
        cfg.setProperty("hazelcast.rest.enabled", "false");
        cfg.setProperty("hazelcast.system.log.enabled", "false");

        cfg.setProperty("hazelcast.elastic.memory.enabled", "true");
        cfg.setProperty("hazelcast.elastic.memory.unsafe.enabled", "true");

        hazel = Hazelcast.newHazelcastInstance(cfg);

        super.init(initialNodeCapacity, initialEdgeCapacity);
    }

    @Override protected Map<String, Edge> newEdgeMap() {  return hazel.getMap(id + "_edgemap"); }
    @Override protected Map<String, Node> newNodeMap() {  return hazel.getMap(id + "_nodemap");    }

}