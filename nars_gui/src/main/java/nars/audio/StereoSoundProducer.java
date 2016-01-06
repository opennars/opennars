package nars.audio;

public interface StereoSoundProducer {
	float read(float[] leftBuf, float[] rightBuf, int readRate);
	void skip(int samplesToSkip, int readRate);
}