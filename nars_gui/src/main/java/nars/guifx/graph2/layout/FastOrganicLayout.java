package nars.guifx.graph2.layout;

import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import javafx.beans.property.SimpleDoubleProperty;
import nars.Global;
import nars.data.Range;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.util.data.list.FasterList;

import java.awt.*;
import java.util.List;

/**
 * Fast organic layout algorithm, adapted from JGraph
 */
public class FastOrganicLayout<V extends TermNode> implements IterativeLayout<V> {

    @Range(min = 0, max = 1f)
    public final SimpleDoubleProperty nodeSpeed = new SimpleDoubleProperty(0.4);

    /**
     * Specifies if the top left corner of the input cells should be the origin
     * of the layout result. Default is true.
     */
    protected boolean useInputOrigin = false;

    /**
     * Specifies if all edge points of traversed edge should be removed.
     * Default is true.
     */
    protected boolean resetEdges = true;


    /**
     * The force constant by which the attractive forces are divided and the
     * replusive forces are multiple by the square of. The value equates to the
     * average radius there is of free space around each node. Default is 50.
     */

    @Range(min = 0, max = 300f)
    public final SimpleDoubleProperty forceConstant = new SimpleDoubleProperty(100);

    @Range(min = 0.5f, max = 4f)
    public final SimpleDoubleProperty spacing = new SimpleDoubleProperty(1f);


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
 longer has an effect
     */
    protected double maxDistanceLimit = 1000;

    /**
     * Start value of temperature. Default is 200.
     */
    protected double initialTemp = 100;

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
     * An array of all vertex to be laid out.
     */
    protected List<TermNode> vertexArray;

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
     * Boolean flag that specifies if the layout is allowed to run. If this is
     * set to false, then the layout exits in the following iteration.
     */
    protected boolean allowedToRun = true;

    /**
     * Maps from vertex to indices.
     */
    protected ObjectIntHashMap<TermNode> indices;

    /** final normalization step to center all nodes */
    private final boolean center = false;
    private final FasterList<TermNode> cells = new FasterList();


    /**
     * Constructs a new fast organic layout for the specified graph.
     */
    public FastOrganicLayout() {
        
        setInitialTemp(9f);
        setMinDistanceLimit(10f);
        setMaxDistanceLimit(200f);
        
        setMaxIterations(2);
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


    @Override
    public void run(SpaceGrapher graph, int iterations) {
        

        if (indices == null)
            indices = new ObjectIntHashMap();
        else
            indices.clear();
        
        // Finds the relevant vertex for the layout
        if (vertexArray == null)
            vertexArray = Global.newArrayList();
        else
            vertexArray.clear();
        
        for (TermNode vd : graph.displayed) {
            //TermNode vd = g.getVertexDisplay(v);
            if (vd == null) continue;
            if (vd.width() /*getRadius()*/ == 0) continue;
            vertexArray.add(vd);
        }
        
        Rectangle initialBounds = null; //new mxRectangle(-100, -50, 100, 50);
                //? graph.getBoundsForCells(vertexArray, false, false, true) : null;
        
        int n = vertexArray.size();

        
        if ((cellLocation == null) || (cellLocation.length!=n)) {
            dispX = new double[n];
            dispY = new double[n];
            cellLocation = new double[n][];
            isMoveable = new boolean[n];
            //if (neighbors == null || neighbors.length<n)
            neighbors = new int[n][];
            radius = new double[n];
            radiusSquared = new double[n];
        }       
        
        minDistanceLimitSquared = minDistanceLimit * minDistanceLimit;

        double forceConstant = this.forceConstant.doubleValue();
        if (forceConstant < 0.001) {
            forceConstant = 0.001;
        }

        forceConstantSquared = forceConstant * forceConstant;

		// Create a map of vertex first. This is required for the array of
        // arrays called neighbours which holds, for each vertex, a list of
        // ints which represents the neighbours cells to that vertex as
        // the indices into vertexArray

        final double spacing = this.spacing.get();

        for (int i = 0; i < n; i++) {
            TermNode vd = vertexArray.get(i);
            
            //TODO is this necessary?
            /*if (!graph.containsVertex(vd.getVertex()))
                continue;*/
            
            /*if (vd == null) {
                vd = new TermNode(vertex);
                displayed.put(vertex, vd);
            }*/
            
            if (cellLocation[i]==null)
                cellLocation[i] = new double[2];

            // Set up the mapping from array indices to cells
            indices.put(vd, i);
            //mxRectangle bounds = getVertexBounds(vertex);

			// Set the X,Y value of the internal version of the cell to
            // the center point of the vertex for better positioning
            double ww = Math.max(vd.width(), vd.height()); /*getRadius()*/
            double width = ww*2f; //bounds.getWidth();
            double height = ww*2f; //bounds.getHeight();

            // Randomize (0, 0) locations
            //TODO re-use existing location
            double x, y;
            if (vd==null) {
                x = 0;//Math.random() * 100.0;//Math.random() * 100; //bounds.getX();
                y = 0;//Math.random() * 100.0;
            }
            else {
                x = vd.x();
                y = vd.y();
            }
            

            cellLocation[i][0] = x + width / 2.0;
            cellLocation[i][1] = y + height / 2.0;

            double r = radius[i] = Math.min(width, height);
            radiusSquared[i] = r*r;
        

            // Moves cell location back to top-left from center locations used in
            // algorithm, resetting the edge points is part of the transaction
        
        
            dispX[i] = 0;
            dispY[i] = 0;
            isMoveable[i] = true; //isVertexMovable(vertexArray[i]);
            // Get lists of neighbours to all vertex, translate the cells
            // obtained in indices into vertexArray and store as an array
            // against the original cell index
            //V v = vertexArray.get(i).getVertex();
            //ProcessingGraphCanvas.TermNode vd = displayed.get(v);

            
            //TODO why does a vertex disappear from the graph... make this unnecessary



            //Set<E> edges = vd.getEdges();
            TermEdge[] edges = vd.getEdges();
            if (edges!=null) {


                final Object[] ccells;
                int cellsSize = edges.length;
                cells.clear();
                cells.ensureCapacity(cellsSize);
                ccells = cells.array();

                int cn = 0;
                for (TermEdge e : edges) {
                    //for (E e : edges) {
//                    if (isResetEdges()) {
//                        //graph.resetEdge(edge[k]);
//                    }
//
//                    if (isDisableEdgeStyle()) {
//                        //setEdgeStyleEnabled(edge[k], false);
//                    }


                    TermNode source = e.aSrc; //graph.getEdgeSource(e);
                    TermNode target = e.bSrc; //graph.getEdgeTarget(e);
                    ccells[cn++] = (source!=vd ? source : target);
                    //else if (target!=vd)  cells.add(target);
                }

                int[] ni;// = neighbors[i];
                //if (ni == null || ni.length != cellsSize)
                    ni = neighbors[i] = new int[cellsSize];
//                else {
//                    Arrays.fill(neighbors[i], -1);
//                }

                for (int j = 0; j < cellsSize; j++) {
                    int index = indices.getIfAbsent(ccells[j], -1);

                    // Check the connected cell in part of the vertex list to be
                    // acted on by this layout
                    if (index != -1) {
                        ni[j] = index;
                    } // Else if index of the other cell doesn't correspond to
                    // any cell listed to be acted upon in this layout. Set
                    // the index to the value of this vertex (a dummy self-loop)
                    // so the attraction force of the edge is not calculated
                    else {
                        ni[j] = i;
                    }
                }
            }
        }

        temperature = initialTemp;

        // If max number of iterations has not been set, guess it
        if (maxIterations == 0) {
            maxIterations = 20.0 * Math.sqrt(n);
        }

        // Main iteration loop
        try {
            for (iteration = 0; iteration < maxIterations; iteration++) {
                if (!allowedToRun) {
                    return;
                }


                // Calculate repulsive forces on all vertex
                calcRepulsion();

                // Calculate attractive forces through edge
                calcAttraction();

                calcPositions();
                reduceTemperature();
            }
        }
        catch (Exception e) { }

        double minx = 0, miny = 0, maxx = 0, maxy = 0;

        double speed = nodeSpeed.get();

        for (int i = 0; i < vertexArray.size(); i++) {
            TermNode vd = vertexArray.get(i);

            if (vd != null) {
                double[] ci = cellLocation[i];

                //cellLocation[i][0] -= 1/2.0; //geo.getWidth() / 2.0;
                //cellLocation[i][1] -= 1/2.0; //geo.getHeight() / 2.0;

                float r = (float)vd.width(); //getRadius();

                double x = /*graph.snap*/(ci[0] - r);
                double y = /*graph.snap*/(ci[1] - r);


                vd.move((float)x, (float)y, speed);

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

        if (center) {
            // Modifies the cloned geometries in-place. Not needed
            // to clone the geometries again as we're in the same
            // undoable change.
            double dx = -(maxx + minx) / 2f;
            double dy = -(maxy + miny) / 2f;

            if (initialBounds != null) {
                dx += initialBounds.getX();
                dy += initialBounds.getY();
            }

            for (int i = 0; i < vertexArray.size(); i++) {
                TermNode vd = vertexArray.get(i);
                vd.moveDelta((float) dx, (float) dy);
            }
        }
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
 edge
     */
    protected void calcAttraction() {
		// Check the neighbours of each vertex and calculate the attractive
        // force of the edge connecting them
        final double forceConstant = this.forceConstant.doubleValue();
        final double spacing = this.spacing.doubleValue();
        double[][] cellLocation = this.cellLocation;
        double[] radiusSquared = this.radiusSquared;

        for (int i = 0; i < vertexArray.size(); i++) {
            int[] neighbor = neighbors[i];
            if (neighbor ==null) continue;
            if (cellLocation[i] == null) continue;
            for (int k = 0; k < neighbor.length; k++) {
                // Get the index of the othe cell in the vertex array
                int j = neighbor[k];


                if (cellLocation[j] == null) continue;
                
                // Do not proceed self-loops
                if (i != j) {
                    double xDelta = cellLocation[i][0] - cellLocation[j][0];
                    double yDelta = cellLocation[i][1] - cellLocation[j][1];

                    // The distance between the nodes

                    double deltaLengthSquared = xDelta * xDelta + yDelta
                            * yDelta - (spacing * (radiusSquared[i] + radiusSquared[j]));

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

        double[] radius = this.radius;
        double[] dispX = this.dispX;
        double[] dispY = this.dispY;
        boolean[] movable = this.isMoveable;

        for (int i = 0; i < vertexCount; i++) {

            double[] ci = cellLocation[i];

            for (int j = i; j < vertexCount; j++) {
                // Exits if the layout is no longer allowed to run
                if (!allowedToRun) {
                    return;
                }

                double[] cj = cellLocation[j];

                if ((j != i) && (ci !=null) && (cj !=null)) {
                    double xDelta = ci[0] - cj[0];
                    double yDelta = ci[1] - cj[1];

                    if (xDelta == 0) {
                        xDelta = 0.01 + Math.random();
                    }

                    if (yDelta == 0) {
                        yDelta = 0.01 + Math.random();
                    }

                    // Distance between nodes
                    double deltaLength = Math.sqrt((xDelta * xDelta)
                            + (yDelta * yDelta));


                    double deltaLengthWithRadius =  deltaLength - radius[i]
                            - radius[j];

                    if (deltaLengthWithRadius > maxDistanceLimit) {
                        // Ignore vertex too far apart
                        continue;
                    }

                    if (deltaLengthWithRadius < minDistanceLimit) {
                        deltaLengthWithRadius = minDistanceLimit;
                    }

                    double force = forceConstantSquared / deltaLengthWithRadius;

                    double displacementX = (xDelta / deltaLength) * force;
                    double displacementY = (yDelta / deltaLength) * force;

                    if (movable[i]) {
                        dispX[i] += displacementX;
                        dispY[i] += displacementY;
                    }

                    if (movable[j]) {
                        dispX[j] -= displacementX;
                        dispY[j] -= displacementY;
                    }
                }
            }
        }
    }


    


}