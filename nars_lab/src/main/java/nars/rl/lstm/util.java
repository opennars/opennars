package nars.rl.lstm;

public class util 
{
	public static int argmax(double[] vec) {
		int result = -1;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < vec.length; i++) {
			if (vec[i] > max)
			{
				max = vec[i];
				result = i;
			}
		}
		return result;
	}
	
	public static int argmin(double[] vec) {
		int result = -1;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < vec.length; i++) {
			if (vec[i] < min)
			{
				min = vec[i];
				result = i;
			}
		}
		return result;
	}
}
