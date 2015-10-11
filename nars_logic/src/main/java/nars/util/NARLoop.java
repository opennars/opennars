package nars.util;

import nars.NAR;
import nars.util.data.Util;

import java.util.logging.Logger;

/**
 * self managed set of processes which run a NAR
 * as a loop at a certain frequency.
 */
public class NARLoop implements Runnable {

    final static Logger logger = Logger.getLogger(NARLoop.class.getSimpleName());

    public final NAR nar;

    private final Thread thread;

    /**
     * sleep mode delay time
     */
    static final long sleepTimeMS = 250;


    volatile int cyclesPerFrame = 1;
    volatile int periodMS = 0;
    private volatile boolean stopped = false;
    //private boolean running;

    //TODO make this into a SimpleIntegerProperty also


    @Override
    public String toString() {
        return nar.toString() + ":loop@" + getFrequency() + "Hz";
    }

    //in Hz / fps
    public double getFrequency() {
        return 1000.0 / periodMS;
    }

    public int getPeriodMS() {
        return periodMS;
    }


    public NARLoop(NAR n, int initialPeriod) {


        this.nar = n;

        nar.the("loop", this);

        setPeriodMS(initialPeriod);

        this.thread = new Thread(this);
        thread.start();

        logger.info(() -> (this + " started thread " + thread + " with priority=" + thread.getPriority()) );
    }


    public final boolean setPeriodMS(final int period) {
        int prevPeriod = getPeriodMS();

        if (prevPeriod == period) return false;

        this.periodMS = period;

        //thread priority control
        if (thread != null) {
            int pri = thread.getPriority();
            final int fullThrottlePri = Thread.MIN_PRIORITY;

            final int targetPri;
            if (periodMS == 0) {
                targetPri = fullThrottlePri;
            } else {
                final int normalPri = Thread.NORM_PRIORITY;
                targetPri = normalPri;
            }

            if (targetPri != pri)
                thread.setPriority(fullThrottlePri);

            thread.interrupt();

        }


        return true;
    }

    public void stop() {
        logger.info(() -> (this + " stop requested") );
        stopped = true;
    }

    public void waitForTermination() throws InterruptedException {
        stop();
        thread.join();
    }

    @Override
    final public void run() {


        final NAR nar = this.nar;

        while (!stopped) {

            final int periodMS = this.periodMS;

            if (periodMS < 0) {
                sleep(sleepTimeMS);
                continue;
            }


            final long start = System.currentTimeMillis();

            if (!nar.running.get()) {
                nar.frame(cyclesPerFrame);
            } else {
                //wait until nar is free
            }


            final long frameTimeMS = System.currentTimeMillis() - start;


            throttle(periodMS, frameTimeMS);

        }

        logger.info(() -> (this + " stopped") );
    }

    protected long throttle(long minFramePeriodMS, long frameTimeMS) {
        double remainingTime = (minFramePeriodMS - frameTimeMS) / 1.0E3;

        if (remainingTime > 0) {

            sleep(minFramePeriodMS);

        } else if (remainingTime < 0) {

            Thread.yield();

            //System.err.println(Thread.currentThread() + " loop lag: " + remainingTime + "ms too slow");

            //minFramePeriodMS++;
            //; incresing frame period to " + minFramePeriodMS + "ms");
        }
        return minFramePeriodMS;
    }


    public void sleep(long sleepTime) {
//        try {
//            Thread.sleep(sleepTime);
//        } catch (InterruptedException e) {
//            //e.printStackTrace();
//        }

        Util.pause(sleepTime);
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
