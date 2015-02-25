package ca.nengo.ui.lib.world.piccolo.icon;

import java.awt.*;

public class RestoreIcon extends WindowIconBase {
	public RestoreIcon(int size) {
		super(size);
	}

	@Override
	protected void paintIcon(Graphics2D g2) {
		int rectangleSize = getSize() - PADDING * 2;
		g2.drawRect(PADDING, PADDING + rectangleSize / 2 - 1, rectangleSize, rectangleSize / 2 + 1);
	}
}