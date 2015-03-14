package ca.nengo.ui.lib.world;

import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.lib.world.piccolo.primitive.PXEdge;

/**
 * A Layer of the world
 * 
 * @author Shu Wu
 */
public interface WorldLayer extends WorldObject {
	/**
	 * @param child
	 *            Child node to add
	 */
	public void addChild(WorldObject child);

	public void addEdge(PXEdge edge);

	/**
	 * @return World which this layer belongs to
	 */
	public WorldImpl getWorld();

	/**
	 * @return A Collection of windows
	 */
	public Iterable<Window> getWindows();

	/**
	 * @param world
	 *            The world
	 */
	public void setWorld(WorldImpl world);

	/**
	 * Clears the layer of all children
	 */
	public void clearLayer();

}
