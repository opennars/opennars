package automenta.vivisect.surfaceplotter.surface;

import java.awt.*;

/**
 * Stands for a Color Model
 * 
 * @author Eric
 * @date 8 avril 2004 15:18:27
 */
public class ColorModel {
	public static final byte DUALSHADE = 0;
	public static final byte SPECTRUM = 1;
	public static final byte FOG = 2;
	public static final byte OPAQUE = 3;

	float hue;
	float sat;
	float bright;
	float min; // hue|sat|bright of z=0
	float max; // Hue|sat|bright of z=1
	byte mode = 0;

	Color ocolor; // fixed color for opaque mode

	public ColorModel(byte mode, float hue, float sat, float bright, float min,
			float max) {
		this.mode = mode;
		this.hue = hue;
		this.sat = sat;
		this.bright = bright;
		this.min = min;
		this.max = max;
	}

	public Color getPolygonColor(float z) {
		if (z < 0 || z > 1)
			return Color.WHITE;
		switch (mode) {
			case DUALSHADE :
				return color(hue, sat, norm(z));
			case SPECTRUM :
				return color(norm(1 - z), sat, bright);
				// return color(norm(1-z),0.3f+z*(0.7f), bright);
			case FOG :
				return color(hue, norm(z), bright);
			case OPAQUE :
				if (ocolor == null)
					ocolor = color(hue, sat, bright);
				return ocolor;
		}
		return Color.WHITE;// default
	}

	/**
	 * @param hue
	 * @param sat
	 * @param bright
	 * @return
	 */
	private Color color(float hue, float sat, float bright) {
		Color hsb = Color.getHSBColor(hue, sat, bright);
		// transparency management: unfortunately we reached power limits of
		// 2010's computers it's laggy
		// return new Color(hsb.getRed(), hsb.getGreen(), hsb.getBlue(), 128);
		return hsb;
	}

	private float norm(float z) {
		if (min == max)
			return min;
		return min + z * (max - min);
	}

}// end of class
