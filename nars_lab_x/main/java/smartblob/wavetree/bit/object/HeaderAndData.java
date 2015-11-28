package smartblob.wavetree.bit.object;

import smartblob.wavetree.bit.Bits;

public interface HeaderAndData{
	
	public Bits data();

	/** may be empty */
	public Bits header();
	
	public Bits headerThenData();

}
