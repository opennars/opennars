package nars.clock;

/** millisecond accuracy */
public class RealtimeMSClock extends RealtimeClock {


    /** if not update each cycle, it will update each frame */
    public RealtimeMSClock(boolean updateEachCycle) {
        super(updateEachCycle);
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
