/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit.object;


import smartblob.wavetree.bit.Bits;

/** Multidim is to multiply as multicat is to plus.
Like struct in C progLang, multicat is cat of things of different types andOr sizes.
Multicat has header similar to multidim, except sizes are added instead of multiplied.
<br><br>
TODO important to answer benfrayfieldResearch.qnMultidimAndMulticatUseLongOrIntIndexs fast:
Should sizes be long or int? Should multidim be same of long or int? Multidim is int,
but I want multicat to be long, so should multidim be long too?
ANSWER: I choose long for both. Multicat needs long because avlBitstring is designed to
allow sparse, and may want a voxel dim or multichannel sound (as allOne vs allZero to
represent scalars like pascalstriZigzag) bigger than int range.
*/
public interface Polycat extends HeaderAndData{
	
	/** total bits */
	public long size();
	
	/** bits of a part */
	public long partSize(long partIndex);
	
	/** how many parts? */
	public long parts();
	
	public Bits part(long partIndex);
	/** TODO benfrayfieldResearch.qnPolycatHeaderCumulativeSize says:
	I choose cumulative sizes. ... Should polycat header be cumulative sizes
	or size of each part? Its the same header size either way. Cumulative sizes
	make binarySearching the header faster.
	<br><br>
	long howManyParts, then a long for each part's size.
	Doesnt include anything earlier in a wrapping header which may say that this is a multicat,
	like common.DatastructHeaders.MULTICATBITARRAY.
	*/
	public Bits header();
	
	/** cat of all parts */
	public Bits data();
	
	/** Same as header().cat(data()) which is efficient but this could be a little faster and less memory.
	The purpose of this function is to give Multicat implementations a way to, at their option,
	avoid duplicating that, even if it is only a small constant cost to create a parent of both of those. 
	*/
	public Bits headerThenData();

}