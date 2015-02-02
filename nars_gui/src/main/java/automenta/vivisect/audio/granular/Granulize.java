package automenta.vivisect.audio.granular;

import automenta.vivisect.audio.SoundProducer;
import automenta.vivisect.audio.sample.SonarSample;
import reactor.jarjar.jsr166e.extra.AtomicDouble;

public class Granulize implements SoundProducer {

	private final float[] sourceBuffer;
	private float now = 0L;
	public final AtomicDouble stretchFactor = new AtomicDouble(1.0);
    public final AtomicDouble pitchFactor = new AtomicDouble(1.0);
	private long[] currentGrain = null;
	private long[] fadingGrain = null;
	private final Granulator granulator;
	private boolean isPlaying = false;
	private float playTime = 0L;
	private int playOffset;



    public Granulize(SonarSample s, float grainSizeSecs, float windowSizeFactor) {
        this(s.buf, s.rate, grainSizeSecs, windowSizeFactor);
        play();
    }

	public Granulize(float[] buffer, float sampleRate, float grainSizeSecs, float windowSizeFactor) {
		this.sourceBuffer = buffer;
		this.granulator = new Granulator(buffer, sampleRate, grainSizeSecs, windowSizeFactor);
	}

	public void process(float[] output, int readRate) {
		if (currentGrain == null && isPlaying) {
			currentGrain = createGrain(currentGrain);
		}
        final float dNow = ((granulator.sampleRate / (float)readRate)) * pitchFactor.floatValue();
		for (int i = 0; i < output.length; i++ ) {
            float nextSample = 0;
            long lnow = (long)now;
			if (currentGrain != null) {
				nextSample = granulator.getSample(currentGrain, sourceBuffer, lnow);
				if (granulator.isFading(currentGrain, lnow)) {
					fadingGrain = currentGrain;
                    if (isPlaying)
                        currentGrain = createGrain(currentGrain);
                    else
                        currentGrain = null;
				}
			}
			if (fadingGrain != null) {
                nextSample += granulator.getSample(fadingGrain, sourceBuffer, lnow);
				if (!granulator.hasMoreSamples(fadingGrain, lnow)) {
					fadingGrain = null;
				}
			}
			now += dNow;
            output[i] = nextSample;
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
        targetGrain = granulator.createGrain(targetGrain, calculateCurrentBufferIndex(), (long)now);
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
        process(buf, readRate);
        return 0f;
    }

    @Override
    public void skip(int samplesToSkip, int readRate) {
        //TODO
    }

    @Override
    public boolean isLive() {
        return isPlaying;
    }



}
