/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.scalar;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;

/** All functions assume channel quantity is at least 1.
<br><br>
For compatibility between function calls, if channel quantity is equal,
the class type of the Wave returned by each function is required
to be compatible with all possible class types that any other function
of this class can return, but only if channel quantity is equal.
<br><br>
Most functions in this class have names made of these abbreviations:
amp = 1 amplitude.
amps = array of amplitudes.
sizle = len of a tree leaf. At least 1 amplitude, 1 for each channel.
len = 1 len (repeat for each amplitude).
tsize = 1 total len.
sizes = array of sizes.
tsizes = array of cumulative sizes.
hybrid = array of amplitudes len amplitudes len...
thybrid = array of amplitudes cumulativeSize amplitudes cumultativeSize... Need to know channel quantity.
chan = channel quantity.
For example, ampLeaf(double,double) returns a leaf Wave with specific amplitude and len,
and ampsSizeleaf(double[],double) returns a leaf Wave with multiple channels.
thybridChan(double[],int) returns a root Wave with specific quantity of channels.
*/
public class WaveFactory{

	public static Wave ampSizle(double amplitude, double len){
		return new DefaultWave1(new WaveTree1(amplitude,len));
	}

	public static Wave ampSizleChan(double amplitude, double len, int channels){
		WaveTree1 wt1 = new WaveTree1(amplitude,len);
		if(channels == 1) return new DefaultWave1(wt1);
		throw new RuntimeException("code not finished");
	}

	/** amplitudeForChannel0 forChannel1 forChannel2... forChannel0 ch1 ch2... third sample...
	The array is not modified.
	*/
	public static Wave ampsSizeChan(double amplitudes[], double sizeOfEachSample, int channels){
		if(amplitudes.length%channels != 0) throw new RuntimeException(
			amplitudes.length+" is not a multiple of "+channels);
		if(channels == 1){
			WaveTree1 w[] = new WaveTree1[amplitudes.length];
			for(int i=0; i<w.length; i++) w[i] = new WaveTree1(amplitudes[i],sizeOfEachSample);
			return new DefaultWave1(createRoot(w));
		}else{
			throw new RuntimeException("code not finished");
		}
	}

	/** amplitudes.length must be a multiple of sizes.length,
	and that multiple is quantity of audio channels.
	*/
	public static Wave ampsSizes(double amplitudes[], double sizes[]){
		if(amplitudes.length%sizes.length != 0) throw new RuntimeException(
			amplitudes.length+" is not a multiple of "+sizes.length);
		int channels = amplitudes.length/sizes.length;
		if(channels == 1){
			WaveTree1 w[] = new WaveTree1[amplitudes.length];
			for(int i=0; i<w.length; i++) w[i] = new WaveTree1(amplitudes[i],sizes[i]);
			return new DefaultWave1(createRoot(w));
		}else{
			throw new RuntimeException("code not finished");
		}
	}

	/** Repeatedly create parents of the trees until there is only 1 left. Does not modify the array. */
	private static WaveTree1 createRoot(WaveTree1 w[]){
		while(w.length > 1){
			//TODO: use 1 array and int count var
			WaveTree1 wNew[] = new WaveTree1[(w.length+1)/2];
			int wNewCount = 0;
			if((w.length&1) == 1) wNew[wNewCount++] = w[0];
			for(int i=w.length&1; i<w.length; i+=2){
				wNew[wNewCount++] = new WaveTree1(w[i],w[i+1]);
			}
			w = wNew;
		}
		return w[0];
	}

	private static WaveTree1[] array(WaveTree1 wt1, int quantity){
		WaveTree1 w[] = new WaveTree1[quantity];
		Arrays.fill(w,wt1);
		return w;
	}

	private static Random rand = new Random();

	public static void main(String s[]){
		test();
	}

	public static void test(){
		test(System.out);
	}

	public static void test(PrintStream logStream) throws RuntimeException{
		//TODO: run this in a loop and change the contents and sizes of the arrays
		double sizeA[] =      {7.7,7.7,7.7,7.7,7.7}; //use equalSizes function
		double sizeB[] =      {5.5,6.5,7.5,8.5,0.1,0.1,0.1,4.4,0.1,6.7};
		double sizeC[] =      {9.9,4.2,5.3,6.7};
		double sizeD[] = new double[49999];
		//double sizeD[] = new double[53];
		for(int i=0; i<sizeD.length; i++){
			sizeD[i] = thisPlusEpsilon(Math.pow(rand.nextDouble(),10),10)*1e1; //FIXME: increase the exponent here. need to prove more accuracy
		}
		double sizeE[] =      {3.3,1.4,1.5,6.6,1.7,8.8,9.9,6.6,190,143,234,3.4,6.7,3.3,1.7,110,1.1};
		double amplitudeA[] = {9.4,3.3,7.3,3.7,2.4};
		double amplitudeB[] = {5.5,8.8,-.2,6.6,4.4,9.6,9.9,9.9,9.9,8.8};
		double amplitudeC[] = {7.6,4.4,3.2,1.2};
		double amplitudeD[] = new double[49999];
		//double amplitudeD[] = new double[53];
		for(int i=0; i<amplitudeD.length; i++){
			amplitudeD[i] = Math.pow(rand.nextDouble(),10)*1e1; //FIXME: increase the exponent here. need to prove more accuracy
			if(rand.nextBoolean()) amplitudeD[i] *= -1;
		}
		double amplitudeE[] = {1.5,2.2,3.3,-.4,-.5,6.6,-.7,8.8,9.9,6.6,-90,-43,234,3.4,6.7,3.3,1.7};
		test(logStream, sizeA, sizeB, sizeC, sizeD, sizeE, amplitudeA, amplitudeB, amplitudeC, amplitudeD, amplitudeE);
	}


	public static void test(
		PrintStream logStream,
		double sizeA[], double sizeB[], double sizeC[], double sizeD[], double sizeE[],
		double amplitudeA[], double amplitudeB[], double amplitudeC[], double amplitudeD[], double amplitudeE[]
	)throws RuntimeException{
		logStream.println("Testing "+WaveFactory.class+" with 1 audio channel.");
		Arrays.fill(sizeA, sizeA[0]); //array A must be all the same size, but can have different values
		int maxBitsOfRoundoff = 10;
		double totalSizeA = sum(sizeA);
		double totalSizeB = sum(sizeB);
		double totalSizeC = sum(sizeC);
		double totalSizeD = sum(sizeD);
		double totalSizeE = sum(sizeE);
		double weightedSumA = weightedSum(sizeA,amplitudeA);
		double weightedSumB = weightedSum(sizeB,amplitudeB);
		double weightedSumC = weightedSum(sizeC,amplitudeC);
		double weightedSumD = weightedSum(sizeD,amplitudeD);
		double weightedSumE = weightedSum(sizeE,amplitudeE);
		double aveA = weightedSumA/totalSizeA;
		double aveB = weightedSumB/totalSizeB;
		double aveC = weightedSumC/totalSizeC;
		double aveD = weightedSumD/totalSizeD;
		double aveE = weightedSumE/totalSizeE;
		Wave waveA = ampsSizeChan(amplitudeA, sizeA[0], 1);
		Wave waveB = ampsSizes(amplitudeB, sizeB);
		Wave waveC = ampsSizes(amplitudeC, sizeC);
		Wave waveD = ampsSizes(amplitudeD, sizeD);
		Wave waveE = ampsSizes(amplitudeE, sizeE);
		t("waveA ave amp", waveA.aveAmp(), aveA);
		t("waveB ave amp", waveB.aveAmp(), aveB);
		t("waveC ave amp", waveC.aveAmp(), aveC);
		t("waveD ave amp", waveD.aveAmp(), aveD);
		t("waveE ave amp", waveE.aveAmp(), aveE);
		Wave waveB_reversed = waveB.reverse();
		verifyReversed("waveB and waveB_reversed", waveB, waveB_reversed);
		Wave waveB_balanced = waveB.balanceTree(); //B should have started balanced. This checks if balance destroys that.
		t("waveB_balanced ave amp", waveB_balanced.aveAmp(), aveB);
		t("waveB_balanced len", waveB_balanced.len(), totalSizeB);
		t("waveB_balanced position 0", waveB_balanced.amp(0.), amplitudeB[0]);
		t("waveB_balanced position just before end", waveB_balanced.amp(thisMinusEpsilon(waveB_balanced.len(),maxBitsOfRoundoff)), amplitudeB[amplitudeB.length-1]);
		t("waveB_balanced position end", waveB_balanced.amp(waveB_balanced.len()), amplitudeB[amplitudeB.length-1]);
		Wave waveAB = waveA.concat(waveB);
		t("waveAB ave amp", waveAB.aveAmp(), (aveA*totalSizeA+aveB*totalSizeB)/(totalSizeA+totalSizeB));
		t("waveAB len", waveAB.len(), totalSizeA+totalSizeB);
		Wave waveBC = waveB.concat(waveC);
		t("waveBC ave amp", waveBC.aveAmp(), (aveB*totalSizeB+aveC*totalSizeC)/(totalSizeB+totalSizeC));
		t("waveBC len", waveBC.len(), totalSizeB+totalSizeC);
		t("waveBC position 0", waveBC.amp(0.), amplitudeB[0]);
		t("waveBC position just before end", waveBC.amp(thisMinusEpsilon(waveBC.len(),maxBitsOfRoundoff)), amplitudeC[amplitudeC.length-1]);
		t("waveBC position end", waveBC.amp(waveBC.len()), amplitudeC[amplitudeC.length-1]);
		Wave waveCD = waveC.concat(waveD);
		t("waveCD ave amp", waveCD.aveAmp(), (aveC*totalSizeC+aveD*totalSizeD)/(totalSizeC+totalSizeD));
		t("waveCD len", waveCD.len(), totalSizeC+totalSizeD);
		Wave waveDE = waveD.concat(waveE);
		t("waveDE ave amp", waveDE.aveAmp(), (aveD*totalSizeD+aveE*totalSizeE)/(totalSizeD+totalSizeE));
		t("waveDE len", waveDE.len(), totalSizeD+totalSizeE);
		Wave waveBC_balanced = waveBC.balanceTree();
		t("waveBC_balanced ave amp", waveBC_balanced.aveAmp(), (aveB*totalSizeB+aveC*totalSizeC)/(totalSizeB+totalSizeC));
		t("waveBC_balanced len", waveBC_balanced.len(), totalSizeB+totalSizeC);
		verify2WavesApproxEqual(100, 50, //FIXME: use bigger first parameter
			"comparing waveBC to itself to test the verify2WavesApproxEqual function.",
			waveBC, waveBC, 1e-8);
		Wave epsilonWaveForBC = WaveFactory.ampSizle(0.,waveBC.len()/1e9);
		Wave waveBC_aLittleBigger = epsilonWaveForBC.concat(waveBC).concat(epsilonWaveForBC);
		t("waveBC_aLittleBigger len",
			waveBC.len()+2*epsilonWaveForBC.len(), waveBC_aLittleBigger.len(), 1e-14);
		verify2WavesApproxEqual(100, 50, //FIXME: use bigger first parameter
			"comparing waveBC to itself between 2 very small waves to test the verify2WavesApproxEqual function on slightly different waves.",
			waveBC, waveBC_aLittleBigger, 1e-8);
		verify2WavesApproxEqual(100, 50, "comparing waveBC to waveBC_balanced.", //FIXME: use bigger first parameter
			waveBC, waveBC_balanced, 1e-8);
		t("waveBC_balanced position 0", waveBC_balanced.amp(0.), amplitudeB[0]);
		t("waveBC_balanced position just before end",
			waveBC_balanced.amp(thisMinusEpsilon(waveBC_balanced.len(),maxBitsOfRoundoff)),
			amplitudeC[amplitudeC.length-1]);
		t("waveBC_balanced position end", waveBC_balanced.amp(waveBC_balanced.len()), amplitudeC[amplitudeC.length-1]);
		Wave waveBCDE = waveBC.concat(waveDE);
		double weightedAveBCDE = aveB*totalSizeB+aveC*totalSizeC+aveD*totalSizeD+aveE*totalSizeE;
		double totalSizeBCDE = totalSizeB+totalSizeC+totalSizeD+totalSizeE;
		t("waveBCDE ave amp", waveBCDE.aveAmp(), weightedAveBCDE/totalSizeBCDE);
		t("waveBCDE len", waveBCDE.len(), totalSizeB+totalSizeC+totalSizeD+totalSizeE);
		t("thisMinusEpsilon", waveBCDE.len()-thisMinusEpsilon(waveBCDE.len(),maxBitsOfRoundoff) > 0);
		t("waveBCDE position 0", waveBCDE.amp(0.), amplitudeB[0]);
		t("waveBCDE position just before end",
			waveBCDE.amp(thisMinusEpsilon(waveBCDE.len(),maxBitsOfRoundoff)),
			amplitudeE[amplitudeE.length-1]);
		t("waveBCDE position end", waveBCDE.amp(waveBCDE.len()), amplitudeE[amplitudeE.length-1]);
		Wave waveBCDE_balanced = waveBCDE.balanceTree();
		t("waveBCDE_balanced ave amp", waveBCDE_balanced.aveAmp(), weightedAveBCDE/totalSizeBCDE);
		t("waveBCDE_balanced len", waveBCDE_balanced.len(), totalSizeBCDE);
		t("waveBCDE_balanced position 0", waveBCDE_balanced.amp(0.), amplitudeB[0]);
		t("waveBCDE_balanced position just before end", waveBCDE_balanced.amp(thisMinusEpsilon(waveBCDE_balanced.len(),maxBitsOfRoundoff)), amplitudeE[amplitudeE.length-1]);
		t("waveBCDE_balanced position end", waveBCDE_balanced.amp(waveBCDE_balanced.len()), amplitudeE[amplitudeE.length-1]);
		Wave waveBCDEx5 = waveBCDE.concat(waveBCDE).concat(waveBCDE).concat(waveBCDE).concat(waveBCDE);
		t("waveBCDEx5 ave amp", waveBCDEx5.aveAmp(), weightedAveBCDE/totalSizeBCDE);
		Wave waveCD_BCDEx5_DE = waveCD.concat(waveBCDEx5).concat(waveDE);
		t("waveCD_BCDEx5_DE len", waveCD_BCDEx5_DE.len(), totalSizeB*5+totalSizeC*6+totalSizeD*7+totalSizeE*6);
		double waveCD_BCDEx5_DE_correctAveAmp = (aveC*totalSizeC+2*aveD*totalSizeD+5*weightedAveBCDE+aveE*totalSizeE)/(totalSizeC+2*totalSizeD+5*totalSizeBCDE+totalSizeE);
		t("waveCD_BCDEx5_DE ave amp", waveCD_BCDEx5_DE.aveAmp(), waveCD_BCDEx5_DE_correctAveAmp);
		t("waveCD_BCDEx5_DE position 0", waveCD_BCDEx5_DE.amp(0.), amplitudeC[0]);
		t("waveCD_BCDEx5_DE position just before end", waveCD_BCDEx5_DE.amp(thisMinusEpsilon(waveCD_BCDEx5_DE.len(),maxBitsOfRoundoff)), amplitudeE[amplitudeE.length-1]);
		t("waveCD_BCDEx5_DE position end", waveCD_BCDEx5_DE.amp(waveCD_BCDEx5_DE.len()), amplitudeE[amplitudeE.length-1]);
		Wave waveCD_BCDEx5_DE_balanced = waveCD_BCDEx5_DE.balanceTree();
		t("waveCD_BCDEx5_DE_balanced ave amp", waveCD_BCDEx5_DE_balanced.aveAmp(), waveCD_BCDEx5_DE_correctAveAmp);
		t("waveCD_BCDEx5_DE_balanced position 0", waveCD_BCDEx5_DE_balanced.amp(0.), amplitudeC[0]);
		t("waveCD_BCDEx5_DE_balanced position just before end", waveCD_BCDEx5_DE_balanced.amp(thisMinusEpsilon(waveCD_BCDEx5_DE_balanced.len(),maxBitsOfRoundoff)), amplitudeE[amplitudeE.length-1]);
		t("waveCD_BCDEx5_DE_balanced position end", waveCD_BCDEx5_DE_balanced.amp(waveCD_BCDEx5_DE_balanced.len()), amplitudeE[amplitudeE.length-1]);
		Wave waveCD_BCDEx5_DE_balanced_reversed = waveCD_BCDEx5_DE_balanced.reverse();
		t("waveCD_BCDEx5_DE_balanced ave amp reversed", waveCD_BCDEx5_DE_balanced_reversed.aveAmp(), waveCD_BCDEx5_DE_correctAveAmp);
		Wave waveCD_BCDEx5_DE_balanced_forwardBackwardForward = waveCD_BCDEx5_DE_balanced
			.concat(waveCD_BCDEx5_DE_balanced_reversed).concat(waveCD_BCDEx5_DE_balanced);
		t("waveCD_BCDEx5_DE_balanced_forwardBackwardForward ave amp",
			waveCD_BCDEx5_DE_balanced_forwardBackwardForward.aveAmp(), waveCD_BCDEx5_DE_correctAveAmp);
		verifyForwardConcatBackwardConcatForward("waveCD_BCDEx5_DE_balanced_forwardBackwardForward",
			waveCD_BCDEx5_DE_balanced_forwardBackwardForward);
		Wave waveCD_BCDEx5_DE_balanced_forwardBackwardForward_balanced =
			waveCD_BCDEx5_DE_balanced_forwardBackwardForward.balanceTree();
		verifyForwardConcatBackwardConcatForward("waveCD_BCDEx5_DE_balanced_forwardBackwardForward_balanced",
				waveCD_BCDEx5_DE_balanced_forwardBackwardForward_balanced);
		//FIXME: more tests. use waveA
		logStream.println("Done testing "+WaveFactory.class+" with 1 audio channel. All tests pass.");
	}

	private static double weightedSum(double weights[], double values[]){
		double sum = 0;
		for(int i=0; i<weights.length; i++) sum += weights[i]*values[i];
		return sum;
	}

	private static double sum(double values[]){
		double sum = 0;
		for(int i=0; i<values.length; i++) sum += values[i];
		return sum;
	}

	private static void t(String description, boolean b){
		if(!b) throw new RuntimeException(description);
	}

	private static void t(String description, double a, double b){
		t(description,a,b,1e-8);
	}

	private static void t(String description, double a, double b, double maxRelativeDiff){
		t(description+" "+a+" "+b, areClose(a,b,maxRelativeDiff));
	}

	/** maxRelativeDiff is relative to 1.0, not the value of the doubles */
	private static boolean areClose(double a, double b, double maxRelativeDiff){
		if(a == 0 && b == 0) return true; //TODO: doesnt handle all errors
		if(a == 0 || b == 0) return false; //TODO: doesnt handle all errors
		double d = a/b;
		return 1-maxRelativeDiff < d && d < 1+maxRelativeDiff;
	}

	/** doubles have 52 digit bits, 11 exponent bits, and 1 sign bit.
	allowedBitsOfRoundoffError comes out of the 52.
	*/
	private static double thisMinusEpsilon(double d, int allowedBitsOfRoundoffError){
		//TODO: verify allowedBitsOfRoundoffError is not off by 1
		return d * (1 - 1./(1L << 52-allowedBitsOfRoundoffError));
	}

	private static double thisPlusEpsilon(double d, int allowedBitsOfRoundoffError){
		return d * (1 + 1./(1L << 52-allowedBitsOfRoundoffError));
	}

	private static void verifyReversed(String varNameBeingTested, Wave wave, Wave waveReversed){
		String prefix = "verifyReversed "+varNameBeingTested+" ";
		//test many floating point pseudorandom positions
		if(!areClose(wave.len(),waveReversed.len(),1e-8)) throw new RuntimeException(
			"wave len "+wave.len()+" is not close to "+waveReversed.len());
		double s = wave.len(), inc = s/(345*Math.PI), incSine = 3.1334, circle = .123;
		int pointsTried = 0;
		for(double p=0; p<s; p+=inc*(.2+Math.sin(circle+=incSine))){
			for(int direction=0; direction<2; direction++){
				pointsTried++;
				double position = direction==0 ? p : s-p;
				double positionReversed = direction==0 ? s-p : p;
				double value = wave.amp(position), valueReversed = waveReversed.amp(positionReversed);
				t(
					prefix+WaveOps.describeWave(wave,100)+" reversed is "
					+WaveOps.describeWave(waveReversed,100)
					+" position="+position+" value="+value
					+" positionReversed="+positionReversed+" valueReversed="+valueReversed
					+" triedThisManyPoints="+pointsTried
				,
					areClose(value,valueReversed,1e-8)
				);
			}
		}
	}

	private static void verifyForwardConcatBackwardConcatForward(String varNameBeingTested, Wave wave){
		String prefix = "verifyForwardConcatBackwardConcatForward "+varNameBeingTested+" ";
		//test many floating point pseudorandom positions
		double s = wave.len()/3, inc = s/(345*Math.PI), incSine = 3.1334, circle = .123;
		int pointsTried = 0;
		for(double p=0; p<s; p+=inc*(.2+Math.sin(circle+=incSine))){
			for(int direction=0; direction<2; direction++){
				pointsTried++;
				double position1 = direction==0 ? p : s-p, position2 = 2*s-position1, position3 = 2*s+position1;
				double value1 = wave.amp(position1), value2 = wave.amp(position2), value3 = wave.amp(position3);
				t(
					prefix+WaveOps.describeWave(wave,100)
					+" position1="+position1+" value1="+value1
					+" position2="+position2+" value2="+value2
					+" position3="+position3+" value3="+value3+" triedThisManyPoints="+pointsTried
				,
					value1==value2 && value2==value3
				);
			}
		}
	}

	private static double[] getSortedPseudorandomIndexsInThisRange(
		boolean includeStart, double start, int requredArraySize, double end, boolean includeEnd
	){
		double d[] = new double[requredArraySize];
		double len = end-start;
		if(len <= 0) throw new RuntimeException("start="+start+" end="+end);
		for(int i=0; i<d.length; i++) d[i] = start+rand.nextDouble()*len;
		if(includeStart) d[0] = start;
		if(includeEnd) d[d.length-1] = end;
		Arrays.sort(d);
		while(!includeStart && d[0]==start){ //rare
			d[0] = start+rand.nextDouble()*len;
			Arrays.sort(d);
		}
		while(!includeEnd && d[d.length-1]==end){ //rare
			d[d.length-1] = start+rand.nextDouble()*len;
			Arrays.sort(d);
		}
		//FIXME: dont let any 2 adjacent values be areClose(double,double)
		return d;
	}

	private static double[] valuesAtPositionsInWave(double positions[], Wave wave){
		double values[] = new double[positions.length];
		for(int i=0; i<values.length; i++) values[i] = wave.amp(positions[i]);
		return values;
	}



	/** looks in totalPosQuantity/2 positions in each Wave
	and throws if no value in waveA is close(double,double) to a value in waveB.
	*/
	private static void verifyThatAny2ValuesInThese2RangesAreClose(
		int totalPosQuantity, String errorMessagePrefix,
		Wave waveA, double posAStart, double posAEnd,
		Wave waveB, double posBStart, double posBEnd,
		double maxRelativeDiff
	){
		if(totalPosQuantity < 6) throw new RuntimeException(
			"totalPosQuantity is "+totalPosQuantity
			+" but must be at least 6 and should probably be much higher");
		int len = totalPosQuantity/2;
		double posA[] = getSortedPseudorandomIndexsInThisRange(true,posAStart,len,posAEnd,true);
		double posB[] = getSortedPseudorandomIndexsInThisRange(true,posBStart,len,posBEnd,true);
		double valuesA[] = valuesAtPositionsInWave(posA, waveA);
		Arrays.sort(valuesA);
		double valuesB[] = valuesAtPositionsInWave(posB, waveB);
		Arrays.sort(valuesB);
		//TODO: optimize this. It could be bigO(len) instead of bigO(len^2)
		for(int a=0; a<len; a++){
			for(int b=0; b<len; b++){
				if(areClose(valuesA[a],valuesB[b],maxRelativeDiff)) return;
			}
		}
		String s = " no close values found in "+WaveOps.describeWave(waveA,100)+" from "+posAStart+" to "+posAEnd
			+" compared to "+((posAStart==posBStart&&posAEnd==posBEnd)
				?("the same range"):("range "+posBStart+" to "+posBEnd));
		if(waveA != waveB) s += " in "+WaveOps.describeWave(waveB,100);
		s += " but I only looked in "+totalPosQuantity+" positions";
		throw new RuntimeException(errorMessagePrefix.trim()+" "+s);
	}

	/** At outerPositionQuantity position ranges, tries innerPositionQuantity/2 total positions
	and must find 1 inner position in each wave thats equal to an inner position in the other wave.
	All of that at each outer range.
	*/
	private static void verify2WavesApproxEqual(int outerPositionQuantity, int innerPositionQuantity,
			String errorMessagePrefix, Wave waveA, Wave waveB, double maxRelativeDiff){
		if(!areClose(waveA.len(),waveB.len(),maxRelativeDiff)) throw new RuntimeException(
			"waveA len "+waveA.len()+" is not close to waveB len "+waveB.len());
		if(outerPositionQuantity < 50) throw new RuntimeException(
			"outerPositionQuantity is "+outerPositionQuantity+" but must be at least 50");
		if(innerPositionQuantity < 6) throw new RuntimeException(
			"innerPositionQuantity is "+innerPositionQuantity+" but must be at least 6");
		double len = Math.min(waveA.len(),waveB.len());
		//FIXME: modify getSortedPseudorandomIndexsInThisRange function to have extra parameter that specifies minimum difference between adjacent values in the returned array
		double outerPos[] = getSortedPseudorandomIndexsInThisRange(true,0.,outerPositionQuantity+1,len,true);
		for(int outer=0; outer<outerPositionQuantity; outer++){
			String err = "outer position range number "+outer
				+" of "+outerPositionQuantity+"-1 parts is from "
				+outerPos[outer]+" to "+outerPos[outer+1]+". "+errorMessagePrefix;
			verifyThatAny2ValuesInThese2RangesAreClose(innerPositionQuantity, err,
				waveA, outerPos[outer], outerPos[outer+1],
				waveB, outerPos[outer], outerPos[outer+1], maxRelativeDiff);
		}
	}

}
