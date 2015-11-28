/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.blobs.layeredzigzag;

public abstract class Adjacent{
	//TODO rename this AdjacentCornersAndTris to AdjacentsTriAndCorner
	
	/** starts full of nulls. Size is for all adjacent to fit.
	TODO first 2 adjacent corners are in same layer.
	*/
	public final CornerData adjacentCorners[];

	public final LineData adjacentLines[];

	/** starts full of nulls. Size is for all adjacent to fit. */
	public final TriData adjacentTris[];

	/** fills in the adjacentCorners and adjacentTris arrays which start as containing nulls */
	public abstract void connectAdjacent();

	public Adjacent(int adjacentTris, int adjacentLines, int adjacentCorners){
		this.adjacentTris = new TriData[adjacentTris];
		this.adjacentLines = new LineData[adjacentLines];
		this.adjacentCorners = new CornerData[adjacentCorners];
	}

}
