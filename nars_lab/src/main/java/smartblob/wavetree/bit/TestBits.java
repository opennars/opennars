/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit;
import java.util.Comparator;

/** Update 2015-5 changing to bigEndian since thats the way people think and multidimensional arrays are
Make sure there are tests to verify its bigEndian.
*/
public class TestBits{
	
	/** For efficiency, considering Bits can be up to size nearly 2^64
	without literally storing all that data (lots of copy/pastes of ranges),
	the design of Bits is equals uses ==. This Comparator is by length
	then by content as if they were integers made by adding a high bit1.
	*/
	public static Comparator<Bits> compare = new Comparator<Bits>(){
		public int compare(Bits x, Bits y){
			long xs = x.siz(), ys = y.siz();
			if(xs < ys) return -1;
			if(xs > ys) return 1;
			for(long i=xs-1; i>-0; i--){
				boolean xBit = x.bitAt(i);
				boolean yBit = y.bitAt(i);
				if(xBit != yBit){
					return xBit ? 1 : -1;
				}
			}
			return 0;
		}
	};
	
	public static String toString(Bits b){
		if(b.siz() > Integer.MAX_VALUE) throw new IndexOutOfBoundsException("Not fit in int "+b.siz());
		char c[] = new char[(int)b.siz()];
		for(int i=0; i<c.length; i++){
			c[i] = b.bitAt(i) ? '1' : '0';
		}
		return new String(c);
	}
	
	public static String toString(Object o){
		if(o instanceof Bits) return toString((Bits)o);
		return ""+o;
	}
	
	public static void verifyClassIs(Object o, Class correctClass){
		if(!correctClass.isAssignableFrom(o.getClass())) throw new RuntimeException(
			"Expected "+correctClass+" but got "+o.getClass()+" in object "+toString(o));
	}
	
	public static void verifyBitContent(Bits b, String correctContent){
		String bContent = toString(b);
		if(!bContent.equals(correctContent)) throw new RuntimeException(
			"Expected content:\r\n"+correctContent+"\r\nbut got:\r\n"+bContent);
	}
	
	public static void verifyHeightIs(Bits b, int correctHeight){
		if(b.height() != correctHeight) throw new RuntimeException(
			"Expected height "+correctHeight+" but height is "+b.height()+" in "+toString(b));
	}
	
	public static void verifyIndexOfNthOne(Bits b, long n, long correctIndex){
		long index = b.indexOfNthOne(n);
		if(index != correctIndex) throw new RuntimeException(
			"verifyIndexOfNthOne n="+n+" Expected index "+correctIndex+" but got index "+index+" in "+toString(b));
	}
	
	public static void verifyIndexOfNthZero(Bits b, long n, long correctIndex){
		long index = b.indexOfNthZero(n);
		if(index != correctIndex) throw new RuntimeException(
			"verifyIndexOfNthZero n="+n+" Expected index "+correctIndex+" but got index "+index+" in "+toString(b));
	}
	
	public static void verifySize(Bits b, long correctSize){
		if(b.siz() != correctSize) throw new RuntimeException(
			"Expected size "+correctSize+" but got "+b.siz()+" in "+toString(b));
	}
	
	public static void main(String args[]){
		System.out.println(TestBits.class+" starting tests...");
		Bits F = Fast0To16Bits.FALSE;
		Bits T = Fast0To16Bits.TRUE;
		Bits empty = Fast0To16Bits.EMPTY;
		//TODO verify empty.cat(anyBits) and anyBits.cat(empty) both == anyBits,
		//because otherwise tree could get too deep by continuing to concat empty
		Bits TF = T.cat(F);
		verifyClassIs(TF, Fast0To16Bits.class);
		Bits TFTF = TF.cat(TF);
		Bits F2 = F.cat(F);
		Bits F4 = F2.cat(F2);
		Bits F8 = F4.cat(F4);
		Bits F16 = F8.cat(F8);
		verifyBitContent(F16, "0000000000000000");
		verifyClassIs(F16,Fast0To16Bits.class);
		Bits F16T = F16.cat(T);
		verifyClassIs(F16T, AvlBitstring.class);
		verifyBitContent(F16T, "00000000000000001");
		verifyIndexOfNthOne(F16T, 0, 16);
		verifyHeightIs(F16T, 1);
		Bits TF16T = T.cat(F16T);
		verifyBitContent(TF16T, "100000000000000001");
		verifyIndexOfNthOne(TF16T, 0, 0);
		verifyIndexOfNthOne(TF16T, 1, 17);
		verifyIndexOfNthZero(TF16T, 0, 1);
		verifyIndexOfNthZero(TF16T, 15, 16);
		verifyHeightIs(TF16T, 2);
		Bits F32 = new AllZero(32);
		Bits T32 = new AllOne(32);
		Bits T32TF16T = T32.cat(TF16T);
		verifyBitContent(T32TF16T, "11111111111111111111111111111111100000000000000001");
		Bits FT32TF16T = F.cat(T32TF16T);
		verifySize(FT32TF16T, 51);
		verifyBitContent(FT32TF16T, "011111111111111111111111111111111100000000000000001");
		Bits T17 = FT32TF16T.sub(1,18);
		verifySize(T17, 17);
		Bits T17FT32TF16T = T17.cat(FT32TF16T);
		String T17FT32TF16TCorrect = "11111111111111111011111111111111111111111111111111100000000000000001";
		verifyBitContent(T17FT32TF16T, T17FT32TF16TCorrect);
		T17FT32TF16T = T17FT32TF16T.balanceTree();
		verifyBitContent(T17FT32TF16T, T17FT32TF16TCorrect);
		Bits F32F = F32.cat(F);
		verifyClassIs(F32F, AllZero.class);
		verifyHeightIs(F32F, 0);
		Bits FF32 = F.cat(F32); //Since F is a Fast0To16Bits its not expected to check if all bits equal
		verifyClassIs(FF32, AvlBitstring.class);
		verifyHeightIs(FF32, 1);
		Bits F32FT = F32F.cat(T);
		verifyBitContent(F32FT, "0000000000000000000000000000000001");
		verifyClassIs(F32FT, AvlBitstring.class);
		
		//verify bigEndian, at least in int case (TODO more tests)
		Bits thirtyTwoBits = T17FT32TF16T.sub(3, 35);
		//String correctThirtyTwoBits = "11111111111111011111111111111111";
		int correctThirtyTwoBits = (int)Long.parseLong("11111111111111011111111111111111",2);
		int thirtyTwoBitsIntAt0 = thirtyTwoBits.intAt(0);
		int T17FT32TF16TIntAt3 = T17FT32TF16T.intAt(3);
		if(thirtyTwoBitsIntAt0 != correctThirtyTwoBits) throw new RuntimeException(
			Integer.toString(thirtyTwoBitsIntAt0,2)+" == thirtyTwoBitsIntAt0 != correctThirtyTwoBits == "
			+Integer.toString(correctThirtyTwoBits,2));
		if(T17FT32TF16TIntAt3 != correctThirtyTwoBits) throw new RuntimeException(
			Integer.toString(T17FT32TF16TIntAt3,2)+" == T17FT32TF16TIntAt3 != correctThirtyTwoBits == "
			+Integer.toString(correctThirtyTwoBits,2));
		
		
		//Next test avl tree rotation a variety of ways...
		
		Fast0To16Bits.main(new String[0]); //test that class
		
		AvlBitstring.main(new String[0]); //test that class
		
		//TODO before filling in code wherever throw TODO is in code, write test here for it
		//throw new RuntimeException("Need more tests");
		
	}

}
