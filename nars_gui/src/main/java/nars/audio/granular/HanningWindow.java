package nars.audio.granular;

public class HanningWindow implements GrainWindow {

	private final float[] factors;

	@Override
	public final int getSize() {
		return factors.length;
	}

	@Override
	public final float getFactor(int offset) {
		int index = offset;
		if (offset < 0) { // Fade in
			index = -offset;
		}
		return index < factors.length ? factors[index] : 0.0F;
	}


	public HanningWindow(int size) {
		factors = buildTable(size);
	}

	private static float[] buildTable(int size) {
		float[] result = new float[size];
		for(int i = 0; i < size; i++) {
			double x = i / (double)size;
			result[i] = (float) (0.5 * Math.cos(x * Math.PI) + 0.5);
		}
		return result;
	}

}
