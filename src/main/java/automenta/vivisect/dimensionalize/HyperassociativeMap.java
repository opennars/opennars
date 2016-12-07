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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.jgrapht.Graph;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
 * 
 * 
 * TODO:
 *   parameter for distance function that takes arguments for max
 *      if distance exceeds max it will return POSITIVE_INFINITY
 *   parameter for max repulsion distance (cutoff)
 *      for use with above
 *   parameter for min attraction distance (cutoff)
 *   
 */
public class HyperassociativeMap<N, E> {

    private static final double REPULSIVE_WEAKNESS = 2.0;
    private static final double ATTRACTION_STRENGTH = 4.0;
    private static final double EQUILIBRIUM_ALIGNMENT_FACTOR = 0.005;
    private static final double LEARNING_RATE_INCREASE_FACTOR = 0.95;
    private static final double LEARNING_RATE_PROCESSING_ADJUSTMENT = 1.01;
    
    private static final double DEFAULT_LEARNING_RATE = 0.4;
    private static final double DEFAULT_MAX_MOVEMENT = 0.0;
    private static final double DEFAULT_TOTAL_MOVEMENT = 0.0;
    private static final double DEFAULT_ACCEPTABLE_DISTANCE_FACTOR = 0.85;
    private static final double DEFAULT_EQUILIBRIUM_DISTANCE = 1.0;

    /** when distance between nodes exceeds this factor times target distance, repulsion is not applied.  set to positive infinity to completely disable */
    double maxRepulsionDistance = 12.0;
    
    private Graph<N, E> graph;
    private final int dimensions;
    private final ExecutorService threadExecutor;
    
    private final Map<N, ArrayRealVector> coordinates;
    
    private static final Random RANDOM = new Random();
    private double equilibriumDistance;
    private double learningRate = DEFAULT_LEARNING_RATE;
    private double maxMovement = DEFAULT_MAX_MOVEMENT;
    private double totalMovement = DEFAULT_TOTAL_MOVEMENT;
    private double acceptableMaxDistanceFactor = DEFAULT_ACCEPTABLE_DISTANCE_FACTOR;
    private EdgeWeightToDistanceFunction edgeWeightToDistance = EdgeWeightToDistanceFunction.OneDivSum;
    
    private DistanceMetric distanceFunction;
    final double[] zero;
    
    public Collection<N> keys() {
        return coordinates.keySet();
    }

    protected ArrayRealVector newNodeCoordinates(N node) {
        ArrayRealVector location = randomCoordinates(dimensions);
        coordinates.put(node, location);
        return location;    
    }
    
    public ArrayRealVector getPosition(N node) {
        ArrayRealVector location = coordinates.get(node);    
        if (location == null) {
            location = newNodeCoordinates(node);
        }
        return location;
    }

    public void run(int i) {
        for ( ; i > 0; i--)
            align();
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

    public HyperassociativeMap(final Graph<N, E> graph, final int dimensions, final double equilibriumDistance, DistanceMetric distance, final ExecutorService threadExecutor) {
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
        this.distanceFunction = distance;
        this.zero = new double[dimensions];

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

    public HyperassociativeMap(final Graph<N, E> graph, final int dimensions, DistanceMetric distance, final ExecutorService threadExecutor) {
        this(graph, dimensions, DEFAULT_EQUILIBRIUM_DISTANCE, distance, threadExecutor);
    }

    public HyperassociativeMap(final Graph<N, E> graph, final int dimensions, final double equilibriumDistance, DistanceMetric distance) {
        this(graph, dimensions, equilibriumDistance, distance, null);
    }

    public HyperassociativeMap(final Graph<N, E> graph, DistanceMetric distance, final int dimensions) {
        this(graph, dimensions, DEFAULT_EQUILIBRIUM_DISTANCE, distance, null);
    }
    
    public HyperassociativeMap(final Graph<N, E> graph, final int dimensions) {
        this(graph, dimensions, DEFAULT_EQUILIBRIUM_DISTANCE, Euclidean, null);
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
        acceptableMaxDistanceFactor = DEFAULT_ACCEPTABLE_DISTANCE_FACTOR;
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

    /*
    //Should use the node-dependent equilibriumDistance that considers additional radius
    
    public boolean isAligned() {
        return isAlignable()
                && (maxMovement < (EQUILIBRIUM_ALIGNMENT_FACTOR * equilibriumDistance))
                && (maxMovement > DEFAULT_MAX_MOVEMENT);
    }
    */

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
    public static void add(ArrayRealVector target, ArrayRealVector add, double factor) {
        if (factor == 0) return;
        
        double[] a = add.getDataRef();
        double[] t = target.getDataRef();
        int dim = t.length;
        for (int i = 0; i < dim; i++) {
            t[i] += a[i] * factor;
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
            ArrayRealVector v = coordinates.get(node);
            if (v!=null)
                sub(v, center);
        }
    }



    /** added to equilibrium distance to get target alignment distance */
    public double getRadius(N n) {
        return 0;
    }
    
    /** speed scaling factor for a node; should be <= 1.0 */
    public double getSpeedFactor(N n) {
        return 1.0;
    }
    
    /** edge "weight" which can be mapped in certain ways (via EdgeWeightToDistanceFunction) to distance */
    public double getEdgeWeight(E e) {
        return 1.0;
    }
    
    public static enum EdgeWeightToDistanceFunction {
        Min, Max, Sum, SumOneDiv, OneDivSum, OneDivSumOneDiv
    }
    
    void getNeighbors(final N nodeToQuery, Map<N, Double> neighbors) {
        if (neighbors == null)
            neighbors = new HashMap<N, Double>();
        else
            neighbors.clear();
        
        for (E neighborEdge : graph.edgesOf(nodeToQuery)) {
            N s = graph.getEdgeSource(neighborEdge);
            N t = graph.getEdgeSource(neighborEdge);
            N neighbor = s == nodeToQuery ? t : s;

            Double existingWeight = neighbors.get(neighbor);
            
            double currentWeight = getEdgeWeight(neighborEdge);

            if (existingWeight!=null) {
                switch (edgeWeightToDistance) {
                    case Min:
                        currentWeight = Math.min(existingWeight, currentWeight);
                        break;
                    case Max:
                        currentWeight = Math.max(existingWeight, currentWeight);
                        break;
                    case SumOneDiv:
                    case OneDivSumOneDiv:
                        currentWeight = 1/currentWeight + existingWeight;
                        break;
                    case Sum:
                    case OneDivSum:                    
                        currentWeight += existingWeight;
                        break;
                        
                     
                }
            }
            
            neighbors.put(neighbor, currentWeight);
        }   
        
        switch (edgeWeightToDistance) {
            case OneDivSumOneDiv:
            case OneDivSum:
                for ( Entry<N, Double> e : neighbors.entrySet()) {
                    e.setValue( 1.0 / e.getValue() );
                }
                break;
        }

        
    }
    
    
    public double magnitude(ArrayRealVector x) {
        return distanceFunction.getDistance(zero, x.getDataRef());
    }

    private ArrayRealVector align(final N nodeToAlign, Map<N, Double> neighbors) {
        
        
        // calculate equilibrium with neighbors
        ArrayRealVector position = getPosition(nodeToAlign);

        double nodeSpeed = getSpeedFactor(nodeToAlign);
        
        if (nodeSpeed == 0) return position;
        
        getNeighbors(nodeToAlign, neighbors);

        ArrayRealVector delta = new ArrayRealVector(dimensions);

        double targetDistance = getRadius(nodeToAlign) + equilibriumDistance;
        
        // align with neighbours
        for (final Entry<N, Double> neighborEntry : neighbors.entrySet()) {
            
            final N neighbor = neighborEntry.getKey();
            
            final double distToNeighbor = neighborEntry.getValue();

            ArrayRealVector attractVector = getPosition(neighbor).subtract(position);
            
            double oldDistance = magnitude(attractVector);
            
            double newDistance;
            double factor = 0;
            if (oldDistance > distToNeighbor) {
                newDistance = Math.pow(oldDistance - distToNeighbor, ATTRACTION_STRENGTH);                
                
            } else {
                
                newDistance = -targetDistance * atanh((distToNeighbor - oldDistance) / distToNeighbor);
                
                if (Math.abs(newDistance) > (Math.abs(distToNeighbor - oldDistance))) {
                    newDistance = -targetDistance * (distToNeighbor - oldDistance);
                }
                
            }
            
            newDistance *= learningRate;
            if (oldDistance != 0) {
                factor = newDistance/oldDistance;
            }
            
            add(delta, attractVector, factor);
        }
        
        ArrayRealVector repelVector = new ArrayRealVector(dimensions);
        double maxEffectiveDistance = targetDistance * maxRepulsionDistance;
        
        // calculate repulsion with all non-neighbors
        for (final N node : graph.vertexSet()) {
            if (node == nodeToAlign) continue;
            if (neighbors.containsKey(node)) continue;

            
            
            double oldDistance = distanceFunction.subtractIfLessThan(getPosition(node), position, repelVector, maxEffectiveDistance);
            if (oldDistance == Double.POSITIVE_INFINITY)
                continue;
            
            double newDistance = -targetDistance / Math.pow(oldDistance, REPULSIVE_WEAKNESS);

            if (Math.abs(newDistance) > targetDistance) {
                newDistance = Math.copySign(targetDistance, newDistance);
            }                
            newDistance *= learningRate;

            add(delta, repelVector, newDistance/oldDistance);
        }

        
        
        if (nodeSpeed!=1.0) {
            delta.mapMultiplyToSelf(nodeSpeed);
        }
        
        double moveDistance = magnitude(delta);
        
        if (moveDistance > targetDistance * acceptableMaxDistanceFactor) {
            final double newLearningRate = ((targetDistance * acceptableMaxDistanceFactor) / moveDistance);
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
            add(position, delta);
        }

        if (moveDistance > maxMovement) {
            maxMovement = moveDistance;
        }
        totalMovement += moveDistance;

        return position;
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
        Map<N, Double> reusableNeighborData = new HashMap();
        
        for (final N node : graph.vertexSet()) {
            
            final ArrayRealVector newPosition = align(node, reusableNeighborData);

            add(pointSum, newPosition);
        }
        
        if ((learningRate * LEARNING_RATE_PROCESSING_ADJUSTMENT) < DEFAULT_LEARNING_RATE) {
            final double acceptableDistanceAdjustment = 0.1;
            if (getAverageMovement() < (equilibriumDistance * acceptableMaxDistanceFactor * acceptableDistanceAdjustment)) {
                acceptableMaxDistanceFactor *= LEARNING_RATE_INCREASE_FACTOR;
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
            if (getAverageMovement() < (equilibriumDistance * acceptableMaxDistanceFactor * acceptableDistanceAdjustment)) {
                acceptableMaxDistanceFactor = maxMovement * 2.0;
            }
            learningRate *= LEARNING_RATE_PROCESSING_ADJUSTMENT;
            //LOGGER.debug("learning rate: " + learningRate + ", acceptableDistanceFactor: " + acceptableDistanceFactor);
        }
        return pointSum;
    }
    
    
    /** distance metric with early termination condition when accumulated distance exceeds a max threshold (in which case it returns positive infinity) */
    public interface DistanceMetric {
        
        /** version which can be overridden to eliminate max distance test in inner loop */
        default public double getDistance(double[] a, double[] b) {
            return getDistance(a, b, Double.POSITIVE_INFINITY);
        }
        
        public double getDistance(double[] a, double[] b, double max);        

        public double subtractIfLessThan(ArrayRealVector a, ArrayRealVector b, ArrayRealVector result, double maxDistance);
    }
    
    public final static DistanceMetric Euclidean = new DistanceMetric() {

        @Override public double getDistance(double[] a, double[] b, double max) {
            double maxSquare = max*max;
            double d = 0;
            for (int i = 0; i < a.length; i++) {
                double ab = (a[i] - b[i]);                
                d += ab*ab;
                
                if (d > maxSquare)
                    return Double.POSITIVE_INFINITY;                
            }
            
            return Math.sqrt(d);            
        }
        @Override public double getDistance(double[] a, double[] b) {
            double d = 0;
            for (int i = 0; i < a.length; i++) {
                double ab = (a[i] - b[i]);                
                d += ab*ab;
            }
            
            return Math.sqrt(d);            
        }        

        @Override
        public double subtractIfLessThan(ArrayRealVector aa, ArrayRealVector bb, ArrayRealVector result, double maxDistance) {
            double a[] = aa.getDataRef();
            double b[] = bb.getDataRef();
            double r[] = result.getDataRef();
            double maxDistanceSq = maxDistance*maxDistance;
            double d = 0;
            for (int i = 0; i < a.length; i++) {
                double ab = a[i] - b[i];
                d += ab*ab;
             
                if (d > maxDistanceSq) return Double.POSITIVE_INFINITY;
                
                r[i] = ab;
            }
            return Math.sqrt(d);
        }
    };
    public final static DistanceMetric Manhattan = new DistanceMetric() {

        @Override public double getDistance(double[] a, double[] b, double max) {            
            double d = 0;
            for (int i = 0; i < a.length; i++) {
                double ab = (a[i] - b[i]);                
                if (ab < 0) ab = -ab; //abs
                d += ab;
                
                if (d > max)
                    return Double.POSITIVE_INFINITY;                
            }
            
            return d;
        }
        @Override public double getDistance(double[] a, double[] b) {                    double d = 0;
            for (int i = 0; i < a.length; i++) {                
                double ab = (a[i] - b[i]);
                if (ab < 0) ab = -ab; //abs
                d += ab;
            }            
            return d;
        }     
        
        @Override
        public double subtractIfLessThan(ArrayRealVector aa, ArrayRealVector bb, ArrayRealVector result, double maxDistance) {
            double a[] = aa.getDataRef();
            double b[] = bb.getDataRef();
            double r[] = result.getDataRef();
            double d = 0;
            for (int i = 0; i < a.length; i++) {
                double ab = a[i] - b[i];
                if (ab < 0) ab = -ab;
                d += ab;
             
                if (d > maxDistance) return Double.POSITIVE_INFINITY;
                
                r[i] = ab;
            }
            return Math.sqrt(d);
        }        
    };

    @Override
    public String toString() {
        return coordinates.toString();
    }
    
    
}
