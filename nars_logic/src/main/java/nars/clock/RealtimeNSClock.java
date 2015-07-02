package nars.clock;

/** nanosecond accuracy */
public class RealtimeNSClock extends RealtimeClock {

    public RealtimeNSClock(boolean updatePerCycle) {
        super(updatePerCycle);
    }

    @Override
    protected long getRealTime() {
        return System.nanoTime();
    }

    @Override
    protected float unitsToSeconds(final long l) {
        return (l / 1e9f);
    }

}
