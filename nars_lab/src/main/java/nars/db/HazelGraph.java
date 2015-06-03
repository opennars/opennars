package nars.db;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.AbstractElement;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.AbstractNode;
import org.graphstream.graph.implementations.MultiGraph;


import java.security.AccessControlException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implements a distributed GraphStream Multigraph with HazelGraph
 * Adapted from: https://raw.githubusercontent.com/graphstream/gs-core/master/src/org/graphstream/graph/implementations/AdjacencyListGraph.java
 */
public class HazelGraph extends MultiGraph {

    public HazelGraph(String id, boolean strictChecking, boolean autoCreate, int initialNodeCapacity, int initialEdgeCapacity) {
        super(id, strictChecking, autoCreate, initialNodeCapacity, initialEdgeCapacity);
    }

    public HazelGraph(String id, boolean strictChecking, boolean autoCreate) {
        this(id, strictChecking, autoCreate, DEFAULT_NODE_CAPACITY,
                DEFAULT_EDGE_CAPACITY);
    }

    public HazelGraph(String id) {
        this(id, true, false);
    }


}