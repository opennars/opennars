package automenta.vivisect.audio.granular;

import automenta.vivisect.audio.SoundProducer;
import automenta.vivisect.audio.sample.SonarSample;
import com.google.common.util.concurrent.AtomicDouble;

public class Granulize implements SoundProducer {

	private final float[] sourceBuffer;
	private long now = 0L;
	public final AtomicDouble stretchFactor = new AtomicDouble(1.0);
	private long[] currentGrain = null;
	private long[] fadingGrain = null;
	private final Granulator granulator;
	private boolean isPlaying = false;
	private long playTime = 0L;
	private int playOffset;


    public Granulize(SonarSample s, float grainSizeSecs, float windowSizeFactor) {
        this(s.buf, s.rate, grainSizeSecs, windowSizeFactor);
        play();
    }

	public Granulize(float[] buffer, float sampleRate, float grainSizeSecs, float windowSizeFactor) {
		this.sourceBuffer = buffer;
		this.granulator = new Granulator(buffer, sampleRate, grainSizeSecs, windowSizeFactor);
	}

	public void process(float[] output) {
		if (currentGrain == null && isPlaying) {
			currentGrain = createGrain(currentGrain);
		}
		for (int i = 0; i < output.length-1; ) {
            float nextSample = 0;
			if (currentGrain != null) {
				nextSample = granulator.getSample(currentGrain, sourceBuffer, now);
				if (granulator.isFading(currentGrain, now)) {
					fadingGrain = currentGrain;
                    if (isPlaying)
                        currentGrain = createGrain(currentGrain);
                    else
                        currentGrain = null;
				}
			}
			if (fadingGrain != null) {
                nextSample += granulator.getSample(fadingGrain, sourceBuffer, now);
				if (!granulator.hasMoreSamples(fadingGrain, now)) {
					fadingGrain = null;
				}
			}
			now++;
            //duplicate for stereo
            output[i++] = nextSample;
            output[i++] = nextSample;
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

	private long[] createGrain(long[] targetGrain) {
		//System.out.println("create grain: " + calculateCurrentBufferIndex() + " " + now);
        targetGrain = granulator.createGrain(targetGrain, calculateCurrentBufferIndex(), now);
        return targetGrain;
	}

	private int calculateCurrentBufferIndex() {
        float sf = stretchFactor.floatValue();

		return (playOffset + Math.round((now - playTime) / sf)) % sourceBuffer.length;
	}

	public Granulize setStretchFactor(float stretchFactor) {
		playOffset = calculateCurrentBufferIndex();
		playTime = now;
		this.stretchFactor.set(stretchFactor);
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
