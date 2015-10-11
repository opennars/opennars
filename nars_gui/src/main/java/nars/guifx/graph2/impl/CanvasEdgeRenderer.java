package nars.guifx.graph2.impl;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nars.guifx.ResizableCanvas;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.scene.DefaultVis;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.guifx.util.ColorMatrix;
import nars.term.Termed;

/**
 * Created by me on 9/6/15.
 */
public class CanvasEdgeRenderer implements SpaceGrapher.EdgeRenderer<TermEdge> {

//    ColorArray colors = new ColorArray(
//            32,
//            Color.BLUE,
//            Color.GREEN
//    );
    ColorMatrix colors = DefaultVis.colors; /*new ColorMatrix(24,24,
        (pri,termTaskBalance) -> {
            return Color.hsb(30 + 120.0 * termTaskBalance, 0.75, 0.35 + 0.5 * pri);
        }
    );*/

    Canvas floorCanvas = null;
    private GraphicsContext gfx = null;
    private double tx = 0.0;
    private double ty = 0.0;
    private double s = 0.0;

//    //for iterative auto-normalization
//    public double maxPri = 1;
//    public double minPri = 0;

    double minWidth = 2;
    double maxWidth = 10;

    @Override
    public void accept(final TermEdge i) {

        TermNode aSrc = i.aSrc;
        TermNode bSrc = i.bSrc;

        boolean v = i.visible = !(!aSrc.visible() || !bSrc.visible());
        if (!v) {
            return;
        }



        /*boolean aVis = a.update(), bVis = b.update();
        visible = (aVis || bVis);
        if (!visible) return false;*/


        final double tx = this.tx;
        final double ty = this.ty;
        final double s = this.s;
        double x1 = s*(tx+aSrc.x());// + fw / 2d;
        double y1 = s*(ty+aSrc.y());// + fh / 2d;
        double x2 = s*(tx+bSrc.x());// + tw / 2d;
        double y2 = s*(ty+bSrc.y());// + th / 2d;
        double cx = 0.5 * (x1+x2);
        double cy = 0.5 * (y1+y2);


        drawHalf(i, aSrc, x1, y1, cx, cy);
        drawHalf(i, bSrc, x2, y2, cx, cy);

    }

    public void drawHalf(TermEdge i, TermNode t, double x1, double y1, double x2, double y2) {


        double te = i.termLinkFrom(t); //t.termLinkStat.getAverage();
        double ta = i.taskLinkFrom(t); //t.taskLinkStat.getAverage();

        double p = 0.5 * (te + ta);

        //double np = normalize(p);

        //System.out.println(p + " " + np + " " + minPri + " " + maxPri);

        //gfx.setStroke(colors.get(np));
        //gfx.setStroke(colors.get(np, te/(te+ta)));

        //HACK specific to Term instances
        if (t.term instanceof Termed) {
            gfx.setStroke(
                    TermNode.getTermColor((Termed)t.term, colors, p)
            );
        }

        /*
            colors.get(
                (t.term.op().ordinal()%colors.cc.length)/((double) Op.values().length),
                p
            )
        );*/

        final double mw = minWidth;
        gfx.setLineWidth(mw + p * (maxWidth-mw));

        gfx.strokeLine(x1, y1, x2, y2);
    }

//    public double normalize(final double p) {
//        double maxPri = this.maxPri, minPri = this.minPri;
//
//        if (minPri > p)
//            this.minPri = minPri = p;
//
//        if (maxPri < p)
//            this.maxPri = maxPri = p;
//
//        if (maxPri == minPri) return p;
//
//        return (p - minPri) / (maxPri - minPri);
//    }

    @Override
    public void reset(SpaceGrapher g) {


        if (floorCanvas == null) {

            floorCanvas = new ResizableCanvas(g.widthProperty(), g.heightProperty());

            g.getChildren().
                    add(0, floorCanvas); //underneath, background must be transparent to see
                    //add(floorCanvas); //over

            this.gfx = floorCanvas.getGraphicsContext2D();
        }
        else {

        }

        Scene scene = g.getScene();
        if (scene == null) return;

        double w = scene.getWidth(); //g.getWidth();
        double h = scene.getHeight();

        tx = g.translate.getX();
        ty = g.translate.getY();
        s = g.scale.getX();


        clear(w, h);

        //unnormalize(0.1);
    }

    protected void clear(double w, double h) {
        clearTotally(w, h);
    }

    protected final void clearTotally(double w, double h) {
        gfx.clearRect(0, 0, w, h );
    }

    final Color FADEOUT = new Color(0,0,0,0.25);

    protected final void clearFade(double w, double h) {
        gfx.setFill(FADEOUT);
        gfx.fillRect(0, 0, w, h );
    }

//    /** iteration in which min/max dynamic range is relaxed; if nothing has stretched it in the past cycle then it will expand the range to its limits */
//    private void unnormalize(double rate) {
//        double maxPriPre = maxPri;
//        double minPriPre = minPri;
//        minPri = Util.lerp(maxPriPre, minPri, rate);
//        maxPri = Util.lerp(minPriPre, maxPri, rate);
//    }

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
