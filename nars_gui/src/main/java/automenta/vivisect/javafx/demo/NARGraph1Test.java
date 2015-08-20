package automenta.vivisect.javafx.demo;

import automenta.vivisect.javafx.graph2.NARGraph1;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import nars.Global;
import nars.NAR;
import nars.guifx.NARfx;
import nars.io.out.TextOutput;
import nars.nar.NewDefault;
import org.apache.commons.math3.util.FastMath;

import java.io.File;
import java.io.IOException;

/**
 * Created by me on 8/15/15.
 */
public class NARGraph1Test {

    public static void main(String[] args) throws IOException {

        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.5f;
        NAR n = new NAR(new NewDefault().setInternalExperience(null));
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
            new Thread(() -> n.loop(50)).start();

        });


    }
}
