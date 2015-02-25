package ca.nengo.ui.lib.action;

import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.world.WorldObject;

import javax.swing.*;
import java.util.Collection;

public class RemoveObjectsAction extends StandardAction {

	private static final long serialVersionUID = 1L;
	private final Collection<WorldObject> objectsToRemove;

	public RemoveObjectsAction(Collection<WorldObject> objectsToRemove, String actionName) {
		super(actionName);
		this.objectsToRemove = objectsToRemove;
	}

	@Override
	protected void action() throws ActionException {

		int response = JOptionPane.showConfirmDialog(UIEnvironment.getInstance(),
				"You are about to remove " + objectsToRemove.size() + " Objects.",
				"Continue?", JOptionPane.YES_NO_OPTION);

		if (response == JOptionPane.YES_OPTION) {

			for (WorldObject wo : objectsToRemove) {
				if (wo instanceof ModelObject) {
					/*
					 * If it's a model, make sure that's destroyed as well
					 */
					((ModelObject) wo).destroyModel();
				} else {
					/*
					 * Just destroy the UI representation
					 */
					wo.destroy();
				}
			}
		}
	}
}
