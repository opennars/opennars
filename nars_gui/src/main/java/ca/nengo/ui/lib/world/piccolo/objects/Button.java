package ca.nengo.ui.lib.world.piccolo.objects;

import ca.nengo.ui.lib.style.NengoStyle;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitives.Path;

/**
 * A button which draws itself as a WorldObject
 * 
 * @author Shu Wu
 */
public class Button extends AbstractButton {

	private final Path buttonCover;

	public Button(WorldObjectImpl worldObject, Runnable action) {
		super(action);

		addChild(worldObject);

		buttonCover = Path.createRectangle(0f, 0f, (float) worldObject.getWidth(), (float) worldObject.getHeight());
		buttonCover.setPaint(NengoStyle.COLOR_FOREGROUND);
		addChild(buttonCover);

		initDefaultState();
		this.setWidth(worldObject.getWidth());
		this.setHeight(worldObject.getHeight());
	}

	private void initDefaultState() {
		buttonCover.setTransparency(0f);
	}

	@Override
	public void stateChanged() {
		ButtonState state = getState();

		switch (state) {
		case DEFAULT:
			initDefaultState();
			break;
		case HIGHLIGHT:
			buttonCover.setTransparency(0.2f);
			break;
		case SELECTED:
			buttonCover.setTransparency(0.4f);
			break;
		}

	}

}
