package ca.nengo.ui.lib.world;

/**
 * An object which can be dropped by a handler
 * 
 * @author Shu Wu
 */
public interface Droppable {

	public void justDropped();

	public boolean acceptTarget(WorldObject target);

}
