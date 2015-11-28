/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit;

//import coredatastruct.Internable;

/** Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
<br><br> 
Bit alignment is always littleEndian because
if used as integer then its the sum of 2^index for all index with a bit1.
<br><br>
J means 64 bit index, as in the java class file format using:
Z boolean, B byte, C char, S short, I int, J long, F float, D double, Lpackage.class; object.
<br><br>
Constant list of 1s and 0s which may be in an array or lazyEvaluated.
Worst case of log time and memory (exponentially faster than normal datastructs)
for common string functions (on bitstrings) including substring, concat,
count 0 bits or 1 bits in any subrange, insert, delete, overwrite,
and there are potential optimizations for checking string equality by
SHA256 secureHash recursively as the main datastruct is avl tree.
<br><br>
Any length is allowed. Alignment on blocks of size a power of 2 can be optimized,
like if you often use bits objects implemented as byte or int arrays.
<br><br>
Like java.nio.Buffer, bits can overlap arrays of different primitive types
in the same memory, but unlike Buffer, bits are immutable like java.lang.String.
Being immutable and efficient is the design challenge, but as demonstrated by
wavetree.scalar code on scalar numbers instead of bits, it is more than fast enough
for streaming realtime audio generated as wave amplitudes vibrating.
This bit code is a more precise version of that with no roundoffError
and designed to be more generally useful for computing functions on bit strings.
*/
public interface Bits /*extends Internable<Bits>*/{
	
	/** How many bit1? Cached using at most logBase2 steps. */
	public long ones();
	
	/** How many bit0? size()-ones(). Cached using at most logBase2 steps. */
	public long zeros();
	
	/** 0 <= n < ones(). Cached using at most logBase2 steps. */
	public long indexOfNthOne(long n);
	
	/** 0 <= n < zeros(). Cached using at most logBase2 steps. */
	public long indexOfNthZero(long n);
	
	/** prefix. Uses at most logBase2 steps (TODO what about balancing? a few times more maybe). */
	public Bits pre(long endExclusive);
	
	/** suffix.  Uses at most logBase2 steps (TODO what about balancing? a few times more maybe). */
	public Bits suf(long start);
	
	/** Uses at most logBase2 steps */
	public boolean bitAt(long index);
	
	/** Uses at most logBase2 steps
	<br><br>
	Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
	*/
	public byte byteAt(long index);
	
	/** Uses at most logBase2 steps
	<br><br>
	Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
	*/
	public char charAt(long index);
	
	/** Uses at most logBase2 steps
	<br><br>
	Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
	*/
	public short shortAt(long index);
	
	/** Uses at most logBase2 steps
	<br><br>
	Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
	*/
	public int intAt(long index);
	
	/** Uses at most logBase2 steps.
	<br><br>
	Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
	*/
	public long longAt(long index);
	
	/** Same as Float.intBitsToFloat(intAt(index)), as if created by Float.floatToRawIntBits,
	and may be more efficient in some implementations like if they store data as float array.
	<br><br>
	Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
	*/ 
	public float floatAt(long index);
	
	/** Same as Double.longBitsToDouble(longAt(index)), as if created by Double.doubleToRawLongBits,
	and may be more efficient in some implementations like if they store data as double array.
	<br><br>
	Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
	*/ 
	public double doubleAt(long index);
	
	/** Returns an int where you use the lowest getHowManyBits bits. Uses at most logBase2 steps.
	<br><br>
	Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
	<br><br>
	Gets endExclusive-start bits in the low bits of the returned int.
	For example, bits(8,8) gets the second byte.
	In this software, always use littleEndian because nth digit adds 2^n to integer value.
	<br><br>
	TODO Should this return long instead of int? It would be a little slower
	even when only getting at most 32 bits, but what if this is for the end of
	a datastruct that uses a long array and is bit aligned? It would otherwise
	have to half the time call intAt before this.
	Maybe the increased speed is worth it.
	<br><br>
	TODO check for errors with returning 32 bits. Can it return at most 31? Make sure it works for 32.
	<br><br>
	TODO should this have 2 params: start and end, both longs, like substring(long,long)?
	*/
	public int bits(long start, byte getHowManyBits);
	
	/** Returns an int where you use the lowest getHowManyBits bits.
	<br><br>
	Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
	<br><br>
	Same as bits(long,byte) except can return up to 64. TODO or is it 63? Make sure it works for 64.
	TODO should this have 2 params: start and end, both longs, like substring(long,long)?
	*/
	public long bitsJ(long start, byte getHowManyBits);
	
	/** Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are.
	<br><br>
	Writes into getBits[], littleEndian if start or end is not a multiple of 8 bits.
	If writing a partial bit, first reads the byte and keeps the bits outside the given range.
	<br><br>
	Starts writing at bit index offset in getBits[], which would normally be used to choose
	if you want the first or last byte to be whole, but you could start anywhere.
	Writes the range start to end from this Bits object.
	<br><br>
	TODO should this have 2 params: start and end, both longs, like substring(long,long)?
	Or should it be like bits(long,byte) and bitsJ(long,byte) which have a start and size?
	*/
	public void bits(byte getBits[], long offset, long start, long end);
	
	//Functions that are normally implemented as combinations of prefix(long) andOr suffix(long):
	
	/** Concat. Uses at most logBase2 steps. Can use as few as 1 step, if balancing isnt done. */
	public Bits cat(Bits suf);
	
	/** Substring of bit string. Uses at most logBase2 steps. */
	public Bits sub(long start, long endExclusive);

	/** Insert bits. Uses at most logBase2 steps. */
	public Bits ins(Bits middle, long start);
	
	/** Overwrite range start to start+middle.siz()-1. Uses at most logBase2 steps. */
	public Bits owt(Bits middle, long start);

	/** Delete range. Uses at most logBase2 steps. */
	public Bits del(long start, long endExclusive);
	
	/** Cached, takes only 1 step. */
	public long siz();
	
	/** Cached, takes only 1 step.
	<br><br>
	If this Bits is an AVL tree, returns its child containing first bits of some length.
	*/
	public Bits firstOrNull();
	
	/** Cached, takes only 1 step.
	<br><br>
	If this Bits is an AVL tree, returns its child containing bits after firstOrNull().
	*/
	public Bits secondOrNull();
	
	/** Cached, takes only 1 step.
	Leaf is 0. Heights normally fit in byte, but some algorithms may use them other ways,
	like linkedlists, or allow maxHeightDiff more than 1 to reuse more branches.
	*/
	public int height();
	//public byte height();
	
	/** Cached, takes only 1 step.
	<br><br>
	TODO If AVL rotation was done at every concat and recursively,
	then there would be no need to track maxHeightDiff since it would always be 0 or 1,
	but it would complicate the AVL tree rotation process since there would not be objects
	for each step.
	<br><br>
	If this Bits is an AVL tree, returns max difference between first.height() and second.height() of any branch.
	TODO In later versions this will be removed and instead enforce all nodes to always be balanced
	by doing tree balancing in concat func.
	*/
	public int maxHeightDiff();
	//public byte maxHeightDiff();
	
	/** TODO Are some of these functions logBase2 squared?
	Or does balanceTree only need to be done at most a constant number of times in those functions?
	AVL Tree is known to take no more than logBase2 time and memory for any single change to the tree.
	<br><br>
	Balanced means maxHeightDiff() <= 1, and returns this if so.
	<br><br>
	See comment in maxHeightDiff(). Both of these functions could be removed,
	but probably not. I'm considering allowing branches to be concat without balancing immediately
	and to expand height from byte var to int var, so they could be used as linkedlists too,
	but the main use is AVL tree which defines balanced as maxHeightDiff() <= 1.
	*/
	public Bits balanceTree();
	
	/** Cached, at most 1 step.
	<br><br>
	Normally 1 bit blocks, but some kinds of Bits store their bits in arrays or recursive Bits of a certain size.
	This is especially useful for MultiDim.
	*/
	public int efficientBlockSize();
	
	/** Like java.lang.String.intern() except uses heap instead of permgen memory.
	2 Bits of equal content, intern() on each gives same object.
	This intern() function is unfinished.
	<br><br>
	Also, see Bagaddr.intern() which is unfinished.
	*
	public Bits intern();
	*/
	
	//TODO? public askequal equalsCallsThis();
	
	//Moved here from superinterface:
	
	/** true when quantity of bits is 0, so firstBit, lastBit, chompFirstBit, chompLastBit
	should not be called. TODO what should they return?
	*
	public boolean isEmpty();
	*/
	
	/** Returns a Bits to be viewed as littleEndian unsigned integer with no 0 bits on the high end,
	so if isEmpty then this returns this, because the empty bitstring is the integer 0.
	*
	public Bits sizeUint();
	*/
	
	/** True when bits can be indexed by a 64 bit integer ("J" in java class file format).
	Theres an edge case where size is 2^64 and indexs range 0 to 2^64-1, but in that case
	you wouldnt want to use 64 bit integers, so this refers to size instead of index.
	*
	public boolean sizeFitsJ();
	
	public boolean firstBit();
	
	public boolean lastBit();
	*/
	
	/** As always, littleEndian to align best with integer digits *
	public boolean bitAt(Bits uint);
	*/
	
	/** Returns an immutable view or copy of all bits after the first.
	View vs copy or somewhere between depends on datastructs used like avlTree
	is efficient to rotate a new view in log time and memory.
	*
	public Bits chompFirstBit();
	
	public Bits chompLastBit();
	*/
	
	//public Bits concat(Bits b);
	
	/** I've decided (pending actually trying it) on a hashcode and equals standard for bitstrings,
	which is by == of objects, so no comparing by content is done. Build your own equality checker
	as some combination of statistical certainty or secureHash in some cases can be exact.
	<br><br>
	TODO decide on a standard hashcode for all bits objects,
	something that can be computed in at most log time from the kinds of calculations its optimized for.
	For example, (int)(49999*ones + zeros) would usually be a unique hashcode for bits that have
	different size or number of ones/zeros, but it would have the same hashcode for permutations of it
	which reorder any way to divide it into substrings. I want something like that combined with
	measurements of the number of 1 bits in certain ranges. Since indexOfNthOne(long) costs log time
	(except in small leaf objects which count linearly, so constant time there)
	and doesnt create new bitstring objects, its a good function to use in this hashcode standard.
	*
	public int hashCode();/*{
		throw new RuntimeException("TODO see comment about choosing a standard hashCode for all bits objects.");
	}*/

	/** I've decided (pending actually trying it) on a hashcode and equals standard for bitstrings,
	which is by == of objects, so no comparing by content is done. Build your own equality checker
	as some combination of statistical certainty or secureHash in some cases can be exact.
	<br><br>
	OLD: 2 bits objects are equal when they are same size and same bit value at each index
	*
	public boolean equals(Object o);
	*/

}