package nars.audio.synth;

import nars.audio.SoundProducer;
import org.apache.commons.math3.util.FastMath;

/**
 * Created by me on 2/4/15.
 */
public class SineWave implements SoundProducer, SoundProducer.Amplifiable {

    private final float freq;
    float beat, x;
    private boolean playing;
    private float amp;

    public SineWave(float freq) {
        this.freq = freq;
        x = 0;
        playing = true;
        amp = 1.0f;
    }


    @Override public float read(float[] buf, int readRate) {
        float dt = 1.0f / readRate;


        float r = (freq ) * (float)(Math.PI* 2.0f);
        float A = amp;
        float X = x;
        for (int i = 0; i < buf.length;) {
            buf[i++] = (float) FastMath.sin(X * r) * A;
            X += dt;
        }
        x = X;

        return 0;
    }

    @Override
    public void skip(int samplesToSkip, int readRate) {
        float dt = 1.0f / readRate;
        x += dt * samplesToSkip;
    }

    @Override
    public boolean isLive() {
        return playing;
    }

    @Override
    public void stop() {
        playing = false;
    }

    @Override
    public void setAmplitude(float a) {
        amp = a;
    }
}
