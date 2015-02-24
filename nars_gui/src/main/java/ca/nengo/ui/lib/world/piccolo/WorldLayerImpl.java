package ca.nengo.ui.lib.world.piccolo;

import ca.nengo.ui.lib.world.WorldLayer;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.objects.Window;
import ca.nengo.ui.lib.world.piccolo.primitives.PiccoloNodeInWorld;

import java.util.ArrayList;
import java.util.List;

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

	public List<Window> getWindows() {
		ArrayList<Window> windows = new ArrayList<Window>(5);
		for (WorldObject wo : getChildren()) {
			if (wo instanceof Window) {
				windows.add((Window) wo);
			}
		}
		return windows;
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
