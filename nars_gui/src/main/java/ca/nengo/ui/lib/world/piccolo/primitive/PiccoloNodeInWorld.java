package ca.nengo.ui.lib.world.piccolo.primitive;

import ca.nengo.ui.lib.world.WorldObject;

public interface PiccoloNodeInWorld {
	public WorldObject getWorldObject();

	public boolean isAnimating();

	public void setWorldObject(WorldObject worldObjectParent);
}
