package ca.nengo.ui.lib.world;

import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.objects.Window;
import ca.nengo.ui.lib.world.piccolo.primitives.PXEdge;

import java.util.Collection;

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
	public Collection<Window> getWindows();

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
