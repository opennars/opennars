package automenta.vivisect.audio.granular;

public class Grain {

	private final float[] sourceBuffer;
	private final int startIndex;
	private final int length;
	private final long showTime;
	
	private final GrainWindow window;

	public Grain(float[] sourceBuffer, int startIndex, int length, long showTime, GrainWindow window) {
		this.sourceBuffer = sourceBuffer;
		this.startIndex = startIndex;
		this.length = length;
		this.showTime = showTime;
		this.window = window;
	}

	public boolean hasMoreSamples(long now) {
		return now < showTime + length + window.getSize();
	}

	public float getSample(long now) {
		long offset = now - showTime;
		int sourceIndex = (startIndex + (int)offset + sourceBuffer.length) % sourceBuffer.length;
		float sample = sourceBuffer[sourceIndex];
		if (offset < 0) {
			return sample * window.getFactor(offset);
		}
		if (offset > length) {
			return sample * window.getFactor(offset - length);
		}
		return sample;
	}

	public boolean isFading(long now) {
		return now > showTime + length;
	}

}
