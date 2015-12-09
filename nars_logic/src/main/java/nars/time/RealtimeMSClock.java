package nars.time;

/** millisecond accuracy */
public class RealtimeMSClock extends RealtimeClock {


    @Override
    protected final long getRealTime() {
        return System.currentTimeMillis();
    }

    @Override
    protected final float unitsToSeconds(long l) {
        return (l / 1000.0f);
    }
}
