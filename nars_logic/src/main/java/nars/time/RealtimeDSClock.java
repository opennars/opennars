package nars.time;

/** decisecond (0.1) accuracy */
public class RealtimeDSClock extends RealtimeClock {

    @Override
    protected long getRealTime() {
        return System.currentTimeMillis()/100;
    }

    @Override
    protected float unitsToSeconds(long l) {
        return (l / 10.0f);
    }

}
