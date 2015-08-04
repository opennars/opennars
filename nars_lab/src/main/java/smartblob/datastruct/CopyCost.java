/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.datastruct;

/** You pay memoryCost andOr computeCost (TODO both) to copy an object.
To make an object uncopyable, set either cost very high.
*/
public interface CopyCost<T>{
	
	/** First pay memoryCostToCopy() to some Context object
	(originally part of eqxor, but expanding to datastruct package).
	*/
	public T copyAfterPayMemory();
	
	public long memoryCostToCopy();
	
	/** Estimated memory cost for "new Object()" and for each array including inner arrays.
	TODO measure this.
	*/
	public static final short bitsOverheadPerObject = 64;
	
	/** Estimated cost per boolean in memory.
	TODO measure this.
	*/
	public static final byte bitsPerBoolean = 8;

}
