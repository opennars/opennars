package nars.audio;

/** Monaural sound source */
public interface SoundProducer {
    float read(float[] buf, int readRate);
    void skip(int samplesToSkip, int readRate);
    boolean isLive();

    default float getAmplitude() { return 1.0f; }

    void stop();

    interface Amplifiable {
        void setAmplitude(float a);
    }
}