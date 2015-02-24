/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */
package ca.nengo.ui.lib.util;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.visualization.LayoutMutable;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * The SpringLayout package represents a visualization of a set of nodes. The
 * SpringLayout, which is initialized with a Graph, assigns X/Y locations to
 * each node. When called <code>relax()</code>, the SpringLayout moves the
 * visualization forward one step.
 * <p>
 * Modified by ShuWu to dimensionless layout
 * </p>
 * 
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
public class ElasticLayout extends AbstractLayout implements LayoutMutable {

	private static final Object SPRING_KEY = "temp_edu.uci.ics.jung.Spring_Visualization_Key";
	public static final LengthFunction UNITLENGTHFUNCTION = new UnitLengthFunction(30);
	protected double force_multiplier = 1.0 / 3.0;
	protected final LengthFunction lengthFunction;

	protected double stretch = 0.70;

	Object key = null;

	/**
	 * Constructor for a SpringLayout for a raw graph with associated
	 * dimension--the input knows how big the graph is. Defaults to the unit
	 * length function.
	 */
	public ElasticLayout(Graph g) {
		this(g, UNITLENGTHFUNCTION);
	}

	/**
	 * Constructor for a SpringLayout for a raw graph with associated component.
	 * 
	 * @param g
	 *            the input Graph
	 * @param f
	 *            the length function
	 */
	public ElasticLayout(Graph g, LengthFunction f) {
		super(g);
		this.lengthFunction = f;
	}

	protected void calcEdgeLength(SpringEdgeData sed, LengthFunction f) {
		sed.length = f.getLength(sed.e);
	}

	protected void calculateRepulsion() {
		try {
			for (Iterator<?> iter = getGraph().getVertices().iterator(); iter.hasNext();) {
				Vertex v = (Vertex) iter.next();
				if (isLocked(v))
					continue;

				SpringVertexData svd = getSpringData(v);
				if (svd == null)
					continue;
				double dx = 0, dy = 0;

				for (Iterator<?> iter2 = getGraph().getVertices().iterator(); iter2.hasNext();) {
					Vertex v2 = (Vertex) iter2.next();
					if (v == v2)
						continue;
					Point2D p = getLocation(v);
					Point2D p2 = getLocation(v2);
					if (p == null || p2 == null)
						continue;
					double vx = p.getX() - p2.getX();
					double vy = p.getY() - p2.getY();
					double distance = vx * vx + vy * vy;
					if (distance == 0) {
						dx += Math.random();
						dy += Math.random();
					} else if (distance < lengthFunction.getMass(v2) * lengthFunction.getMass(v)) {
						double forceFactor = lengthFunction.getMass(v2);
						
						//
						// Normalize the force to a standard mass unit of 200
						forceFactor /= 200;

						dx += forceFactor * vx / Math.pow(distance, 2);
						dy += forceFactor * vy / Math.pow(distance, 2);
					}
				}
				double dlen = dx * dx + dy * dy;
				if (dlen > 0) {
					dlen = Math.sqrt(dlen) / 2;
					svd.repulsiondx += dx / dlen;
					svd.repulsiondy += dy / dlen;
				}
			}
		} catch (ConcurrentModificationException cme) {
			calculateRepulsion();
		}
	}

	protected Vertex getAVertex(Edge e) {
		Vertex v = (Vertex) e.getIncidentVertices().iterator().next();
		return v;
	}

	protected void initialize_local() {
		try {
			for (Iterator<?> iter = getGraph().getEdges().iterator(); iter.hasNext();) {
				Edge e = (Edge) iter.next();
				SpringEdgeData sed = getSpringData(e);
				if (sed == null) {
					sed = new SpringEdgeData(e);
					e.addUserDatum(getSpringKey(), sed, UserData.REMOVE);
				}
				calcEdgeLength(sed, lengthFunction);
			}
		} catch (ConcurrentModificationException cme) {
			initialize_local();
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.ics.jung.visualization.AbstractLayout#initialize_local_vertex(edu.uci.ics.jung.graph.Vertex)
	 */
	protected void initialize_local_vertex(Vertex v) {
		SpringVertexData vud = getSpringData(v);
		if (vud == null) {
			vud = new SpringVertexData();
			v.addUserDatum(getSpringKey(), vud, UserData.REMOVE);
		}
	}

	@Override
	protected void initializeLocation(Vertex v, Coordinates coord, Dimension d) {
		// do nothing
	}

	protected void moveNodes() {

		try {
			for (Iterator<?> i = getVisibleVertices().iterator(); i.hasNext();) {
				Vertex v = (Vertex) i.next();
				if (isLocked(v))
					continue;
				SpringVertexData vd = getSpringData(v);
				if (vd == null)
					continue;
				Coordinates xyd = getCoordinates(v);

				vd.dx += vd.repulsiondx + vd.edgedx;
				vd.dy += vd.repulsiondy + vd.edgedy;

				// keeps nodes from moving any faster than 5 per time unit
				xyd.addX(Math.max(-5, Math.min(5, vd.dx)));
				xyd.addY(Math.max(-5, Math.min(5, vd.dy)));

			}
		} catch (ConcurrentModificationException cme) {
			moveNodes();
		}

	}

	protected void relaxEdges() {
		try {
			for (Iterator<?> i = getVisibleEdges().iterator(); i.hasNext();) {
				Edge e = (Edge) i.next();

				Vertex v1 = getAVertex(e);
				Vertex v2 = e.getOpposite(v1);

				Point2D p1 = getLocation(v1);
				Point2D p2 = getLocation(v2);
				if (p1 == null || p2 == null)
					continue;
				double vx = p1.getX() - p2.getX();
				double vy = p1.getY() - p2.getY();
				double len = Math.sqrt(vx * vx + vy * vy);

				SpringEdgeData sed = getSpringData(e);
				if (sed == null) {
					continue;
				}
				double desiredLen = sed.length;

				// round from zero, if needed [zero would be Bad.].
				len = (len == 0) ? .0001 : len;

				double f = force_multiplier * (desiredLen - len) / len;

				// f = f * Math.pow(stretch, (v1.degree() + v2.degree() - 2));
				// shuwu: reduced the degree attenuation with a sqrt dampener
				f = f * Math.pow(stretch, Math.sqrt((v1.degree() + v2.degree() - 2)));

				// the actual movement distance 'dx' is the force multiplied by
				// the
				// distance to go.
				double dx = f * vx;
				double dy = f * vy;
				SpringVertexData v1D, v2D;
				v1D = getSpringData(v1);
				v2D = getSpringData(v2);

				sed.f = f;

				v1D.edgedx += dx;
				v1D.edgedy += dy;
				v2D.edgedx += -dx;
				v2D.edgedy += -dy;
			}
		} catch (ConcurrentModificationException cme) {
			relaxEdges();
		}
	}

	/**
	 * Relaxation step. Moves all nodes a smidge.
	 */
	public void advancePositions() {
		try {
			for (Iterator<?> iter = getVisibleVertices().iterator(); iter.hasNext();) {
				Vertex v = (Vertex) iter.next();
				SpringVertexData svd = getSpringData(v);
				if (svd == null) {
					continue;
				}
				svd.dx /= 4;
				svd.dy /= 4;
				svd.edgedx = svd.edgedy = 0;
				svd.repulsiondx = svd.repulsiondy = 0;
			}
		} catch (ConcurrentModificationException cme) {
			advancePositions();
		}

		relaxEdges();
		calculateRepulsion();
		moveNodes();
	}

	/**
	 * @return the current value for the edge length force multiplier
	 * @see #setForceMultiplier(double)
	 */
	public double getForceMultiplier() {
		return force_multiplier;
	}

	public double getLength(Edge e) {
		return ((SpringEdgeData) e.getUserDatum(getSpringKey())).length;
	}

	/* ------------------------- */

	/* ------------------------- */

	public SpringEdgeData getSpringData(Edge e) {
		try {
			return (SpringEdgeData) (e.getUserDatum(getSpringKey()));
		} catch (ClassCastException cce) {
			System.out.println(e.getUserDatum(getSpringKey()).getClass());
			throw cce;
		}
	}

	public SpringVertexData getSpringData(Vertex v) {
		return (SpringVertexData) (v.getUserDatum(getSpringKey()));
	}

	public Object getSpringKey() {
		if (key == null)
			key = new Pair(this, SPRING_KEY);
		return key;
	}

	/**
	 * Returns the status.
	 */
	public String getStatus2() {
		return null;
	}

	/**
	 * @return the current value for the stretch parameter
	 * @see #setStretch(double)
	 */
	public double getStretch() {
		return stretch;
	}

	/**
	 * For now, we pretend it never finishes.
	 */
	public boolean incrementsAreDone() {
		return false;
	}

	/**
	 * New initializer for layout of unbounded size
	 */
	public void initialize() {

		initialize_local();
		initializeLocations();
	}

	/**
	 * This one is an incremental visualization
	 */
	public boolean isIncremental() {
		return true;
	}

	/* ---------------Length Function------------------ */

	/**
	 * Sets the force multiplier for this instance. This value is used to
	 * specify how strongly an edge "wants" to be its default length (higher
	 * values indicate a greater attraction for the default length), which
	 * affects how much its endpoints move at each timestep. The default value
	 * is 1/3. A value of 0 turns off any attempt by the layout to cause edges
	 * to conform to the default length. Negative values cause long edges to get
	 * longer and short edges to get shorter; use at your own risk.
	 */
	public void setForceMultiplier(double force) {
		this.force_multiplier = force;
	}

	/**
	 * <p>
	 * Sets the stretch parameter for this instance. This value specifies how
	 * much the degrees of an edge's incident vertices should influence how
	 * easily the endpoints of that edge can move (that is, that edge's tendency
	 * to change its length).
	 * </p>
	 * <p>
	 * The default value is 0.70. Positive values less than 1 cause high-degree
	 * vertices to move less than low-degree vertices, and values > 1 cause
	 * high-degree vertices to move more than low-degree vertices. Negative
	 * values will have unpredictable and inconsistent results.
	 * </p>
	 * 
	 * @param stretch
	 */
	public void setStretch(double stretch) {
		this.stretch = stretch;
	}

	/* ---------------User Data------------------ */

	/**
	 * @see edu.uci.ics.jung.visualization.LayoutMutable#update()
	 */
	public void update() {
		try {
			for (Iterator<?> iter = getGraph().getVertices().iterator(); iter.hasNext();) {
				Vertex v = (Vertex) iter.next();
				Coordinates coord = (Coordinates) v.getUserDatum(getBaseKey());
				if (coord == null) {
					coord = new Coordinates();
					v.addUserDatum(getBaseKey(), coord, UserData.REMOVE);
					initializeLocation(v, coord, getCurrentSize());
					initialize_local_vertex(v);
				}
			}
		} catch (ConcurrentModificationException cme) {
			update();
		}
		initialize_local();
	}

	protected static class SpringEdgeData {

		public double f;

		final Edge e;

		double length;

		public SpringEdgeData(Edge e) {
			this.e = e;
		}
	}

	/* ---------------Resize handler------------------ */

	protected static class SpringVertexData {

		/** movement speed, x */
		public double dx;

		/** movement speed, y */
		public double dy;

		public double edgedx;

		public double edgedy;

		public double repulsiondx;

		public double repulsiondy;

		public SpringVertexData() {
		}
	}

	/**
	 * If the edge is weighted, then override this method to show what the
	 * visualized length is.
	 * 
	 * @author Danyel Fisher
	 */
	public static interface LengthFunction {

		public double getLength(Edge e);

		public double getMass(Vertex v);
	}

	public class SpringDimensionChecker extends ComponentAdapter {

		public void componentResized(ComponentEvent e) {
			resize(e.getComponent().getSize());
		}
	}

	/**
	 * Returns all edges as the same length: the input value
	 * 
	 * @author danyelf
	 */
	public static final class UnitLengthFunction implements LengthFunction {

		final int length;

		public UnitLengthFunction(int length) {
			this.length = length;
		}

		public double getLength(Edge e) {
			return length;
		}

		public double getMass(Vertex v) {
			return 100;
		}
	}

}