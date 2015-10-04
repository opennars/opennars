package nars.guifx.demo;

import nars.Global;
import nars.LocalMemory;
import nars.Memory;
import nars.NAR;
import nars.clock.RealtimeMSClock;
import nars.guifx.NARide;
import nars.nar.Default;
import nars.util.NARLoop;

/**
 * Created by me on 9/7/15.
 */
public class NARfxDefaultIRealtime {

    public static void main(String[] arg) {

        int cyclesPerFrame = 8;

        Global.DEBUG = true;

        Memory mem = new LocalMemory(new RealtimeMSClock(true));
        NAR nar = new Default(mem, 1024, 2, 3, 4);

        nar.memory.duration.set(125);
        nar.setCyclesPerFrame(cyclesPerFrame);
        //nar.spawnThread(1000/60);


        NARLoop n = nar.loop(0.1f);


        NARide.show(nar, (i) -> {
            /*try {
                i.nar.input(new File("/tmp/h.nal"));
            } catch (Throwable e) {
                i.nar.memory().eventError.emit(e);
                //e.printStackTrace();
            }*/
        });

    }
}
