package automenta.vivisect.swing.property.sheet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;


public class ResizeLayout extends BorderLayout {

	private static final long serialVersionUID = -1227060876626317222L;

	public ResizeLayout() {
		super(0, 0);
	}

	@Override
	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			for (int i = 0; i < target.getComponentCount(); i++) {
				Component c = target.getComponent(i);
				Point p = c.getLocation();
				int dx = Math.abs(p.x);
				int dy = Math.abs(p.y);
				int w = target.getWidth();
				int h = target.getHeight();
				c.setBounds(p.x, p.y, w + 2 * dx, h + 2 * dy);
			}
		}
	}
}