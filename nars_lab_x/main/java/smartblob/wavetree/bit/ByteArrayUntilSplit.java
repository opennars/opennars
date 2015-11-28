/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit;

/** Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are
<br><br>
For efficiency of computing time and memory, need a byte array kind of Bits
(which ignores 0-7 bits of last byte) but as soon as any substring of it is
gotten (like with pre(long) or suf(long) that byte array would be copied
into normal avlBitstring then deleted, for the same reason that in later
version of java the String.substring function was changed to copy the
char array instead of return another String which points into it,
and that reason is large strings get stuck in memory when only
small substring of them is kept. This class is more general
than byte array, so it should contain a first Bits that
is copied into an avlBitstring that replaces it, and it becomes
garbageCollectible from here (set to null here), when it is first split.
Or maybe byte array is the only use case this will have,
and it would be more efficient to create the avlBitstring directly
from the byte array, so maybe should do just that one case.
*/
public class ByteArrayUntilSplit implements Bits{
	
	/** size in bits, which ignores 0-7 bits of last byte */
	public final long siz;
	public long siz(){ return siz; }
	
	/** Since Bits point at eachother in immutable merkleForest, the height of a Bits cant change.
	When certain functions are called, the bytes are converted to size 16 Fast0To16Bits,
	except the last which may be smaller, using concat in an order that results in minimum tree size.
	Before that happens, in constructor, the height it would become is calculated, and that is permanent height.
	*/
	public final int height;
	public int height(){ return height; }
	
	/** TODO I'm considering replacing this with leafsOrNull, which could exist before avlBitstringOrNull,
	or they could be created together. Its only double the objects to create the tree,
	but Fast0To16Bits are existing objects, and the other half would be new objects,
	and since this is meant to hold byte arrays that may be created really fast and soon garbage collected,
	and other times needs efficiency, may need to keep the byte array and  
	*/
	protected byte bytesOrNull[];
	
	/** Copied from bytesOrNull when first split, by pre(long) or suf(long) for example */
	protected Bits avlBitstringOrNull;
	
	/** UPDATED PLAN: continue to use bytesOrNull[] and just look up Fast0To16Bits.get(byte or short)
	when asked about, instead of storing them here. All possible values of Fast0To16Bits are already
	cached in a static array in Fast0To16Bits class.
	<br><br>
	OLD...
	<br><br>
	TODO This is approx half size of bytesOrNull. Last Bits is smaller if size not multiple of 16.
	When avlBitstringOrNull is created, its concat of many Fast0To16Bits of 16 bits each
	then whatevers left as last Bits.
	<br><br>
	TODO Should this replace bytesOrNull[] using Fast0To16Bits, or only be used when avlBitstringOrNull exists?
	Things to consider: Object overhead is a few times higher than byte array even with Fast0To16Bits optimization,
	but it would allow forming it into a tree or forest sparsely (in later versions as its openended research path),
	and more importantly you could still continue to use it as randomAccess without the log cost of tree navigation.
	*/
	protected Fast0To16Bits leafsOrNull[];
	
	protected boolean didSplit;
	/** When false, use bytesOrNull[]. When true, use avlBitstringOrNull andOr leafsOrNull[].
	Starts false. Once it becomes true, it stays true.
	*/
	public boolean didSplit(){ return didSplit; }
	
	public ByteArrayUntilSplit(byte bytes[]){
		this(bytes, bytes.length*8);
	}
	
	public static final boolean countWrapBitsAsOneMoreHeight = true;
	
	public ByteArrayUntilSplit(byte b[], long siz){
		//TODO verify size ignores 0-7 bits of last byte
		bytesOrNull = b;
		this.siz = siz;
		long blocksOf16 = (siz+15)>>4; //round up
		byte h = 0;
		while(1 < blocksOf16){
			blocksOf16 >>>= 1;
			h++;
		}
		if(countWrapBitsAsOneMoreHeight) h++;
		height = h;
	}
	
	/** FIXME Its 8 while bytesOrNull!=null. After avlBitstringOrNull is created and the bytes nulled,
	what should this return? It could return 16 since Fast0To16Bits is best used at its largest capacity.
	*/
	public int efficientBlockSize(){ return 8; }
	
	/** Regardless of didSplit, always balanced. */
	public Bits balanceTree(){ return this; }
	
	public int maxHeightDiff(){
		throw new RuntimeException("TODO Return 0 if number of Fast0To16Bits would be a power of 2 (even if last is less than 16 bits), else return 1.");
	}
	
	public long ones(){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public long zeros(){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public long indexOfNthOne(long n){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public long indexOfNthZero(long n){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public Bits pre(long endExclusive){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public Bits suf(long start){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public boolean bitAt(long index){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public byte byteAt(long index){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public char charAt(long index){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public short shortAt(long index){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public int intAt(long index){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public long longAt(long index){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public float floatAt(long index){
		return Float.intBitsToFloat(intAt(index));
	}
	
	public double doubleAt(long index){
		return Double.longBitsToDouble(longAt(index));
	}
	
	public int bits(long start, byte howManyBits){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public long bitsJ(long start, byte howManyBits){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public void bits(byte getBits[], long offset, long start, long end){
		throw new RuntimeException("TODO");
	}
	
	public Bits cat(Bits suf){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public Bits sub(long start, long endExclusive){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}

	public Bits ins(Bits middle, long start){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public Bits owt(Bits middle, long start){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}

	public Bits del(long start, long endExclusive){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public Bits firstOrNull(){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	public Bits secondOrNull(){
		if(didSplit){
			throw new RuntimeException("TODO");
		}else{
			throw new RuntimeException("TODO");
		}
	}
	
	
	/*Use byteAt instead of this, so dont really need this optimization
	public int bits(long start, byte howManyBits){
		if((start&7)==0 && howManyBits==8){ //byte aligned
			return byteAt
		}else{
			throw new RuntimeException("TODO");
		}
		// TODO Auto-generated method stub
		return 0;
	}*/
	
	//TODO is there existing byte array code implementing Bits?
	
	//TODO

}