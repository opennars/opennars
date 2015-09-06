package nars.guifx.graph2;

import javafx.scene.canvas.GraphicsContext;

/**
 * Created by me on 9/5/15.
 */
public class TermEdge /*xtends Group implements ChangeListener*/ {


    public final TermNode bSrc;
    public final TermNode aSrc;
    final TermEdgeHalf a, b;

    //        private final Translate translate;
//        private final Rotate rotate;
//        private final Scale scale;
    //private AtomicBoolean changed = new AtomicBoolean(true);
    public double len;
    public boolean visible = false;

    public TermEdge(TermNode aSrc, TermNode bSrc) {

        this.aSrc = aSrc;
        this.bSrc = bSrc;

        a = new TermEdgeHalf(aSrc, bSrc, this);
        //a.setVisible(false);
        b = new TermEdgeHalf(bSrc, aSrc, this);
        //b.setVisible(false);

        if (aSrc.term.compareTo(bSrc.term) > 0) {
            throw new RuntimeException("invalid term order for TermEdge: " + aSrc + " " + bSrc);
        }

        //getChildren().setAll(a, b);

        //aSrc.layoutXProperty().addListener(this);
        //aSrc.layoutXProperty().addListener(this);
//            aSrc.localToSceneTransformProperty().addListener(this);
//            bSrc.localToSceneTransformProperty().addListener(this);


//            getTransforms().setAll(
//                    translate = Transform.translate(0, 0),
//                    rotate = Transform.rotate(0, 0, 0),
//                    scale = Transform.scale(0, 0)
//            );
//
//            setNeedsLayout(false);
//            setCacheShape(true);
        //setCache(true);
        //setCacheHint(CacheHint.DEFAULT);
    }

    public void delete() {
//            aSrc.localToSceneTransformProperty().removeListener(this);
//            bSrc.localToSceneTransformProperty().removeListener(this);
    }

//        @Override
//        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//
//            changed.set(true);
//
//        }

    //        private void setA(TermNode aSrc) {
//            this.aSrc = aSrc;
//            a.setVisible(aSrc!=null);
//        }
//
//        private void setB(TermNode bSrc) {
//            this.bSrc = bSrc;
//            b.setVisible(bSrc!=null);
//        }

//        /** fx thread */
//        public boolean OLDrender() {
//
//            //changed.set(false);
//
//
//            if (!aSrc.isVisible() || !bSrc.isVisible()) {
//                setVisible(false);
//                return false;
//            }
//
//            double x1 = aSrc.x();// + fw / 2d;
//            double y1 = aSrc.y();// + fh / 2d;
//            double x2 = bSrc.x();// + tw / 2d;
//            double y2 = bSrc.y();// + th / 2d;
//            double dx = (x1 - x2);
//            double dy = (y1 - y2);
//            this.len = Math.sqrt(dx * dx + dy * dy);
//            //len-=fw/2;
//
//            //double rot = Math.atan2(dy, dx);
//            double rot = FastMath.atan2(dy, dx);
//            double cx = 0.5f * (x1 + x2);
//            double cy = 0.5f * (y1 + y2);
//
//
//            translate.setX(cx);
//            translate.setY(cy);
//            rotate.setAngle(FastMath.toDegrees(rot));
//            scale.setX(len);
//            scale.setY(len);
//
//
//            return a.update() || b.update();
//        }

    public final TermNode otherNode(final TermNode x) {
        if (aSrc == x) return bSrc;
        return aSrc;
    }

    double[] xp = new double[3];
    double[] yp = new double[3];
    double[] xr = new double[3];
    double[] yr = new double[3];

    public boolean render(final GraphicsContext g) {

        if (!aSrc.isVisible() || !bSrc.isVisible()) {
            return false;
        }

        boolean aVis = a.update(), bVis = b.update();
        visible = (aVis || bVis);
        if (!visible) return false;

        double x1 = aSrc.sx();// + fw / 2d;
        double y1 = aSrc.sy();// + fh / 2d;
        double x2 = bSrc.sx();// + tw / 2d;
        double y2 = bSrc.sy();// + th / 2d;
        double dx = (x1 - x2);
        double dy = (y1 - y2);
        //this.len = Math.sqrt(dx * dx + dy * dy);
        //len-=fw/2;

        //double rot = Math.atan2(dy, dx);
        double rot = /*Fast*/Math.atan2(dy, dx);

        //double cx = 0.5f * (x1 + x2);
        //double cy = 0.5f * (y1 + y2);

        //norm vector
        double nx = Math.sin(rot);
        double ny = Math.cos(rot);

        //Affine.translate(cx,cy).rotate(rot, 0,0).scale(len,len)
//            translate.setY(cy);
//            rotate.setAngle(FastMath.toDegrees(rot));
//            scale.setX(len);
//            scale.setY(len);


        if (aVis) {
            render(g, a, x1, y1, x2, y2, nx, ny, xp, yp);
        }
        if (bVis) {
            render(g, b, x2, y2, x1, y1, -nx, -ny, xr, yr);
        }

        return true;
    }

    protected void render(GraphicsContext g, TermEdgeHalf e, double x1, double y1, double x2, double y2, double nx, double ny, double[] X, double[] Y) {
        final double t = e.thickness;
        X[0] = x1;
        Y[0] = y1;
        X[1] = x1 + nx * t;
        Y[1] = y1 + ny * t;
        X[2] = x2;
        Y[2] = y2;

        g.setFill(e.fill);
        g.fillPolygon(X, Y, 3);
    }
}
