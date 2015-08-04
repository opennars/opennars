/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit.object;

import smartblob.wavetree.bit.Bits;

/** blocks of 16 bits, littleEndian as always */
public class BitsAsCharSequence implements CharSequence{
	
	public final Bits data;
	
	public BitsAsCharSequence(Bits data){
		this.data = data;
		if(data.siz() >> 4 > Integer.MAX_VALUE) throw new IndexOutOfBoundsException(
			"more bits "+data.siz()+" than blocks of 16 can fit in signed int index");
	}
	
	public int length(){
		return (int)(data.siz()>>>4);
	}
	
	public char charAt(int index){
		return data.charAt( ((long)index) << 4 );
	}
    
	/** This software is designed primarily for efficient substrings of bits in realtime avl tree rotations,
	so this function costs log time and memory without holding a pointer to this object,
	so it will not cause garbage-collector problems of the original usually larger (this) CharSequence
	or the bits object its a view of.
	*/
	public CharSequence subSequence(int start, int endExclusive){
		long bitIndexStart = ((long)start) << 4;
		long bitIndexEndExclusive = ((long)endExclusive) << 4;
		return new BitsAsCharSequence(data.sub(bitIndexStart, bitIndexEndExclusive));
	}

	public String toString(){
		if(data.siz() > 0x1000000) System.out.println(
			"About to toString a "+getClass().getName()+" of length exceeding a megachar "+length());
		char c[] = new char[length()];
		for(int i=0; i<c.length; i++) c[i] = charAt(i);
		return new String(c);
	}

}