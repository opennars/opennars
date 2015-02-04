package automenta.vivisect.audio;

/** Monaural sound source */
public interface SoundProducer {
    public float read(float[] buf, int readRate);
    public void skip(int samplesToSkip, int readRate);
    public boolean isLive();

    default float getAmplitude() { return 1f; }

    abstract public void stop();

    public static interface Amplifiable {
        public void setAmplitude(float a);
    }
}