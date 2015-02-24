package ca.nengo.ui.lib.world;

import javax.swing.*;

/**
 * Objects which can be interacted with through context menus
 * 
 * @author Shu
 */
public interface Interactable extends WorldObject {

	/**
	 * @param event
	 *            The input event triggering the context menu
	 * @return context menu associated to the Named Object
	 */
	public JPopupMenu getContextMenu();

}
