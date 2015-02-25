package ca.nengo.ui.lib.world.piccolo.icon;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class LoadIcon extends LayoutIconBase {
    public LoadIcon(int size) {
        super(size);
    }

    @Override
    protected void paintIcon(Graphics2D g2) {
        float rectangleSize = getSize() - PADDING * 2;
        float pad = PADDING;

        // Line
        g2.drawLine(PADDING, (int) rectangleSize + PADDING,
                (int) rectangleSize + PADDING, (int) rectangleSize + PADDING);

        // Arrow
        GeneralPath path = new GeneralPath();
        path.moveTo(rectangleSize / 2.0f + pad, rectangleSize);
        path.lineTo(rectangleSize / 2.0f + pad, pad);

        // left tick
        path.lineTo(pad * 2, (getSize() / 2.0f) - 1);

        // right tick
        path.moveTo(rectangleSize / 2.0f + pad, pad);
        path.lineTo(rectangleSize, (getSize() / 2.0f) - 1);
        g2.draw(path);
    }
}