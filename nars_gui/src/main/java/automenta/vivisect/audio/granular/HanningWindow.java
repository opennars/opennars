package automenta.vivisect.audio.granular;

public class HanningWindow extends GrainWindow {
	
	public HanningWindow(int size) {
		super(buildTable(size));
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
