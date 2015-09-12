package nars.audio.granular;

public class GrainWindow {

	private final float[] factors;

	public GrainWindow(float[] factors) {
		this.factors = factors;
	}
	
	public final int getSize() {
		return factors.length;
	}

	public final float getFactor(int offset) {
		int index = offset;
		if (offset < 0) { // Fade in
			index = -offset;
		}
		return index < factors.length ? factors[index] : 0.0F;
	}

}
