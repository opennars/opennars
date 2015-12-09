package nars.video;

import nars.util.signal.OneDHaar;
import org.apache.commons.math3.util.FastMath;

/**
 * Created by me on 10/28/15.
 */
public class SineSource implements WaveSource {

    private final float SAMPLE_RATE = 44100;

    private final double freq;
    int samples;
    private float t = 0;

    public SineSource(double freq) {
        this.freq = freq;
        //nyquist:
        samples = OneDHaar.largestPowerOf2NoGreaterThan((int) Math.ceil(freq * 2));
    }

    @Override
    public int start() {
        return samples;
    }

    @Override
    public void stop() {

    }

    @Override
    public int next(float[] buffer) {
        float t = this.t;
        float dt = buffer.length / SAMPLE_RATE  / (float)(Math.PI*2);
        double f = freq;
        int num = buffer.length;
        for (int i = 0; i < num; i++) {
            buffer[i] = (float) FastMath.sin(f * t);
            t += dt;
        }
        this.t = t;
        return 0;
    }
}
