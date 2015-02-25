package ca.nengo.ui.lib.world;

import ca.nengo.ui.lib.world.handler.SelectionHandler;

import java.awt.geom.Point2D;
import java.util.Collection;

public interface World extends WorldObject {
	public WorldLayer getGround();

	public WorldSky getSky();

	public Point2D skyToGround(Point2D position);

	public Collection<WorldObject> getSelection();
	public SelectionHandler getSelectionHandler();

	/**
	 * Animate the sky to view all object on the ground
	 * 
	 * @return reference to animation activity
	 */
	public void zoomToFit();

	/**
	 * @param object
	 *            Object to zoom to
	 * @return reference to animation activity
	 */
	public void zoomToObject(WorldObject object);
}
