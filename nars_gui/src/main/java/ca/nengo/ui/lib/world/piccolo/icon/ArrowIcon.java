package ca.nengo.ui.lib.world.piccolo.icon;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class ArrowIcon extends LayoutIconBase {
    public ArrowIcon(int size) {
        super(size);
    }

    @Override
    protected void paintIcon(Graphics2D g2) {
        float rectangleSize = getSize() - PADDING * 2.0f;
        float pad = PADDING;
        GeneralPath path = new GeneralPath();
        path.moveTo(pad, rectangleSize / 2.0f + pad);
        path.lineTo(rectangleSize + pad, rectangleSize / 2.0f + pad);

        // up tick
        path.lineTo(rectangleSize / 1.5f + pad, pad * 2);

        // down tick
        path.moveTo(rectangleSize + pad, rectangleSize / 2.0f + pad);
        path.lineTo(rectangleSize / 1.5f + pad, rectangleSize);
        g2.draw(path);
    }
}