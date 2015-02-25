package ca.nengo.ui.lib.world.piccolo.icon;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class ZoomIcon extends LayoutIconBase {
    public ZoomIcon(int size) {
        super(size);
    }

    @Override
    protected void paintIcon(Graphics2D g2) {
        float rectangleSize = getSize() - PADDING * 2;
        float ticklen = rectangleSize * 0.3f;
        g2.drawRect(PADDING, PADDING, (int) rectangleSize, (int) rectangleSize);
        // Double sided diagonal arrow
        float arrowpad = PADDING + STROKE_WIDTH + 1;
        rectangleSize = getSize() - arrowpad * 2;
        GeneralPath path = new GeneralPath();
        // ticks
        path.moveTo(arrowpad, rectangleSize + arrowpad);
        path.lineTo(arrowpad, rectangleSize + arrowpad - ticklen);
        path.moveTo(arrowpad, rectangleSize + arrowpad);
        path.lineTo(arrowpad + ticklen, rectangleSize + arrowpad);
        // line
        path.moveTo(arrowpad, rectangleSize + arrowpad);
        path.lineTo(rectangleSize + arrowpad, arrowpad);
        // ticks
        path.lineTo(rectangleSize + arrowpad, arrowpad + ticklen);
        path.moveTo(rectangleSize + arrowpad, arrowpad);
        path.lineTo(rectangleSize + arrowpad - ticklen, arrowpad);
        g2.draw(path);
    }
}