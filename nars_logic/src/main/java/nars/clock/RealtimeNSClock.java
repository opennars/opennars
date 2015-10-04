package nars.clock;

/** nanosecond accuracy */
public class RealtimeNSClock extends RealtimeClock {



    @Override
    protected long getRealTime() {
        return System.nanoTime();
    }

    @Override
    protected float unitsToSeconds(final long l) {
        return (l / 1e9f);
    }

}
