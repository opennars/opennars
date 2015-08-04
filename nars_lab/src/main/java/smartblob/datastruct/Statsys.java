/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.datastruct;

import smartblob.wavetree.bit.Bits;

import java.util.Map;
import java.util.Random;


/** statsys means statistical system, things like boltzmann machines and bayesian networks.
<br><br>
Learning can be done in 1 big batch, many small batches, or 1 at a time (online/continuous).
Some kinds of statsys may do everything 1 at a time even if you ask for it to be done simultaneously,
which would be done automatically after requesting such a batch.
Prediction is the same as learning except it has a learningRate of 0.
<br><br>
Scalar numbers on the nodes are in fraction range (0 to 1).
This is a change from previous design of bifraction range (-1 to 1) because boltz and bayesNet
both use fraction, and only pascalstri uses bifraction. Bifraction is better aligned
to weights since it has negatives, but weights would probably be nonlinearly transformed
anyways so fraction range can just as easily translate, like using a LinearInterpolate1Var.
The main reason for using fraction range is boltz uses the extra precision of numbers
that are near 0, as far more nodes are off than on at any one time.
CellularAutomata may use bifraction range since they are helped by negatives,
and that can be scaled into fraction range by .5+.5*bifraction.
*/
public interface Statsys extends BitsVar, CopyCost<Statsys>, BitVsScalar, StatsysCost{

	
	/** number of statsysVar */
	public int size();
	
	/** All sense/predict data is in fraction range 0 to 1.
	At least until I get equals, hashCode, and garbageCollect problems with
	NsNode and Bagaddr worked out, dont require use of them.
	*/
	public void learnManyScalars(Map<double[], Double> weightedVectors, Random rand);
	
	public void learnManyBitsArray(Map<boolean[], Double> weightedVectors, Random rand);
	
	public void learnManyBitsObject(Map<Bits, Double> weightedVectors, Random rand);
	
	/** All sense/predict data is in fraction range 0 to 1.
	Same as learnOne(Vectoraddr) but without the expensive Vectoraddr object,
	as its sometimes streamed one vector at a time.
	*/
	public void learnOneScalars(double senseIn[], double learnRate, Random rand);
	
	/** All sense/predict data is in fraction range 0 to 1.
	Same as learnOne(double[],double,Random). Normally used when you know the statsys
	reduces scalars to bits bits, like some kinds of boltzmannMachine.
	*/
	public void learnOneBitsArray(boolean senseIn[], double learnRate, Random rand);
	
	public void learnOneBitsObject(Bits senseIn, double learnRate, Random rand);
	
	/** All sense/predict data is in fraction range 0 to 1.
	Same as predict(Vectoraddr,Random) but without the expensive Vectoraddr objects,
	as its normal to stream scalars or bits this way.
	For bits, optionally use predict(boolean[],Random).
	If you use a bit based Statsys as vectors,
	it will interpret equal or greater than than .5 as true, and output 0 or 1.
	*/
	public void predictScalars(double senseInPredictOut[], Random rand);
	
	/** All sense/predict data is in fraction range 0 to 1.
	Same as predict(double[],Random). Normally used when you know the statsys
	reduces scalars to bits bits, like some kinds of boltzmannMachine.
	*/
	public void predictBitsArray(boolean senseInPredictOut[], Random rand);
	
	public Bits predictBitsObject(Bits senseIn, Random rand);
	
	/** norm(0) does nothing. norm(1) does the strongest normalize and should be idempotent,
	meaning calling norm(1) 2 times consecutively should have no effect except roundoff.
	<br><br>
	normalize in a bayesnet might mean making sure all the weights per bayesnode
	sum to as close to 1.0 as possible, but that should be done automatically
	so not much point in a separate function for that, or it might mean to
	change extreme weights so they're more toward the middle, but then
	bayesRule isnt done exactly, so a bayesnet might do nothing in this norm function.
	<br><br>
	A boltzmannMachine might use it to hold its weights on a bellCurve or push them
	a little toward it.
	*/
	public void norm(double fraction);
	
	public boolean normDoesSomething();

}