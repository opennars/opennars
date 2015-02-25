package ca.nengo.ui.lib.world.piccolo.object;

import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;

/**
 * A World Object which does nothing but wrap another world object to add a
 * layer of indirection.
 * 
 * @author Shu Wu
 */
public class Wrapper extends WorldObjectImpl {
	private WorldObject myPackage;

	public Wrapper(WorldObject obj) {
		super();
		setPickable(false);
		setPackage(obj);
	}

	public WorldObject getPackage() {
		return myPackage;
	}

	public final void setPackage(WorldObject obj) {
		if (myPackage != null) {
			myPackage.removeFromParent();
		}
		WorldObject oldPackage = myPackage;

		myPackage = obj;

		if (obj != null) {
			addChild(obj);
			packageChanged(oldPackage);
		}
	}

	public void destroyPackage() {
		if (myPackage != null) {
			myPackage.destroy();
			myPackage = null;
		}
	}

	protected void packageChanged(WorldObject oldPackage) {

	}

}
