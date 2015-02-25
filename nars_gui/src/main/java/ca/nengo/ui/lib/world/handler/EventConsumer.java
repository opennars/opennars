package ca.nengo.ui.lib.world.handler;


import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PInputEventListener;

/**
 * Handler which consumes all events passed to it. Used when an object wants to
 * shield its events from the outside.
 * 
 * @author Shu Wu
 */
public class EventConsumer implements PInputEventListener {
	public void processEvent(PInputEvent aEvent, int type) {
		aEvent.setHandled(true);
	}
}
