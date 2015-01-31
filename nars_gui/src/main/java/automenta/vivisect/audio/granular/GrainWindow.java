package automenta.vivisect.audio.granular;

public class GrainWindow {

	private final float[] factors;

	public GrainWindow(float[] factors) {
		this.factors = factors;
	}
	
	public int getSize() {
		return factors.length;
	}

	public float getFactor(long offset) {
		int index = (int) offset;
		if (offset < 0) { // Fade in
			index = - (int)offset;
		}
		return index < factors.length ? factors[index] : 0.0F;
	}

}
