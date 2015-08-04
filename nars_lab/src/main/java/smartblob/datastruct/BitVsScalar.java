/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.datastruct;

public interface BitVsScalar{
	
	/** Bits and scalars are castable to eachother in this software.
	Scalar to bit uses at least 0 as true. Bit to scalar is always -1 or 1.
	If a Vectoraddr has scalar data, even if its only 2 or more bits of accuracy,
	then prefer scalars. If only 1 bit of accuracy, prefer bits.
	<br><br>
	Throws if not hasDatastructType(d)
	*/
	public boolean preferBitsOverScalars(DatastructCategory d);
	
	public boolean hasDatastructCategory(DatastructCategory d);

}
