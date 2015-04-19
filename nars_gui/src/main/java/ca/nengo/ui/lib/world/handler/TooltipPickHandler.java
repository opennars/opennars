package ca.nengo.ui.lib.world.handler;

import ca.nengo.ui.lib.AppFrame;
import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.WorldLayer;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.object.SelectionBorder;
import ca.nengo.ui.lib.world.piccolo.object.TooltipWrapper;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.lib.world.piccolo.primitive.PiccoloNodeInWorld;
import org.piccolo2d.PNode;
import org.piccolo2d.event.PInputEvent;

/**
 * Picks objects in which to show tooltips for. Handles both both and keyboard
 * events.
 * 
 * @author Shu Wu
 */
public class TooltipPickHandler extends AbstractPickHandler {
	public static final String TOOLTIP_BORDER_ATTR = "tooltipBdr";

	//private WorldObject controls;

	private WorldObject keyboardFocusObject;

	private TooltipWrapper keyboardTooltip;
	private TooltipWrapper mouseOverTooltip;

	private final SelectionBorder tooltipFrame;

	//private final int myPickDelay;
    //private final int myKeepPickDelay;

	public TooltipPickHandler(WorldImpl world, int pickDelay, int keepPickDelay) {
		super(world);
		//myPickDelay = pickDelay;
		//myKeepPickDelay = keepPickDelay;
		tooltipFrame = new SelectionBorder(world);
		tooltipFrame.setFrameColor(NengoStyle.COLOR_TOOLTIP_BORDER);

	}

	private void processKeyboardEvent(PInputEvent event) {
		if ((event.getModifiers() & AppFrame.MENU_SHORTCUT_KEY_MASK) != 0) {

			WorldObject wo = getTooltipNode(event);
			if (wo != null) {
				setKeyboardTooltipFocus(wo);

			}
		} else {
			setKeyboardTooltipFocus(null);
		}

	}

	protected WorldObject getTooltipNode(PInputEvent event) {

		PNode node = event.getPickedNode();
		while (node != null) {

			if (node instanceof PiccoloNodeInWorld) {
				WorldObject wo = ((PiccoloNodeInWorld) node).getWorldObject();

				/*
				 * Do nothing if the mouse is over the controls
				 */
				//if (node == controls) {
					//setKeepPickAlive(true);
				//	return null;
				/*} else */
                if (wo instanceof WorldLayer || wo instanceof Window) {
					break;
				} else if (wo!=null && wo.getVisible() && wo.getTooltip() != null) {
					//return null; // hack to eliminate tool tips
					return wo;
				}

			}

			node = node.getParent();
		}
		//setKeepPickAlive(false);
		return null;

	}

	@Override
	protected void nodePicked() {
        if (mouseOverTooltip!=null) {
            mouseOverTooltip.fadeAndDestroy();
            mouseOverTooltip = null;
        }

		WorldObject node = getPickedNode();
		if (tooltipFrame.setSelected(node)) {
            mouseOverTooltip = getWorld().showTooltip(node);
        }
	}

	@Override
	protected void nodeUnPicked() {

		tooltipFrame.setSelected(null);

        if (mouseOverTooltip!=null) {
            mouseOverTooltip.fadeAndDestroy();
            mouseOverTooltip = null;
        }

	}

	@Override
	protected void processMouseEvent(PInputEvent event) {
		WorldObject node = null;

		processKeyboardEvent(event);
		if (WorldImpl.isTooltipsVisible()) {
			node = getTooltipNode(event);
		} else {
			//setKeepPickAlive(false);
		}
		setSelectedNode(node);
		if (node == null) {

			setSelectedNode(null);
		}

	}

	protected void setKeyboardTooltipFocus(WorldObject wo) {
		if (wo != keyboardFocusObject) {
			keyboardFocusObject = wo;

			if (keyboardTooltip != null) {
				keyboardTooltip.fadeAndDestroy();
				keyboardTooltip = null;
			}

			if (keyboardFocusObject != null) {
				keyboardTooltip = getWorld().showTooltip(wo);
			}

		}
	}

	@Override
	public void keyPressed(PInputEvent event) {
		super.keyPressed(event);
		processKeyboardEvent(event);
	}

	@Override
	public void keyReleased(PInputEvent event) {
		super.keyReleased(event);
		processKeyboardEvent(event);
	}

//    @Override
//    protected int getKeepPickDelay() {
//        return myKeepPickDelay;
//    }
//
//    @Override
//    protected int getPickDelay() {
//        return myPickDelay;
//    }
//

}
