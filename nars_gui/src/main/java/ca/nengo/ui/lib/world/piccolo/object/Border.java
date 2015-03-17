package ca.nengo.ui.lib.world.piccolo.object;

import ca.nengo.ui.lib.world.WorldObject.Listener;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.ShapeObject;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Adds a border around an object.
 * 
 * @author Shu Wu
 */
public class Border extends WorldObjectImpl implements Listener {

	private final Color myColor;
	private final ShapeObject myFrame;
	private final WorldObjectImpl myTarget;

	/**
	 * Create a new border
	 * 
	 * @param target
	 *            Object to create a border around
	 * @param color
	 *            Color of border
	 */
	public Border(WorldObjectImpl target, Color color) {
		super();
		this.myColor = color;
		this.myTarget = target;

		myFrame = ShapeObject.createRectangle(0f, 0f, 1f, 1f);
		setPickable(false);
		setChildrenPickable(false);

		addChild(myFrame);

		myTarget.addPropertyChangeListener(Property.BOUNDS_CHANGED, this);

		updateBorder();
	}

	@Override
	protected void prepareForDestroy() {
		/*
		 * Remove listener from target
		 */
		myTarget.removePropertyChangeListener(Property.BOUNDS_CHANGED, this);

		super.prepareForDestroy();
	}

	/**
	 * Updates the border when the target bounds changes
	 */
	protected void updateBorder() {
		if (myTarget != null) {

			Rectangle2D bounds = myTarget.getBounds();

			myFrame.setBounds((float) bounds.getX(), (float) bounds.getY(), (float) bounds
					.getWidth(), (float) bounds.getHeight());
			myFrame.setPaint(null);
			myFrame.setStrokePaint(myColor);
		}
	}

	public void propertyChanged(Property event) {
		updateBorder();
	}

}
