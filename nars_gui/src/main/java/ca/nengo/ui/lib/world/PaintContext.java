package ca.nengo.ui.lib.world;

import java.awt.*;

public class PaintContext {
	private Graphics2D graphics;
	//private double scale;


	public Graphics2D getGraphics() {
		return graphics;
	}

	/*public double getScale() {
		return scale;
	}*/

    public void set(Graphics2D graphics/*, double scale*/) {
        if (this.graphics!=graphics) {
            this.graphics = graphics;
            //hint(ENABLE_NATIVE_FONTS);
            graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            //this.scale = scale;
        }
    }
}
