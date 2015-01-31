package automenta.vivisect.audio;

public interface StereoSoundProducer
{
    public float read(float[] leftBuf, float[] rightBuf, int readRate);
    public void skip(int samplesToSkip, int readRate);
}