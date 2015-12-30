package nars;

import nars.util.data.Util;
import net.openhft.affinity.AffinityLock;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * self managed set of processes which run a NAR
 * as a loop at a certain frequency.
 */
public class NARLoop implements Runnable {

    static final Logger logger = getLogger(NARLoop.class);

    public final NAR nar;

    private final Thread thread;

    /**
     * sleep mode delay time
     */
    static final long sleepTimeMS = 250;
    private final boolean cpuCoreReserve;


    public volatile int cyclesPerFrame = 1;
    volatile int periodMS = 1000;
    private volatile boolean stopped = false;
    //private boolean running;

    //TODO make this into a SimpleIntegerProperty also


    @Override
    public String toString() {
        return nar + ":loop@" + getFrequency() + "Hz";
    }

    //in Hz / fps
    public double getFrequency() {
        return 1000.0 / periodMS;
    }

    public int getPeriodMS() {
        return periodMS;
    }


    public NARLoop(NAR n, int initialPeriod) {
        this(n, initialPeriod, false);
    }

    /**
     *
     * @param n
     * @param initialPeriod
     * @param reserveCPUCore whether to acquire a thread affinity lock on a CPU core, which will improve performance if a dedicated core can be assigned
     */
    public NARLoop(NAR n, int initialPeriod, boolean reserveCPUCore) {


        nar = n;
        cpuCoreReserve = reserveCPUCore;

        n.the("loop", this);

        setPeriodMS(initialPeriod);

        thread = new Thread(this, n.self + ":loop");
        thread.start();
        logger.info("starting {}", thread);
    }


    public final boolean setPeriodMS(int period) {
        int prevPeriod = getPeriodMS();

        if (prevPeriod == period) return false;

        periodMS = period;
        if (period == -1) {
            logger.info("pause");
        } else {
            if (prevPeriod == -1)
                logger.info("resume:period={}", period);
            else {
                //dont log change in period, too noisy
            }
        }

        //thread priority control
        if (thread != null) {
            int pri = thread.getPriority();
            int fullThrottlePri = Thread.MIN_PRIORITY;

            int targetPri = periodMS == 0 ? fullThrottlePri : Thread.NORM_PRIORITY;

            if (targetPri != pri)
                thread.setPriority(fullThrottlePri);

            thread.interrupt();

        }


        return true;
    }

    public void stop() {
        logger.info("stopping {}", this);
        stopped = true;
    }

    public void waitForTermination() throws InterruptedException {
        stop();
        thread.join();
    }

    @Override
    public final void run() {




        AffinityLock al;
        if (cpuCoreReserve) {
            al = AffinityLock.acquireLock();
            if (al.isAllocated() && al.isBound()) {
                logger.info(thread + " running exclusively on CPU " +  al.cpuId());
            }
        }
        else {
            al = null;
        }

        try {
            NAR nar = this.nar;

            if (periodMS != -1)
                logger.info("started, period={}", periodMS);

            do {
                try {
                    while (!stopped)
                        frame(nar);
                } catch (Exception e) {
                    nar.memory.eventError.emit(e);
                    if (Global.DEBUG) stopped = true;
                }
            } while (!stopped);

        } finally {
            if (al!=null)
                al.release();
        }

        logger.info("stopped");
    }

    public void frame(NAR nar) {
        int periodMS = this.periodMS;

        if (periodMS < 0) {
            //idle
            Util.pause(sleepTimeMS);
        } else {

            long start = System.currentTimeMillis();

            if (!nar.running.get()) {
                nar.frame(cyclesPerFrame);
                throttle(periodMS, System.currentTimeMillis() - start);
            } else {
                //logger.warn("nar began running before this frame attempted to start");
                Thread.yield();
            }
        }

    }

    protected static long throttle(long minFramePeriodMS, long frameTimeMS) {
        double remainingTime = (minFramePeriodMS - frameTimeMS) / 1.0E3;

        if (remainingTime > 0) {

            //        try {
//            Thread.sleep(sleepTime);
//        } catch (InterruptedException e) {
//            //e.printStackTrace();
//        }

            Util.pause(minFramePeriodMS);

        } else if (remainingTime < 0) {

            Thread.yield();

            if (Global.DEBUG) {
                //TODO blink a non-intrusive indicator in GUI
                logger.warn("lag {}ms", remainingTime);
            }

            //minFramePeriodMS++;
            //; incresing frame period to " + minFramePeriodMS + "ms");
        }
        return minFramePeriodMS;
    }


    public final void pause() {
        setPeriodMS(-1);
    }

//    //TODO not well tested
//    public void setRunning(boolean r) {
//        this.running = r;
//    }
//
//    public boolean isRunning() {
//        return running;
//    }
}
