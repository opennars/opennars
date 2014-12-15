/**
 * ****************************************************************************
 *                                                                             *
 * Copyright: (c) Syncleus, Inc. * * You may redistribute and modify this source
 * code under the terms and * conditions of the Open Source Community License -
 * Type C version 1.0 * or any later version as published by Syncleus, Inc. at
 * www.syncleus.com. * There should be a copy of the license included with this
 * file. If a copy * of the license is not included you are granted no right to
 * distribute or * otherwise use this file except through a legal and valid
 * license. You * should also contact Syncleus, Inc. at the information below if
 * you cannot * find a license: * * Syncleus, Inc. * 2604 South 12th Street *
 * Philadelphia, PA 19148 * *
 *****************************************************************************
 */
package automenta.vivisect.dimensionalize;

import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.jgrapht.Graph;

/**
 * FROM:
 * http://gitlab.syncleus.com/syncleus/dANN-core/raw/v2.x/src/main/java/com/syncleus/dann/graph/drawing/hyperassociativemap/HyperassociativeMap.java
 *
 * A Hyperassociative Map is a new type of algorithm that organizes an arbitrary
 * graph of interconnected nodes according to its associations to other nodes.
 * Once a new Hyperassociative Map has been associated and aligned, nodes that
 * are most closely associated will be closest to each other. For more info,
 * please see the
 * <a href ="http://wiki.syncleus.com/index.php/dANN:Hyperassociative_Map">
 * Hyperassociative-Map dANN Wiki page</a>.
 *
 * @author Jeffrey Phillips Freeman
 * @param <G> The graph type
 * @param <N> The node type
 */
public class HyperassociativeMap<N, E> {

    private static final double REPULSIVE_WEAKNESS = 2.0;
    private static final double ATTRACTION_STRENGTH = 4.0;
    private static final double DEFAULT_LEARNING_RATE = 0.4;
    private static final double DEFAULT_MAX_MOVEMENT = 0.0;
    private static final double DEFAULT_TOTAL_MOVEMENT = 0.0;
    private static final double DEFAULT_ACCEPTABLE_DISTANCE_FACTOR = 0.75;
    private static final double EQUILIBRIUM_DISTANCE = 1.0;
    private static final double EQUILIBRIUM_ALIGNMENT_FACTOR = 0.005;
    private static final double LEARNING_RATE_INCREASE_FACTOR = 0.9;
    private static final double LEARNING_RATE_PROCESSING_ADJUSTMENT = 1.01;

    private Graph<N, E> graph;
    private final int dimensions;
    private final ExecutorService threadExecutor;
    
    private final Map<N, ArrayRealVector> coordinates;
    
    private static final Random RANDOM = new Random();
    private final boolean useWeights;
    private double equilibriumDistance;
    private double learningRate = DEFAULT_LEARNING_RATE;
    private double maxMovement = DEFAULT_MAX_MOVEMENT;
    private double totalMovement = DEFAULT_TOTAL_MOVEMENT;
    private double acceptableDistanceFactor = DEFAULT_ACCEPTABLE_DISTANCE_FACTOR;

    protected ArrayRealVector newNodeCoordinates(N node) {
        ArrayRealVector location = randomCoordinates(dimensions);
        coordinates.put(node, location);
        return location;    
    }
    
    public ArrayRealVector getCoordinates(N node) {
        ArrayRealVector location = coordinates.get(node);    
        if (location == null) {
            location = newNodeCoordinates(node);
        }
        return location;
    }

    private class Align implements Callable<ArrayRealVector> {

        private final N node;

        public Align(final N node) {
            this.node = node;
        }

        @Override
        public ArrayRealVector call() {
            return align(node, null);
        }
    }

    public HyperassociativeMap(final Graph<N, E> graph, final int dimensions, final double equilibriumDistance, final boolean useWeights, final ExecutorService threadExecutor) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph can not be null");
        }
        if (dimensions <= 0) {
            throw new IllegalArgumentException("dimensions must be 1 or more");
        }

        this.graph = graph;
        this.dimensions = dimensions;
        this.threadExecutor = threadExecutor;
        this.equilibriumDistance = Math.abs(equilibriumDistance);
        this.useWeights = useWeights;

        if (threadExecutor!=null) {
            coordinates = Collections.synchronizedMap(new WeakHashMap<N, ArrayRealVector>());
        }
        else {
            coordinates = new WeakHashMap<>();
        }
        
        // refresh all nodes
        for (final N node : this.graph.vertexSet()) {
            this.coordinates.put(node, randomCoordinates(this.dimensions));
        }
    }

    public HyperassociativeMap(final Graph<N, E> graph, final int dimensions, final ExecutorService threadExecutor) {
        this(graph, dimensions, EQUILIBRIUM_DISTANCE, true, threadExecutor);
    }

    public HyperassociativeMap(final Graph<N, E> graph, final int dimensions, final double equilibriumDistance, final boolean useWeights) {
        this(graph, dimensions, equilibriumDistance, useWeights, null);
    }

    public HyperassociativeMap(final Graph<N, E> graph, final int dimensions) {
        this(graph, dimensions, EQUILIBRIUM_DISTANCE, true, null);
    }

    public void setGraph(Graph<N, E> graph) {
        this.graph = graph;
    }

    
    
    public final Graph<N, E> getGraph() {
        return graph;
    }

    public double getEquilibriumDistance() {
        return equilibriumDistance;
    }

    public void setEquilibriumDistance(final double equilibriumDistance) {
        this.equilibriumDistance = Math.abs(equilibriumDistance);
    }

    public void resetLearning() {
        learningRate = DEFAULT_LEARNING_RATE;
        maxMovement = DEFAULT_TOTAL_MOVEMENT;
        totalMovement = DEFAULT_TOTAL_MOVEMENT;
        acceptableDistanceFactor = DEFAULT_ACCEPTABLE_DISTANCE_FACTOR;
    }

    public void reset() {
        resetLearning();
        
        // randomize all nodes
        coordinates.clear();
        for (final N node : graph.vertexSet()) {
            coordinates.put(node, randomCoordinates(dimensions));
        }
    }

    public boolean isAlignable() {
        return true;
    }

    public boolean isAligned() {
        return isAlignable()
                && (maxMovement < (EQUILIBRIUM_ALIGNMENT_FACTOR * equilibriumDistance))
                && (maxMovement > DEFAULT_MAX_MOVEMENT);
    }

    private double getAverageMovement() {
        return totalMovement / graph.vertexSet().size();

        //Topography.getOrder((Graph<N, ?>) graph);
    }

    public void align() {
        // refresh all nodes
        /*
        if (!coordinates.keySet().equals(graph.vertexSet())) {
            final Map<N, ArrayRealVector> newCoordinates = new HashMap<N, ArrayRealVector>();
            for (final N node : graph.vertexSet()) {
                if (coordinates.containsKey(node)) {
                    newCoordinates.put(node, coordinates.get(node));
                } else {
                    newCoordinates.put(node, randomCoordinates(dimensions));
                }
            }
            coordinates = Collections.synchronizedMap(newCoordinates);
        }
        */

        totalMovement = DEFAULT_TOTAL_MOVEMENT;
        maxMovement = DEFAULT_MAX_MOVEMENT;
        ArrayRealVector center;
        if (threadExecutor == null) {
            center = processLocally();
        } else {
            // align all nodes in parallel
            final List<Future<ArrayRealVector>> futures = submitFutureAligns();

			// wait for all nodes to finish aligning and calculate new sum of
            // all the points
            try {
                center = waitAndProcessFutures(futures);
            } catch (InterruptedException caught) {
                //LOGGER.warn("waitAndProcessFutures was unexpectedly interrupted", caught);
                throw new RuntimeException("Unexpected interruption. Get should block indefinitely", caught);
            }
        }

        //LOGGER.debug("maxMove: " + maxMovement + ", Average Move: " + getAverageMovement());

		// divide each coordinate of the sum of all the points by the number of
        // nodes in order to calculate the average point, or center of all the
        // points
        int numVertices = graph.vertexSet().size();
        center.mapDivideToSelf(numVertices);

        recenterNodes(center);
    }

    public int getDimensions() {
        return dimensions;
    }


    public static void add(ArrayRealVector target, ArrayRealVector add) {
        double[] a = add.getDataRef();
        double[] t = target.getDataRef();
        int dim = t.length;
        for (int i = 0; i < dim; i++) {
            t[i] += a[i];
        }
    }
    public static void sub(ArrayRealVector target, ArrayRealVector add) {
        double[] a = add.getDataRef();
        double[] t = target.getDataRef();
        int dim = t.length;
        for (int i = 0; i < dim; i++) {
            t[i] -= a[i];
        }
    }
    
    private void recenterNodes(final ArrayRealVector center) {
        for (final N node : graph.vertexSet()) {
            //TODO subtract with modify
            ArrayRealVector v = coordinates.get(node);
            if (v!=null)
                sub(v, center);
        }
    }

    public boolean isUsingWeights() {
        return useWeights;
    }

    void getNeighbors(final N nodeToQuery, Map<N, Double> neighbors) {
        if (neighbors == null)
            neighbors = new HashMap<N, Double>();
        else
            neighbors.clear();
        
        for (E neighborEdge : graph.edgesOf(nodeToQuery)) {
            double currentWeight = graph.getEdgeWeight(neighborEdge);

            N s = graph.getEdgeSource(neighborEdge);
            N t = graph.getEdgeSource(neighborEdge);
            N neighbor = s == nodeToQuery ? t : s;

            neighbors.put(neighbor, currentWeight);
        }                
    }

    private ArrayRealVector align(final N nodeToAlign, Map<N, Double> neighbors) {
        // calculate equilibrium with neighbors
        ArrayRealVector location = getCoordinates(nodeToAlign);
        
        getNeighbors(nodeToAlign, neighbors);

        ArrayRealVector compositeArrayRealVector = new ArrayRealVector(dimensions);

        // align with neighbours
        for (final Entry<N, Double> neighborEntry : neighbors.entrySet()) {
            final N neighbor = neighborEntry.getKey();
            final double distToNeighbor = neighborEntry.getValue();

            ArrayRealVector nv = coordinates.get(neighbor);
            if (nv == null)
                continue;
            
            nv = nv.subtract(location);
            
            
            double nvnorm = nv.getNorm();
            if (nvnorm > distToNeighbor) {
                double newDistance = Math.pow(nvnorm - distToNeighbor, ATTRACTION_STRENGTH);
                if (Math.abs(newDistance) > Math.abs(nvnorm - distToNeighbor)) {
                    newDistance = Math.copySign(Math.abs(nvnorm - distToNeighbor), newDistance);
                }
                
                newDistance *= learningRate;
                if (nvnorm!=0) {
                    nv.mapMultiplyToSelf(newDistance/nvnorm);
                }
                
            } else {
                
                double newDistance = -EQUILIBRIUM_DISTANCE * atanh((distToNeighbor - nvnorm) / distToNeighbor);
                if (Math.abs(newDistance) > (Math.abs(distToNeighbor - nvnorm))) {
                    newDistance = -EQUILIBRIUM_DISTANCE * (distToNeighbor - nvnorm);
                }
                
                newDistance *= learningRate;
                
                
                if (nvnorm != 0) {
                    nv.mapMultiplyToSelf(newDistance / nvnorm);
                }
            }
            add(compositeArrayRealVector, nv);
        }
        
        // calculate repulsion with all non-neighbors
        for (final N node : graph.vertexSet()) {
            if ((!neighbors.containsKey(node)) && (node != nodeToAlign)
                    && (!(graph.containsEdge(node, nodeToAlign) || graph.containsEdge(nodeToAlign, node)))) {
                
                ArrayRealVector nl = getCoordinates(node);
                
                ArrayRealVector nodeArrayRealVector = nl.subtract(location);
                double oldDistance = nodeArrayRealVector.getNorm();
                
                double newDistance = -EQUILIBRIUM_DISTANCE / Math.pow(oldDistance, REPULSIVE_WEAKNESS);
                if (Math.abs(newDistance) > equilibriumDistance) {
                    newDistance = Math.copySign(equilibriumDistance, newDistance);
                }
                newDistance *= learningRate;
                
                nodeArrayRealVector.mapMultiplyToSelf(newDistance/oldDistance);
                add(compositeArrayRealVector, nodeArrayRealVector);
            }
        }

        double moveDistance = compositeArrayRealVector.getNorm();
        //newLocation.getDistance(oldLocation);
        
        if (moveDistance > equilibriumDistance * acceptableDistanceFactor) {
            final double newLearningRate = ((equilibriumDistance * acceptableDistanceFactor) / moveDistance);
            if (newLearningRate < learningRate) {
                learningRate = newLearningRate;
                //LOGGER.debug("learning rate: " + learningRate);
            } else {
                learningRate *= LEARNING_RATE_INCREASE_FACTOR;
                //LOGGER.debug("learning rate: " + learningRate);
            }
            
            moveDistance = DEFAULT_TOTAL_MOVEMENT;
        }
        else {
            add(location, compositeArrayRealVector);
        }

        if (moveDistance > maxMovement) {
            maxMovement = moveDistance;
        }
        totalMovement += moveDistance;

        return location;
    }

    /**
     * Obtains a ArrayRealVector with RANDOM coordinates for the specified
     * number of dimensions. The coordinates will be in range [-1.0, 1.0].
     *
     * @param dimensions Number of dimensions for the RANDOM ArrayRealVector
     * @return New RANDOM ArrayRealVector
     * @since 1.0
     */
    public static ArrayRealVector randomCoordinates(final int dimensions) {
        final double[] randomCoordinates = new double[dimensions];
        for (int randomCoordinatesIndex = 0; randomCoordinatesIndex < dimensions; randomCoordinatesIndex++) {
            randomCoordinates[randomCoordinatesIndex] = (RANDOM.nextDouble() * 2.0) - 1.0;
        }

        return new ArrayRealVector(randomCoordinates);
    }

    /**
     * Returns the inverse hyperbolic tangent of a value. You may see
     * <a href="http://www.mathworks.com/help/techdoc/ref/atanh.html">
     * MathWorks atanh page</a> for more info.
     *
     * @param value the input.
     * @return the inverse hyperbolic tangent of value.
     */
    private static double atanh(final double value) {
        return Math.log(Math.abs((value + 1.0) / (1.0 - value))) / 2;
    }

    private List<Future<ArrayRealVector>> submitFutureAligns() {
        final ArrayList<Future<ArrayRealVector>> futures = new ArrayList<Future<ArrayRealVector>>();
        for (final N node : graph.vertexSet()) {
            futures.add(threadExecutor.submit(new Align(node)));
        }
        return futures;
    }

    private ArrayRealVector processLocally() {
        ArrayRealVector pointSum = new ArrayRealVector(dimensions);
        Map<N, Double> n = new HashMap();
        
        for (final N node : graph.vertexSet()) {
            
            final ArrayRealVector newPoint = align(node, n);

            //TODO use direct array
            add(pointSum, newPoint);
        }
        
        if ((learningRate * LEARNING_RATE_PROCESSING_ADJUSTMENT) < DEFAULT_LEARNING_RATE) {
            final double acceptableDistanceAdjustment = 0.1;
            if (getAverageMovement() < (equilibriumDistance * acceptableDistanceFactor * acceptableDistanceAdjustment)) {
                acceptableDistanceFactor *= LEARNING_RATE_INCREASE_FACTOR;
            }
            learningRate *= LEARNING_RATE_PROCESSING_ADJUSTMENT;
            //LOGGER.debug("learning rate: " + learningRate + ", acceptableDistanceFactor: " + acceptableDistanceFactor);
        }
        return pointSum;
    }

    private ArrayRealVector waitAndProcessFutures(final List<Future<ArrayRealVector>> futures) throws InterruptedException {
        // wait for all nodes to finish aligning and calculate the new center point
        ArrayRealVector pointSum = new ArrayRealVector(dimensions);
        try {
            for (final Future<ArrayRealVector> future : futures) {
                final ArrayRealVector newPoint = future.get();
                //TODO use direct array
                pointSum = pointSum.add(newPoint);

            }
        } catch (ExecutionException caught) {
            //LOGGER.error("Align had an unexpected problem executing.", caught);
            throw new RuntimeException("Unexpected execution exception. Get should block indefinitely", caught);
        }
        if (learningRate * LEARNING_RATE_PROCESSING_ADJUSTMENT < DEFAULT_LEARNING_RATE) {
            final double acceptableDistanceAdjustment = 0.1;
            if (getAverageMovement() < (equilibriumDistance * acceptableDistanceFactor * acceptableDistanceAdjustment)) {
                acceptableDistanceFactor = maxMovement * 2.0;
            }
            learningRate *= LEARNING_RATE_PROCESSING_ADJUSTMENT;
            //LOGGER.debug("learning rate: " + learningRate + ", acceptableDistanceFactor: " + acceptableDistanceFactor);
        }
        return pointSum;
    }
}
