/**
 * Copyright (c) 2007-2013, JGraph Ltd
 */

package nars.guifx.graph2.layout;

//import com.mxgraph.layout.mxGraphLayout;
//import com.mxgraph.model.mxGraphModel;
//import com.mxgraph.model.mxIGraphModel;
//import com.mxgraph.util.mxRectangle;
//import com.mxgraph.view.mxGraph;
//import com.mxgraph.view.mxGraphView;

import com.gs.collections.impl.list.mutable.primitive.IntArrayList;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import nars.data.Range;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.util.data.list.FasterList;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * An implementation of a simulated annealing layout, based on "Drawing Graphs
 * Nicely Using Simulated Annealing" by Davidson and Harel (1996). This
 * paper describes these criteria as being favourable in a graph layout: (1)
 * distributing nodes evenly, (2) making edge-lengths uniform, (3)
 * minimizing cross-crossings, and (4) keeping nodes from coming too close
 * to edges. These criteria are translated into energy cost functions in the
 * layout. Nodes or edges breaking these criteria create a larger cost function
 * , the total cost they contribute related to the extent that they break it.
 * The idea of the algorithm is to minimise the total system energy. Factors
 * are assigned to each of the criteria describing how important that
 * criteria is. Higher factors mean that those criteria are deemed to be
 * relatively preferable in the final layout. Most of  the criteria conflict
 * with the others to some extent and so the setting of the factors determines
 * the general look of the resulting graph.
 * <p>
 * In addition to the four aesthetic criteria the concept of a border line
 * which induces an energy cost to nodes in proximity to the graph bounds is
 * introduced to attempt to restrain the graph. All of the 5 factors can be
 * switched on or off using the <code>isOptimize...</code> variables.
 * <p>
 * Simulated Annealing is a force-directed layout and is one of the more
 * expensive, but generally effective layouts of this type. Layouts like
 * the spring layout only really factor in edge length and inter-node
 * distance being the lowest CPU intensive for the most aesthetic gain. The
 * additional factors are more expensive but can have very attractive results.
 * <p>
 * The main loop of the algorithm consist of processing the nodes in a 
 * deterministic order. During the processing of each node a circle of radius
 * <code>moveRadius</code> is made around the node and split into
 * <code>circleResolution</code> equal segments. Each point between neighbour
 * segments is determined and the new energy of the system if the node were
 * moved to that position calculated. Only the necessary nodes and edges are
 * processed new energy values resulting in quadratic performance, O(VE),
 * whereas calculating the total system energy would be cubic. The default
 * implementation only checks 8 points around the radius of the circle, as
 * opposed to the suggested 30 in the paper. Doubling the number of points
 * float the CPU load and 8 works almost as well as 30.
 * <p>
 * The <code>moveRadius</code> replaces the temperature as the influencing
 * factor in the way the graph settles in later iterations. If the user does
 * not set the initial move radius it is set to half the maximum dimension
 * of the graph. Thus, in 2 iterations a node may traverse the entire graph,
 * and it is more sensible to find minima this way that uphill moves, which
 * are little more than an expensive 'tilt' method. The factor by which
 * the radius is multiplied by after each iteration is important, lowering
 * it improves performance but raising it towards 1.0 can improve the
 * resulting graph aesthetics. When the radius hits the minimum move radius
 * defined, the layout terminates. The minimum move radius should be set
 * a value where the move distance is too minor to be of interest.
 * <p>
 * Also, the idea of a fine tuning phase is used, as described in the paper.
 * This involves only calculating the edge to node distance energy cost
 * at the end of the algorithm since it is an expensive calculation and
 * it really an 'optimizating' function. <code>fineTuningRadius</code>
 * defines the radius value that, when reached, causes the edge to node
 * distance to be calculated.
 * <p>
 * There are other special cases that are processed after each iteration.
 * <code>unchangedEnergyRoundTermination</code> defines the number of
 * iterations, after which the layout terminates. If nothing is being moved
 * it is assumed a good layout has been found. In addition to this if
 * no nodes are moved during an iteration the move radius is halved, presuming
 * that a finer granularity is required.
 * 
 */
public class HyperOrganicLayout<V extends TermNode> implements IterativeLayout<V> {






	/**
	 * Constructs a new fast organic layout for the specified graph.
	 */
//	public FastOrganicLayout() {
//
//		setInitialTemp(13f);
//		setMinDistanceLimit(1f);
//		setMaxDistanceLimit(200f);
//
//		setForceConstant(100f);
//		setMaxIterations(1);
//	}

	/**
	 * Whether or not the distance between edge and nodes will be calculated
	 * as an energy cost function. This function is CPU intensive and is best
	 * only used in the fine tuning phase.
	 */
	protected boolean isOptimizeEdgeDistance = false;

	/**
	 * Whether or not edges crosses will be calculated as an energy cost
	 * function. This function is CPU intensive, though if some iterations
	 * without it are required, it is best to have a few cycles at the start
	 * of the algorithm using it, then use it intermittantly through the rest
	 * of the layout.
	 */
	protected boolean isOptimizeEdgeCrossing = false;

	/**
	 * Whether or not edge lengths will be calculated as an energy cost
	 * function. This function not CPU intensive.
	 */
	protected boolean isOptimizeEdgeLength = false;

	/**
	 * Whether or not nodes will contribute an energy cost as they approach
	 * the bound of the graph. The cost increases to a limit close to the
	 * border and stays constant outside the bounds of the graph. This function
	 * is not CPU intensive
	 */
	protected boolean isOptimizeBorderLine = false;

//	/**
//	 * Whether or not node distribute will contribute an energy cost where
//	 * nodes are close together. The function is moderately CPU intensive.
//	 */
//	protected static final boolean isOptimizeNodeDistribution = true;

	/**
	 * when {@link #moveRadius}reaches this value, the algorithm is terminated
	 */
	protected float minMoveRadius = 1.0f;

	/**
	 * The current radius around each node where the next position energy
	 * values will be calculated for a possible move
	 */
	protected float moveRadius = 0.0F;

	/**
	 * The initial value of <code>moveRadius</code>. If this is set to zero
	 * the layout will automatically determine a suitable value.
	 */
	protected float initialMoveRadius = 0.0f;

	/**
	 * The factor by which the <code>moveRadius</code> is multiplied by after
	 * every iteration. A value of 0.75 is a good balance between performance
	 * and aesthetics. Increasing the value provides more chances to find
	 * minimum energy positions and decreasing it causes the minimum radius
	 * termination condition to occur more quickly.
	 */
	protected float radiusScaleFactor = 0.9f;

	/**
	 * The average amount of area allocated per node. If <code> bounds</code>
	 * is not set this value mutiplied by the number of nodes to find
	 * the total graph area. The graph is assumed square.
	 */
	@Range(min = 50, max = 200000)
	public final SimpleDoubleProperty averageNodeArea = new SimpleDoubleProperty(100);

	/**
	 * The radius below which fine tuning of the layout should start
	 * This involves allowing the distance between nodes and edges to be
	 * taken into account in the total energy calculation. If this is set to
	 * zero, the layout will automatically determine a suitable value
	 */
	protected float fineTuningRadius = 40.0f;

	/**
	 * Limit to the number of iterations that may take place. This is only
	 * reached if one of the termination conditions does not occur first.
	 */
	protected int maxIterations = 10;

	/**
	 * Cost factor applied to energy calculations involving the distance
	 * nodes and edges. Increasing this value tends to cause nodes to move away
	 * from edges, at the partial cost of other graph aesthetics.
	 * <code>isOptimizeEdgeDistance</code> must be true for edge to nodes
	 * distances to be taken into account.
	 */
	protected float edgeDistanceCostFactor = 3000.0f;

	/**
	 * Cost factor applied to energy calculations involving edges that cross
	 * over one another. Increasing this value tends to result in fewer edge
	 * crossings, at the partial cost of other graph aesthetics.
	 * <code>isOptimizeEdgeCrossing</code> must be true for edge crossings
	 * to be taken into account.
	 */
	protected float edgeCrossingCostFactor = 6000.0f;

	/**
	 * Cost factor applied to energy calculations involving the general node
	 * distribution of the graph. Increasing this value tends to result in
	 * a better distribution of nodes across the available space, at the
	 * partial cost of other graph aesthetics.
	 * <code>isOptimizeNodeDistribution</code> must be true for this general
	 * distribution to be applied.
	 */
	protected float nodeDistributionCostFactor = 30000.0f;

	/**
	 * Cost factor applied to energy calculations for node promixity to the
	 * notional border of the graph. Increasing this value results in
	 * nodes tending towards the centre of the drawing space, at the
	 * partial cost of other graph aesthetics.
	 * <code>isOptimizeBorderLine</code> must be true for border
	 * repulsion to be applied.
	 */
	protected float borderLineCostFactor = 5.0f;

	/**
	 * Cost factor applied to energy calculations for the edge lengths.
	 * Increasing this value results in the layout attempting to shorten all
	 * edges to the minimum edge length, at the partial cost of other graph
	 * aesthetics.
	 * <code>isOptimizeEdgeLength</code> must be true for edge length
	 * shortening to be applied.
	 */
	protected float edgeLengthCostFactor = 0.02f;

	/**
	 * The x coordinate of the final graph
	 */
	protected float boundsX = 0.0f;

	/**
	 * The y coordinate of the final graph
	 */
	protected float boundsY = 0.0f;

	/**
	 * The width coordinate of the final graph
	 */
	protected float boundsWidth = 0.0f;

	/**
	 * The height coordinate of the final graph
	 */
	protected float boundsHeight = 0.0f;

	/**
	 * current iteration number of the layout
	 */
	protected int iteration = 0;

	/**
	 * prevents from dividing with zero and from creating excessive energy
	 * values
	 */
	@Range(min = 0.1, max = 2)
	public final SimpleDoubleProperty minDistanceLimit = new SimpleDoubleProperty(1f);


	/**
	 * cached version of <code>minDistanceLimit</code> squared
	 */
	protected float minDistanceLimitSquared;

	/**
	 * distance limit beyond which energy costs due to object repulsive is
	 * not calculated as it would be too insignificant
	 */
	@Range(min = 0, max = 500)
	public final SimpleDoubleProperty maxDistanceLimit = new SimpleDoubleProperty(200);

	/**
	 * cached version of <code>maxDistanceLimit</code> squared
	 */
	protected float maxDistanceLimitSquared = 0.0F;

	/**
	 * Keeps track of how many consecutive round have passed without any energy
	 * changes
	 */
	protected int unchangedEnergyRoundCount = 0;

	@Range(min = 0, max = 0.2)
	public final SimpleDoubleProperty vertexSpeed = new SimpleDoubleProperty(0.04);

	float vertexMotionThreshold = 0.0f;


	/**
	 * The number of round of no node moves taking placed that the layout
	 * terminates
	 */
	protected int unchangedEnergyRoundTermination = 5;

	/**
	 * Whether or not to use approximate node dimensions or not. Set to true
	 * the radius squared of the smaller dimension is used. Set to false the
	 * radiusSquared variable of the CellWrapper contains the width squared
	 * and heightSquared is used in the obvious manner.
	 */
	protected boolean approxNodeDimensions = true;

	/**
	 * Internal models collection of nodes ( vertices ) to be laid out
	 */
	protected CellWrapper<TermNode>[] v = null;

	/**
	 * Internal models collection of edges to be laid out
	 */
	protected List<CellWrapper<TermEdge>> e = new FasterList();

	/**
	 * Array of the x portion of the normalised test vectors that
	 * are tested for a lower energy around each vertex. The vector
	 * of the combined x and y normals are multipled by the current
	 * radius to obtain test points for each vector in the array.
	 */
	protected static final float[] xNormTry;

	/**
	 * Array of the y portion of the normalised test vectors that
	 * are tested for a lower energy around each vertex. The vector
	 * of the combined x and y normals are multipled by the current
	 * radius to obtain test points for each vector in the array.
	 */
	protected static final float[] yNormTry;

	/**
	 * determines, in how many segments the circle around cells is divided, to
	 * find a new position for the cell. Doubling this value floats the CPU
	 * load. Increasing it beyond 16 might mean a change to the
	 * <code>performRound</code> method might further improve accuracy for a
	 * small performance hit. The change is described in the method comment.
	 */
	protected static final int circleResolution = 9; //originally 8, lets try an odd or prime #

	static {

		// Setup the normal vectors for the test points to move each vertex to
		xNormTry = new float[circleResolution];
		yNormTry = new float[circleResolution];

		for (int i = 0; i < circleResolution; i++) {
			double angle = i
					* ((2.0 * Math.PI) / circleResolution);
			xNormTry[i] = (float) Math.cos(angle);
			yNormTry[i] = (float) Math.sin(angle);
		}
	}

	/**
	 * Whether or not fine tuning is on. The determines whether or not
	 * node to edge distances are calculated in the total system energy.
	 * This cost function , besides detecting line intersection, is a
	 * performance intensive component of this algorithm and best left
	 * to optimization phase. <code>isFineTuning</code> is switched to
	 * <code>true</code> if and when the <code>fineTuningRadius</code>
	 * radius is reached. Switching this variable to <code>true</code>
	 * before the algorithm runs mean the node to edge cost function
	 * is always calculated.
	 */
	protected boolean isFineTuning = false;

	/**
	 * Specifies if the STYLE_NOEDGESTYLE flag should be set on edges that are
	 * modified by the result. Default is true.
	 */
	protected boolean disableEdgeStyle = true;



	public HyperOrganicLayout() {
		this(new Rectangle2D.Float(-25 * 1.0f, -25 * 1.0f, 25.0f * 1.0f, 25.0f * 1.0f));
	}

	public HyperOrganicLayout(float scale) {
		this(new Rectangle2D.Float(-scale / 2.0f, -scale / 2.0f, scale * 1.0f, scale * 1.0f));
	}

	/**
	 * Constructor for HyperOrganicLayout.
	 */
	public HyperOrganicLayout(Rectangle2D.Float bounds) {
		boundsX = bounds.x;
		boundsY = bounds.y;
		boundsWidth = bounds.width;
		boundsHeight = bounds.height;
	}

//	/**
//	 * Returns true if the given vertex has no connected edges.
//	 *
//	 * @param vertex Object that represents the vertex to be tested.
//	 * @return Returns true if the vertex should be ignored.
//	 */
//	public boolean isVertexIgnored(Object vertex)
//	{
//		return false;
//	}

	@Override
	public void run(SpaceGrapher graph, int iterations) {
		run(graph.displayed);
	}

	public void run(Parent p, int iterations) {
		ObservableList<Node> c = p.getChildrenUnmodifiable();
		for (int i = 0; i < iterations; i++)
			run(c);
	}

	public void run(ObservableList<Node> c) {
		GraphNode[] gg = c.stream().filter(nn -> nn instanceof GraphNode).map(nn -> (GraphNode) nn).toArray(GraphNode[]::new);
		run(gg);
	}

	public void run(GraphNode[] nodes) {

		if (nodes.length == 0) return;

		GraphNode[] vertexSet = nodes;
		GraphNode[] vertices = vertexSet;

		//HashSet<Object> vertexSet = new HashSet<Object>(Arrays.asList(vertices));

		//HashSet<Object> validEdges = new HashSet<Object>();

//		// Remove edges that do not have both source and target terminals visible
//		for (int i = 0; i < vertexSet.length; i++)
//		{
//			TermNode vertex = vertexSet[i];
//			TermEdge[] edges = vertex.getEdges(); //getEdges(model, vertices[i], false, true, false)
////
////			for (int j = 0; j < edges.length; j++)
////			{
////				// Only deal with sources. To be valid in the layout, each edge must be attached
////				// at both source and target to a vertex in the layout. Doing this avoids processing
////				// each edge twice.
////				if (view.getVisibleTerminal(edges[j], true) == vertices[i] && vertexSet.contains(view.getVisibleTerminal(edges[j], false)))
////				{
////					validEdges.add(edges[j]);
////				}
////			}
//
//		}

		//Object[] edges = validEdges.toArray();

		// If the bounds dimensions have not been set see if the average area
		// per node has been
		Rectangle2D.Float totalBounds = null;
		Rectangle2D.Float bounds = null;

		// Form internal model of nodes
		ObjectIntHashMap vertexMap = new ObjectIntHashMap();

		CellWrapper<TermNode>[] v = this.v = new CellWrapper[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			CellWrapper vi = v[i] = new CellWrapper(vertices[i]);

			vertexMap.put(vertices[i], i); //new Integer(i));
			bounds = getVertexBounds(vertices[i]);

			if (totalBounds == null) {
				totalBounds = (Rectangle2D.Float) bounds.clone();
			} else {
				totalBounds.add(bounds);
			}

			// Set the X,Y value of the internal version of the cell to
			// the center point of the vertex for better positioning
			float width = bounds.width;
			float height = bounds.height;
			vi.x = bounds.x + width / 2.0f;
			vi.y = bounds.y + height / 2.0f;
			if (approxNodeDimensions) {
				vi.radiusSquared = Math.min(width, height);
				vi.radiusSquared *= vi.radiusSquared;
			} else {
				vi.radiusSquared = width * width;
				vi.heightSquared = height * height;
			}
		}

		float averageNodeArea = this.averageNodeArea.floatValue();
		if (averageNodeArea == 0.0) {
			if (boundsWidth == 0.0 && totalBounds != null) {
				// Just use current bounds of graph
				boundsX = totalBounds.x;
				boundsY = totalBounds.y;
				boundsWidth = totalBounds.width;
				boundsHeight = totalBounds.height;
			}
		} else {
			// find the center point of the current graph
			// based the new graph bounds on the average node area set
			float newArea = averageNodeArea * vertices.length;
			float squareLength = (float) Math.sqrt(newArea);
			if (bounds != null) {
				float centreX = totalBounds.x + totalBounds.width / 2.0f;
				float centreY = totalBounds.y + totalBounds.height / 2.0f;
				boundsX = centreX - squareLength / 2.0f;
				boundsY = centreY - squareLength / 2.0f;
			} else {
				boundsX = 0;
				boundsY = 0;
			}
			boundsWidth = squareLength;
			boundsHeight = squareLength;
			// Ensure x and y are 0 or positive
			if (boundsX < 0.0 || boundsY < 0.0) {
				float maxNegativeAxis = Math.min(boundsX, boundsY);
				float axisOffset = -maxNegativeAxis;
				boundsX += axisOffset;
				boundsY += axisOffset;
			}
		}


		// Form internal model of edges

		List<CellWrapper<TermEdge>> e = this.e;
		e.clear();

		for (GraphNode vi : vertices) {
			if (vi == null)
				continue;
			TermEdge[] eii = vi.getEdges();
			for (TermEdge ei : eii) {

				CellWrapper w = new CellWrapper(ei);
				e.add(w);


				Object sourceCell = ei.aSrc; //model.getTerminal(edges[i], true);
				Object targetCell = ei.bSrc; //model.getTerminal(edges[i], false);
				int source = -1;
				// Check if either end of the edge is not connected
				if (sourceCell != null) {
					source = vertexMap.get(sourceCell);
				}
				int target = -1;
				if (targetCell != null) {
					target = vertexMap.get(targetCell);
				}
				w.source = source != -1 ? source : -1;
				w.target = target != -1 ? target : -1;
			}
		}


		// Set up internal nodes with information about whether edges
		// are connected to them or not
		IntArrayList relevantBuffer = null;
		for (int i = 0; i < v.length; i++) {
			relevantBuffer = v[i].relevantEdges = getRelevantEdges(i, relevantBuffer);
			v[i].connectedEdges = getConnectedEdges(i);
		}

		// If the initial move radius has not been set find a suitable value.
		// A good value is half the maximum dimension of the final graph area
		if (initialMoveRadius == 0.0f) {
			initialMoveRadius = Math.max(boundsWidth, boundsHeight) / 2.0f;
		}

		moveRadius = initialMoveRadius;

		float maxDistanceLimit = this.maxDistanceLimit.floatValue();
		float minDistanceLimit = this.minDistanceLimit.floatValue();
		minDistanceLimitSquared = minDistanceLimit * minDistanceLimit;
		maxDistanceLimitSquared = maxDistanceLimit * maxDistanceLimit;

		unchangedEnergyRoundCount = 0;


		// The main layout loop
		for (iteration = 0; iteration < maxIterations; iteration++) {
			performRound();
		}

		// Obtain the final positions
		float[][] result = new float[v.length][2];
		for (int i = 0; i < v.length; i++) {
			CellWrapper<TermNode> vi = v[i];
			vertices[i] = vi.cell;
			bounds = getVertexBounds(vertices[i]);

			result[i][0] = vi.x - bounds.width / 2.0f;
			result[i][1] = vi.y - bounds.height / 2.0f;
		}

		float vertexSpeed = this.vertexSpeed.floatValue();

		//model.beginUpdate();
		/*try
		{*/
		for (int i = 0; i < vertices.length; i++) {
			GraphNode vertice = vertices[i];
			if (vertice != null) {
				float[] ri = result[i];
				vertice.move(ri[0], ri[1], vertexSpeed, vertexMotionThreshold);
			}
			//setVertexLocation(vertices[i], result[i][0], result[i][1]);
		}
		/*}
		finally
		{*/
		//model.endUpdate();
		//}
	}

	Rectangle2D.Float unit = new Rectangle2D.Float(-32.0f, -32.0f, 32.0f, 32.0f);

	private Rectangle2D.Float getVertexBounds(GraphNode vertice) {
		return unit;
	}

	/**
	 * The main round of the algorithm. Firstly, a permutation of nodes
	 * is created and worked through in that random order. Then, for each node
	 * a number of point of a circle of radius <code>moveRadius</code> are
	 * selected and the total energy of the system calculated if that node
	 * were moved to that new position. If a lower energy position is found
	 * this is accepted and the algorithm moves onto the next node. There
	 * may be a slightly lower energy value yet to be found, but forcing
	 * the loop to check all possible positions adds nearly the current
	 * processing time again, and for little benefit. Another possible
	 * strategy would be to take account of the fact that the energy values
	 * around the circle decrease for half the loop and increase for the
	 * other, as a general rule. If part of the decrease were seen, then
	 * when the energy of a node increased, the previous node position was
	 * almost always the lowest energy position. This adds about two loop
	 * iterations to the inner loop and only makes sense with 16 tries or more.
	 */
	protected void performRound() {
		// sequential order cells are computed (every round the same order)

		// boolean to keep track of whether any moves were made in this round
		boolean energyHasChanged = false;
		for (int i = 0; i < v.length; i++) {
			int index = i;

			// Obtain the energies for the node is its current position
			// TODO The energy could be stored from the last iteration
			// and used again, rather than re-calculate
			float oldNodeDistribution = getNodeDistribution(index);
			float oldEdgeDistance = getEdgeDistanceFromNode(index);
			oldEdgeDistance += getEdgeDistanceAffectedNodes(index);
			float oldEdgeCrossing = getEdgeCrossingAffectedEdges(index);
			float oldBorderLine = getBorderline(index);
			float oldEdgeLength = getEdgeLengthAffectedEdges(index);
			float oldAdditionFactors = getAdditionFactorsEnergy(index);

			CellWrapper[] v = this.v;
			float moveRadius = this.moveRadius;
			for (int j = 0; j < circleResolution; j++) {
				float movex = moveRadius * xNormTry[j];
				float movey = moveRadius * yNormTry[j];

				// applying new move
                CellWrapper vv = v[index];

                float oldx = vv.x;
				float oldy = vv.y;
				vv.x = oldx + movex;
				vv.y = oldy + movey;

				// calculate the energy delta from this move
				float energyDelta = calcEnergyDelta(index,
						oldNodeDistribution, oldEdgeDistance, oldEdgeCrossing,
						oldBorderLine, oldEdgeLength, oldAdditionFactors);

				if (energyDelta < 0) {
					// energy of moved node is lower, finish tries for this
					// node
					energyHasChanged = true;
					break; // exits loop
				} else {
					// Revert node coordinates
					vv.x = oldx;
					vv.y = oldy;
				}
			}
		}
		// Check if we've hit the limit number of unchanged rounds that cause
		// a termination condition
		if (energyHasChanged) {
			unchangedEnergyRoundCount = 0;
		} else {
			unchangedEnergyRoundCount++;
			// Half the move radius in case assuming it's set too high for
			// what might be an optimisation case
			moveRadius /= 2.0;
		}
		if (unchangedEnergyRoundCount >= unchangedEnergyRoundTermination) {
			iteration = maxIterations;
		}

		// decrement radius in controlled manner
		float newMoveRadius = moveRadius * radiusScaleFactor;
		// Don't waste time on tiny decrements, if the final pixel resolution
		// is 50 then there's no point doing 55,54.1, 53.2 etc
		if (moveRadius - newMoveRadius < minMoveRadius) {
			newMoveRadius = moveRadius - minMoveRadius;
		}
		// If the temperature reaches its minimum temperature then finish
		if (newMoveRadius <= minMoveRadius) {
			iteration = maxIterations;
		}
		// Switch on fine tuning below the specified temperature
		if (newMoveRadius < fineTuningRadius) {
			isFineTuning = true;
		}

		moveRadius = newMoveRadius;

	}

	/**
	 * Calculates the change in energy for the specified node. The new energy is
	 * calculated from the cost function methods and the old energy values for
	 * each cost function are passed in as parameters
	 *
	 * @param index                      The index of the node in the <code>vertices</code> array
	 * @param oldNodeDistribution        The previous node distribution energy cost of this node
	 * @param oldEdgeDistance            The previous edge distance energy cost of this node
	 * @param oldEdgeCrossing            The previous edge crossing energy cost for edges connected to
	 *                                   this node
	 * @param oldBorderLine              The previous border line energy cost for this node
	 * @param oldEdgeLength              The previous edge length energy cost for edges connected to
	 *                                   this node
	 * @param oldAdditionalFactorsEnergy The previous energy cost for additional factors from
	 *                                   sub-classes
	 * @return the delta of the new energy cost to the old energy cost
	 */
	protected float calcEnergyDelta(int index, float oldNodeDistribution,
									float oldEdgeDistance, float oldEdgeCrossing,
									float oldBorderLine, float oldEdgeLength,
									float oldAdditionalFactorsEnergy) {
		float energyDelta = 0.0f;
		energyDelta += getNodeDistribution(index) * 2.0f;
		energyDelta -= oldNodeDistribution * 2.0f;

		energyDelta += getBorderline(index);
		energyDelta -= oldBorderLine;

		energyDelta += getEdgeDistanceFromNode(index);
		energyDelta += getEdgeDistanceAffectedNodes(index);
		energyDelta -= oldEdgeDistance;

		energyDelta -= oldEdgeLength;
		energyDelta += getEdgeLengthAffectedEdges(index);

		energyDelta -= oldEdgeCrossing;
		energyDelta += getEdgeCrossingAffectedEdges(index);

		energyDelta -= oldAdditionalFactorsEnergy;
		energyDelta += getAdditionFactorsEnergy(index);

		return energyDelta;
	}

	/**
	 * Calculates the energy cost of the specified node relative to all other
	 * nodes. Basically produces a higher energy the closer nodes are together.
	 *
	 * @param i the index of the node in the array <code>v</code>
	 * @return the total node distribution energy of the specified node
	 */
	protected float getNodeDistribution(int i) {
		float energy = 0.0f;

		float minDistanceLimitSquared = this.minDistanceLimitSquared;

		// This check is placed outside of the inner loop for speed, even
		// though the code then has to be duplicated
		if (true) {
			for (int j = 0; j < v.length; j++) {
                if (i != j) {
                    CellWrapper<TermNode> vi = v[i];
                    CellWrapper<TermNode> vj = v[j];
                    float vx = vi.x - vj.x;
                    float vy = vi.y - vj.y;
                    float distanceSquared = vx * vx + vy * vy;
                    distanceSquared -= vi.radiusSquared;
                    distanceSquared -= vj.radiusSquared;

                    // prevents from dividing with Zero.
                    if (distanceSquared < minDistanceLimitSquared) {
                        distanceSquared = minDistanceLimitSquared;
                    }

                    energy += nodeDistributionCostFactor / distanceSquared;
                }
            }
		}
		return energy;
	}

	/**
	 * This method calculates the energy of the distance of the specified
	 * node to the notional border of the graph. The energy increases up to
	 * a limited maximum close to the border and stays at that maximum
	 * up to and over the border.
	 *
	 * @param i the index of the node in the array <code>v</code>
	 * @return the total border line energy of the specified node
	 */
	protected float getBorderline(int i) {
		float energy = 0.0f;
		if (isOptimizeBorderLine) {
			float minDistanceLimit = this.minDistanceLimit.floatValue();
			// Avoid very small distances and convert negative distance (i.e
			// outside the border to small positive ones )
			CellWrapper<TermNode> vi = v[i];

			float l = vi.x - boundsX;
			if (l < minDistanceLimit)
				l = minDistanceLimit;
			float t = vi.y - boundsY;
			if (t < minDistanceLimit)
				t = minDistanceLimit;
			float r = boundsX + boundsWidth - vi.x;
			if (r < minDistanceLimit)
				r = minDistanceLimit;
			float b = boundsY + boundsHeight - vi.y;
			if (b < minDistanceLimit)
				b = minDistanceLimit;
			energy += borderLineCostFactor
					* ((1000000.0 / (t * t)) + (1000000.0 / (l * l))
					+ (1000000.0 / (b * b)) + (1000000.0 / (r * r)));
		}
		return energy;
	}

	/**
	 * Obtains the energy cost function for the specified node being moved.
	 * This involves calling <code>getEdgeLength</code> for all
	 * edges connected to the specified node
	 *
	 * @param node the node whose connected edges cost functions are to be
	 *             calculated
	 * @return the total edge length energy of the connected edges
	 */
	protected float getEdgeLengthAffectedEdges(int node) {
		float energy = 0.0f;
		IntArrayList vce = v[node].connectedEdges;
		for (int i = 0; i < vce.size(); i++) {
			energy += getEdgeLength(vce.get(i));
		}
		return energy;
	}

	/**
	 * This method calculates the energy due to the length of the specified
	 * edge. The energy is proportional to the length of the edge, making
	 * shorter edges preferable in the layout.
	 *
	 * @param i the index of the edge in the array <code>e</code>
	 * @return the total edge length energy of the specified edge
	 */
	protected float getEdgeLength(int i) {
		if (isOptimizeEdgeLength) {
			CellWrapper ei = e.get(i);
			CellWrapper eis = v[ei.source];
			CellWrapper eit = v[ei.target];

			float dx = eis.x - eit.x;
			float dy = eis.y - eit.y;
			float edgeLength = (float) Math.sqrt(dx * dx + dy * dy);
			return (edgeLengthCostFactor * edgeLength * edgeLength);
		} else {
			return 0.0f;
		}
	}

	/**
	 * Obtains the energy cost function for the specified node being moved.
	 * This involves calling <code>getEdgeCrossing</code> for all
	 * edges connected to the specified node
	 *
	 * @param node the node whose connected edges cost functions are to be
	 *             calculated
	 * @return the total edge crossing energy of the connected edges
	 */
	protected float getEdgeCrossingAffectedEdges(int node) {
		float energy = 0.0f;
		IntArrayList vce = v[node].connectedEdges;
		for (int i = 0; i < vce.size(); i++) {
			energy += getEdgeCrossing(vce.get(i));
		}

		return energy;
	}

	/**
	 * This method calculates the energy of the distance from the specified
	 * edge crossing any other edges. Each crossing add a constant factor
	 * to the total energy
	 *
	 * @param i the index of the edge in the array <code>e</code>
	 * @return the total edge crossing energy of the specified edge
	 */
	protected float getEdgeCrossing(int i) {
		// TODO Could have a cost function per edge
		int n = 0; // counts energy of edgecrossings through edge i

		// max and min variable for minimum bounding rectangles overlapping
		// checks

		CellWrapper[] v = this.v;

		if (isOptimizeEdgeCrossing) {
			CellWrapper ei = e.get(i);// e = this.e;
			int eis = ei.source;
			float iP1X = v[eis].x;
			float iP1Y = v[eis].y;
			int eit = ei.target;
			float iP2X = v[eit].x;
			float iP2Y = v[eit].y;

			for (int j = 0; j < e.size(); j++) {
				CellWrapper ej = e.get(j);// e = this.e;

				int ejs = ej.source;
				float jP1X = v[ejs].x;
				float jP1Y = v[ejs].y;
				int ejt = ej.target;
				float jP2X = v[ejt].x;
				float jP2Y = v[ejt].y;
				if (j != i) {
					// First check is to see if the minimum bounding rectangles
					// of the edges overlap at all. Since the layout tries
					// to separate nodes and shorten edges, the majority do not
					// overlap and this is a cheap way to avoid most of the
					// processing
					// Some long code to avoid a Math.max call...
					float maxiX;
					float miniX;
					if (iP1X < iP2X) {
						miniX = iP1X;
						maxiX = iP2X;
					} else {
						miniX = iP2X;
						maxiX = iP1X;
					}
					float maxjX;
					float minjX;
					if (jP1X < jP2X) {
						minjX = jP1X;
						maxjX = jP2X;
					} else {
						minjX = jP2X;
						maxjX = jP1X;
					}
					if (maxiX < minjX || miniX > maxjX) {
						continue;
					}

					float maxiY;
					float miniY;
					if (iP1Y < iP2Y) {
						miniY = iP1Y;
						maxiY = iP2Y;
					} else {
						miniY = iP2Y;
						maxiY = iP1Y;
					}
					float maxjY;
					float minjY;
					if (jP1Y < jP2Y) {
						minjY = jP1Y;
						maxjY = jP2Y;
					} else {
						minjY = jP2Y;
						maxjY = jP1Y;
					}
					if (maxiY < minjY || miniY > maxjY) {
						continue;
					}

					// Ignore if any end points are coincident
					if (((iP1X != jP1X) && (iP1Y != jP1Y))
							&& ((iP1X != jP2X) && (iP1Y != jP2Y))
							&& ((iP2X != jP1X) && (iP2Y != jP1Y))
							&& ((iP2X != jP2X) && (iP2Y != jP2Y))) {
						// Values of zero returned from Line2D.relativeCCW are
						// ignored because the point being exactly on the line
						// is very rare for float and we've already checked if
						// any end point share the same vertex. Should zero
						// ever be returned, it would be the vertex connected
						// to the edge that's actually on the edge and this is
						// dealt with by the node to edge distance cost
						// function. The worst case is that the vertex is
						// pushed off the edge faster than it would be
						// otherwise. Because of ignoring the zero this code
						// below can behave like only a 1 or -1 will be
						// returned. See Lines2D.linesIntersects().
						boolean intersects = ((Line2D.relativeCCW(iP1X, iP1Y,
								iP2X, iP2Y, jP1X, jP1Y) != Line2D.relativeCCW(
								iP1X, iP1Y, iP2X, iP2Y, jP2X, jP2Y)) && (Line2D
								.relativeCCW(jP1X, jP1Y, jP2X, jP2Y, iP1X, iP1Y) != Line2D
								.relativeCCW(jP1X, jP1Y, jP2X, jP2Y, iP2X, iP2Y)));

						if (intersects) {
							n++;
						}
					}
				}
			}
		}
		return edgeCrossingCostFactor * n;
	}


	public static float ptSegDistSq(float var0, float var2, float var4, float var6, float var8, float var10) {
		var4 -= var0;
		var6 -= var2;
		var8 -= var0;
		var10 -= var2;
		float var12 = var8 * var4 + var10 * var6;
		float var14;
		if (var12 <= 0.0f) {
			var14 = 0.0f;
		} else {
			var8 = var4 - var8;
			var10 = var6 - var10;
			var12 = var8 * var4 + var10 * var6;
			var14 = var12 <= 0.0f ? 0.0f : var12 * var12 / (var4 * var4 + var6 * var6);
		}

		float var16 = var8 * var8 + var10 * var10 - var14;
		if (var16 < 0.0f) {
			var16 = 0.0f;
		}

		return var16;
	}


	/**
	 * This method calculates the energy of the distance between Cells and
	 * Edges. This version of the edge distance cost calculates the energy
	 * cost from a specified <strong>node</strong>. The distance cost to all
	 * unconnected edges is calculated and the total returned.
	 *
	 * @param i the index of the node in the array <code>v</code>
	 * @return the total edge distance energy of the node
	 */
	protected float getEdgeDistanceFromNode(int i) {
		float energy = 0.0f;
		// This function is only performed during fine tuning for performance
		if (isOptimizeEdgeDistance && isFineTuning) {
			CellWrapper<TermNode> vi = v[i];

			IntArrayList edges = vi.relevantEdges;
			for (int j = 0; j < edges.size(); j++) {
				// Note that the distance value is squared
				CellWrapper ejj = e.get(edges.get(j));
				CellWrapper<TermNode> vejjs = v[ejj.source];
				CellWrapper<TermNode> vejjt = v[ejj.target];

				float distSquare = ptSegDistSq(
						vejjs.x,
						vejjs.y, vejjt.x,
						vejjt.y, vi.x, vi.y);

				distSquare -= vi.radiusSquared;

				// prevents from dividing with Zero. No Math.abs() call
				// for performance
				if (distSquare < minDistanceLimitSquared) {
					distSquare = minDistanceLimitSquared;
				}

				// Only bother with the divide if the node and edge are
				// fairly close together
				if (distSquare < maxDistanceLimitSquared) {
					energy += edgeDistanceCostFactor / distSquare;
				}
			}
		}
		return energy;
	}

	/**
	 * Obtains the energy cost function for the specified node being moved.
	 * This involves calling <code>getEdgeDistanceFromEdge</code> for all
	 * edges connected to the specified node
	 *
	 * @param node the node whose connected edges cost functions are to be
	 *             calculated
	 * @return the total edge distance energy of the connected edges
	 */
	protected float getEdgeDistanceAffectedNodes(int node) {
		float energy = 0.0f;
		IntArrayList vce = v[node].connectedEdges;
		for (int i = 0; i < (vce.size()); i++) {
			energy += getEdgeDistanceFromEdge(vce.get(i));
		}

		return energy;
	}

	/**
	 * This method calculates the energy of the distance between Cells and
	 * Edges. This version of the edge distance cost calculates the energy
	 * cost from a specified <strong>edge</strong>. The distance cost to all
	 * unconnected nodes is calculated and the total returned.
	 *
	 * @param i the index of the edge in the array <code>e</code>
	 * @return the total edge distance energy of the edge
	 */
	protected float getEdgeDistanceFromEdge(int i) {
		float energy = 0.0f;
		// This function is only performed during fine tuning for performance
		if (isOptimizeEdgeDistance && isFineTuning) {
			for (int j = 0; j < v.length; j++) {
				// Don't calculate for connected nodes
				CellWrapper<TermEdge> ei = e.get(i);
				if (ei.source != j && ei.target != j) {
					CellWrapper vs = v[ei.source];
					CellWrapper vt = v[ei.target];
					CellWrapper<TermNode> vj = v[j];
					float distSquare = ptSegDistSq(vs.x,
							vs.y, vt.x,
							vt.y, vj.x, vj.y);

					distSquare -= vj.radiusSquared;

					// prevents from dividing with Zero. No Math.abs() call
					// for performance
					if (distSquare < minDistanceLimitSquared)
						distSquare = minDistanceLimitSquared;

					// Only bother with the divide if the node and edge are
					// fairly close together
					if (distSquare < maxDistanceLimitSquared) {
						energy += edgeDistanceCostFactor / distSquare;
					}
				}
			}
		}
		return energy;
	}

	/**
	 * Hook method to adding additional energy factors into the layout.
	 * Calculates the energy just for the specified node.
	 *
	 * @param i the nodes whose energy is being calculated
	 * @return the energy of this node caused by the additional factors
	 */
	protected static float getAdditionFactorsEnergy(int i) {
		return 0.0f;
	}

	/**
	 * Returns all Edges that are not connected to the specified cell
	 *
	 * @param cellIndex the cell index to which the edges are not connected
	 * @return Array of all interesting Edges
	 */
	protected IntArrayList getRelevantEdges(int cellIndex, IntArrayList buffer) {
		if (buffer == null) {
			buffer = new IntArrayList(e.size());
		} else {
			buffer.clear();
		}


		for (int i = 0; i < e.size(); i++) {
			CellWrapper ei = e.get(i);
			if (ei.source != cellIndex && ei.target != cellIndex) {
				// Add non-connected edges
				buffer.add(i);
			}
		}

		return buffer;
	}

	/**
	 * Returns all Edges that are connected with the specified cell
	 *
	 * @param cellIndex the cell index to which the edges are connected
	 * @return Array of all connected Edges
	 */
	protected IntArrayList getConnectedEdges(int cellIndex) {
		IntArrayList connectedEdgeList = new IntArrayList(e.size());

		for (int i = 0; i < e.size(); i++) {
			CellWrapper ei = e.get(i);
			if (ei.source == cellIndex || ei.target == cellIndex) {
				// Add connected edges to list by their index number
				connectedEdgeList.add(i);
			}
		}

//		int[] connectedEdgeArray = new int[connectedEdgeList.size()];
//		Iterator<Integer> iter = connectedEdgeList.iterator();
//
//		// Reform the list into an array but replace Integer values with ints
//		for (int i = 0; i < connectedEdgeArray.length; i++)
//		{
//			if (iter.hasNext())
//			{
//				connectedEdgeArray[i] = iter.next().intValue();
//				;
//			}
//		}

		return connectedEdgeList;
	}


	/**
	 * Internal representation of a node or edge that holds cached information
	 * to enable the layout to perform more quickly and to simplify the code
	 */
	static final class CellWrapper<O> {

		/**
		 * The actual graph cell this wrapper represents
		 */
		protected final O cell;

		/**
		 * All edge that repel this cell, only used for nodes. This array
		 * is equivalent to all edges unconnected to this node
		 */
		protected IntArrayList relevantEdges = null;

		/**
		 * the index of all connected edges in the <code>e</code> array
		 * to this node. This is only used for nodes.
		 */
		protected IntArrayList connectedEdges = null;

		/**
		 * The x-coordinate position of this cell, nodes only
		 */
		protected float x = 0.0F;

		/**
		 * The y-coordinate position of this cell, nodes only
		 */
		protected float y = 0.0F;

		/**
		 * The approximate radius squared of this cell, nodes only. If
		 * approxNodeDimensions is true on the layout this value holds the
		 * width of the node squared
		 */
		protected float radiusSquared = 0.0F;

		/**
		 * The height of the node squared, only used if approxNodeDimensions
		 * is set to true.
		 */
		protected float heightSquared = 0.0F;

		/**
		 * The index of the node attached to this edge as source, edges only
		 */
		protected int source = 0;

		/**
		 * The index of the node attached to this edge as target, edges only
		 */
		protected int target = 0;

		/**
		 * Constructs a new CellWrapper
		 *
		 * @param cell the graph cell this wrapper represents
		 */
		public CellWrapper(O cell) {
			this.cell = cell;
		}

		/**
		 * @return the relevantEdges
		 */
		public IntArrayList getRelevantEdges() {
			return relevantEdges;
		}

		/**
		 * @param relevantEdges the relevantEdges to set
		 */
		public void setRelevantEdges(IntArrayList relevantEdges) {
			this.relevantEdges = relevantEdges;
		}

		/**
		 * @return the connectedEdges
		 */
		public IntArrayList getConnectedEdges() {
			return connectedEdges;
		}

		/**
		 * @param connectedEdges the connectedEdges to set
		 */
		public void setConnectedEdges(IntArrayList connectedEdges) {
			this.connectedEdges = connectedEdges;
		}

		/**
		 * @return the x
		 */
		public float getX() {
			return x;
		}

		/**
		 * @param x the x to set
		 */
		public void setX(float x) {
			this.x = x;
		}

		/**
		 * @return the y
		 */
		public float getY() {
			return y;
		}

		/**
		 * @param y the y to set
		 */
		public void setY(float y) {
			this.y = y;
		}

		/**
		 * @return the radiusSquared
		 */
		public float getRadiusSquared() {
			return radiusSquared;
		}

		/**
		 * @param radiusSquared the radiusSquared to set
		 */
		public void setRadiusSquared(float radiusSquared) {
			this.radiusSquared = radiusSquared;
		}

		/**
		 * @return the heightSquared
		 */
		public float getHeightSquared() {
			return heightSquared;
		}

		/**
		 * @param heightSquared the heightSquared to set
		 */
		public void setHeightSquared(float heightSquared) {
			this.heightSquared = heightSquared;
		}

		/**
		 * @return the source
		 */
		public int getSource() {
			return source;
		}

		/**
		 * @param source the source to set
		 */
		public void setSource(int source) {
			this.source = source;
		}

		/**
		 * @return the target
		 */
		public int getTarget() {
			return target;
		}

		/**
		 * @param target the target to set
		 */
		public void setTarget(int target) {
			this.target = target;
		}

		/**
		 * @return the cell
		 */
		public O getCell() {
			return cell;
		}
	}

}