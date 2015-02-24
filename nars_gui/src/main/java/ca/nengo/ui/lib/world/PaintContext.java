package ca.nengo.ui.lib.world;

import java.awt.*;

public class PaintContext {
	private Graphics2D graphics;
	private double scale;


	public Graphics2D getGraphics() {
		return graphics;
	}

	public double getScale() {
		return scale;
	}

    public void set(Graphics2D graphics, double scale) {
        this.graphics = graphics;
        this.scale = scale;
    }
}
