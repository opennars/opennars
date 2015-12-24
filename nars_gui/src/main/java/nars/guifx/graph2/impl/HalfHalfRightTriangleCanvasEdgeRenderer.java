package nars.guifx.graph2.impl;

import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.source.SpaceGrapher;

/** (slower, nicer rendering) half edges are drawn as overlapping polygons */
public class HalfHalfRightTriangleCanvasEdgeRenderer extends CanvasEdgeRenderer {

    private double[] xp = new double[4];
    private double[] yp = new double[4];


    @Override
    public void reset(SpaceGrapher g) {
        super.reset(g);
        gfx.setStroke(null);
        gfx.setLineCap(null);
        gfx.setLineJoin(null);
    }

    @Override
    public void draw(TermEdge e, TermNode aSrc, TermNode bSrc, double x1, double y1, double x2, double y2) {


        double dx = (x1 - x2);
        double dy = (y1 - y2);
        double len = Math.sqrt(dx * dx + dy * dy);
        //len-=fw/2;

        //double rot = Math.atan2(dy, dx);
        double rot = /*Fast*/Math.atan2(dy, dx);

        //double cx = 0.5f * (x1 + x2);
        //double cy = 0.5f * (y1 + y2);

        //Affine.translate(cx,cy).rotate(rot, 0,0).scale(len,len)
//            translate.setY(cy);
//            rotate.setAngle(FastMath.toDegrees(rot));
//            scale.setX(len);
//            scale.setY(len);


        final double t = e.getWeight() *maxWidth + minWidth;

        gfx.save();
        gfx.translate((x1+x2)/2f, (y1+y2)/2f);
        gfx.rotate(rot * 180f/3.14150);
        gfx.scale(len, t);




        //if (aSrc.isVisible()) {
        double[] X = this.xp;
        double[] Y = this.yp;

        render(e, aSrc, -0.5, 0, 0.5, 0, 0, 0.5, X, Y);

        gfx.rotate(180);

        //}
        //if (bVis) {
            render(e, bSrc, -0.5, 0, 0.5, 0, 0, 0.5, X, Y);
        //}

        gfx.restore();
    }

    protected void render(TermEdge e, TermNode origin, double x1, double y1, double x2, double y2, double nx, double ny, double[] X, double[] Y) {
        double p = e.getWeight();


        X[0] = x1;
        Y[0] = y1;
        X[1] = x1 + nx;
        Y[1] = y1 + ny;
        X[2] = x2;
        Y[2] = y2;


        gfx.setFill(TermNode.getTermColor(origin.term, colors, p));
        gfx.fillPolygon(X, Y, 3);
    }

}
