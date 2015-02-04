package automenta.vivisect.audio.granular;

import automenta.vivisect.audio.sample.SonarSample;

public class Granulator {

	private final float[] sourceBuffer;
    public final float sampleRate;
    private int grainSizeSamples;
	private final GrainWindow window;

    public Granulator(SonarSample source, float grainSizeSecs, float windowSizeFactor) {
        this(source.buf, source.rate, grainSizeSecs, windowSizeFactor);
    }

	public Granulator(float[] sourceBuffer, float sampleRate, float grainSizeSecs, float windowSizeFactor) {
		this.sourceBuffer = sourceBuffer;
		this.grainSizeSamples = Math.round(sampleRate * grainSizeSecs);
		this.window = new HanningWindow(Math.round(sampleRate * grainSizeSecs * windowSizeFactor));
        this.sampleRate = sampleRate;
	}

    public boolean hasMoreSamples(long[] grain, long now) {
        final long length = grain[1];
        final long showTime = grain[2];
        return now < showTime + length + window.getSize();
    }

    public float getSample(long[] grain, long now) {
        final long startIndex = grain[0];
        final long length = grain[1];
        final long showTime = grain[2];

        final float[] sb = sourceBuffer;

        long offset = now - showTime;
        int sourceIndex = (int)((startIndex + offset + sb.length) % sb.length);
        float sample = sb[sourceIndex];
        if (offset < 0) {
            return sample * window.getFactor((int)offset);
        }
        if (offset > length) {
            return sample * window.getFactor((int)(offset - length));
        }
        return sample;
    }

    public boolean isFading(long[] grain, long now) {
        final long length = grain[1];
        final long showTime = grain[2];
        return now > showTime + length;
    }

	public long[] createGrain(long[] grain, int startIndex, long fadeInTime) {
        if (grain == null) grain = new long[3];
        grain[0] = (startIndex + window.getSize()) % sourceBuffer.length;
        grain[1] = grainSizeSamples;
        grain[2] = fadeInTime + window.getSize();
        return grain;
	}

}
