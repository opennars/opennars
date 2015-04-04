package ca.nengo.ui.lib.world.handler;

import ca.nengo.ui.lib.AppFrame;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.Universe;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;


/**
 * Handles events which change the Application status bar
 * 
 * @author Shu Wu
 */
public abstract class AbstractStatusHandler extends PBasicInputEventHandler {
	private final WorldImpl world;

	/**
	 * @param world
	 *            World this handler belongs to
	 */
	public AbstractStatusHandler(WorldImpl world) {
		super();
		this.world = world;
	}

	/**
	 * @param event
	 *            Input event
	 * @return Message to show on the status bar
	 */
	protected abstract String getStatusMessage(PInputEvent event);

	/**
	 * @return World this handler belongs to
	 */
	protected WorldImpl getWorld() {
		return world;
	}

	@Override
	public void mouseMoved(PInputEvent event) {
		super.mouseMoved(event);

		AppFrame env = UIEnvironment.getInstance();
		if (env!=null) {

			Universe u = env.getUniverse();
			if (u!=null) {
				u.setStatusMessage(getStatusMessage(event));
			}

		}



	}
}
