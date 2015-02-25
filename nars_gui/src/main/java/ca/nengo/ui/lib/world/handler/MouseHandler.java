package ca.nengo.ui.lib.world.handler;


import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.Interactable;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.object.SelectionBorder;
import ca.nengo.ui.lib.world.piccolo.primitive.PiccoloNodeInWorld;
import ca.nengo.ui.model.NodeContainer;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NetworkViewer;
import ca.nengo.ui.model.viewer.NodeViewer;
import org.piccolo2d.PNode;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Stack;

/**
 * Handles mouse events. Passes double click and mouse context button events to
 * World Objects. Displays a frame around interactable objects as the mouse
 * moves.
 * 
 * @author Shu Wu
 */
public class MouseHandler extends PBasicInputEventHandler {

	/**
	 * Hand cursor
	 */
	private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

	/**
	 * Maximum distance that the mouse is allowed to drag before the handler
	 * gives up on the context menu
	 */
	private static final double MAX_CONTEXT_MENU_DRAG_DISTANCE = 20;
	
	private static MouseHandler activeMouseHandler = null;
	public static MouseHandler getActiveMouseHandler() {
		return activeMouseHandler;
	}
	private static void setActiveMouseHandler(MouseHandler mh) {
		if (activeMouseHandler != mh)
			activeMouseHandler = mh;
	}

	///////////////////////////////////////////////////////////////////////////
	/// Members and constructor
	
	private final SelectionBorder frame;

	private boolean handCursorShown = false;

	private boolean mousePressedIsPopupTrigger = false;
	private Point2D mousePressedCanvasPosition;
	private Interactable mousePressedInteractableObj;
	
	private Point2D mouseMovedCanvasPosition = null;
	private Interactable mouseMovedInteractableObj = null;

	private final WorldImpl world;

	public MouseHandler(WorldImpl world) {
		super();
		frame = new SelectionBorder(world);
		frame.setFrameColor(NengoStyle.COLOR_TOOLTIP_BORDER);
		this.world = world;
	}
	
	///////////////////////////////////////////////////////////////////////////
	/// Accessors and mutators
	
	public WorldImpl getWorld() {
		return this.world;
	}

	private WorldImpl getMouseMovedWorldImpl() {
		if (mouseMovedInteractableObj != null &&
			mouseMovedInteractableObj instanceof WorldImpl)
			return (WorldImpl)mouseMovedInteractableObj;
		else
			return null;
	}
	public Point2D getMouseMovedRelativePosition() {
		WorldImpl obj = getMouseMovedWorldImpl();
		if (obj != null)
			return getRelativePosition(obj, mouseMovedCanvasPosition);
		else
			return null;
	}
	
	///////////////////////////////////////////////////////////////////////////
	/// Events

	@Override
	public void mouseClicked(PInputEvent event) {
		boolean altClicked = false;
		boolean doubleClicked = false;

		if (event.isAltDown()) {
			altClicked = true;
		} else if (event.getClickCount() == 2) {
			doubleClicked = true;
		}

		if (altClicked || doubleClicked) {
			PNode node = event.getPickedNode();
			while (node != null) {
				if (node instanceof PiccoloNodeInWorld) {

					WorldObject wo = ((PiccoloNodeInWorld) node).getWorldObject();

					if (wo.isSelectable()) {
						if (doubleClicked) {
							wo.doubleClicked();
						}
						if (altClicked) {
							wo.altClicked();
						}
						break;
					}

				}
				node = node.getParent();
			}
		}
		super.mouseClicked(event);

	}

	@Override
	public void mouseMoved(PInputEvent event) {

		Interactable obj = getInteractableFromEvent(event);
		mouseMovedInteractableObj = obj;
		mouseMovedCanvasPosition = event.getCanvasPosition();
		setActiveMouseHandler(this);

		
		// Show cursor and frame around interactable objects NOTE: Do not show
		// cursor and frame around Windows or Worlds
		if (obj == null || (obj instanceof Window) || (obj instanceof WorldImpl)) {
			if (handCursorShown) {
				handCursorShown = false;
				event.getComponent().popCursor();
			}

			frame.setSelected(null);
		} else {
			if (!handCursorShown) {
				handCursorShown = true;
				event.getComponent().pushCursor(HAND_CURSOR);
			}

			frame.setSelected(obj);
		}
	}
	
	@Override
	public void mousePressed(PInputEvent event) {
		super.mousePressed(event);

		// get information for popup
		mousePressedIsPopupTrigger = event.isPopupTrigger();
		mousePressedCanvasPosition = event.getCanvasPosition();
		mousePressedInteractableObj = getInteractableFromEvent(event);
		
		setActiveMouseHandler(this);
	}

	@Override
	public void mouseReleased(PInputEvent event) {
		super.mouseReleased(event);

		// On Linux, only the mousePressed registers as a popupTrigger, and 
		// on Windows, only the mouseReleased, so check both
		if (mousePressedIsPopupTrigger || event.isPopupTrigger()) {
			mousePressedIsPopupTrigger = false;
			
			// Check the mouse hasn't moved too far off from it's pressed position
			double dist = mousePressedCanvasPosition.distance(event.getCanvasPosition());
			if (dist < MAX_CONTEXT_MENU_DRAG_DISTANCE) {
				triggerPopup(event);
			}
		}
	}
	
	/**
	 * @param event
	 * @return Was Popup Triggered?
	 */
	private void triggerPopup(PInputEvent event) {
		JPopupMenu menuToShow = null;
		MouseEvent e = (MouseEvent) event.getSourceSwingEvent();
		
		if (world.getSelection().size() > 1) {
			menuToShow = world.getSelectionMenu(world.getSelection());
		} else if (mousePressedInteractableObj != null &&
				   mousePressedInteractableObj == getInteractableFromEvent(event)) {
			if (mousePressedInteractableObj instanceof WorldImpl) {
				// determine the position to add the pasted object
				WorldImpl world = (WorldImpl)mousePressedInteractableObj;
				Point2D newPosition = getRelativePosition(world, mousePressedCanvasPosition);
				menuToShow = world.getContextMenu(newPosition.getX(), newPosition.getY());
			} else {
				menuToShow = mousePressedInteractableObj.getContextMenu();
			}
		}
		
		if (menuToShow != null) {
			menuToShow.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
			menuToShow.setVisible(true);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	/// Helper methods
	
	/**
	 * @return Interactable object
	 */
	private Interactable getInteractableFromEvent(PInputEvent event) {
		Interactable obj = (Interactable) Util.getNodeFromPickPath(event, Interactable.class);

		if (obj == null || !world.isAncestorOf(obj)) {
			return null;
		} else {
			return obj;
		}
	}
	
	private static Point2D getRelativePosition(WorldImpl world, Point2D globalPosition) {
		// We need to loop through each sub-network to transform
		// the point to each sub-network's coordinate system
		WorldObject obj = world;
		
		// First determine the path from the root node to the sub-network
		// into which we are pasting
		Stack<WorldObject> objStack = new Stack<WorldObject>();
		while (obj != null) {
			objStack.push(obj);
			if (obj instanceof NetworkViewer) {
				UINetwork nViewer = ((NetworkViewer) obj).getViewerParent();
				UINetwork v = nViewer.getNetworkParent();
				if (v != null)
					obj = v.getViewer();
				else
					obj = AbstractNengo.getInstance().getWorld();
			} else {
				obj = null;
			}
		}
		
		// Now loop through the stack of nodes and transform the point
		// into the proper network's coordinate system
		Point2D newPosition = globalPosition;
		while (!objStack.empty()) {
			obj = objStack.pop();
			newPosition = obj.globalToLocal(newPosition);
			if (obj instanceof NodeViewer)
				newPosition = ((NodeViewer)obj).localToView(newPosition);
			else
				newPosition = ((NodeContainer)obj).localToView(newPosition);
		}
		
		return newPosition;
	}
}
