package ca.nengo.ui.lib.world.piccolo.objects.icons;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class CloseIcon extends WindowIconBase {
    public CloseIcon(int size) {
        super(size);
    }

    @Override
    protected void paintIcon(Graphics2D g2) {
        int rectangleSize = getSize() - PADDING;
        GeneralPath path = new GeneralPath();
        path.moveTo(PADDING, PADDING);
        path.lineTo(rectangleSize, rectangleSize);
        path.moveTo(PADDING, rectangleSize);
        path.lineTo(rectangleSize, PADDING);
        g2.draw(path);
    }
}