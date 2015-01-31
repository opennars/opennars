package automenta.vivisect.audio.granular;

import automenta.vivisect.audio.SoundProducer;
import automenta.vivisect.audio.sample.SonarSample;

public class TimeStretcher implements SoundProducer {

	private final float[] sourceBuffer;
	private long now = 0L;
	private float stretchFactor = 1.0F;
	private Grain currentGrain = null;
	private Grain fadingGrain = null;
	private final Granulator granulator;
	private boolean isPlaying = false;
	private long playTime = 0L;
	private int playOffset;

    public TimeStretcher(SonarSample s, float grainSizeSecs) {
        this(s.buf, s.rate, grainSizeSecs);
        play();
    }

	public TimeStretcher(float[] buffer, float sampleRate, float grainSizeSecs) {
		this.sourceBuffer = buffer;
		this.granulator = new Granulator(buffer, sampleRate, grainSizeSecs);
	}

	public void process(float[] output) {
		if (currentGrain == null && isPlaying) {
			currentGrain = createGrain();
		}
		for (int i = 0; i < output.length; i++) {
			if (currentGrain != null) {
				output[i] = currentGrain.getSample(now);
				if (currentGrain.isFading(now)) {
					fadingGrain = currentGrain;
					currentGrain = isPlaying ? createGrain() : null;
				}
			}
			if (fadingGrain != null) {
				output[i] += fadingGrain.getSample(now);
				if (!fadingGrain.hasMoreSamples(now)) {
					fadingGrain = null;
				}
			}
			now++;
		}
	}

	public void play() {
		playOffset = 0;
		playTime = now;
		isPlaying = true;
	}

	public void stop() {
		isPlaying = false;
	}

	private Grain createGrain() {
		return granulator.createGrain(calculateCurrentBufferIndex(), now);
	}

	private int calculateCurrentBufferIndex() {
		return (playOffset + Math.round((now - playTime) / stretchFactor)) % sourceBuffer.length;
	}

	public TimeStretcher setStretchFactor(float stretchFactor) {
		playOffset = calculateCurrentBufferIndex();
		playTime = now;
		this.stretchFactor = stretchFactor;
        return this;
	}

    @Override
    public float read(float[] buf, int readRate) {
        process(buf);
        return 0f;
    }

    @Override
    public void skip(int samplesToSkip, int readRate) {

    }

    @Override
    public boolean isLive() {
        return isPlaying;
    }
}
