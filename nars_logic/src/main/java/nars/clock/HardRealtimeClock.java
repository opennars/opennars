package nars.clock;

/**
 * hard realtime does not cache the value and will always update when time()
 * is called
 */
public class HardRealtimeClock extends RealtimeClock {

    private final boolean msOrNano;

    public HardRealtimeClock(boolean msOrNano) {
        super(false);
        this.msOrNano = msOrNano;
    }

    /** default: ms resolution */
    public HardRealtimeClock() {
        this(true);
    }

    @Override
    public void preCycle() {

    }

    @Override
    protected long getRealTime() {
        if (msOrNano) {
            return System.currentTimeMillis();
        }
        else {
            return System.nanoTime();
        }
    }

    @Override
    protected float unitsToSeconds(long l) {
        if (msOrNano) {
            return (l / 1000f);
        }
        else {
            return (l / 1e9f);
        }
    }

    public long time() {
        return getRealTime();
    }

}
