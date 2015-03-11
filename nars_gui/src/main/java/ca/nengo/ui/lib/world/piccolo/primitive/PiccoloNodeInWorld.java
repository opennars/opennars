package ca.nengo.ui.lib.world.piccolo.primitive;

import ca.nengo.ui.lib.world.WorldObject;

public interface PiccoloNodeInWorld {
	public WorldObject getWorldObject();

	public boolean isAnimating(long now);

	public void setWorldObject(WorldObject worldObjectParent);
}
