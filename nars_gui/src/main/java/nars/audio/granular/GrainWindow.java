package nars.audio.granular;

public interface GrainWindow {

	/** in samples */
	int getSize();

	/** amplitude factor */
	float getFactor(int offset);

}
