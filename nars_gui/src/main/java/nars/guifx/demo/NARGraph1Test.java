package nars.guifx.demo;

import javafx.scene.Scene;
import nars.Global;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.graph2.NARGraph1;
import nars.nar.experimental.Equalized;

import java.io.IOException;

/**
 * Created by me on 8/15/15.
 */
public class NARGraph1Test {

    public static void main(String[] args) throws IOException {

        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.5f;
        NAR n = new NAR(
                new Equalized(1000, 2, 3).setInternalExperience(null)
        );
        //n.input(new File("/tmp/h.nal"));
        n.input("<a-->b>.");
        n.input("<b-->c>.");




        NARfx.run( (a, s) -> {


            NARGraph1 ng = new NARGraph1(n);
            ng.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);



            Scene sc = new Scene(ng, 1000, 900);


            sc.getStylesheets().setAll(NARfx.css, "dark.css");
            s.setScene(sc);

            s.show();
            s.setOnCloseRequest(e -> {
                System.exit(1);
            });

            //TextOutput.out(n);
            new Thread(() -> n.loop(35)).start();

        });


    }
}
