package nars.guifx.demo;

import nars.Global;
import nars.LocalMemory;
import nars.Memory;
import nars.NAR;
import nars.bag.impl.MapCacheBag;
import nars.clock.RealtimeMSClock;
import nars.guifx.NARide;
import nars.nar.Default2;

import java.util.HashMap;

/**
 * Created by me on 9/7/15.
 */
public class NARideRealtimeDefault {

    public static void main(String[] arg) {


        Global.DEBUG = true;

        Memory mem = new LocalMemory(new RealtimeMSClock(),
            new MapCacheBag(
                    new HashMap()
                    //new WeakValueHashMap()
            )
            //new GuavaCacheBag<>()
            /*new InfiniCacheBag(
                InfiniPeer.tmp().getCache()
            )*/
        );
        NAR nar = new Default2(mem, 1024, 1, 1, 3);

        //nar.memory.conceptForgetDurations.set(10);
        nar.memory.duration.set(250 /* ie, milliseconds */);
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
