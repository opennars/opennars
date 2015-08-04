/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.datastruct;


import smartblob.wavetree.bit.Bits;

/** An object whose entire internal state can be read and written as Bits object,
but it may reject (TODO throws or returns false?) any Bits which are not a valid state.
Any Bits read can always be later written. If you understand what kind of object it is
and how its internal state works, you can edit the Bits externally and write them.
*/
public interface BitsVar{
	
	public Bits readState();
	
	public void writeState(Bits state) throws CantWriteBitsState;
	
	/** If true, state can change between calls of writeState(Bits).
	If false, then this is a simple Bits variable that only stores the bits
	and may have functions to view the bits other ways but not to write them
	or change what the object does in any way since only writeState(Bits) could.
	*/
	public boolean stateCanChangeBetweenWriteState();

}
