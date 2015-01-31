package automenta.vivisect.audio.granular;

public class Granulator {

	private final float[] sourceBuffer;
	private int grainSizeSamples;
	private final GrainWindow window;

	public Granulator(float[] sourceBuffer, float sampleRate, float grainSizeSecs) {
		this.sourceBuffer = sourceBuffer;
		this.grainSizeSamples = Math.round(sampleRate * grainSizeSecs);
		this.window = new HanningWindow(Math.round(sampleRate * grainSizeSecs / 5.0F));
	}

	public Grain createGrain(int startIndex, long fadeInTime) {
		long showTime = fadeInTime + window.getSize();
		return new Grain(sourceBuffer, (startIndex + window.getSize()) % sourceBuffer.length, grainSizeSamples, showTime, window);
	}

}
