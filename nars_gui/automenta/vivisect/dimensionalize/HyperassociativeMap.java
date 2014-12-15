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
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.log4j.Logger;
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
    private static final Logger LOGGER = Logger.getLogger(HyperassociativeMap.class);
    private Map<N, ArrayRealVector> coordinates = Collections.synchronizedMap(new HashMap<N, ArrayRealVector>());
    private static final Random RANDOM = new Random();
    private final boolean useWeights;
    private double equilibriumDistance;
    private double learningRate = DEFAULT_LEARNING_RATE;
    private double maxMovement = DEFAULT_MAX_MOVEMENT;
    private double totalMovement = DEFAULT_TOTAL_MOVEMENT;
    private double acceptableDistanceFactor = DEFAULT_ACCEPTABLE_DISTANCE_FACTOR;

    private class Align implements Callable<ArrayRealVector> {

        private final N node;

        public Align(final N node) {
            this.node = node;
        }

        @Override
        public ArrayRealVector call() {
            return align(node);
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
        this.equilibriumDistance = equilibriumDistance;
        this.useWeights = useWeights;

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
        this.equilibriumDistance = equilibriumDistance;
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
        for (final N node : coordinates.keySet()) {
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
                LOGGER.warn("waitAndProcessFutures was unexpectedly interrupted", caught);
                throw new RuntimeException("Unexpected interruption. Get should block indefinitely", caught);
            }
        }

        LOGGER.debug("maxMove: " + maxMovement + ", Average Move: " + getAverageMovement());

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

    public Map<N, ArrayRealVector> getCoordinates() {
        return coordinates;
    }

    private void recenterNodes(final ArrayRealVector center) {
        for (final N node : graph.vertexSet()) {
            //TODO subtract with modify
            coordinates.put(node, coordinates.get(node).subtract(center));
        }
    }

    public boolean isUsingWeights() {
        return useWeights;
    }

    Map<N, Double> getNeighbors(final N nodeToQuery) {
        final Map<N, Double> neighbors = new HashMap<N, Double>();
        for (E neighborEdge : graph.edgesOf(nodeToQuery)) {
            double currentWeight = graph.getEdgeWeight(neighborEdge);

            N s = graph.getEdgeSource(neighborEdge);
            N t = graph.getEdgeSource(neighborEdge);
            N neighbor = s == nodeToQuery ? t : s;

            neighbors.put(neighbor, currentWeight);
        }
        return neighbors;
    }

    private ArrayRealVector align(final N nodeToAlign) {
        // calculate equilibrium with neighbors
        final ArrayRealVector location = coordinates.get(nodeToAlign);
        final Map<N, Double> neighbors = getNeighbors(nodeToAlign);

        ArrayRealVector compositeArrayRealVector = new ArrayRealVector(location.getDimension());

        // align with neighbours
        for (final Entry<N, Double> neighborEntry : neighbors.entrySet()) {
            final N neighbor = neighborEntry.getKey();
            final double associationEquilibriumDistance = neighborEntry.getValue();

            RealVector neighborArrayRealVector = coordinates.get(neighbor).subtract(location);
            if (Math.abs(neighborArrayRealVector.getNorm()) > associationEquilibriumDistance) {
                double newDistance = Math.pow(Math.abs(neighborArrayRealVector.getNorm()) - associationEquilibriumDistance, ATTRACTION_STRENGTH);
                if (Math.abs(newDistance) > Math.abs(Math.abs(neighborArrayRealVector.getNorm()) - associationEquilibriumDistance)) {
                    newDistance = Math.copySign(Math.abs(Math.abs(neighborArrayRealVector.getNorm()) - associationEquilibriumDistance), newDistance);
                }
                newDistance *= learningRate;
                double oldDistance = neighborArrayRealVector.getNorm();
                neighborArrayRealVector = neighborArrayRealVector.mapMultiplyToSelf(newDistance/oldDistance);
            } else {
                double newDistance = -EQUILIBRIUM_DISTANCE * atanh((associationEquilibriumDistance - Math.abs(neighborArrayRealVector.getNorm())) / associationEquilibriumDistance);
                if (Math.abs(newDistance) > (Math.abs(associationEquilibriumDistance - Math.abs(neighborArrayRealVector.getNorm())))) {
                    newDistance = -EQUILIBRIUM_DISTANCE * (associationEquilibriumDistance - Math.abs(neighborArrayRealVector.getNorm()));
                }
                newDistance *= learningRate;
                
                double oldDistance = neighborArrayRealVector.getNorm();
                if (oldDistance != 0) {
                    neighborArrayRealVector = neighborArrayRealVector.mapMultiplyToSelf(newDistance / oldDistance);
                }
            }
            compositeArrayRealVector = compositeArrayRealVector.add(neighborArrayRealVector);
        }
        // calculate repulsion with all non-neighbors
        for (final N node : graph.vertexSet()) {
            if ((!neighbors.containsKey(node)) && (node != nodeToAlign)
                    && (!(graph.containsEdge(node, nodeToAlign) || graph.containsEdge(nodeToAlign, node)))) {
                RealVector nodeArrayRealVector = coordinates.get(node).subtract(location);
                double newDistance = -EQUILIBRIUM_DISTANCE / Math.pow(nodeArrayRealVector.getNorm(), REPULSIVE_WEAKNESS);
                if (Math.abs(newDistance) > Math.abs(equilibriumDistance)) {
                    newDistance = Math.copySign(equilibriumDistance, newDistance);
                }
                newDistance *= learningRate;
                nodeArrayRealVector = nodeArrayRealVector.unitVector().mapMultiplyToSelf(newDistance);
                compositeArrayRealVector = compositeArrayRealVector.add(nodeArrayRealVector);
            }
        }
        ArrayRealVector newLocation = location.add(compositeArrayRealVector);
        final ArrayRealVector oldLocation = coordinates.get(nodeToAlign);
        double moveDistance = Math.abs(newLocation.subtract(oldLocation).getNorm());
        if (moveDistance > equilibriumDistance * acceptableDistanceFactor) {
            final double newLearningRate = ((equilibriumDistance * acceptableDistanceFactor) / moveDistance);
            if (newLearningRate < learningRate) {
                learningRate = newLearningRate;
                LOGGER.debug("learning rate: " + learningRate);
            } else {
                learningRate *= LEARNING_RATE_INCREASE_FACTOR;
                LOGGER.debug("learning rate: " + learningRate);
            }

            newLocation = oldLocation;
            moveDistance = DEFAULT_TOTAL_MOVEMENT;
        }

        if (moveDistance > maxMovement) {
            maxMovement = moveDistance;
        }
        totalMovement += moveDistance;

        coordinates.put(nodeToAlign, newLocation);
        return newLocation;
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
        for (final N node : graph.vertexSet()) {
            final ArrayRealVector newPoint = align(node);

            //TODO use direct array
            pointSum = pointSum.add(newPoint);
        }
        if ((learningRate * LEARNING_RATE_PROCESSING_ADJUSTMENT) < DEFAULT_LEARNING_RATE) {
            final double acceptableDistanceAdjustment = 0.1;
            if (getAverageMovement() < (equilibriumDistance * acceptableDistanceFactor * acceptableDistanceAdjustment)) {
                acceptableDistanceFactor *= LEARNING_RATE_INCREASE_FACTOR;
            }
            learningRate *= LEARNING_RATE_PROCESSING_ADJUSTMENT;
            LOGGER.debug("learning rate: " + learningRate + ", acceptableDistanceFactor: " + acceptableDistanceFactor);
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
            LOGGER.error("Align had an unexpected problem executing.", caught);
            throw new RuntimeException("Unexpected execution exception. Get should block indefinitely", caught);
        }
        if (learningRate * LEARNING_RATE_PROCESSING_ADJUSTMENT < DEFAULT_LEARNING_RATE) {
            final double acceptableDistanceAdjustment = 0.1;
            if (getAverageMovement() < (equilibriumDistance * acceptableDistanceFactor * acceptableDistanceAdjustment)) {
                acceptableDistanceFactor = maxMovement * 2.0;
            }
            learningRate *= LEARNING_RATE_PROCESSING_ADJUSTMENT;
            LOGGER.debug("learning rate: " + learningRate + ", acceptableDistanceFactor: " + acceptableDistanceFactor);
        }
        return pointSum;
    }
}
