package nars.video;

/**
 * Source of a digitized 1D wave signal
 */
public interface WaveSource {

	/**
	 * returns the buffer size, in samples
	 */
	int start();

	void stop();

	int next(float[] buffer);
}
