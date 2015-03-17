package ca.nengo.ui.lib.world.piccolo.object;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.piccolo.primitive.ShapeObject;
import ca.nengo.ui.lib.world.piccolo.primitive.Text;

import java.awt.*;

/**
 * A Button whose represntation is a text label
 * 
 * @author Shu Wu
 */
public class TextButton extends AbstractButton {

	private static final int BORDER_HEIGHT = 3;

	private static final int BORDER_WIDTH = 5;

	private final ShapeObject frame;

	private final Text myTextNode;

	/**
	 * @param textLabel
	 *            Button label
	 * @param action
	 *            Action to execute when the button is pressed
	 */
	public TextButton(String textLabel, Runnable action) {
		super(action);

		myTextNode = new Text("");
		myTextNode.setOffset(BORDER_WIDTH, BORDER_HEIGHT);
		myTextNode.setFont(NengoStyle.FONT_BUTTONS);
		myTextNode.setTextPaint(NengoStyle.COLOR_FOREGROUND);

		frame = ShapeObject.createRectangle(0, 0, 100, 100);
		frame.setStrokePaint(NengoStyle.COLOR_BUTTON_BORDER);

		addChild(frame);
		addChild(myTextNode);

		setText(textLabel);

		stateChanged();
	}

	public ShapeObject getFrame() {
		return frame;
	}

	public Text getText() {
		return myTextNode;
	}

	public void updateBounds() {

		frame.setBounds(0f, 0f,
				(float) (myTextNode.getWidth() + 2 * BORDER_WIDTH),
				(float) (myTextNode.getHeight() + 2 * BORDER_HEIGHT));
		setBounds(frame.getBounds());
	}

	public void setFont(Font font) {
		myTextNode.setFont(font);
		updateBounds();
	}

	public void setText(String textLabel) {
		myTextNode.setText(textLabel);
		updateBounds();
	}

	@Override
	public void stateChanged() {
		ButtonState buttonState = getState();

		switch (buttonState) {
		case DEFAULT:
			frame.setPaint(getDefaultColor());
			break;
		case HIGHLIGHT:
			frame.setPaint(getHighlightColor());
			break;
		case SELECTED:
			frame.setPaint(getSelectedColor());
			break;
		}
		repaint();
	}

	public void setStrokePaint(Paint paint) {
		frame.setStrokePaint(paint);
	}
}
