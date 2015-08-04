/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.scalar;

/** An immutable wave with 1 or more channels,
backed by trees of more efficient objects.
They are more efficient because their classes can be final
and they have a constant quantity of channels.
Of course, since this is an interface, that can not be enforced.
<br><br>
For efficiency, no function of this class is required to throw an Exception
if parameters are invalid (like a channel number that does not exist).
It may, for example, return data for a valid channel, or random data.
<br><br>
If you combine incompatible types of Wave, a WaveCastException should be thrown,
but this will probably only happen in functions that have a Wave parameter.
*/
public interface Wave{

	/** balances the tree that backs this Wave. You dont need to call this,
	because the tree is automatically balanced after some small number of modifications,
	but you may want it to be balanced immediately.
	*/
	public Wave balanceTree();

	/** adjacent audio samples with amplitudes that are almost the same number
	will be merged into bigger audio samples with amplitude averaged
	weighted by len of audio samples
	*
	public Wave mergeCloseAmplitudes(double maxAmplitudeDiff);
	*/

	/** if the audio amplitude changes quickly, those many small sections
	can be averaged into a smaller number of bigger sections.
	Only sections whose individual len() is at most maxLenToMerge can be merged.
	*
	public Wave mergeShortSections(double maxLenToMerge);
	*/

	public int chan();

	/** position ranges from 0 to len() inclusive */
	public double len();

	/** average amplitude of the whole wave */
	public double aveAmp();

	/** average amplitude of the whole wave but only for channel */
	public double aveAmp(int channel);

	/** average amplitude of all channels at position */
	public double amp(double position);

	/** amplitude at specific position and channel */
	public double amp(double position, int channel);

	/** returns a Wave with only 1 channel and the same data as 1 of my channels */
	public Wave chan(int channelToGet);

	public Wave sub(double positionStart, double positionEnd);

	public Wave left(double cutPosition);

	public Wave right(double cutPosition);

	public Wave concat(Wave w) throws WaveCastException;

	public Wave insert(Wave w, double position);

	public Wave overwrite(Wave w, double position);

	public Wave delete(double positionStart, double positionEnd);

	public Wave reverse();

	/** like String.intern().
	For any equal Waves x and y, x.intern()==y.intern().
	*/
	public Wave intern();

	/** May return null for any reason, or returns the immutable tree object that this Wave wraps.
	*/
	public Object unwrap();

}
