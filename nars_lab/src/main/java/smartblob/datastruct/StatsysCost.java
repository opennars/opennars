/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.datastruct;

/** Compute and memory costs for the things statsys does and how it can change
depending on what calls you make.
*/
public interface StatsysCost{
	
	/** Total memory used now by the Statsys */
	public double memoryCost();
	
	/** Max memory of new datastructs which become a permanent part of the Statsys,
	which may be added in each call of a func described by StatsysFuncType.
	<br><br>
	An example of nonzero max cost is a bayesNet which sometimes adds more bayesNode on
	new combinations of existing bayesVar when new  
	*/
	public double maxMemoryIncreasePerVector(StatsysFuncType funcType);
	
	//TODO? public double aveMemoryIncreasePerCall(StatsysFuncType funcType);
	
	public double maxComputeCostPerVector(StatsysFuncType funcType);
	
	//TODO public double aveComputeCost(StatsysFuncType funcType);
	
	/** Returns true if the StatsysCost funcs of this object always return the same for the same input.
	Example: If this is a mutable statsys but it never changes array sizes inside itself,
	just changes the array contents, then memory() would be constant,
	and maxMemoryIncreasePerCall would be 0.
	Or if this StatsysCost is an object that only describes cost and is not a statsys,
	it would be best to be immutable.
	*/ 
	public boolean statsysCostIsImmutable();

}