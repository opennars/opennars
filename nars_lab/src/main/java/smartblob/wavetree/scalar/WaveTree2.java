/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.scalar;

/** same as WaveTree1 except has 2 values, for stereo sound
*/
public final class WaveTree2{

	public final double value1;

	public final double value2;

	public final double size;

	public final WaveTree2 left;

	public final WaveTree2 right;

	public final byte depth;

	public final byte maxDepthDiff;

	private int hash;

	public WaveTree2(WaveTree2 left, WaveTree2 right){
		this.left = left;
		this.right = right;
		size = left.size + right.size;
		value1 = (left.size*left.value1 + right.size*right.value1)/size;
		value2 = (left.size*left.value2 + right.size*right.value2)/size;
		depth = (byte)(left.depth<right.depth ? right.depth+1 : left.depth+1);
		byte newMaxDepthDiff = (byte)(right.depth-left.depth);
		if(newMaxDepthDiff < 0) newMaxDepthDiff = (byte)-newMaxDepthDiff;
		if(newMaxDepthDiff < left.maxDepthDiff) newMaxDepthDiff = left.maxDepthDiff;
		if(newMaxDepthDiff < right.maxDepthDiff) newMaxDepthDiff = right.maxDepthDiff;
		maxDepthDiff = newMaxDepthDiff;
	}

	public WaveTree2(double value, double value2, double size){
		left = right = null;
		this.value1 = value;
		this.value2 = value2;
		this.size = size;
		depth = maxDepthDiff = 0;
	}

	public int hashCode(){
		throw new RuntimeException();
	}

	public boolean equals(Object o){
		throw new RuntimeException();
	}

	public final boolean isLeaf(){
		return depth == 0;
	}

	public String toString(){
		if(isLeaf()){
			return "{"+value1+"+"+value2+"@"+size+"}";
		}else{
			return "["+value1+"+"+value2+"@"+size+left+right+"]";
		}
	}

}

