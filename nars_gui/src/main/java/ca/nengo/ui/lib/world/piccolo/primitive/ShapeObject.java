package ca.nengo.ui.lib.world.piccolo.primitive;

import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.util.RegularPolygon;

import java.awt.*;
import java.awt.geom.Point2D;

public class ShapeObject extends WorldObjectImpl {
	public static ShapeObject createEllipse(float x, float y, float width, float height) {
		return new ShapeObject(PXPath.createEllipse(x, y, width, height));
	}

	public static ShapeObject createLine(float x1, float y1, float x2, float y2) {
		return new ShapeObject(PXPath.createLine(x1, y1, x2, y2));
	}

	public static ShapeObject createPolyline(float[] xp, float[] yp) {
		return new ShapeObject(PXPath.createPolyline(xp, yp));
	}

	public static ShapeObject createPolyline(Point2D[] points) {
		return new ShapeObject(PXPath.createPolyline(points));
	}

	public static ShapeObject createRectangle(float x, float y, float width, float height) {
		return new ShapeObject(PXPath.createRectangle(x, y, width, height));
	}

	private PXPath pathNode;

	public ShapeObject(PXPath path) {
		super(path);
		init();
	}

	public ShapeObject() {
		super(new PXPath());
		init();
	}

	protected void init() {
        pathNode = (PXPath) getPNode();
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

    public static ShapeObject createPolygon(int sides, float radius, Color c) {
        ShapeObject p = new ShapeObject(new PXPath(new RegularPolygon(0,0,radius,sides)));
        p.setPaint(c);
        p.setStroke(null);
        return p;
    }

    public PXPath getGeometry() { return pathNode; }

    @Override
    public String toString() {
        return name() + ":Path[" + pnode + ']';
    }
}
