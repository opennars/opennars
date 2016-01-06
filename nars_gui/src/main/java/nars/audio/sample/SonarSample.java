package nars.audio.sample;

public class SonarSample {
	public final float[] buf;
	public final float rate;

	public SonarSample(float[] buf, float rate) {
		this.buf = buf;
		this.rate = rate;
		// System.out.println("SonarSample: " + buf.length + " " + rate);
		// System.out.println(Arrays.toString(buf));
	}
}