package nars.guifx.graph2;

import javafx.scene.canvas.GraphicsContext;

/**
 * Created by me on 9/6/15.
 */
public class CanvasEdgeRenderer<E> implements NARGraph1.EdgeRenderer<E> {

    @Override
    public void accept(E i) {
            /*if (!e.render(floorGraphics)) {

            }*/


    }

    @Override
    public void reset(NARGraph1 g) {

    }
//
//    public boolean render(final GraphicsContext g) {
//
//        if (!aSrc.isVisible() || !bSrc.isVisible()) {
//            return false;
//        }
//
//        boolean aVis = a.update(), bVis = b.update();
//        visible = (aVis || bVis);
//        if (!visible) return false;
//
//        double x1 = aSrc.sx();// + fw / 2d;
//        double y1 = aSrc.sy();// + fh / 2d;
//        double x2 = bSrc.sx();// + tw / 2d;
//        double y2 = bSrc.sy();// + th / 2d;
//        double dx = (x1 - x2);
//        double dy = (y1 - y2);
//        //this.len = Math.sqrt(dx * dx + dy * dy);
//        //len-=fw/2;
//
//        //double rot = Math.atan2(dy, dx);
//        double rot = /*Fast*/Math.atan2(dy, dx);
//
//        //double cx = 0.5f * (x1 + x2);
//        //double cy = 0.5f * (y1 + y2);
//
//        //norm vector
//        double nx = Math.sin(rot);
//        double ny = Math.cos(rot);
//
//        //Affine.translate(cx,cy).rotate(rot, 0,0).scale(len,len)
////            translate.setY(cy);
////            rotate.setAngle(FastMath.toDegrees(rot));
////            scale.setX(len);
////            scale.setY(len);
//
//
//        if (aVis) {
//            render(g, a, x1, y1, x2, y2, nx, ny, xp, yp);
//        }
//        if (bVis) {
//            render(g, b, x2, y2, x1, y1, -nx, -ny, xr, yr);
//        }
//
//        return true;
//    }
//
//    protected void render(GraphicsContext g, TermEdgeHalf e, double x1, double y1, double x2, double y2, double nx, double ny, double[] X, double[] Y) {
//        final double t = e.thickness;
//        X[0] = x1;
//        Y[0] = y1;
//        X[1] = x1 + nx * t;
//        Y[1] = y1 + ny * t;
//        X[2] = x2;
//        Y[2] = y2;
//
//        g.setFill(e.fill);
//        g.fillPolygon(X, Y, 3);
//    }
//

    //if (edgeDirty.get()) {
    //edgeDirty.set(false);

//        if (floorGraphics == null) floorGraphics = floorCanvas.getGraphicsContext2D();
//
//        floorGraphics.setFill(
//                FADEOUT
//        );
//
//        floorGraphics.fillRect(0, 0, floorGraphics.getCanvas().getWidth(), floorGraphics.getCanvas().getHeight());
//
//        floorGraphics.setStroke(null);
//        floorGraphics.setLineWidth(0);

}
