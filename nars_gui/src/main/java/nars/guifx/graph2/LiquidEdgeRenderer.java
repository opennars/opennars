//package nars.guifx.graph2;
//
//import javafx.scene.paint.Color;
//
///**
// * Created by me on 9/6/15.
// */
//public class LiquidEdgeRenderer implements NARGraph1.EdgeRenderer<TermEdge> {
//
//    final int rx = 64;
//    final int ry = 64;
//
//    @Override
//    public void reset(NARGraph1 g) {
//
//    }
//
//    @Override
//    public void accept(TermEdge termEdge) {
//        MarchingSquarePolygonizer p = (MarchingSquarePolygonizer)termEdge.data;
//        if (p == null) {
//            termEdge.data = p = new MarchingSquarePolygonizer(rx,ry);
//            //p.setThreshold(1);
//            //p.setMaxPoints(32);
//            //p.setEdgeLength(1);
//
//            termEdge.getChildren().add(p.polygon);
//            p.polygon.setFill(Color.hsb(
//                    Math.random()*360,
//                    0.7, 0.8, 0.3));
//
//        }
//
//        double x1 = termEdge.aSrc.sx();
//        double y1 = termEdge.aSrc.sy();
//        double x2 = termEdge.bSrc.sx();
//        double y2 = termEdge.bSrc.sy();
//
//        p.clear(x1, y1, x2, y2, 0);
//        p.circle(x1, y1, termEdge.aSrc.width()*2, 1);
//        p.circle(x2, y2, termEdge.bSrc.width()*2, 1);
//
//        p.polygonize();
//
//
//    }
//
//}
