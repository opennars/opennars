package ca.nengo.ui.lib.world.piccolo.primitives;

import ca.nengo.ui.lib.world.WorldObject;
import org.piccolo2d.PLayer;

public class PXLayer extends PLayer implements PiccoloNodeInWorld {

	private static final long serialVersionUID = 1L;

	private WorldObject wo;

	public WorldObject getWorldObject() {
		return wo;
	}

	public boolean isAnimating() {
		return false;
	}

	public void setWorldObject(WorldObject worldObjectParent) {
		wo = worldObjectParent;

	}

}
