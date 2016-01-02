package nars.audio.granular;

import nars.audio.SoundProducer;
import nars.audio.sample.SonarSample;
import nars.util.data.MutableDouble;
import nars.util.data.random.XorShift128PlusRandom;

import java.util.Random;

public class Granulize implements SoundProducer, SoundProducer.Amplifiable {

	private final float[] sourceBuffer;
	private float now = 0L;

    /** this actually represents the target amplitude which the current amplitude will continuously interpolate towards */
    public final MutableDouble amplitude = new MutableDouble(1.0);

    protected float currentAmplitude = amplitude.floatValue();

	public final MutableDouble stretchFactor = new MutableDouble(1.0);
    public final MutableDouble pitchFactor = new MutableDouble(1.0);

    /** grains are represented as a triple of long integers (see Granulator.createGrain() which constructs these) */
	private long[] currentGrain = null;
	private long[] fadingGrain = null;

	private final Granulator granulator;
	private boolean isPlaying = false;
	private float playTime = 0L;
	private int playOffset = -1;



	public Granulize(SonarSample s, float grainSizeSecs) {
		this(s, grainSizeSecs, 1.0f);
	}

    public Granulize(SonarSample s, float grainSizeSecs, float windowSizeFactor) {
        this(s.buf, s.rate, grainSizeSecs, windowSizeFactor);
    }

	public Granulize(float[] buffer, float sampleRate, float grainSizeSecs, float windowSizeFactor) {

		sourceBuffer = buffer;

		granulator = new Granulator(buffer, sampleRate, grainSizeSecs, windowSizeFactor);

		play();
	}

	public Granulize at(int pos) {
		playOffset = pos;
		return this;
	}

	public void process(float[] output, int readRate) {
		if (currentGrain == null && isPlaying) {
			currentGrain = nextGrain(currentGrain);
		}
        float dNow = ((granulator.sampleRate / readRate)) * pitchFactor.floatValue();

        float amp = currentAmplitude;
        float dAmp = (amplitude.floatValue() - amp) / output.length;

        float n = now;

        Granulator g = granulator;


        boolean p = isPlaying;
        if (!p)
            dAmp = (0 - amp) / output.length; //fade out smoothly if isPlaying false

        long samples = output.length;

        long[] cGrain = currentGrain;
        long[] fGrain = fadingGrain;

		for (int i = 0; i < samples; i++ ) {
            float nextSample = 0;
            long lnow = (long)n;
			if (cGrain != null) {
				nextSample = g.getSample(cGrain, lnow);
				if (Granulator.isFading(cGrain, lnow)) {
					fGrain = cGrain;
					cGrain = p ? nextGrain(cGrain) : null;
				}
			}
			if (fGrain != null) {
                nextSample += g.getSample(fGrain, lnow);
				if (!g.hasMoreSamples(fGrain, lnow)) {
					fGrain = null;
				}
			}
			n += dNow;
            output[i] = nextSample * amp;
            amp += dAmp;
		}


        //access and modify these fields only outside of the critical rendering loop
        currentGrain = cGrain;
        fadingGrain = fGrain;
        now = n;
        currentAmplitude = amp;
	}

    @Override
	public final void setAmplitude(float amplitude) {
        this.amplitude.set(amplitude);
    }

    @Override
    public float getAmplitude() {
        return amplitude.floatValue();
    }

	final Random rng = new XorShift128PlusRandom(1);

    public void play() {
		playOffset = rng.nextInt();
		playTime = now;
		isPlaying = true;
	}

	/** continue */
	public void cont() {
		isPlaying = true;
	}

	@Override
	public void stop() {
		isPlaying = false;
	}

	private long[] nextGrain(long[] targetGrain) {
		//System.out.println("create grain: " + calculateCurrentBufferIndex() + " " + now);
        targetGrain = granulator.nextGrain(targetGrain, calculateCurrentBufferIndex(), (long)now);
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
        return 0.0f;
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
