/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit;

/** Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are */
public class AllZero implements Bits{
	
	public final long size;
	
	public static final double allZerosAsDouble = Double.longBitsToDouble(0L);
	
	public static final float allZerosAsFloat = Float.intBitsToFloat(0);
	
	public AllZero(long size){
		this.size = size;
	}
	
	public long siz(){ return size; }
	
	public long ones(){ return 0; }
	
	public long zeros(){ return size; }
	
	public long indexOfNthOne(long n){
		throw new IndexOutOfBoundsException("all zeros");
	}
	
	public long indexOfNthZero(long n){
		return n;
	}
	
	public Bits pre(long endExclusive){
		return new AllZero(endExclusive);
	}
	
	public Bits suf(long start){
		return new AllZero(size-start);
	}
	
	public boolean bitAt(long index){
		return false;
	}
	
	public byte byteAt(long index){
		return 0;
	}
	
	public char charAt(long index){
		return 0;
	}
	
	public short shortAt(long index){
		return 0;
	}
	
	public int intAt(long index){
		return 0;
	}
	
	public long longAt(long index){
		return 0;
	}
	
	public float floatAt(long index){ return allZerosAsFloat; }
	
	public double doubleAt(long index){ return allZerosAsDouble; }
	
	public int bits(long start, byte howManyBits){
		return 0;
	}
	
	public long bitsJ(long start, byte howManyBits){
		return 0;
	}
	
	public void bits(byte getBits[], long offset, long start, long end){
		throw new RuntimeException("TODO");
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
		if(suf.ones() == 0) return new AllZero(size+sufSiz);
		return new AvlBitstring(this,suf);
	}
	
	public Bits sub(long start, long endExclusive){
		return new AllZero(endExclusive-start);
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
		return new AllZero(size-deleteSize);
	}


}
