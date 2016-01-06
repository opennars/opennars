//package nars.guifx.demo;
//
//import javafx.scene.layout.BorderPane;
//import nars.NAR;
//import nars.guifx.NARMenu;
//import nars.guifx.NARfx;
//import nars.guifx.util.SizeAwareWindow;
//import nars.nar.experimental.Equalized;
//
//import static javafx.application.Platform.runLater;
//
///**
// * Created by me on 9/2/15.
// */
//public class DemoRealtimeClock extends NARMenu {
//
//    public DemoRealtimeClock(NAR nar) {
//        super(nar);
//
//    }
//
//    public static void main(String[] args) {
//        NARfx.run((a, b) -> {
//
//            SizeAwareWindow s;
//            b.setScene(s = new SizeAwareWindow(d -> {
//                return () -> {
//                    NAR nar = new Equalized(1000,8,4);
//
//                    return new BorderPane(new DemoRealtimeClock(nar));
//                };
//
//            }).show());
//
//
//            runLater(() -> {
//
//                s.pixelScale(2.0);
//                //s.getRoot().maxWidth(Double.MAX_VALUE);
//                //s.getRoot().maxHeight(Double.MAX_VALUE);
//
//
//                s.getRoot().setStyle("-fx-font-size: 205%;");
//                //s.getRoot().setStyle("-fx-scale-x: 205%;");
//                //s.getRoot().setScaleX(2f);
//                //s.getRoot().setScaleY(2f);
//                //s.getRoot().setScaleZ(0f);
//
//
//
//            });
//
//
//        });
//    }
//
//
// }
