//package nars.guifx.demo;
//
//import javafx.beans.property.SimpleDoubleProperty;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.VBox;
//import nars.NAR;
//import nars.guifx.NARide;
//import nars.guifx.TracePane;
//import nars.nar.Default;
//import nars.op.io.UDPNetwork;
//
//import java.net.SocketException;
//
//import static javafx.application.Platform.runLater;
//
///**
// * small app that embeds a self-contained reasoner + UDP network interface
// * with some controls for enable/disable and connecting to peers
// *
// */
//public class UDPWidget extends BorderPane {
//
//
//    public UDPWidget(NAR n) {
//        super();
//        setCenter(new TracePane(n, new SimpleDoubleProperty(0)));
//        runLater(()-> {
//            setLeft(new UDPPane(n));
//        });
//    }
//
//    public static void main(String[] args) {
//
//        NAR n = new Default();
////                new Default(
////            new Memory(
////                new RealtimeMSClock(),
////                new MapCacheBag(new HashMap())
////        ), 1024, 2, 3, 4);
//
//        NARide.show(n.loop(), (i)->{
//
//            i.addView(new UDPPane(n));
//
//            //b.setScene(new Scene(new UDPWidget(n), 500,400));
//            //b.show();
//
//        });
//    }
//
//    public static class UDPPane extends VBox {
//
//        private final NAR nar;
//        UDPNetwork u;
//
//        public void start(int port) {
//            if (u != null) {
//                u.stop();
//                u = null;
//            }
//
//            try {
//                u = new UDPNetwork("udp", port);
//                u.connect(nar);
//                nar.memory.eventSpeak.emit("Connect: " + u.id + " " + u.toString());
//            } catch (SocketException e) {
//                nar.memory.eventError.emit(e);
//            }
//        }
//
//        public UDPPane(NAR n) {
//            super();
//            this.nar = n;
//
//            start((int)(Math.random()*100 + 10000));
//
//        }
//    }
// }
