package ca.nengo.ui.lib.world.piccolo.icon;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;

import java.awt.*;

public abstract class LayoutIconBase extends WorldObjectImpl {

    private final int size;
    public static final int STROKE_WIDTH = 2;
    public static final int PADDING = 5;

    public LayoutIconBase(int size) {
        this.size = size;
        this.setBounds(0, 0, size, size);
    }

    public int getSize() {
        return size;
    }

    @Override
    public void paint(PaintContext paintContext) {
        super.paint(paintContext);
        Graphics2D g2 = paintContext.getGraphics();
        g2.setColor(NengoStyle.COLOR_FOREGROUND);
        g2.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        paintIcon(g2);
    }

    protected abstract void paintIcon(Graphics2D g2);
}