package ca.nengo.ui.lib.world.piccolo.primitives;

import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;

import java.awt.*;
import java.awt.geom.Point2D;

public class Path extends WorldObjectImpl {
	public static Path createEllipse(float x, float y, float width, float height) {
		return new Path(PXPath.createEllipse(x, y, width, height));
	}

	public static Path createLine(float x1, float y1, float x2, float y2) {
		return new Path(PXPath.createLine(x1, y1, x2, y2));
	}

	public static Path createPolyline(float[] xp, float[] yp) {
		return new Path(PXPath.createPolyline(xp, yp));
	}

	public static Path createPolyline(Point2D[] points) {
		return new Path(PXPath.createPolyline(points));
	}

	public static Path createRectangle(float x, float y, float width, float height) {
		return new Path(PXPath.createRectangle(x, y, width, height));
	}

	private PXPath pathNode;

	private Path(PXPath path) {
		super(path);
		init();
	}

	public Path() {
		super(new PXPath());
		init();
	}

	private void init() {
		pathNode = (PXPath) getPiccolo();
		setPickable(false);
	}

	public Paint getStrokePaint() {
		return pathNode.getStrokePaint();
	}

	public void setStroke(Stroke stroke) {
		pathNode.setStroke(stroke);
	}

	public void setStrokePaint(Paint paint) {
		pathNode.setStrokePaint(paint);
	}

	public void setPathToPolyline(Point2D[] points) {
		pathNode.setPathToPolyline(points);
	}
}
