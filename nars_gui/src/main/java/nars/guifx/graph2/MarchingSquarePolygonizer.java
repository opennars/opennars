//package nars.guifx.graph2;
//
//import javafx.scene.shape.Polygon;
//
//import java.util.Arrays;
//
///**
// * Created by me on 9/6/15.
// */
//public class MarchingSquarePolygonizer extends MarchingSquares {
//
//    public Polygon polygon = new Polygon();
//    double cx, cy, cw, ch;
//    protected int w, h;
//    double[][] input;
//
//    public MarchingSquarePolygonizer(int w, int h) {
//        super();
//        input = new double[w][h];
//
//    }
//
//    public void clear(double x1, double y1, double x2, double y2, double val) {
//        final double cw = this.cw = Math.abs(x2-x1);
//        final double ch = this.ch = Math.abs(y2-y1);
//        final double cx = this.cx = 0.5 * cw + x1;
//        final double cy = this.cy = 0.5 * ch + y1;
//
//        Arrays.fill(input, val);
//    }
//
//
//    public final void set(final int x, final int y, final double v) {
//        if ((x < 0) || (y < 0) || ( x >= w) || (y >= h)) return;
//        input[x][y] = v;
//    }
//    public final double get(final int x, final int y) {
//        if ((x < 0) || (y < 0) || ( x >= w) || (y >= h)) return;
//        return input[x][y];
//    }
//
//
//    public int v2px(double v) {
//        return (int)((((v-cx)/cw) * w));
//    }
//    public int v2py(double v) {
//        return (int)((((v-cy)/ch) * h));
//    }
//
//    public void circle(double x, double y, double r, int circleColor) {
//        int px = v2px(x);
//        int py = v2py(y);
//        int rr = v2px(r)-v2px(0);
//        circle(px, py, rr, circleColor);
//    }
//
//    public void circle(int centerX, int centerY, int r, int circleColor) {
//        centerX+=r*2;
//        centerY+=r*2;
//
//        int d = (5 - r * 4) / 4;
//        int x = 0;
//        int y = r;
//
//        do {
//            set(centerX + x, centerY + y, circleColor);
//            set(centerX + x, centerY - y, circleColor);
//            set(centerX - x, centerY + y, circleColor);
//            set(centerX - x, centerY - y, circleColor);
//            set(centerX + y, centerY + x, circleColor);
//            set(centerX + y, centerY - x, circleColor);
//            set(centerX - y, centerY + x, circleColor);
//            set(centerX - y, centerY - x, circleColor);
//            if (d < 0) {
//                d += 2 * x + 1;
//            } else {
//                d += 2 * (x - y) + 1;
//                y--;
//            }
//            x++;
//        } while (x <= y);
//
//        flood(centerX, centerY, 0, circleColor);
//
//    }
//
//    public void flood(int x, int y, int srcColor, int tgtColor) {
//        // make sure x and y are inside the image
//        if (x < 0) return;
//        if (y < 0) return;
//        if (x >= w) return;
//        if (y >= h) return;
//
//        // make sure this pixel hasn't been visited yet
//        if (get(x, y)!=srcColor) return;
//
//
//        // fill pixel with target color and mark it as visited
//        set(x, y, tgtColor);
//
//
//        // recursively fill surrounding pixels
//        // (this is equivelant tgtColor depth-first search)
//        if (x > 0)
//            flood(x - 1, y, srcColor, tgtColor);
//        if (x < w-1)
//            flood(x + 1, y, srcColor, tgtColor);
//        if (y > 0)
//            flood(x, y - 1, srcColor, tgtColor);
//        if (y < h-1)
//            flood(x, y + 1, srcColor, tgtColor);
//    }
//
//
//    public void drawLine(int x1, int y1, int x2, int y2, int v) {
//        // delta of exact value and rounded value of the dependant variable
//        int d = 0;
//
//        int dy = Math.abs(y2 - y1);
//        int dx = Math.abs(x2 - x1);
//
//        int dy2 = (dy << 1); // slope scaling factors to avoid floating
//        int dx2 = (dx << 1); // point
//
//        int ix = x1 < x2 ? 1 : -1; // increment direction
//        int iy = y1 < y2 ? 1 : -1;
//
//        if (dy <= dx) {
//            for (; ; ) {
//
//                set(x1, y1, v);
//
//                if (x1 == x2)
//                    break;
//                x1 += ix;
//                d += dy2;
//                if (d > dx) {
//                    y1 += iy;
//                    d -= dx2;
//                }
//            }
//        } else {
//            for (; ; ) {
//
//                set(x1, y1, v);
//
//                if (y1 == y2)
//                    break;
//                y1 += iy;
//                d += dx2;
//                if (d > dy) {
//                    x1 += ix;
//                    d -= dy2;
//                }
//            }
//        }
//    }
//
//
//
//
//
//    public Polygon polygonize() {
//        update(polygon);
//        return polygon;
//    }
//
//    private void update(Polygon polygon) {
//
//        double thresh = 0.5;
//        IsoCell[][] contour = mkContour(input, thresh);
//
//        // Convert contour to GeneralPath.
//        mkIso(contour, input, thresh, polygon);
//    }
//
//    public static void main(String[] args) {
//
//        MarchingSquarePolygonizer w = new MarchingSquarePolygonizer(32,32);
//        w.setThreshold(25);
//        System.out.println(Arrays.toString(w.cl));
//
//        //w.setMaxPoints(32);
//        //w.setEdgeLength(2);
//
//        w.clear(-1, -1, 1, 1, 0);
//        //w.circle(0,0,0.25,1);
//        w.circle(0,0,0.20, 50);
//        w.print();
//
//        Polygon p = w.polygonize();
//        System.out.println(p);
//    }
//
//}
