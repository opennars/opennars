package ca.nengo.ui.lib.object.line;

import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.util.PPickPath;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class LineWell extends WorldObjectImpl {
	private final LineSourceIcon myIcon;

	public LineWell() {
		super();
		myIcon = new LineSourceIcon();

		addChild(myIcon);
		setBounds(getFullBoundsClone());
		myIcon.addInputEventListener(new CreateLineEndHandler(this));
	}

	/**
	 * @return The new LineEnd which has been created and added to the
	 *         LineEndWell
	 */
	abstract protected LineConnector createProjection();
    /*{
		LineConnector newLineEnd = createProjection();
		addChild(newLineEnd);
		return newLineEnd;
	}*/

	public Color getColor() {
		return myIcon.getColor();
	}

	public void setColor(Color color) {
		myIcon.setColor(color);
	}

}

/**
 * This handler listens for mouse events on the line end well and creates new
 * line ends when needed.
 * 
 * @author Shu Wu
 */
class CreateLineEndHandler extends PBasicInputEventHandler {

	private final LineWell lineEndWell;

	private LineConnector newLineEnd;

	public CreateLineEndHandler(LineWell lineEndWell) {
		super();
		this.lineEndWell = lineEndWell;

	}

	@Override
	public void mousePressed(PInputEvent event) {

		super.mousePressed(event);

		if (event.getButton() != MouseEvent.BUTTON1)
			return;

		newLineEnd = lineEndWell.createProjection();

		PPickPath path = event.getPath();

        path.pushNode(newLineEnd.getPNode());
        path.pushTransform(newLineEnd.getPNode().getTransform());

	}

}
