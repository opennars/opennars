/*
 * Copyright 2006 - 2013
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.graph.implementations;

import org.graphstream.graph.*;

import java.util.*;

/**
 * <p>
 * A lightweight graph class intended to allow the construction of big graphs
 * (millions of elements).
 * </p>
 * 
 * <p>
 * The main purpose here is to minimize memory consumption even if the
 * management of such a graph implies more CPU consuming. See the
 * <code>complexity</code> tags on each method so as to figure out the impact on
 * the CPU.
 * </p>
 */
public class AdjacencyListGraph extends AbstractGraph {

	public static final double GROW_FACTOR = 1.1;
	public static final int DEFAULT_NODE_CAPACITY = 128;
	public static final int DEFAULT_EDGE_CAPACITY = 1024;

	protected Map<String, Node> nodeMap;
	protected Map<String, Edge> edgeMap;

//	@Deprecated transient protected List<Node> nodeArray;
//    @Deprecated transient protected List<Edge> edgeArray;

	// *** Constructors ***

	/**
	 * Creates an empty graph.
	 * 
	 * @param id
	 *            Unique identifier of the graph.
	 * @param strictChecking
	 *            If true any non-fatal error throws an exception.
	 * @param autoCreate
	 *            If true (and strict checking is false), nodes are
	 *            automatically created when referenced when creating a edge,
	 *            even if not yet inserted in the graph.
	 * @param initialNodeCapacity
	 *            Initial capacity of the node storage data structures. Use this
	 *            if you know the approximate maximum number of nodes of the
	 *            graph. The graph can grow beyond this limit, but storage
	 *            reallocation is expensive operation.
	 * @param initialEdgeCapacity
	 *            Initial capacity of the edge storage data structures. Use this
	 *            if you know the approximate maximum number of edges of the
	 *            graph. The graph can grow beyond this limit, but storage
	 *            reallocation is expensive operation.
	 */
	public AdjacencyListGraph(String id, boolean strictChecking, boolean autoCreate,
			int initialNodeCapacity, int initialEdgeCapacity) {
		super(id, strictChecking, autoCreate);

        if (initialNodeCapacity < DEFAULT_NODE_CAPACITY)
            initialNodeCapacity = DEFAULT_NODE_CAPACITY;
        if (initialEdgeCapacity < DEFAULT_EDGE_CAPACITY)
            initialEdgeCapacity = DEFAULT_EDGE_CAPACITY;

        init(initialNodeCapacity, initialEdgeCapacity);
	}

    protected void init(int initialNodeCapacity, int initialEdgeCapacity) {
        setNodeFactory(new NodeFactory<AdjacencyListNode>() {
            public AdjacencyListNode newInstance(String id, Graph graph) {
                return new AdjacencyListNode((AbstractGraph) graph, id);
            }
        });

        setEdgeFactory(new EdgeFactory<AbstractEdge>() {
            public AbstractEdge newInstance(String id, Node src, Node dst,
                                            boolean directed) {
                return new AbstractEdge(id, (AbstractNode) src,
                        (AbstractNode) dst, directed);
            }
        });


        nodeMap = newNodeMap();
        edgeMap = newEdgeMap();


    }


    protected Map<String, Edge> newEdgeMap() {
		return new TreeMap<>();
	}

    protected Map<String, Node> newNodeMap() {
		return new TreeMap<>();
	}

	/**
	 * Creates an empty graph with default edge and node capacity.
	 * 
	 * @param id
	 *            Unique identifier of the graph.
	 * @param strictChecking
	 *            If true any non-fatal error throws an exception.
	 * @param autoCreate
	 *            If true (and strict checking is false), nodes are
	 *            automatically created when referenced when creating a edge,
	 *            even if not yet inserted in the graph.
	 */
	public AdjacencyListGraph(String id, boolean strictChecking, boolean autoCreate) {
		this(id, strictChecking, autoCreate, DEFAULT_NODE_CAPACITY,
				DEFAULT_EDGE_CAPACITY);
	}

	/**
	 * Creates an empty graph with strict checking and without auto-creation.
	 * 
	 * @param id
	 *            Unique identifier of the graph.
	 */
	public AdjacencyListGraph(String id) {
		this(id, true, false);
	}

	// *** Callbacks ***

	@Override
	protected void addEdgeCallback(Edge edge) {
		edgeMap.put(edge.getId(), edge);
//		int eas = edgeArray.size();
//		if (edgeCount == eas) {
//			List<Edge> tmp = newEdgeList((int) (eas * GROW_FACTOR) + 1);
//			//System.arraycopy(edgeArray, 0, tmp, 0, eas);
//            tmp.addAll(edgeArray);
//			//Collections.fill(edgeArray, null);
//			edgeArray = tmp;
//		}
//		edgeArray.set(edgeCount, edge);
//		((AbstractEdge)edge).setIndex(edgeCount++);
	}

	@Override
	protected void addNodeCallback(Node node) {
		nodeMap.put(node.getId(), node);
//		int nas = nodeArray.size();
//		if (nodeCount == nas) {
//			List<Node> tmp = newNodeList((int) (nas * GROW_FACTOR) + 1);
//            tmp.addAll(nodeArray);
//			//System.arraycopy(nodeArray, 0, tmp, 0, nas);
//			//Collections.fill(nodeArray, null);
//			nodeArray = tmp;
//		}
//		nodeArray.set(nodeCount, node);
//		((AbstractNode)node).setIndex(nodeCount++);
	}

	@Override
	protected void removeEdgeCallback(Edge edge) {
		edgeMap.remove(edge.getId());
//		int i = edge.getIndex();
//		Edge ea;
//		edgeArray.set(i,  ea = edgeArray.get(--edgeCount));
//        ((AbstractEdge)ea).setIndex(i);
//		edgeArray.set(edgeCount, null);
	}

	@Override
	protected void removeNodeCallback(Node node) {
		nodeMap.remove(node.getId());
//		int i = node.getIndex();
//		Node ea;
//		nodeArray.set(i,  ea = nodeArray.get(--nodeCount));
//        ((AbstractNode)ea).setIndex(i);
//		nodeArray.set(nodeCount,  null);
	}

	@Override
	protected void clearCallback() {
		nodeMap.clear();
		edgeMap.clear();

	}



	@Override
	public Edge getEdge(String id) {
		return edgeMap.get(id);
	}


	@Override
	public int getEdgeCount() {
		return edgeMap.size();
	}

	@Override
	public Node getNode(String id) {
		return nodeMap.get(id);
	}



	@Override
	public int getNodeCount() {
		return nodeMap.size();
	}

	// *** Iterators ***

    @Override
	public Iterator<Edge> getEdgeIterator() {
		return edgeMap.values().iterator();
	}

	@Override
	public Iterator<Node> getNodeIterator() {
		return nodeMap.values().iterator();
	}

}
