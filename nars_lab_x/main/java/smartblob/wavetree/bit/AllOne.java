/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit;

/** Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are */
public class AllOne implements Bits{
	
	public final long size;
	
	public static final double allOnesAsDouble = Double.longBitsToDouble(-1L);
	
	public static final float allOnesAsFloat = Float.intBitsToFloat(-1);
	
	public AllOne(long size){
		this.size = size;
	}
	
	public long siz(){ return size; }
	
	public long ones(){ return size; }
	
	public long zeros(){ return 0; }
	
	public long indexOfNthOne(long n){
		return n;
	}
	
	public long indexOfNthZero(long n){
		throw new IndexOutOfBoundsException("all ones");
	}
	
	public Bits pre(long endExclusive){
		return new AllOne(endExclusive);
	}
	
	public Bits suf(long start){
		return new AllOne(size-start);
	}
	
	public boolean bitAt(long index){
		return true;
	}
	
	public byte byteAt(long index){
		return (byte)-1;
	}
	
	public char charAt(long index){
		return (char)-1;
	}
	
	public short shortAt(long index){
		return (short)-1;
	}
	
	public int intAt(long index){
		return -1;
	}
	
	public long longAt(long index){
		return -1L;
	}
	
	public float floatAt(long index){ return allOnesAsFloat; }
	
	public double doubleAt(long index){ return allOnesAsDouble; }
	
	//TODO should bits and bitsJ's howManyBits param be an int for efficiency?
	
	public int bits(long start, byte howManyBits){
		if(howManyBits >= 32) return -1;
		return (1 << howManyBits)-1;
	}
	
	public long bitsJ(long start, byte howManyBits){
		if(howManyBits >= 64) return -1;
		return (1L << howManyBits)-1L;
	}
	
	public void bits(byte getBits[], long offset, long start, long end){
		throw new RuntimeException("TODO only write end-start bits, which means unless thats divisible by 8, one of the bytes will have to be read and written");
	}
	
	/** use default public int hashCode(){
		throw new RuntimeException("TODO see comment about choosing a standard hashCode for all bits objects.");
	}
	public boolean equals(Object o){
		throw new RuntimeException("TODO see comment about choosing a standard hashCode for all bits objects.");
	}*/
	
	public Bits firstOrNull(){ return null; }
	
	public Bits secondOrNull(){ return null; }
	
	public int height(){ return 0; }
	
	public int maxHeightDiff(){ return 0; }
	
	public Bits balanceTree(){ return this; }
	
	public int efficientBlockSize(){ return 1; }
	
	//Functions that are normally implemented as combinations of pre(long) andOr suf(long):
	
	public Bits cat(Bits suf){
		long sufSiz = suf.siz();
		if(sufSiz == 0) return this;
		if(suf.zeros() == 0) return new AllOne(size+sufSiz);
		return new AvlBitstring(this,suf);
	}
	
	public Bits sub(long start, long endExclusive){
		return new AllOne(endExclusive-start);
	}

	public Bits ins(Bits middle, long start){
		throw new RuntimeException("TODO");
	}

	public Bits owt(Bits middle, long start){
		throw new RuntimeException("TODO");
	}

	public Bits del(long start, long endExclusive){
		long deleteSize = endExclusive-start;
		if(deleteSize < 0) throw new IndexOutOfBoundsException("start="+start+" endExclusive="+endExclusive);
		return new AllOne(size-deleteSize);
	}

}
