/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit;

//import common.Nanotimer;

/** Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are
<br><br>
For anything 16 bits or smaller, use Fast0To16Bits
*/
public class BitsUtil{
	
	/** TODO create a kind of Bits that holds an int.
	TODO use a LongBits for this, which will be for anything from 16-64 bits.
	*/	
	public static Bits intToBits(int i){
		short high = (short)(i>>>16);
		short low = (short)(i&0xffff);
		//Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are
		return Fast0To16Bits.get(high).cat(Fast0To16Bits.get(low));
		//OLD: littleEndian is standard for this software
		//return Fast0To16Bits.get(low).cat(Fast0To16Bits.get(high));
	}
	
	/** TODO create a kind of Bits that holds a long.
	TODO use a LongBits for this, which will be for anything from 16-64 bits.
	*/
	public static Bits longToBits(long g){
		int high = (int)(g>>>32);
		//int low = (int)(g&0xffffffff);
		int low = (int)g;
		//Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are
		return intToBits(high).cat(intToBits(low));
		//OLD: littleEndian is standard for this software
		//return intToBits(low).cat(intToBits(high));
	}
	
	/** TODO create a kind of Bits that holds a long */
	public static Bits doubleToBits(double d){
		return longToBits(Double.doubleToRawLongBits(d));
	}
	
	/** TODO create a kind of Bits that holds an int */	
	public static Bits floatToBits(float f){
		return intToBits(Float.floatToRawIntBits(f));
	}
	
	/** TODO use wavetree.bit.object.ByteArrayUntilSplit when that code is finished. For now just concat the bytes.
	As usual, AVL tree rotations prevent it from becoming slow or deep tree, but it could be optimized better
	if we know its a byte array and can be represented internally as a Fast0To16Bits array.
	*/
	public static Bits bytesToBits(byte b[]){
		/* TODO If know the height, can concat in an order that gets minimum tree height. Always use powers of 2.
		byte h = 0;
		while(1 < blocksOf16){
			blocksOf16 >>>= 1;
			h++;
		}*/
		Bits bits = Fast0To16Bits.EMPTY;
		//Nanotimer t = new Nanotimer();
		int count = 0;
		for(byte nextByte : b){
			bits = bits.cat(Fast0To16Bits.get(nextByte));
			count++;
			/*if((count%10000)==0){
				double duration = t.secondsSinceLastCall();
				System.out.println("bytesToBits Next 10kB took "+duration+" seconds.");
			}*/
		}
		return bits;
		//FIXME for speed, If know the height, can concat in an order that gets minimum tree height. Always use powers of 2. It wont solve the general speed problems when we dont know what will be concat later, but we can at least get bits of byte array much faster, and that is a common thing to do.
	}

}