package nars.video;

/**
 * Source of a digitized 1D wave signal
 */
public interface WaveSource {

    /**
     * returns the buffer size, in samples
     */
    public int start();

    public void stop();

    public int next(float[] buffer);
}
