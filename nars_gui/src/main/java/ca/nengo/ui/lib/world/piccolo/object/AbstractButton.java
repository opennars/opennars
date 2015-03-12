package ca.nengo.ui.lib.world.piccolo.object;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;

import javax.swing.*;
import java.awt.*;

/**
 * Button which executes an action on click
 * 
 * @author Shu Wu
 */
public abstract class AbstractButton extends WorldObjectImpl {

	private Color defaultColor = NengoStyle.COLOR_BUTTON_BACKGROUND;

	private Color highlightColor = NengoStyle.COLOR_BUTTON_HIGHLIGHT;

	private Runnable myAction;

	private ButtonState myState = ButtonState.DEFAULT;

	private Color selectedColor = NengoStyle.COLOR_BUTTON_SELECTED;

	/**
	 * @param action
	 *            Action to execute when the button is pressed
	 */
	public AbstractButton(Runnable action) {
		super();
		this.myAction = action;

		this.setChildrenPickable(false);

        getPNode().addInputEventListener(new HandCursorHandler());
        getPNode().addInputEventListener(new ButtonStateHandler(this));
	}

	protected void doAction() {
		if (myAction != null) {
			SwingUtilities.invokeLater(myAction);

		}
	}

	protected ButtonState getState() {

		return myState;
	}

	public Runnable getAction() {
		return myAction;
	}

	public Color getDefaultColor() {
		return defaultColor;
	}

	public Color getHighlightColor() {
		return highlightColor;
	}

	public Color getSelectedColor() {
		return selectedColor;
	}

	public void setAction(Runnable action) {
		this.myAction = action;
	}

	public void setButtonState(ButtonState pState) {
		myState = pState;
		stateChanged();
	}

	public void setDefaultColor(Color btnDefaultColor) {
		this.defaultColor = btnDefaultColor;
		setButtonState(myState);
	}

	public void setHighlightColor(Color btnHighlightColor) {
		this.highlightColor = btnHighlightColor;
		setButtonState(myState);
	}

	public void setSelectedColor(Color btnSelectedColor) {
		this.selectedColor = btnSelectedColor;
		setButtonState(myState);
	}

	public abstract void stateChanged();

	public static enum ButtonState {
		DEFAULT, HIGHLIGHT, SELECTED
	}

}

/**
 * Changes the button state from mouse events
 * 
 * @author Shu Wu
 */
class ButtonStateHandler extends PBasicInputEventHandler {
	private final AbstractButton button;

	public ButtonStateHandler(AbstractButton button) {
		super();
		this.button = button;
	}

	@Override
	public void mouseClicked(PInputEvent event) {
		button.doAction();

	}

	@Override
	public void mouseEntered(PInputEvent event) {
		button.setButtonState(AbstractButton.ButtonState.HIGHLIGHT);

	}

	@Override
	public void mouseExited(PInputEvent event) {
		button.setButtonState(AbstractButton.ButtonState.DEFAULT);
	}

	@Override
	public void mousePressed(PInputEvent event) {
		button.setButtonState(AbstractButton.ButtonState.SELECTED);
	}

	@Override
	public void mouseReleased(PInputEvent event) {
		button.setButtonState(AbstractButton.ButtonState.DEFAULT);
	}

}

/**
 * Changes the mouse cursor to a hand when it enters the object
 * 
 * @author Shu Wu
 */
class HandCursorHandler extends PBasicInputEventHandler {
	private Cursor handCursor;

	@Override
	public void mouseEntered(PInputEvent event) {
		if (handCursor == null) {
			handCursor = new Cursor(Cursor.HAND_CURSOR);
			event.getComponent().pushCursor(handCursor);
		}
	}

	@Override
	public void mouseExited(PInputEvent event) {
		if (handCursor != null) {
			handCursor = null;
			event.getComponent().popCursor();
		}
	}

}
