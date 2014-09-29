package nars.gui.output.graph.layout;

import com.mxgraph.util.mxRectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nars.gui.output.graph.ProcessingGraphCanvas;
import nars.gui.output.graph.ProcessingGraphCanvas.VertexDisplay;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * Fast organic layout algorithm, adapted from JGraph
 */
public class FastOrganicLayout<V, E> {

    /**
     * Specifies if the top left corner of the input cells should be the origin
     * of the layout result. Default is true.
     */
    protected boolean useInputOrigin = true;

    /**
     * Specifies if all edge points of traversed edges should be removed.
     * Default is true.
     */
    protected boolean resetEdges = true;

    /**
     * Specifies if the STYLE_NOEDGESTYLE flag should be set on edges that are
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
     * The maximum distance between vertices, beyond which their repulsion no
     * longer has an effect
     */
    protected double maxDistanceLimit = 500;

    /**
     * Start value of temperature. Default is 200.
     */
    protected double initialTemp = 200;

    /**
     * Temperature to limit displacement at later stages of layout.
     */
    protected double temperature = 0;

    /**
     * Total number of iterations to run the layout though.
     */
    protected double maxIterations = 0;

    /**
     * Current iteration count.
     */
    protected double iteration = 0;

    /**
     * An array of all vertices to be laid out.
     */
    protected List<V> vertexArray;

    /**
     * An array of locally stored X co-ordinate displacements for the vertices.
     */
    protected double[] dispX;

    /**
     * An array of locally stored Y co-ordinate displacements for the vertices.
     */
    protected double[] dispY;

    /**
     * An array of locally stored co-ordinate positions for the vertices.
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
     * Array of booleans representing the movable states of the vertices.
     */
    protected boolean[] isMoveable;

    /**
     * Local copy of cell neighbours.
     */
    protected int[][] neighbours;

    /**
     * Boolean flag that specifies if the layout is allowed to run. If this is
     * set to false, then the layout exits in the following iteration.
     */
    protected boolean allowedToRun = true;

    /**
     * Maps from vertices to indices.
     */
    protected Map<V, Integer> indices = new HashMap<V, Integer>();
    private final DirectedMultigraph<V, E> graph;

    /**
     * Constructs a new fast organic layout for the specified graph.
     */
    public FastOrganicLayout(DirectedMultigraph<V, E> graph) {
        this.graph = graph;
        

    }

    /**
     * Returns a boolean indicating if the given <mxCell> should be ignored as a
     * vertex. This returns true if the cell has no connections.
     *
     * @param vertex Object that represents the vertex to be tested.
     * @return Returns true if the vertex should be ignored.
     */
    public boolean isVertexIgnored(V vertex) {
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
     *
     * @param value
     */
    public void setUseInputOrigin(boolean value) {
        useInputOrigin = value;
    }

    /**
     *
     */
    public boolean isResetEdges() {
        return resetEdges;
    }

    /**
     *
     * @param value
     */
    public void setResetEdges(boolean value) {
        resetEdges = value;
    }

    /**
     *
     */
    public boolean isDisableEdgeStyle() {
        return disableEdgeStyle;
    }

    /**
     *
     * @param value
     */
    public void setDisableEdgeStyle(boolean value) {
        disableEdgeStyle = value;
    }

    /**
     *
     */
    public double getMaxIterations() {
        return maxIterations;
    }

    /**
     *
     * @param value
     */
    public void setMaxIterations(double value) {
        maxIterations = value;
    }

    /**
     *
     */
    public double getForceConstant() {
        return forceConstant;
    }

    /**
     *
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
     *
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
     *
     * @param value
     */
    public void setInitialTemp(double value) {
        initialTemp = value;
    }

    /**
     * Reduces the temperature of the layout from an initial setting in a linear
     * fashion to zero.
     */
    protected void reduceTemperature() {
        temperature = initialTemp * (1.0 - iteration / maxIterations);
    }

    /* (non-Javadoc)
     * @see com.mxgraph.layout.mxIGraphLayout#execute(java.lang.Object)
     */
    public void execute(Map<V, ProcessingGraphCanvas.VertexDisplay> displayed) {

        // Finds the relevant vertices for the layout
        vertexArray = new ArrayList<>( graph.vertexSet() );

        mxRectangle initialBounds = null; //new mxRectangle(-100, -50, 100, 50);
                //? graph.getBoundsForCells(vertexArray, false, false, true) : null;
        
        int n = vertexArray.size();

        dispX = new double[n];
        dispY = new double[n];
        cellLocation = new double[n][];
        isMoveable = new boolean[n];
        neighbours = new int[n][];
        radius = new double[n];
        radiusSquared = new double[n];

        
        
        minDistanceLimitSquared = minDistanceLimit * minDistanceLimit;

        if (forceConstant < 0.001) {
            forceConstant = 0.001;
        }

        forceConstantSquared = forceConstant * forceConstant;

		// Create a map of vertices first. This is required for the array of
        // arrays called neighbours which holds, for each vertex, a list of
        // ints which represents the neighbours cells to that vertex as
        // the indices into vertexArray
        
        for (int i = 0; i < n; i++) {
            V vertex = vertexArray.get(i);
            VertexDisplay vd = displayed.get(vertex);
            
            cellLocation[i] = new double[2];

            // Set up the mapping from array indices to cells
            indices.put(vertex, i);
            //mxRectangle bounds = getVertexBounds(vertex);

			// Set the X,Y value of the internal version of the cell to
            // the center point of the vertex for better positioning
            double width = vd.getRadius()*2f; //bounds.getWidth();
            double height = vd.getRadius()*2f; //bounds.getHeight();

            // Randomize (0, 0) locations
            //TODO re-use existing location
            double x, y;
            if (vd==null) {
                x = 0;//Math.random() * 100.0;//Math.random() * 100; //bounds.getX();
                y = 0;//Math.random() * 100.0;
            }
            else {
                x = vd.getX(); 
                y = vd.getY();
            }
            

            cellLocation[i][0] = x + width / 2.0;
            cellLocation[i][1] = y + height / 2.0;

            radius[i] = Math.min(width, height);
            radiusSquared[i] = radius[i] * radius[i];
        

            // Moves cell location back to top-left from center locations used in
            // algorithm, resetting the edge points is part of the transaction
        
        
            dispX[i] = 0;
            dispY[i] = 0;
            isMoveable[i] = true; //isVertexMovable(vertexArray[i]);
            // Get lists of neighbours to all vertices, translate the cells
            // obtained in indices into vertexArray and store as an array
            // against the original cell index
            V v = vertexArray.get(i);
            //ProcessingGraphCanvas.VertexDisplay vd = displayed.get(v);

            Set<E> edges = graph.edgesOf(v);
            List<V> cells = new ArrayList(edges.size());
            for (E e : edges) {
                if (isResetEdges()) {
                    //graph.resetEdge(edges[k]);
                }

                if (isDisableEdgeStyle()) {
                    //setEdgeStyleEnabled(edges[k], false);
                }

                V source = graph.getEdgeSource(e);
                V target = graph.getEdgeTarget(e);
                if (source!=v)  cells.add(source);
                else if (target!=v)  cells.add(target);
            }

            neighbours[i] = new int[cells.size()];

            for (int j = 0; j < cells.size(); j++) {
                Integer index = indices.get(cells.get(j));

                                    // Check the connected cell in part of the vertex list to be
                // acted on by this layout
                if (index != null) {
                    neighbours[i][j] = index.intValue();
                } // Else if index of the other cell doesn't correspond to
                // any cell listed to be acted upon in this layout. Set
                // the index to the value of this vertex (a dummy self-loop)
                // so the attraction force of the edge is not calculated
                else {
                    neighbours[i][j] = i;
                }
            }
        }

        temperature = initialTemp;

        // If max number of iterations has not been set, guess it
        if (maxIterations == 0) {
            maxIterations = 20.0 * Math.sqrt(n);
        }

        // Main iteration loop
        for (iteration = 0; iteration < maxIterations; iteration++) {
            if (!allowedToRun) {
                return;
            }

            // Calculate repulsive forces on all vertices
            calcRepulsion();

            // Calculate attractive forces through edges
            calcAttraction();

            calcPositions();
            reduceTemperature();
        }

        double minx = 0, miny = 0, maxx = 0, maxy = 0;

        for (int i = 0; i < vertexArray.size(); i++) {
            V vertex = vertexArray.get(i);                
            ProcessingGraphCanvas.VertexDisplay vd = displayed.get(vertex);

            if (vd != null) {
                //cellLocation[i][0] -= 1/2.0; //geo.getWidth() / 2.0;
                //cellLocation[i][1] -= 1/2.0; //geo.getHeight() / 2.0;

                float r = vd.getRadius();
                double x = /*graph.snap*/(cellLocation[i][0] - r/2f);
                double y = /*graph.snap*/(cellLocation[i][1] - r/2f);                    
                vd.setPosition((float)x, (float)y);

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
        double dx = -(maxx+minx)/2f;
        double dy = -(maxy+miny)/2f;

        if (initialBounds != null) {
            dx += initialBounds.getX();
            dy += initialBounds.getY();
        }

        for (VertexDisplay vd: displayed.values())
            vd.movePosition((float)dx, (float)dy);
            
    }

    /**
     * Takes the displacements calculated for each cell and applies them to the
     * local cache of cell positions. Limits the displacement to the current
     * temperature.
     */
    protected void calcPositions() {
        for (int index = 0; index < vertexArray.size(); index++) {
            if (isMoveable[index]) {
				// Get the distance of displacement for this node for this
                // iteration
                double deltaLength = Math.sqrt(dispX[index] * dispX[index]
                        + dispY[index] * dispY[index]);

                if (deltaLength < 0.001) {
                    deltaLength = 0.001;
                }

				// Scale down by the current temperature if less than the
                // displacement distance
                double newXDisp = dispX[index] / deltaLength
                        * Math.min(deltaLength, temperature);
                double newYDisp = dispY[index] / deltaLength
                        * Math.min(deltaLength, temperature);

                // reset displacements
                dispX[index] = 0;
                dispY[index] = 0;

                // Update the cached cell locations
                cellLocation[index][0] += newXDisp;
                cellLocation[index][1] += newYDisp;
            }
        }
    }

    /**
     * Calculates the attractive forces between all laid out nodes linked by
     * edges
     */
    protected void calcAttraction() {
		// Check the neighbours of each vertex and calculate the attractive
        // force of the edge connecting them
        for (int i = 0; i < vertexArray.size(); i++) {
            for (int k = 0; k < neighbours[i].length; k++) {
                // Get the index of the othe cell in the vertex array
                int j = neighbours[i][k];

                // Do not proceed self-loops
                if (i != j) {
                    double xDelta = cellLocation[i][0] - cellLocation[j][0];
                    double yDelta = cellLocation[i][1] - cellLocation[j][1];

                    // The distance between the nodes
                    double deltaLengthSquared = xDelta * xDelta + yDelta
                            * yDelta - radiusSquared[i] - radiusSquared[j];

                    if (deltaLengthSquared < minDistanceLimitSquared) {
                        deltaLengthSquared = minDistanceLimitSquared;
                    }

                    double deltaLength = Math.sqrt(deltaLengthSquared);
                    double force = (deltaLengthSquared) / forceConstant;

                    double displacementX = (xDelta / deltaLength) * force;
                    double displacementY = (yDelta / deltaLength) * force;

                    if (isMoveable[i]) {
                        this.dispX[i] -= displacementX;
                        this.dispY[i] -= displacementY;
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
        int vertexCount = vertexArray.size();

        for (int i = 0; i < vertexCount; i++) {
            for (int j = i; j < vertexCount; j++) {
                // Exits if the layout is no longer allowed to run
                if (!allowedToRun) {
                    return;
                }

                if ((j != i) && (cellLocation[i]!=null) && (cellLocation[j]!=null)) {
                    double xDelta = cellLocation[i][0] - cellLocation[j][0];
                    double yDelta = cellLocation[i][1] - cellLocation[j][1];

                    if (xDelta == 0) {
                        xDelta = 0.01 + Math.random();
                    }

                    if (yDelta == 0) {
                        yDelta = 0.01 + Math.random();
                    }

                    // Distance between nodes
                    double deltaLength = Math.sqrt((xDelta * xDelta)
                            + (yDelta * yDelta));

                    double deltaLengthWithRadius = deltaLength - radius[i]
                            - radius[j];

                    if (deltaLengthWithRadius > maxDistanceLimit) {
                        // Ignore vertices too far apart
                        continue;
                    }

                    if (deltaLengthWithRadius < minDistanceLimit) {
                        deltaLengthWithRadius = minDistanceLimit;
                    }

                    double force = forceConstantSquared / deltaLengthWithRadius;

                    double displacementX = (xDelta / deltaLength) * force;
                    double displacementY = (yDelta / deltaLength) * force;

                    if (isMoveable[i]) {
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
