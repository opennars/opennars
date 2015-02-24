package ca.nengo.ui.lib.world.elastic;

import edu.uci.ics.jung.graph.impl.LeanSparseVertex;

import java.awt.geom.Point2D;

public class ElasticVertex extends LeanSparseVertex {
	private final ElasticObject myObject;

	public ElasticVertex(ElasticObject nodeUI) {
		super();
		this.myObject = nodeUI;
	}

	public Point2D getLocation() {
		return myObject.getOffsetReal();
	}

	public double getRepulsionRange() {
		return myObject.getRepulsionRange();
	}

}
