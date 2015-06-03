package nars.db;

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


        super.init(initialNodeCapacity, initialEdgeCapacity);
    }

    @Override protected Map<String, Edge> newEdgeMap() {
        //return hazel.getMap(id + "_edgemap"); }
        return null;
    }
    @Override protected Map<String, Node> newNodeMap() {
            //return hazel.getMap(id + "_nodemap")}
            return null;
        }

}