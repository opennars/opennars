/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit.object;


import smartblob.wavetree.bit.Bits;

/** common.DatastructHeaders.MULTIDIMBITARRAY
benfrayfieldResearch.multidimDenseBitArrayDataFormat
<br><br>
Its purpose is to store trainingData but could be more generally useful.
First int tells how many dims. Then, for each dim, an int.
Then, the data which is size the multiply of all those ints.
Each int is at least 1 and may have many size 1 dims for purpose of
sparse dims similar to bayesaddr. A simple case is 10000 16x16 images
where each pixel is a bit. The dimensions would be 10000, 16, 16
or could also be used as 10000 256 since you can always after looping
into some number of dims ignore the later dims and use it as a flat array.
<br><br>
This is the 'a' byte. Reserving 'A' for more complex kind of this
which may be sparse or have variable size numbers or who knows what advancements.
<br><br>
Bits can represent scalars by using Double.doubleToRawLongBits(double),
Double.longBitsToDouble(long), or similar functions.
That can be slow in some implementations, depending on low level integration.
I recommend instead, when less precision is needed, using 16 bit Fast0To16Bit objects,
which are already allocated all possible values of 16 bits,
each as 4 whole number bits and 12 bits after decimal point,
as twosComplement so it ranges -8 (inclusive) to 8 (exclusive), has precision of 1/4096,
and has enough accuracy for music but it may sound low quality if not aligned right.
I plan to try this for storing the weights and node states of boltzmann machines.
If that is not enough precision, then at the cost of creating more Bits objects each
change of scalar value (or not storing them as Bits at each change),
you can have as many bits as needed, using them as integers.
I prefer this method over floating point format when practical. 
<br><br>
MultiDim can be a sparse matrix of bits or scalars by using sparse kinds of Bits,
specificly AllZero between Bits that have 1s. Whole rows/columns/etc can be skipped
by a single AllZero even across multiple dims because Bits is 1-dimensional.
Another way is to reuse repeated sections as compression.
<br><br>
benfrayfieldResearch.endianOfMultidimAndRowsVsColsOnScreen says:
I choose multidim bigEndian same as array. ... Do I want x or y to be first
in arrays and multidim? Do I want arrays and multidim to have the same
endian? The endian of avlBitstring is certainly going to stay littleEndian,
but this is awkwardly opposite of array endian which is bigEndian in
memory and how its written. Array bigEndian means an n dimensional array
is an array of nMinusOne dimensional arrays. It makes no sense to get the
smallest part of an array before the biggest part. Pointers dont work that
way. I can change which of x or y is first in array and multidim since
that is an arbitrary choice I just wanted to align to the standard of x
coming before y when written. The important question is should multidim
be same endian as arrays? Even if multidim's func of getting inner
multidim after observing certain dims is written as littleEndian, the
inner multidims are still organized as bigendian. It seems clear that
arrays and multidim must both be bigEndian, even while avlBitstring is
littleEndian (because an integer is sum of 2^n for each nth digit
thats bit1).
<br><br>
TODO important to answer benfrayfieldResearch.qnMultidimAndMulticatUseLongOrIntIndexs fast:
Should sizes be long or int? Should multidim be same of long or int? Multidim is int,
but I want multicat to be long, so should multidim be long too?
ANSWER: I choose long for both. Multicat needs long because avlBitstring is designed to
allow sparse, and may want a voxel dim or multichannel sound (as allOne vs allZero to
represent scalars like pascalstriZigzag) bigger than int range.
*/
public interface Polydim extends HeaderAndData{
	
	/** I dont expect number of dims to ever exceed int range, but since Polycat
	uses longs, and longs are the standard integer size for this software,
	it simplifies things.
	*/
	public long dims();
	
	public long dimSize(long dimIndex);
	
	/** indexs.length == dims(), and each index is in range dimSize(int) */
	public boolean bit(int... indexs);
	
	public boolean bit(long... indexs);
	
	/** If lessDims.length==0, returns this.
	If lessDims.length==dims(), returns a MultiDimBitArray whose Bits is size 1.
	If its less dims than that, returns the remaining data.
	Similar to how Java stores arrays as pointers to a polydim array with 1 less dim,
	you could give it only 1 int at a time and recurse dims.
	*/
	public Polydim bits(int... lessDims);
	//TODO MultiDim.bits(int...) decide on bigEndian (as it is now) or littleEndian (default for this software) list of dims should be.
	
	public Polydim bits(long... lessDims);
	
	/** int howManyDims and an int for size of each dim.
	Doesnt include anything which says this is a polydim, like common.DatastructHeaders.MULTIDIMBITARRAY.
	*/
	public Bits header();
	
	/** A Multidim is a view of Bits which are an immutable forest (can be merkleForest),
	so its like Subversion on many changes to the matrix not taking extra space,
	while Bits.balanceTree() keeps it from becoming too deep or slow.
	This data does not include header.
	*/
	public Bits data();
	
	/** Same as header().cat(data()) which is efficient but this could be a little faster and less memory.
	The purpose of this function is to give Multidim implementations a way to, at their option,
	avoid duplicating that, even if it is only a small constant cost to create a parent of both of those. 
	*/
	public Bits headerThenData();

}