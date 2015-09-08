package nars.guifx.demo;

import nars.NAR;
import nars.guifx.NARfx;
import nars.nar.experimental.Equalized;

import java.io.File;
import java.io.IOException;

/**
 * Created by me on 9/7/15.
 */
public class NARfxEqualizedIterative {
    public static void main(String[] arg) {


//        Platform.runLater(new Runnable() {
//
//            @Override
//            public void run() {
//
//            }
//        });

        //Application.launch(NARfx.class, arg);

        NARfx.newWindow(new NAR(new Equalized(1000,3,2)), (i) -> {
            try {
                i.nar.input(new File("/tmp/h.nal"));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

    }
}
