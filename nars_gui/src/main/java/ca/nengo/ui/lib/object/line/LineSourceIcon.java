package ca.nengo.ui.lib.object.line;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;

import java.awt.*;

/**
 * Icon for a line end well
 * 
 * @author Shu Wu
 */
public class LineSourceIcon extends WorldObjectImpl {

	private static final int _LINE_END_HEIGHT = 30;

	private static final int _LINE_END_WIDTH = 30;

	protected static final double ICON_RADIUS = Math
			.sqrt((_LINE_END_WIDTH * _LINE_END_WIDTH)
					+ (_LINE_END_HEIGHT * _LINE_END_HEIGHT)) / 2;

	private Color color = NengoStyle.COLOR_FOREGROUND, bright2, dark, medium, bright1, hilite;

	public LineSourceIcon() {
		super();
		this.setBounds(-_LINE_END_WIDTH / 2, -_LINE_END_HEIGHT / 2, _LINE_END_WIDTH, _LINE_END_HEIGHT);
		setColor(NengoStyle.COLOR_LINEENDWELL);

	}

	@Override
	public void paint(PaintContext paintContext) {
		super.paint(paintContext);
		Graphics2D g2 = paintContext.getGraphics();
		//if (paintContext.getScale() < 0.5) {
			g2.setColor(color);
		/*} else {
            g2.setColor(dark);
        }*/
        g2.fillOval(-_LINE_END_WIDTH / 2, -_LINE_END_HEIGHT / 2, _LINE_END_WIDTH, _LINE_END_HEIGHT);

//
//			g2.setColor(medium);
//			g2.fillOval(_LINE_END_WIDTH / 4, 0, _LINE_END_WIDTH / 2,
//					_LINE_END_HEIGHT);
//
//			g2.setColor(bright1);
//			g2.fillOval(_LINE_END_WIDTH / 6, _LINE_END_HEIGHT / 2,
//					2 * _LINE_END_WIDTH / 3, _LINE_END_HEIGHT / 3);
//
//			g2.setColor(bright2);
//			g2.fillOval(_LINE_END_WIDTH / 6 + 2, _LINE_END_HEIGHT / 2 + 2,
//					2 * _LINE_END_WIDTH / 3 - 4, _LINE_END_HEIGHT / 3 - 2);
//
//			g2.setColor(hilite);
//			g2.fillOval(_LINE_END_WIDTH / 3 - 1, _LINE_END_HEIGHT / 6,
//					_LINE_END_WIDTH / 3 + 2, 3 * _LINE_END_HEIGHT / 16);

	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
        this.color = color;
        bright2 = NengoStyle.colorAdd(color, new Color(0.4f, 0.4f, 0.4f));
        dark = NengoStyle.colorAdd(NengoStyle.colorTimes(color, 0.65),
                new Color(0.05f, 0.05f, 0.05f));
        medium = color;
        bright1 = NengoStyle.colorAdd(color,
                new Color(0.15f, 0.15f, 0.15f));

        hilite = NengoStyle.colorAdd(NengoStyle.colorTimes(color, 0.5),
                new Color(0.5f, 0.5f, 0.5f));

	}

}
