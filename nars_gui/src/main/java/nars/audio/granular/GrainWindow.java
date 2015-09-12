package nars.audio.granular;

public interface GrainWindow {

	/** in samples */
	public int getSize();

	/**  amplitude factor */
	public float getFactor(int offset);

}
