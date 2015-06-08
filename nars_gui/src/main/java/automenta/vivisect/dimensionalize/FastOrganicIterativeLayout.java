package automenta.vivisect.dimensionalize;

import nars.gui.output.graph.nengo.UIEdge;
import nars.gui.output.graph.nengo.UIVertex;
import com.google.common.collect.Iterators;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.util.data.random.XORShiftRandom;

import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * Fast organic layout algorithm, adapted from JGraph
 */
abstract public class FastOrganicIterativeLayout<N extends UIVertex, E extends UIEdge> implements IterativeLayout<N, E> {

    public static final double DisplacementLengthEpsilon = 0.0001; //minimum distinguishable length
    transient final List<Object> cells = new ArrayList();
    private final Random rng = new XORShiftRandom();
    private final Rectangle2D bounds;
    /**
     * Specifies if the top left corner of the input cells should be the origin
     * of the layout result. Default is true.
     */
    protected boolean useInputOrigin = true;
    /**
     * Specifies if all edge points of traversed edge should be removed.
     * Default is true.
     */
    protected boolean resetEdges = true;
    /**
     * Specifies if the STYLE_NOEDGESTYLE flag should be set on edge that are
     * modified by the result. Default is true.
     */
    protected boolean disableEdgeStyle = true;
    /**
     * The force constant by which the attractive forces are divided and the
     * replusive forces are multiple by the square of. The value equates to the
     * average radius there is of free space around each node. Default is 50.
     */
    protected double forceConstant = 50;
    /**
     * Cache of <forceConstant>^2 for performance.
     */
    protected double forceConstantSquared = 0;
    /**
     * Minimal distance limit. Default is 2. Prevents of dividing by zero.
     */
    protected double minDistanceLimit = 2;
    /**
     * Cached version of <minDistanceLimit> squared.
     */
    protected double minDistanceLimitSquared = 0;
    /**
     * The maximum distance between vertex, beyond which their repulsion no
     * longer has an effect
     */
    protected double maxDistanceLimit;
    /**
     * Start value of temperature. Default is 200.
     */
    protected double initialTemp;
    /**
     * An array of all vertex to be laid out.
     */
    protected List<N> vertices = new ArrayList();
    /**
     * An array of locally stored X co-ordinate displacements for the vertex.
     */
    protected double[] dispX;
    /**
     * An array of locally stored Y co-ordinate displacements for the vertex.
     */
    protected double[] dispY;
    /**
     * An array of locally stored co-ordinate positions for the vertex.
     */
    protected double[][] cellLocation;
    /**
     * The approximate radius of each cell, nodes only.
     */
    protected double[] radius;
    /**
     * The approximate radius squared of each cell, nodes only.
     */
    protected double[] radiusSquared;
    /**
     * Array of booleans representing the movable states of the vertex.
     */
    protected boolean[] isMoveable;
    /**
     * Local copy of cell neighbours.
     */
    protected int[][] neighbors;
    /**
     * Maps from vertex to indices.
     */

    protected ObjectIntHashMap<N> indices = new ObjectIntHashMap<>();
    double temperature;
    double temperatureDecay;
    double minTemperature;
    private List<Double> edgeWeights = new ArrayList();
    private double[][] attractStrength;


    public FastOrganicIterativeLayout(Rectangle2D bounds) {
        setInitialTemp(200, 1.0f);
        setMinDistanceLimit(0.01f);
        setMaxDistanceLimit(9550f);

        setForceConstant(150);

        this.bounds = bounds;

        resetLearning();

    }

    @Override
    public void resetLearning() {
        temperature = initialTemp;
    }

    @Override
    public double getRadius(N n) {
        return 0.5;
    }

    /**
     * Returns a boolean indicating if the given <mxCell> should be ignored as a
     * vertex. This returns true if the cell has no connections.
     *
     * @param vertex Object that represents the vertex to be tested.
     * @return Returns true if the vertex should be ignored.
     */
    public boolean isVertexIgnored(N vertex) {
        return false;
        //return super.isVertexIgnored(vertex)
        //	graph.getConnections(vertex).length == 0;
    }

    /**
     *
     */
    public boolean isUseInputOrigin() {
        return useInputOrigin;
    }

    /**
     * @param value
     */
    public void setUseInputOrigin(boolean value) {
        useInputOrigin = value;
    }

    /**
     *
     */
    public double getForceConstant() {
        return forceConstant;
    }

    /**
     * @param value
     */
    public void setForceConstant(double value) {
        forceConstant = value;
    }

    /**
     *
     */
    public double getMinDistanceLimit() {
        return minDistanceLimit;
    }

    /**
     * @param value
     */
    public void setMinDistanceLimit(double value) {
        minDistanceLimit = value;
    }

    /**
     * @return the maxDistanceLimit
     */
    public double getMaxDistanceLimit() {
        return maxDistanceLimit;
    }

    /**
     * @param maxDistanceLimit the maxDistanceLimit to set
     */
    public void setMaxDistanceLimit(double maxDistanceLimit) {
        this.maxDistanceLimit = maxDistanceLimit;
    }

    /**
     *
     */
    public double getInitialTemp() {
        return initialTemp;
    }

    /**
     * @param value
     */
    public void setInitialTemp(double value, double decay) {
        initialTemp = value;
        temperatureDecay = decay;
        minTemperature = value / 100;
    }

    public void run(int iterations) {

        if (temperature < minTemperature) return;

        indices.clear();


        // Finds the relevant vertex for the layout
        vertices.clear();

        Iterators.addAll(vertices, getVertices());

        if (vertices.isEmpty()) return;


        pre(vertices);


        //null disables offset adjustment at the end

        //? graph.getBoundsForCells(vertexArray, false, false, true) : null;


        int n = vertices.size();


        if ((cellLocation == null) || (cellLocation.length < n)) {
            dispX = new double[n];
            dispY = new double[n];
            cellLocation = new double[n][];
            isMoveable = new boolean[n];
            neighbors = new int[n][];
            attractStrength = new double[n][];
            radius = new double[n];
            radiusSquared = new double[n];
        }

        minDistanceLimitSquared = minDistanceLimit * minDistanceLimit;

        if (forceConstant < 0.001) {
            forceConstant = 0.001;
        }

        forceConstantSquared = forceConstant * forceConstant;

        // Create a map of vertex first. This is required for the array of
        // arrays called neighbours which holds, for each vertex, a list of
        // ints which represents the neighbours cells to that vertex as
        // the indices into vertexArray

        final double cellLocation[][] = this.cellLocation;
        final int[][] neighbors = this.neighbors;
        final List<Object> cells = this.cells;
        final List<Double> edgeWeights = this.edgeWeights;
        final double[] radii = this.radius;


        for (int i = 0; i < n; i++) {
            N v = vertices.get(i);

            if (cellLocation[i] == null)
                cellLocation[i] = new double[2];

            // Set up the mapping from array indices to cells
            indices.put(v, i);
            //mxRectangle bounds = getVertexBounds(vertex);

            // Set the X,Y value of the internal version of the cell to
            // the center point of the vertex for better positioning
            double radius = getRadius(v);
            double width = radius * 2f; //bounds.getWidth();
            double height = radius * 2f; //bounds.getHeight();

            // Randomize (0, 0) locations
            //TODO re-use existing location
            double[] c = getPosition(v).getDataRef();
            double x = c[0], y = c[1];


            cellLocation[i][0] = x;// + width / 2.0;
            cellLocation[i][1] = y;// + height / 2.0;

            radii[i] = radius;
            this.radiusSquared[i] = radius * radius;


            // Moves cell location back to top-left from center locations used in
            // algorithm, resetting the edge points is part of the transaction


            dispX[i] = 0;
            dispY[i] = 0;
            isMoveable[i] = true; //isVertexMovable(vertexArray[i]);
            // Get lists of neighbours to all vertex, translate the cells
            // obtained in indices into vertexArray and store as an array
            // against the original cell index
            //V v = vertexArray.get(i).getVertex();
            //ProcessingGraphCanvas.VertexVis vd = displayed.get(v);


            //TODO why does a vertex disappear from the graph... make this unnecessary


            cells.clear();
            edgeWeights.clear();
            for (Object oo : v.getEdgesIn())
                updateEdge(cells, edgeWeights, v, (UIEdge) oo);
            /*for (Object oo : v.getEdgesOut())
                updateEdge(cells, edgeWeights, v, (UIEdge) oo);*/


            if (cells.isEmpty() && neighbors[i] != null) {
                Arrays.fill(neighbors[i], -1);
                continue;
            }

            if (neighbors[i] == null || neighbors[i].length < cells.size()) {
                neighbors[i] = new int[cells.size()];
                attractStrength[i] = new double[cells.size()];
            }

            for (int j = 0; j < cells.size(); j++) {
                Object cj = cells.get(j);
                double w = edgeWeights.get(j);

                if (indices.containsKey(cj)) {
                    int index = indices.get(cj);

                    // Check the connected cell in part of the vertex list to be
                    // acted on by this layout

                    neighbors[i][j] = index;
                    attractStrength[i][j] = w;
                }
                // Else if index of the other cell doesn't correspond to
                // any cell listed to be acted upon in this layout. Set
                // the index to the value of this vertex (a dummy self-loop)
                // so the attraction force of the edge is not calculated
                else {
                    neighbors[i][j] = i;
                }
            }

            //fill up the remaining indexes with -1 in case it was shrunk and not unallocated
            for (int j = cells.size(); j < neighbors[i].length; j++)
                neighbors[i][j] = -1;


        }


        // Main iteration loop
        //try {
        for (int iteration = 0; iteration < iterations; iteration++) {

            // Calculate repulsive forces on all vertex
            calcRepulsion();

            // Calculate attractive forces through edge
            calcAttraction();

            temperature = temperature * temperatureDecay;
            calcPositions(temperature);
        }

        //} catch (Exception e) { }

        double minx = 0, miny = 0, maxx = 0, maxy = 0;

        for (int i = 0; i < vertices.size(); i++) {
            N v = vertices.get(i);

            if (v != null) {
                //cellLocation[i][0] -= 1/2.0; //geo.getWidth() / 2.0;
                //cellLocation[i][1] -= 1/2.0; //geo.getHeight() / 2.0;

                double r = radii[i];
                double x = /*graph.snap*/(cellLocation[i][0]);
                double y = /*graph.snap*/(cellLocation[i][1]);

                double[] pos = getPosition(v).getDataRef();
                pos[0] = x;
                pos[1] = y;

                if (i == 0) {
                    minx = maxx = x;
                    miny = maxy = y;
                } else {
                    if (x < minx) minx = x;
                    if (y < miny) miny = y;
                    if (x > maxx) maxx = x;
                    if (y > maxy) maxy = y;
                }

            }
        }


        // Modifies the cloned geometries in-place. Not needed
        // to clone the geometries again as we're in the same
        // undoable change.
        double dx = -(maxx + minx) / 2f;
        double dy = -(maxy + miny) / 2f;

        //normalize to bounds

        double wx, wy;


        wx = (maxx - minx);
        if (wx == 0) wx = 1;
        else wx = bounds.getWidth() / wx;
        wy = (maxy - miny);
        if (wy == 0) wy = 1;
        else wy = bounds.getHeight() / wy;
        dx += bounds.getCenterX();
        dy += bounds.getCenterY();


        for (final N x : vertices) {
            double[] p = getPosition(x).getDataRef();
            p[0] = p[0] * wx + dx;
            p[1] = p[1] * wy + dy;
        }

    }

    private void updateEdge(List<Object> cells, List<Double> weights, N v, UIEdge e) {
        Object source = e.getSource();
        Object target = e.getTarget();
        if (source != v) {
            cells.add(source);
            weights.add(getEdgeWeight((E) e));
        }
        else if (target != v) {
            cells.add(target);
        }
    }


    protected abstract Iterator<? extends N> getVertices();


    /**
     * Takes the displacements calculated for each cell and applies them to the
     * local cache of cell positions. Limits the displacement to the current
     * temperature.
     */
    protected void calcPositions(double temperature) {

        final double[] dispX = this.dispX;
        final double[] dispY = this.dispY;
        int size = vertices.size();

        final double[][] cellLocation = this.cellLocation;


        for (int index = 0; index < size; index++) {
            if (isMoveable[index]) {
                // Get the distance of displacement for this node for this
                // iteration
                double deltaLength = Math.sqrt(dispX[index] * dispX[index]
                        + dispY[index] * dispY[index]);

                if (deltaLength < DisplacementLengthEpsilon) {
                    deltaLength = DisplacementLengthEpsilon;
                }

                // Scale down by the current temperature if less than the
                // displacement distance
                double minDLT = Math.min(deltaLength, temperature);
                double newXDisp = dispX[index] / deltaLength * minDLT;
                double newYDisp = dispY[index] / deltaLength * minDLT;

                // Update the cached cell locations
                cellLocation[index][0] += newXDisp;
                cellLocation[index][1] += newYDisp;

                // reset displacements
                dispX[index] = 0;
                dispY[index] = 0;


            }
        }
    }

    /**
     * Calculates the attractive forces between all laid out nodes linked by
     * edge
     */
    protected void calcAttraction() {
        // Check the neighbours of each vertex and calculate the attractive
        // force of the edge connecting them
        final int[][] neighbors = this.neighbors;
        final double[][] attractStrength = this.attractStrength;
        final double[][] cellLocation = this.cellLocation;

        final double minDist = minDistanceLimit; //cache as local variable for speed
        final double fc = forceConstant;

        final double dispX[] = this.dispX;
        final double dispY[] = this.dispY;
        final double radius[] = this.radius;
        final boolean[] isMoveable = this.isMoveable;

        for (int i = 0; i < vertices.size(); i++) {
            if (neighbors[i] == null) continue;
            if (cellLocation[i] == null) continue;
            for (int k = 0; k < neighbors[i].length; k++) {
                // Get the index of the othe cell in the vertex array
                int j = neighbors[i][k];
                if (j == -1) break; //empty neighbor index at the end of the shrunk array

                if (cellLocation[j] == null) continue;

                // Do not proceed self-loops
                if (i != j) {
                    double rsum = radius[i] + radius[j];
                    double xDelta = cellLocation[i][0] - cellLocation[j][0];
                    double yDelta = cellLocation[i][1] - cellLocation[j][1];

                    // The distance between the nodes
                    double deltaLength = Math.sqrt(xDelta * xDelta + yDelta
                            * yDelta);

                    //deltaLength -= rsum;

                    if (deltaLength < minDist) {
                        deltaLength = minDist;
                    }

                    double w = attractStrength[i][k];

                    double force = (deltaLength) / (fc) * w;

                    double displacementX = xDelta * force;
                    double displacementY = yDelta * force;

                    if (isMoveable[i]) {
                        dispX[i] -= displacementX;
                        dispY[i] -= displacementY;
                    }

                    if (isMoveable[j]) {
                        dispX[j] += displacementX;
                        dispY[j] += displacementY;
                    }
                }
            }
        }
    }

    /**
     * Calculates the repulsive forces between all laid out nodes
     */
    protected void calcRepulsion() {
        final int vertexCount = vertices.size();

        final double maxDist = maxDistanceLimit; //cache as local variable for speed
        final double minDist = minDistanceLimit; //cache as local variable for speed
        final double fcSq = forceConstantSquared;

        final double dispX[] = this.dispX;
        final double dispY[] = this.dispY;
        final double cellLocation[][] = this.cellLocation;
        final double radius[] = this.radius;

        for (int i = 0; i < vertexCount; i++) {
            final double ri = radius[i];
            if (cellLocation[i] == null) continue;

            final double ix = cellLocation[i][0];
            final double iy = cellLocation[i][1];
            final boolean imi = isMoveable[i];

            for (int j = i; j < vertexCount; j++) {
                // Exits if the layout is no longer allowed to run

                if ((j != i) && (cellLocation[j] != null)) {
                    double xDelta = ix - cellLocation[j][0];
                    double yDelta = iy - cellLocation[j][1];

                    //final double rj = radius[j];
                    //double rij = ri + rj; //total radii

                    if (xDelta == 0) {
                        xDelta = 0.01 + (-0.5f + 0.5f * rng.nextDouble());
                    }

                    //if (xDelta - rij > maxDist) continue; // early exit condition

                    if (yDelta == 0) {
                        yDelta = 0.01 + (-0.5f + 0.5f * rng.nextDouble());
                    }

                    //if (yDelta - rij > maxDist) continue; // early exit condition

                    // Distance between nodes
                    double deltaLength = Math.sqrt((xDelta * xDelta)
                            + (yDelta * yDelta));

                    double deltaLengthWithRadius = deltaLength;// - rij;

                    if (deltaLengthWithRadius > maxDist) {
                        // Ignore vertex too far apart
                        continue;
                    }

                    if (deltaLengthWithRadius < minDist) {
                        deltaLengthWithRadius = minDist;
                    }

                    double force = fcSq / deltaLengthWithRadius;

                    force /= deltaLength;

                    double displacementX = xDelta * force;
                    double displacementY = yDelta * force;

                    //TODO double displacement if only one is not moveable

                    if (imi) {
                        dispX[i] += displacementX;
                        dispY[i] += displacementY;
                    }

                    if (isMoveable[j]) {
                        dispX[j] -= displacementX;
                        dispY[j] -= displacementY;
                    }
                }
            }
        }
    }


}
