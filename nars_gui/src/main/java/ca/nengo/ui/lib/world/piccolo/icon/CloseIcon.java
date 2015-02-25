package ca.nengo.ui.lib.world.piccolo.icon;


import ca.nengo.ui.lib.world.piccolo.object.Window;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class CloseIcon extends WindowIconBase {

    static final Stroke defaultStroke = new BasicStroke(0.1f);
    static final GeneralPath closePath = new GeneralPath();

    static {
        final float p = 1.1f * PADDING / Window.BUTTON_SIZE;
        float rectangleSize = 1 - p;
        closePath.moveTo(p, p);
        closePath.lineTo(rectangleSize, rectangleSize);
        closePath.moveTo(p, rectangleSize);
        closePath.lineTo(rectangleSize, p);
    }

    public CloseIcon(int size) {
        super(size);
    }

    @Override
    protected void paintIcon(Graphics2D g2) {

        float s = getSize();
        g2.setStroke(defaultStroke);
        g2.scale(s, s);
        g2.draw(closePath);
        g2.scale(1f/s, 1f/s);
    }
}