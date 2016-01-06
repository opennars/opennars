//package nars.guifx.graph2;
//
//import javafx.geometry.BoundingBox;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Polygon;
//import org.apache.commons.math3.util.FastMath;
//
///**
// * Created by me on 9/6/15.
// */
//public class QuadPolyEdgeRenderer implements NARGraph.EdgeRenderer<TermEdge> {
//    @Override
//    public void reset(NARGraph g) {
//
//    }
//
//    @Override
//    public void accept(TermEdge termEdge) {
//        Polygon p = null; //(Polygon)termEdge.data;
//        if (p == null) {
//            //termEdge.data = p = new Polygon();
//
//            /*for (int i = 0; i < 4 * 2; i++)
//                p.getPoints().addAll(0d);*/
//
//            //double q = 0.25f;
//
//            //if (!order(from.term, to.term)) {
//            //p.getPoints().setAll(0.5d, 0d, -0.5d, q, -0.5d, -q); //right triangle
//            //} else {
//            //180deg rotate
//            //  p.getPoints().setAll(-0.5d, 0d, 0.5d, -q, 0.5d, q); //right triangle
//            //}
//
//            termEdge.getChildren().setAll(p);
//
//            p.setSmooth(false);
//            //p.setNeedsLayout(false);
//            p.setStrokeWidth(0);
//            p.setStroke(null);
//
//            termEdge.setVisible(true);
//            p.setVisible(true);
//            p.setFill(Color.ORANGE);
//
//
//            //termEdge.setManaged(false);
//            //p.setManaged(false);
//
//            //p.setCacheShape(true);
//            //termEdge.aSrc.getChildren().add(p);
//        }
//
//
//
//
//        TermNode aSrc = termEdge.aSrc;
//        TermNode bSrc = termEdge.bSrc;
//
//        double x1 = aSrc.x();// + fw / 2d;
//        double y1 = aSrc.y();// + fh / 2d;
//        double x2 = bSrc.x();// + tw / 2d;
//        double y2 = bSrc.y();// + th / 2d;
//        double dx = (x1 - x2);
//        double dy = (y1 - y2);
//        double len = Math.sqrt(dx * dx + dy * dy);
//        //len-=fw/2;
//
//        //double rot = Math.atan2(dy, dx);
//        double rot = FastMath.atan2(dy, dx);
//        double cx = 0; //0.5f * (x1 + x2);
//        double cy = 0; //0.5f * (y1 + y2);
//
//
//
//
//        /*p.setTranslateX(cx);
//        p.setTranslateY(cy);
//
//        p.setRotate(FastMath.toDegrees(rot));
//
//        p.setScaleX(len);
//        p.setScaleY(len);
//        p.setScaleZ(len);*/
//
//        double sca = len;
//        p.getPoints().setAll(cx+-sca,cy+-sca,
//                cx+sca,cy+-sca,
//                cx+sca,cy+sca,
//                cx+-sca,cy+sca);
//
//        termEdge.autosize();
//
//        //p.setScaleX(2);
//        //p.setScaleY(2);
//
//        termEdge.layout();
//
//
//        System.out.println(len + " " + p.localToScene(new BoundingBox(0,0,1,1)));
//
//        //System.out.println(len + " " +cx + " "  + cy + " " + rot);
//
//        //System.out.println(p.localToScene(0,0) + " " + p.localToScene(new BoundingBox(-0.5,-0.5,0, 1,1,0)));
//
//
//    }
// }
