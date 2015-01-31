package automenta.vivisect.audio.granular;


public class TimeStretcherMax {
	
	private class Grain {
		public long startTime;
		public int startIndex;
		public int length;
		
		public Grain(long startTime, int startIndex, int length) {
			this.startTime = startTime;
			this.startIndex = startIndex;
			this.length = length;
		}
	}

	private final float[] sourceBuffer;
	private long now = 0L;
	private float stretchFactor = 1.0F;
	
	private Grain currentGrain = null;
	private Grain fadingGrain = null;
	
	private boolean isPlaying = false;
	private long playTime = 0L;
	private int playOffset;
	private int grainSizeSmp;
	
	private float[] volumeEnvelope;
	private int fadeTimeSmp;

	public TimeStretcherMax(float[] sourceBuffer, float sampleRate, float grainSizeSecs) {
		this.sourceBuffer = sourceBuffer;
		grainSizeSmp = Math.round(sampleRate * grainSizeSecs);
		fadeTimeSmp = Math.round(sampleRate * grainSizeSecs / 5.0F);
		
		volumeEnvelope = new float[fadeTimeSmp];
		for(int i = 0; i < volumeEnvelope.length; i++) {
			double x = i / (double)volumeEnvelope.length;
			volumeEnvelope[i] = (float) (0.5 * Math.cos(x * Math.PI) + 0.5);
		}
	}

	public void process(float[] samples) {
		if (currentGrain == null && isPlaying) {
			currentGrain = createGrain();
		}
		for (int i = 0; i < samples.length; i++) {
			if (currentGrain != null) {
				samples[i] = getSample(currentGrain);
				if (now > (currentGrain.startTime + currentGrain.length - fadeTimeSmp)) {
					fadingGrain = currentGrain;
					currentGrain = isPlaying ? createGrain() : null;
				}
			}
			if (fadingGrain != null) {
				samples[i] += getSample(fadingGrain);
				if (now > fadingGrain.startTime + fadingGrain.length) {
					fadingGrain = null;
				}
			}
			now++;
		}
	}

	private float getSample(Grain grain) {
		int offset = (int)(now - grain.startTime);
		if (offset < 0 || offset > grain.length) {
			return 0.0F;
		}
		float windowFactor = 1.0F;
		if (offset < volumeEnvelope.length) {
			windowFactor = volumeEnvelope[offset];
		} else if (offset > (grain.length - volumeEnvelope.length)) {
			windowFactor = volumeEnvelope[grain.length - offset];
		}
		return windowFactor * sourceBuffer[(grain.startIndex + offset) % sourceBuffer.length];
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
		return new Grain(now, calculateCurrentBufferIndex(), grainSizeSmp);
	}

	private int calculateCurrentBufferIndex() {
		return (playOffset + Math.round((now - playTime) / stretchFactor)) % sourceBuffer.length;
	}

	public void setStretchFactor(float stretchFactor) {
		playOffset = calculateCurrentBufferIndex();
		playTime = now;
		this.stretchFactor = stretchFactor;
	}

}
