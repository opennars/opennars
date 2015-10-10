package nars.guifx.demo;

import nars.Global;
import nars.guifx.NARide;
import nars.nar.Default;
import nars.nar.SingleStepNAR;

/**
 * Created by me on 9/7/15.
 */
public class NARfxDefaultIterative {

    public static void main(String[] arg) {

        Global.DEBUG = true;

        NARide.show(new Default().loop(), (i) -> {
            /*try {
                i.nar.input(new File("/tmp/h.nal"));
            } catch (Throwable e) {
                i.nar.memory().eventError.emit(e);
                //e.printStackTrace();
            }*/
        });

    }
}
