package ca.nengo.ui.lib.world;

public interface Destroyable {
	/**
	 * Call this method if this Object does not need to be used again. The
	 * Object will prepare itself for garbage collection. Note: calling this
	 * method does not mean that the object WILL be garbage collected. That will
	 * not happen as long as there are external links to this object.
	 */
	public void destroy();
}
