package nars.clock;

/** millisecond accuracy */
public class RealtimeMSClock extends RealtimeClock {



    @Override
    protected long getRealTime() {
        return System.currentTimeMillis();
    }

    @Override
    protected float unitsToSeconds(final long l) {
        return (l / 1000f);
    }
}
