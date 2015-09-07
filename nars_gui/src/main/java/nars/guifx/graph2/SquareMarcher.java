//package nars.guifx.graph2;
//
//import javafx.scene.shape.Polygon;
//import nars.guifx.NARfx;
//
//import java.util.Arrays;
//
///**
// * Created by me on 9/6/15.
// */
//@Deprecated public class SquareMarcher extends Isolines {
//
//    public int[] input;
//
//
//
//    public SquareMarcher(int w, int h) {
//        super(w, h);
//
//        this.w = w;
//        this.h = h;
//        input = new int[w * h];
//
//    }
//
//    public void clear(double x1, double y1, double x2, double y2, int val) {
//        final double cw = this.cw = Math.abs(x2-x1);
//        final double ch = this.ch = Math.abs(y2-y1);
//        final double cx = this.cx = 0.5 * cw + x1;
//        final double cy = this.cy = 0.5 * ch + y1;
//
//        Arrays.fill(input, val);
//    }
//
//
//    public final void set(final int x, final int y, final int v) {
//        final int w = this.w;
//        if ((x < 0) || (y < 0) || ( x >= w) || (y >= h)) return;
//        final int i = x + y * w;
//        input[i] = v;
//    }
//    public final int get(final int x, final int y) {
//        final int w = this.w;
//        if ((x < 0) || (y < 0) || ( x >= w) || (y >= h)) return -1;
//        final int i = x + y * w;
//        return input[i];
//    }
//
//    public boolean getContourPoints(int contour, Polygon target) {
//
//        //PVector[] result = new PVector[getContourLength(contour)];
//        double xx = 0, yy = 0;
//        for (int i = 0; i < getContourLength(contour); i++) {
//            xx += getContourX(contour, i);
//            yy += getContourY(contour, i);
//        }
//        xx = xx / getContourLength(contour);
//        yy = yy / getContourLength(contour);
//
//        double cx = this.cx;
//        double cy = this.cy;
//        double sh = this.ch / h;
//        double sw = this.cw / w;
//
//
//        if (getNumContours() < 1) {
//            return false;
//        }
//
//        int length = getContourLength(contour);
//
//        int existingCoords = target.getPoints().size();
//        int targetCoords = length;
//        if (existingCoords != targetCoords) {
//            if (existingCoords < targetCoords) {
//                //clear, just as expensive as remove repeatedly?
//                target.getPoints().clear();
//                existingCoords = 0;
//            }
//
//            for (int i = existingCoords; i < targetCoords; i++) {
//                target.getPoints().add(0d);
//                target.getPoints().add(0d);
//            }
//        }
//
//
//        double s = 1.0;
//        for (int i = 0; i < getContourLength(contour); ) {
//            double xi = ((getContourX(contour, i) - xx) * s + xx);
//            double yi = ((getContourY(contour, i) - yy) * s + yy);
//
//            //scale
//            double px = cx + sw * xi;
//            double py = cy + sh * yi;
//
//              /*int xj = (int)((getContourX(contour, i - 1) - cx) * s + cx);
//		      int yj = (int)((getContourY(contour, i - 1) - cy) * s + cy);
//		      int tx = (xi + xj) / 2;
//		      int ty = (yi + yj) / 2;
//		      int nx = (int)(10 * (getContourY(contour, i) - getContourY(contour, i - 1)));
//		      int ny = (int)(10 * (getContourX(contour, i - 1) - getContourX(contour, i)));
//		      */
//
//            target.getPoints().set(i++, px);
//            target.getPoints().set(i++, py);
//        }
//
//        return true;
//    }
//
//
//    public final void find(Polygon output) {
//        find(input);
//        int c = getNumContours();
//        if(c >= 0) {
//            output.setVisible(true);
//            getContourPoints(c, output);
//        }
//        else {
//            output.setVisible(false);
//        }
//
//    }
//
//}
