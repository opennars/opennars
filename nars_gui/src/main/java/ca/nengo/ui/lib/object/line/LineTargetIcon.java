package ca.nengo.ui.lib.object.line;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * Standard Icon for a line end holder
 * 
 * @author Shu Wu
 */
public class LineTargetIcon extends WorldObjectImpl {

	static final int LINE_IN_HEIGHT = 30;

	static final int LINE_IN_WIDTH = 30;
    private static final Area a1 = new Area(new Ellipse2D.Double(0, 0, LINE_IN_WIDTH,
            LINE_IN_HEIGHT));
    static {
        a1.exclusiveOr(new Area(new Ellipse2D.Double(5.0, 5.0,
                LINE_IN_WIDTH - 10.0, LINE_IN_HEIGHT - 10.0)));
    }

    private Color myColor = NengoStyle.COLOR_LINEIN;

	public LineTargetIcon() {
		super();
		this.setBounds(0, 0, LINE_IN_WIDTH, LINE_IN_HEIGHT);


    }

	@Override
	public void paint(PaintContext paintContext) {
		super.paint(paintContext);

		Graphics2D g2 = paintContext.getGraphics();
		g2.setColor(getColor());
		g2.fill(a1);
	}

	public Color getColor() {
		return myColor;
	}

	public void setColor(Color color) {
		this.myColor = color;
	}

}
