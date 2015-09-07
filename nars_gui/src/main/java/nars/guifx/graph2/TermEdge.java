package nars.guifx.graph2;

import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 * Created by me on 9/5/15.
 */
public class TermEdge extends Group /*implements ChangeListener*/ {


    public final TermNode bSrc;
    public final TermNode aSrc;

    //        private final Translate translate;
//        private final Rotate rotate;
//        private final Scale scale;
    //private AtomicBoolean changed = new AtomicBoolean(true);
    public double len;
    public boolean visible = false;
    public Object data;

    public TermEdge(TermNode aSrc, TermNode bSrc) {
        super();

        setVisible(true);

        //setAutoSizeChildren(true);

        this.aSrc = aSrc;
        this.bSrc = bSrc;

        //a = new TermEdgeHalf(aSrc, bSrc, this);
        //a.setVisible(false);
        //b = new TermEdgeHalf(bSrc, aSrc, this);
        //b.setVisible(false);

        if (aSrc.term.compareTo(bSrc.term) > 0) {
            throw new RuntimeException("invalid term order for TermEdge: " + aSrc + " " + bSrc);
        }

        //getChildren().setAll(a, b);

        //aSrc.layoutXProperty().addListener(this);
        //aSrc.layoutXProperty().addListener(this);
//            aSrc.localToSceneTransformProperty().addListener(this);
//            bSrc.localToSceneTransformProperty().addListener(this);


//        getTransforms().setAll(
//                translate = Transform.translate(0, 0),
//                rotate = Transform.rotate(0, 0, 0),
//                scale = Transform.scale(1, 1)
//        );
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

}
