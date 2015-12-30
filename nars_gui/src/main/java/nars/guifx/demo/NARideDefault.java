package nars.guifx.demo;

import nars.Global;
import nars.Memory;
import nars.nar.Default;
import nars.term.compile.TermIndex;
import nars.time.FrameClock;

/**
 * Created by me on 9/7/15.
 */
public class NARideDefault {

    public static void main(String[] arg) {

        Global.DEBUG = false;

        FrameClock clock = new FrameClock();
        NARide.show(new Default(
                new Memory(
                    clock,
                    TermIndex.memoryWeak(clock, 100)),
                1024, 1, 2, 3).loop(), (i) -> {
            /*try {
                i.nar.input(new File("/tmp/h.nal"));
            } catch (Throwable e) {
                i.nar.memory().eventError.emit(e);
                //e.printStackTrace();
            }*/
        });

    }
}
