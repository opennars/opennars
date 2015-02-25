package ca.nengo.ui.lib.world;

import ca.nengo.ui.lib.action.UserCancelledException;

import java.util.Collection;

/**
 * An object which accepts a list of drop targets. It differs from IDroppable in
 * that the Drag Handler does add any drop behavior.
 * 
 * @author Shu Wu
 */
public interface DroppableX {

	public void droppedOnTargets(Collection<WorldObject> targets) throws UserCancelledException;

}
