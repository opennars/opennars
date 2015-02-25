package ca.nengo.ui.lib.world.piccolo.icon;

import java.awt.*;

public class MaximizeIcon extends WindowIconBase {
	public MaximizeIcon(int size) {
		super(size);
	}

	@Override
	protected void paintIcon(Graphics2D g2) {
		int rectangleSize = getSize() - PADDING * 2;
		g2.drawRect(PADDING, PADDING, rectangleSize, rectangleSize);
	}
}