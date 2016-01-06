//package automenta.vivisect.dimensionalize;
//
//
//import nars.util.data.map.CuckooMap;
//import org.jgrapht.Graph;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
///**
// * Created by me on 2/25/15.
// */
//abstract public class AbstractFastOrganicLayout<V, E, D> {
//
//
//    /**
//     * Specifies if the top left corner of the input cells should be the origin
//     * of the layout result. Default is true.
//     */
//    protected boolean useInputOrigin = true;
//    /**
//     * Specifies if all edge points of traversed edge should be removed.
//     * Default is true.
//     */
//    protected boolean resetEdges = true;
//    /**
//     * Specifies if the STYLE_NOEDGESTYLE flag should be set on edge that are
// modified by the result. Default is true.
//     */
//    protected boolean disableEdgeStyle = true;
//    /**
//     * The force constant by which the attractive forces are divided and the
//     * replusive forces are multiple by the square of. The value equates to the
//     * average radius there is of free space around each node. Default is 50.
//     */
//    protected double forceConstant = 50;
//    /**
//     * Cache of <forceConstant>^2 for performance.
//     */
//    protected double forceConstantSquared = 0;
//    /**
//     * Minimal distance limit. Default is 2. Prevents of dividing by zero.
//     */
//    protected double minDistanceLimit = 2;
//    /**
//     * Cached version of <minDistanceLimit> squared.
//     */
//    protected double minDistanceLimitSquared = 0;
//    /**
//     * The maximum distance between vertex, beyond which their repulsion no
// longer has an effect
//     */
//    protected double maxDistanceLimit = 500;
//    /**
//     * Start value of temperature. Default is 200.
//     */
//    protected double initialTemp = 200;
//    /**
//     * Temperature to limit displacement at later stages of layout.
//     */
//    protected double temperature = 0;
//    /**
//     * Total number of iterations to run the layout though.
//     */
//    protected double iterationsRemain = 0;
//    /**
//     * Current iteration count.
//     */
//    protected double iteration = 0;
//    /**
//     * An array of all vertex to be laid out.
//     */
//    protected List<D> vertexArray;
//    /**
//     * An array of locally stored X co-ordinate displacements for the vertex.
//     */
//    protected double[] dispX;
//    /**
//     * An array of locally stored Y co-ordinate displacements for the vertex.
//     */
//    protected double[] dispY;
//    /**
//     * An array of locally stored co-ordinate positions for the vertex.
//     */
//    protected double[][] cellLocation;
//    /**
//     * The approximate radius of each cell, nodes only.
//     */
//    protected double[] radius;
//    /**
//     * The approximate radius squared of each cell, nodes only.
//     */
//    protected double[] radiusSquared;
//    /**
//     * Array of booleans representing the movable states of the vertex.
//     */
//    protected boolean[] isMoveable;
//    /**
//     * Local copy of cell neighbours.
//     */
//    protected int[][] neighbors;
//    /**
//     * Boolean flag that specifies if the layout is allowed to run. If this is
//     * set to false, then the layout exits in the following iteration.
//     */
//    protected boolean allowedToRun = true;
//    /**
//     * Maps from vertex to indices.
//     */
//    protected Map<V, Integer> indices;
//
//    public AbstractFastOrganicLayout() {
//        setInitialTemp(13f);
//        setMinDistanceLimit(1f);
//        setMaxDistanceLimit(200f);
//
//        setForceConstant(100f);
//        setIterationsRemain(1);
//    }
//
//    /**
//     * Returns a boolean indicating if the given <mxCell> should be ignored as a
//     * vertex. This returns true if the cell has no connections.
//     *
//     * @param vertex Object that represents the vertex to be tested.
//     * @return Returns true if the vertex should be ignored.
//     */
//    public boolean isVertexIgnored(V vertex) {
//        return false;
//		//return super.isVertexIgnored(vertex)
//        //	graph.getConnections(vertex).length == 0;
//    }
//
//    /**
//     *
//     */
//    public boolean isUseInputOrigin() {
//        return useInputOrigin;
//    }
//
//    /**
//     *
//     * @param value
//     */
//    public void setUseInputOrigin(boolean value) {
//        useInputOrigin = value;
//    }
//
//    /**
//     *
//     */
//    public boolean isResetEdges() {
//        return resetEdges;
//    }
//
//    /**
//     *
//     * @param value
//     */
//    public void setResetEdges(boolean value) {
//        resetEdges = value;
//    }
//
//    /**
//     *
//     */
//    public boolean isDisableEdgeStyle() {
//        return disableEdgeStyle;
//    }
//
//    /**
//     *
//     * @param value
//     */
//    public void setDisableEdgeStyle(boolean value) {
//        disableEdgeStyle = value;
//    }
//
//    /**
//     *
//     */
//    public double getIterationsRemain() {
//        return iterationsRemain;
//    }
//
//    /**
//     *
//     * @param value
//     */
//    public void setIterationsRemain(double value) {
//        iterationsRemain = value;
//    }
//
//    /**
//     *
//     */
//    public double getForceConstant() {
//        return forceConstant;
//    }
//
//    /**
//     *
//     * @param value
//     */
//    public void setForceConstant(double value) {
//        forceConstant = value;
//    }
//
//    /**
//     *
//     */
//    public double getMinDistanceLimit() {
//        return minDistanceLimit;
//    }
//
//    /**
//     *
//     * @param value
//     */
//    public void setMinDistanceLimit(double value) {
//        minDistanceLimit = value;
//    }
//
//    /**
//     * @return the maxDistanceLimit
//     */
//    public double getMaxDistanceLimit() {
//        return maxDistanceLimit;
//    }
//
//    /**
//     * @param maxDistanceLimit the maxDistanceLimit to set
//     */
//    public void setMaxDistanceLimit(double maxDistanceLimit) {
//        this.maxDistanceLimit = maxDistanceLimit;
//    }
//
//    /**
//     *
//     */
//    public double getInitialTemp() {
//        return initialTemp;
//    }
//
//    /**
//     *
//     * @param value
//     */
//    public void setInitialTemp(double value) {
//        initialTemp = value;
//    }
//
//    /**
//     * Reduces the temperature of the layout from an initial setting in a linear
//     * fashion to zero.
//     */
///*    protected void reduceTemperature() {
//        temperature = initialTemp * (1.0 - iteration / iterationsRemain);
//    }*/
//
//    abstract public D getDisplay(Graph<V,E> graph, V vertex);
//
//    public boolean update(Graph<V,E> graph) {
//        // If max number of iterations is reached
//        if (iterationsRemain == 0) {
//            //maxIterations = 20.0 * Math.sqrt(n);
//            return false;
//        }
//
//        if (indices == null)
//            indices = new CuckooMap<>();
//        else
//            indices.clear();
//
//        // Finds the relevant vertex for the layout
//        if (vertexArray == null)
//            vertexArray = new ArrayList();
//        else
//            vertexArray.clear();
//
//        Set<V> vx = graph.vertexSet();
//        if (vx == null) return true;
//
//        for (V v : graph.vertexSet()) {
//            D vd = getDisplay(graph, v);
//            if (vd == null) continue;
//            if (getRadius(vd) == 0) continue;
//            vertexArray.add(vd);
//        }
//
//        mxRectangle initialBounds = null; //new mxRectangle(-100, -50, 100, 50);
//                //? graph.getBoundsForCells(vertexArray, false, false, true) : null;
//
//        int n = vertexArray.size();
//
//
//        if ((cellLocation == null) || (cellLocation.length!=n)) {
//            dispX = new double[n];
//            dispY = new double[n];
//            cellLocation = new double[n][];
//            isMoveable = new boolean[n];
//            neighbors = new int[n][];
//            radius = new double[n];
//            radiusSquared = new double[n];
//        }
//
//        minDistanceLimitSquared = minDistanceLimit * minDistanceLimit;
//
//        if (forceConstant < 0.001) {
//            forceConstant = 0.001;
//        }
//
//        forceConstantSquared = forceConstant * forceConstant;
//
//		// Create a map of vertex first. This is required for the array of
//        // arrays called neighbours which holds, for each vertex, a list of
//        // ints which represents the neighbours cells to that vertex as
//        // the indices into vertexArray
//
//        for (int i = 0; i < n; i++) {
//            D vd = vertexArray.get(i);
//
//            //TODO is this necessary?
//            /*if (!graph.containsVertex(vd.getVertex()))
//                continue;*/
//
//            /*if (vd == null) {
//                vd = new VertexVis(vertex);
//                displayed.put(vertex, vd);
//            }*/
//
//            if (cellLocation[i]==null)
//                cellLocation[i] = new double[2];
//
//            // Set up the mapping from array indices to cells
//            indices.put(getVertex(vd), i);
//            //mxRectangle bounds = getVertexBounds(vertex);
//
//			// Set the X,Y value of the internal version of the cell to
//            // the center point of the vertex for better positioning
//            double width = getRadius(vd)*2f; //bounds.getWidth();
//            double height = getRadius(vd)*2f; //bounds.getHeight();
//
//            // Randomize (0, 0) locations
//            //TODO re-use existing location
//            double x, y;
//            if (vd==null) {
//                x = 0;//Math.random() * 100.0;//Math.random() * 100; //bounds.getX();
//                y = 0;//Math.random() * 100.0;
//            }
//            else {
//                x = getX(vd);
//                y = getY(vd);
//            }
//
//
//            cellLocation[i][0] = x + width / 2.0;
//            cellLocation[i][1] = y + height / 2.0;
//
//            radius[i] = Math.min(width, height);
//            radiusSquared[i] = radius[i] * radius[i];
//
//
//            // Moves cell location back to top-left from center locations used in
//            // algorithm, resetting the edge points is part of the transaction
//
//
//            dispX[i] = 0;
//            dispY[i] = 0;
//            isMoveable[i] = isVertexMovable(vd);
//            // Get lists of neighbours to all vertex, translate the cells
//            // obtained in indices into vertexArray and store as an array
//            // against the original cell index
//            //V v = vertexArray.get(i).getVertex();
//            //ProcessingGraphCanvas.VertexVis vd = displayed.get(v);
//
//
//            //TODO why does a vertex disappear from the graph... make this unnecessary
//
//
//            V v = getVertex(vd);
//            Set<E> edges = getEdges(graph, vd);
//            if (edges!=null) {
//                List<V> cells = new ArrayList(edges.size());
//                for (E e : edges) {
//                    if (isResetEdges()) {
//                        //graph.resetEdge(edge[k]);
//                    }
//
//                    if (isDisableEdgeStyle()) {
//                        //setEdgeStyleEnabled(edge[k], false);
//                    }
//
//
//                    V source = graph.getEdgeSource(e);
//                    V target = graph.getEdgeTarget(e);
//                    if (source!=v)  cells.add(source);
//                    else if (target!=v)  cells.add(target);
//                }
//
//                neighbors[i] = new int[cells.size()];
//
//                for (int j = 0; j < cells.size(); j++) {
//                    Integer index = indices.get(cells.get(j));
//
//                                        // Check the connected cell in part of the vertex list to be
//                    // acted on by this layout
//                    if (index != null) {
//                        neighbors[i][j] = index.intValue();
//                    } // Else if index of the other cell doesn't correspond to
//                    // any cell listed to be acted upon in this layout. Set
//                    // the index to the value of this vertex (a dummy self-loop)
//                    // so the attraction force of the edge is not calculated
//                    else {
//                        neighbors[i][j] = i;
//                    }
//                }
//            }
//        }
//
//        temperature = initialTemp;
//
//
//        // Main iteration loop
//        /*try {
//            for (iteration = 0; iteration < iterationsRemain; iteration++) {*/
//                if (!allowedToRun) {
//                    return false;
//                }
//
//                // Calculate repulsive forces on all vertex
//                calcRepulsion();
//
//                // Calculate attractive forces through edge
//                calcAttraction();
//
//                calcPositions();
//                //reduceTemperature();
//        /*    }
//        }
//        catch (Exception e) { }*/
//
//        double minx = 0, miny = 0, maxx = 0, maxy = 0;
//
//        for (int i = 0; i < vertexArray.size(); i++) {
//            D vd = vertexArray.get(i);
//
//            if (vd != null) {
//                //cellLocation[i][0] -= 1/2.0; //geo.getWidth() / 2.0;
//                //cellLocation[i][1] -= 1/2.0; //geo.getHeight() / 2.0;
//
//                float r = getRadius(vd);
//                double x = /*graph.snap*/(cellLocation[i][0] - r);
//                double y = /*graph.snap*/(cellLocation[i][1] - r);
//                setPosition(vd, (float) x, (float) y);
//
//                if (i == 0) {
//                    minx = maxx = x;
//                    miny = maxy = y;
//                } else {
//                    if (x < minx) minx = x;
//                    if (y < miny) miny = y;
//                    if (x > maxx) maxx = x;
//                    if (y > maxy) maxy = y;
//                }
//
//            }
//        }
//
//                    // Modifies the cloned geometries in-place. Not needed
//        // to clone the geometries again as we're in the same
//        // undoable change.
//        double dx = -(maxx+minx)/2f;
//        double dy = -(maxy+miny)/2f;
//
//        if (initialBounds != null) {
//            dx += initialBounds.getX();
//            dy += initialBounds.getY();
//        }
//
//        for (int i = 0; i < vertexArray.size(); i++) {
//            D vd = vertexArray.get(i);
//            movePosition(vd, (float) dx, (float) dy);
//        }
//
//        iterationsRemain--;
//
//        return true;
//    }
//
//    public boolean isVertexMovable(D vd) {
//        return true;
//    }
//
//    abstract public void setPosition(D vd, float x, float y);
//    abstract public void movePosition(D vd, float dx, float dy);
//
//    public Set<E> getEdges(Graph<V,E> graph, D vd) {
//        return graph.edgesOf(getVertex(vd));
//    }
//
//    abstract public V getVertex(D vd);
//
//    abstract public double getX(D vd);
//    abstract public double getY(D vd);
//
//    abstract public float getRadius(D vd);
//
//    /**
//     * Takes the displacements calculated for each cell and applies them to the
//     * local cache of cell positions. Limits the displacement to the current
//     * temperature.
//     */
//    protected void calcPositions() {
//        for (int index = 0; index < vertexArray.size(); index++) {
//            if (isMoveable[index]) {
//				// Get the distance of displacement for this node for this
//                // iteration
//                double deltaLength = Math.sqrt(dispX[index] * dispX[index]
//                        + dispY[index] * dispY[index]);
//
//                if (deltaLength < 0.001) {
//                    deltaLength = 0.001;
//                }
//
//				// Scale down by the current temperature if less than the
//                // displacement distance
//                double newXDisp = dispX[index] / deltaLength
//                        * Math.min(deltaLength, temperature);
//                double newYDisp = dispY[index] / deltaLength
//                        * Math.min(deltaLength, temperature);
//
//                // reset displacements
//                dispX[index] = 0;
//                dispY[index] = 0;
//
//                // Update the cached cell locations
//                cellLocation[index][0] += newXDisp;
//                cellLocation[index][1] += newYDisp;
//            }
//        }
//    }
//
//    /**
//     * Calculates the attractive forces between all laid out nodes linked by
// edge
//     */
//    protected void calcAttraction() {
//		// Check the neighbours of each vertex and calculate the attractive
//        // force of the edge connecting them
//        for (int i = 0; i < vertexArray.size(); i++) {
//            if (neighbors[i]==null) continue;
//            if (cellLocation[i] == null) continue;
//            for (int k = 0; k < neighbors[i].length; k++) {
//                // Get the index of the othe cell in the vertex array
//                int j = neighbors[i][k];
//
//                if (cellLocation[j] == null) continue;
//
//                // Do not proceed self-loops
//                if (i != j) {
//                    double xDelta = cellLocation[i][0] - cellLocation[j][0];
//                    double yDelta = cellLocation[i][1] - cellLocation[j][1];
//
//                    // The distance between the nodes
//                    double deltaLengthSquared = xDelta * xDelta + yDelta
//                            * yDelta - radiusSquared[i] - radiusSquared[j];
//
//                    if (deltaLengthSquared < minDistanceLimitSquared) {
//                        deltaLengthSquared = minDistanceLimitSquared;
//                    }
//
//                    double deltaLength = Math.sqrt(deltaLengthSquared);
//                    double force = (deltaLengthSquared) / forceConstant;
//
//                    double displacementX = (xDelta / deltaLength) * force;
//                    double displacementY = (yDelta / deltaLength) * force;
//
//                    if (isMoveable[i]) {
//                        this.dispX[i] -= displacementX;
//                        this.dispY[i] -= displacementY;
//                    }
//
//                    if (isMoveable[j]) {
//                        dispX[j] += displacementX;
//                        dispY[j] += displacementY;
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * Calculates the repulsive forces between all laid out nodes
//     */
//    protected void calcRepulsion() {
//        int vertexCount = vertexArray.size();
//
//        for (int i = 0; i < vertexCount; i++) {
//            for (int j = i; j < vertexCount; j++) {
//                // Exits if the layout is no longer allowed to run
//                if (!allowedToRun) {
//                    return;
//                }
//
//                if ((j != i) && (cellLocation[i]!=null) && (cellLocation[j]!=null)) {
//                    double xDelta = cellLocation[i][0] - cellLocation[j][0];
//                    double yDelta = cellLocation[i][1] - cellLocation[j][1];
//
//                    if (xDelta == 0) {
//                        xDelta = 0.01 + Math.random();
//                    }
//
//                    if (yDelta == 0) {
//                        yDelta = 0.01 + Math.random();
//                    }
//
//                    // Distance between nodes
//                    double deltaLength = Math.sqrt((xDelta * xDelta)
//                            + (yDelta * yDelta));
//
//                    double deltaLengthWithRadius = deltaLength - radius[i]
//                            - radius[j];
//
//                    if (deltaLengthWithRadius > maxDistanceLimit) {
//                        // Ignore vertex too far apart
//                        continue;
//                    }
//
//                    if (deltaLengthWithRadius < minDistanceLimit) {
//                        deltaLengthWithRadius = minDistanceLimit;
//                    }
//
//                    double force = forceConstantSquared / deltaLengthWithRadius;
//
//                    double displacementX = (xDelta / deltaLength) * force;
//                    double displacementY = (yDelta / deltaLength) * force;
//
//                    if (isMoveable[i]) {
//                        dispX[i] += displacementX;
//                        dispY[i] += displacementY;
//                    }
//
//                    if (isMoveable[j]) {
//                        dispX[j] -= displacementX;
//                        dispY[j] -= displacementY;
//                    }
//                }
//            }
//        }
//    }
//
// }
