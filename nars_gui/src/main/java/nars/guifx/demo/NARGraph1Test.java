package nars.guifx.demo;

import javafx.scene.Scene;
import nars.Global;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.graph2.NARGraph1;
import nars.io.out.TextOutput;
import nars.nar.experimental.Equalized;

import java.io.File;
import java.io.IOException;

/**
 * Created by me on 8/15/15.
 */
public class NARGraph1Test {

    public static void main(String[] args) throws IOException {

        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.5f;
        NAR n = new NAR(
            new Equalized(60, 1, 1).setInternalExperience(null)
        );
        //n.input(new File("/tmp/h.nal"));
        n.input("<a-->b>.");
        n.input("<b-->c>.");

        n.frame(10);



        NARfx.run( (a, s) -> {



            NARGraph1 ng = new NARGraph1(n);
            ng.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            Scene sc = ng.newScene(1000, 1000);


            sc.getStylesheets().setAll(NARfx.css, "dark.css");

            s.setOnCloseRequest(e -> {
                System.exit(1);
            });


            ng.setVisible(true);
            s.show();

            NARfx.newWindow(n);
        });
//        TextOutput.out(n);
//        new Thread(() -> n.loop(185)).start();


    }
}
