package ca.nengo.math.impl;

import ca.nengo.math.NetworkPartitioner;
import ca.nengo.model.Node;
import ca.nengo.model.NSource;
import ca.nengo.model.Projection;
import ca.nengo.model.NTarget;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.NetworkImpl.SourceWrapper;
import ca.nengo.model.impl.NetworkImpl.TargetWrapper;
import ca.nengo.neural.nef.impl.NEFGroupImpl;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

import java.util.*;

/**
 * Employs the multi-level Kernighan-Lin graph partitioning heuristic to partition
 * a network into a given number of partitions such that the amount of information
 * passed along the projections that cross partitions is minimized, while making sure
 * the number of neurons in each partition is relatively balanced.
 * 
 * @author e2crawfo
 *
 */

public class MultiLevelKLNetworkPartitioner implements NetworkPartitioner {
	
	private int myNumPartitions;
	private WeightedGraph<NodeVertex, DefaultWeightedEdge> myGraph;
	private Node[] myNodes;
	private double myBalanceFactor = 0.1;
	
	public double getBalanceFactor() {
		return myBalanceFactor;
	}

	public void setBalanceFactor(double myBalanceFactor) {
		this.myBalanceFactor = myBalanceFactor;
	}

	public void initialize(Node[] nodes, Projection[] projections, int numPartitions) {
		myNodes = nodes;
		myGraph = convertNetworkToGraph(nodes, projections);
		myNumPartitions = numPartitions;
	}

	public ArrayList<Set<Node>> getPartitions() {
		
		ArrayList<Set<NodeVertex>> partitions = partitionGraph(myGraph, myNumPartitions);
		ArrayList<Set<Node>> nodePartitions = new ArrayList<Set<Node>>();
		
		for(Set<NodeVertex> part : partitions){
			
			Set<Node> newPart = new HashSet<Node>();
		
			for(NodeVertex nodeVertex : part){
				newPart.add(nodeVertex.getNode());
			}
			
			nodePartitions.add(newPart);
		}
		
		return nodePartitions;
	}
		
	public int[] getPartitionsAsIntArray() {
		ArrayList<Set<Node>> nodePartitions = getPartitions();
		int[] nodeAssignments = new int[myNodes.length];

		int nodeIndex = 0;
		for(Node node : myNodes){
			int partitionIndex = 0;
			while(!nodePartitions.get(partitionIndex++).contains(node) && partitionIndex < nodePartitions.size()){
			}
			
			if(partitionIndex < nodePartitions.size()){
				nodeAssignments[nodeIndex] = partitionIndex;
			}
			
			nodeIndex++;
		}
		
		return nodeAssignments;
	}

	/**
	 * Find the vertices in the neighbourhood of given vertex in a given graph.
	 * 
	 * @author Eric Crawford
	 */
	private <V extends Vertex> Set<V>
	getNeighbourhood(WeightedGraph<V, DefaultWeightedEdge> graph, V vertex){
		
		Set<DefaultWeightedEdge> edgeSet = graph.edgesOf(vertex);
		Set<V> neighbourhood = new HashSet<V>();
		
		for(DefaultWeightedEdge curEdge : edgeSet ){
			V neighbour = graph.getEdgeSource(curEdge);
			
			if(neighbour == vertex){
				neighbour = graph.getEdgeTarget(curEdge);
			}
			
			neighbourhood.add(neighbour);
		}
		
		return neighbourhood;
	}
	
	/**
	 * Takes a graph and returns a "coarser" version of it. The graph is coarsened as follows:
	 * 
	 * 1. Find a maximal heavy-edge matching. A matching is a subset of the edges in the graph such
	 * that no two edges are incident on the same vertex. A maximal matching is a matching
	 * we can't add more edges to.
	 * 
	 * The "heavy-edge" part comes from the fact that we randomly visit vertices which aren't yet in
	 * the matching, and add their heaviest incident edge which are not incident on another vertex already
	 * in the matching.
	 * 
	 * 2. Add vertices to the coarse graph as follows: 
	 * 
	 * - Every vertex in the original graph which did not have an edge in the matching gets a corresponding 
	 *   vertex in the coarse graph. The new vertex in the coarse graph is called the "parent" of the vertex 
	 *   in the original graph.
	 *   
	 * - Every edge in the matching gets a corresponding vertex  in the coarse graph.
	 * 	 The new vertex in the coarse graph is called the "parent" of the two vertices the edge
	 * 	 was incident on. The parent vertex has weight equal to the sum of the weights of the two child vertices.
	 * 
	 * 3. Add edges to the coarse graph as follows: 
	 * 
	 * - For each edge in the original graph which was not in the matching, add an edge between the 
	 * 	 parents of the two vertices the edge is incident on. If it turns out there are multiple edges between 
	 *   two vertices in the coarse graph, combine them into a single edge, summing their weights.
	 * 
	 * @author Eric Crawford
	 */
	private <V extends Vertex> WeightedGraph<Vertex, DefaultWeightedEdge> 
			coarsenGraph(WeightedGraph<V, DefaultWeightedEdge> graph){
		
		//Compute heavy-edge maximal matching.
		LinkedList<V> vertexOrder = new LinkedList<V>(graph.vertexSet());
		
		Collections.shuffle(vertexOrder);
		
		Set<V> verticesInMatching = new HashSet<V>();
		Set<DefaultWeightedEdge> edgesInMatching = new HashSet<DefaultWeightedEdge>();
		
		EdgeComparator edgeComparator = new EdgeComparator(graph);
		
		for(V curVertex : vertexOrder){
			
			if(!verticesInMatching.contains(curVertex)){
				
				verticesInMatching.add(curVertex);
				
				Set<DefaultWeightedEdge> curEdges = graph.edgesOf(curVertex);
				
				LinkedList<DefaultWeightedEdge> curEdgesList = new LinkedList<DefaultWeightedEdge>(curEdges);
				
				Collections.sort(curEdgesList, edgeComparator);
				
				Iterator<DefaultWeightedEdge> edgeIter = curEdgesList.iterator();
				while(edgeIter.hasNext()){
					DefaultWeightedEdge curEdge = edgeIter.next();
					V neighbourVertex = graph.getEdgeSource(curEdge);
					
					if(neighbourVertex == curVertex){
						neighbourVertex = graph.getEdgeTarget(curEdge);
					}
					
					if(!verticesInMatching.contains(neighbourVertex)){
						// We've found an edge to add to the matching
						edgesInMatching.add(curEdge);
						verticesInMatching.add(neighbourVertex);
						break;
					}
				}
			}
		}
		
		// now use the matching to construct the coarser graph
		WeightedGraph<Vertex, DefaultWeightedEdge> coarseGraph = 
			new SimpleWeightedGraph<Vertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		// add to the coarse graph vertices which correspond to edges in the matching
		for(DefaultWeightedEdge curEdge : edgesInMatching){
			Vertex newVertex = new Vertex();
			
			V source = graph.getEdgeSource(curEdge);
			V target = graph.getEdgeTarget(curEdge);
			
			newVertex.addSubordinate(source);
			newVertex.addSubordinate(target);
			
			coarseGraph.addVertex(newVertex);
			
			verticesInMatching.remove(source);
			verticesInMatching.remove(target);
		}
		
		// verticesInMatching now only contains lone vertices,
		// those which weren't assigned a partner in the matching :(
		for(V curVertex : verticesInMatching){
			Vertex newVertex = new Vertex();
			newVertex.addSubordinate(curVertex);
			coarseGraph.addVertex(newVertex);
		}

		// the courseGraph has all the vertices it'll ever get, now it needs the edges
		for(DefaultWeightedEdge curEdge : graph.edgeSet()){
			Vertex parent1 = graph.getEdgeSource(curEdge).getParent();
			Vertex parent2 = graph.getEdgeTarget(curEdge).getParent();
			
			if(parent1 != parent2){
				
				double oldEdgeWeight = graph.getEdgeWeight(curEdge);
				DefaultWeightedEdge edgeInCoarseGraph = coarseGraph.getEdge(parent1, parent2);
				
				if(edgeInCoarseGraph != null){
					coarseGraph.setEdgeWeight(edgeInCoarseGraph, coarseGraph.getEdgeWeight(edgeInCoarseGraph) + oldEdgeWeight);
				}else{
					edgeInCoarseGraph = coarseGraph.addEdge(parent1, parent2);
					coarseGraph.setEdgeWeight(edgeInCoarseGraph, oldEdgeWeight);
				}
			}
		}
		
		return coarseGraph;
	}
	
	/**
	 * Applies a partition to a graph. Puts all the vertices in the given set on one side of the partition,
	 * and the rest of the vertices on the other.
	 * 
	 * @author Eric Crawford
	 */
	private static <V extends Vertex> void applyPartition(Graph<V, DefaultWeightedEdge> graph, Set<V> partition){
		for(Vertex v : graph.vertexSet()){
			if(partition.contains(v)){
				v.setPartition(true);
			}else{
				v.setPartition(false);
			}
		}
	}

	/**
	 * Implements the Kernighan-Lin heuristic (note this is different from multi-level Kernighan-Lin) for
	 * improving a given partition of a graph. Works by finding promising sets of vertices on opposite sides
	 * of the parition to swap, and swapping the two sets which will give the best gain in the size of the min-cut.
	 * 
	 * For pseudo code, see the relevant page in:
	 * http://www.cs.berkeley.edu/~demmel/cs267_Spr99/Lectures/Lect_15_1999.pdf
	 * One difference in our implementation is that we only consider swaps which maintain a good
	 * balance between the two sides of the partition.
	 * 
	 * @author Eric Crawford
	 */
	private <V extends Vertex> Set<V>
	kernighanLinPartitionImprovement(Set<V> partition, WeightedGraph<V, DefaultWeightedEdge> graph){
		Set<V> vertexSet = graph.vertexSet();
		Set<V> sideA = new HashSet<V>(partition);
		Set<V> sideB = new HashSet<V>(graph.vertexSet());
		sideB.removeAll(sideA);

		double totalVertexWeight = 0;
		for(V v : vertexSet){
			totalVertexWeight += v.getWeight();
		}
		
		Map<V, Double> vertexCosts = new HashMap<V, Double>();
		
		double maxOverallGain = 1.0;
		
		while(maxOverallGain > 0.0){
			
			//apply the partition to the graph.
			applyPartition(graph, sideA);
			
			double sideAVertexWeight = 0;
			for(V v : sideA){
				sideAVertexWeight += v.getWeight();
			}

			Set<V> unmarkedVerticesA = new HashSet<V>(sideA);
			Set<V> unmarkedVerticesB = new HashSet<V>(sideB);
			Set<V> markedVertices = new HashSet<V>();
			
			LinkedList<VertexPairContainer<V>> maxGainsList = new LinkedList<VertexPairContainer<V>>();
			
			while(!unmarkedVerticesA.isEmpty() && !unmarkedVerticesB.isEmpty()){
				
				// Compute cost D for each vertex
				for(V curVertex : vertexSet){
					double vertexCost = 0;
					
					Set<DefaultWeightedEdge> incidentEdges = graph.edgesOf(curVertex);
					
					for(DefaultWeightedEdge curEdge : incidentEdges){
						Vertex neighbourVertex = graph.getEdgeSource(curEdge);
						
						if(neighbourVertex == curVertex){
							neighbourVertex = graph.getEdgeTarget(curEdge);
						}
						
						if(curVertex.getPartition() == neighbourVertex.getPartition()){
							vertexCost -= graph.getEdgeWeight(curEdge); 
						}else{
							vertexCost += graph.getEdgeWeight(curEdge); 
						}
					}
					
					vertexCosts.put(curVertex, vertexCost);
				}
				
				// calculate pair of unmarked vertices which gives max gain in cut quality if switched
				double maxGain = Double.NEGATIVE_INFINITY;
				V maxGainVertexA = null, maxGainVertexB = null;
				for(V curVertexA : unmarkedVerticesA){
					for(V curVertexB : unmarkedVerticesB){
						DefaultWeightedEdge currentEdge = graph.getEdge( curVertexA, curVertexB);
						double edgeWeight = (currentEdge != null) ? graph.getEdgeWeight(currentEdge) : 0.0;
						
						double gain = vertexCosts.get(curVertexA) + vertexCosts.get(curVertexB) - 2 * edgeWeight;
						
						if(gain > maxGain){
							maxGain = gain;
							maxGainVertexA = curVertexA;
							maxGainVertexB = curVertexB;
						}
					}
				}
				
				VertexPairContainer<V> pairContainer = new VertexPairContainer<V>(maxGain, maxGainVertexA, maxGainVertexB);
				maxGainsList.addLast(pairContainer);
				
				// swap a and b in the partition applied to the graph
				// we recalculate the costs of the remaining vertices
				// as if a and b were swapped.
				maxGainVertexA.switchPartition();
				maxGainVertexB.switchPartition();
				
				unmarkedVerticesA.remove(maxGainVertexA);
				unmarkedVerticesB.remove(maxGainVertexB);
				markedVertices.add(maxGainVertexA);
				markedVertices.add(maxGainVertexB);
			}
			
			maxOverallGain = Double.NEGATIVE_INFINITY;
			double runningSum = 0.0;
			int maxOverallGainIndex = 0;
			double sideAVertexWeightAfterSwap = sideAVertexWeight;
			
			// Compute the two swap sets which will give the highest gain while
			// maintaining a good balance between the two sides of the partition.
			int j = 0;
			for(VertexPairContainer<V> pairContainer : maxGainsList){
				runningSum += pairContainer.getVal();
				
				sideAVertexWeightAfterSwap -= pairContainer.myVertexA.getWeight();
				sideAVertexWeightAfterSwap += pairContainer.myVertexB.getWeight();
				
				double balance = sideAVertexWeightAfterSwap / totalVertexWeight;
				boolean goodBalance = Math.abs(balance - 0.5) < myBalanceFactor;
				
				if(maxOverallGain < runningSum && goodBalance){
					maxOverallGain = runningSum;
					maxOverallGainIndex = j;
				}
				
				j++;
			}
			
			// If there is a swap that will give positive gain, then make it so.
			if(maxOverallGain > 0.0){
				Set<V> sideASwap = new HashSet<V>();
				Set<V> sideBSwap = new HashSet<V>();
				
				ListIterator<VertexPairContainer<V>> iter = maxGainsList.listIterator();
				
				for(j = 0; j <= maxOverallGainIndex; j++){
					
					VertexPairContainer<V> pairContainer = iter.next();
					
					sideASwap.add(pairContainer.getVertexA());
					sideBSwap.add(pairContainer.getVertexB());
				}
				
				sideA.removeAll(sideASwap);
				sideA.addAll(sideBSwap);
				
				sideB.removeAll(sideBSwap);
				sideB.addAll(sideASwap);
			}
		}
		
		return sideA;
	}

	/**
	 * Implements the multi-level Kernighan-Lin heuristic. See 
	 * http://www.cc.gatech.edu/~bader/COURSES/GATECH/CSE6140-Fall2007/papers/KK95a.pdf 
	 * for an overview, though this algorithm was not followed exactly.
	 * 
	 *  Creates a "coarser" version of the original graph. If the coarse graph is not much smaller
	 *  than the original graph, then simply partition the original graph using a BFS algorithm starting from
	 *  a random vertex. Otherwise, recursively call this function on the coarser graph to find a good 
	 *  partition of it. Use this coarse-graph partition to get a partition of the original 
	 *  graph. Use the Kernighan-Lin heuristic (note this is distinct from multi-level Kernighan Lin) to 
	 *  improve this partition. Return the improved partition.
	 * 
	 * @author Eric Crawford
	 */
	@SuppressWarnings("unchecked")
	private <V extends Vertex> Set<V> multilevelKL(WeightedGraph<V, DefaultWeightedEdge> graph){
		
		Set<V> vertexSet = graph.vertexSet();
		
		int graphSize = vertexSet.size();
		System.out.println("In multilevelKL with " + graphSize + " vertices.");
		int tooBig = 10;

		WeightedGraph<Vertex, DefaultWeightedEdge> coarseGraph = null;
		
		if(!(graphSize < tooBig)){
			coarseGraph = coarsenGraph(graph);
		}
		
		double reductionThreshold = 0.8;
		
		// if coarsening the graph does not significantly reduce the number of vertices,
		// or there weren't that many to begin with, then just partition the original graph
		if(graphSize < tooBig || coarseGraph.vertexSet().size() > reductionThreshold * vertexSet.size()){
			
			// run the BFS algorithm several times
			int numReps = Math.min(20, graphSize);
			
			double totalVertexWeight = 0;
			for(V v : vertexSet){
				totalVertexWeight += v.getWeight();
			}
			
			double minCut = Double.POSITIVE_INFINITY;
			Set<V> minCutPartition = new HashSet<V>();
			
			List<V> startVertexList = new LinkedList<V>(vertexSet);
			Collections.shuffle(startVertexList);
			
			ListIterator<V> startVertexIter = startVertexList.listIterator();
			
			for(int i = 0; i < numReps; i++){
				
				V startVertex = startVertexIter.next();
				
				// start with a different random vertex on each rep,
				// run BFS and see which gives best partition
				double partitionVertexWeight = 0.0;
				double balance = 0.0;
				
				PriorityQueue<V> queue = new PriorityQueue<V>();
				Set<V> partition = new HashSet<V>();
				Set<V> checked = new HashSet<V>();
				LinkedList<V> unchecked = new LinkedList<V>(vertexSet);
				Collections.shuffle(unchecked);
				
				queue.add(startVertex);
				
				while(!queue.isEmpty() && balance < 0.5){
					V curVertex = queue.poll();
					double curVertexWeight = curVertex.getWeight();
					
					double balanceWithCurrentVertex = (partitionVertexWeight + curVertexWeight) / totalVertexWeight;
					boolean betterBalance = Math.abs(balanceWithCurrentVertex - 0.5) < Math.abs(balance - 0.5);
					
					// if adding the current head of the queue to 
					// the partition would give a better balance, then do it
					if(betterBalance){
						partition.add(curVertex);
						
						partitionVertexWeight += curVertexWeight;
						
						balance = partitionVertexWeight / totalVertexWeight;
						
						Set<V> neighbourhood = getNeighbourhood(graph, curVertex);
						
						neighbourhood.removeAll(checked);
						
						queue.addAll(neighbourhood);
					}
					
					checked.add(curVertex);
					unchecked.remove(curVertex);
					
					// if the queue is empty but we don't yet have a good balance, randomly choose a vertex
					// we haven't visited yet and start from there (still on the same rep)
					if(queue.isEmpty() && !unchecked.isEmpty() && (Math.abs(balance - 0.5) > myBalanceFactor)){
						queue.add(unchecked.get(0));
					}
				}
				
				// find the cut value of the partition found on this rep
				double cutValue = 0.0;
				for(DefaultWeightedEdge curEdge : graph.edgeSet()){
					V source = graph.getEdgeSource(curEdge);
					V target = graph.getEdgeTarget(curEdge);
					
					boolean sourceInPartition = partition.contains(source);
					boolean targetInPartition = partition.contains(target);
					
					if(sourceInPartition != targetInPartition){
						cutValue += graph.getEdgeWeight(curEdge);
					}
				}
				
				// compare current partition to the best so far, as long as current partition has decent balance
				if(Math.abs(balance - 0.5) < myBalanceFactor){
					if(cutValue < minCut){
						minCut = cutValue;
						minCutPartition = partition;
					}
				}
			}
			
			return minCutPartition;
			
		}else{
			
			Set<Vertex> coarsePartition = multilevelKL(coarseGraph);

			// Use partition on coarse graph to find a partition of the original graph
			Set<V> partition = new HashSet<V>();
			
			for(Vertex v : coarsePartition){
				Set<Vertex> subordinates = v.getSubordinates();
				
				for(Vertex subord : subordinates){
					
					//Could get rid of this cast by adding a type parameter to Vertex class
					partition.add((V) subord);
				}
			}
		
			partition = kernighanLinPartitionImprovement(partition, graph);
			
			return partition;
		}
	}
	
	/**
	 * Takes a set of nodes and projections which are assumed to form a network and returns a graph.
	 * The edges of the graph correspond to the projections, and are weighted according to the amount of data 
	 * that passes along the projection. The vertices of the graph correspond to the nodes, and are weighted
	 * according to the number of neurons in the node. Self loops are disregarded. If 
	 * there are multiple projections between a pair of nodes, then a single edge is constructed whose 
	 * weight is the sum of the dimensions of these projections. The direction of the projections is also
	 * ignored. Thus the returned graph is a simple (no self-loops), edge-weighted, vertex-weighted, undirected graph.
	 * 
	 * @author Eric Crawford
	 */
	private WeightedGraph<NodeVertex, DefaultWeightedEdge> convertNetworkToGraph(Node[] nodes, Projection[] projections){
		
		WeightedGraph<NodeVertex, DefaultWeightedEdge> networkGraph = new SimpleWeightedGraph<NodeVertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		Map<Node, NodeVertex> nodeToVertex = new HashMap<Node, NodeVertex>();
		
		for(Node node : nodes){
			NodeVertex vertex = new NodeVertex(node);
			networkGraph.addVertex(vertex);
			
			nodeToVertex.put(node, vertex);
		}
	
		for(Projection proj : projections){
			
			NTarget projectionTarget = proj.getTarget();
			boolean projectionTerminationWrapped = projectionTarget instanceof TargetWrapper;
			if(projectionTerminationWrapped)
				projectionTarget = ((TargetWrapper) projectionTarget).getBaseTermination();
			
			Node terminationNode = projectionTarget.getNode();

			NSource projectionSource = proj.getSource();
			boolean projectionOriginWrapped = projectionSource instanceof SourceWrapper;
			
			if(projectionOriginWrapped)
				projectionSource = ((SourceWrapper) projectionSource).getWrappedOrigin();
			
			Node originNode = projectionSource.getNode();
			
			//Don't add self loops, since they are irrelevant to the min-cut computation 
			//(and it allows us to use a SimpleWeightedGraph object).
			if(originNode != terminationNode){
				NodeVertex terminationVertex = nodeToVertex.get(terminationNode);  
				NodeVertex originVertex = nodeToVertex.get(originNode);  
				
				DefaultWeightedEdge edge = networkGraph.addEdge(originVertex, terminationVertex);
				int edgeWeight = projectionTarget.getDimensions();
				
				if(edge != null){
					networkGraph.setEdgeWeight(edge, (double)edgeWeight);
				}else{
					edge = networkGraph.getEdge(originVertex, terminationVertex);
					double newEdgeWeight = edgeWeight + networkGraph.getEdgeWeight(edge);
					networkGraph.setEdgeWeight(edge, newEdgeWeight);
				}
			}
		}
		
		return networkGraph;
	}
	
	
	/**
	 * Takes takes a graph with weighted edges and partitions it into the given number of partitions.
	 * Uses the multi-level Kernighan-Lin heuristic to minimize the weight of the edges between
	 * the partitions (min-cut) while ensuring the sums of the weights of the vertices on each
	 * side of the partition are relatively balanced. Uses a heuristic because this problem is NP-Complete.
	 * 
	 * @author Eric Crawford
	 */
	private <V extends Vertex> ArrayList<Set<V>> 
		partitionGraph(WeightedGraph<V, DefaultWeightedEdge> graph, int numPartitions){
		
		ArrayList<Set<V>> partitions = new ArrayList<Set<V>>();
		
		if(numPartitions < 1){
			return partitions;
		}else if(graph.vertexSet().size() <= numPartitions){
			
			// In this case there is no point in computing min cut, 
			//just assign each node to a different partition.
			Iterator<V> nodeIter = graph.vertexSet().iterator();
			for(int i = 0; i < numPartitions; i++){
				Set<V> newSet = new HashSet<V>();
				
				if(nodeIter.hasNext()){
					newSet.add(nodeIter.next());
				}
				
				partitions.add(newSet);
			}
			
			return partitions;
			
		}else if(numPartitions == 1){
			partitions.add(graph.vertexSet());
			return partitions;
		}
		
		Set<V> partition = multilevelKL(graph);
		
		Set<V> leftSubgraphVertexSet = partition;
		Set<V> rightSubgraphVertexSet = new HashSet<V>(graph.vertexSet());
		for(V node : leftSubgraphVertexSet){
			rightSubgraphVertexSet.remove(node);
		}
		
		// swap to make sure left partition is the larger one.
		if(leftSubgraphVertexSet.size() < rightSubgraphVertexSet.size()){
			Set<V> temp = rightSubgraphVertexSet;
			rightSubgraphVertexSet = leftSubgraphVertexSet;
			leftSubgraphVertexSet = temp;
		}
		
		if(numPartitions == 2){
			partitions.add(leftSubgraphVertexSet);
			partitions.add(rightSubgraphVertexSet);
			return partitions;
		}

		Iterator<DefaultWeightedEdge> edgeIter = graph.edgeSet().iterator();
		
		Set<DefaultWeightedEdge> leftSubgraphEdgeSet = new HashSet<DefaultWeightedEdge>();
		Set<DefaultWeightedEdge> rightSubgraphEdgeSet = new HashSet<DefaultWeightedEdge>();
		
		while(edgeIter.hasNext()){
			DefaultWeightedEdge edge = edgeIter.next();
			
			Vertex source = graph.getEdgeSource(edge);
			Vertex target = graph.getEdgeTarget(edge);
			
			boolean sourceInLeft = leftSubgraphVertexSet.contains(source);
			boolean targetInLeft = leftSubgraphVertexSet.contains(target);
			
			if(sourceInLeft && targetInLeft){
				leftSubgraphEdgeSet.add(edge);
			}else if(!sourceInLeft && !targetInLeft){
				rightSubgraphEdgeSet.add(edge);
			}
		}

		WeightedGraph<V, DefaultWeightedEdge> leftSubgraph, rightSubgraph;
		
		leftSubgraph = 
			new UndirectedWeightedSubgraph<V, DefaultWeightedEdge>(graph, leftSubgraphVertexSet, leftSubgraphEdgeSet);
		
		rightSubgraph = 
			new UndirectedWeightedSubgraph<V, DefaultWeightedEdge>(graph, rightSubgraphVertexSet, rightSubgraphEdgeSet);
		
		int numLeftSubPartitions = (int) Math.ceil((double) numPartitions / 2), 
			numRightSubPartitions = (int) Math.floor((double) numPartitions / 2);
		
		ArrayList<Set<V>> leftPartitions = partitionGraph(leftSubgraph, numLeftSubPartitions);
		ArrayList<Set<V>> rightPartitions = partitionGraph(rightSubgraph, numRightSubPartitions);
		
		partitions.addAll(leftPartitions);
		partitions.addAll(rightPartitions);
		return partitions;
	}
	
	
	/*
	 * Classes for partitioning
	 */
	
	/**
	 * A weighted vertex. Member mySubordinates supports hierarchical graph algorithms; vertices in a lower level graph
	 * can "belong to" a vertex in a higher level graph as subordinates. Member myParent stores the vertex to which the 
	 * current vertex is subordinate. Also has a partition field which allows us to apply a bipartition to a graph. 
	 * Vertices on one side of the bipartition have myPartition = false, vertices on the other have myPartition = true. 
	 * Can be compared to other vertices using weight as the measure of comparison.
	 * 
	 * @author Eric Crawford
	 */
	private static class Vertex implements Comparable<Vertex>{
		
		double myWeight;
		final Set<Vertex> mySubordinates;
		Vertex myParent;
		boolean myPartition;

		public Vertex(){
			myWeight = 0;
			mySubordinates = new HashSet<Vertex>();
		}
		
		public double getWeight(){
			return myWeight;
		}
		
		public void addSubordinate(Vertex sub){
			sub.setParent(this);
			mySubordinates.add(sub);
			myWeight += sub.getWeight();
		}
		
		public Set<Vertex> getSubordinates(){
			return mySubordinates;
		}
		
		public Vertex getParent() {
			return myParent;
		}

		public void setParent(Vertex parent) {
			this.myParent = parent;
		}
		
		public boolean getPartition(){
			return myPartition;
		}
		
		public void setPartition(boolean partition){
			myPartition = partition;
		}
		
		public void switchPartition(){
			myPartition = !myPartition;
		}
		
		public int compareTo(Vertex v){
			double weight1 = this.getWeight();
			double weight2 = v.getWeight();
				
			if(weight1 > weight2)
				return 1;
			else if(weight1 < weight2)
				return -1;
			else
				return 0;
		}
	}
	
	/**
	 * A weighted vertex which corresponds to a node in a network. The number of neurons in the node is added
	 * to the weight of the vertex since this class is used for partitioning Nengo networks.
	 * 
	 * @author Eric Crawford
	 */
	private static class NodeVertex extends Vertex{
		
		Node myNode;
		
		public NodeVertex(Node node){
			super();
			setNode(node);
		}
		
		public void setNode(Node node){
			myNode = node;
			
			
			if(myNode instanceof NetworkImpl){
				myWeight += ((NetworkImpl) node).getNeuronCount();
			}else if(myNode instanceof NEFGroupImpl){
				myWeight += ((NEFGroupImpl) node).getNeuronCount();
			}
		}
		
		public Node getNode(){
			return myNode;
		}
	}

	/**
	 * Compare edges by their weights in a given graph. Used for sorting edges.
	 * 
	 * @author Eric Crawford
	 */
	private static class EdgeComparator implements Comparator<DefaultWeightedEdge>{
		
		final WeightedGraph<? extends Vertex, DefaultWeightedEdge> myGraph;
		
		public EdgeComparator(WeightedGraph<? extends Vertex, DefaultWeightedEdge> graph){
			myGraph = graph;
		}
		
		public int compare(DefaultWeightedEdge e1, DefaultWeightedEdge e2){
			double weight1 = myGraph.getEdgeWeight(e1);
			double weight2 = myGraph.getEdgeWeight(e2);
			
			if(weight1 > weight2)
				return 1;
			else if(weight1 < weight2)
				return -1;
			else
				return 0; 
		}
	}
	
	/**
	 * Data-structure used in the Kernighan-Lin heuristic. Stores pairs of vertices and a corresponding double value.
	 * In the Kernighan-Lin algorithm, the vertices are on opposite sides of a partition and the value is the gain in
	 * the quaity of min-cut we would acheive if their partitions were swapped.
	 * 
	 * @author Eric Crawford
	 */
	private static class VertexPairContainer <V extends Vertex>{
		private final double myVal;
		private final V myVertexA;
		private final V myVertexB;
		
		public VertexPairContainer(double val, V vertexA, V vertexB){
			myVal = val;
			myVertexA = vertexA;
			myVertexB = vertexB;
		}
		
		public double getVal() {
			return myVal;
		}

		public V getVertexA() {
			return myVertexA;
		}

		public V getVertexB() {
			return myVertexB;
		}
	}
	
}
