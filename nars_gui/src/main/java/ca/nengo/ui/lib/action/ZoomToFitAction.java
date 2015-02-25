package ca.nengo.ui.lib.action;

import ca.nengo.ui.lib.world.piccolo.WorldImpl;

/**
 * Action to zoom to fit
 * 
 * @author Shu Wu
 */
public class ZoomToFitAction extends StandardAction {

	private static final long serialVersionUID = 1L;

	final WorldImpl world;

	public ZoomToFitAction(String actionName, WorldImpl world) {
		super("Zoom to fit", actionName);
		this.world = world;
	}

	@Override
	protected void action() throws ActionException {
		if(world != null)
			world.zoomToFit();
	}

}