/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.datastruct;


import smartblob.wavetree.bit.Bits;

/** Used with BitsVar when try to write a Bits that doesnt contain a valid state,
like if a specific data format was expected but you gave random bits.
*/
public class CantWriteBitsState extends RuntimeException{
	
	public final BitsVar object;
	
	public Bits triedToWriteState;
	
	public CantWriteBitsState(BitsVar object, Bits triedToWriteState){
		this.object = object;
		this.triedToWriteState = triedToWriteState;
	}
	
	public CantWriteBitsState(BitsVar object, Bits triedToWriteState, String message){
		super(message);
		this.object = object;
		this.triedToWriteState = triedToWriteState;
	}

}
