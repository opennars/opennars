package nars.rl.lstm;

public class util 
{
	public static int argmax(final double[] vec) {
		int result = -1;
		double max = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < vec.length; i++) {
			final double v = vec[i];
			if (v > max)  {
				max = v;
				result = i;
			}
		}
		return result;
	}
	
	public static int argmin(double[] vec) {
		int result = -1;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < vec.length; i++) {
			final double v = vec[i];
			if (v < min)  {
				min = v;
				result = i;
			}
		}
		return result;
	}
}
