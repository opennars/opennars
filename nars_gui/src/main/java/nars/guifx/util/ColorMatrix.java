package nars.guifx.util;

import javafx.scene.paint.Color;

/**
 * generates a gradient of colors, stored in an array.
 * useful for re-usable color objects instead of creating new instances
 */
public class ColorMatrix {

    public final Color[][] cc;

    private final Double2Function<Color> compute;
    private final int w;
    private final int h;

    public interface Double2Function<X> {
        X apply(double x, double y);
    }

    public ColorMatrix(int w, int h, Double2Function<Color> compute) {
        cc = new Color[w][];
        for (int i = 0; i < w; i++)
            cc[i] = new Color[h];
        this.w = w;
        this.h = h;
        this.compute = compute;
    }


    public final Color get(double px, double py) {
        int x = p(px, w);
        int y = p(py, h);

        Color[] c = cc[x];
        Color z;
        if ((z = c[y]) == null) {
            c[y] = z = compute.apply(px, py);
        }
        return z;
    }

    public final Color get(int x, int y) {
        return get( (double)x/w, (double)y/h );
    }

    /** p = 0..1.0 */
    public static int p(double p, int range) {
//        if ((p < 0) || (p > 1))
//            throw new RuntimeException("Out of bounds color range: " + p);
        int i = (int) /*FastMath.round*/(p*(range-1));
        if (i < 0) i = 0;
        if ( i >= range) i = range-1;
        return i;
    }


//    public void ColorMatrix2(int n, Color start, Color end, boolean hsb) {
//
//        Color[] c = new Color[n];
//        for (int i = 0; i < n; i++) {
//            float p = ((float) i) / n;
//            float q = 1 - p;
//
//            double r = start.getRed() * q + end.getRed() * p;
//            double g = start.getGreen() * q + end.getGreen() * p;
//            double b = start.getBlue() * q + end.getBlue() * p;
//            double a = start.getOpacity() * q + end.getOpacity() * p;
//
//            if (!hsb) {
//            }
//            else {
//                throw new RuntimeException("not impl yet");
//            }
//            c[i] = new Color(r, g , b , a );
//
//        }
//
//    }


//    public Color get(double p, double opacity) {
//        Color c = get(p);
//        if (opacity == 1f) return c;
//        if (opacity < 0.01) return Color.TRANSPARENT;
//        return new Color(c.getRed(), c.getGreen(), c.getBlue(), opacity);
//    }

}
