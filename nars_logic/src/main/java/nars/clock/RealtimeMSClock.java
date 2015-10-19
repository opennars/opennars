package nars.clock;

/** millisecond accuracy */
public class RealtimeMSClock extends RealtimeClock {


    @Override
    protected final long getRealTime() {
        return System.currentTimeMillis();
    }

    @Override
    protected final float unitsToSeconds(final long l) {
        return (l / 1000f);
    }
}
