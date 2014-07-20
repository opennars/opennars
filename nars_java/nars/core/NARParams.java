package nars.core;

/**
 * NAR Parameters which can be changed during runtime.
 */
public class NARParams {

    /** Silent threshold for task reporting, in [0, 100]. */
    private int silenceLevel;

    public int getSilenceLevel() {
        return silenceLevel;
    }

    public NARParams setSilenceLevel(int silenceLevel) {
        this.silenceLevel = silenceLevel;
        return this;
    }
}
