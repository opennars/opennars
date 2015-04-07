package ca.nengo.ui.lib.world.handler;

import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.world.Destroyable;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;


/**
 * Handles key inputs
 * 
 * @author Shu Wu
 */
public class KeyboardHandler extends PBasicInputEventHandler implements Destroyable {

	public KeyboardHandler() {
		super();
	}

	@Override
	public void keyPressed(PInputEvent event) {
//		if (NengoClassic.getInstance().isScriptConsoleVisible()
//				&& !event.isControlDown() && !event.isMetaDown()
//				&& event.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
//			// letter key press, package as KeyEvent and pass to ScriptConsole
//			KeyEvent e = new KeyEvent(AbstractNengo.getInstance(), 0, System.currentTimeMillis(),
//									  event.getModifiers(), event.getKeyCode(), event.getKeyChar() );
//            NengoClassic.getInstance().getScriptConsole().passKeyEvent( e );

		if (event.isShiftDown()) {
			// shift down
			UIEnvironment.getInstance().getUniverse().setSelectionMode(true);
		}

	}

	@Override
	public void keyReleased(PInputEvent event) {
		if (!event.isShiftDown()) {
			try {
				UIEnvironment.getInstance().getUniverse().setSelectionMode(false);
			}
			  catch (NullPointerException e) {System.err.println("isShiftDown #HACK: " + e);}

		}
		super.keyReleased(event);
	}

	@Override
	public void mousePressed(PInputEvent event) {
		super.mousePressed(event);
	}

	public void destroy() {
		//do nothing
	}
}
