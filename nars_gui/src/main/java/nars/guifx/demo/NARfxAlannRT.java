package nars.guifx.demo;

import nars.guifx.NARide;
import nars.nar.experimental.DefaultAlann;

import java.io.File;

/**
 * Created by me on 9/7/15.
 */
public class NARfxAlannRT {
    public static void main(String[] arg) {


//        Platform.runLater(new Runnable() {
//
//            @Override
//            public void run() {
//
//            }
//        });

        //Application.launch(NARfx.class, arg);

        //Equalized d = new Equalized(1000, 3, 2);
        DefaultAlann d = new DefaultAlann(16);
        //d.memory().setClock(new RealtimeMSClock(false));

        NARide.show(d, (i) -> {
            try {
                i.nar.input(new File("/tmp/h.nal"));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

    }
}
