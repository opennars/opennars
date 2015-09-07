package nars.guifx.demo;

import nars.NAR;
import nars.clock.RealtimeClock;
import nars.clock.RealtimeMSClock;
import nars.guifx.NARfx;
import nars.nar.experimental.Equalized;

import java.io.File;
import java.io.IOException;

/**
 * Created by me on 9/7/15.
 */
public class NARfxEqualizedRT {
    public static void main(String[] arg) {


//        Platform.runLater(new Runnable() {
//
//            @Override
//            public void run() {
//
//            }
//        });

        //Application.launch(NARfx.class, arg);

        Equalized d = new Equalized(1000, 3, 2);
        d.setClock(new RealtimeMSClock(false));

        NARfx.newWindow(new NAR(d), (i) -> {
            try {
                i.nar.input(new File("/tmp/h.nal"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}
