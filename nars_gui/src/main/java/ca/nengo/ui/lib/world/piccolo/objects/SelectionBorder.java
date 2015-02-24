package ca.nengo.ui.lib.world.piccolo.objects;

import ca.nengo.ui.lib.style.NengoStyle;
import ca.nengo.ui.lib.world.World;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.WorldObject.Listener;
import ca.nengo.ui.lib.world.WorldObject.Property;
import ca.nengo.ui.lib.world.WorldSky;
import ca.nengo.ui.lib.world.piccolo.primitives.Path;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A Border instance that can change its object of focus (As long as the object
 * are within the world that the frame is in). Border is attached to the sky
 * layer so there is no attenuation of the edge width when the ground is viewed
 * at a low scale.
 * 
 * @author Shu Wu
 */
public class SelectionBorder implements Listener {
	private Path frame;

	private Color frameColor = NengoStyle.COLOR_BORDER_SELECTED;

	private WorldSky frameHolder;

	private WorldObject selectedObj;

	/**
	 * @param world
	 *            World, whose sky, this border shall be added to.
	 */
	public SelectionBorder(World world) {
		super();
		init(world);
	}

	/**
	 * @param world
	 *            World, whose sky, this border shall be added to.
	 * @param objSelected
	 *            Object to select initially
	 */
	public SelectionBorder(World world, WorldObject objSelected) {
		super();
		init(world);
		setSelected(objSelected);
	}

	/**
	 * Initializes this instance
	 * 
	 * @param world
	 *            World, whose sky, this border shall be added to.
	 */
	private void init(World world) {
		this.frameHolder = world.getSky();
		frame = Path.createRectangle(0f, 0f, 1f, 1f);
		frame.setPickable(false);

		frameHolder.addPropertyChangeListener(Property.VIEW_TRANSFORM, this);

		frameHolder.addChild(frame);
	}

	/**
	 * Updates the bounds of the border to match those of the selected object
	 */
	protected void updateBounds() {
		if (selectedObj != null) {
			if (selectedObj.getVisible()) {
				Rectangle2D bounds = selectedObj.objectToSky(selectedObj.getBounds());

				frame.setBounds((float) bounds.getX(), (float) bounds.getY(), (float) bounds
						.getWidth(), (float) bounds.getHeight());
				frame.setPaint(null);
				frame.setStrokePaint(frameColor);
				frame.setVisible(true);
			} else {
				frame.setVisible(false);
			}
		} else {
			setSelected(null);
		}
	}

	public void destroy() {
		setSelected(null);

		frameHolder.removePropertyChangeListener(Property.VIEW_TRANSFORM, this);
	}

	public Color getFrameColor() {
		return frameColor;
	}

	public void propertyChanged(Property event) {
		if (event == Property.REMOVED_FROM_WORLD) {
			setSelected(null);
		}
		updateBounds();

	}

	public void setFrameColor(Color frameColor) {
		this.frameColor = frameColor;
		updateBounds();
	}

	public void setSelected(WorldObject newSelected) {
		if (newSelected == selectedObj) {
			return;
		}

		if (selectedObj != null) {
			selectedObj.removePropertyChangeListener(Property.GLOBAL_BOUNDS, this);
			selectedObj.removePropertyChangeListener(Property.REMOVED_FROM_WORLD, this);
			selectedObj = null;
		}

		selectedObj = newSelected;
		if (selectedObj != null) {

			selectedObj.addPropertyChangeListener(Property.GLOBAL_BOUNDS, this);
			selectedObj.addPropertyChangeListener(Property.REMOVED_FROM_WORLD, this);

			frameHolder.addChild(frame);
			updateBounds();
		} else {

			frame.removeFromParent();
		}

	}

}
