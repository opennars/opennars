package nars.guifx.demo;

import nars.Global;
import nars.nar.Default;

/**
 * Created by me on 9/7/15.
 */
public class NARfxDefaultIterative {

    public static void main(String[] arg) {

        Global.DEBUG = false;

        NARide.show(new Default(1024, 2, 2, 3).loop(), (i) -> {
            /*try {
                i.nar.input(new File("/tmp/h.nal"));
            } catch (Throwable e) {
                i.nar.memory().eventError.emit(e);
                //e.printStackTrace();
            }*/
        });

    }
}
