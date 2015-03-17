package ca.nengo.ui.lib.world.elastic;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.Destroyable;
import ca.nengo.ui.lib.world.WorldLayer;
import ca.nengo.ui.lib.world.WorldObject.Listener;
import ca.nengo.ui.lib.world.WorldObject.Property;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.ShapeObject;
import ca.nengo.ui.lib.world.piccolo.primitive.PiccoloNodeInWorld;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class ElasticObject extends WorldObjectImpl {

	// Cache the Elastic world for fast access because it is used often
	private ElasticGround elasticGround;

	private boolean isAnchored = false;
	private static final double DEFAULT_REPULSION_DISTANCE = 150;
	private double repulsionRange;

	/**
	 * Counts the number of times the position has been locked
	 */
	private int positionLock = 0;

	public ElasticObject() {
		super();
		init();
	}

	public ElasticObject(PiccoloNodeInWorld node) {
		super(node);
		init();
	}

	public ElasticObject(String name) {
		super(name);
		init();
	}

	public ElasticObject(String name, PiccoloNodeInWorld node) {
		super(name, node);
		init();
	}

	private void init() {

		addPropertyChangeListener(Property.PARENTS_CHANGED, new Listener() {
			public void propertyChanged(Property event) {
				if (getParent() instanceof ElasticGround) {
					elasticGround = (ElasticGround) getParent();
				}
			}
		});

		addPropertyChangeListener(Property.BOUNDS_CHANGED, new Listener() {
			public void propertyChanged(Property event) {
				recalculateRepulsionRange();
			}
		});
		recalculateRepulsionRange();
	}

	private void recalculateRepulsionRange() {
		double range = DEFAULT_REPULSION_DISTANCE;

		Rectangle2D bounds = localToGlobal(getBounds());

		double radius = Math.sqrt(bounds.getWidth() * bounds.getWidth() + bounds.getHeight()
				* bounds.getHeight());

		range += radius;

		this.repulsionRange = range;
	}

	protected ElasticGround getElasticWorld() {
		return elasticGround;
	}

	@Override
	public Point2D getOffset() {
		if (getElasticWorld() != null) {
			return getElasticWorld().getElasticPosition(this);
		} else {
		}
		return super.getOffset();
	}

	/**
	 * This is the real getOffset function.
	 * 
	 * @param x
	 * @param y
	 */
	public Point2D getOffsetReal() {
		return super.getOffset();
	}

	public boolean isAnchored() {
		return isAnchored;
	}

	public boolean isPositionLocked() {
		if (elasticGround != null) {
			return elasticGround.isPositionLocked(this);
		}
		return false;
	}

	private Anchor anchor;

	public void setAnchored(boolean anchored) {
		if (isAnchored != anchored) {
			isAnchored = anchored;
			if (isAnchored) {
				if (anchor == null) {
					anchor = new Anchor(this);
				}

				setPositionLocked(true);
			} else {
				if (anchor != null) {
					anchor.destroy();
					anchor = null;
				}

				setPositionLocked(false);
			}
		}

	}

	/**
	 * @see edu.umd.cs.piccolo.PNode#setOffset(double, double)
	 *      <p>
	 *      If NetworkViewer exists as a parent, this becomes a re-direct to
	 *      Network Viewer's set location function.
	 *      </p>
	 */
	@Override
	public void setOffset(double x, double y) {
		if (getElasticWorld() != null) {
			getElasticWorld().setElasticPosition(this, x, y);
		} else {
			super.setOffset(x, y);
		}
	}

	/**
	 * This is the real setOffset function.
	 * 
	 * @param x
	 * @param y
	 */
	public void setOffsetReal(double x, double y) {
		super.setOffset(x, y);
	}

	public void setPositionLocked(boolean lock) {
		if (lock) {
			positionLock++;
		} else {
			positionLock--;
			if (positionLock < 0)
				positionLock = 0;
		}

		if (elasticGround != null) {
			if (positionLock > 0) {
				elasticGround.setPositionLocked(this, true);
			} else {
				elasticGround.setPositionLocked(this, false);
			}

		}
	}

	@Override
	public void setSelected(boolean isSelected) {
		super.setSelected(isSelected);
		setPositionLocked(isSelected);
	}

	@Override
	protected void prepareForDestroy() {
		if (anchor != null) {
			anchor.destroy();
			anchor = null;
		}
		super.prepareForDestroy();
	}

	public double getRepulsionRange() {
		return repulsionRange;
	}

}

class Anchor implements Destroyable, Listener {
	private final ElasticObject obj;
	private final ShapeObject border;
	private final ShapeObject line;

	public Anchor(ElasticObject obj) {
		super();
		this.obj = obj;
		WorldLayer ground = obj.getWorldLayer();

		border = ShapeObject.createRectangle(0, 0, 1, 1);
		line = new ShapeObject();
		// line.setStrokePaint(style.COLOR)
		border.setPaint(null);
		line.setPaint(null);

		border.setStroke(new BasicStroke(2f));
		line.setStroke(new BasicStroke(2f));
		border.setStrokePaint(NengoStyle.COLOR_ANCHOR);
		line.setStrokePaint(NengoStyle.COLOR_ANCHOR);

		ground.addChild(line);
		ground.addChild(border);
		updateBounds();
		obj.addPropertyChangeListener(Property.REMOVED_FROM_WORLD, this);
		obj.addPropertyChangeListener(Property.GLOBAL_BOUNDS, this);
	}

	private boolean destroyed;

	public void destroy() {
		if (!destroyed) {
			line.destroy();
			border.destroy();
			destroyed = true;
			obj.removePropertyChangeListener(Property.REMOVED_FROM_WORLD, this);
			obj.removePropertyChangeListener(Property.GLOBAL_BOUNDS, this);
		}
	}

	private static final int SIZE_ANCHOR = 15;

	private void updateBounds() {
		border.moveToFront();
		line.moveToFront();

		Rectangle2D bounds = obj.localToGlobal(obj.getBounds());
		border.setBounds(bounds);

		if (bounds.getWidth() > 0) {
			ArrayList<Point2D> points = new ArrayList<Point2D>(4);
			double x = bounds.getCenterX();
			double y = bounds.getMaxY();
			points.add(new Point2D.Double(x, y));
			y += SIZE_ANCHOR * (2f / 3f);
			points.add(new Point2D.Double(x, y));
			x -= SIZE_ANCHOR;
			points.add(new Point2D.Double(x, y));
			x += SIZE_ANCHOR * 2;
			points.add(new Point2D.Double(x, y));

			line.setPathToPolyline(points.toArray(new Point2D[points.size()]));
		}
	}

	public void propertyChanged(Property event) {
		if (event == Property.REMOVED_FROM_WORLD) {
			destroy();
		} else if (event == Property.GLOBAL_BOUNDS) {
			updateBounds();
		}
	}
}
