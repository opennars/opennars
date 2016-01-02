/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.blobs.layeredzigzag;

/** Mutable data about a specific triangle in a specific polygon mesh
of shape the same as LayeredZigzag.
<br><br>
The first 2 triangles are in the same trianglelayer
(different layers but same distance outward because of inward/outward boolean).
The other is either 1 layer higher or lower.
This array is either size 2 (if edge) or 3.
All adjacent Tri are opposite of inward/outward.
*/
public class TriData extends Adjacent {

	public final LayeredZigzag smartblob;
	
	public final Tri tri;
	
	public float volume;
	
	/** Volume mostly flows between adjacent triangles so total volume is conserved
	and it helps to flow force through the smartblob.
	*/
	public float targetVolume;
	
	//protected Shape cachedShape;
	//"TODO use int x[] and y[] instead of Shape? In LayeredZigzag outer shape? At least to replace TriData.cachedShape, or actually let that be replaced by each TriData knowing its 3 CornerData and let the display code get it from there."
	
	protected long cachedInWhatCycle = -1;
	
	/** If null, use the default color depending on tri.inward and the LayeredZigzag.
	This can be used to display pressure, selection by mouse, or other things that happen on screen.
	*/
	public Color colorOrNull;
	
	/** If edge, there are 2 adjacent Tri, else 3.
	Those TriData are filled in later by caller, so that array contains nulls until then.
	*/
	public TriData(LayeredZigzag smartblob, Tri tri, boolean edge){
		super(edge?2:3, 3, 3);
		this.smartblob = smartblob;
		this.tri = tri;
		//adjacentTris = new TriData[edge ? 2 : 3];
	}


	/** First LineData in TriData is at same layer as first 2 corners */
	public void connectAdjacent(){
		//TODO pointers to TriData
		
		//pointers to CornerData works
		adjacentCorners[0] = smartblob.corners[tri.layer][tri.point];
		adjacentCorners[1] = smartblob.corners[tri.layer][(tri.point+1)%smartblob.layerSize];
		boolean layerIsOdd = (tri.layer&1)==1;
		int pInOtherLayer = layerIsOdd ? (tri.point+1)%smartblob.layerSize : tri.point;
		adjacentCorners[2] = tri.inward
			? smartblob.corners[tri.layer-1][pInOtherLayer]
			: smartblob.corners[tri.layer+1][pInOtherLayer];
			
			
		//TODO pointers to LineData
	}
	
	//"TODO qnSmartblobLineObject"
	
	/** Returns from cache if same LayeredZigzag.cycle() as last call *
	public Shape triangle(){
		long cycleNow = smartblob.cycle();
		if(cachedInWhatCycle != cycleNow){
			cachedShape = newTriangle();
			cachedInWhatCycle = cycleNow;
		}
		return cachedShape;
	}*/
	
	/** THIS FUNC IS BEING REPLACED BY 3 CornerData knowing their positions.
	layer and pointInLayer define the first corner of the triangle.
	The second point is in the same layer and wrapping counterclockwise up.
	The third point is in the next lower or higher layer.
	Odd layers have a half point angle offset when in balanced circle view,
	so every pair of adjacent points
	in a layer are touching 1 specific point in next lower and higher layers.
	*
	protected Shape newTriangle(){
		final float y[][] = smartblob.y, x[][] = smartblob.x;
		final int layer = tri.layer, point = tri.point,
			layers = smartblob.layers, layerSize = smartblob.layerSize; 
		if(tri.inward){
			//TODO merge duplicate code
			if(layer < 1) throw new IndexOutOfBoundsException("Inward from layer "+layer);
			boolean layerIsOdd = (layer&1)==1;
			int pointInOtherLayer = layerIsOdd ? (tri.point+1)%smartblob.layerSize : point;
			int wrapPointInLayer = (point+1)%layerSize;
			int yi[] = new int[]{
				(int)y[layer][point],
				(int)y[layer][wrapPointInLayer],
				(int)y[layer-1][pointInOtherLayer]
			};
			int xi[] = new int[]{
				(int)x[layer][point],
				(int)x[layer][wrapPointInLayer],
				(int)x[layer-1][pointInOtherLayer]
			};
			return new Polygon(xi, yi, 3);
		}else{ //outward
			//TODO merge duplicate code
			if(layers <= layer) throw new IndexOutOfBoundsException("Outward from layer "+layer);
			boolean layerIsOdd = (layer&1)==1;
			int pointInOtherLayer = layerIsOdd ? (point+1)%layerSize : point;
			int wrapPointInLayer = (point+1)%layerSize;
			int yi[] = new int[]{
				(int)y[layer][point],
				(int)y[layer][wrapPointInLayer],
				(int)y[layer+1][pointInOtherLayer]
			};
			int xi[] = new int[]{
				(int)x[layer][point],
				(int)x[layer][wrapPointInLayer],
				(int)x[layer+1][pointInOtherLayer]
			};
			return new Polygon(xi, yi, 3);
		}
	}*/

}
