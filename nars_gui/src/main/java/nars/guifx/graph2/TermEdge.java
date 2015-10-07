package nars.guifx.graph2;

import nars.link.TLink;
import nars.link.TaskLink;
import nars.link.TermLink;

/**
 * Created by me on 9/5/15.
 */
public class TermEdge /*implements ChangeListener*/ {


    public static final TermEdge[] empty = new TermEdge[0];
    public final TermNode bSrc;
    public final TermNode aSrc;

    //        private final Translate translate;
//        private final Rotate rotate;
//        private final Scale scale;
    //private AtomicBoolean changed = new AtomicBoolean(true);
    public double len;
    public boolean visible = false;

    //TODO use a Half inner class for this
    public TermLink termLinkAB = null;
    public TermLink termLinkBA = null;
    public TaskLink taskLinkAB = null;
    public TaskLink taskLinkBA = null;


    public TermEdge(TermNode aSrc, TermNode bSrc) {
        super();


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


    final public float termLinkFrom(TermNode src) {
        TermLink tl = (src == aSrc) ? termLinkAB : termLinkBA;
        if (tl == null) return 0;
        return tl.getPriority();
    }
    final public float taskLinkFrom(TermNode src) {
        TaskLink tl = (src == aSrc) ? taskLinkAB : taskLinkBA;
        if (tl == null) return 0;
        return tl.getPriority();
    }

    final public void linkFrom(TermNode src, TLink link) {

        if (link instanceof TermLink) {
            TermLink tl = (TermLink)link;
            if (src == aSrc)
                termLinkAB = tl;
            else
                termLinkBA = tl;
        }
        else {
            TaskLink tl = (TaskLink)link;
            if (src == aSrc)
                taskLinkAB = tl;
            else
                taskLinkBA = tl;
        }

    }
}
