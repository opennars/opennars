/** Ben F Rayfield offers this "common" software to everyone opensource GNU LGPL */
package smartblob.common;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Random;

/** More core to the software than datastruct package, the core datastructs and interfaces etc. */
public class CoreUtil{
	
	public static double sigmoid(double x){
		return 1/(1+Math.exp(-x));
	}
	
	public static final double veryPositive = Double.MAX_VALUE/0x1000000;
	
	public static final double veryNegative = -veryPositive;
	
	/** s = 1/(1+e^-x).
	s*(1+e^-x) = 1.
	1+e^-x = 1/s.
	e^-x = 1/s - 1.
	-x = logBaseE(1/s - 1).
	x = -logBaseE(1/s - 1).
	*/
	public static double inverseSigmoid(double fraction){
		//x = -logBaseE(1/s - 1)
		//if(s == 0) return .5; //TODO verify this is on the curve
		if(fraction == 0) return veryNegative; //TODO verify this is on the curve
		double inverseS = 1/fraction;
		return -Math.log(inverseS - 1);
	}
	
	public static void main(String args[]){
		testSigmoidAndItsInverse();
		testWeightedRandomBit();
	}
		
	public static void testSigmoidAndItsInverse(){
		System.out.println("Testing sigmoid and inverseSigmoid");
		double epsilon = 1e-12;
		for(double s=0; s<=1; s+=.01){
			double x = inverseSigmoid(s);
			double ss = sigmoid(x);
			System.out.println("s="+s+" x="+x+" ss="+ss);
			if(Math.abs(s-ss) > epsilon) throw new RuntimeException("s != ss and is not close");
		}	
	}
	
	/** Uses SecureRandom and only an average of 2 random bits from it */
	public static boolean weightedRandomBit(double chance){
		return weightedRandomBit(chance, strongRand);
	}
	
	/** Consumes an average of 2 random bits (so its practical to use SecureRandom which is slow)
	by consuming random bits until get the first 1 then going directly to that digit
	in the chance as a binary fraction and returning it as the weighted random bit observe.
	TODO I wrote that code somewhere, copy it here so its more practical more often to use SecureRandom.
	*/
	public static boolean weightedRandomBit(double chance, Random rand){
		if(chance < 0 || 1 < chance) throw new ArithmeticException("chance="+chance);
		while(rand.nextBoolean()){
			if(.5 <= chance) chance -= .5;
			chance *= 2;
		}
		return .5 <= chance;
	}
	
	public static final Random weakRand;
	public static final SecureRandom strongRand;
	static{
		strongRand = new SecureRandom();
		//TODO set seed as bigger byte array, more hashcodes to fill it maybe
		strongRand.setSeed(3+System.nanoTime()*49999+System.currentTimeMillis()*new Object().hashCode());
		weakRand = new Random(strongRand.nextLong());
	}
	
	public static String nextRandomNodeName(){
		char c[] = new char[8];
		for(int i=0; i<c.length; i++) c[i] = (char)('a'+strongRand.nextInt(26));
		return new String(c);
	}
	
	public static final double epsilon = 1e-12;
	
	public static final long startMillis;
	
	public static final long startNano;
	
	static{
		startMillis = System.currentTimeMillis();
		startNano = System.nanoTime();
	}
	
	/** Seconds since year 1970
	with relative nanosecond precision (System.nanoTime)
	and absolute few milliseconds precision (System.currentTimeMillis).
	<br><br>
	Practically, at least in normal computers in year 2011, this has about microsecond precision
	because you can only run it a few million times per second.
	TODO test it again on newer computers.
	*/
	public static double time(){
		//TODO optimize by caching the 2 start numbers into 1 double */
		long nanoDiff = System.nanoTime()-startNano;
		return .001*startMillis + 1e-9*nanoDiff; 
	}
	
	protected final DecimalFormat secondsFormat = new DecimalFormat();
	
	/** Fast because it leaves it the complexity of NaN and positive/negative zero.
	TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM?
	*/
	public static double max(double x, double y){
		return x>y ? x : y;
	}
	
	/** TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM? */
	public static float max(float x, float y){
		return x>y ? x : y;
	}
	
	/** TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM? */
	public static int max(int x, int y){
		return x>y ? x : y;
	}
	
	/** Fast because it leaves it the complexity of NaN and positive/negative zero.
	TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM?
	*/
	public static double min(double x, double y){
		return x<y ? x : y;
	}
	
	/** TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM? */
	public static float min(float x, float y){
		return x<y ? x : y;
	}
	
	/** TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM? */
	public static int min(int x, int y){
		return x<y ? x : y;
	}
	
	/** Same as max(minValue, min(value, maxValue))
	TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM?
	*/
	public static double holdInRange(double min, double value, double max){
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	
	/** TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM? */
	public static float holdInRange(float min, float value, float max){
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	
	/** TODO consider using java.lang.Math funcs instead of this in case its native optimized internal to JVM? */
	public static int holdInRange(int min, int value, int max){
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	
	public static void moveToScreenCenter(Window w){
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		w.setLocation((screen.width-w.getWidth())/2, (screen.height-w.getHeight())/2);
	}
	
	/** Unlike new Color(r,g,b).getRGB(), 1-epsilon rounds to brightest.
	Truncates red, green, and blue into range 0 to 1 if needed.
	*/
	public static int color(float red, float green, float blue){
		return 0xff000000 |
			(CoreUtil.holdInRange(0, (int)(red*0x100), 0xff) << 16) |
			(CoreUtil.holdInRange(0, (int)(green*0x100), 0xff) << 8) |
			CoreUtil.holdInRange(0, (int)(blue*0x100), 0xff);
	}
	
	public static int color(float alpha, float red, float green, float blue){
		return (CoreUtil.holdInRange(0, (int)(alpha*0x100), 0xff) << 24) |
			(CoreUtil.holdInRange(0, (int)(red*0x100), 0xff) << 16) |
			(CoreUtil.holdInRange(0, (int)(green*0x100), 0xff) << 8) |
			CoreUtil.holdInRange(0, (int)(blue*0x100), 0xff);
	}
	
	public static void testWeightedRandomBit(){
		System.out.print("Testing weightRandomBit...");
		for(double targetChance=0; targetChance<1; targetChance+=.03){
			int countZeros = 0, countOnes = 0;
			for(int i=0; i<100000; i++){
				if(weightedRandomBit(targetChance,strongRand)) countOnes++;
				else countZeros++;
			}
			double observedChance = (double)countOnes/(countZeros+countOnes);
			System.out.println("targetChance="+targetChance+" observedChance="+observedChance);
			if(Math.abs(targetChance-observedChance) > .01) throw new RuntimeException("targetChance too far from observedChance");
		}
	}
	
	public static byte[] bytes(InputStream in){
		System.out.println("Reading "+in);
		try{
			byte b[] = new byte[1];
			int avail;
			int totalBytesRead = 0;
			while((avail = in.available()) != 0){
				int maxInstantCapacityNeeded = totalBytesRead+avail;
				if(b.length < maxInstantCapacityNeeded){
					byte b2[] = new byte[maxInstantCapacityNeeded*2];
					System.arraycopy(b, 0, b2, 0, totalBytesRead);
					b = b2;
				}
				//System.out.println("totalBytesRead="+totalBytesRead+" avail="+avail);
				int instantBytesRead = in.read(b, totalBytesRead, avail);
				if(instantBytesRead > 0) totalBytesRead += instantBytesRead; //last is -1
			}
			byte b2[] = new byte[totalBytesRead];
			System.arraycopy(b, 0, b2, 0, totalBytesRead);
			return b2;
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public static int readIntBigendian(byte b[], int offset){
		return (b[offset]<<24)|(b[offset+1]<<16)|(b[offset+2]<<8)|b[offset+3]; 
	}

}