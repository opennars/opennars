package ca.nengo.ui.lib.world.piccolo;

import ca.nengo.ui.lib.world.WorldLayer;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.lib.world.piccolo.primitive.PiccoloNodeInWorld;
import com.google.common.collect.Iterables;

public abstract class WorldLayerImpl extends WorldObjectImpl implements WorldLayer {

	/**
	 * World this layer belongs to
	 */
	protected WorldImpl world;

	/**
	 * Create a new ground layer
	 *
	 *            World this layer belongs to
	 */
	public WorldLayerImpl(String name, PiccoloNodeInWorld node) {
		super(name, node);
	}

	public Iterable<Window> getWindows() {
        return Iterables.filter(getChildren(), Window.class);
	}

	/**
	 * Removes and destroys children
	 */
	public void clearLayer() {
		for (WorldObject wo : getChildren()) {
			wo.destroy();
		}
	}

	@Override
	public WorldImpl getWorld() {
		return world;
	}

	public void setWorld(WorldImpl world) {
		this.world = world;
	}

}
