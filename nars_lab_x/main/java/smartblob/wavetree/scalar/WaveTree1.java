/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.scalar;

/** DESCRIPTION FOR THIS TREE AS A SEPARATE SOFTWARE:
Buffer 64 bit floating point index and values designed for multi streaming
wave data scattered across times, like microseconds since year 1970.
Average amplitude of any time range takes log time.
Immutable shared subwaves. AVL tree balancing. Pure Java
<br><br>
An immutable binary-tree where each node has floating point (D) len
and floating point (D) value1, and all useful information (except for roundoff error)
is in the leafs, assuming you know their order.
All trees with equal leafs and order of leafs are equal, ignoring the nonleafs.
The tree represents a range from 0 to 'len', where each position in that range has a 'value1'.
You can interpret the tree as flowing smoothly from leaf to leaf or as a step function.
*/
public final class WaveTree1{

	/** value1 of this leaf or weighted (by len) value1 of all leafs recursively */
	public final double value1;

	/** For example, the second audio channel amplitude *
	public final double value2;
	*/

	/** length of this leaf or len of all childs recursively. Must be positive. */
	public final double len;

	/** null if this WaveTree1 is a leaf */
	public final WaveTree1 left;

	/** null if and only if left is null */
	public final WaveTree1 right;

	/** Leafs are height 0. WaveTree1Ops with leafs as childs are height 1. Grandparents height 2...
	height should not be allowed to exceed approximately 10 to 30.
	FIXME: how can I prove height hasnt rolled over byte range?
	TODO: should height be a byte or short? Probably byte
	*/
	public final byte height;

	/** For all childs recursively, what is the maximum difference between left and right heights?
	If maxheightDiff is 0 or 1, then this is an AVL tree.
	TODO: remove this avl variable if possible. It slows things down a little.
	*/
	public final byte maxheightDiff;

	/** if this is 0, when hashCode() is called, it must be set to a nonzero value1 */
	private int hash;

	public static byte maxHeight = 0;

	/** not a leaf. left and right must both be nonnull. */
	public WaveTree1(WaveTree1 left, WaveTree1 right){
		//instanceCount++;
		this.left = left;
		this.right = right;
		len = left.len + right.len;
		//FIXME: what if len is 0? value1 becomes NaN
		//value1 = (left.len*left.value1 + right.len*right.value1)/len;
		value1 = len>0 ? (left.len*left.value1 + right.len*right.value1)/len : left.value1;
		height = (byte)(left.height<right.height ? right.height+1 : left.height+1);
		byte newMaxheightDiff = (byte)(right.height-left.height);
		if(newMaxheightDiff < 0) newMaxheightDiff = (byte)-newMaxheightDiff;
		if(newMaxheightDiff < left.maxheightDiff) newMaxheightDiff = left.maxheightDiff;
		if(newMaxheightDiff < right.maxheightDiff) newMaxheightDiff = right.maxheightDiff;
		maxheightDiff = newMaxheightDiff;
		if(maxHeight < height) maxHeight = height; //FIXME: remove this testing line
	}

	/** leaf */
	public WaveTree1(double value, double len){
		//instanceCount++;
		left = right = null;
		this.value1 = value;
		this.len = len;
		//if(len <= 0) throw new NonpositiveWaveLen(len); //FIXME: is there any way to avoid this expensive compare? I could set value1 to left.value1 if len is 0.
		height = maxheightDiff = 0;
	}

	//TODO: instead of using java.util.Map and hashCode(), use a SortedSet of SortedSet of WeakReference to WaveTree1
	//Sort one SortedSet by WaveTree1.len, which returns a SortedSet containing all WaveTree1Ops of that len sorted by WaveTree1.value1,
	//and so on for the other attributes.
	//Probably faster to use 1 SortedSet that uses a Comparator that compares all those attributes,
	//and goes to the next attribute only if a tie occurs.

	public int hashCode(){
		//TODO: use secure hash algorithm or use an object to do the hashing
		if(hash == 0){
			double d = len*value1;
			if(d==0){
				if(left==null){
					hash = (int) Double.doubleToRawLongBits(len+value1);
					if(hash==0) hash = Integer.MIN_VALUE+7;
				}else{
					hash = -17*left.hashCode() + 49999*right.hashCode()
						+ (int) Double.doubleToRawLongBits(d);
					if(hash==0) hash = Integer.MAX_VALUE-7;
				}
			}else{
				if(isLeaf()){
					hash = (int) Double.doubleToRawLongBits(d);
					if(hash==0) hash = (int) Double.doubleToRawLongBits(len+value1);
					if(hash==0) hash = (int) Double.doubleToRawLongBits(len);
					if(hash==0) hash = (int) Double.doubleToRawLongBits(value1);
					if(hash==0) hash = Integer.MIN_VALUE;
				}else{
					hash = 17*left.hashCode() - 49999*right.hashCode()
						+ (int) Double.doubleToRawLongBits(d);
					if(hash==0) hash = 19*left.hashCode() - 37*right.hashCode()
						+ (int) Double.doubleToRawLongBits(d+Math.PI);
					if(hash==0) hash = Integer.MAX_VALUE;
				}
			}
		}
		return hash;
	}

	public boolean equals(Object o){
		if(!(o instanceof WaveTree1)) return false;
		WaveTree1 dt = (WaveTree1) o;
		if(left==null){
			return dt.hash==hash && dt.len==len && dt.value1==value1;
		}else{
			return dt.hash==hash && dt.len==len && dt.value1==value1
				&& left.equals(dt.left) && right.equals(dt.right);
		}
	}

	public final boolean isLeaf(){
		return height == 0;
	}

	public String toString(){
		if(isLeaf()){
			return "{"+value1+"@"+len+"}";
		}else{
			return "["+value1+"@"+len+left+right+"]";
		}
	}

}

