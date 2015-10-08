package nars.guifx.demo;

import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import nars.Global;
import nars.NAR;
import nars.clock.RealtimeMSClock;
import nars.guifx.IOPane;
import nars.guifx.NARfx;
import nars.guifx.NARide;
import nars.io.UDPNetwork;
import nars.nar.Default;
import nars.util.data.Util;

import java.net.SocketException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by me on 10/8/15.
 */
public class InterNARSimulator {

    public static void main(String[] args) throws SocketException {
        int num = 2;
        float freq = 15f;
        List<NAR> group = Global.newArrayList();
        @Deprecated /* to be accessed with narsese commands */ List<UDPNetwork> networks = Global.newArrayList();

        RealtimeMSClock clock = new RealtimeMSClock(); //common clock

        Global.DEBUG = true;

        for (int i = 0; i < num; i++) {
            NAR a = new Default(1000, 1, 1, 3, clock);
            group.add(a);

            UDPNetwork u = new UDPNetwork(10001 + i);
            u/*.setFrequency(freq)*/.connect(a);
            networks.add(u);
        }

        for (int _i = 0; _i < num; _i++) {
            final int i = _i;
            NAR a = group.get(i);
            new Thread(() -> {

                //temporary until narsee connect()
                if (i > 0)
                    networks.get(i).peer.connect(
                            "localhost",
                            10001 + (i - 1)
                    ); //connect to the previous one

                switch (i) {
                    case 0:
                        int n = 0;
                        a.stdout();
                        while (true) {
                            Util.pause(4000);
                            a.input("send(" +
                                networks.get(i).id +
                                ", ping:{" +
                                (n++) + "});");
                        }

                        //break;
                }
            }).start();

        }


        NARfx.run((a, b) -> {

            HBox w = new HBox(8);

            group.stream().map(
                    n -> {
                        NARide x = new NARide(n.loop(250));
                        x.addView(new IOPane(n));
                        return x;
                    })
                    .collect(Collectors.toCollection(() ->
                            w.getChildren()));

            b.setScene(new Scene(w, 800, 600));
            b.show();
        });

    }
}
