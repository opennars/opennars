package automenta.vivisect.swing;

import java.awt.*;

/**
 * generates a gradient of colors, stored in an array. useful for re-usable
 * color objects instead of creating new instances
 */
public class ColorArray {
	public final Color[] c;

	public ColorArray(int n, Color start, Color end) {
		this(n, start, end, false);
	}

	public ColorArray(int n, Color start, Color end, boolean hsb) {

		c = new Color[n];
		for (int i = 0; i < n; i++) {
			float p = ((float) i) / n;
			float q = 1 - p;

			float r = start.getRed() * q + end.getRed() * p;
			float g = start.getGreen() * q + end.getGreen() * p;
			float b = start.getBlue() * q + end.getBlue() * p;
			float a = start.getAlpha() * q + end.getAlpha() * p;

			if (!hsb) {
			} else {
				throw new RuntimeException("not impl yet");
			}
			c[i] = new Color(r / 256.0f, g / 256.0f, b / 256.0f, a / 256.0f);

		}

	}

	/** p = 0..1.0 */
	public Color get(double p) {
		if ((p < 0) || (p > 1))
			throw new RuntimeException("Out of bounds color range: " + p);
		return c[(int) Math.round(p * (c.length - 1))];
	}
}
