package nars.guifx.graph2.impl;

import javafx.scene.transform.Affine;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.source.SpaceGrapher;

/** (slower, nicer rendering) half edges are drawn as overlapping polygons */
public class HalfHalfRightTriangleCanvasEdgeRenderer extends CanvasEdgeRenderer {

    private final double[] xp = new double[3];
    private final double[] yp = new double[3];

    static final Affine reverse = new Affine();
    static {
        reverse.appendRotation(180);
    }


    @Override
    public void reset(SpaceGrapher g) {
        super.reset(g);
        gfx.setStroke(null);
        gfx.setLineCap(null);
        gfx.setLineJoin(null);
        //if (aSrc.isVisible()) {
        double[] X = this.xp;
        double[] Y = this.yp;
        /*{
            X[0] = -0.5;
            Y[0] = 0d;
            X[1] = -0.5;
            Y[1] = 0.5;
            X[2] = 0.5;
            Y[2] = 0d;
        }*/
        X[0] = -0.5;
        Y[0] = -0.5;
        X[1] = -0.5;
        Y[1] = 0.5;
        X[2] = 0.5;
        Y[2] = 0d;

    }

    @Override
    public void draw(TermEdge e, TermNode aSrc, TermNode bSrc, double x1, double y1, double x2, double y2) {


        double dx = (x1 - x2);
        double dy = (y1 - y2);
        double len = Math.sqrt(dx * dx + dy * dy);
        //len-=fw/2;

        //double rot = Math.atan2(dy, dx);

        //double cx = 0.5f * (x1 + x2);
        //double cy = 0.5f * (y1 + y2);

        //Affine.translate(cx,cy).rotate(rot, 0,0).scale(len,len)
//            translate.setY(cy);
//            rotate.setAngle(FastMath.toDegrees(rot));
//            scale.setX(len);
//            scale.setY(len);


        double p = e.getWeight();
        final double t = p *maxWidth + minWidth;

        gfx.save();
        gfx.translate((x1+x2)/2f, (y1+y2)/2f);

        double rot = /*Fast*/Math.atan2(dy, dx);
        gfx.rotate(rot * 180f/3.14150);

        gfx.scale(len, t);




        //if (aSrc.isVisible()) {
        double[] X = this.xp;
        double[] Y = this.yp;


        gfx.setFill(TermNode.getTermColor(aSrc.term, colors, p));
        gfx.fillPolygon(X, Y, 3);

        gfx.transform(reverse);

        gfx.setFill(TermNode.getTermColor(aSrc.term, colors, p));
        gfx.fillPolygon(X, Y, 3);


        gfx.restore();
    }

}
