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

import com.google.common.collect.Collections2;
import com.hazelcast.util.CollectionUtil;
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

	protected final Map<String, Node> nodeMap;
	protected final Map<String, Edge> edgeMap;

	protected List<Node> nodeArray;
	protected List<Edge> edgeArray;

	protected int nodeCount;
	protected int edgeCount;

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

		if (initialNodeCapacity < DEFAULT_NODE_CAPACITY)
			initialNodeCapacity = DEFAULT_NODE_CAPACITY;
		if (initialEdgeCapacity < DEFAULT_EDGE_CAPACITY)
			initialEdgeCapacity = DEFAULT_EDGE_CAPACITY;

		nodeMap = newNodeMap();
		edgeMap = newEdgeMap();
		nodeArray = newNodeList(initialNodeCapacity);
		edgeArray = newEdgeList(initialEdgeCapacity);;
		nodeCount = edgeCount = 0;
	}

	private List<Node> newNodeList(int initialNodeCapacity) {
		return new ArrayList(initialNodeCapacity);
	}
	private List<Edge> newEdgeList(int initialNodeCapacity) {
		return new ArrayList(initialNodeCapacity);
	}

	private Map<String, Edge> newEdgeMap() {
		return new TreeMap<>();
	}

	private Map<String, Node> newNodeMap() {
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
		int eas = edgeArray.size();
		if (edgeCount == eas) {
			List<Edge> tmp = newEdgeList((int) (eas * GROW_FACTOR) + 1);
			System.arraycopy(edgeArray, 0, tmp, 0, eas);
			Collections.fill(edgeArray, null);
			edgeArray = tmp;
		}
		edgeArray.set(edgeCount, edge);
		((AbstractEdge)edge).setIndex(edgeCount++);
	}

	@Override
	protected void addNodeCallback(Node node) {
		nodeMap.put(node.getId(), node);

		int nas = nodeArray.size();
		if (nodeCount == nas) {
			List<Node> tmp = newNodeList((int) (nas * GROW_FACTOR) + 1);
			System.arraycopy(nodeArray, 0, tmp, 0, nas);
			Collections.fill(nodeArray, null);
			nodeArray = tmp;
		}
		nodeArray.set(nodeCount, node);
		((AbstractNode)node).setIndex(nodeCount++);
	}

	@Override
	protected void removeEdgeCallback(Edge edge) {
		edgeMap.remove(edge.getId());
		int i = edge.getIndex();
		Edge ea;
		edgeArray.set(i,  ea = edgeArray.get(--edgeCount));
        ((AbstractEdge)ea).setIndex(i);
		edgeArray.set(edgeCount, null);
	}

	@Override
	protected void removeNodeCallback(Node node) {
		nodeMap.remove(node.getId());
		int i = node.getIndex();

		Node ea;
		nodeArray.set(i,  ea = nodeArray.get(--nodeCount));
        ((AbstractNode)ea).setIndex(i);
		nodeArray.set(nodeCount,  null);
	}

	@Override
	protected void clearCallback() {
		nodeMap.clear();
		edgeMap.clear();

		Collections.fill(nodeArray, null); //(nodeArray, 0, nodeCount, null);
		Collections.fill(edgeArray, null); //fill(edgeArray, 0, edgeCount, null);
		nodeCount = edgeCount = 0;
	}



	@SuppressWarnings("unchecked")
	@Override
	public Edge getEdge(String id) {
		return edgeMap.get(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Edge getEdge(int index) {
		if (index < 0 || index >= edgeCount)
			throw new IndexOutOfBoundsException(index
					+ " does not exist");
		return edgeArray.get(index);
	}

	@Override
	public int getEdgeCount() {
		return edgeCount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Node getNode(String id) {
		return nodeMap.get(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Node getNode(int index) {
		if (index < 0 || index > nodeCount)
			throw new IndexOutOfBoundsException(index
					+ " does not exist");
		return nodeArray.get(index);
	}

	@Override
	public int getNodeCount() {
		return nodeCount;
	}

	// *** Iterators ***

	public class EdgeIterator<T extends Edge> implements Iterator<T> {
		int iNext = 0;
		int iPrev = -1;

        @Override
		public boolean hasNext() {
			return iNext < edgeCount;
		}

		@SuppressWarnings("unchecked")
        @Override
		public T next() {
			if (iNext >= edgeCount)
				throw new NoSuchElementException();
			iPrev = iNext++;
			return (T) edgeArray.get(iPrev);
		}

        @Override
		public void remove() {
			if (iPrev == -1)
				throw new IllegalStateException();
			removeEdge(edgeArray.get(iPrev), true, true, true);
			iNext = iPrev;
			iPrev = -1;
		}
	}

	public class NodeIterator<T extends Node> implements Iterator<T> {
		int iNext = 0;
		int iPrev = -1;

        @Override
		public boolean hasNext() {
			return iNext < nodeCount;
		}

		@SuppressWarnings("unchecked")
        @Override
		public T next() {
			if (iNext >= nodeCount)
				throw new NoSuchElementException();
			iPrev = iNext++;
			return (T) nodeArray.get(iPrev);
		}

        @Override
		public void remove() {
			if (iPrev == -1)
				throw new IllegalStateException();
			removeNode(nodeArray.get(iPrev), true);
			iNext = iPrev;
			iPrev = -1;
		}
	}

	@Override
	public <T extends Edge> Iterator<T> getEdgeIterator() {
		return new EdgeIterator<T>();
	}

	@Override
	public <T extends Node> Iterator<T> getNodeIterator() {
		return new NodeIterator<T>();
	}
}
