package ca.nengo.ui.lib.world.handler;

import ca.nengo.ui.lib.world.WorldObject;
import org.piccolo2d.PCamera;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PPanEventHandler;
import org.piccolo2d.util.PBounds;
import org.piccolo2d.util.PDimension;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Iterator;


/**
 * Extend PPanEventHandler so that panning direction can be inverted
 * 
 * @author Shu Wu
 */
public class PanEventHandler extends PPanEventHandler {

	private boolean isInverted = false;
	private SelectionHandler selectionHandler = null;
    private PInputEvent lastEvent;

    public PanEventHandler() {
		super();
	}

	/**
	 * Do auto panning even when the mouse is not moving.
	 */
	protected void dragActivityStep(PInputEvent aEvent) {
        if (!getAutopan())
			return;

        //if (aEvent == lastEvent) return; //avoid duplicate events
        //lastEvent = aEvent;

        //aEvent.setHandled(true);

		PCamera c = aEvent.getCamera();
		PBounds b = c.getBoundsReference();
		Point2D l = aEvent.getPositionRelativeTo(c);
		int outcode = b.outcode(l);
		PDimension delta = new PDimension();

		if ((outcode & Rectangle.OUT_TOP) != 0) {
			delta.height = validatePanningSpeed(-1.0
					- (0.5 * Math.abs(l.getY() - b.getY())));
		} else if ((outcode & Rectangle.OUT_BOTTOM) != 0) {
			delta.height = validatePanningSpeed(1.0 + (0.5 * Math.abs(l.getY()
					- (b.getY() + b.getHeight()))));
		}

		if ((outcode & Rectangle.OUT_RIGHT) != 0) {
			delta.width = validatePanningSpeed(1.0 + (0.5 * Math.abs(l.getX()
					- (b.getX() + b.getWidth()))));
		} else if ((outcode & Rectangle.OUT_LEFT) != 0) {
			delta.width = validatePanningSpeed(-1.0
					- (0.5 * Math.abs(l.getX() - b.getX())));
		}

		c.localToView(delta);


		if (delta.width != 0 || delta.height != 0) {
			if (isInverted) {
				c.translateView(-1 * delta.width, -1 * delta.height);
			} else {
				c.translateView(delta.width, delta.height);
			}
		}



        // Loop through selected objects, compensate for camera panning
		// so that objects will remain stationary relative to cursor
		Iterator<WorldObject> selectionEn = selectionHandler.getSelection().iterator();
		while (selectionEn.hasNext()) {
			WorldObject node = selectionEn.next();
			node.localToParent(node.globalToLocal(delta));
			node.dragOffset(delta.getWidth(), delta.getHeight());
		}

    }

	public void setInverted(boolean isInverted) {
		this.isInverted = isInverted;
	}

	public boolean isInverted() {
		return isInverted;
	}
	
	public void setSelectionHandler(SelectionHandler s) {
		selectionHandler = s;
	}
	
	public SelectionHandler getSelectionHandler() {
		return selectionHandler;
	}
}
