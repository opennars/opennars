package nars.guifx.demo;

import nars.Global;
import nars.LocalMemory;
import nars.Memory;
import nars.NAR;
import nars.clock.RealtimeMSClock;
import nars.guifx.NARide;
import nars.nar.Default;

/**
 * Created by me on 9/7/15.
 */
public class NARfxDefaultIRealtime {

    public static void main(String[] arg) {

        int cyclesPerFrame = 2;

        Global.DEBUG = true;

        Memory mem = new LocalMemory(new RealtimeMSClock());
        NAR nar = new Default(mem, 1024, 2, 3, 4);

        nar.memory.conceptForgetDurations.set(50);
        nar.memory.duration.set(100);
        nar.setCyclesPerFrame(cyclesPerFrame);
        //nar.spawnThread(1000/60);



        NARide.show(nar.loop(), (i) -> {
            /*try {
                i.nar.input(new File("/tmp/h.nal"));
            } catch (Throwable e) {
                i.nar.memory().eventError.emit(e);
                //e.printStackTrace();
            }*/
        });

    }
}
