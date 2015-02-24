package ca.nengo.ui.lib.world.elastic;

import ca.nengo.ui.lib.util.ElasticLayout;
import ca.nengo.ui.lib.util.ElasticLayout.LengthFunction;
import ca.nengo.ui.lib.util.Util;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.SparseGraph;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;

public class ElasticLayoutRunner {
	/**
	 * Used to determine when to pause the algorithm
	 */
	public static final double RELAX_DELTA = 2;
	public static final float SPRING_LAYOUT_FORCE_MULTIPLIER = 1f / 3f;
	public static final int SPRING_LAYOUT_DEFAULT_LENGTH = 300;
	public static final int SPRING_LAYOUT_DEFAULT_REPULSION_DISTANCE = 200;
	private int relaxCount;
	private boolean continueLayout = true;

	private ElasticLayout layout;

	private SparseGraph myGraph;

	private final ElasticGround myParent;

	public ElasticLayoutRunner(ElasticGround world) {
		super();
		this.myParent = world;
		init();
	}

	static class ElasticLengthFunction implements LengthFunction {

		public double getLength(Edge e) {
			if (e.containsUserDatumKey(ElasticGround.ELASTIC_LENGTH_KEY)) {
				return (Double) e.getUserDatum(ElasticGround.ELASTIC_LENGTH_KEY);
			} else {
				return SPRING_LAYOUT_DEFAULT_LENGTH;
			}
		}

		public double getMass(Vertex v) {
			if (v instanceof ElasticVertex) {
				return ((ElasticVertex) v).getRepulsionRange();
			}

			return SPRING_LAYOUT_DEFAULT_REPULSION_DISTANCE;
		}

	}

	private void init() {

		myParent.updateGraph();
		myGraph = myParent.getGraph();
		this.layout = new ElasticLayout(myGraph, new ElasticLengthFunction());
		layout.setForceMultiplier(SPRING_LAYOUT_FORCE_MULTIPLIER);
		layout.initialize();

		for (Object obj : myGraph.getVertices()) {
			ElasticVertex vertex = (ElasticVertex) obj;
			Point2D vertexLocation = vertex.getLocation();
			layout.forceMove(vertex, vertexLocation.getX(), vertexLocation.getY());
		}

	}

	private void runLayout() {

		while (!layout.incrementsAreDone() && !myParent.isDestroyed() && continueLayout) {

			/**
			 * Layout nodes needs to be done in the Swing dispatcher thread
			 */
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						updateLayout();
					}
				});
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(1000 / 25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (layout.incrementsAreDone()) {
			Util.Assert(false, "Iterable layout is done, this shouldn't be possible");
		}
	}

	public void updateLayout() {
		if (checkLayout()) {
			myParent.updateChildrenFromLayout(layout, false, false);
		}
	}

	private boolean checkLayout() {

		ElasticGround.UpdateGraphResult result = myParent.updateGraph();

		if (result.isGraphUpdated()) {
			layout.update();

			// update new vertex positions
			for (ElasticVertex vertex : result.getAddedVertices()) {
				layout.forceMove(vertex, vertex.getLocation().getX(), vertex.getLocation().getY());
			}

			relaxCount = 0;
		}

		boolean isResting = false;

		if (relaxCount >= 50) {
			relaxCount = 50;
			isResting = true;
		}
		if (!isResting) {
			layout.advancePositions();

			// Check to see if the elastic graph has settled in a certain
			// position
			double maxDelta = 0;
			for (Object obj : myGraph.getVertices()) {
				ElasticVertex vertex = (ElasticVertex) obj;
				Point2D vertexLocation = vertex.getLocation();
				Point2D layoutLocation = layout.getLocation(vertex);

				double delta = Math.abs(vertexLocation.distance(layoutLocation));

				if (delta > maxDelta) {
					maxDelta = delta;
				}
			}

			if (maxDelta < RELAX_DELTA) {
				relaxCount++;
			}
		}
		return !isResting;
	}

	public void start() {
		Thread myLayoutThread = new Thread(new Runnable() {
			public void run() {
				runLayout();
			}
		}, "Elastic layout runner");

		// myLayoutThread.setPriority(Thread.NORM_PRIORITY);
		myLayoutThread.start();
	}

	public void stopLayout() {
		continueLayout = false;
	}

	public void forceMove(Vertex picked, double x, double y) {
		relaxCount = 0;
		layout.forceMove(picked, x, y);
	}

	public boolean isLocked(Vertex v) {
		return layout.isLocked(v);
	}

	public Point2D getLocation(ArchetypeVertex v) {
		return layout.getLocation(v);
	}

	public boolean isLockedVertex(Vertex v) {
		return layout.isLocked(v);
	}

	public void lockVertex(Vertex v) {
		layout.lockVertex(v);
	}

	public void unlockVertex(Vertex v) {
		layout.unlockVertex(v);
	}
}