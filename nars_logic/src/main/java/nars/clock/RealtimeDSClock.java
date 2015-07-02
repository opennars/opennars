package nars.clock;

/** decisecond (0.1) accuracy */
public class RealtimeDSClock extends RealtimeClock {

    public RealtimeDSClock(boolean updatePerCycle) {
        super(updatePerCycle);
    }

    @Override
    protected long getRealTime() {
        return System.currentTimeMillis()/100;
    }

    @Override
    protected float unitsToSeconds(final long l) {
        return (l / 10f);
    }

}
