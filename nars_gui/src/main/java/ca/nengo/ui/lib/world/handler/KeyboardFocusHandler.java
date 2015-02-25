package ca.nengo.ui.lib.world.handler;


import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;

/**
 * Focuses the Keyboard handler on the event Pick Path when the mouse enters a a
 * new path.
 * 
 * @author Shu Wu
 */
public class KeyboardFocusHandler extends PBasicInputEventHandler {

	@Override
	public void mouseEntered(PInputEvent event) {
		event.getInputManager().setKeyboardFocus(event.getPath());
	}
}
