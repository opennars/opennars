package nars.rl.elsy;

import static nars.rl.elsy.Rand.d;



/**
 * This class is used to choose random element from array
 * with the probability proportional to the value of the element
 * @author Elser
 */
public class RR {
	/**
	 * Simply returns a random object from given array. 
	 */
	public static Object pickRandom(Object[] arr) {
		return arr[Rand.randInt(arr.length)];
	}

	/**
	 * Returns the index of random element from the given array
	 * with the probability proportional to the value of the element
	 */
	public static int pickBestIndex(double[] arr) {
		double sum = 0.0;
		for (int i = 0; i < arr.length; i++) {
			double evaluate = arr[i];
			if(evaluate<0) {
				new Exception("arr[i].evaluate() < 0").printStackTrace();
			}
			sum += evaluate;
		}
		double randomPick = d(sum);
		sum = 0.0;
		for(int i= 0; i<arr.length; i++) {
			sum += arr[i];	
			if(randomPick <= sum) {
				return i;
			}
		}
		return Rand.randInt(arr.length);
	}
}
