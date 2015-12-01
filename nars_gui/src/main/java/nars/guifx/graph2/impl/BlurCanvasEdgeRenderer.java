package nars.guifx.graph2.impl;

/**
 * applies a blurring effect on canvas redraws
 */
public class BlurCanvasEdgeRenderer extends CanvasEdgeRenderer {

    @Override
    protected final void clear(double w, double h) {
        clearFade(w, h);
    }

    protected final void clearFade(double w, double h) {
        gfx.setFill(FADEOUT);
        gfx.fillRect(0, 0, w, h );
    }

}
