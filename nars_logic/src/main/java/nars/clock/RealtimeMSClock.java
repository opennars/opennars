package nars.clock;

/** millisecond accuracy */
public class RealtimeMSClock extends RealtimeClock {


    public RealtimeMSClock(boolean updatePerCycle) {
        super(updatePerCycle);
    }

    @Override
    protected long getRealTime() {
        return System.currentTimeMillis();
    }

    @Override
    protected float unitsToSeconds(final long l) {
        return (l / 1000f);
    }
}
