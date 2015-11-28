/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.scalar;

/** A Wave with 1 audio channel.
All functions that return Wave return an instance of this class.
*/
public class DefaultWave1 implements Wave{

	public final WaveTree1 waveTree;

	public DefaultWave1(WaveTree1 wt){
		waveTree = wt;
	}

	public Object unwrap(){
		return waveTree;
	}

	public Wave balanceTree(){
		return new DefaultWave1(WaveTree1Ops.balance(waveTree,(byte)1));
	}

	public int chan(){
		return 1;
	}

	public double len(){
		return waveTree.len;
	}

	public double aveAmp(){
		return waveTree.value1;
	}

	public double aveAmp(int channel){
		return waveTree.value1;
	}

	public double amp(double position){
		return WaveTree1Ops.valueAt(waveTree,position);
	}

	public double amp(double position, int channel){
		return WaveTree1Ops.valueAt(waveTree,position);
	}

	public Wave chan(int channelToGet){
		return this;
	}

	public Wave sub(double positionStart, double positionEnd){
		return new DefaultWave1(WaveTree1Ops.subtree(waveTree, positionStart, positionEnd));
	}

	public Wave left(double cutPosition){
		return new DefaultWave1(WaveTree1Ops.left(waveTree,cutPosition));
	}

	public Wave right(double cutPosition){
		return new DefaultWave1(WaveTree1Ops.right(waveTree,cutPosition));
	}

	public Wave concat(Wave w) throws WaveCastException{
		return new DefaultWave1(WaveTree1Ops.concat(waveTree,getWaveTree1(w)));
	}

	public Wave insert(Wave w, double position){
		return new DefaultWave1(WaveTree1Ops.insert(waveTree,getWaveTree1(w),position));
	}

	public Wave overwrite(Wave w, double position){
		return new DefaultWave1(WaveTree1Ops.overwrite(waveTree,getWaveTree1(w),position));
	}

	public Wave delete(double positionStart, double positionEnd){
		return new DefaultWave1(WaveTree1Ops.delete(waveTree,positionStart,positionEnd));
	}

	public Wave reverse(){
		return new DefaultWave1(WaveTree1Ops.reverse(waveTree));
	}

	public Wave intern(){
		throw new RuntimeException("code not finished");
	}

	protected WaveTree1 getWaveTree1(Wave w){
		//TODO: be compatible with more types of Wave
		try{
			return ((DefaultWave1)w).waveTree;
		}catch(ClassCastException e){
			throw new WaveCastException(w,DefaultWave1.class);
		}
	}

}
