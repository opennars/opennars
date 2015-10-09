package nars.guifx.demo;

import javafx.scene.Scene;
import nars.Global;
import nars.NAR;
import nars.clock.RealtimeMSClock;
import nars.guifx.IOPane;
import nars.guifx.NARfx;
import nars.guifx.NARide;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.layout.HyperOrganicLayout;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.io.UDPNetwork;
import nars.nar.Default;
import nars.term.Atom;
import nars.util.data.Util;

import java.net.SocketException;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 10/8/15.
 */
public class InterNARSimulator {

    public static void main(String[] args) throws SocketException {
        int num = 5;
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
                            Util.pause(7000);
                            a.input("send(" +
                                    networks.get(i).id +
                                    ", ping:{" +
                                    (n++) + "})!");
                        }

                        //break;
                    default:
                        Util.pause(200);
                        a.input("peer(" +
                                networks.get(i).id +
                                ",(\"localhost\",10001))!");

                        break;
                }
            }).start();

        }


        NARfx.run((a, b) -> {


            SpaceGrapher sg;
            b.setScene(new Scene(
                sg = SpaceGrapher.forCollection(group.stream().map(n -> {
                            NARide x = new NARide(n.loop(250));//.start();
                            if (Math.random() < 0.5)
                                x.addView(new IOPane(n));
                            else
                                x.addView(NARGraph1Test.newGraph(n));
                            return x;
                        })
                    .collect(Collectors.toList()),
                        (NARide n) -> Atom.the(n.nar.getName()),
                        (NARide n, TermNode tn) -> {
                            n.setScaleX(0.45);
                            n.setScaleY(0.45);
                            tn.getChildren().add(n);
                        }, new HyperOrganicLayout(1000))
                    , 800, 600));

            b.getScene().getStylesheets().add(NARfx.css);
            b.show();

            runLater(()->  sg.start(250) );

        });

    }
}
