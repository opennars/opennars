/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.blobs.layeredzigzag;

/** A corner shared by up to 6 triangles, a point with mutable x and y.
Corners are equal if their layer and point equal.
*/
public final class Corner{
	
	public final int layer, point;
	
	protected final int hash;
	public int hashCode(){ return hash; }
	
	public Corner(int layer, int point){
		this.layer = layer;
		this.point = point;
		int h = point*3;
		h += layer<<15;
		hash = h;
	}
	
	public boolean equals(Object o){
		if(o == this) return true;
		if(!(o instanceof Corner)) return false;
		Corner t = (Corner)o;
		return point==t.point && layer==t.layer;
	}

}
