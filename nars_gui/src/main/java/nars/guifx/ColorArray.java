package nars.guifx;

import javafx.scene.paint.Color;

/**
 * generates a gradient of colors, stored in an array.
 * useful for re-usable color objects instead of creating new instances
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

            double r = start.getRed() * q + end.getRed() * p;
            double g = start.getGreen() * q + end.getGreen() * p;
            double b = start.getBlue() * q + end.getBlue() * p;
            double a = start.getOpacity() * q + end.getOpacity() * p;

            if (!hsb) {
            }
            else {
                throw new RuntimeException("not impl yet");
            }
            c[i] = new Color(r, g , b , a );

        }

    }

    /** p = 0..1.0 */
    public Color get(double p) {
        if ((p < 0) || (p > 1))
            throw new RuntimeException("Out of bounds color range: " + p);
        return c[ (int)Math.round(p*(c.length-1)) ];
    }

    public Color get(double p, double opacity) {
        Color c = get(p);
        if (opacity == 1f) return c;
        if (opacity < 0.01) return Color.TRANSPARENT;
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), opacity);
    }

}
