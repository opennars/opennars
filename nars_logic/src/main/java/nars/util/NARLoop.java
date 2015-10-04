package nars.util;

import nars.NAR;
import org.clockwise.PeriodicTrigger;
import org.clockwise.Schedulers;

import java.util.concurrent.atomic.AtomicInteger;

/** self managed set of processes which run a NAR
 *  as a loop at a certain frequency. */
public class NARLoop {

    private final NAR nar;

    int cycleDivisor;

    public final AtomicInteger targetPeriodMS = new AtomicInteger(1);

    public NARLoop(NAR n) {
        this(n, Schedulers.newDefault());
    }

    public NARLoop(NAR n, Schedulers time) {
        this.nar = n;
        time.schedule(() -> nar.frame(), new PeriodicTrigger(cycleDivisor * 1000/60));
    }

    public NARLoop setPeriod(long initialFramePeriod) {

        return this;
    }
}
