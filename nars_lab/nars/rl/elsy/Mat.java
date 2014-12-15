package nars.rl.elsy;

import static java.lang.Math.exp;

/**
 * Some helpful math methods. 
 * Contains methods returning precomputed values of sigmoid function
 * (used by neural network).
 * @author Elser
 */
public class Mat {
	private static double sigmoPrecomputed[];
	private static double SIGMO_RANGE = 30.0; // -4.0..4.0
	static {
		sigmoPrecomputed = new double[800];
		for (int i = 0; i < sigmoPrecomputed.length; i++) {
			double d = SIGMO_RANGE*(i-sigmoPrecomputed.length/2)/sigmoPrecomputed.length;
			sigmoPrecomputed[i] = 1.0/(1.0 + exp(-d ));
		}
	}
	/**
	 * Fast version of unipolar sigmoid function.
	 */
	public static double sigmoidUniFast(double d) {
		int i = (int) (d/SIGMO_RANGE*sigmoPrecomputed.length+sigmoPrecomputed.length/2);
		if(i<0) return 0.01;
		if(i>=sigmoPrecomputed.length) return 0.99;
		return sigmoPrecomputed[i];
	}
	/**
	 * Fast version of bipolar sigmoid function.
	 */
	public static double sigmoidBiFast(double d) {
		int i = (int) (d/SIGMO_RANGE*sigmoPrecomputed.length+sigmoPrecomputed.length/2);
		if(i<0) return -0.99;
		if(i>=sigmoPrecomputed.length) return 0.99;
		return sigmoPrecomputed[i]*2-1.0;
	}
	/**
	 * Bipolar sigmoid function.
	 */
	public static double sigmoidBi(double d) {
		return 2.0/(1.0 + exp(-d))-1.0;
	}
	/**
	 * Unipolar sigmoid function.
	 */
	public static double sigmoidUni(double d) {
		return 1.0/(1.0 + exp(-d));
	}
	/**
	 * @param x
	 * @param dMin
	 * @param dMax
	 * @return true if x is inside (dMin..dMax)
	 */
	public static boolean inside(double x, double dMin, double dMax) {
		return x > dMin && x < dMax;
	}

	/**
	 * @param x
	 * @param dMax
	 * @return true if x is inside (-dMax..dMax)
	 */
	public static boolean inside(double x, double dMax) {
		return x > -dMax && x < dMax;
	}
	/**
	 * @param x
	 * @param dMax
	 * @return true if x is inside <-dMax..dMax>
	 */
	public static boolean insideInclusive(double x, double dMax) {
		return x >= -dMax && x <= dMax;
	}

	/**
	 * Returns x limited to (min..max) 
	 * @param x
	 * @param min
	 * @param max
	 */
	static public double lim(double x,double min,double max) {
		 if(x<min) return min;
		 if(x>max) return max;
		 return x;
	}
    
    static public double sqr(double d) {
        return d*d;
    }
    
    static public int sqr(int i) {
        return i*i;
    }
//    public static double fastSqrt(double x) {
//        //return Math.sqrt(x);
//        //*
//        if(x<100) {
//            return sqrtPrecomputedTo100[(int) (x*10)];
//        } else if(x<sqrtPrecomputedLimit) {
//            return sqrtPrecomputedMore100[(int) (x/10)];
//        } else {
//            return Math.sqrt(x);
//        }/**/
//    }
//
//    public static double sin(int i) {
//        return sinPrecomputed[i%360];
//    }
//
//    public static double cos(int i) {
//        return sinPrecomputed[(i+90)%360];
//    }
//
//    static public double lim(double x,double d) {
//        return lim(x,-d,d);
//    }
//
//    public static double minDist(double max, double a, double b, double c, double d) {
//        double min = max;
//        if(a>0 && a < min) {
//			min=a;
//		}
//        if(b>0 && b < min) {
//			min=b;
//		}
//        if(c>0 && c < min) {
//			min=c;
//		}
//        if(d>0 && d < min) {
//			min=d;
//		}
//        return min;
//	}
}
