package nars.guifx.demo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import nars.LocalMemory;
import nars.Memory;
import nars.NAR;
import nars.bag.impl.GuavaCacheBag;
import nars.clock.RealtimeMSClock;
import nars.guifx.LogPane;
import nars.guifx.NARide;
import nars.io.UDPNetwork;
import nars.nar.Default;

import java.net.SocketException;

import static javafx.application.Platform.runLater;

/**
 * small app that embeds a self-contained reasoner + UDP network interface
 * with some controls for enable/disable and connecting to peers
 *
 */
public class UDPWidget extends BorderPane {


    public UDPWidget(NAR n) {
        super();
        setCenter(new LogPane(n, new SimpleDoubleProperty(0), "eventCycle", "eventFrame"));
        runLater(()-> {
            setLeft(new UDPPane(n));
        });
    }

    public static void main(String[] args) {

        Memory mem = new LocalMemory(new RealtimeMSClock(),
                new GuavaCacheBag<>()
            /*new InfiniCacheBag(
                InfiniPeer.tmp().getCache()
            )*/
        );
        NAR n = new Default(mem, 1024, 3, 5, 7);
        NARide.show(n.loop(),(i)->{

            i.addView(new UDPPane(n));

            //b.setScene(new Scene(new UDPWidget(n), 500,400));
            //b.show();

        });
    }

    public static class UDPPane extends VBox {

        private final NAR nar;
        UDPNetwork u;

        public void start(int port) {
            if (u != null) {
                u.stop();
                u = null;
            }

            try {
                u = new UDPNetwork(port);
                nar.memory.eventSpeak.emit("Connect: " + u);
            } catch (SocketException e) {
                nar.memory.eventError.emit(e);
            }
        }

        public UDPPane(NAR n) {
            super();
            this.nar = n;

            start(0);

        }
    }
}
