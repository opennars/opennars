package automenta.vivisect.audio.brainwave;

import automenta.vivisect.audio.SoundProducer;
import org.apache.commons.math3.util.FastMath;

/**
 * Binaural Beats for Human Brainwave Entrainment and Hemispheric Synchronization
 * http://en.wikipedia.org/wiki/Binaural_beats
 *
 * TODO make this a set of 2 SoundProducer's, set at fixed ambient Left/Right positions
 */
public class BinauralTones implements SoundProducer {

    private final float carrier;
    float beat, x;
    private boolean playing;

    public BinauralTones(float initialBeat, float initialCarrier) {
        this.beat = initialBeat;
        this.carrier = initialCarrier;
        this.x = 0;
        this.playing = true;
    }


    @Override public float read(float[] buf, int readRate) {
        float dt = 1f / readRate;

        final float leftRate = (carrier - (beat / 2f)) * (float)(Math.PI*2f);
        final float rigtRate = (carrier + (beat / 2f)) * (float)(Math.PI*2f);
        for (int i = 0; i < buf.length-1; /*stereo*/) {
            buf[i++] = (float)FastMath.sin( x * leftRate );
            buf[i++] = (float)FastMath.sin( x * rigtRate );
            x += dt;
        }

        return 0;
    }

    @Override
    public void skip(int samplesToSkip, int readRate) {
        float dt = 1f / readRate;
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
}
