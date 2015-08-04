/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit;

/** A size 2 Bits which has a 0 and a 1, but you dont know if its 01 or 10
until either index is first observed, and then its the same forever.
Its 0 bits and 1 bits can be counted wihout observing them, as long as you count
them in the whole size 2 range instead of either index alone or any range that
starts or ends in that middle. Whichever bitvar index is observed first becomes 1.
<br><br>
benfrayfieldResearch.immutableAvlBitstringAsInAndOutStream says:
A bizarre concept. avlBitstrings are immutable, but if nobody has ever read the bit
at a certain index, it could still change as long as it is always read as the same
bit forever after. This includes functions that count the number of 0 or 1 bits in
a range, unless xChooseY parts are in that range which means any y bits are 1 and x-y
bits are 0 so that count would be the same. For example, pairs of adjacent indexs that
have a 0 and a 1 but it could be 01 or 10, and depending on which of them is read first,
that index is always 1 after that, so it could be used to send bits to others who are
reading such an avlBitstring, while technically its immutable since its contents dont
exist until first observed, and order of observing could be used to choose what those
first bits are. I want a kind of avlBitstring, size 2, to be created to use as
variables that way, and some simple examples of using it to send bits between objects.
*/
public class ObserveToWrite implements Bits{
	
	public static final Fast0To16Bits zeroOne = Fast0To16Bits.get(2, 1);
	
	public static final Fast0To16Bits oneZero = Fast0To16Bits.get(2, 2);

	public long ones(){ return 1; }

	public long zeros(){ return 1; }

	public long indexOfNthOne(long n){
		throw new RuntimeException("TODO");
	}

	public long indexOfNthZero(long n){
		throw new RuntimeException("TODO");
	}

	public Bits pre(long endExclusive){
		throw new RuntimeException("TODO");
	}

	public Bits suf(long start){
		throw new RuntimeException("TODO");
	}

	public boolean bitAt(long index){
		throw new RuntimeException("TODO");
	}

	public byte byteAt(long index){
		throw new RuntimeException("TODO");
	}

	public char charAt(long index){
		throw new RuntimeException("TODO");
	}

	public short shortAt(long index){
		throw new RuntimeException("TODO");
	}

	public int intAt(long index){
		throw new RuntimeException("TODO");
	}

	public long longAt(long index){
		throw new RuntimeException("TODO");
	}

	public float floatAt(long index){
		throw new RuntimeException("TODO");
	}

	public double doubleAt(long index){
		throw new RuntimeException("TODO");
	}

	public int bits(long start, byte getHowManyBits){
		throw new RuntimeException("TODO");
	}

	public long bitsJ(long start, byte getHowManyBits){
		throw new RuntimeException("TODO");
	}

	public void bits(byte getBits[], long offset, long start, long end){
		throw new RuntimeException("TODO");
	}

	public Bits cat(Bits suf){
		throw new RuntimeException("TODO");
	}

	public Bits sub(long start, long endExclusive){
		throw new RuntimeException("TODO");
	}

	public Bits ins(Bits middle, long start){
		throw new RuntimeException("TODO");
	}

	public Bits owt(Bits middle, long start){
		throw new RuntimeException("TODO");
	}

	public Bits del(long start, long endExclusive){
		throw new RuntimeException("TODO");
	}

	public long siz(){ return 2; }

	public Bits firstOrNull(){ return null; }

	public Bits secondOrNull(){ return null; }

	public int height(){ return 0; }

	public int maxHeightDiff(){ return 0; }

	public Bits balanceTree(){ return this; }

	public int efficientBlockSize(){ return 2; }

}